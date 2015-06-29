package edu.cmu.cs.lti.semreranking;

public class DataPaths {

    public static final String SEMHOME = SemRerankerMain.semHome;

    public static final String WV_FILENAME = SEMHOME
            + "/training/data/emnlp2015/" + SemRerankerMain.wvfile;

    public static final String XML_FILE_EXTN = "thBest.argid.predict.xml";
    public static final String FE_FILE_EXTN = "thBest.argid.predict.frame.elements";
    public static final String TURBO_FILE_EXTN = "thBest.synscore";

    private String mini = "_mini";

    private String experimentsDir;
    public String xmlDir;
    public String feDir;
    public String synDir;

    public String conllFile;

    public DataPaths(boolean useMini, String dataSet) {
        if (useMini == false) {
            mini = "";
        }
        experimentsDir = SEMHOME + "experiments/" + SemRerankerMain.model + "/";

        xmlDir = experimentsDir + "results/semreranker_" + dataSet + mini + "/partial/";
        feDir = experimentsDir + "output/semreranker_" + dataSet + mini + "/frameElements/";
        synDir = SEMHOME + "/training/data/emnlp2015/semreranker." + dataSet + ".synscores/";

        conllFile = SEMHOME
                + "/training/data/emnlp2015/semreranker." + dataSet
                + ".sentences.turboparsed.basic.stanford.lemmatized.conll";
    }

}
