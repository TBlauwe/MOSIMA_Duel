package MOSIMA_Duel.behaviours;

import jade.core.Agent;

public class TerminateBehaviour extends AbstractFSMSimpleBehaviour {
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

    public TerminateBehaviour(Agent a) {
        super(a);
        outcome = Outcome.DEFAULT;
    }

    @Override
    public void action() {
        myAgent.doWait(myAgent.SLEEP_DURATION);
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
