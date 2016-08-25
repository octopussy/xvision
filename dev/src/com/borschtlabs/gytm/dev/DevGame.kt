package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx

/**
 * @author octopussy
 */


class DevGame : Game() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        setScreen(DevScreen())
    }
}