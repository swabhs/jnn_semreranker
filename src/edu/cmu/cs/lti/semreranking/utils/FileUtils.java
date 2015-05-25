package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.RerankingData;
import edu.cmu.cs.lti.semreranking.Scored;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.SentsAndToks;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.jnn.FrameNetVocabs;

public class FileUtils {

    public static RerankingData readAllTrainingData(boolean useMini) {

        FeReader reader = new FeReader();

        SentsAndToks sentsAndToks = readTokFile(DataPaths.TOKEN_FILE_TRAIN);
        DataPaths dataPaths = new DataPaths(useMini, true, SemRerankerMain.model);
        Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> allTrainFsps =
                readAndSortTrain(dataPaths.xmlDir, dataPaths.feDir, reader);

        System.err.println("Number of sentences read = " + sentsAndToks.allToks.size());
        System.err.println("Numer of FSPs read = " + allTrainFsps.keySet().size());

        // Adding the tokens to the fes
        List<TrainInstance> instances = Lists.newArrayList();
        for (int ex : allTrainFsps.keySet()) {
            instances.add(new TrainInstance(sentsAndToks.allToks.get(ex), allTrainFsps.get(ex)));
        }

        // TEST /////////////////////////

        DataPaths testDataPaths = new DataPaths(useMini, SemRerankerMain.model);
        SentsAndToks testSentsAndToks = readTokFile(DataPaths.TOKEN_FILE_TEST);
        sentsAndToks.tokensVocab.addAll(testSentsAndToks.tokensVocab);
        Map<Integer, List<Scored<FrameSemanticParse>>> allTestFsps =
                readTest(testDataPaths.xmlDir, testDataPaths.feDir, reader);

        System.err.println("Number of TEST sentences read = " + testSentsAndToks.allToks.size());
        System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());

        Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        for (int ex : allTestFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allTestFsps.get(ex);
            testInstances
                    .put(ex, new TestInstance(testSentsAndToks.allToks.get(ex), unsortedParses));
        }

        // DEV ////////////////////////////////////////

        DataPaths devDataPaths = new DataPaths(useMini, SemRerankerMain.model, "dev");
        SentsAndToks devSentsAndToks = readTokFile(DataPaths.TOKEN_FILE_DEV);
        sentsAndToks.tokensVocab.addAll(devSentsAndToks.tokensVocab);
        Map<Integer, List<Scored<FrameSemanticParse>>> allDevFsps =
                readTest(devDataPaths.xmlDir, devDataPaths.feDir, reader);

        System.err.println("Number of DEV sentences read = " + devSentsAndToks.allToks.size());
        System.err.println("Numer of DEV FSPs read = " + allDevFsps.keySet().size());

        Map<Integer, TestInstance> devInstances = Maps.newHashMap();
        for (int ex : allDevFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allDevFsps.get(ex);
            devInstances
                    .put(ex, new TestInstance(devSentsAndToks.allToks.get(ex), unsortedParses));
        }

        // //////////////

        FrameNetVocabs vocabs = new FrameNetVocabs(
                sentsAndToks.tokensVocab,
                reader.getFrameIds(),
                reader.getFrameArgIds());
        return new RerankingData(new TrainData(instances, vocabs), new TestData(
                testInstances), new TestData(devInstances));
    }

    public static Table<Integer, Integer, Scored<FrameSemanticParse>> readScoredFsps(
            String xmlDir, String feDir, FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps = HashBasedTable.create();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readSingleFEfile(feFileName);
            List<String> fscoreLines = BasicFileReader.readFile(xmlFileName);

            for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
                String[] ele = fscoreLines.get(lineNum).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);
                if (fsps.containsKey(exNum)) { // read only relevant f-scores
                    String rfrac = ele[1].split("\\(")[1];
                    String rNum = rfrac.split("/")[0];
                    String rDenom = rfrac.split("/")[1].split("\\)")[0];
                    String pfrac = ele[2].split("\\(")[1];
                    String pNum = pfrac.split("/")[0];
                    String pDenom = pfrac.split("/")[1].split("\\)")[0];
                    Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                            fsps.get(exNum),
                            Double.parseDouble(ele[3]),
                            Double.parseDouble(rNum),
                            Double.parseDouble(rDenom),
                            Double.parseDouble(pNum),
                            Double.parseDouble(pDenom));
                    allFsps.put(exNum, rank, instance);
                }
            }
        }

        System.err.println("Number of examples read = " + allFsps.rowKeySet().size());
        System.err
                .println("Number of ranked parses per example = " + allFsps.columnKeySet().size());
        return allFsps;
    }

    public static Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> readAndSortTrain(
            String xmlDir,
            String feDir, FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> allFsps = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readSingleFEfile(feFileName);
            List<String> fscoreLines = BasicFileReader.readFile(xmlFileName);

            for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
                String[] ele = fscoreLines.get(lineNum).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);

                if (fsps.containsKey(exNum)) { // read only relevant f-scores
                    String rfrac = ele[1].split("\\(")[1];
                    String rNum = rfrac.split("/")[0];
                    String rDenom = rfrac.split("/")[1].split("\\)")[0];
                    String pfrac = ele[2].split("\\(")[1];
                    String pNum = pfrac.split("/")[0];
                    String pDenom = pfrac.split("/")[1].split("\\)")[0];
                    Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), Double.parseDouble(ele[3]),
                            Double.parseDouble(rNum),
                            Double.parseDouble(rDenom),
                            Double.parseDouble(pNum),
                            Double.parseDouble(pDenom));

                    if (allFsps.containsKey(exNum) == false) {
                        TreeMultiset<Scored<FrameSemanticParse>> multiset = TreeMultiset.create();
                        allFsps.put(exNum, multiset);
                    }
                    TreeMultiset<Scored<FrameSemanticParse>> sortedFsps = allFsps.get(exNum);
                    sortedFsps.add(instance);
                    allFsps.put(exNum, sortedFsps);
                }
            }
            if (rank % 25 == 0)
                System.err.println("read and sorted rank " + rank);
        }
        // System.err.println("Number of examples = " + allFsps.keySet().size());
        // for (int i : allFsps.keySet()) {
        // System.err.println(allFsps.get(i).size() + " ranks for ex " + i);
        // }
        return allFsps;
    }

    public static Map<Integer, List<Scored<FrameSemanticParse>>> readTest(String xmlDir,
            String feDir, FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, List<Scored<FrameSemanticParse>>> allFsps = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readSingleFEfile(feFileName);
            List<String> fscoreLines = BasicFileReader.readFile(xmlFileName);

            for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
                String[] ele = fscoreLines.get(lineNum).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);

                if (fsps.containsKey(exNum)) { // read only relevant f-scores
                    String rfrac = ele[1].split("\\(")[1];
                    String rNum = rfrac.split("/")[0];
                    String rDenom = rfrac.split("/")[1].split("\\)")[0];
                    String pfrac = ele[2].split("\\(")[1];
                    String pNum = pfrac.split("/")[0];
                    String pDenom = pfrac.split("/")[1].split("\\)")[0];
                    Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), Double.parseDouble(ele[3]),
                            Double.parseDouble(rNum),
                            Double.parseDouble(rDenom),
                            Double.parseDouble(pNum),
                            Double.parseDouble(pDenom));

                    if (allFsps.containsKey(exNum) == false) {

                        allFsps.put(exNum, new ArrayList<Scored<FrameSemanticParse>>());
                    }
                    List<Scored<FrameSemanticParse>> sortedFsps = allFsps.get(exNum);
                    sortedFsps.add(instance);
                    allFsps.put(exNum, sortedFsps);
                }
            }
            if (rank % 25 == 0)
                System.err.println("read rank " + rank);
        }
        // System.err.println("Number of examples = " + allFsps.keySet().size());
        // for (int i : allFsps.keySet()) {
        // System.err.println(allFsps.get(i).size() + " ranks for ex " + i);
        // }
        return allFsps;
    }

    public static SentsAndToks readTokFile(String fileName) {
        Set<String> tokensVocab = Sets.newHashSet();
        List<String[]> allToks = Lists.newArrayList();
        List<String> sents = BasicFileReader.readFile(fileName);

        for (String sent : sents) {
            String[] tokens = sent.split(" ");
            allToks.add(tokens);
            for (String token : tokens) {
                tokensVocab.add(token);
            }
        }
        return new SentsAndToks(allToks, tokensVocab);
    }

}
