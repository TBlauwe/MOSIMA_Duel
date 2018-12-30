package MOSIMA_Duel.WekaInterface;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.classifiers.Evaluation;

import java.awt.*;
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
    public enum DataSet{
        GAME_OVER("gameOver/results.arff"),
        STATE_CHANGED("stateChanged/state_changed.arff");

        private String value;
        DataSet(String value) { this.value = value; }
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
    public static DataSet dataSet = DataSet.GAME_OVER;

    //| =======================================
    //| ========== PUBLIC FUNCTIONS ===========
    //| =======================================
    public static void evalutate() throws Exception {
        // Initialization
        DataSource source = new DataSource(getFullDataSetPath());
        Instances data = source.getDataSet();
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        // Filtering
        Instances filteredData = filterData(data);

        // Classify
        J48 tree = new J48();         // new instance of tree
        tree.setOptions(weka.core.Utils.splitOptions("-C 0.25 -M 2"));     // set the options
        tree.buildClassifier(filteredData);   // build classifier

        evaluateTree(tree, filteredData);
    }

    public static Instances filterData(Instances data) throws Exception{
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

    public static void evaluateTree(Classifier cls, Instances data) throws Exception{
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(cls, data, 10, new Random(1));
    }

    public static void visualizeTree(String graph){
        TreeVisualizer tv = new TreeVisualizer(null, graph, new PlaceNode2());
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
    private static String getFullDataSetPath(){
        return dirPath + dataSet.value;
    }

    public static void main(String[] args) throws Exception{
        // Initialization
        DataSource source = new DataSource(getFullDataSetPath());
        Instances data = source.getDataSet();
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        // Filtering
        Instances filteredData = filterData(data);

        // Classify
        J48 tree = new J48();         // new instance of tree
        tree.setOptions(weka.core.Utils.splitOptions("-C 0.25 -M 2"));     // set the options
        tree.buildClassifier(filteredData);   // build classifier

        evaluateTree(tree, filteredData);
        visualizeTree(tree.graph());
    }

}
