package edu.cmu.cs.lti.semreranking;

import java.util.Map;

import jnn.training.GlobalParameters;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Oracle;
import edu.cmu.cs.lti.semreranking.jnn.RerankerApp;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;

/**
 * Sentences with no frames are basically not read as a FrameSemanticParse.
 * 
 * @author sswayamd
 *
 */
public class SemRerankerMain {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome =
            "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";
    // public static String semHome = "/usr0/home/sswayamd/semafor/semafor/";

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    private static boolean useMini = true;

    @Parameter(names = "-model", description = "semafor model used to obtain reranking data")
    public static String model = "basic_tbps";

    public static void main(String[] args) {
        new JCommander(new SemRerankerMain(), args);

        GlobalParameters.learningRateDefault = 0.005;
        GlobalParameters.useMomentumDefault = true;
        GlobalParameters.useAdagradDefault = true;

        RerankingData allData = FileUtils.readAllTrainingData(useMini);
        TrainData trainData = allData.trainData;
        TestData testData = allData.testData;
        TestData devData = allData.devData;

        System.err.println("######### Test instances : " + testData.testInstances.size());

        System.err.println("Num frame - arguments = " + trainData.vocabs.frameArguments.size());
        System.err.println("Num frames = " + trainData.vocabs.frameIds.size());
        System.err.println("Num tokens = " + trainData.vocabs.tokens.size());

        RerankerApp reranker = new RerankerApp(trainData, testData, devData);
        Map<Integer, Integer> bestRanks = reranker.getBestRanks(testData);
        System.err.println("Oracle 1-best score = "
                + Oracle.getMicroCorpusAvg(testData.testInstances, 1));
        System.err.println("Oracle 100-best score = "
                + Oracle.getMicroCorpusAvg(testData.testInstances, 100));

        System.err.println("Reranked score = " + Evaluator.getRerankedMacroAvg(
                testData.testInstances, bestRanks));
    }
}
