package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.ComponentMapper
import com.borschlabs.xcom.components.*

/**
 * @author octopussy
 */

object Mappers {
    val TRANSFORM: ComponentMapper<TransformComponent> = ComponentMapper.getFor(TransformComponent::class.java)

    val TEXTURE: ComponentMapper<TextureComponent> = ComponentMapper.getFor(TextureComponent::class.java)

    val GAME_UNIT: ComponentMapper<GameUnitComponent> = ComponentMapper.getFor(GameUnitComponent::class.java)

    val PLAYER: ComponentMapper<PlayerComponent> = ComponentMapper.getFor(PlayerComponent::class.java)

    val ROUTE: ComponentMapper<RouteComponent> = ComponentMapper.getFor(RouteComponent::class.java)
}