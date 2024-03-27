package src.pas.battleship.agents;


import java.util.*; 

// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.Game;
import edu.bu.battleship.game.Constants;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;

// my project imports
import edu.bu.battleship.game.Constants.Ship;
import edu.bu.battleship.game.ships.Ship.ShipType;

public class ProbabilisticAgent
    extends Agent
{

    public ProbabilisticAgent(String name)
    {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game)
    {
        //
        // board details
        Constants gameConstants = game.getGameConstants();
        // board size
        int boardWidth = gameConstants.getNumCols();
        int boardHeight = gameConstants.getNumRows();
        // ships
        Map<ShipType, Integer> ships = gameConstants.getShipTypeToPopulation();
        // ship types
        ArrayList<ShipType> shipTypes = new ArrayList<>();
        // ship sizes
        ArrayList<Integer> shipSizes = new ArrayList<>();
        for (Map.Entry<ShipType, Integer> ship : ships.entrySet()) {
            // ship types
            shipTypes.add(ship.getKey());
            // ship sizes
            shipSizes.add(Ship.getShipSize(ship.getKey()));
        }
        // total number of ships
        int shipCount = shipTypes.size();

        while (shipCount >= 1){
           return new Coordinate(2, 2); 
        }
        
        return new Coordinate(2, 2); 
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
