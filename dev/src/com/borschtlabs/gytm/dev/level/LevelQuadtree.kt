package com.borschtlabs.gytm.dev.level

import com.badlogic.gdx.math.Rectangle

/**
 * @author octopussy
 */

class LevelQuadtree(private var level: Int, private var bounds: Rectangle) {

    interface Object {
        val bounds: Rectangle
    }

    private val MAX_OBJECTS = 4
    private val MAX_LEVELS = 100

    private var objects = mutableListOf<Object>()

    private var nodes = mutableListOf<LevelQuadtree>()

    fun insert(obj: Object) {
        if (nodes.isNotEmpty()) {
            val index = getIndex(obj)

            if (index != -1) {
                nodes[index].insert(obj)

                return
            }
        }

        objects.add(obj)

        if (objects.size > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes.isEmpty()) {
                split()
            }

            var i = 0
            while (i < objects.size) {
                val index = getIndex(objects[i])
                if (index != -1) {
                    nodes[index].insert(objects.removeAt(i))
                } else {
                    i++
                }
            }
        }
    }

    fun retrieve(returnObjects: MutableList<Object>, obj: Object): List<Object> {
        val index = getIndex(obj)
        if (index != -1 && nodes.isNotEmpty()) {
            nodes[index].retrieve(returnObjects, obj)
        }

        returnObjects.addAll(objects)

        return returnObjects
    }

    fun clear() {
        objects.clear()
        nodes.forEach { it.clear() }
        nodes.clear()
    }

    fun countObjects(): Int {
        var c = 0

        if (nodes.isNotEmpty()) {
            c += nodes[0].countObjects()
            c += nodes[1].countObjects()
            c += nodes[2].countObjects()
            c += nodes[3].countObjects()
        }

        return objects.size + c
    }

    private fun split() {
        val subWidth = (bounds.getWidth() / 2)
        val subHeight = (bounds.getHeight() / 2)
        val x = bounds.getX()
        val y = bounds.getY()

        nodes.forEach { it.clear() }
        nodes.clear()
        nodes.add(LevelQuadtree(level + 1, Rectangle(x + subWidth, y, subWidth, subHeight)))
        nodes.add(LevelQuadtree(level + 1, Rectangle(x, y, subWidth, subHeight)))
        nodes.add(LevelQuadtree(level + 1, Rectangle(x, y + subHeight, subWidth, subHeight)))
        nodes.add(LevelQuadtree(level + 1, Rectangle(x + subWidth, y + subHeight, subWidth, subHeight)))
    }

    private fun getIndex(obj: Object): Int {
        val rect = obj.bounds

        var index = -1
        val verticalMidpoint = bounds.getX() + bounds.getWidth() / 2
        val horizontalMidpoint = bounds.getY() + bounds.getHeight() / 2

        // Object can completely fit within the top quadrants
        val topQuadrant = rect.getY() < horizontalMidpoint && rect.getY() + rect.getHeight() < horizontalMidpoint
        // Object can completely fit within the bottom quadrants
        val bottomQuadrant = rect.getY() > horizontalMidpoint

        // Object can completely fit within the left quadrants
        if (rect.getX() < verticalMidpoint && rect.getX() + rect.getWidth() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1
            } else if (bottomQuadrant) {
                index = 2
            }
        } else if (rect.getX() > verticalMidpoint) {
            if (topQuadrant) {
                index = 0
            } else if (bottomQuadrant) {
                index = 3
            }
        }// Object can completely fit within the right quadrants

        return index
    }
}