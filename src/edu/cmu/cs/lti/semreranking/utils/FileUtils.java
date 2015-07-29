package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.basic.ConllElement;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.FsaScore;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class FileUtils {

    public static class AllRerankingData {

        public TrainData trainData;
        public TestData testData;
        public TestData devData;

        public FrameNetVocabs vocabs;

        public AllRerankingData(TrainData trainData, TestData testData, TestData devData,
                FrameNetVocabs vocabs) {
            this.trainData = trainData;
            this.testData = testData;
            this.devData = devData;

            this.vocabs = vocabs;
        }
    }

    public static AllRerankingData readAllRerankingingData(boolean useMini) {

        FeReader reader = new FeReader();
        Set<String> tokensVocab = Sets.newHashSet();
        Set<String> posVocab = Sets.newHashSet();

        DataPaths dataPaths = new DataPaths(useMini, "train");
        Map<Integer, List<Scored<FrameSemAnalysis>>> allTrainFsps =
                readTest(dataPaths.semaforResultsDir, dataPaths.semaforOutFEDir,
                        dataPaths.synScoresDir, reader);
        int numTrainRanks = allTrainFsps.entrySet().iterator().next().getValue().size();

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

        Map<Integer, List<Scored<FrameSemAnalysis>>> allTestFsps =
                readTest(testDataPaths.semaforResultsDir, testDataPaths.semaforOutFEDir,
                        testDataPaths.synScoresDir, reader);
        int numTestRanks = allTestFsps.entrySet().iterator().next().getValue().size();

        System.err.println("Number of TEST sentences read = "
                + testSentsAndToks.allLemmas.size());
        System.err.println("Numer of TEST FSPs read = " + allTestFsps.keySet().size());

        List<TestInstance> testInstances = Lists.newArrayList();
        for (int ex : allTestFsps.keySet()) {
            List<Scored<FrameSemAnalysis>> unsortedParses = allTestFsps.get(ex);
            testInstances.add(new TestInstance(testSentsAndToks.allLemmas.get(ex),
                    testSentsAndToks.allPostags.get(ex), unsortedParses));
        }

        // DEV ////////////////////////////////////////

        DataPaths devDataPaths = new DataPaths(useMini, "dev");
        SentsAndToks devSentsAndToks = readConlls(devDataPaths.conllFile);
        tokensVocab.addAll(devSentsAndToks.tokensVocab);
        posVocab.addAll(devSentsAndToks.posVocab);

        Map<Integer, List<Scored<FrameSemAnalysis>>> allDevFsps =
                readTest(devDataPaths.semaforResultsDir, devDataPaths.semaforOutFEDir,
                        devDataPaths.synScoresDir, reader);
        int numDevRanks = allDevFsps.entrySet().iterator().next().getValue().size();

        System.err.println("Number of DEV sentences read = "
                + devSentsAndToks.allLemmas.size());
        System.err.println("Numer of DEV FSPs read = " + allDevFsps.keySet().size());

        List<TestInstance> devInstances = Lists.newArrayList();
        for (int ex : allDevFsps.keySet()) {
            List<Scored<FrameSemAnalysis>> unsortedParses = allDevFsps.get(ex);
            devInstances.add(new TestInstance(devSentsAndToks.allLemmas.get(ex),
                    devSentsAndToks.allPostags.get(ex), unsortedParses));
        }

        // posVocab.addAll(reader.getPosTags());
        FrameNetVocabs vocabs = new FrameNetVocabs(
                tokensVocab,
                posVocab,
                // reader.getPosTags(),
                reader.getFrameIds(),
                reader.getFrameArgIds());
        return new AllRerankingData(
                new TrainData(instances, numTrainRanks),
                new TestData(testInstances, numTestRanks),
                new TestData(devInstances, numDevRanks),
                vocabs);
    }

    public static Map<Integer, List<Scored<FrameSemAnalysis>>> readTest(
            String xmlDir,
            String feDir,
            String synDir,
            FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, List<Scored<FrameSemAnalysis>>> allFsps = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.RESULTS_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            String synScoreFileName = synDir + rank + DataPaths.SYNSCORE_FILE_EXTN;

            Multimap<Integer, FrameSemAnalysis> fsps = reader.readFeFile(feFileName);
            Map<Integer, FsaScore> framescores = readFscoreFile(xmlFileName);
            Map<Integer, Double> synScores = readSynScoreFile(synScoreFileName);

            for (int exNum : fsps.keySet()) {
                for (FrameSemAnalysis fsp : fsps.get(exNum)) {
                    Scored<FrameSemAnalysis> scoFsp = null;
                    if (framescores.containsKey(exNum) == false) {
                        scoFsp = new Scored<FrameSemAnalysis>(
                                fsp, new FsaScore(), synScores.get(exNum), rank);
                    } else {
                        scoFsp = new Scored<FrameSemAnalysis>(
                                fsp, framescores.get(exNum), synScores.get(exNum), rank);
                    }
                    if (allFsps.containsKey(exNum) == false) {

                        allFsps.put(exNum, new ArrayList<Scored<FrameSemAnalysis>>());
                    }
                    List<Scored<FrameSemAnalysis>> sortedFsps = allFsps.get(exNum);
                    sortedFsps.add(scoFsp);
                    allFsps.put(exNum, sortedFsps);
                }
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

    public static Map<Integer, FsaScore> readFscoreFile(String fscoreFileName) {
        List<String> fscoreLines = BasicFileReader.readFile(fscoreFileName);

        Map<Integer, FsaScore> scores = new TreeMap<Integer, FsaScore>();
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

            scores.put(exNum, new FsaScore(pNum, pDenom, rNum, rDenom, fscore));
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
