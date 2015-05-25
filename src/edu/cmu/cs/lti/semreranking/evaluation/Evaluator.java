package edu.cmu.cs.lti.semreranking.evaluation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.Scored;
import edu.cmu.cs.lti.semreranking.TestInstance;

public class Evaluator {

    public static double getRerankedMacroAvg(
            Map<Integer, TestInstance> instances,
            Map<Integer, Integer> bestRanks) {
        double avgf1 = 0.0;
        for (int ex : instances.keySet()) {
            TestInstance inst = instances.get(ex);
            avgf1 += inst.unsortedParses.get(bestRanks.get(ex)).fscore;
        }
        return avgf1 / instances.size();
    }

    public static double getRerankedMicroAvg(
            Map<Integer, TestInstance> instances,
            Map<Integer, Integer> bestRanks) {

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int ex : instances.keySet()) {
            TestInstance inst = instances.get(ex);
            Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(bestRanks.get(ex));

            pNumTotal += fsp.pNum;
            pDenomTotal += fsp.pDenom;
            rNumTotal += fsp.rNum;
            rDenomTotal += fsp.rDenom;
        }

        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = 2.0 * precision * recall / (precision + recall);
        return fscore;
    }

    public static void writeRerankedBest(
            Map<Integer, TestInstance> instances,
            Map<Integer, Integer> bestRanks) {

        List<String> lines = Lists.newArrayList();

        for (int ex : instances.keySet()) {
            TestInstance inst = instances.get(ex);
            Scored<FrameSemanticParse> fsp = inst.unsortedParses.get(bestRanks.get(ex));

            lines.add(fsp.toString());
        }

        BasicFileWriter.writeStrings(lines, "reranked.test.frame.elements");
    }

}
