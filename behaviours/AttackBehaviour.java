package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.jpl7.Query;
import sma.AbstractAgent;
import sma.actionsBehaviours.PrologBehavior;


public class AttackBehaviour extends TickerBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID    = 4340498260100499547L;
    private static final long   FORGET_TIME         = 35;


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private MosimaAgent agent;
    private String      enemy;
    private long        lastTimeSeen;
    private Vector3f     lastPosition;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public AttackBehaviour(Agent a, long period, String enemy) {
        super(a, period);
        this.enemy = enemy;
        agent = (MosimaAgent) a;
        lastPosition = agent.getEnemyLocation(enemy);
        lastTimeSeen = System.currentTimeMillis();
        agent.log("Attacking !");
    }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    @Override
    protected void onTick() {
        agent.goTo(lastPosition);

        if (agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)) {
            lastTimeSeen = System.currentTimeMillis();
            lastPosition = agent.getEnemyLocation(enemy);
            agent.lookAt(lastPosition);

            if (askForFirePermission()) {
                agent.log("Enemy visible, FIRE !");
                agent.lastAction = Situation.SHOOT;
                agent.shoot(enemy);
            }

        } else {
            if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME * getPeriod()) {
                agent.log("The Enemy run away");
                agent.removeBehaviour(this);
                agent.currentBehavior = null;
            }
            agent.lastAction = Situation.FOLLOW;
        }
    }

    private static boolean askForFirePermission() {
        String query = "toOpenFire("
                + DecisionBehaviour.sit.enemyInSight + ","
                + DecisionBehaviour.sit.impactProba + ")";
        return Query.hasSolution(query);
    }
}














