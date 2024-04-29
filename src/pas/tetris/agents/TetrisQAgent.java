package src.pas.tetris.agents;


import java.util.ArrayList;
// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.util.HashMap;
import java.util.Map;



// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Block;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;
import edu.bu.tetris.utils.Coordinate;


public class TetrisQAgent
    extends QAgent
{

    public static final double EXPLORATION_PROB = 0.05;

    private Random random;

    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int numPixelsInImage = Board.NUM_ROWS * Board.NUM_COLS;
        final int hiddenDim = 2 * numPixelsInImage;
        final int outDim = 1;

        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(numPixelsInImage, hiddenDim));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(hiddenDim, outDim));

        return qFunction;
    }

    /**
        This function is for you to figure out what your features
        are. This should end up being a single row-vector, and the
        dimensions should be what your qfunction is expecting.
        One thing we can do is get the grayscale image
        where squares in the image are 0.0 if unoccupied, 0.5 if
        there is a "background" square (i.e. that square is occupied
        but it is not the current piece being placed), and 1.0 for
        any squares that the current piece is being considered for.
        
        We can then flatten this image to get a row-vector, but we
        can do more than this! Try to be creative: how can you measure the
        "state" of the game without relying on the pixels? If you were given
        a tetris game midway through play, what properties would you look for?
     */

    Matrix qFunctionInput;

    @Override
    public Matrix getQFunctionInput(final GameView game,
        final Mino potentialAction)
    {
        Matrix flattenedImage = null;
        // Matrix qFunctionInput = null;
        if (qFunctionInput == null) qFunctionInput = Matrix.zeros(5 * Board.NUM_COLS * Board.NUM_ROWS, 1);
        
        try
        {
            flattenedImage = game.getGrayscaleImage(potentialAction).flatten();
        } catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        // make a row vector thats like:
        // [grayscale image value, mino type ordinal, row number, col number, rotation]
        // Block[] blocks = potentialAction.getBlocks();

        List<Integer> xCoords = new ArrayList<Integer>();
        List<Integer> yCoords = new ArrayList<Integer>();
        for (int x = 0; x < Board.NUM_ROWS; x++) {
            for (int y = 0; y < Board.NUM_COLS; y++) {
                xCoords.add(x);
                yCoords.add(y);
            }
        }

        Board board = game.getBoard();
        Block[][] blocks = board.getBoard();

        // [grayscale image value, mino type ordinal, row number, col number, highest block]
        // feature index (0,1,2,3,4)
        int k = 0;
        // row (0,21) & col (0,9) index (0, NUM_ROW * NUM_COL) : {(0,0), (0,1), ... (9,21)}
        int j = 0;
        for (int i = 0; i < 5 * Board.NUM_COLS * Board.NUM_ROWS; i++){
            // grayscale image value
            if (k == 0) {
                qFunctionInput.set(i, 0, flattenedImage.get(0, j));
            // mino type (ordinal)
            } else if (k == 1) {
                if (qFunctionInput.get(i - 1, 0) == 1.0){
                    qFunctionInput.set(i, 0, (double) potentialAction.getType().ordinal());
                } else if (qFunctionInput.get(i,0) == 0.0){
                    qFunctionInput.set(i, 0, -1.0);
                }
            // row number
            } else if (k == 2) {
                qFunctionInput.set(i,0, (double) xCoords.get(j));
            // col number
            } else if (k == 3) {
                qFunctionInput.set(i,0, (double) yCoords.get(j));
            // highest row with a block (potential blockage)
            } else if (k == 4) {
                int highestRow = Board.NUM_ROWS;
                for (int row = 0; row < Board.NUM_ROWS; row++) {
                    if (blocks[row][yCoords.get(j)] != null) {
                        highestRow = row;
                        break;
                    }
                }

                qFunctionInput.set(i, 0, (double) highestRow);
                j++; 
            }
            k++;
            if (k==5) k = 0;
        }

        // System.out.println(qFunctionInput);
        
        // potentialAction.
        if (qFunctionInput == null) {
            for (int i = 0; i < 2 * Board.NUM_COLS * Board.NUM_ROWS; i++) {
                if (i % 2 == 0) {
                    qFunctionInput.set(i, 0, flattenedImage.get( i / 2, 0));
                }
            }
        }

        // flattenedImage = game.getGrayscaleImage(potentialAction);
        // add mino type so have it be grayscale value and then minotype
        // flattenedImage = flattenedImage.append(potentialAction.getType().ordinal())
        
        // System.out.println(potentialAction.getType().ordinal());
        // System.out.println(potentialAction.getBlocks());

        // System.out.println();
        // System.out.println("NEW TURN");
        // System.out.println();

        return flattenedImage;
    }

    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game,
        final GameCounter gameCounter)
    {
        return this.getRandom().nextDouble() <= EXPLORATION_PROB;
    }

    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */
    @Override
    public Mino getExplorationMove(final GameView game)
    {
        int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        return game.getFinalMinoPositions().get(randIdx);
    }

    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a phase, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
        LossFunction lossFunction,
        Optimizer optimizer,
        long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                    lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */
    @Override
    public double getReward(final GameView game)
    {
        Board board = game.getBoard();
        Block[][] blocks = board.getBoard();

        int reward = 0;
        // highest row with a block
        int highestRow = board.NUM_ROWS; 

        // highest = bad
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                if (blocks[i][j] == null) {
                    // System.out.print(".");
                } else {
                    // System.out.print("@");
                    if (i < highestRow) {
                        highestRow = i;
                    }
                }
                // System.out.print(" ");
            }
            // reward += (i * consecutiveMax);
            // System.out.println();
        }

        // consecutive 
        int consecutiveMax = 0;
        int consecutive1 = 0;
        int consecutive2 = 0;
        int consecutive3 = 0;
        int consecutive4 = 0;

        // check for blockages also
        for (int i = 0; i < 4; i++){
            int left = 0;
            int right = left;
            while (right < Board.NUM_COLS - 1) {
                if (blocks[Board.NUM_ROWS - i - 1][left] == null){
                    left++;
                    right = left;
                }
                if (blocks[Board.NUM_ROWS - i - 1][right] != null){
                    if (i == 1) consecutive1++;
                    if (i == 2) consecutive2++;
                    if (i == 3) consecutive3++;
                    if (i == 4) consecutive4++;
                    consecutiveMax = Math.max(consecutiveMax, right - left + 1);
                    if (right < Board.NUM_COLS - 1) right++;
                    else break;
                }
                if (blocks[Board.NUM_ROWS - i - 1][right] == null){
                    left = right;
                }
            }
        }

        // check for blockages
        for (int i = 0; i < Board.NUM_COLS; i++) {
            if (blocks[Board.NUM_ROWS - 1][i] == null) {
                for (int j = 0; j < Board.NUM_ROWS; j++) {
                    if (blocks[j][i] != null) {
                        reward -= 50;
                        break;
                    }
                }
            }
        }
        double factor = 0.75;
        // reward += (int) (consecutive1 * Math.pow(factor, 1) + consecutive2 * Math.pow(factor, 2) + consecutive3 * Math.pow(factor, 3) + consecutive4 * Math.pow(factor, 4));
        reward += 25 * (consecutive1 + consecutive2 + consecutive3 + consecutive4);
        reward += 5 * (highestRow - board.NUM_ROWS + 5);
        // reward = consecutiveMax;
        // reward = highestRow * (highestRow - board.NUM_ROWS + 10);
        if (game.didAgentLose()) {
            reward -= 1000;
            // System.out.println("LOSE");
        }
        // if (game.getScoreThisTurn() > 0) System.out.println("JUST EARNED " + game.getScoreThisTurn());

        reward += game.getScoreThisTurn() * 100;
        // System.out.println("reward: " + reward);
        // System.out.println();

        return reward;
    }

}
