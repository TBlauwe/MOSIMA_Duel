package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.jpl7.Query;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.InterestPoint.Type;
import sma.agents.FinalAgent;

import java.util.ArrayList;

public class ExplorationBehaviour extends TickerBehaviour {


    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID = 4958939169231338495L;

    public static final float   RANDOM_MAX_DIST = 10f;
    public static final float   VISION_ANGLE    = 360f;
    public static final float   VISION_DISTANCE = 350f;
    public static final float   CAST_PRECISION  = 2f;
    public static final int     RANDOM_REFRESH  = 20;
    public static boolean       prlNextOffend;


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private MosimaAgent agent;
    private Vector3f    target;
    private Type        targetType;
    private long        randDate;
    private long        time;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public ExplorationBehaviour(Agent a, long period) {
        super(a, period);
        agent = (MosimaAgent) a;
        target = null;
        randDate = 0;
        time = System.currentTimeMillis();
        prlNextOffend = true;
    }


    protected void onTick() {
        if (target == null && !setTarget()) {
            randomMove();
            return;
        }

        if (agent.getCurrentPosition().distance(target) < MosimaAgent.NEIGHBORHOOD_DISTANCE) {
            Vector3f nei = findInterestingNeighbor();
            if (nei != null && agent.getCurrentPosition().distance(nei) < MosimaAgent.NEIGHBORHOOD_DISTANCE / 2f) {
                target = nei;
                agent.moveTo(target);
            } else {
                addInterestPoint();
                target = null;
            }
        }
    }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private void addInterestPoint() {
        if (targetType == Type.Offensive) {
            agent.offPoints.add(new InterestPoint(Type.Offensive, agent));
        } else {
            agent.defPoints.add(new InterestPoint(Type.Defensive, agent));
        }
    }

    private Vector3f findInterestingNeighbor() {
        if (targetType == Type.Offensive) {
            return findHighestNeighbor();
        } else {
            return findLowestNeighbor();
        }
    }

    private Vector3f findHighestNeighbor() {
        ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE, AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
        return getHighest(points);
    }

    private Vector3f findLowestNeighbor() {
        ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE, AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
        return getLowest(points);
    }

    private boolean setTarget() {
        Type t = getNextTargetType();

        if (t == Type.Offensive) {
            target = findOffensiveTarget();
        } else {
            target = findDefensiveTarget();
        }

        if (target != null) {
            agent.goTo(target);
            targetType = t;
            agent.lastAction = (targetType == Type.Offensive) ? Situation.EXPLORE_OFF : Situation.EXPLORE_DEF;
        }
        return target != null;
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

    private Vector3f findOffensiveTarget() {
        ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);

        ArrayList<Vector3f> toRemove = new ArrayList<>();

        for (InterestPoint intPoint : agent.offPoints)
            for (Vector3f point : points)
                if (intPoint.isInfluenceZone(point, Type.Offensive))
                    toRemove.add(point);

        for (Vector3f v3 : toRemove) {
            points.remove(v3);
        }

        return getHighest(points);
    }

    private Vector3f findDefensiveTarget() {
        ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);

        ArrayList<Vector3f> toRemove = new ArrayList<>();

        for (InterestPoint intPoint : agent.defPoints)
            for (Vector3f point : points)
                if (intPoint.isInfluenceZone(point, Type.Defensive))
                    toRemove.add(point);


        for (Vector3f v3 : toRemove) {
            points.remove(v3);
        }

        return getLowest(points);
    }

    private Type getNextTargetType() {

        if (agent.useProlog) {
            String query = "explore_points(" + agent.offPoints.size() + "," + agent.defPoints.size() + ")";
            if (Query.hasSolution(query)) {
                prlNextOffend = false;
            } else {
                prlNextOffend = true;
            }
        }

        return (prlNextOffend) ? Type.Offensive : Type.Defensive;
    }

    private void randomMove() {
        long time = System.currentTimeMillis();
        if (time - randDate > RANDOM_REFRESH * getPeriod()) {
            agent.randomMove(); // Should be something in the neighborhound of the agent, and not some random point in the all map
            randDate = time;
            //agent.getEnvironement().drawDebug(agent.getCurrentPosition(), agent.getDestination());
        }
    }
}























