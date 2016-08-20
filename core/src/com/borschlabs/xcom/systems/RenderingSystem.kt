package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.RouteComponent
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.draw
import com.borschlabs.xcom.geometry.GeomUtils
import com.borschlabs.xcom.geometry.Poly
import com.borschlabs.xcom.geometry.VisibleMapBuilder
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.GameUnitTurnArea
import java.util.*

/**
 * @author octopussy
 */

class RenderingSystem(val camera: OrthographicCamera, val tiledMap: TiledMap, val field: Field) : EntitySystem() {

    private val cellSize = (tiledMap.layers[0] as TiledMapTileLayer).tileWidth

    private val TURN_AREA_COLOR = Color(0f, 0f, 1f, 0.2f)
    private val ROUTE_COLOR = Color(0f, 1f, 1f, 0.6f)

    private val batch: SpriteBatch = SpriteBatch()

    private val debugShapeRenderer: ShapeRenderer = ShapeRenderer()

    private val tiledMapRenderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var visibleObjects: ImmutableArray<Entity> = ImmutableArray(Array())
    private var units: ImmutableArray<Entity> = ImmutableArray(Array())
    private var routes: ImmutableArray<Entity> = ImmutableArray(Array())

    override fun addedToEngine(engine: Engine) {
        visibleObjects = engine.getEntitiesFor(Family.all(TextureComponent::class.java, TransformComponent::class.java).get())
        units = engine.getEntitiesFor(Family.all(GameUnitComponent::class.java).get())
        routes = engine.getEntitiesFor(Family.all(RouteComponent::class.java).get())
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
        debugShapeRenderer.dispose()
        tiledMapRenderer.dispose()
    }

    override fun update(deltaTime: Float) {
        camera.update()

        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        debugShapeRenderer.projectionMatrix = camera.combined

        drawTurnAreas()

        batch.projectionMatrix = camera.combined
        batch.begin()

        drawVisibleObjects()

        batch.end()

        drawRoutes()

        drawVisMap()
    }

    private fun drawVisibleObjects() {
        disableBlending()

        for (obj in visibleObjects) {
            val transform = Mappers.TRANSFORM.get(obj)
            val texture = Mappers.TEXTURE.get(obj)
            batch.draw(texture.region, transform.pos.x, transform.pos.y)
        }
    }

    private fun drawTurnAreas() {
        enableBlending()
        for (e in units) {
            val unit = Mappers.GAME_UNIT.get(e)
            if (unit.state == GameUnitComponent.Companion.State.IDLE) {
                drawTurnArea(unit.turnArea)
            }
        }
    }

    private fun drawRoutes() {
        enableBlending()
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugShapeRenderer.color = ROUTE_COLOR
        for (r in routes) {
            val routeComp = Mappers.ROUTE.get(r)
            val cells = routeComp.route.cells
            if (cells.size > 1) {
                var from = cells[0]
                var i = 1
                while (i < cells.size) {
                    drawRouteSeg(from, cells[i])
                    from = cells[i]
                    ++i
                }
            }

        }

        debugShapeRenderer.end()
    }

    fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
    }

    private fun drawTurnArea(turnArea: GameUnitTurnArea) {
        turnArea.reachableCells.forEach { fillCell(it.x, it.y, TURN_AREA_COLOR) }
    }

    private fun fillCell(cellX: Int, cellY: Int, color: Color) {
        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Filled, color) {
            rect(cellX.toFloat() * cellSize, cellY.toFloat() * cellSize, cellSize, cellSize)
        }
    }

    private fun drawRouteSeg(from: FieldCell, to: FieldCell) {
        debugShapeRenderer.line(
                from.x * cellSize + cellSize / 2.0f,
                from.y * cellSize + cellSize / 2.0f,
                to.x * cellSize + cellSize / 2.0f,
                to.y * cellSize + cellSize / 2.0f)
    }

    private fun enableBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun disableBlending() {
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private var bl: Vector3 = Vector3.Zero
    private var tl: Vector3 = Vector3.Zero
    private var tr: Vector3 = Vector3.Zero
    private var br: Vector3 = Vector3.Zero

    private var visMapVertices: FloatArray = FloatArray(16)
    private var bounds: Poly = Poly()
    private val geometry = ArrayList<Poly>()
    private var walls: MutableList<Poly.Wall> = mutableListOf()
    private var visibleMapBuilder: VisibleMapBuilder = VisibleMapBuilder(debugShapeRenderer)

    private fun updateBounds() {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        bl = camera.unproject(Vector3(0f, height, 0f))
        tl = camera.unproject(Vector3(0f, 0f, 0f))
        tr = camera.unproject(Vector3(width, 0f, 0f))
        br = camera.unproject(Vector3(width, height, 0f))

        bounds = Poly(Vector2(bl.x, bl.y), Vector2(tl.x, tl.y),
                Vector2(tr.x, tr.y), Vector2(br.x, br.y))

        //floorMesh.setVertices(floatArrayOf(-1000f, -1000f, 0f, 0f, -1000f, 1000f, 0f, 100f, 1000f, 1000f, 100f, 100f, 1000f, -1000f, 100f, 0f))
    }

    // TODO: implement pool
    private fun drawVisMap() {
        updateBounds()

        val player = engine.getSystem(CoreSystem::class.java).getPlayer() ?: return
        val playerPosition = Mappers.GAME_UNIT.get(player).pos.cpy()

        playerPosition.x += cellSize / 2.0f
        playerPosition.y += cellSize / 2.0f

        val playerPos2 = Vector2(playerPosition.x, playerPosition.y)

        val cellSize = field.cellSize

        geometry.clear()
        walls = ArrayList<Poly.Wall>()

        val visibleWalls = field.getVisibleWalls(playerPosition, 128.0f)
        walls.addAll(visibleWalls)

        for (c in visibleWalls) {
            /*Vector2 lb = new Vector2(c.x * CELL_SIZE, c.y * CELL_SIZE);
            Vector2 rb = new Vector2((c.x + 1f) * CELL_SIZE, c.y * CELL_SIZE);
            Vector2 rt = new Vector2((c.x + 1f) * CELL_SIZE, (c.y + 1f) * CELL_SIZE);
            Vector2 lt = new Vector2(c.x * CELL_SIZE, (c.y + 1f) * CELL_SIZE);

            walls.add(new Poly.Wall(lb, rb));
            walls.add(new Poly.Wall(rb, rt));
            walls.add(new Poly.Wall(rt, lt));
            walls.add(new Poly.Wall(lt, lb));*/
            /*addWall(c.x, c.y, c.x, c.y + 1)
            addWall(c.x, c.y + 1, c.x + 1, c.y + 1)
            addWall(c.x + 1, c.y + 1, c.x + 1, c.y)
            addWall(c.x + 1, c.y, c.x, c.y)*/
        }

       // walls.add(Poly.Wall(Vector2(bl.x, bl.y), Vector2(tl.x, tl.y)))
      //  walls.add(Poly.Wall(Vector2(tl.x, tl.y), Vector2(tr.x, tr.y)))
      //  walls.add(Poly.Wall(Vector2(tr.x, tr.y), Vector2(br.x, br.y)))
      //  walls.add(Poly.Wall(Vector2(br.x, br.y), Vector2(bl.x, bl.y)))

        val outputPoints = ArrayList<Vector2>()
        visibleMapBuilder.build(playerPos2, walls, bounds, outputPoints)
        outputPoints.add(0, playerPos2)

        val verticesCount = outputPoints.size
        if (visMapVertices.size / 2 < verticesCount) {
            visMapVertices = FloatArray(verticesCount * 2)
        }

        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugShapeRenderer.color = Color.FIREBRICK

        for (i in outputPoints.indices) {
            val prev = if (i > 0) outputPoints[i - 1] else null
            val next = outputPoints[i]
            visMapVertices[i * 2] = next.x
            visMapVertices[i * 2 + 1] = next.y

            if (prev != null) {
                val toWallDir = playerPos2.cpy().sub(prev)
                val wallDir = next.cpy().sub(prev)
                if (!GeomUtils.isOnLine(toWallDir, wallDir)) {
                    //visibleWalls.add(Poly.Wall(prev, next))
                }
                debugShapeRenderer.line(playerPos2, prev)
            }


            debugShapeRenderer.line(playerPos2, next)
        }

        debugShapeRenderer.end()

        val r : Random = Random()
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        enableBlending()
        walls.forEach {
            debugShapeRenderer.color = Color.BLUE//Color(Color.rgb888(r.nextFloat(), r.nextFloat(), r.nextFloat()))
            debugShapeRenderer.line(it.corners[0], it.corners[1])
        }
        debugShapeRenderer.end()
        /*visMapMesh.setVertices(visMapVertices)

        visMapFB.begin()
        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 0.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        visMapShaderProgram.begin()
        visMapShaderProgram.setUniformf("u_centerPoint", player)
        visMapShaderProgram.setUniformMatrix("u_projTrans", cam.combined)
        visMapMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, verticesCount)

        //debugMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, 3);

        visMapShaderProgram.end()
        visMapFB.end()*/
    }
}