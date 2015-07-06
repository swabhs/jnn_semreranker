package edu.cmu.cs.lti.semreranking;

public class DataPaths {

    public static final String SEMHOME = SemRerankerMain.semHome;
    public static final String METRIC = SemRerankerMain.metric;
    public static final String RERANKER_DATADIR = SEMHOME + "/training/data/emnlp2015/";

    public static final String WV_FILENAME = SEMHOME
            + "/training/data/emnlp2015/" + SemRerankerMain.wvfile;

    public static final String RESULTS_FILE_EXTN = "thBest.argid.predict.xml";
    public static final String FE_FILE_EXTN = "thBest.argid.predict.frame.elements";
    public static final String SYNSCORE_FILE_EXTN = "thBest.synscore";

    private String mini = ".mini";

    public String semaforResultsDir;
    public String semaforOutFEDir;
    public String synScoresDir;
    public String conllDir;

    public String conllFile;

    public DataPaths(boolean useMini, String dataSet) {
        if (useMini == false) {
            mini = "";
        }
        dataSet = "." + dataSet;

        semaforResultsDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".semaforResults/";
        semaforOutFEDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".frameElements/";
        synScoresDir = RERANKER_DATADIR + METRIC + dataSet + ".synscores/";
        conllDir = RERANKER_DATADIR + METRIC + dataSet + ".conlls/";

        conllFile = RERANKER_DATADIR + "semreranker" + dataSet
                + ".stanfdep.turbobasic.1best.conll";
    }

}
