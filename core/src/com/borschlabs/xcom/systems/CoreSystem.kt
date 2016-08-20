package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.PlayerComponent
import com.borschlabs.xcom.components.RouteComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.Route

/**
 * @author octopussy
 */

class CoreSystem(val field: Field) : EntitySystem() {

    private var state:CoreState = CoreState.UNKNOWN

    private var players: ImmutableArray<Entity> = ImmutableArray(Array())

    override fun addedToEngine(engine: Engine) {
        players = engine.getEntitiesFor(Family.all(
                PlayerComponent::class.java,
                GameUnitComponent::class.java,
                TransformComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        for (e in players) {

            val unitComponent = Mappers.GAME_UNIT.get(e)
            val transComponent = Mappers.TRANSFORM.get(e)

            when (unitComponent.state) {
                GameUnitComponent.Companion.State.IDLE -> {
                    val cell = unitComponent.cell

                    if (cell != null) {
                        transComponent.pos.x = cell.x * field.cellSize
                        transComponent.pos.y = cell.y * field.cellSize
                    }
                }
                GameUnitComponent.Companion.State.MOVING -> {
                    unitComponent.stepMovement(deltaTime, 5f)
                }
            }
        }
    }

    fun startPlayerTurn() {
        getPlayer()?.let {
            Mappers.GAME_UNIT.get(it).startTurn(20)
            state = CoreState.WAIT_FOR_PLAYER_TURN
        }
    }

    fun handleTap(x: Float, y: Float) {
        val fx = Math.floor(x / field.cellSize.toDouble())
        val fy = Math.floor(y / field.cellSize.toDouble())

        val cell = field.getCell(fx.toInt(), fy.toInt())

        getPlayer()?.let {
            val unitC = Mappers.GAME_UNIT.get(it)
            when (unitC.state) {
                GameUnitComponent.Companion.State.IDLE -> {
                    val isInTurnArea = unitC.turnArea.reachableCells.contains(cell)
                    if (cell != null && isInTurnArea) {
                        setRouteToOrGo(it, cell)
                    }
                }
                GameUnitComponent.Companion.State.MOVING -> { /* do nothing */ }
            }
        }
    }

    private fun setRouteToOrGo(e: Entity, targetCell: FieldCell) {
        val unitComponent = Mappers.GAME_UNIT.get(e)
        val sourceCell = unitComponent.cell
        val routeComponent = if (Mappers.ROUTE.has(e)) {
            Mappers.ROUTE.get(e)
        }
        else {
            val newRouteComp = RouteComponent()
            e.add(newRouteComp)
            newRouteComp
        }

        // tap to the end of the route?
        if (routeComponent.route.cells.isNotEmpty() && targetCell == routeComponent.route.cells.last()) {
            unitComponent.startMoving(routeComponent.route)
            e.remove(RouteComponent::class.java)
            Gdx.app.log(TAG, "Start movement to $targetCell")
        }

        if (sourceCell != null) {
            routeComponent.route = Route(unitComponent.turnArea.getRoute(sourceCell, targetCell))
        }
    }

    private fun getPlayer():Entity? = if (players.size() > 0) players.first() else null

    companion object {
        private val TAG = "CoreSystem"
    }

    enum class CoreState {
        UNKNOWN,
        WAIT_FOR_PLAYER_TURN
    }
}