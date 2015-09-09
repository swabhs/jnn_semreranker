package edu.cmu.cs.lti.semreranking;

import java.io.File;

public class DataPaths {

    public static final String SEMHOME = SemRerankerMain.semHome;
    public static final String METRIC = SemRerankerMain.metric;
    public static final String RERANKER_DATADIR = SEMHOME + "/training/data/emnlp2015/";

    public static final String WV_FILENAME = RERANKER_DATADIR + SemRerankerMain.wvfile;

    public static final String RESULTS_FILE_EXTN = "thBest.argid.predict.xml";
    public static final String FE_FILE_EXTN = "thBest.argid.predict.frame.elements";
    public static final String SYNSCORE_FILE_EXTN = "thBest.synscore";

    private String mini = ".mini";

    public String semaforResultsDir;
    public String semaforOutFEDir;
    public String synScoresDir;
    public String conllDir;

    public String tokFile;
    public String conllFile;
    public int numRanks;

    public DataPaths(boolean useMini, String dataSet) {
        if (useMini == false) {
            mini = "";
        }
        dataSet = "." + dataSet;

        semaforResultsDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".semaforResults/";
        semaforOutFEDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".frameElements/";
        synScoresDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".synscores/";
        conllDir = RERANKER_DATADIR + METRIC + dataSet + mini + ".conlls/";

        tokFile = RERANKER_DATADIR + "semreranker" + dataSet + mini + ".sentences.tokenized";
        conllFile = RERANKER_DATADIR + "semreranker" + dataSet + mini
                + ".stanfdep.turbobasic.1best.conll";

        numRanks = new File(semaforResultsDir).listFiles().length;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(semaforOutFEDir);
        builder.append("\n");
        builder.append(semaforResultsDir);
        builder.append("\n");
        builder.append(synScoresDir);
        builder.append("\n");
        return builder.toString();
    }

}
