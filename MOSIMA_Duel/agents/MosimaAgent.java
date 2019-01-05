package MOSIMA_Duel.agents;

import MOSIMA_Duel.Weka.Arff;
import MOSIMA_Duel.behaviours.*;
import MOSIMA_Duel.env.Situation;
import env.jme.NewEnv;
import jade.core.behaviours.FSMBehaviour;
import sma.actionsBehaviours.PrologBehavior;
import sma.agents.FinalAgent;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


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

    // ~ Misc
    private long timeSinceLastShot;
    public boolean useWeka;

    // ~ Log
    private boolean         bTrace;
    private boolean         verbose;
    private String          logFileName;
    private List<String>    logEntries;
    private List<String>    csvEntries;


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
        if (args[0] == null || args[1] == null || args[2] == null || args[3] == null || args[4] == null) {
            System.err.println("Malfunction during parameter's loading of agent" + this.getClass().getName());
            System.exit(-1);
        }

        timeSinceLastShot = 0;

        bTrace          = (boolean) args[2];
        verbose         = (boolean) args[3];
        useWeka         = (boolean) args[4];
        logEntries      = new ArrayList<>();
        csvEntries      = new ArrayList<>();

        logFileName     = "logs/" + getLocalName();

        String s        =
                        "~~~ DESCRIPTION ~~~"       + "\n" +
                        "Name = " + getLocalName()  + "\n" +
                        "~~~ EXECUTION ~~~"         + "\n";

        writeToFile(s, false);

        deployAgent((NewEnv) args[0], true);

        System.out.println("Agent " + getLocalName() + " deployed !");
    }


    //| ======================================
    //| ========== PUBLIC FUNCTIONS ==========
    //| ======================================
    public boolean canShoot(){
        return System.currentTimeMillis() - timeSinceLastShot > FIRE_RATE;
    }

    public void confirmShoot(){
        timeSinceLastShot = System.currentTimeMillis();
    }

    public boolean hasArrivedToDestination(){
        return (getDestination() != null && getCurrentPosition().distance(getDestination()) < MosimaAgent.NEIGHBORHOOD_DISTANCE / 2f) || getDestination() == null;
    }

    public void saveCurrentSituation(){
        addCSVEntry(Situation.getCurrentSituation(this).toCSV());
    }

    public void saveRun(boolean won){
        String gameOver = "," + ((won) ? "VICTORY" : "DEFEAT");

        // ===== I. Save all situation saved when stateChanged
        for(String entry: csvEntries){
            entry += gameOver;
            saveCSV("/ressources/learningBase/stateChanged/",
                    "state_changed",
                    "STATE_CHANGED",
                    entry,
                    true);
        }
        clearCSVEntries();

        // ===== II. Save all situation saved when stateChanged
        saveCSV("/ressources/learningBase/gameover/",
                "results",
                "GAMEOVER",
                Situation.getCurrentSituation(this).toCSV() + gameOver,
                true);

    }


    //| =====================================
    //| ========== UTILITY METHODS ==========
    //| =====================================
    public void addLogEntry(String s) {
        String prefix = "... ";
        if (bTrace)
            logEntries.add(prefix + s);
    }

    public void addCSVEntry(String s) {
            csvEntries.add(s);
    }

    public void clearCSVEntries() {
        csvEntries.clear();
    }

    public void trace(String title) {
        if(!bTrace) { return; }

        String header = "|\n~~~ [" + getLocalName() + "] - " + title + " ~~~";
        StringBuilder sb = new StringBuilder(header + "\n");
        for(String log:logEntries){
            sb.append(log);
            sb.append("\n");
        }
        sb.append(String.join("", Collections.nCopies(header.length(), "~")));
        sb.append("\n|\n");

        if(verbose)
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

    public void saveCSV(String folder, String name, String relation, String data, boolean append) {
        String pathName = System.getProperty("user.dir") + folder;
        String filename = name + ".arff";

        Path path = Paths.get(pathName);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean newFile = true;
        File f = new File(pathName + filename);
        if(f.exists() && !f.isDirectory()) {
            newFile = false;
        }

        try(FileWriter fw = new FileWriter( pathName + filename, append);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            Map<String, Arff.TYPE> columns = Situation.getColumns();
            if(newFile){
               out.println(Arff.getArffRelationHeader(relation));
               for(Map.Entry<String, Arff.TYPE> attribute : columns.entrySet()){
                   out.println(Arff.getArffAttributeHeader(attribute.getKey(), attribute.getValue()));
               }
               out.println(Arff.getArffClassHeader("class", Situation.getClasses()));
               out.println("");
               out.println("@DATA");
            }
            out.println(data);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Experiment saving failed");
        }
    }
}
