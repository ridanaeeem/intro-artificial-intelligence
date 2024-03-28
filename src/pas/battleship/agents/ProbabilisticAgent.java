package src.pas.battleship.agents;

// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.Game;
import edu.bu.battleship.game.Constants;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;

// my project imports
import java.util.*;
import java.util.Random;
import edu.bu.battleship.game.Constants.Ship;
import edu.bu.battleship.game.ships.Ship.ShipType;
import edu.bu.battleship.game.PlayerView;
import edu.bu.battleship.game.EnemyBoard;

public class ProbabilisticAgent
    extends Agent
{

    public ProbabilisticAgent(String name)
    {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    public final ArrayList<Coordinate> attackedCoordinates = new ArrayList<Coordinate>();

    @Override
    public Coordinate makeMove(final GameView game)
    {
        // plan:
        // get all coords that can be picked
        // pick a random coord
        // if the coord is a hit, then pick a coord cardinally around it
        // prob of all ships: 1/total number of ships
        // once getting another hit, figure out the probability of that being a particular type of ship
        // this will be based on the ship sizes and will become more clear with more hits
        // if the coord is a miss, then pick a random coord

        // in pitfall, made a bunch of combos of possible pit placements 
        // then assumed query square had a pit and summed the probability of 
        // each of the placement combos that had a pit in that query square
        // then divided by the total number of combos to get probability of pit in query square
        // then picked highest square probability

        // in this...
        // trying to figure out a possible orientation of every ship sounds like way too much with a time limit
        // maybe work way up to figuring our orientations of ships ?
        // start by just examining probabilities based on hits
        // decide to attack based on joint probability of all ships



        // enemy
        EnemyBoard.Outcome[][] enemyBoard = game.getEnemyBoardView();
        // board details
        Constants gameConstants = game.getGameConstants();
        // board size
        int boardWidth = gameConstants.getNumCols();
        int boardHeight = gameConstants.getNumRows();
        // ships
        Map<ShipType, Integer> myShips = gameConstants.getShipTypeToPopulation();
        // Map<ShipType, java.lang.Integer> enemyShips = PlayerView.getEnemyShipTypeToNumRemaining();
        // ship types
        ArrayList<ShipType> shipTypes = new ArrayList<>();
        // ship sizes
        ArrayList<Integer> shipSizes = new ArrayList<>();
        for (Map.Entry<ShipType, Integer> ship : myShips.entrySet()) {
            // ship types
            shipTypes.add(ship.getKey());
            // ship sizes
            shipSizes.add(Ship.getShipSize(ship.getKey()));
        }
        // total number of ships
        int shipCount = shipTypes.size();
        
        // java.util.Set<Coordinate> allCoords = game.getCoordinates();
        

        Random random = new Random();
        int x = random.nextInt(boardWidth);
        int y = random.nextInt(boardHeight);
        while (attackedCoordinates.contains(new Coordinate(x, y))) {
            x = random.nextInt(boardWidth);
            y = random.nextInt(boardHeight);
        }
        attackedCoordinates.add(new Coordinate(x, y));
        // System.out.println("outcomes:" + enemyBoard[x][y]); 

        // while (shipCount >= 1){
        //     return new Coordinate(2, 2); 
        // }
        
        for (Coordinate attackedCoord : attackedCoordinates) {
            System.out.println("here's the stuff " + attackedCoord + " " +
            enemyBoard[attackedCoord.getXCoordinate()][attackedCoord.getYCoordinate()]);
        }

        return new Coordinate(x, y); 
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
