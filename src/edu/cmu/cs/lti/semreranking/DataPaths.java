package edu.cmu.cs.lti.semreranking;

public class DataPaths {

    public static final String SEMHOME = SemRerankerMain.semHome;
    public static final String TOKEN_FILE_TRAIN = SEMHOME
            + "/training/data/emnlp2015/semreranker.train.sentences.tokenized";
    public static final String TOKEN_FILE_DEV = SEMHOME
            + "/training/data/emnlp2015/semreranker.dev.sentences.tokenized";
    public static final String TOKEN_FILE_TEST = SEMHOME
            + "/training/data/emnlp2015/semreranker.test.sentences.tokenized";

    public static final String XML_FILE_EXTN = "thBest.argid.predict.xml";
    public static final String FE_FILE_EXTN = "thBest.argid.predict.frame.elements";

    private String dataSet;
    private String mini;

    private String experimentsDir;
    public String xmlDir;
    public String feDir;

    public DataPaths(boolean useMini, boolean useSorted, String model) {
        if (useSorted) {
            dataSet = "semreranker_train_sorted";
        } else {
            dataSet = "semreranker_train";
        }

        if (useMini) {
            mini = "_mini";
        } else {
            mini = "";
        }
        experimentsDir = SEMHOME + "experiments/" + model + "/";

        xmlDir = experimentsDir + "results/" + dataSet + mini + "/partial/";
        feDir = experimentsDir + "output/" + dataSet + mini + "/frameElements/";
    }

    /** for test data */
    public DataPaths(boolean useMini, String model) {
        dataSet = "semreranker_test";
        if (useMini) {
            mini = "_mini";
        } else {
            mini = "";
        }

        experimentsDir = SEMHOME + "experiments/" + model + "/";

        xmlDir = experimentsDir + "results/" + dataSet + mini + "/partial/";
        feDir = experimentsDir + "output/" + dataSet + mini + "/frameElements/";
    }

    public DataPaths(boolean useMini, String model, String dataSet) {
        dataSet = "semreranker_dev";
        if (useMini) {
            mini = "_mini";
        } else {
            mini = "";
        }

        experimentsDir = SEMHOME + "experiments/" + model + "/";

        xmlDir = experimentsDir + "results/" + dataSet + mini + "/partial/";
        feDir = experimentsDir + "output/" + dataSet + mini + "/frameElements/";
    }
}
