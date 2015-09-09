package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Maps;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.SemevalScore;
import edu.cmu.cs.lti.semreranking.evaluation.Result;

public class ResultsFileUtils {

    public static Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> readAllFscores(
            String tokFileName, String fScoreDir) {
        List<String> sentences = BasicFileReader.readFile(tokFileName);
        Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> globalExIdFspMap = Maps
                .newTreeMap();
        int numRanks = new File(fScoreDir).listFiles().length;
        for (int rank = 0; rank < numRanks; rank++) {
            String fscoreFileName = fScoreDir + rank + DataPaths.RESULTS_FILE_EXTN;
            globalExIdFspMap = readFscoreFile(fscoreFileName, sentences, globalExIdFspMap);
        }
        return globalExIdFspMap;
    }

    private static Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> readFscoreFile(
            String fscoreFileName,
            List<String> tokLines,
            Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> exFrameIdScoresMap) {

        List<String> fscoreLines = BasicFileReader.readFile(fscoreFileName);
        for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
            String[] ele = fscoreLines.get(lineNum).trim().split("\t");
            if (ele[1].equals("Complete Sentence")) {
                continue;
            }
            int exNum = Integer.parseInt(ele[0]);

            FrameIdentifier identifier = getFspIdentifierFromFscoreLine(
                    fscoreLines.get(lineNum), tokLines.get(exNum));
            SemevalScore score = getSemevalScoreFromFscoreLine(fscoreLines.get(lineNum));

            Map<FrameIdentifier, List<SemevalScore>> idScoreMap = null;
            if (exFrameIdScoresMap.containsKey(exNum)) {
                idScoreMap = exFrameIdScoresMap.get(exNum);
            } else {
                idScoreMap = Maps.newHashMap();
            }

            List<SemevalScore> scores = null;
            if (idScoreMap.containsKey(identifier)) {
                scores = idScoreMap.get(identifier);
            } else {
                scores = Lists.newArrayList();
            }

            scores.add(score);
            idScoreMap.put(identifier, scores);
            exFrameIdScoresMap.put(exNum, idScoreMap);
        }
        return exFrameIdScoresMap;
    }

    private static SemevalScore getSemevalScoreFromFscoreLine(String line) {
        String[] ele = line.trim().split("\t");
        String rfrac = ele[2].split("\\(")[1];
        double rNum = Double.parseDouble(rfrac.split("/")[0]);
        double rDenom = Double.parseDouble(rfrac.split("/")[1].split("\\)")[0]);
        String pfrac = ele[3].split("\\(")[1];
        double pNum = Double.parseDouble(pfrac.split("/")[0]);
        double pDenom = Double.parseDouble(pfrac.split("/")[1].split("\\)")[0]);
        return new SemevalScore(pNum, pDenom, rNum, rDenom);
    }

    public static String getFscoreLine(FrameIdentifier id, SemevalScore score, int sentNum) {
        StringBuilder builder = new StringBuilder();
        builder.append(sentNum);
        builder.append("\t");
        builder.append(id.sentIdx.first);
        builder.append("-");
        builder.append(id.sentIdx.second);
        builder.append("=FR=");
        builder.append(id.frameId);
        builder.append("\t");
        builder.append(SemRerankerMain.formatter.format(
                Result.getRecallOrPrec(score.rnum, score.rdenom)));
        builder.append("(");
        builder.append(score.rnum); // format it
        builder.append("/");
        builder.append(score.rdenom); // format it
        builder.append(")\t");
        builder.append(SemRerankerMain.formatter.format(
                Result.getRecallOrPrec(score.pnum, score.pdenom)));
        builder.append("(");
        builder.append(score.pnum); // format it
        builder.append("/");
        builder.append(score.pdenom); // format it
        builder.append(")\t");
        builder.append(SemRerankerMain.formatter.format(
                score.fscore));
        return builder.toString();
    }

    private static FrameIdentifier getFspIdentifierFromFscoreLine(String line, String sentence) {
        String[] ele = line.trim().split("\t");
        String[] frameIdOffsetStrings = ele[1].split("=");
        String[] tokenOffsets = frameIdOffsetStrings[0].split("-");
        int startOffSet = Integer.parseInt(tokenOffsets[0]);
        int endOffSet = Integer.parseInt(tokenOffsets[1]);

        int startPos = CharMatcher.is(' ').countIn(sentence.substring(0, startOffSet));
        int endPos = CharMatcher.is(' ').countIn(sentence.substring(0, endOffSet));

        return new FrameIdentifier(frameIdOffsetStrings[2], startPos, endPos,
                new Pair<Short, Short>((short) startOffSet, (short) endOffSet));
    }

    public static void main(String[] args) {
        String scoreFileName = "data/training/data/emnlp2015/exactKbest.dev.semaforResults/0thBest.argid.predict.xml";
        List<String> fscorelines = BasicFileReader.readFile(scoreFileName);
        String tokFileName = "cv.dev.sentences.tokenized";
        List<String> sentlines = BasicFileReader.readFile(tokFileName);
        FrameIdentifier identifier = getFspIdentifierFromFscoreLine(
                fscorelines.get(29), sentlines.get(4));
        System.err.println(identifier.toString());

        SemevalScore score1 = getSemevalScoreFromFscoreLine(fscorelines.get(29));

        System.err.println(getFscoreLine(identifier, score1, 0));
        // Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> map = Maps.newHashMap();
        // map = readFscoreFile(scoreFileName, sentlines, map);
        // FrameIdentifier id = map.get(0).keySet().iterator().next();
        // SemevalScore score = map.get(0).get(id).get(0);
        // System.err.println(getFscoreLine(id, score, 0));
    }
}
