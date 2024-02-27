package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


import java.util.HashSet;   // will need for dfs
import java.util.Stack;     // will need for dfs
import java.util.Set;       // will need for dfs
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.lang.Math;

// JAVA PROJECT IMPORTS


public class DFSMazeAgent
    extends MazeAgent
{

    public DFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        Stack<Vertex> stack = new Stack<>();
        HashSet<Vertex> visited = new HashSet<>();
        Vertex currentVertex = src;
        stack.push(src);
        visited.add(src);

        // for finding the next vertex, just add these to the current coordinate
        List<List<Integer>> adjacentAdd = new ArrayList<>(Arrays.asList(
            Arrays.asList(-1, 1),
            Arrays.asList(0, 1),
            Arrays.asList(1, 1),
            Arrays.asList(1, 0),
            Arrays.asList(1, -1),
            Arrays.asList(0, -1),
            Arrays.asList(-1, -1),
            Arrays.asList(-1, 0)
        ));

        Path p = new Path(currentVertex); 

        while (!stack.empty()){
            // reach right before the goal
            if (Math.max(Math.abs(currentVertex.getXCoordinate() - goal.getXCoordinate()), Math.abs(currentVertex.getYCoordinate() - goal.getYCoordinate())) == 1){
                return p;
            }

            // pick the next vertex 
            for (int i = 0; i < 8; i++) {
                List<Integer> add = adjacentAdd.get(i);
                Integer curX = currentVertex.getXCoordinate();
                Integer curY = currentVertex.getYCoordinate();    
                Vertex nextVertex = new Vertex(curX + add.get(0), curY + add.get(1));
                // no need to revisit at this stage
                // make sure it's in bounds
                // make sure there's not a tree there
                // check for enemy also
                if (visited.contains(nextVertex) == false 
                && state.inBounds(nextVertex.getXCoordinate(), nextVertex.getYCoordinate()) 
                && !state.isResourceAt(nextVertex.getXCoordinate(), nextVertex.getYCoordinate())
                && !state.isUnitAt(nextVertex.getXCoordinate(), nextVertex.getYCoordinate())){
                    stack.push(nextVertex);
                    visited.add(nextVertex);
                    currentVertex = nextVertex;
                    p = new Path(currentVertex, 1, p); 
                    // don't need to keep trying with the previous currentvertex, move on and continue exploring here
                    break;
                } else if (i == 7){
                    if (stack.isEmpty()) {
                        return null;
                    }
                    // pop the currentVertex, and then pop again to get what was right before that
                    // now what was right before that is the currentVertex so add that back to the stack
                    // to keep track of it. add it to the path as well
                    currentVertex = stack.pop();
                    currentVertex = stack.pop();
                    stack.push(currentVertex);
                    p = new Path(currentVertex, 1, p); 
                }
            }
        }

        return null;
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        return false;
    }

}
