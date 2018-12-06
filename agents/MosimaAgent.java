package MOSIMA_Duel.agents;

import MOSIMA_Duel.behaviours.DecisionBehaviour;
import env.jme.NewEnv;
import env.jme.Situation;
import sma.actionsBehaviours.PrologBehavior;
import sma.agents.FinalAgent;


public class MosimaAgent extends FinalAgent {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long serialVersionUID = 5215165765928961044L;

    //| ============================
    //| ========== ENUMS ==========
    //| ============================

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================

    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| =======================================
    protected void setup() {
        super.setup();
        PrologBehavior.sit = Situation.getCurrentSituation(this);
    }

    @Override
    protected void deploiment() {
        final Object[] args = getArguments();
        if (args[0] != null && args[1] != null) {
            addBehaviour(new DecisionBehaviour(this, PERIOD));
            deployAgent((NewEnv) args[0], true);
            System.out.println("Agent " + getLocalName() + " deployed !");
        } else {
            System.err.println("Malfunction during parameter's loading of agent" + this.getClass().getName());
            System.exit(-1);
        }
    }

    public void log(String text){
        System.out.println("[" + this.getLocalName() + "] : " + text);
    }
}
