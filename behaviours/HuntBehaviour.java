package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.agents.FinalAgent;

import java.util.ArrayList;

public class HuntBehaviour extends TickerBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long serialVersionUID = -183650362971906511L;

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private MosimaAgent agent;
    private InterestPoint target;
    private InterestPoint lastTarget;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public HuntBehaviour(Agent a, long period) {
        super(a, period);
        agent = (MosimaAgent) a;
    }

    public HuntBehaviour(Agent a, long period, InterestPoint firstPoint) { // Called when getting back from Attack
        super(a, period);
        agent = (MosimaAgent) a;
        target = firstPoint;
        agent.log("Starting the Hunt");
    }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    @Override
    protected void onTick() {
        Tuple2<Vector3f, String> enemy = checkEnemyInSight(agent, false);
        agent.lastAction = Situation.HUNT;

        if (target != null) {
            agent.log("Distance to target : " + agent.getSpatial().getWorldTranslation().distance(target.position));
        } else {
            agent.log("no target");
        }

        if (target != null && agent.getSpatial().getWorldTranslation().distance(target.position) < 5f) {
            target.lastVisit = System.currentTimeMillis();
            lastTarget = target;
            target = null;
            enemy = checkEnemyInSight(agent, true);
            agent.log("CheckPoint !");
        }

        if (enemy != null) {
            agent.log("Enemy in sight");
            agent.removeBehaviour(this);
            AttackBehaviour a = new AttackBehaviour(agent, FinalAgent.PERIOD, enemy.getSecond());
            agent.currentBehavior = a;
            agent.addBehaviour(a);
            return;
        }

        if (target == null) {
            agent.log("Looking for new target ...");

            InterestPoint point = findNextInterestPoint();

            if (point != null) {
                target = point;
                agent.goTo(point.position);
                agent.log("Found it : " + point.position);

            } else {
                ExplorationBehaviour ex = new ExplorationBehaviour(agent, FinalAgent.PERIOD);
                agent.currentBehavior = ex;
                agent.addBehaviour(ex);
                stop();
            }
        }

    }

    private static Tuple2<Vector3f, String> checkEnemyInSight(FinalAgent agent, boolean fullVision) {
        ArrayList<Tuple2<Vector3f, String>> enemies = agent.getVisibleAgents((fullVision) ? (float) (Math.PI * 2f) : AbstractAgent.VISION_DISTANCE, AbstractAgent.VISION_ANGLE);

        Tuple2<Vector3f, String> best = null;
        float value = -1f;

        for (Tuple2<Vector3f, String> enemy : enemies) {
            float tmp = evaluateEnemy(agent, enemy);
            if (tmp > value) {
                best = enemy;
                value = tmp;
            }
        }
        return best;
    }

    private static float evaluateEnemy(FinalAgent agent, Tuple2<Vector3f, String> enemy) {
        return NewEnv.MAX_DISTANCE - agent.getSpatial().getWorldTranslation().distance(enemy.getFirst());
    }

    private InterestPoint findNextInterestPoint() {
        float value = -1f;
        InterestPoint best = null;
        long time = System.currentTimeMillis();

        for (InterestPoint point : agent.offPoints) {

            float tmp = evaluateInterestPoint(point, time);
            if (tmp > value && point != lastTarget) {
                best = point;
                value = tmp;
            }

        }

        return best;
    }

    private float evaluateInterestPoint(InterestPoint point, long time) {
        float dist = agent.getSpatial().getWorldTranslation().distance(point.position);
        long idleness = (time - point.lastVisit) / 1000;
        return NewEnv.MAX_DISTANCE - dist + 5 * Math.max(30 - idleness, 0); // Au pif
    }
}
