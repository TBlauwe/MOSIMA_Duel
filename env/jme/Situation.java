package env.jme;

import java.util.*;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.HuntBehavior;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.agents.FinalAgent;


/**
 * Class representing a situation at a given moment.
 *
 * @author WonbinLIM
 */
public class Situation {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    public static final String SHOOT = "shoot";
    public static final String FOLLOW = "follow";
    public static final String EXPLORE_OFF = "explore_off";
    public static final String EXPLORE_DEF = "explore_def";
    public static final String HUNT = "hunt";
    public static final String RETREAT = "retreat";

    //| ===========================
    //| ========== ENUMS ==========
    //| ===========================
    public enum ARFF_TYPE {
        REAL("REAL"),
        INTEGER("INTEGER"),
        NOMINAL("NOMINAL"),
        STRING("STRING");

        private String value;
        ARFF_TYPE(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    // Database
    public int offSize;
    public int defSize;
    public float offValue;
    public float defValue;
    public float offScatteringValue;
    public float defScatteringValue;

    // Statistics
    public float averageAltitude;
    public float minAltitude;
    public float maxAltitude;
    public float currentAltitude;

    // Situation
    public float fovValue;
    public String lastAction;
    public int life;
    public int timeSinceLastShot;
    public boolean enemyInSight;
    public float impactProba;
    public String enemy;
    public boolean victory;

    //| ======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| ======================================
    public static Situation getCurrentSituation(FinalAgent a) {
        Situation sit = new Situation();

        sit.offSize = a.offPoints.size();
        sit.defSize = a.defPoints.size();

        sit.offValue = getInterestPointSetValue(a.offPoints);
        sit.defValue = getInterestPointSetValue(a.defPoints);

        sit.offScatteringValue = 0f;
        sit.defScatteringValue = 0f;

        ArrayList<Vector3f> goldenPoints = a.sphereCast(a.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);

        setLocationInfo(a, sit, goldenPoints);

        sit.lastAction = a.lastAction;

        sit.life = a.life;
        sit.timeSinceLastShot = (int) Math.max(0, Math.min(Integer.MAX_VALUE, (System.currentTimeMillis() - a.lastHit)));

        setEnemyInfo(a, sit);

        sit.victory = false;

        return sit;
    }

    public static Situation getSituationFromPos(FinalAgent a, Vector3f pos) {
        Situation sit = new Situation();

        Vector3f oldPos = a.getCurrentPosition();

        // TELEPORT THE PLAYER TO THE POSITION
        a.teleport(pos);

        // Compute situation based on this new position
        ArrayList<Vector3f> goldenPoints = a.sphereCast(a.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);
        setLocationInfo(a, sit, goldenPoints);
        sit.lastAction = a.lastAction;
        sit.life = a.life;
        sit.timeSinceLastShot = (int) Math.max(0, Math.min(Integer.MAX_VALUE, (System.currentTimeMillis() - a.lastHit)));
        setEnemyInfo(a, sit);
        sit.victory = false;

        // TELEPORT THE PLAYER BACK
        a.teleport(oldPos);

        return sit;
    }

    public static Map<String, Double> getCSVValuesFrom(Situation sit) {
        LinkedHashMap<String, Double> columns = new LinkedHashMap<>();

        columns.put("AvgAltitude", (double) sit.averageAltitude);
        columns.put("MinAltitude", (double) sit.minAltitude);
        columns.put("MaxAltitude", (double) sit.maxAltitude);
        columns.put("CurrentAltitude", (double) sit.currentAltitude);
        columns.put("FovValue", (double) sit.currentAltitude);
        columns.put("Life", (double) sit.life);
        columns.put("ImpactProba", (double) sit.impactProba);

        return columns;
    }

    public static Map<String, ARFF_TYPE> getColumns() {
        LinkedHashMap<String, ARFF_TYPE> columns = new LinkedHashMap<>();

        columns.put("AvgAltitude", ARFF_TYPE.REAL);
        columns.put("MinAltitude", ARFF_TYPE.REAL);
        columns.put("MaxAltitude", ARFF_TYPE.REAL);
        columns.put("CurrentAltitude", ARFF_TYPE.REAL);
        columns.put("FovValue", ARFF_TYPE.REAL);
        columns.put("Life", ARFF_TYPE.REAL);
        columns.put("ImpactProba", ARFF_TYPE.REAL);

        return columns;
    }

    public String toCSVFile() {
        String res =
                averageAltitude + "," +
                        minAltitude + "," +
                        maxAltitude + "," +
                        currentAltitude + "," +
                        fovValue + "," +
                        life + "," +
                        impactProba;
        return res;
    }

    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private static void setLocationInfo(FinalAgent a, Situation sit, ArrayList<Vector3f> goldenPoints) {
        float min = 255f, max = -255f, averageAltitude = 0f, fovValue = 0f;

        for (Vector3f point : goldenPoints) {
            if (point.getY() > max) {
                max = point.getY();
            }
            if (point.getY() < min) {
                min = point.getY();
            }

            averageAltitude += point.getY();

            fovValue += AbstractAgent.VISION_DISTANCE - a.getSpatial().getWorldTranslation().distance(point);
        }

        fovValue += AbstractAgent.VISION_DISTANCE * (AbstractAgent.FAR_PRECISION - goldenPoints.size());

        sit.maxAltitude = max;
        sit.minAltitude = min;
        sit.averageAltitude = averageAltitude / goldenPoints.size();
        sit.currentAltitude = a.getSpatial().getWorldTranslation().getY();

        sit.fovValue = fovValue;
    }

    private static void setEnemyInfo(FinalAgent a, Situation sit) {
        Tuple2<Vector3f, String> t = HuntBehavior.checkEnemyInSight(a, false);

        sit.enemyInSight = false;
        sit.impactProba = 0f;

        if (t != null) {
            sit.enemy = t.getSecond();
            sit.enemyInSight = true;
            sit.impactProba = a.impactProba(a.getCurrentPosition(), t.getFirst());
        }
    }

    private static float getInterestPointSetValue(ArrayList<InterestPoint> set) {
        float val = 0f;

        for (InterestPoint point : set) {
            val += point.value;
        }
        return val;
    }
}
