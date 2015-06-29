package edu.cmu.cs.lti.semreranking.datageneration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.utils.FeReader;

public class TrainingDataGeneratorMain {

    public static String semHome = SemRerankerMain.semHome;
    public static String dir = semHome + "/training/data/emnlp2015/prepare/";
    public static String xmlFile = dir + "cv.train+dev.sentences.lrb.xml";
    public static String conllFile = dir
            + "cv.train+dev.sentences.turboparsed.basic.stanford.lemmatized.conll";
    public static String tokFile = dir + "cv.train+dev.sentences.tokenized";
    public static String feFile = dir + "cv.train+dev.sentences.frame.elements";
    public static String framesFile = dir + "cv.train+dev.sentences.frames";

    public static String outDir = semHome + "training/data/emnlp2015/cv/";

    public static final int numFolds = 5;
    public static int foldSize;

    public static void main(String[] args) {
        List<String> allGoldXmls = readXmlFile(xmlFile);
        String header = allGoldXmls.get(0);
        String footer = allGoldXmls.get(allGoldXmls.size() - 1);
        allGoldXmls = allGoldXmls.subList(2, allGoldXmls.size() - 1); // dropping the first sent,
                                                                      // which has
        // no annotation anyway, to make
        // equisized folds

        List<Conll> allConlls = BasicFileReader.readConllFile(conllFile);
        allConlls = allConlls.subList(1, allConlls.size());
        List<String> allToks = BasicFileReader.readFile(tokFile);
        allToks = allToks.subList(1, allToks.size());

        FeReader reader = new FeReader();
        Multimap<Integer, FrameSemanticParse> allFes = reader.readFeFile(feFile); // TODO

        List<Integer> randomizedOrder = readRandomList(outDir + "train+dev.sents.order"); // generateRandomList(allGoldXmls.size());

        System.err.println("# folds = " + numFolds + "\tfold size = " + allGoldXmls.size() * 1.0
                / numFolds);
        foldSize = allGoldXmls.size() / numFolds;

        for (int fold = 0; fold < numFolds; fold++) {
            String foldOutDir = outDir + "cv" + fold + "/";

            // TEST FILES
            List<String> xmlOutLines = Lists.newArrayList();
            List<Conll> outConlls = Lists.newArrayList();
            List<String> outToks = Lists.newArrayList();
            Map<Integer, FrameSemanticParse> outFsps = Maps.newTreeMap();

            xmlOutLines.add(header);
            for (int testIdx = 0; testIdx < foldSize; testIdx++) {
                int randidx = randomizedOrder.get(fold * foldSize + testIdx);
                String line = allGoldXmls.get(randidx);
                xmlOutLines.add(line.replaceAll("ID=\"" + (randidx + 1), "ID=\"" + testIdx));
                outConlls.add(allConlls.get(randidx));
                outToks.add(allToks.get(randidx));
                if (allFes.containsKey(randidx + 1)) {
                    outFsps.put(testIdx, allFes.get(randidx + 1).iterator().next());

                }
            }
            xmlOutLines.add(footer);

            BasicFileWriter.writeStrings(xmlOutLines, foldOutDir + "cv" + fold
                    + ".test.sentences.lrb.xml");
            BasicFileWriter.writeConll(outConlls, foldOutDir + "cv" + fold
                    + ".test.sentences.turboparsed.basic.stanford.lemmatized.conll");
            BasicFileWriter.writeStrings(outToks, foldOutDir + "cv" + fold
                    + ".test.sentences.tokenized");
            writeFramesLines(outFsps, foldOutDir + "cv" + fold
                    + ".test.sentences.frames");
            writeFeLines(outFsps, foldOutDir + "cv" + fold
                    + ".test.sentences.frame.elements");

            // TRAIN FILES
            List<Conll> trainOutConlls = Lists.newArrayList();
            List<String> trainOutToks = Lists.newArrayList();
            Map<Integer, FrameSemanticParse> trainOutFsps = Maps.newTreeMap();

            int idx = 0;
            for (int testIdx = 0; testIdx < allGoldXmls.size(); testIdx++) {
                if (testIdx >= fold * foldSize && testIdx < (fold + 1) * foldSize) {
                    continue;
                }
                int randidx = randomizedOrder.get(testIdx);
                trainOutConlls.add(allConlls.get(randidx));
                trainOutToks.add(allToks.get(randidx));
                if (allFes.containsKey(randidx + 1)) {
                    trainOutFsps.put(idx, allFes.get(randidx + 1).iterator().next());
                }
                idx++;
            }

            BasicFileWriter.writeConll(trainOutConlls, foldOutDir + "cv" + fold
                    + ".train.sentences.turboparsed.basic.stanford.lemmatized.conll");
            BasicFileWriter.writeStrings(trainOutToks, foldOutDir + "cv" + fold
                    + ".train.sentences.tokenized");
            writeFeLines(trainOutFsps, foldOutDir + "cv" + fold
                    + ".train.sentences.frame.elements");
        }
    }

    public static List<Integer> generateRandomList(int sz) {
        List<Integer> idxs = Lists.newArrayList();

        for (int i = 0; i < sz; i++) {
            idxs.add(i);
        }
        Collections.shuffle(idxs);

        List<String> idxStrs = Lists.newArrayList();
        for (int i = 0; i < sz; i++) {
            idxStrs.add(idxs.get(i).toString());
        }
        BasicFileWriter.writeStrings(idxStrs, outDir + "train+dev.sents.order");
        return idxs;
    }

    public static List<Integer> readRandomList(String fileName) {
        List<Integer> idxs = Lists.newArrayList();
        List<String> idxStrs = BasicFileReader.readFile(fileName);
        for (String idxStr : idxStrs) {
            idxs.add(Integer.parseInt(idxStr));
        }
        return idxs;
    }

    public static List<String> readXmlFile(String fileName) {
        List<String> allXmls = Lists.newArrayList();
        List<String> xmlLines = BasicFileReader.readFile(fileName);

        int idx = 0;
        int infileIdx = -1;

        StringBuilder builder = new StringBuilder();
        for (String line : xmlLines.subList(0, 7)) {
            builder.append(line);
            builder.append("\n");
        }

        for (String line : xmlLines.subList(7, xmlLines.size() - 6)) {
            if (line.contains("<sentence ID=")) {
                allXmls.add(builder.toString().substring(0, builder.length() - 1));

                builder = new StringBuilder();
                builder.append("            <sentence ID=\"");
                builder.append((idx));
                builder.append("\">\n");
                idx++;
                infileIdx = (infileIdx + 1) % 2780; // HACKY!
                continue;
            }
            String outLine = line.replaceAll("ID=\"" + infileIdx, "ID=\"" + (idx - 1));
            builder.append(outLine);
            builder.append("\n");
        }
        allXmls.add(builder.toString().substring(0, builder.length() - 1));

        builder = new StringBuilder();
        for (String line : xmlLines.subList(xmlLines.size() - 6, xmlLines.size())) {
            builder.append(line);
            builder.append("\n");
        }
        allXmls.add(builder.toString().substring(0, builder.length() - 1));

        return allXmls;
    }

    public static void writeFeLines(Map<Integer, FrameSemanticParse> fsps, String fileName) {
        List<String> feLines = Lists.newArrayList();
        for (int i : fsps.keySet()) {
            feLines.add(fsps.get(i).toString(i));
        }
        BasicFileWriter.writeStrings(feLines, fileName);
    }

    public static void writeFramesLines(Map<Integer, FrameSemanticParse> fsps, String fileName) {
        List<String> feLines = Lists.newArrayList();
        for (int i : fsps.keySet()) {
            FrameSemanticParse fsp = fsps.get(i);
            for (Frame frame : fsp.frames) {
                StringBuilder builder = new StringBuilder();
                builder.append("1\t");
                builder.append(frame.score);
                builder.append("\t1\t");
                builder.append(frame.id);
                builder.append("\t");
                builder.append(frame.lexicalUnit);
                builder.append("\t");
                builder.append(frame.predStartPos);
                if (frame.predEndPos != frame.predStartPos) {
                    for (int j = frame.predStartPos + 1; j <= frame.predEndPos; j++) {
                        builder.append("_");
                        builder.append(j);
                    }
                }
                builder.append("\t");
                builder.append(frame.predToken);
                builder.append("\t");
                builder.append(i);
                feLines.add(builder.toString());
            }
        }
        BasicFileWriter.writeStrings(feLines, fileName);
    }
}
