package edu.cmu.cs.lti.semreranking.evaluation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.Data;
import edu.cmu.cs.lti.semreranking.DataInstance;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class Oracle {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome = SemRerankerMain.semHome;

    @Parameter(names = "-dataset", description = "dataset - train, dev or test")
    public static String dataSet = "test";

    @Parameter(names = "-usemini", description = "should you use mini")
    public static String useMini = "";

    @Parameter(names = "-conlldir", description = "SEMAFOR home")
    public static String kBestConllsDir = semHome
            + "experiments/swabha/diversekbestdeps/tbps_basic_1.30438665209_test/";

    public static String oracleBestRanksFile = "oracle." + dataSet + ".ranks";
    public static String oracleConllBest = "oracle." + dataSet + ".conll";
    public static String oracleFeBestFile = "oracle." + dataSet + ".frame.elements";

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public static Result getMicroCorpusAvg(Data data, int maxRank) {
        double macrof1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int exNum = 0; exNum < data.numInstances; exNum++) {
            for (FrameIdentifier identifier : data.getFramesInEx(exNum)) {
                DataInstance inst = data.getInstance(exNum, identifier);
                double maxF = Double.NEGATIVE_INFINITY;
                double pNum = 0.0;
                double pDenom = 0.0;

                double rNum = 0.0;
                double rDenom = 0.0;

                for (int rank = 0; rank < maxRank; rank++) {
                    if (rank >= inst.numUniqueParses) {
                        break;
                    }
                    Scored<FrameSemParse> fsp = inst.getParseAtRank(rank);
                    if (fsp.fscore > maxF) {
                        maxF = fsp.fscore;
                        pNum = fsp.semevalScore.pnum;
                        pDenom = fsp.semevalScore.pdenom;
                        rNum = fsp.semevalScore.rnum;
                        rDenom = fsp.semevalScore.rdenom;
                    }
                }
                macrof1 += maxF;
                pNumTotal += pNum;
                pDenomTotal += pDenom;
                rNumTotal += rNum;
                rDenomTotal += rDenom;
            }
        }

        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        macrof1 /= data.numInstances;
        return new Result(precision, recall, fscore, macrof1);
    }

    public static Result getWorstPerformance(Data data, int maxRank) {
        double avgf1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int exNum = 0; exNum < data.numInstances; exNum++) {
            for (FrameIdentifier id : data.getFramesInEx(exNum)) {
                DataInstance inst = data.getInstance(exNum, id);
                double minF = Double.POSITIVE_INFINITY;
                double pNum = 0.0;
                double pDenom = 0.0;

                double rNum = 0.0;
                double rDenom = 0.0;
                for (int rank = 0; rank < maxRank; rank++) {
                    if (rank >= inst.numUniqueParses) {
                        break;
                    }
                    Scored<FrameSemParse> fsp = inst.getParseAtRank(rank);
                    if (fsp.fscore < minF) {
                        minF = fsp.fscore;
                        pNum = fsp.semevalScore.pnum;
                        pDenom = fsp.semevalScore.pdenom;
                        rNum = fsp.semevalScore.rnum;
                        rDenom = fsp.semevalScore.rdenom;
                    }
                }
                avgf1 += minF;
                pNumTotal += pNum;
                pDenomTotal += pDenom;
                rNumTotal += rNum;
                rDenomTotal += rDenom;
            }
        }
        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore, avgf1 /= data.numInstances);
    }

    public static Result getRandomPerformance(Data data, int maxRank) {
        double avgf1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int exNum = 0; exNum < data.numInstances; exNum++) {
            for (FrameIdentifier identifier : data.getFramesInEx(exNum)) {
                DataInstance inst = data.getInstance(exNum, identifier);
                double pNum = 0.0;
                double pDenom = 0.0;

                double rNum = 0.0;
                double rDenom = 0.0;
                int rank = (int) (Math.random() * inst.numUniqueParses);
                Scored<FrameSemParse> fsp = inst.getParseAtRank(rank);

                pNum = fsp.semevalScore.pnum;
                pDenom = fsp.semevalScore.pdenom;
                rNum = fsp.semevalScore.rnum;
                rDenom = fsp.semevalScore.rdenom;

                avgf1 += fsp.fscore;
                pNumTotal += pNum;
                pDenomTotal += pDenom;
                rNumTotal += rNum;
                rDenomTotal += rDenom;
            }
        }
        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore, avgf1 /= data.numInstances);
    }

    public static void writeOracleBest(List<TestInstance> instances) {
        TreeMap<Integer, String> orderFes = new TreeMap<Integer, String>();
        TreeMap<Integer, String> orderedRanks = new TreeMap<Integer, String>();

        for (int numEx = 0; numEx < instances.size(); numEx++) {
            TestInstance inst = instances.get(numEx);
            double maxF = Double.NEGATIVE_INFINITY;
            int bestRank = -1;

            for (int rank = 0; rank < inst.numUniqueParses; rank++) {
                Scored<FrameSemParse> fsp = inst.getParseAtRank(rank);
                if (fsp.fscore > maxF) {
                    maxF = fsp.fscore;
                    bestRank = rank;
                }
            }

            // TODO: WRONG! Need to write a complete frame-semantic analysis!!!!
            orderFes.put(numEx, inst.getParseAtRank(bestRank).entity.toString(numEx));
            orderedRanks.put(numEx, numEx + "\t" + bestRank);
        }

        List<String> lines = new ArrayList<String>(instances.size());
        List<String> ranklines = new ArrayList<String>(instances.size());
        ranklines.add("Sentence ID\tBest Rank");
        for (int numEx : orderedRanks.keySet()) {
            lines.add(orderFes.get(numEx));
            ranklines.add(orderedRanks.get(numEx));
        }

        BasicFileWriter.writeStrings(lines, oracleFeBestFile);
        BasicFileWriter.writeStrings(ranklines, oracleBestRanksFile);
    }

    public static void writeBestParses() {
        List<String> lines = BasicFileReader.readFile(oracleBestRanksFile);
        List<Conll> outConlls = Lists.newArrayList();
        for (int i = 1; i < lines.size(); i++) {
            String ele[] = lines.get(i).trim().split("\t");
            int numEx = Integer.parseInt(ele[0]);
            int rank = Integer.parseInt(ele[1]);
            List<Conll> conlls = BasicFileReader.readConllFile(
                    kBestConllsDir + rank + "thBest.conll");
            outConlls.add(conlls.get(numEx));
        }
        BasicFileWriter.writeConll(outConlls, oracleConllBest);
    }

    public static Result getTrainUnsortedOracle1best(TrainData data) {
        double macrof1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int exNum = 0; exNum < data.numInstances; exNum++) {
            for (FrameIdentifier identifier : data.getFramesInEx(exNum)) {
                Scored<FrameSemParse> fsp = data.getInstance(exNum, identifier).getParseAtRank(0);

                macrof1 += fsp.fscore;
                pNumTotal += fsp.semevalScore.pnum;
                pDenomTotal += fsp.semevalScore.pdenom;
                rNumTotal += fsp.semevalScore.rnum;
                rDenomTotal += fsp.semevalScore.rdenom;
            }
        }

        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        macrof1 /= data.numInstances;
        return new Result(precision, recall, fscore, macrof1);
    }

    public static void main(String[] args) {
        // new JCommander(new Oracle(), args);
        // String xmlDir = semHome +
        // "experiments/basic_tbps/results/semreranker_" + dataSet + useMini + "/partial/";
        //
        // String feDir = semHome +
        // "experiments/basic_tbps/output/semreranker_" + dataSet + useMini
        // + "/frameElements/";
        //
        // String synDir = semHome +
        // "training/data/emnlp2015/semreranker." + dataSet + ".turboscores/";
        //
        // String conllFile = semHome +
        // "training/data/emnlp2015/semreranker." + dataSet
        // + ".sentences.turboparsed.basic.stanford.lemmatized.conll";
        // SentsAndToks testSentsAndToks = FileUtils.readConlls(conllFile);
        // Map<Integer, List<Scored<FrameSemAnalysis>>> allTestFsps =
        // FileUtils.readDataSet(xmlDir, feDir, synDir, new FeReader());
        // int numTestRanks = allTestFsps.entrySet().iterator().next().getValue().size();
        //
        // System.err.println("Number of TEST sentences read = "
        // + testSentsAndToks.allLemmas.size());
        // System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());
        //
        // List<NewTestInstance> testInstances = Lists.newArrayList();
        // for (int ex : allTestFsps.keySet()) {
        // List<Scored<FrameSemAnalysis>> unsortedParses = allTestFsps.get(ex);
        // testInstances.add(new NewTestInstance(testSentsAndToks.allLemmas.get(ex),
        // testSentsAndToks.allPostags.get(ex),
        // unsortedParses));
        // }
        // NewTestData testData = new NewTestData(testInstances, numTestRanks);
        // System.err.println("\nMicro Oracle Evaluation\n---------------------");
        //
        // int maxRank = new File(feDir).listFiles().length;
        // System.err.println("1\t" + formatter.format(getMicroCorpusAvg(testData, 1).f1));
        // for (int i = 25; i <= maxRank; i += 25) {
        // System.err.println(i + "\t" + formatter.format(getMicroCorpusAvg(testData, i).f1));
        // }
        //
        // System.err.println("Random performance: "
        // + formatter.format(getRandomPerformance(testData, maxRank).f1));
        // System.err.println("Worst-case performance: "
        // + formatter.format(getWorstPerformance(testData, maxRank).f1));
        //
        // writeOracleBest(testInstances);
        // writeBestParses();
    }

}
