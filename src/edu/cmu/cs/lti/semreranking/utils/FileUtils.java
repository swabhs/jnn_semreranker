package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.TrainingData;
import edu.cmu.cs.lti.semreranking.TrainingInstance;
import edu.cmu.cs.lti.semreranking.jnn.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.utils.FeReader.SentsAndToks;

public class FileUtils {

    public static TrainingData readAllTrainingData(
            String resDir,
            String xmlFileExtn,
            String trainfeFileDir,
            String feFileExtn,
            String tokFileName) {
        List<TrainingInstance> instances = Lists.newArrayList();
        FeReader reader = new FeReader();
        SentsAndToks sentsAndToks = reader.readTokFile(tokFileName);

        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps =
                readKBestSemaforOutput(resDir, xmlFileExtn, trainfeFileDir, feFileExtn); // TODO:
                                                                                         // not
                                                                                         // right
        // because f-scores
        // do not correspond to sorted
        // parses

        System.err.println(sentsAndToks.allToks.size());

        System.err.println(allFsps.rowKeySet().size());

        for (int ex = 0; ex < allFsps.rowKeySet().size(); ex++) {
            List<Scored<FrameSemanticParse>> sortedParses = Lists.newArrayList();
            for (int rank = 0; rank < allFsps.columnKeySet().size(); rank++) {
                sortedParses.add(allFsps.get(ex, rank));
            }

            instances.add(new TrainingInstance(sentsAndToks.allToks.get(ex), sortedParses));
        }

        FrameNetVocabs vocabs = new FrameNetVocabs(
                sentsAndToks.tokensVocab,
                reader.getFrameIds(),
                reader.getFrameArgIds());
        return new TrainingData(instances, vocabs);
    }

    public static Table<Integer, Integer, Scored<FrameSemanticParse>> readKBestSemaforOutput(
            String resultsDir,
            String xmlFileExtn,
            String outputFeDir,
            String feFileExtn) {
        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps = HashBasedTable.create();
        int numRanks = new File(resultsDir).listFiles().length;
        for (int i = 0; i < numRanks; i++) {
            String resFileName = resultsDir + i + xmlFileExtn;
            String feFileName = outputFeDir + i + feFileExtn;
            FeReader reader = new FeReader();

            Map<Integer, FrameSemanticParse> fsps = reader.readSingleFEfile(feFileName);
            List<String> lines = BasicFileReader.readFile(resFileName);

            for (int j = 1; j < lines.size(); j++) {
                String[] ele = lines.get(j).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);
                Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                        fsps.get(exNum), Double.parseDouble(ele[3]));
                allFsps.put(exNum, i, instance);

                // TODO: remove
                // if (j == 5) {
                // break;
                // }
            }
        }
        return allFsps;
    }

}
