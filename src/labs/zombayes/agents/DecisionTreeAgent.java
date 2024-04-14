package src.labs.zombayes.agents;


// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
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
import edu.bu.labs.zombayes.features.quality.Entropy;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;



public class DecisionTreeAgent
    extends SurvivalAgent
{

    public static class DecisionTree
        extends Object
    {

        public static abstract class Node
            extends Object
        {

            private Matrix X;
            private Matrix y_gt;

            public Node(Matrix X, Matrix y_gt)
            {
                this.X = X;
                this.y_gt = y_gt;
            }

            public final Matrix getX() { return this.X; }
            public final Matrix getY() { return this.y_gt; }

            public int getMajorityClass(Matrix X, Matrix y_gt)
            {
                Pair<Matrix, Matrix> uniqueYGtAndCounts = y_gt.unique();
                Matrix uniqueYGtVals = uniqueYGtAndCounts.getFirst();
                Matrix counts = uniqueYGtAndCounts.getSecond();

                // find the argmax of the counts
                int rowIdxOfMaxCount = -1;
                double maxCount = Double.NEGATIVE_INFINITY;

                for(int rowIdx = 0; rowIdx < counts.getShape().getNumRows(); ++rowIdx)
                {
                    if(counts.get(rowIdx, 0) > maxCount)
                    {
                        rowIdxOfMaxCount = rowIdx;
                        maxCount = counts.get(rowIdx, 0);
                    }
                }

                return (int)uniqueYGtVals.get(rowIdxOfMaxCount, 0);
            }

            public abstract int predict(Matrix x);
            public abstract List<Pair<Matrix, Matrix> > getChildData() throws Exception;

        }

        public static class LeafNode
            extends Node
        {

            private int predictedClass;

            public LeafNode(Matrix X, Matrix y_gt)
            {
                super(X, y_gt);
                this.predictedClass = this.getMajorityClass(X, y_gt);
            }

            @Override
            public int predict(Matrix x)
            {
                // predict the class (an integer)
                return this.predictedClass;
            }

            @Override
            public List<Pair<Matrix, Matrix> > getChildData() throws Exception { return null; }

        }

        public static class InteriorNode
            extends Node
        {

            private int             featureIdx;
            private FeatureType     featureType;

            // when we're processing a discrete feature, it is possible that even though that discrete feature
            // can take on any value in its domain (for example, like 5 values), the data we have may not contain
            // all of those values in it. Therefore, whenever we want to predict a test point, it is possible
            // that the test point has a discrete value that we haven't seen before. When we encounter such scenarios
            // we should predict the majority class (aka assign an "out-of-bounds" leaf node)
            private int             majorityClass;

            private List<Double>    splitValues; 
            private List<Node>      children;
            private Set<Integer>    childColIdxs;
            // if this feature is discrete, then |splitValues| = |children|
            // if this feature is continuous, then p = 1

            public InteriorNode(Matrix X, Matrix y_gt, Set<Integer> availableColIdxs)
            {
                super(X, y_gt);
                this.splitValues = new ArrayList<Double>();
                this.children = new ArrayList<Node>();
                this.majorityClass = this.getMajorityClass(X, y_gt);

                // make a deepcopy of the set that is given to us....we need to potentially remove stuff from this
                // so don't use a shallow copy and risk messing up parent nodes (with a shared shallow copy)!
                this.childColIdxs = new HashSet<Integer>(availableColIdxs);

                // quite a lot happens in this method.
                // this method will figure out which feature (amongst all the ones that we are allowed to see)
                // has the "best" quality (as measured by info gain). It will also populate the field 'this.splitValues'
                // with the correct values for that feature.
                // (side note: this is why this method is being called *after* this.splitValues is initialized)
                this.featureIdx = this.pickBestFeature(X, y_gt, availableColIdxs);
                this.featureType = DecisionTree.FEATURE_HEADER[this.getFeatureIdx()];

                // once we know what feature this node has, we need to remove that feature from our children
                // if that feature is discrete.
                // we made a deepcopy of the set so we're all good to in-place remove here.
                if(this.getFeatureType().equals(FeatureType.DISCRETE))
                {
                    this.getChildColIdxs().remove(this.getFeatureIdx());
                }
            }

            //------------------------ some getters and setters (cause this is java) ------------------------
            public int getFeatureIdx() { return this.featureIdx; }
            public final FeatureType getFeatureType() { return this.featureType; }

            private List<Double> getSplitValues() { return this.splitValues; }
            private List<Node> getChildren() { return this.children; }

            public Set<Integer> getChildColIdxs() { return this.childColIdxs; }
            public int getMajorityClass() { return this.majorityClass; }
            //-----------------------------------------------------------------------------------------------

            // make sure we add children in the correct order when we use this!
            public void addChild(Node n) { this.getChildren().add(n); }


            // TODO: complete me!
            private int pickBestFeature(Matrix X, Matrix y_gt, Set<Integer> availableColIdxs) {
                System.out.println("pickbestfeature");
                InteriorNode potentialHead;
                ArrayList<Pair<Double, Integer>> featureEntropies = new ArrayList<Pair<Double, Integer>>();
                for (int colIdx : availableColIdxs) {
                    potentialHead = new InteriorNode(X, y_gt, availableColIdxs);
                    try {
                        // entropy of the feature before splitting based on feature values
                        // before should just be the feature --> 0 and 1
                        Pair<Double, Matrix> beforeEntropy = potentialHead.getConditionalEntropy(X, y_gt, colIdx);
                        featureEntropies.add(new Pair<Double, Integer>(beforeEntropy.getFirst(), colIdx));
                        // find the best feature to split on
                        // this uses the entropy of the feature before splitting based on feature values
                        // int bestFeature = potentialHead.pickBestFeature(X, y_gt, availableColIdxs);
                    } catch (Exception e) {
                        System.out.println("error");
                        // Handle the exception here
                        e.printStackTrace();
                    }
                }
                System.out.println("feature entropies");
                for (Pair<Double, Integer> featureEntropy : featureEntropies) {
                    System.out.println(featureEntropy.getFirst() + " " + featureEntropy.getSecond());
                }
                int bestColIdx = -1;
                double bestEntropy = Double.NEGATIVE_INFINITY;
                for (Pair<Double, Integer> featureEntropy : featureEntropies) {
                    if (featureEntropy.getFirst() > bestEntropy) {
                        bestColIdx = featureEntropy.getSecond();
                        bestEntropy = featureEntropy.getFirst();
                    }
                }
                return bestColIdx;
            }

            // TODO: complete me!
            private Pair<Double, Matrix> getConditionalEntropy(Matrix X, Matrix y_gt, int colIdx) throws Exception
            {
                System.out.println("getConditionalEntropy");
                if (colIdx == 2) {
                    // Matrix colMatrix = X.getCol(2);
                    System.out.println("Entropy function " + Entropy.entropy(X, 2));
                    // manually calculate the conditional entropy
                    // feature 3 options: 0, 1, 2
                    double f3HumanCount = 0.0;
                    double f3ZombieCount = 0.0;
                    double f30Count = 0.0;
                    double f31Count = 0.0;
                    double f32Count = 0.0;
                    double f30HumanCount = 0.0;
                    double f31HumanCount = 0.0;
                    double f32HumanCount = 0.0;
                    double f30ZombieCount = 0.0;
                    double f31ZombieCount = 0.0;
                    double f32ZombieCount = 0.0;
                    // calculate before split
                    for (int i=0; i < y_gt.getShape().getNumRows(); i++){
                        if (y_gt.get(i, 0) == 0){
                            f3HumanCount++;
                        } else {
                            f3ZombieCount++;
                        }
                    }
                    double f3TotalCount = f3HumanCount + f3ZombieCount;
                    double bEntropy = ((f3HumanCount / f3TotalCount) * (Math.log(f3HumanCount / f3TotalCount) / Math.log(2))) + ((f3ZombieCount / f3TotalCount) * (Math.log(f3ZombieCount / f3TotalCount) / Math.log(2)));
                    bEntropy = -bEntropy;
                    // System.out.println("f3HumanCount " + f3HumanCount);
                    // System.out.println("f3ZombieCount " + f3ZombieCount);
                    // System.out.println("f3 human log term " + (f3HumanCount / f3TotalCount) * (Math.log10(f3HumanCount / f3TotalCount) / Math.log10(2)));
                    // System.out.println("f3 zombie log term " + (f3ZombieCount / f3TotalCount) * (Math.log10(f3ZombieCount / f3TotalCount) / Math.log10(2)));
                    System.out.println("bEntropy " + bEntropy);
                    // calculate after split
                    for (int i=0; i < X.getShape().getNumRows(); i++){
                        if (X.get(i, 2) == 0){
                            f30Count++;
                            if (y_gt.get(i, 0) == 0){
                                f30HumanCount++;
                            } else {
                                f30ZombieCount++;
                            }
                        } else if (X.get(i, 2) == 1){
                            f31Count++;
                            if (y_gt.get(i, 0) == 0){
                                f31HumanCount++;
                            } else {
                                f31ZombieCount++;
                            }
                        } else {
                            f32Count++;
                            if (y_gt.get(i, 0) == 0){
                                f32HumanCount++;
                            } else {
                                f32ZombieCount++;
                            }
                        }
                    }
                    // System.out.println("f30HumanCount " + f30HumanCount);
                    // System.out.println("f30ZombieCount " + f30ZombieCount);
                    // System.out.println("f31HumanCount " + f31HumanCount);
                    // System.out.println("f31ZombieCount " + f31ZombieCount);
                    // System.out.println("f32HumanCount " + f32HumanCount);
                    // System.out.println("f32ZombieCount " + f32ZombieCount);
                    double f30Entropy = ((f30HumanCount / f30Count) * (Math.log(f30HumanCount / f30Count) / Math.log(2))) + ((f30ZombieCount / f30Count) * (Math.log(f30ZombieCount / f30Count) / Math.log(2)));
                    double f31Entropy = ((f31HumanCount / f31Count) * (Math.log(f31HumanCount / f31Count) / Math.log(2))) + ((f31ZombieCount / f31Count) * (Math.log(f31ZombieCount / f31Count) / Math.log(2)));
                    double f32Entropy = ((f32HumanCount / f32Count) * (Math.log(f32HumanCount / f32Count) / Math.log(2))) + ((f32ZombieCount / f32Count) * (Math.log(f32ZombieCount / f32Count) / Math.log(2)));
                    double aEntropy = ((f30Count / f3TotalCount) * -f30Entropy) + ((f31Count / f3TotalCount) * -f31Entropy) + ((f32Count / f3TotalCount) * -f32Entropy);
                    // System.out.println("f30Entropy " + f30Entropy);
                    // System.out.println("f31Entropy " + f31Entropy);
                    // System.out.println("f32Entropy " + f32Entropy);
                    System.out.println("aEntropy " + aEntropy);
                    System.out.println("information gain " + (bEntropy - aEntropy));
                    return new Pair<Double, Matrix>((bEntropy - aEntropy), X);
                } else if (colIdx == 3) {
                    // Matrix colMatrix = X.getCol(3);
                    System.out.println("Entropy function " + Entropy.entropy(X, 3));
                    // manually calculate the conditional entropy
                    // feature 4 options: 0, 1, 2, 3
                    double f4HumanCount = 0.0;
                    double f4ZombieCount = 0.0;
                    double f40Count = 0.0;
                    double f41Count = 0.0;
                    double f42Count = 0.0;
                    double f43Count = 0.0;
                    double f40HumanCount = 0.0;
                    double f41HumanCount = 0.0;
                    double f42HumanCount = 0.0;
                    double f43HumanCount = 0.0;
                    double f40ZombieCount = 0.0;
                    double f41ZombieCount = 0.0;
                    double f42ZombieCount = 0.0;
                    double f43ZombieCount = 0.0;
                    // calculate before split
                    for (int i=0; i < y_gt.getShape().getNumRows(); i++){
                        if (y_gt.get(i, 0) == 0){
                            f4HumanCount++;
                        } else {
                            f4ZombieCount++;
                        }
                    }
                    double f4TotalCount = f4HumanCount + f4ZombieCount;
                    double bEntropy = ((f4HumanCount / f4TotalCount) * (Math.log(f4HumanCount / f4TotalCount) / Math.log(2))) + ((f4ZombieCount / f4TotalCount) * (Math.log(f4ZombieCount / f4TotalCount) / Math.log(2)));
                    bEntropy = -bEntropy;
                    // System.out.println("f4HumanCount " + f4HumanCount);
                    // System.out.println("f4ZombieCount " + f4ZombieCount);
                    // System.out.println("f4 human log term " + (f4HumanCount / f4TotalCount) * (Math.log10(f4HumanCount / f4TotalCount) / Math.log10(2)));
                    // System.out.println("f4 zombie log term " + (f4ZombieCount / f4TotalCount) * (Math.log10(f4ZombieCount / f4TotalCount) / Math.log10(2)));
                    System.out.println("bEntropy " + bEntropy);
                    // calculate after split
                    for (int i=0; i < X.getShape().getNumRows(); i++){
                        if (X.get(i, 3) == 0){
                            f40Count++;
                            if (y_gt.get(i, 0) == 0){
                                f40HumanCount++;
                            } else {
                                f40ZombieCount++;
                            }
                        } else if (X.get(i, 3) == 1){
                            f41Count++;
                            if (y_gt.get(i, 0) == 0){
                                f41HumanCount++;
                            } else {
                                f41ZombieCount++;
                            }
                        } else if (X.get(i, 3) == 2){
                            f42Count++;
                            if (y_gt.get(i, 0) == 0){
                                f42HumanCount++;
                            } else {
                                f42ZombieCount++;
                            }
                        } else {
                            f43Count++;
                            if (y_gt.get(i, 0) == 0){
                                f43HumanCount++;
                            } else {
                                f43ZombieCount++;
                            }
                        }
                    }
                    // System.out.println("f40HumanCount " + f40HumanCount);
                    // System.out.println("f40ZombieCount " + f40ZombieCount);
                    // System.out.println("f41HumanCount " + f41HumanCount);
                    // System.out.println("f41ZombieCount " + f41ZombieCount);
                    // System.out.println("f42HumanCount " + f42HumanCount);
                    // System.out.println("f42ZombieCount " + f42ZombieCount);
                    // System.out.println("f43HumanCount " + f43HumanCount);
                    // System.out.println("f43ZombieCount " + f43ZombieCount);
                    double f40Entropy = ((f40HumanCount / f40Count) * (Math.log(f40HumanCount / f40Count) / Math.log(2))) + ((f40ZombieCount / f40Count) * (Math.log(f40ZombieCount / f40Count) / Math.log(2)));
                    double f41Entropy = ((f41HumanCount / f41Count) * (Math.log(f41HumanCount / f41Count) / Math.log(2))) + ((f41ZombieCount / f41Count) * (Math.log(f41ZombieCount / f41Count) / Math.log(2)));
                    double f42Entropy = ((f42HumanCount / f42Count) * (Math.log(f42HumanCount / f42Count) / Math.log(2))) + ((f42ZombieCount / f42Count) * (Math.log(f42ZombieCount / f42Count) / Math.log(2)));
                    double f43Entropy = ((f43HumanCount / f43Count) * (Math.log(f43HumanCount / f43Count) / Math.log(2))) + ((f43ZombieCount / f43Count) * (Math.log(f43ZombieCount / f43Count) / Math.log(2)));
                    double aEntropy = ((f40Count / f4TotalCount) * -f40Entropy) + ((f41Count / f4TotalCount) * -f41Entropy) + ((f42Count / f4TotalCount) * -f42Entropy) + ((f43Count / f4TotalCount) * -f43Entropy);
                    // System.out.println("f40Entropy " + f40Entropy);
                    // System.out.println("f41Entropy " + f41Entropy);
                    // System.out.println("f42Entropy " + f42Entropy);
                    // System.out.println("f43Entropy " + f43Entropy);
                    System.out.println("aEntropy " + aEntropy);
                    System.out.println("information gain " + (bEntropy - aEntropy));
                    return new Pair<Double, Matrix>((bEntropy - aEntropy), X);
                }
                return new Pair<Double, Matrix>(-1.0, X);
            }

            // TODO: complete me!
            @Override
            public int predict(Matrix x)
            {
                System.out.println("predict");
                return -1;
            }

            // TODO: complete me!
            @Override
            public List<Pair<Matrix, Matrix> > getChildData() throws Exception
            {
                System.out.println("getchilddata");
                return null;
            }

        }

        public Node root;
        public static final FeatureType[] FEATURE_HEADER = {FeatureType.CONTINUOUS,
                                                            FeatureType.CONTINUOUS,
                                                            FeatureType.DISCRETE,
                                                            FeatureType.DISCRETE};

        public DecisionTree()
        {
            this.root = null;
        }

        public Node getRoot() { return this.root; }
        private void setRoot(Node n) { this.root = n; }

        // TODO: complete me!
        private Node dfsBuild(Matrix X, Matrix y_gt, Set<Integer> availableColIdxs) throws Exception
        {
            System.out.println("dfsbuild");
            // how many options should we compare?
            int numOptions = availableColIdxs.size();
            if (numOptions == 0) {
                return new LeafNode(X, y_gt);
            } else {
                InteriorNode potentialHead = new InteriorNode(X, y_gt, availableColIdxs);
                potentialHead.children = new ArrayList<>(potentialHead.pickBestFeature(X, y_gt, availableColIdxs));
                // create a node for where we're currently at
                // InteriorNode potentialHead = new InteriorNode(X, y_gt, availableColIdxs);
                // potentialHead.children = new ArrayList<>(potentialHead.pickBestFeature(X, y_gt, availableColIdxs));
                // find the best feature to split X on
                // for (int colIdx : availableColIdxs) {
                //     potentialHead = new InteriorNode(X, y_gt, availableColIdxs);
                //     // entropy of the feature before splitting based on feature values
                //     // before should just be the feature --> 0 and 1
                //     // Pair<Double, Matrix> beforeEntropy = potentialHead.getConditionalEntropy(X, y_gt, colIdx); 
                //     // find the best feature to split on
                //     // this uses the entropy of the feature before splitting based on feature values
                //     int bestFeature = potentialHead.pickBestFeature(X, y_gt, availableColIdxs);
                // }
            }
            

            System.out.println(X);
            System.out.println(" ");
            System.out.println(X.getRowMaskEq(1.0, 2));

            // Set<Integer> remainingColIdxs = new HashSet<Integer>(availableColIdxs);
            // for (int colIdx : availableColIdxs) {
            //     Matrix colMatrix = X.getCol(colIdx);
            //     System.out.println(Entropy.entropy(y_gt, colIdx));
            // }

            // look at the two discrete features first
            // feature 3
            
            // if (availableColIdxs.contains(3)) {
            //     Matrix colMatrix = X.getCol(3);
            //     System.out.println(Entropy.entropy(colMatrix, 3));
            // }

            // InteriorNode head = new InteriorNode(X, y_gt, availableColIdxs);
            // System.out.println(Entropy.entropy(X, 2));
            // if (availableColIdxs.size() == 0) {
            //     return new LeafNode(X, y_gt);
            // }
            return new LeafNode(X, y_gt);
        }
        

        public void fit(Matrix X, Matrix y_gt)
        {
            System.out.println("DecisionTree.fit: X.shape=" + X.getShape() + " y_gt.shape=" + y_gt.getShape());
            try
            {
                Set<Integer> allColIdxs = new HashSet<Integer>();
                for(int colIdx = 0; colIdx < X.getShape().getNumCols(); ++colIdx)
                {
                    allColIdxs.add(colIdx);
                }
                this.setRoot(this.dfsBuild(X, y_gt, allColIdxs));
            } catch(Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        public int predict(Matrix x)
        {
            // class 0 means Human (i.e. not a zombie), class 1 means zombie
            System.out.println("DecisionTree.predict: x=" + x);
            return this.getRoot().predict(x);
        }

    }

    private DecisionTree tree;

    public DecisionTreeAgent(int playerNum, String[] args)
    {
        super(playerNum, args);
        this.tree = new DecisionTree();
        System.out.println("DecisionTreeAgent created");

    }

    public DecisionTree getTree() { return this.tree; }

    @Override
    public void train(Matrix X, Matrix y_gt)
    {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getTree().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector)
    {
        return this.getTree().predict(featureRowVector);
    }

}
