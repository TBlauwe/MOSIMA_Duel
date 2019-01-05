package MOSIMA_Duel.env;

import MOSIMA_Duel.Weka.Arff;
import MOSIMA_Duel.agents.MosimaAgent;
import com.jme3.math.Vector3f;
import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import sma.AbstractAgent;

import java.util.*;

public class Situation {
    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    public static final float  MIN_HEIGHT = -255f;
    public static final float  MAX_HEIGHT = +255f;

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    public Float    avgAltitude;
    public Float    minAltitude;
    public Float    maxAltitude;
    public Float    currentAltitude;
    public Float    fovValue;
    public Float    impactProba;
    public String   enemy;
    public Boolean  victory;
    public Boolean  enemyInSight;
    public Integer  life;

    private MosimaAgent agent;

    //| ======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| ======================================
    public Situation(MosimaAgent agent){
        this.agent      = agent;
        minAltitude     = MAX_HEIGHT;
        maxAltitude     = MIN_HEIGHT;
        avgAltitude     = 0f;
        fovValue        = 0f;
        enemyInSight    = false;
        impactProba     = 0f;
        life            = agent.life;
        victory         = false;

        setLocationInfo(getGoldenPoints());
        setEnemyInfo();
    }

    public Situation(MosimaAgent agent, Vector3f pos){
        this.agent      = agent;

        // TELEPORT THE PLAYER TO THE POSITION
        Vector3f oldPos = agent.getCurrentPosition();
        agent.teleport(pos);

        minAltitude     = MAX_HEIGHT;
        maxAltitude     = MIN_HEIGHT;
        avgAltitude     = 0f;
        fovValue        = 0f;
        enemyInSight    = false;
        impactProba     = 0f;
        life            = agent.life;
        victory         = false;

        setLocationInfo(getGoldenPoints());
        setEnemyInfo();

        // TELEPORT THE PLAYER BACK
        agent.teleport(oldPos);
    }

    public static Map<String, Arff.TYPE> getColumns() {
        LinkedHashMap<String, Arff.TYPE> columns = new LinkedHashMap<>();
        columns.put("AvgAltitude", Arff.TYPE.REAL);
        columns.put("MinAltitude", Arff.TYPE.REAL);
        columns.put("MaxAltitude", Arff.TYPE.REAL);
        columns.put("CurrentAltitude", Arff.TYPE.REAL);
        columns.put("Life", Arff.TYPE.REAL);
        columns.put("FovValue", Arff.TYPE.REAL);
        return columns;
    }

    public static Situation getCurrentSituation(MosimaAgent a){
        return new Situation(a);
    }

    public static Situation getSituationFromPos(MosimaAgent a, Vector3f pos){
        return new Situation(a, pos);
    }
    public static List<String> getClasses(){
       return Arrays.asList("VICTORY", "DEFEAT");
    }

    public String toCSV() {
        return
                avgAltitude + "," +
                minAltitude + "," +
                maxAltitude + "," +
                currentAltitude + "," +
                life + "," +
                fovValue;
    }

    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================

    private ArrayList<Vector3f> getGoldenPoints(){
        return agent.sphereCast(
                agent.getSpatial(),
                AbstractAgent.VISION_DISTANCE,
                AbstractAgent.FAR_PRECISION,
                AbstractAgent.VISION_ANGLE);
    }

    private void setLocationInfo(ArrayList<Vector3f> goldenPoints) {
        for (Vector3f point : goldenPoints) {
            if (point.getY() > maxAltitude){
                maxAltitude = point.getY();
            }
            if (point.getY() < minAltitude)
                minAltitude = point.getY();

            avgAltitude += point.getY();

            fovValue += AbstractAgent.VISION_DISTANCE - agent.getSpatial().getWorldTranslation().distance(point);
        }

        fovValue += AbstractAgent.VISION_DISTANCE * (AbstractAgent.FAR_PRECISION - goldenPoints.size());
        avgAltitude = avgAltitude / goldenPoints.size();
        currentAltitude = agent.getSpatial().getWorldTranslation().getY();
    }

    private void setEnemyInfo() {
        Tuple2<Vector3f, String> t = checkEnemyInSight();

        if (t != null) {
            enemy = t.getSecond();
            enemyInSight = true;
            impactProba = agent.impactProba(agent.getCurrentPosition(), t.getFirst());
        }
    }

    private Tuple2<Vector3f, String> checkEnemyInSight() {
        ArrayList<Tuple2<Vector3f, String>> enemies =
                agent.getVisibleAgents(AbstractAgent.VISION_DISTANCE, AbstractAgent.VISION_ANGLE);

        Tuple2<Vector3f, String> best = null;
        float value = -1f;

        for (Tuple2<Vector3f, String> enemy : enemies) {
            float tmp = evaluateEnemy(enemy);
            if (tmp > value) {
                best = enemy;
                value = tmp;
            }
        }
        return best;
    }

    private float evaluateEnemy(Tuple2<Vector3f, String> enemy) {
        return NewEnv.MAX_DISTANCE - agent.getSpatial().getWorldTranslation().distance(enemy.getFirst());
    }
}
