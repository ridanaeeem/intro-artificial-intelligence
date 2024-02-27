package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


import java.util.HashSet;       // will need for bfs
import java.util.Queue;         // will need for bfs
import java.util.LinkedList;    // will need for bfs
import java.util.Set;           // will need for bfs
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap; 
import java.util.Stack;     

import java.lang.Math;


// JAVA PROJECT IMPORTS


public class BFSMazeAgent
    extends MazeAgent
{

    public BFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        System.out.print("bfs");
        // queue of neighbors to explore
        Queue<Vertex> neighbors = new LinkedList<>();
        HashSet<Vertex> visited = new HashSet<>();
        HashMap<Vertex, Vertex> prev = new HashMap<>();
        Vertex currentVertex = src;
        // start by exploring what is available to src
        neighbors.add(src);

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

        while (!neighbors.isEmpty()){
            currentVertex = neighbors.poll();
            visited.add(currentVertex);
            if (Math.max(Math.abs(currentVertex.getXCoordinate() - goal.getXCoordinate()), Math.abs(currentVertex.getYCoordinate() - goal.getYCoordinate())) == 1){
                // conversions to get stuff in the right format
                Stack<Vertex> shortest = new Stack<>();
                Vertex tempCurrent = currentVertex;
                shortest.push(tempCurrent);
                while (prev.containsKey(tempCurrent)){
                    shortest.push(prev.get(tempCurrent));
                    tempCurrent = prev.get(tempCurrent);
                }
                Path p = new Path(shortest.pop());
                while (!shortest.isEmpty()){
                    Vertex poppedVertex = shortest.pop();
                    p = new Path(poppedVertex, 1, p);     
                }

                return p;
            }

            for (int i = 0; i < 8; i++) {
                List<Integer> add = adjacentAdd.get(i);
                Integer curX = currentVertex.getXCoordinate();
                Integer curY = currentVertex.getYCoordinate();    
                Vertex nextVertex = new Vertex(curX + add.get(0), curY + add.get(1));
                // no need to revisit at this stage
                // make sure its not already in neighbors, only look at them once
                //     that way, you'll get the shortest path to that one vertex by default in prev
                //     then, can just follow them back
                // make sure it's in bounds
                // make sure there's not a tree there
                // check for enemy also
                if (visited.contains(nextVertex) == false 
                && (neighbors.contains(nextVertex) == false)
                && state.inBounds(nextVertex.getXCoordinate(), nextVertex.getYCoordinate()) 
                && !state.isResourceAt(nextVertex.getXCoordinate(), nextVertex.getYCoordinate())
                && !state.isUnitAt(nextVertex.getXCoordinate(), nextVertex.getYCoordinate())){
                    neighbors.add(nextVertex);
                    prev.put(nextVertex, currentVertex);
                }
            } 
        }

        return null;
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        // return true if current path is invalid
        // can be invalid if you reach the src again 
        return false;
    }

}
