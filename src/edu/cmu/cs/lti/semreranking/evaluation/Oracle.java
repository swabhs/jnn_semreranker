package edu.cmu.cs.lti.semreranking.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.beust.jcommander.internal.Lists;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.Scored;
import edu.cmu.cs.lti.semreranking.TestInstance;

public class Oracle {

    public static double getMicroCorpusAvg(Map<Integer, TestInstance> instances, int maxRank) {
        // double avgf1 = 0.0;

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (TestInstance inst : instances.values()) {
            double maxF = 0.0;
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

        double fscore = 2.0 * precision * recall / (precision + recall);
        return fscore;
    }

    public static double getMacroCorpusAvg(Map<Integer, TestInstance> instances, int maxRank) {
        double avgf1 = 0.0;
        for (TestInstance inst : instances.values()) {
            double max = 0.0;
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
            double maxF = 0.0;
            int bestRank = 0;
            for (int rank = 0; rank < maxRank; rank++) {
                Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(rank);
                if (fsp.fscore > maxF) {
                    maxF = fsp.fscore;
                    bestRank = rank;
                }
            }

            orderFes.put(numEx,
                    inst.unsortedParses.get(bestRank).entity.toString(inst.tokens, numEx));
            orderedRanks.put(numEx, numEx + "\t" + bestRank);
        }

        List<String> lines = new ArrayList<String>(instances.size());
        List<String> ranklines = new ArrayList<String>(instances.size());
        ranklines.add("Sentence ID\tBest Rank");
        for (int numEx : orderedRanks.keySet()) {
            lines.add(orderFes.get(numEx));
            ranklines.add(orderedRanks.get(numEx));
        }

        BasicFileWriter.writeStrings(lines, "oracle.test.frame.elements");
        BasicFileWriter.writeStrings(ranklines, "oracle.test.ranks");
    }

    public static void writeBestParses(String conllDir, String bestRanksFile) {
        List<String> lines = BasicFileReader.readFile(bestRanksFile);
        List<Conll> outConlls = Lists.newArrayList();
        for (int i = 1; i < lines.size(); i++) {
            String ele[] = lines.get(i).trim().split("\t");
            int numEx = Integer.parseInt(ele[0]);
            int rank = Integer.parseInt(ele[1]);
            List<Conll> conlls = BasicFileReader.readConllFile(conllDir + rank + "thBest.conll");
            outConlls.add(conlls.get(numEx));
        }
        BasicFileWriter.writeConll(outConlls, "oracle.test.conll");
    }

    public static void main(String[] args) {
        // DataPaths testDataPaths = new DataPaths(false, SemRerankerMain.model);
        // SentsAndToks testSentsAndToks = FileUtils.readTokFile(DataPaths.TOKEN_FILE_TEST);
        // Map<Integer, List<Scored<FrameSemanticParse>>> allTestFsps =
        // FileUtils.readTest(testDataPaths.xmlDir, testDataPaths.feDir, new FeReader());
        //
        // System.err.println("Number of TEST sentences read = " + testSentsAndToks.allToks.size());
        // System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());
        //
        // Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        // for (int ex : allTestFsps.keySet()) {
        // List<Scored<FrameSemanticParse>> unsortedParses = allTestFsps.get(ex);
        // testInstances
        // .put(ex, new TestInstance(testSentsAndToks.allToks.get(ex), unsortedParses));
        // }
        //
        // System.err.println("Micro = " + getMicroCorpusAvg(testInstances, 1));
        // int maxRank = 100;
        // System.err.println("Micro = " + getMicroCorpusAvg(testInstances, maxRank));
        // System.err.println("Macro = " + getMacroCorpusAvg(testInstances, maxRank));
        // writeOracleBest(testInstances, maxRank);
        writeBestParses(args[0], args[1]);
    }
}
