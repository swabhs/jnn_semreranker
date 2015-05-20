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
                readKBestSemaforOutput(resDir, xmlFileExtn, trainfeFileDir, feFileExtn, reader); // TODO:
        // not
        // right
        // because f-scores
        // do not correspond to sorted
        // parses. TODO: make sorted f-scores!!

        System.err.println("Number of sentences read = " + sentsAndToks.allToks.size());
        System.err.println("Numer of FSPs read = " + allFsps.rowKeySet().size());

        for (int ex : allFsps.rowKeySet()) {
            List<Scored<FrameSemanticParse>> sortedParses = Lists.newArrayList();
            for (int rank = 0; rank < allFsps.columnKeySet().size(); rank++) {
                sortedParses.add(allFsps.get(ex, rank));
            }
            System.err.println("num sorted parses of ex " + ex + " = " + sortedParses.size());
            instances.add(new TrainingInstance(sentsAndToks.allToks.get(ex), sortedParses));
        }

        // System.err.println("test: ***** " +
        // instances.get(0).sortedParses.get(0).entity.toString());

        instances.get(0).sortedParses.get(0).entity.print();
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
            String feFileExtn,
            FeReader reader) {
        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps = HashBasedTable.create();
        int numRanks = new File(outputFeDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String resFileName = resultsDir + rank + xmlFileExtn;
            String feFileName = outputFeDir + rank + feFileExtn;

            Map<Integer, FrameSemanticParse> fsps = reader.readSingleFEfile(feFileName);
            List<String> fscoreLines = BasicFileReader.readFile(resFileName);

            for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
                String[] ele = fscoreLines.get(lineNum).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);

                if (fsps.containsKey(exNum)) { // read only relevant f-scores
                    Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), Double.parseDouble(ele[3]));
                    allFsps.put(exNum, rank, instance);
                }

            }
        }

        System.err.println("Number of ranks read = " + allFsps.columnKeySet().size());
        System.err.println("Number of examples read = " + allFsps.rowKeySet().size());
        System.err.println("total = " + allFsps.size());

        allFsps.get(1, 0).entity.print();
        return allFsps;
    }

}
