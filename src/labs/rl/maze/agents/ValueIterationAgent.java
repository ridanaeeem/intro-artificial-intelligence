package src.labs.rl.maze.agents;


// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.labs.rl.maze.agents.StochasticAgent;
import edu.bu.labs.rl.maze.agents.StochasticAgent.RewardFunction;
import edu.bu.labs.rl.maze.agents.StochasticAgent.TransitionModel;
import edu.bu.labs.rl.maze.utilities.Coordinate;
import edu.bu.labs.rl.maze.utilities.Pair;



public class ValueIterationAgent
    extends StochasticAgent
{

    public static final double GAMMA = 0.1; // feel free to change this around!
    public static final double EPSILON = 1e-6; // don't change this though

    private Map<Coordinate, Double> utilities;

	public ValueIterationAgent(int playerNum)
	{
		super(playerNum);
        this.utilities = null;
	}

    public Map<Coordinate, Double> getUtilities() { return this.utilities; }
    private void setUtilities(Map<Coordinate, Double> u) { this.utilities = u; }

    public boolean isTerminalState(Coordinate c)
    {
        return c.equals(StochasticAgent.POSITIVE_TERMINAL_STATE)
            || c.equals(StochasticAgent.NEGATIVE_TERMINAL_STATE);
    }

    /**
     * A method to get an initial utility map where every coordinate is mapped to the utility 0.0
     */
    public Map<Coordinate, Double> getZeroMap(StateView state)
    {
        Map<Coordinate, Double> m = new HashMap<Coordinate, Double>();
        for(int x = 0; x < state.getXExtent(); ++x)
        {
            for(int y = 0; y < state.getYExtent(); ++y)
            {
                if(!state.isResourceAt(x, y))
                {
                    // we can go here
                    m.put(new Coordinate(x, y), 0.0);
                }
            }
        }
        return m;
    }

    public void valueIteration(StateView state)
    {
        // TODO: complete me!
        Map<Coordinate, Double> uMap = getZeroMap(state);
        Map<Coordinate, Double> uPrimeMap = getZeroMap(state);   
        double delta = Double.POSITIVE_INFINITY;

        int iter = 0;
        while (delta >= (EPSILON * (1-GAMMA)) / GAMMA){
            iter ++;
            uMap = getZeroMap(state);
            for (Coordinate c: uPrimeMap.keySet()){
                Double copy = uPrimeMap.get(c);
                uMap.put(c, copy);
            }

            // want to find max delta across all coordinates and then reset when looking at coords again
            delta=0;

            for (Coordinate c: uMap.keySet()){
                // bellman equation
                // calculate U'(s) = R(s) + gamma * max_a(sum_s' P(s'|s,a)U(s'))

                double reward = RewardFunction.getReward(c);
                double maxUtility = Double.NEGATIVE_INFINITY;

                // for a given coordinate, need to find the max utility
                // to do this consider all possible directions we could make a move in
                // for each direction, multiply probability of that move * utility of that coordinate
                // sum up all of these values and find the max among directions
                for (Direction d: TransitionModel.CARDINAL_DIRECTIONS){
                    double runSum = 0;
                    // transition.getFirst() is the coordinate 
                    // transition.getSecond() is the probability of transitioning to that coordinate 
                    for (Pair<Coordinate, Double> transition: TransitionModel.getTransitionProbs(state, c, d)){
                        // runSum += probability of action * utility of coordinate
                        // System.out.println("Coordinate " + c + " direction " + d + " trans: " + transition.getFirst() + " prob: " + transition.getSecond());
                        Coordinate curCoordinate = transition.getFirst();
                        double curProb = transition.getSecond();
                        // we are using uMap here - we want to use uMap and not uPrimeMap
                        // because we want to look at that whole state and if we used
                        // uPrimeMap that would be involving the new in progress state
                        runSum += (curProb * uMap.get(curCoordinate));
                    }
                    if (runSum > maxUtility){
                        maxUtility = runSum;
                    }
                }

                // update the utility of the coordinate
                double newUtility = reward + (GAMMA * maxUtility);
                uPrimeMap.put(c, newUtility);

                if (c.equals(StochasticAgent.POSITIVE_TERMINAL_STATE)){
                    uPrimeMap.put(c, reward);
                    continue;
                }
                if (c.equals(StochasticAgent.NEGATIVE_TERMINAL_STATE)){
                    uPrimeMap.put(c, reward);
                    continue;
                }
                
                // calculate error between steps
                if (Math.abs(uPrimeMap.get(c) - uMap.get(c)) > delta){
                    delta = Math.abs(uPrimeMap.get(c) - uMap.get(c));
                }
            }
        }
        // System.out.println("Iterations: " + iter);
        // update utilities to latest, converged values
        // final value ends up being the last updated uMap 
        // because of the way the code is set up uPrimeMap gets updated and then delta is too small, 
        // so we want to use the values right before that update calculation ???
        this.setUtilities(uMap);
        for (Coordinate c: uMap.keySet()){
            System.out.println("Coordinate: " + c + " Utility: " + uMap.get(c));
        }
        return;
    }

    @Override
    public void computePolicy(StateView state, HistoryView history)
    {
        // compute the utilities
        this.valueIteration(state);

        // compute the policy from the utilities
        Map<Coordinate, Direction> policy = new HashMap<Coordinate, Direction>();

        for(Coordinate c : this.getUtilities().keySet())
        {
            // figure out what to do when in this state
            double maxActionUtility = Double.NEGATIVE_INFINITY;
            Direction bestDirection = null;

            // go over every action
            for(Direction d : TransitionModel.CARDINAL_DIRECTIONS)
            {

                // measure how good this action is as a weighted combination of future state's utilities
                double thisActionUtility = 0.0;
                for(Pair<Coordinate, Double> transition : TransitionModel.getTransitionProbs(state, c, d))
                {
                    thisActionUtility += transition.getSecond() * this.getUtilities().get(transition.getFirst());
                }

                // keep the best one!
                if(thisActionUtility > maxActionUtility)
                {
                    maxActionUtility = thisActionUtility;
                    bestDirection = d;
                }
            }

            // policy recommends the best action for every state
            policy.put(c, bestDirection);
        }

        this.setPolicy(policy);
    }

}
