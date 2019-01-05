package MOSIMA_Duel.Weka;

import java.util.List;

public class Arff {

    //| ===========================
    //| ========== ENUMS ==========
    //| ===========================
    public enum TYPE {
        REAL("REAL"),
        INTEGER("INTEGER"),
        NOMINAL("NOMINAL"),
        STRING("STRING");

        private String value;
        TYPE(String value) { this.value = value; }
        public String getValue() { return this.value; }
    }

    public static String getArffRelationHeader(String name) {
        return "@RELATION " + name;
    }

    public static String getArffAttributeHeader(String name, Arff.TYPE type) {
        return "@ATTRIBUTE " + name + " " + type.toString();
    }

    public static String getArffClassHeader(String name, List<String> values) {
        String s = "@ATTRIBUTE " + name + " {";
        boolean first = true;
        for(String value : values){
            if(first){
                s += value;
                first = false;
            }else{
                s +=  "," + value;
            }
        }
        return s + "}";
    }
}
