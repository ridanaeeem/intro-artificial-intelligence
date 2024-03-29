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
    public final ArrayList<Coordinate> missedCoordinates = new ArrayList<Coordinate>();

    boolean hitSearch = false;
    Coordinate lastHit;

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

        int prevX;
        int prevY;
        if (attackedCoordinates.size() != 0) {
            prevX = attackedCoordinates.get(attackedCoordinates.size() - 1).getXCoordinate();
            prevY = attackedCoordinates.get(attackedCoordinates.size() - 1).getYCoordinate();
        } else {
            prevX = 0;
            prevY = 0;  
        }
        System.out.println("previous vals: " + enemyBoard[prevX][prevY] + " " + prevX + " " + prevY);

        Random random = new Random();
        EnemyBoard.Outcome hit = EnemyBoard.Outcome.HIT;
        EnemyBoard.Outcome sunk = EnemyBoard.Outcome.SUNK;
        EnemyBoard.Outcome miss = EnemyBoard.Outcome.MISS;

        if (enemyBoard[prevX][prevY] == hit) {
            lastHit = new Coordinate(prevX, prevY);
            System.out.println("last hit: " + lastHit);
            hitCoordinates.add(lastHit);
            hitSearch = true;
        } else if (enemyBoard[prevX][prevY] == miss) {
            missedCoordinates.add(new Coordinate(prevX, prevY));
        }
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
        // if we just sunk something
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
        } 
        // if we just hit something and want more hits
        else if (hitSearch || enemyBoard[prevX][prevY] == hit){
            System.out.println("LOOKING FOR A HIT");
            // look cardinally around hit
            Coordinate top = new Coordinate(lastHit.getXCoordinate(), lastHit.getYCoordinate() - 1);
            Coordinate bottom = new Coordinate(lastHit.getXCoordinate(), lastHit.getYCoordinate() + 1);
            Coordinate left = new Coordinate(lastHit.getXCoordinate() - 1, lastHit.getYCoordinate());
            Coordinate right = new Coordinate(lastHit.getXCoordinate() + 1, lastHit.getYCoordinate());
            System.out.println("top: " + top + " bottom: " + bottom + " left: " + left + " right: " + right);

            if (hitCoordinates.contains(bottom) || hitCoordinates.contains(top) || hitCoordinates.contains(left) || hitCoordinates.contains(right)) {
               // if we have already attacked above go beyond that
                System.out.println("EXPANDING UPWARDS");
                while (attackedCoordinates.contains(top)){
                    top = new Coordinate(top.getXCoordinate(), top.getYCoordinate() - 1);
                }
                x = top.getXCoordinate();
                y = top.getYCoordinate();

                // // but if that was a miss, switch directions and go down
                // if (missedCoordinates.contains(top) || top.getYCoordinate() < 0) {
                //     System.out.println("GOING DOWNWARDS");
                //     while (attackedCoordinates.contains(bottom)){
                //         bottom = new Coordinate(bottom.getXCoordinate(), bottom.getYCoordinate() + 1);
                //     }
                //     x = bottom.getXCoordinate();
                //     y = bottom.getYCoordinate();

                //     // but if we can't go down either, this is two ships stacked on top of each other, let's start by going left
                //     if (missedCoordinates.contains(left) || bottom.getYCoordinate() > boardHeight) {
                //         System.out.println("GOING LEFT");
                //         while (attackedCoordinates.contains(left)){
                //             left = new Coordinate(left.getXCoordinate() - 1, left.getYCoordinate());
                //         }
                //         x = left.getXCoordinate();
                //         y = left.getYCoordinate();

                //         // if we can't go left then this ship is on the right
                //         if (missedCoordinates.contains(right) || left.getXCoordinate() < 0) {
                //             System.out.println("GOING RIGHT");
                //             while (attackedCoordinates.contains(right)){
                //                 right = new Coordinate(right.getXCoordinate() + 1, right.getYCoordinate());
                //             }
                //             x = right.getXCoordinate();
                //             y = right.getYCoordinate();
                //         }
                //     }
                // } 
            } 
            // 0,0 is top left btw
            // expanding upwards
            // else if (hitCoordinates.contains(bottom)){
            //     System.out.println("NOTHING DONE YET");
            //     // pick random for now, later take into acc ship sizes
            //     if (!attackedCoordinates.contains(top)) {
            //         x = top.getXCoordinate();
            //         y = top.getYCoordinate(); 
            //     } else if (!attackedCoordinates.contains(bottom)) {
            //         x = bottom.getXCoordinate();
            //         y = bottom.getYCoordinate();
            //     } else if (!attackedCoordinates.contains(left)) {
            //         x = left.getXCoordinate();
            //         y = left.getYCoordinate();
            //     } else if (!attackedCoordinates.contains(right)) {
            //         x = right.getXCoordinate();
            //         y = right.getYCoordinate();
            //     }
            // } 
            else {
                System.out.println("NOTHING DONE YET");
                // pick random for now, later take into acc ship sizes
                if (!attackedCoordinates.contains(top)) {
                    x = top.getXCoordinate();
                    y = top.getYCoordinate(); 
                } else if (!attackedCoordinates.contains(bottom)) {
                    x = bottom.getXCoordinate();
                    y = bottom.getYCoordinate();
                } else if (!attackedCoordinates.contains(left)) {
                    x = left.getXCoordinate();
                    y = left.getYCoordinate();
                } else if (!attackedCoordinates.contains(right)) {
                    x = right.getXCoordinate();
                    y = right.getYCoordinate();
                }
            }

        if (x > boardWidth || y > boardHeight || x < 0 || y < 0) {
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
        }
        // System.out.println("outcomes:" + enemyBoard[x][y]); 

        // while (shipCount >= 1){
        //     return new Coordinate(2, 2); 
        // }
        
        // for (Coordinate attackedCoord : attackedCoordinates) {
        //     System.out.println("here's the stuff " + attackedCoord + " " +
        //     enemyBoard[attackedCoord.getXCoordinate()][attackedCoord.getYCoordinate()]);
        // }
        }        
        System.out.println(" ");
        attackedCoordinates.add(new Coordinate(x, y));
        return new Coordinate(x, y); 
    }

    @Override
    public void afterGameEnds(final GameView game) {}

}
