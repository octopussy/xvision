package com.borschtlabs.gytm.dev;

import com.badlogic.gdx.ai.pfa.Heuristic;

/**
 * @author octopussy
 */
public class ManhattanDistanceHeuristic implements Heuristic<TurnArea.WayPoint> {

   @Override
   public float estimate(TurnArea.WayPoint node, TurnArea.WayPoint endNode) {
      return Math.abs(endNode.getX() - node.getX()) + Math.abs(endNode.getY() - node.getY());
   }
}
