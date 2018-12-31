package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import MOSIMA_Duel.WekaInterface.IWeka;
import MOSIMA_Duel.utils.MapUtil;
import com.jme3.math.Vector3f;
import env.jme.Situation;
import jade.core.Agent;
import org.jpl7.Query;
import org.lwjgl.Sys;
import weka.core.Instance;
import weka.core.Utils;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;


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
    private boolean     act;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public AttackBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        outcome = Outcome.DEFAULT;
        sit     = Situation.getCurrentSituation(myAgent);
        act     = false;

        if(sit.enemy != null){
            if (myAgent.isVisible(sit.enemy, MosimaAgent.VISION_DISTANCE)) {
                String res = Situation.getCurrentSituation(myAgent).toCSVFile();
                myAgent.addCSVEntry(res);
                if (myAgent.canShoot()) { // Is weapon cooldown finished ?
                    if (askForFirePermission()){ // Prolog call
                        // Go to nearest position
                        Vector3f lastPosition   = myAgent.getEnemyLocation(sit.enemy);
                        if(lastPosition != null) {
                                myAgent.goTo(findHighestNeighborClosestTo(lastPosition));
                        }

                        // Firing
                        myAgent.addLogEntry("FIRING !");
                        myAgent.shoot(sit.enemy);
                        myAgent.confirmShoot();
                        myAgent.lastAction = "attacking";
                        act = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean done() {
        if(act)
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
    private Vector3f findHighestNeighborClosestTo(Vector3f destination) {
        ArrayList<Vector3f> points = myAgent.sphereCast(myAgent.getSpatial(),
                MosimaAgent.NEIGHBORHOOD_DISTANCE,
                MosimaAgent.CLOSE_PRECISION,
                MosimaAgent.VISION_ANGLE);

        // Sorting
        Collections.sort(points, new Comparator<Vector3f>() {
            @Override
            public int compare(Vector3f pointA, Vector3f pointB)
            {
                float res = pointA.getY() - pointB.getY();
                if(res == 0.0f)
                    return 0;
                else if(res > 0.0f)
                    return 1;
                else
                    return -1;
            }
        });

        Vector3f position = myAgent.getCurrentPosition();
        float mindistance = destination.distance(position);
        Vector3f bestPoint = points.get(0);
        for(Vector3f point : points){
            float distance = destination.distance(point);
            if(distance < mindistance){
                // On se rapproche de la destination
                return point;
            }
        }
        return bestPoint;
    }

    private boolean askForFirePermission() {
        ArrayList<Object>   terms       = new ArrayList<>();
        terms.add(sit.enemyInSight);
        terms.add(sit.impactProba);
        terms.add("'" + this.getClass().getCanonicalName() + "'" );
        return Query.hasSolution(prologQuery("toOpenFire", terms));
    }

    private String prologQuery(String behaviour, ArrayList<Object> terms) {
        StringBuilder query = new StringBuilder(behaviour +"(");
        for (Object t : terms) {
            query.append(t);
            query.append(",");
        }
        return query.substring(0, query.length() - 1) + ")";
    }
}














