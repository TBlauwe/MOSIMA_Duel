package MOSIMA_Duel.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import MOSIMA_Duel.agents.MosimaAgent;

public abstract class AbstractFSMSimpleBehaviour extends SimpleBehaviour {

    private static final long serialVersionUID = -933252582709302922L;

    protected MosimaAgent myAgent;

    public AbstractFSMSimpleBehaviour(Agent a) {
        super(a);
        this.myAgent = (MosimaAgent) a;
    }

    public abstract void action();

    public abstract boolean done();

    public abstract int onEnd();
}
