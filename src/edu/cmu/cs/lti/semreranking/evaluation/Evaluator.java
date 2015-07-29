package edu.cmu.cs.lti.semreranking.evaluation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class Evaluator {

    public static Result getRerankedMicroAvg(Map<Integer, Scored<FrameSemAnalysis>> bestRanks) {

        double pNumTotal = 0.0;
        double pDenomTotal = 0.0;

        double rNumTotal = 0.0;
        double rDenomTotal = 0.0;

        double avgf1 = 0.0;
        for (int ex : bestRanks.keySet()) {
            Scored<FrameSemAnalysis> fsp = (bestRanks.get(ex));

            pNumTotal += fsp.detailedFspScore.pnum;
            pDenomTotal += fsp.detailedFspScore.pdenom;
            rNumTotal += fsp.detailedFspScore.rnum;
            rDenomTotal += fsp.detailedFspScore.rdenom;

            avgf1 += (bestRanks.get(ex)).fscore;
        }

        double precision = pNumTotal / pDenomTotal;
        double recall = rNumTotal / rDenomTotal;

        double fscore = Result.getFscore(precision, recall);
        return new Result(precision, recall, fscore, avgf1 / bestRanks.size());
    }

    public static void writeRerankedBest(
            Map<Integer, Scored<FrameSemAnalysis>> bestRanks,
            String feFileName,
            String rankFileName) {
        List<String> rankLines = Lists.newArrayList();
        rankLines.add("Sentence ID\tBest Rank");
        // List<String> feLines = Lists.newArrayList();

        for (int ex : bestRanks.keySet()) {
            Scored<FrameSemAnalysis> fsp = bestRanks.get(ex);
            // feLines.add(fsp.entity.toString(ex));
            rankLines.add(ex + "\t" + fsp.origRank);
        }

        // BasicFileWriter.writeStrings(feLines, feFileName);
        BasicFileWriter.writeStrings(rankLines, rankFileName);
    }
}
