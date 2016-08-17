package com.borschlabs.xcom.world

/**
 * @author octopussy
 */

class PathFinder(val field: Field) {
    private val openList:MutableSet<Node> = mutableSetOf()
    fun findPath(startX: Int, startY: Int, targetX: Int, targetY: Int) {
        openList.add(Node(FieldCell(startX, startY)))
    }

    private class Node(val cell:FieldCell) {
        var parent:Node? = null
    }
}