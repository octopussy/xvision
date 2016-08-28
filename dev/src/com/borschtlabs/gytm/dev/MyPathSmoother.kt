package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils

/**
 * @author octopussy
 */

class MyPathSmoother(internal var raycastCollisionDetector: RaycastCollisionDetector<Vector2>) : IndexedGraph<MyPathSmoother.Node> {

    inner class MyConnection(val from: Node, val to: Node, val distance: Float) : Connection<Node> {
        override fun getCost(): Float = distance
        override fun getFromNode(): Node = from
        override fun getToNode(): Node = to
    }

    inner class Node(val index: Int, val wp: TurnArea.WayPoint) {
        val connections: Array<Connection<Node>> = Array()
    }

    private val nodes: MutableList<Node> = mutableListOf()

    override fun getIndex(node: Node): Int = node.index

    override fun getNodeCount(): Int = nodes.size

    override fun getConnections(fromNode: Node): Array<Connection<Node>> = fromNode.connections

    internal var ray: Ray<Vector2> = Ray(Vector2(), Vector2())

    fun smoothPath(path: MySmoothableGraphPath) {

        val _start = TimeUtils.millis()

        val inputPathLength = path.count

        if (inputPathLength <= 2) return

        (0..inputPathLength - 1).forEach { nodes.add(Node(it, path[it])) }

        for (i in 0..path.count - 2) {
            ray.start.set(path[i].center)

            var j = i + 1

            while (j < path.count) {
                ray.end.set(path[j].center)
                if (!raycastCollisionDetector.collides(ray)) {
                    val distance = Vector2.dst(ray.end.x, ray.end.y, ray.start.x, ray.start.y)
                    nodes[i].connections.add(MyConnection(nodes[i], nodes[j], distance))
                }

                ++j
            }
        }

        val out: GraphPath<Node> = DefaultGraphPath()
        val pf = IndexedAStarPathFinder<Node>(this)

        pf.searchNodePath(nodes.first(), nodes.last(), { node, endNode -> 1f }, out)

        path.clear()

        out.forEach { path.add(it.wp) }

        //Gdx.app.log("PERF", "${TimeUtils.millis() - _start}")
    }
}
