package edu.cmu.cs.lti.semreranking.evaluation;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.utils.FeReader;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;
import edu.cmu.cs.lti.semreranking.utils.FileUtils.SentsAndToks;

public class Oracle {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome = SemRerankerMain.semHome;

    @Parameter(names = "-dataset", description = "dataset - train, dev or test")
    public static String dataSet = "test";

    @Parameter(names = "-usemini", description = "should you use mini")
    public static String useMini = "_mini";

    @Parameter(names = "-xmldir", description = "directory containing SEMAFOR scores")
    public static String xmlDir = semHome +
            "experiments/basic_tbps/results/semreranker_" + dataSet + useMini + "/partial/";

    @Parameter(names = "-fedir", description = "SEMAFOR home")
    public static String feDir = semHome +
            "experiments/basic_tbps/output/semreranker_" + dataSet + useMini + "/frameElements/";

    @Parameter(names = "-synDir", description = "directory containing syntactic scores")
    public static String synDir = semHome +
            "training/data/emnlp2015/semreranker." + dataSet + ".turboscores/";

    @Parameter(names = "-tokfile", description = "SEMAFOR home")
    public static String tokfile = semHome +
            "training/data/emnlp2015/semreranker." + dataSet + ".sentences.tokenized";

    @Parameter(names = "-conlldir", description = "SEMAFOR home")
    public static String conlldir = semHome
            + "experiments/swabha/diversekbestdeps/tbps_basic_1.30438665209_test/";

    public static String oracleBestRanks = "oracle." + dataSet + ".ranks";
    public static String oracleConllBest = "oracle." + dataSet + ".conll";
    public static String oracleFeBest = "oracle." + dataSet + ".frame.elements";

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public static Result getMicroCorpusAvg(Map<Integer, TestInstance> instances, int maxRank) {
        // double avgf1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (TestInstance inst : instances.values()) {
            double maxF = Double.NEGATIVE_INFINITY;
            double pNum = 0.0;
            double pDenom = 0.0;

            double rNum = 0.0;
            double rDenom = 0.0;
            for (int rank = 0; rank < maxRank; rank++) {
                Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(rank);
                if (fsp.fscore > maxF) {
                    maxF = fsp.fscore;
                    pNum = fsp.pNum;
                    pDenom = fsp.pDenom;
                    rNum = fsp.rNum;
                    rDenom = fsp.rDenom;
                }
            }
            // avgf1 += maxF;
            pNumTotal += pNum;
            pDenomTotal += pDenom;
            rNumTotal += rNum;
            rDenomTotal += rDenom;
        }
        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore);
    }

    public static Result getMicroCorpusAvg(List<TrainInstance> instances) {
        // double avgf1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (TrainInstance inst : instances) {
            double maxF = Double.NEGATIVE_INFINITY;
            double pNum = 0.0;
            double pDenom = 0.0;

            double rNum = 0.0;
            double rDenom = 0.0;
            for (int rank = 0; rank < 1; rank++) {
                Scored<FrameSemanticParse> fsp = inst.kbestParses.get(rank);
                if (fsp.fscore > maxF) {
                    maxF = fsp.fscore;
                    pNum = fsp.pNum;
                    pDenom = fsp.pDenom;
                    rNum = fsp.rNum;
                    rDenom = fsp.rDenom;
                }
            }
            // avgf1 += maxF;
            pNumTotal += pNum;
            pDenomTotal += pDenom;
            rNumTotal += rNum;
            rDenomTotal += rDenom;
        }
        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore);
    }

    public static double getMacroCorpusAvg(Map<Integer, TestInstance> instances, int maxRank) {
        double avgf1 = 0.0;
        for (TestInstance inst : instances.values()) {
            double max = Double.NEGATIVE_INFINITY;
            for (int rank = 0; rank < maxRank; rank++) {
                Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(rank);
                if (fsp.fscore > max) {
                    max = fsp.fscore;
                }
            }
            avgf1 += max;
        }
        return avgf1 / instances.size();
    }

    public static void writeOracleBest(
            Map<Integer, TestInstance> instances, int maxRank) {

        TreeMap<Integer, String> orderFes = new TreeMap<Integer, String>();
        TreeMap<Integer, String> orderedRanks = new TreeMap<Integer, String>();
        for (int numEx : instances.keySet()) {
            TestInstance inst = instances.get(numEx);
            double maxF = Double.NEGATIVE_INFINITY;
            int bestRank = 0;
            for (int rank = 0; rank < maxRank; rank++) {
                Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(rank);
                if (fsp.fscore > maxF) {
                    maxF = fsp.fscore;
                    bestRank = rank;
                }
            }

            orderFes.put(numEx, inst.unsortedParses.get(bestRank).entity.toString(numEx));
            orderedRanks.put(numEx, numEx + "\t" + bestRank);
        }

        List<String> lines = new ArrayList<String>(instances.size());
        List<String> ranklines = new ArrayList<String>(instances.size());
        ranklines.add("Sentence ID\tBest Rank");
        for (int numEx : orderedRanks.keySet()) {
            lines.add(orderFes.get(numEx));
            ranklines.add(orderedRanks.get(numEx));
        }

        BasicFileWriter.writeStrings(lines, oracleFeBest);
        BasicFileWriter.writeStrings(ranklines, oracleBestRanks);
    }

    public static void writeBestParses() {
        List<String> lines = BasicFileReader.readFile(oracleBestRanks);
        List<Conll> outConlls = Lists.newArrayList();
        for (int i = 1; i < lines.size(); i++) {
            String ele[] = lines.get(i).trim().split("\t");
            int numEx = Integer.parseInt(ele[0]);
            int rank = Integer.parseInt(ele[1]);
            List<Conll> conlls = BasicFileReader.readConllFile(conlldir + rank + "thBest.conll");
            outConlls.add(conlls.get(numEx));
        }
        BasicFileWriter.writeConll(outConlls, oracleConllBest);
    }

    public static double getTrainUnsortedOracle1best() {
        SentsAndToks testSentsAndToks = FileUtils.readConlls(tokfile);
        Map<Integer, List<Scored<FrameSemanticParse>>> allTestFsps =
                FileUtils.readTest(xmlDir, feDir, synDir, new FeReader());

        Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        for (int ex : allTestFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allTestFsps.get(ex);
            testInstances
                    .put(ex, new TestInstance(testSentsAndToks.allLemmas.get(ex),
                            testSentsAndToks.allPostags.get(ex),
                            unsortedParses));
        }

        return getMicroCorpusAvg(testInstances, 1).f1;
    }

    public static void main(String[] args) {
        new JCommander(new Oracle(), args);
        SentsAndToks testSentsAndToks = FileUtils.readConlls(tokfile);
        Map<Integer, List<Scored<FrameSemanticParse>>> allTestFsps =
                FileUtils.readTest(xmlDir, feDir, synDir, new FeReader());

        System.err.println("Number of TEST sentences read = "
                + testSentsAndToks.allLemmas.size());
        System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());

        Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        int numFrames = 0;
        for (int ex : allTestFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allTestFsps.get(ex);
            numFrames += allTestFsps.get(ex).get(0).entity.numFrames;
            testInstances
                    .put(ex, new TestInstance(testSentsAndToks.allLemmas.get(ex),
                            testSentsAndToks.allPostags.get(ex),
                            unsortedParses));
        }
        System.err.println("Number of total frames = " + numFrames);

        System.err.println("\nMicro Oracle Evaluation\n---------------------");

        int maxRank = 100;
        System.err.println("1\t" + getMicroCorpusAvg(testInstances, 1).f1);
        for (int i = 25; i <= maxRank; i += 25) {
            System.err.println(i + "\t" + getMicroCorpusAvg(testInstances, i).f1);
        }

        System.err.println("\nMacro Oracle Evaluation\n---------------------");

        System.err.println("1\t" + getMacroCorpusAvg(testInstances, 1));
        for (int i = 25; i <= maxRank; i += 25) {
            System.err.println(i + "\t" + getMacroCorpusAvg(testInstances, i));
        }

        writeOracleBest(testInstances, maxRank);
        writeBestParses();
    }

}
