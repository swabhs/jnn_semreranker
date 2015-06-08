package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.basic.ConllElement;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.FspScore;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class FileUtils {

    public static class RerankingData {

        public TrainData trainData;
        public TestData testData;
        public TestData devData;

        public FrameNetVocabs vocabs;

        public RerankingData(TrainData trainData, TestData testData, TestData devData,
                FrameNetVocabs vocabs) {
            this.trainData = trainData;
            this.testData = testData;
            this.devData = devData;

            this.vocabs = vocabs;
        }
    }

    public static RerankingData readAllRerankingingData(boolean useMini) {

        FeReader reader = new FeReader();
        Set<String> tokensVocab = Sets.newHashSet();
        Set<String> posVocab = Sets.newHashSet();

        DataPaths dataPaths = new DataPaths(useMini, "train");
        Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> allTrainFsps =
                readAndSortTrain(dataPaths.xmlDir, dataPaths.feDir, dataPaths.synDir, reader);
        SentsAndToks sentsAndToks = readConlls(dataPaths.conllFile);
        tokensVocab.addAll(sentsAndToks.tokensVocab);
        posVocab.addAll(sentsAndToks.posVocab);

        System.err.println("Number of sentences read = " + sentsAndToks.allLemmas.size());
        System.err.println("Numer of FSPs read = " + allTrainFsps.keySet().size());

        // Adding the tokens to the fes
        List<TrainInstance> instances = Lists.newArrayList();
        for (int ex : allTrainFsps.keySet()) {
            instances.add(new TrainInstance(
                    sentsAndToks.allLemmas.get(ex),
                    sentsAndToks.allPostags.get(ex),
                    allTrainFsps.get(ex)));
        }

        // TEST /////////////////////////

        DataPaths testDataPaths = new DataPaths(useMini, "test");
        SentsAndToks testSentsAndToks = readConlls(testDataPaths.conllFile);
        tokensVocab.addAll(testSentsAndToks.tokensVocab);
        posVocab.addAll(testSentsAndToks.posVocab);

        Map<Integer, List<Scored<FrameSemanticParse>>> allTestFsps =
                readTest(testDataPaths.xmlDir, testDataPaths.feDir, testDataPaths.synDir, reader);

        System.err.println("Number of TEST sentences read = "
                + testSentsAndToks.allLemmas.size());
        System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());

        Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        for (int ex : allTestFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allTestFsps.get(ex);
            testInstances.put(ex, new TestInstance(testSentsAndToks.allLemmas.get(ex),
                    testSentsAndToks.allPostags.get(ex), unsortedParses));
        }

        // DEV ////////////////////////////////////////

        DataPaths devDataPaths = new DataPaths(useMini, "dev");
        SentsAndToks devSentsAndToks = readConlls(devDataPaths.conllFile);
        tokensVocab.addAll(devSentsAndToks.tokensVocab);
        posVocab.addAll(devSentsAndToks.posVocab);

        Map<Integer, List<Scored<FrameSemanticParse>>> allDevFsps =
                readTest(devDataPaths.xmlDir, devDataPaths.feDir, devDataPaths.synDir, reader);

        System.err.println("Number of DEV sentences read = "
                + devSentsAndToks.allLemmas.size());
        System.err.println("Numer of DEV FSPs read = " + allDevFsps.keySet().size());

        Map<Integer, TestInstance> devInstances = Maps.newHashMap();
        for (int ex : allDevFsps.keySet()) {
            List<Scored<FrameSemanticParse>> unsortedParses = allDevFsps.get(ex);
            devInstances
                    .put(ex, new TestInstance(devSentsAndToks.allLemmas.get(ex),
                            devSentsAndToks.allPostags.get(ex),
                            unsortedParses));
        }

        // //////////////
        // posVocab.addAll(reader.getPosTags());
        FrameNetVocabs vocabs = new FrameNetVocabs(
                tokensVocab,
                posVocab,
                reader.getPosTags(),
                reader.getFrameIds(),
                reader.getFrameArgIds());
        return new RerankingData(new TrainData(instances), new TestData(
                testInstances), new TestData(devInstances), vocabs);
    }

    public static Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> readAndSortTrain(
            String xmlDir,
            String feDir,
            String synDir,
            FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> allFsps = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            String synScoreFileName = synDir + rank + DataPaths.TURBO_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readFeFile(feFileName);
            Map<Integer, FspScore> framescores = readFscoreFile(xmlFileName);
            Map<Integer, Double> synScores = readSynScoreFile(synScoreFileName);

            for (int exNum : fsps.keySet()) {
                Scored<FrameSemanticParse> scoFsp = null;
                if (framescores.containsKey(exNum) == false) {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), 0.0, 0.0, 0.0, 0.0, synScores.get(exNum));
                } else {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), framescores.get(exNum), synScores.get(exNum));
                }

                if (allFsps.containsKey(exNum) == false) {
                    TreeMultiset<Scored<FrameSemanticParse>> multiset = TreeMultiset.create();
                    allFsps.put(exNum, multiset);
                }
                TreeMultiset<Scored<FrameSemanticParse>> sortedFsps = allFsps.get(exNum);
                sortedFsps.add(scoFsp);
                allFsps.put(exNum, sortedFsps);
            }
            if (rank % 25 == 0)
                System.err.println("read and sorted rank " + rank);
        }
        return allFsps;
    }

    public static Map<Integer, List<Scored<FrameSemanticParse>>> readTest(
            String xmlDir,
            String feDir,
            String synDir,
            FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, List<Scored<FrameSemanticParse>>> allFsps = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            String synScoreFileName = synDir + rank + DataPaths.TURBO_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readFeFile(feFileName);
            Map<Integer, FspScore> framescores = readFscoreFile(xmlFileName);
            Map<Integer, Double> synScores = readSynScoreFile(synScoreFileName);

            // if (rank == 0 && fsps.size() != framescores.size()) {
            // System.err.println(fsps.size() + " " + framescores.size());
            // System.err.println("##### TIME TO RAISE HELL #####");
            // }
            for (int exNum : fsps.keySet()) {
                Scored<FrameSemanticParse> scoFsp = null;
                if (framescores.containsKey(exNum) == false) {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), 0.0, 0.0, 0.0, 0.0, synScores.get(exNum));
                } else {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), framescores.get(exNum), synScores.get(exNum));
                }
                if (allFsps.containsKey(exNum) == false) {

                    allFsps.put(exNum, new ArrayList<Scored<FrameSemanticParse>>());
                }
                List<Scored<FrameSemanticParse>> sortedFsps = allFsps.get(exNum);
                sortedFsps.add(scoFsp);
                allFsps.put(exNum, sortedFsps);
            }
            if (rank % 25 == 0)
                System.err.println("read rank " + rank);
        }
        return allFsps;
    }

    public static class SentsAndToks {

        public List<String[]> allLemmas;
        public List<String[]> allPostags;
        public Set<String> tokensVocab;
        public Set<String> posVocab;

        public SentsAndToks(
                List<String[]> allLemmas,
                List<String[]> allPostags,
                Set<String> tokensVocab,
                Set<String> posVocab) {
            this.allLemmas = allLemmas;
            this.allPostags = allPostags;
            this.tokensVocab = tokensVocab;
            this.posVocab = posVocab;
        }
    }

    public static SentsAndToks readConlls(String fileName) {
        List<String[]> allToks = Lists.newArrayList();
        List<String[]> allPostags = Lists.newArrayList();
        Set<String> tokensVocab = Sets.newHashSet();
        Set<String> posVocab = Sets.newHashSet();

        List<Conll> sents = BasicFileReader.readConllFile(fileName);

        for (Conll sent : sents) {
            String[] tokens = new String[sent.getElements().size()];
            String[] posTags = new String[sent.getElements().size()];
            int i = 0;
            for (ConllElement element : sent.getElements()) {
                tokens[i] = element.getLemma();
                tokensVocab.add(element.getLemma());
                posTags[i] = element.getCoarsePosTag();
                posVocab.add(element.getCoarsePosTag());
                i++;
            }
            allToks.add(tokens);
            allPostags.add(posTags);
        }

        return new SentsAndToks(allToks, allPostags, tokensVocab, posVocab);
    }

    public static Map<Integer, FspScore> readFscoreFile(String fscoreFileName) {
        List<String> fscoreLines = BasicFileReader.readFile(fscoreFileName);

        Map<Integer, FspScore> scores = new TreeMap<Integer, FspScore>();
        for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
            String[] ele = fscoreLines.get(lineNum).trim().split("\t");
            int exNum = Integer.parseInt(ele[0]);

            String rfrac = ele[1].split("\\(")[1];
            double rNum = Double.parseDouble(rfrac.split("/")[0]);
            double rDenom = Double.parseDouble(rfrac.split("/")[1].split("\\)")[0]);
            String pfrac = ele[2].split("\\(")[1];
            double pNum = Double.parseDouble(pfrac.split("/")[0]);
            double pDenom = Double.parseDouble(pfrac.split("/")[1].split("\\)")[0]);
            double fscore = Double.parseDouble(ele[3]);

            scores.put(exNum, new FspScore(pNum, pDenom, rNum, rDenom, fscore));
        }
        return scores;
    }

    public static Map<Integer, Double> readSynScoreFile(String turboScoreFileName) {
        List<String> synScoreLines = BasicFileReader.readFile(turboScoreFileName);

        Map<Integer, Double> scores = new TreeMap<Integer, Double>();
        for (int lineNum = 1; lineNum < synScoreLines.size(); lineNum++) {
            String[] ele = synScoreLines.get(lineNum).trim().split("\t");
            int exNum = Integer.parseInt(ele[0]);
            double synScore = Double.parseDouble(ele[1]);
            scores.put(exNum, synScore);
        }
        return scores;
    }

}
