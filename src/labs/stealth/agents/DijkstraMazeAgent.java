package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;                           // Directions in Sepia


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue; // heap in java
import java.util.*;
import java.lang.Math; 


// JAVA PROJECT IMPORTS


public class DijkstraMazeAgent
    extends MazeAgent
{

    public DijkstraMazeAgent(int playerNum)
    {
        super(playerNum);
    }
    
    public static final Direction NORTH = Direction.NORTH;
    public static final Direction NORTHEAST = Direction.NORTHEAST;
    public static final Direction EAST = Direction.EAST;
    public static final Direction SOUTHEAST = Direction.SOUTHEAST;
    public static final Direction SOUTH = Direction.SOUTH;
    public static final Direction SOUTHWEST = Direction.SOUTHWEST;
    public static final Direction WEST = Direction.WEST;
    public static final Direction NORTHWEST = Direction.NORTHWEST;

    // Define costs associated with each direction
    private static final float[] cost = {(float) 10.0, (float) Math.sqrt(125.0), (float) 5.0, (float) Math.sqrt(26.0), (float) 1.0, (float) Math.sqrt(26.0), (float) 5.0, (float) Math.sqrt(125.0)};

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        // queue of neighbors to explore
        PriorityQueue<Path> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(vertex -> vertex.getCost()));
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
                Vertex prior = shortest.pop();
                Path p = new Path(prior);
                while (!shortest.isEmpty()){
                    Vertex poppedVertex = shortest.pop();
                    p = new Path(poppedVertex, cost[getDirectionToMoveTo(prior,poppedVertex).ordinal()], p);
                    prior = poppedVertex;     
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
        return false;
    }

}
