package edu.cmu.cs.lti.semreranking;

import edu.cmu.cs.lti.semreranking.jnn.ArrayParams;
import edu.cmu.cs.lti.semreranking.jnn.RerankerApp;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;

public class SemRerankerMain {

    public static final String SEMHOME = "/Users/sswayamd/Documents/workspace/"
            + "jnn/SemanticReranker/data/";
    public static String tokFileName = SEMHOME
            + "training/data/naacl2012/cv.train.sentences.mini.tokenized";
    public static String experimentsDir = SEMHOME + "experiments/basic_tbps/semreranker_train/";
    public static String resultsDir = experimentsDir + "results/partial/";
    public static String xmlFileExtn = "thBest.argid.predict.xml";
    public static String trainFeDir = experimentsDir + "output/frameElements/sorted/";
    public static String feFileExtn = "thBest.SORTED.frame.elements";

    public static void main(String[] args) {

        TrainingData data = FileUtils.readAllTrainingData(
                resultsDir, xmlFileExtn, trainFeDir, feFileExtn, tokFileName);

        System.err.println("Num frame - arguments = " + data.vocabs.frameArguments.size());
        System.err.println("Num frames = " + data.vocabs.frameIds.size());
        System.err.println("Num tokens = " + data.vocabs.tokens.size());

        System.err.println(data.instances.get(0).sortedParses.get(0).entity);

        RerankerApp app = new RerankerApp();
        app.run(data.vocabs, data.instances, new ArrayParams());
        // TODO: how to deal with sentences which contain no frames?

    }
}
