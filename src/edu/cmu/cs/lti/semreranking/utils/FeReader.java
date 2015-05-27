package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.Argument;
import edu.cmu.cs.lti.semreranking.Frame;
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

    public Frame getFrameFromFeLine(String feLine) {
        String[] feToks = feLine.trim().split("\t");

        double frameScore = Double.parseDouble(feToks[1]);
        String frameId = feToks[3];
        String predType = feToks[4];
        String predToken = feToks[6];
        addFrameId(frameId);

        String[] predToks = feToks[5].split("_");
        int predStart = Integer.parseInt(predToks[0]);
        int predEnd = predToks.length == 2 ? Integer.parseInt(predToks[1]) : predStart;

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

        return new Frame(frameId, predStart, predEnd, predType, predToken, arguments, frameScore);
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

    public static void main(String[] args) {
        FeReader r = new FeReader();
        String feFileName = "/Users/sswayamd/Documents/"

                + "workspace/jnn/SemanticReranker/data/experiments/basic_tbps/output/" +
                "semreranker_test/frameElements/99thBest.argid.predict.frame.elements";
        Map<Integer, FrameSemanticParse> fsps = r.readFeFile(feFileName);
        int total = 0;
        for (int ex : fsps.keySet()) {
            total += fsps.get(ex).numFrames;
        }

        fsps.get(1).print();
        System.err.println(total);
        List<String> feLines = BasicFileReader.readFile(feFileName);
        System.err.println(feLines.size());
    }
}
