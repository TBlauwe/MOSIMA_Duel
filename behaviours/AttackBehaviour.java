package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import org.jpl7.Query;
import sma.AbstractAgent;

import java.lang.reflect.Modifier;


public class AttackBehaviour extends AbstractFSMSimpleBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID    = 4340498260100499547L;
    private static final long   FORGET_TIME         = 35;


    //| ============================
    //| ========== ENUMS ==========
    //| ============================
    public enum Outcome{
        DEFAULT(-1);

        private int value;
        Outcome(int value) { this.value = value; }
        public int getValue() { return this.value; }
    }

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private String      enemy;
    private Outcome     outcome;
    private long        lastTimeSeen;
    private Vector3f    lastPosition;
    private Situation   sit;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public AttackBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
        sit = Situation.getCurrentSituation(myAgent);
        enemy = sit.enemy;
        lastPosition = myAgent.getEnemyLocation(enemy);
        lastTimeSeen = System.currentTimeMillis();
    }

    @Override
    public void action() {
        myAgent.goTo(lastPosition);

        if (myAgent.isVisible(enemy, MosimaAgent.VISION_DISTANCE)) {
            lastTimeSeen = System.currentTimeMillis();
            lastPosition = myAgent.getEnemyLocation(enemy);
            myAgent.lookAt(lastPosition);

            if (askForFirePermission()) {
                myAgent.addLogEntry("Enemy visible, FIRE !");
                myAgent.lastAction = Situation.SHOOT;
                myAgent.shoot(enemy);
            }

        } else {
            if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME) {
                myAgent.addLogEntry("The Enemy run away");
            }
            myAgent.lastAction = Situation.FOLLOW;
        }
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
    private boolean askForFirePermission() {
        String query = "toOpenFire("
                + sit.enemyInSight + ","
                + sit.impactProba + ")";
        return Query.hasSolution(query);
    }
}














