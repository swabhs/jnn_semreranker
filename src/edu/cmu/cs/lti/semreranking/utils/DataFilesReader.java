package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.datastructs.SemevalScore;
import edu.cmu.cs.lti.semreranking.utils.CopyOfSentConllReader.SentsAndToks;

public class DataFilesReader {

    public static class AllRerankingData {
        public TrainData trainData;
        public TestData devData;
        public FrameNetVocabs vocabs;

        public AllRerankingData(TrainData trainData, TestData devData, FrameNetVocabs vocabs) {
            this.trainData = trainData;
            this.devData = devData;
            this.vocabs = vocabs;
        }
    }

    public static AllRerankingData readAllRerankingData(boolean useMini) {
        FeReader reader = new FeReader();

        Set<String> tokensVocab = Sets.newHashSet();
        Set<String> posVocab = Sets.newHashSet();

        DataPaths trainDataPaths = new DataPaths(useMini, "train");
        SentsAndToks trainSentsAndToks = CopyOfSentConllReader.readConlls(trainDataPaths.conllFile);
        tokensVocab.addAll(trainSentsAndToks.tokensVocab);
        posVocab.addAll(trainSentsAndToks.posVocab);

        Map<Integer, Map<FrameIdentifier, List<Scored<FrameSemParse>>>> allTrainFsps =
                readDataSet(trainDataPaths, reader, trainSentsAndToks.allLemmas.size());

        System.err.println("Number of sentences read = " + trainSentsAndToks.allLemmas.size());
        System.err.println("Numer of sentences with FSAs = " + allTrainFsps.keySet().size());

        // Adding the tokens to the FEs
        Map<Integer, Map<FrameIdentifier, TrainInstance>> trainInstances = Maps.newTreeMap();
        int numTrainInstances = 0;
        for (int ex : allTrainFsps.keySet()) {
            Map<FrameIdentifier, TrainInstance> instancesInEx = Maps.newHashMap();
            for (FrameIdentifier identifier : allTrainFsps.get(ex).keySet()) {
                numTrainInstances++;
                instancesInEx.put(identifier, new TrainInstance(
                        ex,
                        identifier,
                        allTrainFsps.get(ex).get(identifier)));
            }
            trainInstances.put(ex, instancesInEx);
        }

        // DEV ////////////////////////////////////////

        DataPaths devDataPaths = new DataPaths(useMini, "dev");
        SentsAndToks devSentsAndToks = CopyOfSentConllReader.readConlls(
                devDataPaths.conllFile); // taking
                                         // care
                                         // of
                                         // unknown
                                         // tokens
        tokensVocab.addAll(devSentsAndToks.tokensVocab);
        posVocab.addAll(devSentsAndToks.posVocab);

        Map<Integer, Map<FrameIdentifier, List<Scored<FrameSemParse>>>> allDevFsps =
                readDataSet(devDataPaths, reader, devSentsAndToks.allLemmas.size());

        System.err.println("Number of DEV sentences read = " + devSentsAndToks.allLemmas.size());
        System.err.println("Numer of DEV sentences with FSAs = " + allDevFsps.keySet().size());

        Map<Integer, Map<FrameIdentifier, TestInstance>> devInstances = Maps.newTreeMap();
        int numTestInstances = 0;
        for (int exNum : allDevFsps.keySet()) {
            Map<FrameIdentifier, TestInstance> instancesInEx = Maps.newHashMap();
            for (FrameIdentifier identifier : allDevFsps.get(exNum).keySet()) {
                numTestInstances++;
                instancesInEx.put(identifier, new TestInstance(
                        exNum,
                        identifier,
                        allDevFsps.get(exNum).get(identifier)));
            }
            devInstances.put(exNum, instancesInEx);
        }

        FrameNetVocabs vocabs = new FrameNetVocabs(
                trainSentsAndToks.tokensVocab,
                trainSentsAndToks.posVocab,
                reader.getFrameIds(),
                reader.getFrameArgIds());
        return new AllRerankingData(
                new TrainData(
                        trainSentsAndToks.allLemmas,
                        trainSentsAndToks.allPostags,
                        trainInstances,
                        numTrainInstances,
                        trainDataPaths.numRanks),
                new TestData(
                        devSentsAndToks.allLemmas,
                        devSentsAndToks.allPostags,
                        devInstances,
                        numTestInstances,
                        devDataPaths.numRanks),
                vocabs);
    }

    public static Map<Integer, Map<FrameIdentifier, List<Scored<FrameSemParse>>>> readDataSet(
            DataPaths dataPaths,
            FeReader feReader,
            int totNumEx) {
        System.err.println("Reading data from...");
        String feDir = dataPaths.semaforOutFEDir;
        System.err.println(feDir);
        String xmlDir = dataPaths.semaforResultsDir;
        System.err.println(xmlDir);
        String synDir = dataPaths.synScoresDir;
        System.err.println(synDir);
        String tokFileName = dataPaths.tokFile;

        Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> kbestFspsMap = feReader
                .readAllFeFiles(feDir);

        Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> kbestFscoMap = ResultsFileUtils
                .readAllFscores(tokFileName, xmlDir);

        Map<Integer, List<Double>> kbestSynScoresMap = SynScoreReader.readAllSynScores(synDir);

        Map<Integer, Map<FrameIdentifier, List<Scored<FrameSemParse>>>> dataInsts =
                Maps.newTreeMap();

        for (int exNum = 0; exNum < totNumEx; exNum++) {
            if (kbestFspsMap.containsKey(exNum) == false // not all sents have frames
                    || kbestFscoMap.containsKey(exNum) == false) { // not all frames have annotation
                continue; // TODO: is this the right thing to do?
            }
            Map<FrameIdentifier, List<Scored<FrameSemParse>>> idParseMap = Maps.newHashMap();

            for (FrameIdentifier identifier : kbestFspsMap.get(exNum).keySet()) {
                // matching:
                if (kbestFscoMap.get(exNum).containsKey(identifier) == false) {
                    System.err
                            .println("WARNING:Missing sent " + exNum + ":\n" + identifier.frameId);
                    continue; // this should never happen, every FSP must have a score
                }

                int numRanks = kbestFspsMap.get(exNum).get(identifier).size();
                if (numRanks != kbestFscoMap.get(exNum).get(identifier).size()) {
                    System.err.println("WARNING:#" + exNum + " " + identifier.frameId
                            + "\tnumParses = " + numRanks
                            + " evaluated = " + kbestFscoMap.get(exNum).get(identifier).size());
                    // this should never happen, every FSP must have a score. BUT it does!
                }

                List<Scored<FrameSemParse>> scoredParses = Lists.newArrayList();
                for (int rank = 0; rank < numRanks; rank++) {
                    SemevalScore seScore = null;
                    if (kbestFscoMap.get(exNum).get(identifier).size() <= rank) {
                        seScore = new SemevalScore(); // HACK: all zeroes for missing
                    } else {
                        seScore = kbestFscoMap.get(exNum).get(identifier).get(rank);
                    }
                    scoredParses.add(new Scored<FrameSemParse>(
                            kbestFspsMap.get(exNum).get(identifier).get(rank),
                            seScore,
                            kbestSynScoresMap.get(exNum).get(rank),
                            rank));
                }
                idParseMap.put(identifier, scoredParses);
            }
            dataInsts.put(exNum, idParseMap);
        }
        return dataInsts;
    }
}
