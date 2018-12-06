package MOSIMA_Duel.behaviours;

import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import org.jpl7.Query;
import sma.InterestPoint;
import java.util.ArrayList;
import java.util.EnumSet;

public class DecisionBehaviour extends AbstractFSMSimpleBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID    = 5739600674796316846L;
    private static final String prologFile          = "'./src/MOSIMA_DUEL/prolog/requetes.pl'";

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

    // String correspond to a prolog request's name
    public enum Behaviour {
        EXPLORATION("explore"),
        ATTACK("attack");

        private String value;
        Behaviour(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private static Outcome     outcome;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public DecisionBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
    }

    @Override
    public void action() {
        outcome = Outcome.DEFAULT;
        try {
            String prolog = "consult(" + prologFile + ")";

            if (!Query.hasSolution(prolog)) {
                System.out.println("Cannot open file " + prologFile);
            } else {
                Situation sit = Situation.getCurrentSituation(myAgent);
                EnumSet<Behaviour>  behaviours  = EnumSet.allOf(Behaviour.class);
                ArrayList<Object>   terms       = new ArrayList<>();

                for (Behaviour b : behaviours) {
                    terms.clear();
                    if (b.equals(Behaviour.EXPLORATION)) {
                        terms.add(sit.timeSinceLastShot);
                        terms.add(sit.offSize);
                        terms.add(InterestPoint.INFLUENCE_ZONE);
                        terms.add(NewEnv.MAX_DISTANCE);
                    } else if (b.equals(Behaviour.ATTACK)) {
                        terms.add(sit.enemyInSight);
                    }
                    terms.add("'" + this.getClass().getCanonicalName() + "'" );
                    Query.hasSolution(prologQuery(b.value, terms));
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public boolean done() {
        myAgent.addLogEntry("I will " + outcome.getValue());
        myAgent.trace(getBehaviourName());
        return true;
    }

    @Override
    public int onEnd() {
        return outcome.getValue();
    }

    public static void executeExplore() { outcome = Outcome.EXPLORATION; }
    public static void executeAttack()  { outcome = Outcome.ATTACK; }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private String prologQuery(String behaviour, ArrayList<Object> terms) {
        StringBuilder query = new StringBuilder(behaviour +"(");
        for (Object t : terms) {
            query.append(t);
            query.append(",");
        }
        return query.substring(0, query.length() - 1) + ")";
    }
}