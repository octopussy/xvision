package com.borschtlabs.gytm.dev.core

import com.badlogic.ashley.core.Engine
import com.borschtlabs.gytm.dev.level.Level
import com.borschtlabs.gytm.dev.level.LevelLoader

/**
 * @author octopussy
 */

class World(val engine: Engine) {

    private var _levelName: String = "<no level>"

    var level: Level? = null

    val levelName: String = _levelName

    inline fun <reified T: Actor> spawnActor(initializer: T.() -> Unit): T {
        val constr = T::class.java.getConstructor(World::class.java)
        val actor = constr.newInstance(this)
        engine.addEntity(actor)
        initializer(actor)
        return actor
    }

    fun loadLevel(name: String) {
        level = LevelLoader("maps").load("$name")
        _levelName = name
    }
}