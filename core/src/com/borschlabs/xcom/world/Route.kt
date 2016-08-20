package com.borschlabs.xcom.world

/**
 * @author octopussy
 */

class Route(var cells: List<FieldCell>) {
    constructor() : this(listOf())

    val length: Float by lazy { if (cells.size > 1) (cells.size - 1).toFloat() else 0f }

}