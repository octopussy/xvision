package com.borschlabs.xcom

import com.badlogic.gdx.Application
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx

class XcomGame : Game() {
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        setScreen(GameScreen())
    }
}
