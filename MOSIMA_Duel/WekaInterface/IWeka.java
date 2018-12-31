package MOSIMA_Duel.WekaInterface;

import env.jme.Situation;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.Evaluation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

import javax.swing.*;

public class IWeka {

    //| ===============================
    //| ========== CONSTANTS ==========
    //| ===============================
    public static String dirPath = System.getProperty("user.dir").replace("\\", "/") +
            "/ressources/learningBase/";

    //| ============================
    //| ========== ENUMS ==========
    //| ============================
    public enum EDataSet {
        GAME_OVER("gameOver/results.arff"),
        STATE_CHANGED("stateChanged/state_changed.arff");

        private String value;
        EDataSet(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }

    public enum EAttribute{
        AvgAltitude(true),
        MinAltitude(true),
        MaxAltitude(true),
        CurrentAltitude(true),
        FovValue(true),
        Life(false),
        ImpactProba(false);

        private boolean value;
        EAttribute(boolean value) { this.value = value; }
        public boolean getValue() { return this.value; }
    }

    //| =============================
    //| ========== MEMBERS ==========
    //| =============================
    public static EDataSet source = EDataSet.GAME_OVER;
    public static Instances dataSet;
    public static Classifier classifier;
    public static Evaluation eval;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ===========
    //| =======================================
    public static void initialize() throws Exception {
        // Initialization
        DataSource source = new DataSource(getFullDataSetPath());
        Instances data = source.getDataSet();
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        // Filtering
        dataSet = filterData(data);

        // Classify
        classifier = new J48();         // new instance of tree
        classifier.setOptions(weka.core.Utils.splitOptions("-C 0.25 -M 2"));     // set the options
        classifier.buildClassifier(dataSet);   // build classifier

        evaluateTree(classifier, dataSet);
    }

    public static Map.Entry<String, Double> classify(Instance instance) {

        try {
            // Make the prediction here.
            double predictionIndex = classifier.classifyInstance(instance);

            // Get the predicted class label from the predictionIndex.
            String predictedClassLabel = instance.classAttribute().value((int) predictionIndex);
            // 0.0 = Victory
            // 1.0 = Defeat

            // Get the prediction probability distribution.
            double[] predictionDistribution = classifier.distributionForInstance(instance);
            double predictionProbability = predictionDistribution[(int) predictionIndex];

            return new HashMap.SimpleEntry<>(predictedClassLabel, predictionProbability);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static Instance situationToInstance(Situation sit){
        Instance instance = new Instance(EAttribute.values().length);
        instance.setDataset(dataSet);
        instance.setValue(0, (double) sit.averageAltitude);
        instance.setValue(1, (double) sit.minAltitude);
        instance.setValue(2, (double) sit.maxAltitude);
        instance.setValue(3, (double) sit.currentAltitude);
        instance.setValue(4, (double) sit.currentAltitude);
        instance.setValue(5, (double) sit.life);
        instance.setValue(6, (double) sit.impactProba);
        instance.setClassValue("DEFEAT"); //Doesn't matter since it won't be used

        return instance;
    }

    public static void visualizeTree(){
        TreeVisualizer tv = null;
        try {
            tv = new TreeVisualizer(null, ((J48) classifier).graph(), new PlaceNode2());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame jf = new JFrame("IWeka Classifier Tree Visualizer: J48");
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.setSize(800, 600);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.setVisible(true);
        tv.fitToScreen();
    }

    //| =======================================
    //| ========== PRIVATE FUNCTIONS ===========
    //| =======================================
    private static void evaluateTree(Classifier cls, Instances data) throws Exception{
        eval = new Evaluation(data);
        eval.crossValidateModel(cls, data, 10, new Random(1));
    }

    private static Instances filterData(Instances data) throws Exception{
        Remove remove = new Remove(); // new instance of filter
        String options = "";
        int counter = 1;
        for(EAttribute attribute : EAttribute.values()){
            if(!attribute.value) {
                if (options.length() == 0){
                    options += "-R ";
                    options += Integer.toString(counter);
                }
                else
                    options += "," + Integer.toString(counter);
            }
            counter++;
        }
        remove.setOptions(weka.core.Utils.splitOptions(options));   // set options
        remove.setInputFormat(data);                                // inform filter about dataset **AFTER** setting options
        return Filter.useFilter(data, remove);                      // apply filter
    }

    private static String getFullDataSetPath(){
        return dirPath + source.value;
    }
}
