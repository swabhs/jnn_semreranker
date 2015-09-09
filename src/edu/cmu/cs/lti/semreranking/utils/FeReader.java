package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;

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

    public FrameSemParse getFrameFromFeLine(String feLine) {
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
            String frameArgId = FormatUtils.makeFrameArgId(frameId, argId);
            addFrameArgId(frameArgId);
            arguments.add(new Argument(argId, start, end));
        } else {
            for (int argNum = 0; argNum < numArgs; argNum++) {
                argId = feToks[startArgToken + argNum * 2];

                String frameArgId = FormatUtils.makeFrameArgId(frameId, argId);
                addFrameArgId(frameArgId);

                String argPosToks[] = feToks[startArgToken + argNum * 2 + 1].split(":");
                start = Integer.parseInt(argPosToks[0]);
                end = argPosToks.length == 2 ? Integer.parseInt(argPosToks[1]) : start;
                arguments.add(new Argument(argId, start, end));
            }
        }

        return new FrameSemParse(frameId, predStart, predEnd, predLexUnit, predToken, arguments,
                frameScore);
    }

    private Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> readFeFile(
            String feFileName,
            Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> allCorpusFsps) {
        List<String> feLines = BasicFileReader.readFile(feFileName);

        for (int lineNum = 0; lineNum < feLines.size(); lineNum++) {
            String[] feToks = feLines.get(lineNum).split("\t");

            int corpusSentNum = Integer.parseInt(feToks[7]);
            FrameSemParse parse = getFrameFromFeLine(feLines.get(lineNum));
            FrameIdentifier identifier = parse.getIdentifier();

            Map<FrameIdentifier, List<FrameSemParse>> fspsForSent = null;
            if (allCorpusFsps.containsKey(corpusSentNum)) {
                fspsForSent = allCorpusFsps.get(corpusSentNum);
            } else {
                fspsForSent = Maps.newHashMap();// should happen only at rank 0
            }

            List<FrameSemParse> kbestParses = null;
            if (fspsForSent.containsKey(identifier)) {
                kbestParses = fspsForSent.get(identifier);
            } else {
                kbestParses = Lists.newArrayList(); // should happen only at rank 0
            }
            kbestParses.add(parse);
            fspsForSent.put(identifier, kbestParses);
            allCorpusFsps.put(corpusSentNum, fspsForSent);
        }
        return allCorpusFsps;
    }

    public Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> readAllFeFiles(String feDir) {
        Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> allCorpusFsps = Maps.newTreeMap();
        int numRanks = new File(feDir).listFiles().length;
        for (int rank = 0; rank < numRanks; rank++) {
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            allCorpusFsps = readFeFile(feFileName, allCorpusFsps);
        }
        return allCorpusFsps;
    }

}
