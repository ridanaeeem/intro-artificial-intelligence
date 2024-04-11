package src.labs.zombayes.agents;


// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.labs.zombayes.agents.SurvivalAgent;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;



public class NaiveBayesAgent
    extends SurvivalAgent
{

    public static class NaiveBayes
        extends Object
    {

        public static final FeatureType[] FEATURE_HEADER = {FeatureType.CONTINUOUS,
                                                            FeatureType.CONTINUOUS,
                                                            FeatureType.DISCRETE,
                                                            FeatureType.DISCRETE};

        List<Double> f3HumanProbs = new ArrayList<Double>();
        List<Double> f3ZombieProbs = new ArrayList<Double>();
        List<Double> f4HumanProbs = new ArrayList<Double>();
        List<Double> f4ZombieProbs = new ArrayList<Double>();

        // TODO: complete me!
        public NaiveBayes()
        {
        }

        // TODO: complete me!
        // X = feature matrix
        // y_gt = ground truth labels
        public void fit(Matrix X, Matrix y_gt)
        {
            // System.out.println(" ");
            // System.out.println("X " + X);
            // System.out.println(" ");
            // System.out.println("y_gt " + y_gt);
            // System.out.println(" ");
            // filltered so that only rows with X[3] = 1 are returned
            // try {
            //     System.out.println("filtered " + X.filterRows(X.getRowMaskEq(1.0,3)));
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }

            int totalDataPoints = X.getShape().getNumRows();
            int numFeatures = X.getShape().getNumCols();
            int dTrainSize = totalDataPoints/20 * 18;
            int dTestSize = totalDataPoints/20;
            int dValSize = totalDataPoints - dTrainSize - dTestSize;

            int numDataPoints = totalDataPoints;

            // DISCRETE features
            //
            // feature 3: domain {0,1,2}
            // how many humans have these particular feature values
            List<Integer> f3Human = new ArrayList<Integer>();
            // how many zombies have these particular feature values
            List<Integer> f3Zombie = new ArrayList<Integer>();
            for (int i = 0; i < 3; i++) {
                f3Human.add(0);
                f3Zombie.add(0);
            }
            // go through every row of y_gt
            // incremenet corresponding value of feature 3
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    // find the value of feature 3 from X (col 2) and that's the index (0-indexed)
                    // replace that with the current value + 1
                    f3Human.set((int)X.get(i, 2), f3Human.get((int)X.get(i, 2)) + 1);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f3Zombie.set((int)X.get(i, 2), f3Zombie.get((int)X.get(i, 2)) + 1);
                }
            }
            // System.out.println("f3Human " + f3Human);
            // System.out.println("f3Zombie " + f3Zombie);

            // feature 4: domain {0,1,2,3}
            // how many humans have these particular feature values
            List<Integer> f4Human = new ArrayList<Integer>();
            // how many zombies have these particular feature values
            List<Integer> f4Zombie = new ArrayList<Integer>();
            for (int i = 0; i < 4; i++) {
                f4Human.add(0);
                f4Zombie.add(0);
            }
            // go through every row of y_gt
            // incremenet corresponding value of feature 4
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    // find the value of feature 4 from X (col 3) and that's the index (0-indexed)
                    // replace that with the current value + 1
                    f4Human.set((int)X.get(i, 3), f4Human.get((int)X.get(i, 3)) + 1);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f4Zombie.set((int)X.get(i, 3), f4Zombie.get((int)X.get(i, 3)) + 1);
                }
            }
            // System.out.println("f4Human " + f4Human);
            // System.out.println("f4Zombie " + f4Zombie);

            // probabilities of discrete features
            // feature 3
            double totalHumans = f3Human.get(0) + f3Human.get(1) + f3Human.get(2);
            double totalZombies = f3Zombie.get(0) + f3Zombie.get(1) + f3Zombie.get(2);
            for (int i = 0; i < 3; i++) {
                f3HumanProbs.add(f3Human.get(i) / totalHumans);
                f3ZombieProbs.add(f3Zombie.get(i) / totalZombies);
            }
            // feature 4
            for (int i = 0; i < 4; i++) {
                f4HumanProbs.add(f4Human.get(i) / totalHumans);
                f4ZombieProbs.add(f4Zombie.get(i) / totalZombies);
            }

            // System.out.println("f3HumanProbs " + f3HumanProbs);
            // System.out.println("f3ZombieProbs " + f3ZombieProbs);
            // System.out.println("f4HumanProbs " + f4HumanProbs);
            // System.out.println("f4ZombieProbs " + f4ZombieProbs);
            return;
        }

        // TODO: complete me!
        // x = feature row vector
        // return 0 or 1
        // 0 = human
        // 1 = zombie
        public int predict(Matrix x)
        {
            int prediction = 1;
            int f3 = (int)x.get(0,2);
            int f4 = (int)x.get(0,3);

            double humanProb = f3HumanProbs.get(f3) * f4HumanProbs.get(f4); 
            double zombieProb = f3ZombieProbs.get(f3) * f4ZombieProbs.get(f4);
            // System.out.println("x " + x);
            // System.out.println("f3 " + f3);
            // System.out.println("f4 " + f4);
            // System.out.println("humanProb " + humanProb);
            // System.out.println("zombieProb " + zombieProb);
            if (humanProb > zombieProb) {
                prediction = 0;
            } 
            return prediction;
        }

    }
    
    private NaiveBayes model;

    public NaiveBayesAgent(int playerNum, String[] args)
    {
        super(playerNum, args);
        this.model = new NaiveBayes();
    }

    public NaiveBayes getModel() { return this.model; }

    @Override
    public void train(Matrix X, Matrix y_gt)
    {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getModel().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector)
    {
        return this.getModel().predict(featureRowVector);
    }

}
