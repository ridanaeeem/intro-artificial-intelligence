package src.labs.pitfall.agents;

// JAVA PROJECT IMPORTS
import edu.bu.labs.pitfall.Difficulty;
import edu.bu.labs.pitfall.Synchronizer;
import edu.bu.labs.pitfall.utilities.Coordinate;
// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BayesianAgent extends Agent {

  public static class PitfallBayesianNetwork extends Object {

    private Map<Coordinate, Boolean> knownBreezeCoordinates;
    private Set<Coordinate> frontierPitCoordinates;
    private Set<Coordinate> otherPitCoordinates;
    private final double pitProb;

    public PitfallBayesianNetwork(Difficulty difficulty) {
      this.knownBreezeCoordinates = new HashMap<Coordinate, Boolean>();

      this.frontierPitCoordinates = new HashSet<Coordinate>();
      this.otherPitCoordinates = new HashSet<Coordinate>();

      this.pitProb = Difficulty.getPitProbability(difficulty);
    }

    public Map<Coordinate, Boolean> getKnownBreezeCoordinates() {
      return this.knownBreezeCoordinates;
    }

    public Set<Coordinate> getFrontierPitCoordinates() {
      return this.frontierPitCoordinates;
    }

    public Set<Coordinate> getOtherPitCoordinates() {
      return this.otherPitCoordinates;
    }

    public final double getPitProb() {
      return this.pitProb;
    }

    /**
     *  TODO: please replace this code. The code here will pick a **random** frontier square to explore next,
     *        which may be a pit! You should do the following steps:
     *          1) for each frontier square X, calculate the query Pr[Pit_X = true | evidence]
     *             we typically expand this to say:
     *                         Pr[Pit_X = true | evidence] = alpha * Pr[Pit_X = true && evidence]
     *             however you don't need to calculate alpha explicitly.
     *             If you calculate Pr[Pit_X = true && evidence] for every X, you can convert the values into
     *             probabilities by adding up all Pr[Pit_X = true && evidence] values and dividing each
     *             Pr[Pit_X = true && evidence] value by the sum.
     *
     *          2) pick the pit that is the least likely to have a pit in it to explore next!
     *
     *          As an aside here, you can certainly choose to calculate Pr[Pit_X = false | evidence] values
     *          instead (and then pick the coordinate with the highest prob), its up to you!
     **/
    public Coordinate getNextCoordinateToExplore() {
      // System.out.println(
      //   "frontier coordinates " + this.getFrontierPitCoordinates()
      // );

      Coordinate toExplore = null;
      double lowestProbVal = 100;

      List<Coordinate> possiblePits = new ArrayList<Coordinate>();
      List<Coordinate> breezes = new ArrayList<Coordinate>();

      // counting breezes doesnt mean anything bc 4 can corrsepond to one pit
      //   int breezeCount = 0;
      //   for (Map.Entry<Coordinate, Boolean> entry : this.getKnownBreezeCoordinates()
      //     .entrySet()) {
      //     if (entry.getValue() == true) {
      //       breezeCount += 1;
      //     }
      //   }

      if (this.getFrontierPitCoordinates().size() > 0) {
        List<Coordinate> frontier = new ArrayList<Coordinate>(
          this.getFrontierPitCoordinates()
        );

        // possible pit combos
        if (frontier.size() > 0) {
          // which frontier coords can even have pits?
          int validPits = 0;
          // see if any of the frontier coords are surrounded by at least one breeze
          for (Coordinate coord : frontier) {
            int x = coord.getXCoordinate();
            int y = coord.getYCoordinate();
            Coordinate left = new Coordinate(x - 1, y);
            Coordinate right = new Coordinate(x + 1, y);
            Coordinate up = new Coordinate(x, y - 1);
            Coordinate down = new Coordinate(x, y + 1);
            
            // assume they have pits
            if (this.getKnownBreezeCoordinates().containsKey(left)) {
              if (this.getKnownBreezeCoordinates().get(left) == true) {
                validPits += 1;
                if (!possiblePits.contains(coord)) possiblePits.add(coord);
                if (!breezes.contains(left)) breezes.add(left);
              } 
            }
            if (this.getKnownBreezeCoordinates().containsKey(right)) {
              if (this.getKnownBreezeCoordinates().get(right) == true) {
                validPits += 1;
                if (!possiblePits.contains(coord)) possiblePits.add(coord);
                if (!breezes.contains(right)) breezes.add(right);
              } 
            }
            if (this.getKnownBreezeCoordinates().containsKey(down)) {
              if (this.getKnownBreezeCoordinates().get(down) == true) {
                validPits += 1;
                if (!possiblePits.contains(coord)) possiblePits.add(coord);
                if (!breezes.contains(down)) breezes.add(down);
              } 
            }
            if (this.getKnownBreezeCoordinates().containsKey(up)) {
              if (this.getKnownBreezeCoordinates().get(up) == true) {
                validPits += 1;
                if (!possiblePits.contains(coord)) possiblePits.add(coord);
                if (!breezes.contains(up)) breezes.add(up);
              } 
            }
            
            // if they are surrounded by a single breeze=false, they do not have a pit
            if (this.getKnownBreezeCoordinates().containsKey(left)){
              if (this.getKnownBreezeCoordinates().get(left) == false) {
                if (possiblePits.contains(coord)) possiblePits.remove(coord);
              }
            }
            if (this.getKnownBreezeCoordinates().containsKey(right)){
              if (this.getKnownBreezeCoordinates().get(right) == false) {
                if (possiblePits.contains(coord)) possiblePits.remove(coord);
              }
            }
            if (this.getKnownBreezeCoordinates().containsKey(down)){
              if (this.getKnownBreezeCoordinates().get(down) == false) {
                if (possiblePits.contains(coord)) possiblePits.remove(coord);
              }
            }
            if (this.getKnownBreezeCoordinates().containsKey(up)){
              if (this.getKnownBreezeCoordinates().get(up) == false) {
                if (possiblePits.contains(coord)) possiblePits.remove(coord);
              }
            }
          }

          // we now have a list of potential pits that makes sense with breeze info
          // System.out.println("validPits " + validPits);
          // System.out.println("might be pits here " + possiblePits);
          // every combo of the possible pits that are near breezes
          List<List<Coordinate>> pitPowerSet = generatePowerSet(possiblePits);

          // refine power set
          List<List<Coordinate>> actuallyPossiblePits = new ArrayList<List<Coordinate>>();
          for (List<Coordinate> subset : pitPowerSet) {
            if (subset.size() > 0) actuallyPossiblePits.add(subset);
          }
          if (pitPowerSet.size() > 0) {
            for (List<Coordinate> subset : pitPowerSet) {
              // make sure that each breeze has a potential pit corresponding to it 
              int pitCount = 0;
              if (subset.size() > 0) {
                for (Coordinate breeze : breezes) {
                  int x = breeze.getXCoordinate();
                  int y = breeze.getYCoordinate();
                  Coordinate left = new Coordinate(x - 1, y);
                  Coordinate right = new Coordinate(x + 1, y);
                  Coordinate up = new Coordinate(x, y - 1);
                  Coordinate down = new Coordinate(x, y + 1);
                  if (subset.contains(left) || subset.contains(right) || subset.contains(up) || subset.contains(down)){ 
                    continue;
                  } else {
                    if (actuallyPossiblePits.contains(subset)){
                      actuallyPossiblePits.remove(subset);
                    }
                  }
                }
              }
            }
          }

          // calculate the probability of each pit combo
          // List<Double> pitProbs = new ArrayList<Double>();
          // probabilities for every square summed up
          double totalProb = 0;
          // query each square in the frontier
          for (Coordinate frontierCoord : frontier){
            double squareProb = 0;
            if (possiblePits.contains(frontierCoord)){
              // default probability for a square
              squareProb = pitProb;
              // for every combo of pits
              for (List<Coordinate> pitCombo : actuallyPossiblePits){
                // if the query square is in the pit combo
                if (pitCombo.contains(frontierCoord)){
                  // find probability of that combo even happening
                  double comboProb = 1;
                  for (Coordinate coord : this.getKnownBreezeCoordinates().keySet()){
                    // probability of a pit being at a coord
                    if (pitCombo.contains(coord)){
                      comboProb *= pitProb;
                    } 
                    // probability of a pit not being at a coord
                    else {
                      comboProb *= (1 - pitProb);
                    }
                  }
                  // add the probability of that combo to the probability of that square
                  squareProb += comboProb;
                } 
              }
            } 
            
            totalProb += squareProb;
            if (squareProb < lowestProbVal){
              lowestProbVal = squareProb;
              toExplore = frontierCoord;
              // System.out.println("we should explore " + toExplore + " with prob " + lowestProbVal);
            } else if (squareProb == lowestProbVal){
              if (new Random().nextBoolean()){
                lowestProbVal = squareProb;
                toExplore = frontierCoord;
              }
            }
            // pitProbs.add(squareProb);
          }
          // for (int i = 0; i < pitProbs.size(); i++){
          //   pitProbs.set(i, pitProbs.get(i) / totalProb);
          // }
          // System.out.println("totalProb " + totalProb);
          // System.out.println("pitProbs " + pitProbs);

          // for (List<Coordinate> pitCombo : pitPowerSet) {
          //   double prob = pitProb;
          //   // probability for each pit in the combo
          //   for (Coordinate pit : pitCombo) {
          //     prob *= pitProb;
          //   }
          //   for (Coordinate frontierSquare : frontier) {
          //     if (!pitCombo.contains(frontierSquare)) {
          //       prob *= (1 - pitProb);
          //     }
          //   }
            // pitProbs.add(prob);
          // }

          // System.out.println("pitProbs " + pitProbs);
        };

        // if there's a breeze thats surrounded on 3/4 sides by not breezes, then its a pit

        // System.out.println("breezes " + breezes);
      }
      return toExplore;
    }
    // for finding every combo of pit placements use a power set
    public static List<List<Coordinate>> generatePowerSet(
      List<Coordinate> list
    ) {
      List<List<Coordinate>> result = new ArrayList<>();
      generatePowerSetHelper(list, 0, new ArrayList<>(), result);
      return result;
    }

    private static void generatePowerSetHelper(
      List<Coordinate> list,
      int index,
      List<Coordinate> current,
      List<List<Coordinate>> result
    ) {
      if (index == list.size()) {
        result.add(new ArrayList<>(current));
        return;
      }

      // add current element + recursive call with it
      current.add(list.get(index));
      generatePowerSetHelper(list, index + 1, current, result);

      // remove current element + recursive call without it
      current.remove(current.size() - 1);
      generatePowerSetHelper(list, index + 1, current, result);
    }
  }

  private int myUnitID;
  private int enemyPlayerNumber;
  private Set<Coordinate> gameCoordinates;
  private Set<Coordinate> unexploredCoordinates;
  private Coordinate coordinateIJustAttacked;
  private Coordinate srcCoordinate;
  private Coordinate dstCoordinate;
  private PitfallBayesianNetwork bayesianNetwork;

  private final Difficulty difficulty;

  public BayesianAgent(int playerNum, String[] args) {
    super(playerNum);
    if (args.length != 3) {
      System.err.println(
        "[ERROR] BayesianAgent.BayesianAgent: need to provide args <playerID> <seed> <difficulty>"
      );
    }

    this.myUnitID = -1;
    this.enemyPlayerNumber = -1;
    this.gameCoordinates = new HashSet<Coordinate>();
    this.unexploredCoordinates = new HashSet<Coordinate>();
    this.coordinateIJustAttacked = null;
    this.srcCoordinate = null;
    this.dstCoordinate = null;
    this.bayesianNetwork = null;

    this.difficulty = Difficulty.valueOf(args[2].toUpperCase());
  }

  public int getMyUnitID() {
    return this.myUnitID;
  }

  public int getEnemyPlayerNumber() {
    return this.enemyPlayerNumber;
  }

  public Set<Coordinate> getGameCoordinates() {
    return this.gameCoordinates;
  }

  public Set<Coordinate> getUnexploredCoordinates() {
    return this.unexploredCoordinates;
  }

  public final Coordinate getCoordinateIJustAttacked() {
    return this.coordinateIJustAttacked;
  }

  public final Coordinate getSrcCoordinate() {
    return this.srcCoordinate;
  }

  public final Coordinate getDstCoordinate() {
    return this.dstCoordinate;
  }

  public PitfallBayesianNetwork getBayesianNetwork() {
    return this.bayesianNetwork;
  }

  public final Difficulty getDifficulty() {
    return this.difficulty;
  }

  private void setMyUnitID(int i) {
    this.myUnitID = i;
  }

  private void setEnemyPlayerNumber(int i) {
    this.enemyPlayerNumber = i;
  }

  private void setCoordinateIJustAttacked(Coordinate c) {
    this.coordinateIJustAttacked = c;
  }

  private void setSrcCoordinate(Coordinate c) {
    this.srcCoordinate = c;
  }

  private void setDstCoordinate(Coordinate c) {
    this.dstCoordinate = c;
  }

  private void setBayesianNetwork(PitfallBayesianNetwork n) {
    this.bayesianNetwork = n;
  }

  @Override
  public Map<Integer, Action> initialStep(
    StateView state,
    HistoryView history
  ) {
    // locate enemy and friendly units
    Set<Integer> myUnitIDs = new HashSet<Integer>();
    for (Integer unitID : state.getUnitIds(this.getPlayerNumber())) {
      myUnitIDs.add(unitID);
    }

    if (myUnitIDs.size() != 1) {
      System.err.println(
        "[ERROR] PitfallAgent.initialStep: should only have 1 unit but found " +
        myUnitIDs.size()
      );
      System.exit(-1);
    }

    // check that all units are archers units
    if (
      !state
        .getUnit(myUnitIDs.iterator().next())
        .getTemplateView()
        .getName()
        .toLowerCase()
        .equals("archer")
    ) {
      System.err.println(
        "[ERROR] PitfallAgent.initialStep: should only control archers!"
      );
      System.exit(1);
    }

    // get the other player
    Integer[] playerNumbers = state.getPlayerNumbers();
    if (playerNumbers.length != 2) {
      System.err.println("ERROR: Should only be two players in the game");
      System.exit(-1);
    }
    Integer enemyPlayerNumber = null;
    if (playerNumbers[0] != this.getPlayerNumber()) {
      enemyPlayerNumber = playerNumbers[0];
    } else {
      enemyPlayerNumber = playerNumbers[1];
    }

    // check enemy units
    Set<Integer> enemyUnitIDs = new HashSet<Integer>();
    for (Integer unitID : state.getUnitIds(enemyPlayerNumber)) {
      if (
        !state
          .getUnit(unitID)
          .getTemplateView()
          .getName()
          .toLowerCase()
          .equals("hiddensquare")
      ) {
        System.err.println(
          "ERROR [BayesianAgent.initialStep]: Enemy should start off with HiddenSquare units!"
        );
        System.exit(-1);
      }
      enemyUnitIDs.add(unitID);
    }

    // initially everything is unknown
    Coordinate coord = null;
    for (Integer unitID : enemyUnitIDs) {
      coord =
        new Coordinate(
          state.getUnit(unitID).getXPosition(),
          state.getUnit(unitID).getYPosition()
        );
      this.getUnexploredCoordinates().add(coord);
      this.getGameCoordinates().add(coord);
    }

    this.setMyUnitID(myUnitIDs.iterator().next());
    this.setEnemyPlayerNumber(enemyPlayerNumber);
    this.setSrcCoordinate(new Coordinate(1, state.getYExtent() - 2));
    this.setDstCoordinate(new Coordinate(state.getXExtent() - 2, 1));
    this.setBayesianNetwork(new PitfallBayesianNetwork(this.getDifficulty()));

    Map<Integer, Action> initialActions = new HashMap<Integer, Action>();
    initialActions.put(
      this.getMyUnitID(),
      Action.createPrimitiveAttack(
        this.getMyUnitID(),
        state.unitAt(
          this.getSrcCoordinate().getXCoordinate(),
          this.getSrcCoordinate().getYCoordinate()
        )
      )
    );
    this.getUnexploredCoordinates().remove(this.getSrcCoordinate());
    return initialActions;
  }

  public boolean isFrontierCoordiante(Coordinate src, StateView state) {
    int dirs[][] = new int[][] { { -1, 0 }, { +1, 0 }, { 0, -1 }, { 0, +1 } };
    for (int dir[] : dirs) {
      int x = src.getXCoordinate() + dir[0];
      int y = src.getYCoordinate() + dir[1];

      if (
        x >= 1 &&
        x <= state.getXExtent() - 2 &&
        y >= 1 &&
        y <= state.getYExtent() - 2 &&
        (
          !state.isUnitAt(x, y) ||
          !state
            .getUnit(state.unitAt(x, y))
            .getTemplateView()
            .getName()
            .toLowerCase()
            .equals("hiddensquare")
        )
      ) {
        return true;
      }
    }
    return false;
  }

  public void makeObservations(StateView state, HistoryView history) {
    this.getBayesianNetwork().getKnownBreezeCoordinates().clear();
    this.getBayesianNetwork().getFrontierPitCoordinates().clear();
    this.getBayesianNetwork().getOtherPitCoordinates().clear();

    Set<Coordinate> exploredCoordinates = new HashSet<Coordinate>();
    for (Integer enemyUnitID : state.getUnitIds(this.getEnemyPlayerNumber())) {
      UnitView enemyUnitView = state.getUnit(enemyUnitID);
      if (
        enemyUnitView
          .getTemplateView()
          .getName()
          .toLowerCase()
          .equals("breezesquare")
      ) {
        this.getBayesianNetwork()
          .getKnownBreezeCoordinates()
          .put(
            new Coordinate(
              enemyUnitView.getXPosition(),
              enemyUnitView.getYPosition()
            ),
            true
          );
      } else if (
        enemyUnitView
          .getTemplateView()
          .getName()
          .toLowerCase()
          .equals("safesquare")
      ) {
        this.getBayesianNetwork()
          .getKnownBreezeCoordinates()
          .put(
            new Coordinate(
              enemyUnitView.getXPosition(),
              enemyUnitView.getYPosition()
            ),
            false
          );
      } else if (
        enemyUnitView
          .getTemplateView()
          .getName()
          .toLowerCase()
          .equals("hiddensquare")
      ) {
        this.getBayesianNetwork()
          .getOtherPitCoordinates()
          .add(
            new Coordinate(
              enemyUnitView.getXPosition(),
              enemyUnitView.getYPosition()
            )
          );
      }

      // now separate out the frontier from the "other" ones
      for (Coordinate unknownCoordinate : this.getBayesianNetwork()
        .getOtherPitCoordinates()) {
        if (this.isFrontierCoordiante(unknownCoordinate, state)) {
          this.getBayesianNetwork()
            .getFrontierPitCoordinates()
            .add(unknownCoordinate);
        }
      }
      this.getBayesianNetwork()
        .getOtherPitCoordinates()
        .removeAll(this.getBayesianNetwork().getFrontierPitCoordinates());
    }
  }

  @Override
  public Map<Integer, Action> middleStep(StateView state, HistoryView history) {
    Map<Integer, Action> actions = new HashMap<Integer, Action>();

    if (Synchronizer.isMyTurn(this.getPlayerNumber(), state)) {
      // get the observation from the past
      if (state.getTurnNumber() > 0) {
        this.makeObservations(state, history);
      }

      Coordinate coordinateOfUnitToAttack =
        this.getBayesianNetwork().getNextCoordinateToExplore();

      // could have won the game (and waiting for enemy units to die)
      // or we have a coordinate to attack
      // we need to check that the unit at that coordinate is a hidden square (not allowed to attack other units)
      if (coordinateOfUnitToAttack != null) {
        Integer unitID = state.unitAt(
          coordinateOfUnitToAttack.getXCoordinate(),
          coordinateOfUnitToAttack.getYCoordinate()
        );
        if (unitID == null) {
          System.err.println(
            "ERROR: BayesianAgent.middleStep: deciding to attack unit at " +
            coordinateOfUnitToAttack +
            " but no unit was found there!"
          );
          System.exit(-1);
        }

        String unitTemplateName = state
          .getUnit(unitID)
          .getTemplateView()
          .getName();
        if (!unitTemplateName.toLowerCase().equals("hiddensquare")) {
          // can't attack non hidden-squares!
          System.err.println(
            "ERROR: BayesianAgent.middleStep: deciding to attack unit at " +
            coordinateOfUnitToAttack +
            " but unit at that square is [" +
            unitTemplateName +
            "] " +
            "and should be a HiddenSquare unit!"
          );
          System.exit(-1);
        }
        this.setCoordinateIJustAttacked(coordinateOfUnitToAttack);

        actions.put(
          this.getMyUnitID(),
          Action.createPrimitiveAttack(this.getMyUnitID(), unitID)
        );
        this.getUnexploredCoordinates().remove(coordinateOfUnitToAttack);
      }
    }

    return actions;
  }

  @Override
  public void terminalStep(StateView state, HistoryView history) {}

  @Override
  public void loadPlayerData(InputStream arg0) {}

  @Override
  public void savePlayerData(OutputStream arg0) {}
}
