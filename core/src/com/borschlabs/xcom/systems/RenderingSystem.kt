package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
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

    private val debugShapeRenderer: ShapeRenderer = ShapeRenderer()

    private val mainShaderProgram: ShaderProgram

    private var visMapFB: FrameBuffer? = null
    private val visMapShaderProgram: ShaderProgram
    private val visMapMesh: Mesh

    private var visMapVertices: FloatArray = FloatArray(16)
    private var walls: MutableList<Poly.Wall> = mutableListOf()
    private var visibleMapBuilder: VisibleMapBuilder = VisibleMapBuilder(debugShapeRenderer)

    private val leftBound: Poly.Wall = Poly.Wall(Vector2(), Vector2())
    private val topBound: Poly.Wall = Poly.Wall(Vector2(), Vector2())
    private val rightBound: Poly.Wall = Poly.Wall(Vector2(), Vector2())
    private val bottomBound: Poly.Wall = Poly.Wall(Vector2(), Vector2())

    private val batch: SpriteBatch = SpriteBatch()

    private val tiledMapRenderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var visibleObjects: ImmutableArray<Entity> = ImmutableArray(Array())
    private var units: ImmutableArray<Entity> = ImmutableArray(Array())
    private var routes: ImmutableArray<Entity> = ImmutableArray(Array())

    init {
        mainShaderProgram = ShaderProgram(Gdx.files.internal("shaders/main.vsh"), Gdx.files.internal("shaders/main.fsh"))
        println(if (mainShaderProgram.isCompiled()) "Main shader compiled!" else mainShaderProgram.getLog())

        visMapShaderProgram = ShaderProgram(Gdx.files.internal("shaders/vis_field.vsh"), Gdx.files.internal("shaders/vis_field.fsh"))
        println(if (visMapShaderProgram.isCompiled) "Lightmap shader compiled!" else visMapShaderProgram.log)

        visMapMesh = Mesh(false, 4096, 0, VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"))

        batch.shader = mainShaderProgram
    }

    override fun addedToEngine(engine: Engine) {
        visibleObjects = engine.getEntitiesFor(Family.all(TextureComponent::class.java, TransformComponent::class.java).get())
        units = engine.getEntitiesFor(Family.all(GameUnitComponent::class.java).get())
        routes = engine.getEntitiesFor(Family.all(RouteComponent::class.java).get())

        createVisMapFB(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
        debugShapeRenderer.dispose()
        tiledMapRenderer.dispose()
    }

    override fun update(deltaTime: Float) {
        camera.update()

        drawVisMap()

        // set vis map
        mainShaderProgram.begin()

        Gdx.graphics.gL20.glActiveTexture(GL20.GL_TEXTURE1)
        visMapFB!!.colorBufferTexture.bind()

        mainShaderProgram.setUniformi("u_vismap", 1)

        Gdx.graphics.gL20.glActiveTexture(GL20.GL_TEXTURE0)

        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        mainShaderProgram.end()

        debugShapeRenderer.projectionMatrix = camera.combined

        //drawTurnAreas()

        batch.projectionMatrix = camera.combined
        batch.begin()

        drawVisibleObjects()

        batch.end()

        drawRoutes()
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
        createVisMapFB(height, width)
    }

    private fun createVisMapFB(height: Int, width: Int) {
        visMapFB?.dispose()
        visMapFB = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        visMapFB?.colorBufferTexture?.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
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

    // TODO: implement pool
    private fun drawVisMap() {
        val player = engine.getSystem(CoreSystem::class.java).getPlayer() ?: return
        val playerPosition = Mappers.GAME_UNIT.get(player).pos.cpy()

        playerPosition.x += cellSize / 2.0f
        playerPosition.y += cellSize / 2.0f

        val playerPos2 = Vector2(playerPosition.x, playerPosition.y)

        walls.clear()

        val maxDistance = 512.0f

        field.getVisibleWalls(playerPosition, maxDistance, walls)

        leftBound.corners[0].set(playerPos2.x - maxDistance, playerPos2.y - maxDistance)
        leftBound.corners[1].set(playerPos2.x - maxDistance, playerPos2.y + maxDistance)

        topBound.corners[0] = leftBound.corners[1]
        topBound.corners[1].set(playerPos2.x + maxDistance, playerPos2.y + maxDistance)

        rightBound.corners[0] = topBound.corners[1]
        rightBound.corners[1].set(playerPos2.x + maxDistance, playerPos2.y - maxDistance)

        bottomBound.corners[0] = rightBound.corners[1]
        bottomBound.corners[1] = leftBound.corners[0]

        walls.add(leftBound)
        walls.add(topBound)
        walls.add(rightBound)
        walls.add(bottomBound)

        val outputPoints = ArrayList<Vector2>()
        visibleMapBuilder.build(playerPos2, walls, outputPoints)
        outputPoints.add(0, playerPos2)

        val verticesCount = outputPoints.size
        if (visMapVertices.size / 2 < verticesCount) {
            visMapVertices = FloatArray(verticesCount * 2)
        }

        //debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        //debugShapeRenderer.color = Color.FIREBRICK

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
                //debugShapeRenderer.line(playerPos2, prev)
            }


            //debugShapeRenderer.line(playerPos2, next)
        }

        //debugShapeRenderer.end()

        /*val r: Random = Random()
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        enableBlending()
        walls.forEach {
            debugShapeRenderer.color = Color.BLUE//Color(Color.rgb888(r.nextFloat(), r.nextFloat(), r.nextFloat()))
            debugShapeRenderer.line(it.corners[0], it.corners[1])
        }
        debugShapeRenderer.end()*/

        visMapMesh.setVertices(visMapVertices)

        visMapFB!!.begin()
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        visMapShaderProgram.begin()
        visMapShaderProgram.setUniformf("u_centerPoint", playerPos2)
        visMapShaderProgram.setUniformMatrix("u_projTrans", camera.combined)
        visMapMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, verticesCount)

        //debugMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, 3);

        visMapShaderProgram.end()
        visMapFB!!.end()
    }
}