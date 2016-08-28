package com.borschtlabs.gytm.dev.core

/**
 * @author octopussy
 */

class TestActor : Actor() {

    var testField: String

    init {
        testField = "123"
    }

    override fun toString(): String {
        return testField
    }
}