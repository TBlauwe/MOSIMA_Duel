package MOSIMA_Duel.agents;

import MOSIMA_Duel.behaviours.*;
import com.jme3.math.Vector3f;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.behaviours.FSMBehaviour;
import org.lwjgl.Sys;
import sma.actionsBehaviours.PrologBehavior;
import sma.agents.FinalAgent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class MosimaAgent extends FinalAgent {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    private static final long serialVersionUID = 5215165765928961044L;

    public static final long SLEEP_DURATION = 3000;
    public static final long FIRE_RATE = 1000;


    //| ============================
    //| ========== ENUMS ==========
    //| ============================
    public enum State {
        STARTUP("State_Startup"),
        DECISION("State_Decision"),
        EXPLORATION("State_Exploration"),
        ATTACK("State_Attack"),
        TERMINATE("State_Terminate");

        private String value;
        State(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    private FSMBehaviour fsm;
    private long timeSinceLastShoot;

    // ~ Log
    private boolean		bTrace;
    private String		logFileName;
    private List<String> logEntries;


    //| =======================================
    //| ========== PRIVATE FUNCTIONS ==========
    //| =======================================
    private void defineFSM() {
        // ===== FSM STATES =====
		fsm.registerFirstState(new StartupBehaviour(this)   , State.STARTUP.getValue());
        fsm.registerState(new DecisionBehaviour(this)       , State.DECISION.getValue());
        fsm.registerState(new ExplorationBehaviour(this)    , State.EXPLORATION.getValue());
        fsm.registerState(new AttackBehaviour(this)         , State.ATTACK.getValue());
		fsm.registerLastState(new TerminateBehaviour(this)  , State.TERMINATE.getValue());

        // ===== FSM TRANSITIONS =====
		fsm.registerDefaultTransition(State.STARTUP.getValue()  ,State.DECISION.getValue());

        fsm.registerDefaultTransition(State.DECISION.getValue()  ,State.DECISION.getValue());
		fsm.registerTransition(State.DECISION.getValue(), State.EXPLORATION.getValue()  , DecisionBehaviour.Outcome.EXPLORATION.getValue());
        fsm.registerTransition(State.DECISION.getValue(), State.ATTACK.getValue()       , DecisionBehaviour.Outcome.ATTACK.getValue());

        fsm.registerDefaultTransition(State.EXPLORATION.getValue()  ,State.DECISION.getValue());

        fsm.registerDefaultTransition(State.ATTACK.getValue()  ,State.DECISION.getValue());
    }

    protected void setup() {
        super.setup();

        PrologBehavior.sit = Situation.getCurrentSituation(this);

        // ~~~~~ FSM  ~~~~~
        this.fsm = new FSMBehaviour(this) {
            private static final long serialVersionUID = -6846743268622891898L;

            public int onEnd() {
                myAgent.doDelete();
                return super.onEnd();
            }
        };
        defineFSM();
        addBehaviour(fsm);
    }

    @Override
    protected void deploiment() {
        final Object[] args = getArguments();
        if (args[0] == null || args[1] == null || args[2] == null) {
            System.err.println("Malfunction during parameter's loading of agent" + this.getClass().getName());
            System.exit(-1);
        }

        timeSinceLastShoot = 0;
        bTrace		= (boolean) args[2];
        logEntries	= new ArrayList<>();
        logFileName = "logs/" + getLocalName();
        String s	=   "~~~ DESCRIPTION ~~~" + "\n" +
                        "Name = " + getLocalName() + "\n" +
                        "~~~ EXECUTION ~~~" + "\n";
        writeToFile(s, false);

        deployAgent((NewEnv) args[0], true);

        System.out.println("Agent " + getLocalName() + " deployed !");
    }


    //| ======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| ======================================
    public boolean canShoot(){
        return System.currentTimeMillis() - timeSinceLastShoot > FIRE_RATE;
    }

    public void confirmShoot(){
        timeSinceLastShoot = System.currentTimeMillis();
    }

    public boolean hasArrivedToDestination(){
        return getDestination() != null && getCurrentPosition().distance(getDestination()) < MosimaAgent.NEIGHBORHOOD_DISTANCE / 2f;
    }


    //| =====================================
    //| ========== UTILITY METHODS ==========
    //| =====================================
    public void addLogEntry(String s) {
        String prefix = "... ";
        if (bTrace)
            logEntries.add(prefix + s);
    }

    public void trace(String title) {
        if(!bTrace) { return; }

        String header = "~~~ [" + getLocalName() + "] - " + title + " ~~~";
        StringBuilder sb = new StringBuilder(header + "\n");
        for(String log:logEntries){
            sb.append(log);
            sb.append("\n");
        }
        sb.append(String.join("", Collections.nCopies(header.length(), "~")));
        sb.append("\n\n");

        System.out.println(sb.toString());
        writeToFile(sb.toString(), true);
        logEntries.clear();
    }

    private void writeToFile(String s, boolean append) {
        try {
            if(bTrace) {
                PrintWriter pw = new PrintWriter(new FileWriter(logFileName + ".log", append), true);
                pw.println(s);
                pw.close();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveCSV(String folder, String name, boolean append) {
        String res = Situation.getCurrentSituation(this).toCSVFile();
        //String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Calendar.getInstance().getTime());
        String pathName = System.getProperty("user.dir") + folder;
        String filename = name + ".csv";

        Path path = Paths.get(pathName);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try(FileWriter fw = new FileWriter( pathName + filename, append);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(res);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Experiment saving failed");
        }
    }
}