package src.labs.infexf.agents;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;


// JAVA PROJECT IMPORTS
import java.lang.Math; 
import java.util.*; 


public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state)
    {
        Set<Integer> enemies = getOtherEnemyUnitIDs();
        // figure out which enemy is closest and prioritize avoiding them
        Vertex closest = null;
        double lowDistance = 1000.0;
        float weight = 0f;

        for (int enemyID : enemies){;
            Vertex enemyVertex = new Vertex(state.getUnit(enemyID).getXPosition(), state.getUnit(enemyID).getYPosition());
            lowDistance = DistanceMetric.euclideanDistance(dst,enemyVertex);

            if (lowDistance <= 10.0){
            weight += 100000f;
            } else if (lowDistance <= 20.0){
                weight += 50000f;
            } else if (lowDistance <= 25.0){
                weight += 500f;
            } else if (lowDistance <= 30.0){
                weight += 100f;
            } else {
                weight += 1f;
            }
            // if (closest == null){
            //     closest = enemyVertex;
            //     lowDistance = DistanceMetric.euclideanDistance(dst,enemyVertex);
            // } else {
            //     double newDistance = DistanceMetric.euclideanDistance(dst,enemyVertex);
            //     if (newDistance < lowDistance){
            //         closest = enemyVertex;
            //         lowDistance = newDistance;
            //     }
            // }
        }

        // float weight = 0f;
        // the lower the distance between the new destination and the closest enemy, the higher the weight
        // if (lowDistance < 10.0){
        //     weight = 500f;
        // } else if (lowDistance < 20.0){
        //     weight = 200f;
        // } else if (lowDistance < 25.0){
        //     weight = 150f;
        // } else {
        //     weight = 10f;
        // }

        // see if the nearby 8 tiles are trees if close to enemies, otherwise it's fine
        // might take too long to get back --> null poitner exception
        float tree = 1.0f;
        // if (getAgentPhase() == AgentPhase.EXFILTRATE){
            if (lowDistance < 15.0){
                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++){
                        // see if this is in range in the first place
                        if (dst.getXCoordinate() + i < state.getXExtent() && dst.getYCoordinate() + j < state.getYExtent()){
                                if (state.isResourceAt(dst.getXCoordinate() + i,dst.getYCoordinate() + j)){
                                    if (lowDistance < 10.0){
                                        tree *= 5f;
                                    } else {
                                        tree *= 3.0f;   
                                    }
                                }             
                            }
                        }
                    }
                }
        // }
        return weight + tree;
    }



    // is the current plan still good? if not make a new one
    @Override
    public boolean shouldReplacePlan(StateView state)
    {
        Vertex myUnit = new Vertex(state.getUnit(this.getMyUnitID()).getXPosition(), state.getUnit(this.getMyUnitID()).getYPosition());
        Set<Integer> enemies = getOtherEnemyUnitIDs();
        // figure out which enemy is closest and prioritize avoiding them
        Vertex closest = null;
        double lowDistance = 1000.0;
        // if (getAgentPhase() == AgentPhase.INFILTRATE){
        if (!enemies.isEmpty()){
            for (int enemyID : enemies){;
                // if the enemy is dead, don't consider it
                if (state.getUnit(enemyID) == null){
                    continue;
                }

                Vertex enemyVertex = new Vertex(state.getUnit(enemyID).getXPosition(), state.getUnit(enemyID).getYPosition());
    
                if (closest == null){
                    closest = enemyVertex;
                    lowDistance = DistanceMetric.euclideanDistance(myUnit,enemyVertex);
                } else {
                    double newDistance = DistanceMetric.euclideanDistance(myUnit,enemyVertex);
                    if (newDistance < lowDistance){
                        closest = enemyVertex;
                        lowDistance = newDistance;
                    }
                }
            }
        }

        int treeCount = 0;
        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                // see if this is in range in the first place
                if (myUnit.getXCoordinate() + i < state.getXExtent() && myUnit.getYCoordinate() + j < state.getYExtent()){
                    if (state.isResourceAt(myUnit.getXCoordinate() + i,myUnit.getYCoordinate() + j)){
                        treeCount++;
                    }             
                }
            }
        }
        

        // the lower the distance between the new destination and the closest enemy, the higher the weight
        if (lowDistance <= 30.0){
            System.out.println(lowDistance);
            return true;
        } else if (treeCount >= 3){
            return true;
        } else {
            return false;
        }
    }

}
