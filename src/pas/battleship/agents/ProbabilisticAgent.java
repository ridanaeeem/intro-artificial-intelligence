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

    public final ArrayList<Coordinate> diagonalCoordinates = new ArrayList<Coordinate>();
    public final ArrayList<Coordinate> attackedCoordinates = new ArrayList<Coordinate>();
    public final ArrayList<Coordinate> hitCoordinates = new ArrayList<Coordinate>();
    public final ArrayList<Coordinate> missedCoordinates = new ArrayList<Coordinate>();

    boolean hitSearch;
    boolean haveSunk;
    Coordinate lastHit;

    double prTop;
    double prBottom;
    double prLeft;
    double prRight;

    EnemyBoard.Outcome hit = EnemyBoard.Outcome.HIT;
    EnemyBoard.Outcome sunk = EnemyBoard.Outcome.SUNK;
    EnemyBoard.Outcome miss = EnemyBoard.Outcome.MISS;
    EnemyBoard.Outcome unknown = EnemyBoard.Outcome.UNKNOWN;

    @Override
    public Coordinate makeMove(final GameView game)
    {
        // enemy
        EnemyBoard.Outcome[][] enemyBoard = game.getEnemyBoardView();
        // board details
        Constants gameConstants = game.getGameConstants();
        // board size
        int boardWidth = gameConstants.getNumCols();
        int boardHeight = gameConstants.getNumRows();
        // ships
        Map<ShipType, Integer> myShips = gameConstants.getShipTypeToPopulation();
        Map<ShipType, java.lang.Integer> enemyShips = game.getEnemyShipTypeToNumRemaining();
        ArrayList<Integer> shipSizes = new ArrayList<>();
        for (Map.Entry<ShipType, Integer> entry : enemyShips.entrySet()) {
            ShipType ship = entry.getKey();
            Integer numRemaining = entry.getValue();
            shipSizes.add(Ship.getShipSize(ship));
        }

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
        
        // initialize all diagonal coordinates
        // int options = 0;
        for (int x=0; x<boardWidth; x++){
            for (int y=0; y<boardHeight; y++){
                if (!attackedCoordinates.contains(new Coordinate(x, y))){
                    // check for diagonality
                    if ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0)){
                        if (!diagonalCoordinates.contains(new Coordinate(x,y))) diagonalCoordinates.add(new Coordinate(x, y));
                        // if (!shipSizes.contains(2) && !shipSizes.contains(3)){
                        //     if (shipSizes.contains(4) || shipSizes.contains(5)){
                        //         if (missedCoordinates.contains(new Coordinate(x,y)))
                        //         if (!diagonalCoordinates.contains(new Coordinate(x,y))) diagonalCoordinates.add(new Coordinate(x, y));
                        //     } else {
                        //         if (!diagonalCoordinates.contains(new Coordinate(x,y))) diagonalCoordinates.add(new Coordinate(x, y));
                        //     }
                        // } else {
                        //     if (!diagonalCoordinates.contains(new Coordinate(x,y))) diagonalCoordinates.add(new Coordinate(x, y));
                        // }
                    } 
                } 
            }
        }
        int options = diagonalCoordinates.size();
        System.out.println("diag coords: " + diagonalCoordinates);
        System.out.println("options: " + options);

        if (enemyBoard[prevX][prevY] == hit) {
            lastHit = new Coordinate(prevX, prevY);
            // if (haveSunk){
            //     lastHit = new Coordinate(prevX, prevY);
            //     haveSunk = false;
            // }
            // System.out.println("last hit: " + lastHit);
            // hitSearch = true;
            hitCoordinates.add(new Coordinate(prevX, prevY));
        } else if (enemyBoard[prevX][prevY] == miss) {
            missedCoordinates.add(new Coordinate(prevX, prevY));
            // System.out.println("most recent miss: " + prevX + " " + prevY);
        } else if (enemyBoard[prevX][prevY] == sunk){
            hitSearch = false;
        }

        if (hitCoordinates.size() > 0){
            hitSearch = true;
            for (int i = 0; i < hitCoordinates.size(); i++) {
                if (enemyBoard[hitCoordinates.get(i).getXCoordinate()][hitCoordinates.get(i).getYCoordinate()] != hit){
                    hitCoordinates.remove(i);
                };
            }
        }

        if (hitCoordinates.size() > 0){
            hitSearch = true;
            if (!hitCoordinates.contains(lastHit)){
                lastHit = hitCoordinates.get(hitCoordinates.size() - 1);
            }
        } else {
            hitSearch = false;
        }

        System.out.println("hit coords: " + hitCoordinates);
        System.out.println("missed coords: " + missedCoordinates);
        System.out.println("last hit: " + lastHit);
        // if (hitCoordinates.size() > 0) hitSearch = true;
        // System.out.println("hit search: " + hitSearch);

        // pick any x
        int x = random.nextInt(boardWidth);
        // follow a checkerboard pattern
        int y = random.nextInt(boardHeight);

        // if no leads, attack randomly diagonally 
        if (enemyBoard[prevX][prevY] == sunk){
            // hitSearch = false;
            System.out.println("WE GOT A SINK");
        }
        if (hitSearch) {
            // System.out.println("LOOKING FOR A HIT");
            // look cardinally around the last hit
            Coordinate top = new Coordinate(lastHit.getXCoordinate(), lastHit.getYCoordinate() - 1);
            Coordinate bottom = new Coordinate(lastHit.getXCoordinate(), lastHit.getYCoordinate() + 1);
            Coordinate left = new Coordinate(lastHit.getXCoordinate() - 1, lastHit.getYCoordinate());
            Coordinate right = new Coordinate(lastHit.getXCoordinate() + 1, lastHit.getYCoordinate());
            System.out.println("top: " + top + " bottom: " + bottom + " left: " + left + " right: " + right);

            // can't go any higher up
            if (missedCoordinates.contains(top) || top.getYCoordinate() < 0) 
                prTop = 0;
            else 
                prTop = 0.25;
            // limited by bottom
            if (missedCoordinates.contains(bottom) || bottom.getYCoordinate() >= boardHeight) 
                prBottom = 0;
            else 
                prBottom = 0.25;
            if (missedCoordinates.contains(left) || left.getXCoordinate() < 0) 
                prLeft = 0;
            else 
                prLeft = 0.25;
            if (missedCoordinates.contains(right) || right.getXCoordinate() >= boardWidth) 
                prRight = 0;
            else 
                prRight = 0.25;

            // If top has already been attacked, see if thats an indicator or if we can keep going
            if (prTop != 0){
                System.out.println("top has potential");
                // can keep expanding
                if (hitCoordinates.contains(top)){
                    // keep expanding up
                    while (hitCoordinates.contains(top)){ 
                        top = new Coordinate(top.getXCoordinate(), top.getYCoordinate() - 1);
                    }
                } 
                // if we reach an indicator 
                if (attackedCoordinates.contains(top) || top.getYCoordinate() < 0){
                    System.out.println("indicator at top: " + top.getXCoordinate() + " " + top.getYCoordinate());
                    System.out.println("contains? " + attackedCoordinates.contains(top) + " too high? " + top.getYCoordinate() + " " + boardHeight);
                    prTop = 0;
                // have not explored, this has potential
                } else {
                    prTop = 1;
                    x = top.getXCoordinate();
                    y = top.getYCoordinate();
                }
            }
            if (prTop == 0 && prBottom != 0){
                System.out.println("bottom has potential");
                // can keep expanding
                if (hitCoordinates.contains(bottom)){
                    // keep expanding bottom
                    while (hitCoordinates.contains(bottom)){ 
                        bottom = new Coordinate(bottom.getXCoordinate(), bottom.getYCoordinate() + 1);
                    }
                } 
                // if we reach an indicator 
                if (attackedCoordinates.contains(bottom) || bottom.getYCoordinate() >= boardHeight){
                    System.out.println("indicator at bottom: " + bottom.getXCoordinate() + " " + bottom.getYCoordinate());
                    System.out.println("contains? " + attackedCoordinates.contains(bottom) + " too high? " + bottom.getYCoordinate() + " " + boardHeight);
                    prBottom = 0;
                // have not explored, this has potential
                } else {
                    prBottom = 1;
                    x = bottom.getXCoordinate();
                    y = bottom.getYCoordinate();
                }
            } if (prTop == 0 && prBottom == 0 && prLeft != 0){
                System.out.println("left has potential");
                // can keep expanding
                if (hitCoordinates.contains(left)){
                    // keep expanding left
                    while (hitCoordinates.contains(left)){ 
                        left = new Coordinate(left.getXCoordinate() - 1, left.getYCoordinate());
                    }
                } 
                // if we reach an indicator 
                if (attackedCoordinates.contains(left) || left.getXCoordinate() < 0){
                    System.out.println("indicator at left: " + left.getXCoordinate() + " " + left.getYCoordinate());
                    System.out.println("contains? " + attackedCoordinates.contains(left) + " too high? " + left.getXCoordinate() + " " + boardWidth);
                    prLeft = 0;
                // have not explored, this has potential
                } else {
                    prLeft = 1;
                    x = left.getXCoordinate();
                    y = left.getYCoordinate();
                }
            }if (prTop == 0 && prBottom == 0 && prLeft == 0 && prRight != 0){
                System.out.println("right has potential");
                System.out.println("contains" + attackedCoordinates.contains(right) + " " + right.getXCoordinate() + " " + boardWidth);
                // can keep expanding
                if (hitCoordinates.contains(right)){
                    // keep expanding right
                    while (hitCoordinates.contains(right)){ 
                        right = new Coordinate(right.getXCoordinate() + 1, right.getYCoordinate());
                        System.out.println("indicator at right: " + right.getXCoordinate() + " " + right.getYCoordinate());
                    }
                } 
                // if we reach an indicator 
                if (attackedCoordinates.contains(right) || right.getXCoordinate() >= boardWidth){
                    prRight = 0;
                // have not explored, this has potential
                } else {
                    prRight = 1;
                    x = right.getXCoordinate();
                    y = right.getYCoordinate();
                }
            } 
            if (prTop == 0 && prBottom == 0 && prLeft == 0 && prRight == 0) {
                int randomIndex = random.nextInt(options);
                Coordinate randomCoordinate = diagonalCoordinates.get(randomIndex);
                x = randomCoordinate.getXCoordinate();
                y = randomCoordinate.getYCoordinate(); 
            }
            System.out.println("probs: " + prTop + " " + prBottom + " " + prLeft + " " + prRight);
            
        } else {
            int randomIndex = random.nextInt(options);
            Coordinate randomCoordinate = diagonalCoordinates.get(randomIndex);
            x = randomCoordinate.getXCoordinate();
            y = randomCoordinate.getYCoordinate();
        }

        // for (int row=0; row<boardWidth; row++){
        //     for (int col=0; col<boardHeight; col++){
        //         if (enemyBoard[row][col] == unknown){
        //             System.out.println("all sides are misses");
        //             x = row;
        //             y = col;
        //             missedCoordinates.add(new Coordinate(row, col));
        //         }
        //     }
        // }
                
        if (x >= boardWidth || y >= boardHeight || x < 0 || y < 0 || attackedCoordinates.contains(new Coordinate(x, y))) {
            System.out.println("fixing!!" + x + " " + y);
            int randomIndex = random.nextInt(options);
            Coordinate randomCoordinate = diagonalCoordinates.get(randomIndex);
            x = randomCoordinate.getXCoordinate();
            y = randomCoordinate.getYCoordinate();
        }

        System.out.println("im gonna attack " + x + " " + y);
        System.out.println(" ");
        attackedCoordinates.add(new Coordinate(x, y));
        if (diagonalCoordinates.contains(new Coordinate(x,y))) diagonalCoordinates.remove(new Coordinate(x, y));
        return new Coordinate(x, y); 
    }
    

    @Override
    public void afterGameEnds(final GameView game) {}

}
