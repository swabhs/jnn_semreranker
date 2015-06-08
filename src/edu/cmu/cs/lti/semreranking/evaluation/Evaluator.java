package edu.cmu.cs.lti.semreranking.evaluation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

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

    public static Result getRerankedMicroAvg(
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

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore);
    }

    public static Result getTrainRerankedMicroAvg(
            List<TrainInstance> instances,
            Map<Integer, Integer> bestRanks) {

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        for (int ex = 0; ex < instances.size(); ex++) {
            Scored<FrameSemanticParse> fsp = instances.get(ex).kbestParses.get(bestRanks.get(ex));

            pNumTotal += fsp.pNum;
            pDenomTotal += fsp.pDenom;
            rNumTotal += fsp.rNum;
            rDenomTotal += fsp.rDenom;

        }

        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore);
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
