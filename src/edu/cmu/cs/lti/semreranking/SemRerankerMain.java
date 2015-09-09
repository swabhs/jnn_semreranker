package edu.cmu.cs.lti.semreranking;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import jnn.training.GlobalParameters;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import edu.cmu.cs.lti.semreranking.datageneration.MiniDataGenerator;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Oracle;
import edu.cmu.cs.lti.semreranking.jnn.FspRerankerApp;
import edu.cmu.cs.lti.semreranking.lossfunctions.LogLoss;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader.AllRerankingData;

/**
 * Sentences with no frames are basically not read as a FrameSemanticParse.
 * 
 * @author sswayamd
 *
 */
public class SemRerankerMain {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome = MiniDataGenerator.semhome;
    // "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    @Parameter(names = "-metric", description = "source of k-best syntax")
    public static String metric = "exactKbest";

    @Parameter(names = "-wordvectors", description = "")
    public static String wvfile = "wiki_structskipngram_50.framenet";

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    public static boolean useMini = true;

    @Parameter(names = "-usewordvecs", arity = 1, description = "use pretrained word vectors")
    public static boolean usePretrained = true;

    @Parameter(names = "-dim", description = "size of param representation")
    public static int paramDim = 50; // same as word vec dimension

    @Parameter(names = "-dimfile", description = "file containing parameter dimensions")
    public static String dimFile = "dimensions.txt";

    @Parameter(names = "-useInitModel", arity = 1, description = "use AdaDelta for learning")
    public static boolean useInitModel = false;

    @Parameter(names = "-initmodel", description = "initial model")
    public static String initmodel = "models/bestinit.model";

    @Parameter(names = "-numIter", description = "number of iterations of SGD/Adadelta/Adagrad")
    public static int numIter = 200;

    @Parameter(names = "-learnrate", description = "size of param representation")
    public static double learningRate = 0.001;

    @Parameter(names = "-l2", description = "l2 regularizer")
    public static double l2 = 0.005;

    @Parameter(names = "-adadelta", arity = 1, description = "use AdaDelta for learning")
    public static boolean useAdadelta = false;

    @Parameter(names = "-adagrad", arity = 1, description = "use AdaGrad for learning")
    public static boolean useAdagrad = false;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public static void main(String[] args) {
        new JCommander(new SemRerankerMain(), args);

        GlobalParameters.learningRateDefault = learningRate;
        if (useAdagrad == false && useAdadelta == false) {
            GlobalParameters.useMomentumDefault = true;
            GlobalParameters.momentumDefault = 0.7; // 0.7 - works great!
        }
        GlobalParameters.useAdadeltaDefault = useAdadelta;
        GlobalParameters.useAdagradDefault = useAdagrad;
        GlobalParameters.l2regularizerLambdaDefault = l2;

        AllRerankingData allData = DataFilesReader.readAllRerankingData(useMini);
        FrameNetVocabs vocabs = allData.vocabs;

        System.err.println("\n\nVocab Stats:");
        System.err.println("# frame - arguments = " + vocabs.frameArguments.size());
        System.err.println("# frames = " + vocabs.frameIds.size());
        System.err.println("# tokens = " + vocabs.tokens.size());
        System.err.println("# pos tags = " + vocabs.posTags.size());
        printOracle(allData);

        System.err.println("\n\nPerforming Deep Learning:");
        FspRerankerApp reranker = new FspRerankerApp(allData, new LogLoss());
        Map<Integer, Map<FrameIdentifier, Scored<FrameSemParse>>> bestParses = reranker
                .doDeepDecoding(allData.devData);

        System.err.print(
                "Reranked final:\t" + Evaluator.getRerankedMicroAvgNew(bestParses).toString());
        printOracle(allData);
    }

    static void printOracle(AllRerankingData allData) {
        TrainData trainData = allData.trainData;
        TestData devData = allData.devData;

        double trainBest = Oracle.getTrainUnsortedOracle1best(trainData).f1;
        double oracleTrainBest = Oracle.getMicroCorpusAvg(trainData, trainData.numRanks).f1;

        double devBest = Oracle.getMicroCorpusAvg(devData, 1).f1;
        double oracleDevBest = Oracle.getMicroCorpusAvg(devData, devData.numRanks).f1;

        // double testBest = Oracle.getMicroCorpusAvg(testData, 1).f1;
        // double oracleTestBest = Oracle.getMicroCorpusAvg(testData, testData.numRanks).f1;

        System.err.println("\n\nOracle Results:");
        System.err.println("TRAIN\t1-best = "
                + formatter.format(trainBest)
                + "\t" + trainData.numRanks + "-best = "
                + formatter.format(oracleTrainBest));

        System.err.println("DEV  \t1-best = "
                + formatter.format(devBest)
                + "\t" + devData.numRanks + "-best = "
                + formatter.format(oracleDevBest));

        // System.err.println("TEST \t1-best = "
        // + formatter.format(testBest)
        // + "\t" + testData.numRanks + "-best = "
        // + formatter.format(oracleTestBest));
    }
}
