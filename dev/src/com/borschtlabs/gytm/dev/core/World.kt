package com.borschtlabs.gytm.dev.core

/**
 * @author octopussy
 */

class World {

    inline fun <reified T: Actor> spawnActor(initializer: T.() -> Unit): T {
        val actor = T::class.java.newInstance()
        initializer(actor)
        return actor
    }

}