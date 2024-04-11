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

        // DISCRETE features
        // indexed by range 
        // feature 3: domain {0,1,2}
        List<Double> f3HumanProbs = new ArrayList<Double>();
        List<Double> f3ZombieProbs = new ArrayList<Double>();
        // feature 4: domain {0,1,2,3}
        List<Double> f4HumanProbs = new ArrayList<Double>();
        List<Double> f4ZombieProbs = new ArrayList<Double>();

        // CONTINUOUS features
        // mean of feature 1 for humans
        double f1HumanMean = 0;
        // mean of feature 1 for zombies
        double f1ZombieMean = 0;
        // sum of feature 1 for humans
        double f1HumanSum = 0;
        // sum of feature 1 for zombies
        double f1ZombieSum = 0;
        // mean of feature 2 for humans
        double f2HumanMean = 0;
        // mean of feature 2 for zombies
        double f2ZombieMean = 0;
        // sum of feature 2 for humans
        double f2HumanSum = 0;
        // sum of feature 2 for zombies
        // basically turn continuous into discrete
        // feature 1
        Pair <Double, Double> f1HumanStd1 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f1HumanStd2 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f1HumanStd3 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f1ZombieStd1 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f1ZombieStd2 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f1ZombieStd3 = new Pair<Double, Double>(0.0, 0.0);
        // feature 2
        Pair <Double, Double> f2HumanStd1 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f2HumanStd2 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f2HumanStd3 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f2ZombieStd1 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f2ZombieStd2 = new Pair<Double, Double>(0.0, 0.0);
        Pair <Double, Double> f2ZombieStd3 = new Pair<Double, Double>(0.0, 0.0);

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
            List<Double> f3Human = new ArrayList<Double>();
            // how many zombies have these particular feature values
            List<Double> f3Zombie = new ArrayList<Double>();
            for (int i = 0; i < 3; i++) {
                f3Human.add(0.0);
                f3Zombie.add(0.0);
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
            // redistrubte in case 0 probability
            for (int i = 0; i < 3; i++) {
                if (f3Human.get(i) == 0) {
                    double f3HumanRedistributeAmount = 0.0;
                    for (int j = 0; j < 3; j++) {
                        if (f3Human.get(j) != 0) {
                            f3HumanRedistributeAmount += 0.1;
                        }
                    }
                    double f3HumanRedistribution = f3HumanRedistributeAmount/3;
                    for (int j = 0; j < 3; j++) {
                        f3Human.set(j, f3Human.get(j) + f3HumanRedistribution);
                    }
                }
                if (f3Zombie.get(i) == 0) {
                    double f3ZombieRedistributeAmount = 0.0;
                    for (int j = 0; j < 3; j++) {
                        if (f3Zombie.get(j) != 0) {
                            f3ZombieRedistributeAmount += 0.1;
                        }
                    }
                    double f3ZombieRedistribution = f3ZombieRedistributeAmount/3;
                    for (int j = 0; j < 3; j++) {
                        f3Zombie.set(j, f3Zombie.get(j) + f3ZombieRedistribution);
                    }
                }
            }
            // System.out.println("f3Human " + f3Human);
            // System.out.println("f3Zombie " + f3Zombie);

            // feature 4: domain {0,1,2,3}
            // how many humans have these particular feature values
            List<Double> f4Human = new ArrayList<Double>();
            // how many zombies have these particular feature values
            List<Double> f4Zombie = new ArrayList<Double>();
            for (int i = 0; i < 4; i++) {
                f4Human.add(0.0);
                f4Zombie.add(0.0);
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
            // redistrubte in case 0 probability
            for (int i = 0; i < 3; i++) {
                if (f4Human.get(i) == 0) {
                    double f4HumanRedistributeAmount = 0.0;
                    for (int j = 0; j < 3; j++) {
                        if (f4Human.get(j) != 0) {
                            f4HumanRedistributeAmount += 0.1;
                        }
                    }
                    double f4HumanRedistribution = f4HumanRedistributeAmount/3;
                    for (int j = 0; j < 3; j++) {
                        f4Human.set(j, f4Human.get(j) + f4HumanRedistribution);
                    }
                }
                if (f4Zombie.get(i) == 0) {
                    double f4ZombieRedistributeAmount = 0.0;
                    for (int j = 0; j < 3; j++) {
                        if (f4Zombie.get(j) != 0) {
                            f4ZombieRedistributeAmount += 0.1;
                        }
                    }
                    double f4ZombieRedistribution = f4ZombieRedistributeAmount/3;
                    for (int j = 0; j < 3; j++) {
                        f4Zombie.set(j, f4Zombie.get(j) + f4ZombieRedistribution);
                    }
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


            // CONTINUOUS features
            //
            // feature 1
            // go through every row of y_gt
            // increment corresponding value of feature 1
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    f1HumanSum += X.get(i, 0);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f1ZombieSum += X.get(i, 0);
                }
            }
            f1HumanMean = f1HumanSum / totalHumans;
            f1ZombieMean = f1ZombieSum / totalZombies;
            // System.out.println("f1HumanMean " + f1HumanMean);
            // System.out.println("f1ZombieMean " + f1ZombieMean);
            // standard deviation of feature 1 for humans
            double f1HumanStdDev = 0;
            // standard deviation of feature 1 for zombies
            double f1ZombieStdDev = 0;
            // go through every row of y_gt
            // increment corresponding value of feature 1
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    f1HumanStdDev += Math.pow(X.get(i, 0) - f1HumanMean, 2);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f1ZombieStdDev += Math.pow(X.get(i, 0) - f1ZombieMean, 2);
                }
            }
            f1HumanStdDev = Math.pow((f1HumanStdDev / totalHumans),0.5);
            f1ZombieStdDev =  Math.pow((f1HumanStdDev / totalHumans),0.5);
            // System.out.println("f1HumanStdDev " + f1HumanStdDev);
            // System.out.println("f1ZombieStdDev " + f1ZombieStdDev);
            // ranges
            f1HumanStd1 = new Pair<Double, Double>(f1HumanMean - f1HumanStdDev, f1HumanMean + f1HumanStdDev);
            f1HumanStd2 = new Pair<Double, Double>(f1HumanMean - 2*f1HumanStdDev, f1HumanMean + 2*f1HumanStdDev);
            f1HumanStd3 = new Pair<Double, Double>(f1HumanMean - 3*f1HumanStdDev, f1HumanMean + 3*f1HumanStdDev);
            f1ZombieStd1 = new Pair<Double, Double>(f1ZombieMean - f1ZombieStdDev, f1ZombieMean + f1ZombieStdDev);
            f1ZombieStd2 = new Pair<Double, Double>(f1ZombieMean - 2*f1ZombieStdDev, f1ZombieMean + 2*f1ZombieStdDev);
            f1ZombieStd3 = new Pair<Double, Double>(f1ZombieMean - 3*f1ZombieStdDev, f1ZombieMean + 3*f1ZombieStdDev);
            // System.out.println("f1HumanStd1 " + f1HumanStd1.getFirst() + " " + f1HumanStd1.getSecond());
            // System.out.println("f1HumanStd2 " + f1HumanStd2.getFirst() + " " + f1HumanStd2.getSecond());
            // System.out.println("f1HumanStd3 " + f1HumanStd3.getFirst() + " " + f1HumanStd3.getSecond());
            // System.out.println("f1ZombieStd1 " + f1ZombieStd1.getFirst() + " " + f1ZombieStd1.getSecond());
            // System.out.println("f1ZombieStd2 " + f1ZombieStd2.getFirst() + " " + f1ZombieStd2.getSecond());
            // System.out.println("f1ZombieStd3 " + f1ZombieStd3.getFirst() + " " + f1ZombieStd3.getSecond());


            // feature 2
            double f2ZombieSum = 0;
            // go through every row of y_gt
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    f2HumanSum += X.get(i, 1);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f2ZombieSum += X.get(i, 1);
                }
            }
            f2HumanMean = f2HumanSum / totalHumans;
            f2ZombieMean = f2ZombieSum / totalZombies;
            // System.out.println("f2HumanMean " + f2HumanMean);
            // System.out.println("f2ZombieMean " + f2ZombieMean);
            // standard deviation of feature 2 for humans
            double f2HumanStdDev = 0;
            // standard deviation of feature 2 for zombies
            double f2ZombieStdDev = 0;
            // go through every row of y_gt
            for (int i = 0; i < numDataPoints; i++) {
                // if human
                if (y_gt.get(i, 0) == 0) {
                    f2HumanStdDev += Math.pow(X.get(i, 1) - f2HumanMean, 2);
                // if zombie
                } else if (y_gt.get(i, 0) == 1) {
                    f2ZombieStdDev += Math.pow(X.get(i, 1) - f2ZombieMean, 2);
                }
            }
            f2HumanStdDev = Math.pow((f2HumanStdDev / totalHumans),0.5);
            f2ZombieStdDev = Math.pow((f2ZombieStdDev / totalZombies),0.5);
            // System.out.println("f2HumanStdDev " + f2HumanStdDev);
            // System.out.println("f2ZombieStdDev " + f2ZombieStdDev);
            // ranges
            f2HumanStd1 = new Pair<Double, Double>(f2HumanMean - f2HumanStdDev, f2HumanMean + f2HumanStdDev);
            f2HumanStd2 = new Pair<Double, Double>(f2HumanMean - 2*f2HumanStdDev, f2HumanMean + 2*f2HumanStdDev);
            f2HumanStd3 = new Pair<Double, Double>(f2HumanMean - 3*f2HumanStdDev, f2HumanMean + 3*f2HumanStdDev);
            f2ZombieStd1 = new Pair<Double, Double>(f2ZombieMean - f2ZombieStdDev, f2ZombieMean + f2ZombieStdDev);
            f2ZombieStd2 = new Pair<Double, Double>(f2ZombieMean - 2*f2ZombieStdDev, f2ZombieMean + 2*f2ZombieStdDev);
            f2ZombieStd3 = new Pair<Double, Double>(f2ZombieMean - 3*f2ZombieStdDev, f2ZombieMean + 3*f2ZombieStdDev);
            // System.out.println("f2HumanStd1 " + f2HumanStd1.getFirst() + " " + f2HumanStd1.getSecond());
            // System.out.println("f2HumanStd2 " + f2HumanStd2.getFirst() + " " + f2HumanStd2.getSecond());
            // System.out.println("f2HumanStd3 " + f2HumanStd3.getFirst() + " " + f2HumanStd3.getSecond());
            // System.out.println("f2ZombieStd1 " + f2ZombieStd1.getFirst() + " " + f2ZombieStd1.getSecond());
            // System.out.println("f2ZombieStd2 " + f2ZombieStd2.getFirst() + " " + f2ZombieStd2.getSecond());
            // System.out.println("f2ZombieStd3 " + f2ZombieStd3.getFirst() + " " + f2ZombieStd3.getSecond());


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
            double f1 = x.get(0,0);
            double f2 = x.get(0,1);
            int f3 = (int)x.get(0,2);
            int f4 = (int)x.get(0,3);

            double f1HumanProb = 0;
            double f1ZombieProb = 0;

            if (f1 >= f1HumanStd1.getFirst() && f1 <= f1HumanStd1.getSecond()) {
                f1HumanProb = .318;
            } else if (f1 >= f1HumanStd2.getFirst() && f1 <= f1HumanStd2.getSecond()) {
                f1HumanProb = .046;
            } else if (f1 >= f1HumanStd3.getFirst() && f1 <= f1HumanStd3.getSecond()) {
                f1HumanProb = .003;
            } else {
                f1HumanProb = .0001;
            }

            if (f1 >= f1ZombieStd1.getFirst() && f1 <= f1ZombieStd1.getSecond()) {
                f1ZombieProb = .318;
            } else if (f1 >= f1ZombieStd2.getFirst() && f1 <= f1ZombieStd2.getSecond()) {
                f1ZombieProb = .046;
            } else if (f1 >= f1ZombieStd3.getFirst() && f1 <= f1ZombieStd3.getSecond()) {
                f1ZombieProb = .003;
            } else {
                f1ZombieProb = .0001;
            }

            if (f1HumanProb == f1ZombieProb){
                double f1DiffHuman = Math.abs(f1 - f1HumanMean);
                double f1DiffZombie = Math.abs(f1 - f1ZombieMean);
                f1HumanProb = f1DiffHuman/(f1DiffHuman + f1DiffZombie);
                f1ZombieProb = f1DiffZombie/(f1DiffHuman + f1DiffZombie);
            }

            double f2HumanProb = 0;
            double f2ZombieProb = 0;
            if (f2 >= f2HumanStd1.getFirst() && f2 <= f2HumanStd1.getSecond()) {
                f2HumanProb = .318;
            } else if (f2 >= f2HumanStd2.getFirst() && f2 <= f2HumanStd2.getSecond()) {
                f2HumanProb = .046;
            } else if (f2 >= f2HumanStd3.getFirst() && f2 <= f2HumanStd3.getSecond()) {
                f2HumanProb = 003;
            } else {
                f2HumanProb = .0001;
            }

            if (f2 >= f2ZombieStd1.getFirst() && f2 <= f2ZombieStd1.getSecond()) {
                f2ZombieProb = .318;
            } else if (f2 >= f2ZombieStd2.getFirst() && f2 <= f2ZombieStd2.getSecond()) {
                f2ZombieProb = .046;
            } else if (f2 >= f2ZombieStd3.getFirst() && f2 <= f2ZombieStd3.getSecond()) {
                f2ZombieProb = 003;
            } else {
                f2ZombieProb = .0001;
            }

            if (f2HumanProb == f2ZombieProb){
                double f2DiffHuman = Math.abs(f2 - f2HumanMean);
                double f2DiffZombie = Math.abs(f2 - f2ZombieMean);
                f2HumanProb = f2DiffHuman/(f2DiffHuman + f2DiffZombie);
                f2ZombieProb = f2DiffZombie/(f2DiffHuman + f2DiffZombie);
            }

            double humanProb = f1HumanProb * f2HumanProb * f3HumanProbs.get(f3) * f4HumanProbs.get(f4); 
            double zombieProb = f1ZombieProb * f2ZombieProb * f3ZombieProbs.get(f3) * f4ZombieProbs.get(f4);
            // System.out.println("x " + x);
            // System.out.println("f1 " + f1);
            // System.out.println("f2 " + f2);
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
