package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import org.jpl7.Query;


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
    private Outcome     outcome;
    private Situation   sit;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public AttackBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
        sit     = Situation.getCurrentSituation(myAgent);
    }

    @Override
    public void action() {
        Vector3f lastPosition   = myAgent.getEnemyLocation(sit.enemy);

        //| ========== COMPUTATION ==========
        /**
        if(lastPosition != null) {
            myAgent.goTo(lastPosition);
            myAgent.addLogEntry("going to last position");
        }
         **/

        if(sit.enemy != null){
            if (myAgent.isVisible(sit.enemy, MosimaAgent.VISION_DISTANCE)) {
                myAgent.lookAt(lastPosition);
                myAgent.addLogEntry("Enemy visible !");
                myAgent.saveCSV("/ressources/learningBase/stateChanged/", "results_sawEnemy", true);
                if (askForFirePermission()){
                    if (myAgent.canShoot()) {
                        myAgent.addLogEntry("FIRE !");
                        myAgent.shoot(sit.enemy);
                        myAgent.confirmShoot();
                        myAgent.lastAction = "attacking";
                    }else{
                        myAgent.addLogEntry("Reloading ...");
                    }
                }
            }
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














