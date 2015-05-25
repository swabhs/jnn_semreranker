package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;

public class FeReader {

    private Set<String> frameIds;
    private Set<String> frameArgIds;

    public FeReader() {
        this.frameIds = Sets.newHashSet();
        this.frameArgIds = Sets.newHashSet();
    }

    public Set<String> getFrameIds() {
        return frameIds;
    }

    public Set<String> getFrameArgIds() {
        return frameArgIds;
    }

    private void addFrameId(String frameId) {
        frameIds.add(frameId);
    }

    private void addFrameArgId(String frameArgId) {
        frameArgIds.add(frameArgId);
    }

    public Map<Integer, FrameSemanticParse> readSingleFEfile(String feFileName) {
        Map<Integer, FrameSemanticParse> fsps = Maps.newHashMap(); // map to example number

        List<String> feLines = BasicFileReader.readFile(feFileName);

        Map<String, Integer> predStartPosMap = Maps.newHashMap();
        Map<String, Integer> predEndPosMap = Maps.newHashMap();
        Table<String, String, Pair<Integer, Integer>> frameMap = HashBasedTable.create();
        Map<String, Double> frameScores = Maps.newHashMap();

        final int startArgToken = 8;

        int corpusSentNum = 1; // HACK - this corpus ignores the 0th sentence
        int prevSentNum = corpusSentNum;
        for (int f = 0; f < feLines.size(); f++) {
            String[] feToks = feLines.get(f).split("\t");
            corpusSentNum = Integer.parseInt(feToks[7]);
            while (corpusSentNum != prevSentNum) { // new Sentence

                fsps.put(prevSentNum, new FrameSemanticParse(predStartPosMap, predEndPosMap,
                        frameMap, Optional.of(frameScores)));

                predStartPosMap = Maps.newHashMap();
                predEndPosMap = Maps.newHashMap();
                frameMap = HashBasedTable.create();
                frameScores = Maps.newHashMap();
                prevSentNum = corpusSentNum;
            }

            String frameId = feToks[3];
            addFrameId(frameId);

            String[] predToks = feToks[5].split("_");
            int predStart = Integer.parseInt(predToks[0]);
            int predEnd = predToks.length == 2 ? Integer.parseInt(predToks[1]) : predStart;
            predStartPosMap.put(frameId, predStart);
            predEndPosMap.put(frameId, predEnd);

            // save the arguments or roles
            int numArgs = (feToks.length - 8) / 2; // BUG in FE generation script...;
            if (numArgs == 0) { // No args for frame
                String argId = "NULL"; // Fake argid for frame with no args

                String frameArgId = StringUtils.makeFrameArgId(frameId, argId);
                addFrameArgId(frameArgId);

                int start = -1; // HACKS for frames with no arguments
                int end = -1;// HACKS for frames with no arguments
                frameMap.put(frameId, argId + "___" + start, Pair.of(start, end));

            } else {
                for (int arg = 0; arg < numArgs; arg++) {
                    String argId = feToks[startArgToken + arg * 2];

                    String frameArgId = StringUtils.makeFrameArgId(frameId, argId);
                    addFrameArgId(frameArgId);

                    String argPosToks[] = feToks[startArgToken + arg * 2 + 1].split(":");
                    int start = Integer.parseInt(argPosToks[0]);
                    int end = argPosToks.length == 2 ? Integer.parseInt(argPosToks[1]) : start;
                    frameMap.put(frameId, argId + "___" + start, Pair.of(start, end));
                }
            }
        }

        fsps.put(corpusSentNum, new FrameSemanticParse(predStartPosMap, predEndPosMap,
                frameMap, Optional.of(frameScores)));
        return fsps;
    }
}
