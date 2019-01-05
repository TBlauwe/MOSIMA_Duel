package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.Utility.Utils;
import MOSIMA_Duel.Weka.J48Classifier;
import MOSIMA_Duel.agents.MosimaAgent;
import MOSIMA_Duel.env.Situation;
import com.jme3.math.Vector3f;
import jade.core.Agent;
import org.jpl7.JPL;
import org.jpl7.Query;
import weka.core.Instance;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ExplorationBehaviour extends AbstractFSMSimpleBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long serialVersionUID = 4958939169231338495L;
    private static final float RANDOM_EXPLORATION_PROBA = 0.20f;

    //| ============================
    //| ========== ENUMS ==========
    //| ============================
    public enum Outcome {
        DEFAULT(-1),
        EXPLORATION(0),
        ATTACK(1);

        private int value;

        Outcome(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private Outcome outcome;
    private boolean changedTarget;
    private static Vector3f target;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public ExplorationBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        changedTarget = false;
        outcome = Outcome.DEFAULT;

        if (myAgent.hasArrivedToDestination()) {
            if(myAgent.useWeka) {
                findBestPos(); // Call Prolog
                myAgent.addLogEntry("going to the best position based on my knowledge ");
            }else{
                if(Utils.dice(RANDOM_EXPLORATION_PROBA)){
                    myAgent.addLogEntry("going to an random position locally)");
                    target = findRandomNeighbor();
                }else{
                    myAgent.addLogEntry("going to the highest position");
                    target = findHighestNeighbor();
                }
            }
            myAgent.moveTo(target);
            changedTarget = true;
        }
    }

    @Override
    public boolean done() {
        myAgent.lastAction = "exploring";
        if (changedTarget)
            myAgent.trace(getBehaviourName());
        return true;
    }

    @Override
    public int onEnd() {
        return outcome.getValue();
    }

    //| =======================================
    //| ========== PROLOG FUNCTIONS ===========
    //| =======================================
    public static void evaluateBestPos() {
        try {
            ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                    MosimaAgent.NEIGHBORHOOD_DISTANCE,
                    MosimaAgent.CLOSE_PRECISION,
                    MosimaAgent.VISION_ANGLE);

            HashMap<Vector3f, Double> victoryProbabilities = new HashMap<>();

            // Classify each point with trained classifier
            for (Vector3f point : points) {
                Situation situation = Situation.getSituationFromPos(myAgent, point);
                Instance instance = J48Classifier.situationToInstance(situation);
                HashMap.Entry<String, Double> score = J48Classifier.classify(instance);
                if (score.getKey().compareToIgnoreCase("VICTORY") == 0) {
                    victoryProbabilities.put(point, score.getValue());
                } else {
                    victoryProbabilities.put(point, 1 - score.getValue());
                }
            }

            // Get the most promising point (highest victory probabilities)
            // Sort and return a map containing the best one
            Map<Vector3f, Double> bestOne =
                    victoryProbabilities.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .limit(1)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            target = bestOne.entrySet().iterator().next().getKey();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean  findBestPos() {
        ArrayList<Object> terms = new ArrayList<>();
        MOSIMA_Duel.env.Situation sit = MOSIMA_Duel.env.Situation.getCurrentSituation(myAgent);
        terms.add(sit.enemyInSight);
        terms.add("'" + this.getClass().getCanonicalName() + "'" );
        return Query.hasSolution(prologQuery("see", terms));
    }

    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private Vector3f findHighestNeighbor() {
        ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                MosimaAgent.VISION_DISTANCE,
                MosimaAgent.CLOSE_PRECISION,
                MosimaAgent.VISION_ANGLE);
        return getHighest(points);
    }

    private Vector3f findHighestInSight() {
        ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                MosimaAgent.VISION_DISTANCE,
                MosimaAgent.FAR_PRECISION,
                MosimaAgent.VISION_ANGLE);
        return getHighest(points);
    }

    private Vector3f findRandomNeighbor() {
        ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                MosimaAgent.VISION_DISTANCE,
                MosimaAgent.CLOSE_PRECISION,
                MosimaAgent.VISION_ANGLE);
        return points.get(ThreadLocalRandom.current().nextInt(points.size()));
    }

    private Vector3f getHighest(ArrayList<Vector3f> points) {
        float maxHeight = -256;
        Vector3f best = null;

        for (Vector3f v3 : points) {
            if (v3.getY() > maxHeight) {
                best = v3;
                maxHeight = v3.getY();
            }
        }
        return best;
    }

    private Vector3f getLowest(ArrayList<Vector3f> points) {
        float minHeight = 256;
        Vector3f best = null;

        for (Vector3f v3 : points) {
            if (v3.getY() < minHeight) {
                best = v3;
                minHeight = v3.getY();
            }
        }
        return best;
    }

    private String prologQuery (String behaviour, ArrayList < Object > terms){
        StringBuilder query = new StringBuilder(behaviour + "(");
        for (Object t : terms) {
            query.append(t);
            query.append(",");
        }
        return query.substring(0, query.length() - 1) + ")";
    }
}























