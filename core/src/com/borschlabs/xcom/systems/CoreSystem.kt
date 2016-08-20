package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.PlayerComponent
import com.borschlabs.xcom.components.RouteComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell

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
                        unitComponent.isTurnAreaVisible = true
                        transComponent.pos.x = cell.x * field.cellSize
                        transComponent.pos.y = cell.y * field.cellSize
                    }
                }
                GameUnitComponent.Companion.State.MOVING -> {
                    unitComponent.isTurnAreaVisible = false
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
        when (state) {

            CoreSystem.CoreState.UNKNOWN -> {}

            CoreSystem.CoreState.WAIT_FOR_PLAYER_TURN -> {
                getPlayer()?.let {
                    val unit = Mappers.GAME_UNIT.get(it)
                    if (cell != null && unit.turnArea.reachableCells.contains(cell)) {
                        setRouteTo(it, cell)
                    } else {
                        removeRoute(it)
                    }
                }
            }
        }
    }

    private fun setRouteTo(e: Entity, targetCell: FieldCell) {
        val playerComp = Mappers.GAME_UNIT.get(e)
        val sourceCell = playerComp.cell
        val routeComponent = if (Mappers.ROUTE.has(e)) {
            Mappers.ROUTE.get(e)
        }
        else {
            val newRoute = RouteComponent()
            e.add(newRoute)
            newRoute
        }

        if (sourceCell != null) {
            routeComponent.route = playerComp.turnArea.getRoute(sourceCell, targetCell)
        }
    }

    private fun removeRoute(e: Entity) {
        e.remove(RouteComponent::class.java)
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