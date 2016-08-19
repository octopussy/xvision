package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.world.Field

/**
 * @author octopussy
 */

class CoreSystem(val field: Field) : EntitySystem() {

    private var units: ImmutableArray<Entity> = ImmutableArray(Array())

    private val unitM = ComponentMapper.getFor(GameUnitComponent::class.java)

    private val transM = ComponentMapper.getFor(TransformComponent::class.java)

    override fun addedToEngine(engine: Engine) {
        units = engine.getEntitiesFor(Family.all(GameUnitComponent::class.java, TransformComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        for (e in units) {
            val unitComponent = unitM.get(e)
            val transComponent = transM.get(e)
            when (unitComponent.state) {
                GameUnitComponent.Companion.State.IDLE -> {
                    unitComponent.isTurnAreaVisible = true
                    transComponent.pos.x = unitComponent.cell.x * field.cellSize
                    transComponent.pos.y = unitComponent.cell.y * field.cellSize

                    if (!unitComponent.isTurnAreaCalculated) {
                        unitComponent.turnArea.calculateArea(unitComponent.cell, unitComponent.actionPoints)
                    }
                }
                GameUnitComponent.Companion.State.MOVING -> {
                    unitComponent.isTurnAreaVisible = false
                }
            }
        }
    }
}