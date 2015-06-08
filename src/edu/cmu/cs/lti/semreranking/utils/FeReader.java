package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;

public class FeReader {

    private Set<String> frameIds;
    private Set<String> frameArgIds;
    private Set<String> posTags;

    public FeReader() {
        this.frameIds = Sets.newHashSet();
        this.frameArgIds = Sets.newHashSet();
        this.posTags = Sets.newHashSet();
    }

    public Set<String> getFrameIds() {
        return frameIds;
    }

    public Set<String> getFrameArgIds() {
        return frameArgIds;
    }

    public Set<String> getPosTags() {
        return posTags;
    }

    private void addFrameId(String frameId) {
        frameIds.add(frameId);
    }

    private void addFrameArgId(String frameArgId) {
        frameArgIds.add(frameArgId);
    }

    private void addPosTag(String posTag) {
        posTags.add(posTag);
    }

    public Frame getFrameFromFeLine(String feLine) {
        String[] feToks = feLine.trim().split("\t");

        double frameScore = Double.parseDouble(feToks[1]);

        String frameId = feToks[3];
        addFrameId(frameId);

        String predLexUnit = feToks[4];
        addPosTag(predLexUnit.split("\\.")[1]);

        String predToken = feToks[6];

        String[] predPosToks = feToks[5].split("_");
        int predStart = Integer.parseInt(predPosToks[0]);
        int predEnd = predPosToks.length == 2 ? Integer.parseInt(predPosToks[1]) : predStart;

        // save the arguments or roles
        final int startArgToken = 8;
        int numArgs = (feToks.length - 8) / 2; // BUG in FE generation script...;
        // List<Argument> arguments = Lists.newArrayList();
        Set<Argument> arguments = Sets.newHashSet();

        String argId = "NULL";// Default argid for frame with no args
        int start = -1; // HACKS for frames with no arguments
        int end = -1;// HACKS for frames with no arguments
        if (numArgs == 0) { // No args for frame
            String frameArgId = StringUtils.makeFrameArgId(frameId, argId);
            addFrameArgId(frameArgId);
            arguments.add(new Argument(argId, start, end));
        } else {
            for (int argNum = 0; argNum < numArgs; argNum++) {
                argId = feToks[startArgToken + argNum * 2];

                String frameArgId = StringUtils.makeFrameArgId(frameId, argId);
                addFrameArgId(frameArgId);

                String argPosToks[] = feToks[startArgToken + argNum * 2 + 1].split(":");
                start = Integer.parseInt(argPosToks[0]);
                end = argPosToks.length == 2 ? Integer.parseInt(argPosToks[1]) : start;
                arguments.add(new Argument(argId, start, end));
            }
        }

        return new Frame(frameId, predStart, predEnd, predLexUnit, predToken, arguments, frameScore);
    }

    public Map<Integer, FrameSemanticParse> readFeFile(String feFileName) {
        List<String> feLines = BasicFileReader.readFile(feFileName);

        // map to example number
        Map<Integer, FrameSemanticParse> fsps = new TreeMap<Integer, FrameSemanticParse>();
        List<Frame> frames = Lists.newArrayList();

        int corpusSentNum = Integer.parseInt(feLines.get(0).trim().split("\t")[7]);
        int prevSentNum = corpusSentNum;

        for (int f = 0; f < feLines.size(); f++) {
            String[] feToks = feLines.get(f).split("\t");
            corpusSentNum = Integer.parseInt(feToks[7]);

            if (corpusSentNum != prevSentNum) { // new Sentence

                fsps.put(prevSentNum, new FrameSemanticParse(frames));
                frames = Lists.newArrayList();
                prevSentNum = corpusSentNum;
            }

            frames.add(getFrameFromFeLine(feLines.get(f)));
        }

        fsps.put(corpusSentNum, new FrameSemanticParse(frames));
        return fsps;
    }

}
