package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.jpl7.Query;
import org.lwjgl.Sys;
import sma.InterestPoint;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

public class DecisionBehaviour extends TickerBehaviour {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long   serialVersionUID    = 5739600674796316846L;
    private static final String prologFile          = "'./src/MOSIMA_DUEL/prolog/requetes.pl'";
    private static final HashMap<Behaviour, Class> behavioursClass = createMap();

    private static HashMap<Behaviour, Class> createMap()
    {
        HashMap<Behaviour,Class> map = new HashMap<>();
        map.put(Behaviour.EXPLORATION, ExplorationBehaviour.class);
        map.put(Behaviour.HUNT, HuntBehaviour.class);
        map.put(Behaviour.ATTACK, AttackBehaviour.class);
        return map;
    }


    //| ============================
    //| ========== ENUMS ==========
    //| ============================

    // String correspond to a prolog request's name
    public enum Behaviour {
        EXPLORATION("explore"),
        HUNT("hunt"),
        ATTACK("attack");

        private String value;
        Behaviour(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }


    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private static  MosimaAgent agent;
    private static  Behaviour   nextBehaviour;

    public static   Situation   sit;


    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    public DecisionBehaviour(Agent a, long period) {
        super(a, period);
        agent = (MosimaAgent) a;
    }

    public static void executeExplore() { nextBehaviour = Behaviour.EXPLORATION; }
    public static void executeHunt()    { nextBehaviour = Behaviour.HUNT; }
    public static void executeAttack()  { nextBehaviour = Behaviour.ATTACK; }


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    @Override
    protected void onTick() {
        try {
            String prolog = "consult(" + prologFile + ")";

            if (!Query.hasSolution(prolog)) {
                System.out.println("Cannot open file " + prologFile);
            } else {
                sit = Situation.getCurrentSituation(agent);
                EnumSet<Behaviour>  behaviours  = EnumSet.allOf(Behaviour.class);
                ArrayList<Object>   terms       = new ArrayList<>();

                for (Behaviour b : behaviours) {
                    terms.clear();
                    if (b.equals(Behaviour.EXPLORATION)) {
                        terms.add(sit.timeSinceLastShot);
                        terms.add(((ExplorationBehaviour.prlNextOffend) ? sit.offSize : sit.defSize));
                        terms.add(InterestPoint.INFLUENCE_ZONE);
                        terms.add(NewEnv.MAX_DISTANCE);
                    } else if (b.equals(Behaviour.HUNT)) {
                        terms.add(sit.life);
                        terms.add(sit.timeSinceLastShot);
                        terms.add(sit.offSize);
                        terms.add(sit.defSize);
                        terms.add(InterestPoint.INFLUENCE_ZONE);
                        terms.add(NewEnv.MAX_DISTANCE);
                        terms.add(sit.enemyInSight);
                    } else if (b.equals(Behaviour.ATTACK)) {
                        //terms.add(sit.life);
                        terms.add(sit.enemyInSight);
                        //terms.add(sit.impactProba);
                    }
                    terms.add("'" + this.getClass().getCanonicalName() + "'");

                    if (Query.hasSolution(prologQuery(b.value, terms))) {
                        setNextBehavior();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }

    private void setNextBehavior() {
        if (agent.currentBehavior != null && behavioursClass.get(nextBehaviour) == agent.currentBehavior.getClass()) {
            return;
        }

        if (agent.currentBehavior != null) {
            agent.removeBehaviour(agent.currentBehavior);
        }

        jade.core.behaviours.Behaviour behaviour = null;
        switch (nextBehaviour){
            case EXPLORATION:
                this.agent.log("Switching to " + nextBehaviour.toString());
                behaviour = new ExplorationBehaviour(agent, MosimaAgent.PERIOD);
                break;
            case HUNT:
                this.agent.log("Switching to " + nextBehaviour.toString());
                behaviour = new HuntBehaviour(agent, MosimaAgent.PERIOD);
                break;
            case ATTACK:
                this.agent.log("Switching to " + nextBehaviour.toString());
                behaviour = new AttackBehaviour(agent, MosimaAgent.PERIOD, sit.enemy);
                break;
            default:
                assert false;
        }
        agent.addBehaviour(behaviour);
        agent.currentBehavior = behaviour;
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