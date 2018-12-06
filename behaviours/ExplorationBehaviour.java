package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import org.jpl7.Query;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.InterestPoint.Type;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ExplorationBehaviour extends AbstractFSMSimpleBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID = 4958939169231338495L;
    private static final float  RANDOM_GO_HIGH  = 0.80f;

    //| ============================
    //| ========== ENUMS ==========
    //| ============================
    public enum Outcome{
        DEFAULT(-1),
        EXPLORATION(0),
        ATTACK(1);

        private int value;
        Outcome(int value) { this.value = value; }
        public int getValue() { return this.value; }
    }


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private Outcome     outcome;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public ExplorationBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
    }

    @Override
    public void action() {
        if(ThreadLocalRandom.current().nextFloat() <= RANDOM_GO_HIGH){
            myAgent.addLogEntry("going to an higher position");
            Vector3f target = findHighestNeighbor();
            if (target != null && myAgent.getCurrentPosition().distance(target) < MosimaAgent.NEIGHBORHOOD_DISTANCE / 2f) {
                myAgent.moveTo(target);
            }else{
                myAgent.addLogEntry("exploring (random move)");
                myAgent.randomMove();
            }
        }else{
            myAgent.addLogEntry("exploring (random move)");
            myAgent.randomMove();
        }
        //agent.getEnvironement().drawDebug(agent.getCurrentPosition(), agent.getDestination());
    }

    @Override
    public boolean done() {
        myAgent.trace(getBehaviourName());
        return true;
    }

    @Override
    public int onEnd() {
        return outcome.getValue();
    }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private Vector3f findHighestNeighbor() {
        ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                MosimaAgent.NEIGHBORHOOD_DISTANCE,
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
}























