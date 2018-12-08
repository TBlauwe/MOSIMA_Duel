package MOSIMA_Duel.behaviours;

import MOSIMA_Duel.agents.MosimaAgent;
import jade.core.Agent;

public class StartupBehaviour extends AbstractFSMSimpleBehaviour {
    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================


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

    public StartupBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
    }

    @Override
    public void action() {
        myAgent.addLogEntry("sleeping for " + MosimaAgent.SLEEP_DURATION + " milliseconds");
        myAgent.trace(getBehaviourName());
        try {
            Thread.sleep(MosimaAgent.SLEEP_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myAgent.addLogEntry("waking up");
        myAgent.lastAction = "idle";
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
}
