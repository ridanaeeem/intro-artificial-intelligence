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
    public final ArrayList<Coordinate> hitCoordinates = new ArrayList<Coordinate>();
    public final ArrayList<Coordinate> doNotHitCoordinates = new ArrayList<Coordinate>();

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

        int prevX = 0;
        int prevY = 0;
        if (attackedCoordinates.size() != 0) {
            prevX = attackedCoordinates.get(attackedCoordinates.size() - 1).getXCoordinate();
            prevY = attackedCoordinates.get(attackedCoordinates.size() - 1).getYCoordinate();
        }
        System.out.println(enemyBoard[prevX][prevY] + " " + prevX + " " + prevY);
        Random random = new Random();
        EnemyBoard.Outcome hit = EnemyBoard.Outcome.HIT;
        EnemyBoard.Outcome sunk = EnemyBoard.Outcome.SUNK;
        EnemyBoard.Outcome miss = EnemyBoard.Outcome.MISS;

        // pick any x
        int x = random.nextInt(boardWidth);
        // follow a checkerboard pattern
        int y = random.nextInt(boardHeight);

        // if no leads, attack randomly diagonally 
        if ((attackedCoordinates.size() == 0) || (enemyBoard[prevX][prevY] == miss)){
            // ensure diagonality first
            if (x % 2 == 0) {
                while (y % 2 != 0) {
                    y = random.nextInt(boardHeight);
                }
            } else {
                while (y % 2 == 0) {
                    y = random.nextInt(boardHeight);
                }
            }
            // then check and see if it would be a waste to attack a set of coordinates
            while (attackedCoordinates.contains(new Coordinate(x, y))) {
                x = random.nextInt(boardWidth);
                if (x % 2 == 0) {
                    while (y % 2 != 0) {
                        y = random.nextInt(boardHeight);
                    }
                } else {
                    while (y % 2 == 0) {
                        y = random.nextInt(boardHeight);
                    }
                }
            }
            attackedCoordinates.add(new Coordinate(x, y));    
        }
        else if (enemyBoard[prevX][prevY] == hit){
            System.out.println("WE GOT A HIT");
            if (x % 2 == 0) {
                while (y % 2 != 0) {
                    y = random.nextInt(boardHeight);
                }
            } else {
                while (y % 2 == 0) {
                    y = random.nextInt(boardHeight);
                }
            }
            while (attackedCoordinates.contains(new Coordinate(x, y))) {
               if (x % 2 == 0) {
                while (y % 2 != 0) {
                    y = random.nextInt(boardHeight);
                }
            } else {
                while (y % 2 == 0) {
                    y = random.nextInt(boardHeight);
                }
            }
            }
            attackedCoordinates.add(new Coordinate(x, y));
        } else if (enemyBoard[prevX][prevY] == sunk){
            System.out.println("WE GOT A SINK");
            // pick any x
            if (x % 2 == 0) {
                while (y % 2 != 0) {
                    y = random.nextInt(boardHeight);
                }
            } else {
                while (y % 2 == 0) {
                    y = random.nextInt(boardHeight);
                }
            }
            while (attackedCoordinates.contains(new Coordinate(x, y))) {
                if (x % 2 == 0) {
                while (y % 2 != 0) {
                    y = random.nextInt(boardHeight);
                }
            } else {
                while (y % 2 == 0) {
                    y = random.nextInt(boardHeight);
                }
            }
            }
            attackedCoordinates.add(new Coordinate(x, y));
        // if no hits yet, keep firing randomly 
        } 
        
        // System.out.println("outcomes:" + enemyBoard[x][y]); 

        // while (shipCount >= 1){
        //     return new Coordinate(2, 2); 
        // }
        
        // for (Coordinate attackedCoord : attackedCoordinates) {
        //     System.out.println("here's the stuff " + attackedCoord + " " +
        //     enemyBoard[attackedCoord.getXCoordinate()][attackedCoord.getYCoordinate()]);
        // }

        return new Coordinate(x, y); 
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
