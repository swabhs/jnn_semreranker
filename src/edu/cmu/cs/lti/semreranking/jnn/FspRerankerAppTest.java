package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.FsaScore;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.lossfunctions.PairwiseLoss;
import edu.cmu.cs.lti.semreranking.utils.FileUtils.AllRerankingData;
import edu.cmu.cs.lti.semreranking.utils.StringUtils;

public class FspRerankerAppTest {

    // sentence: the rich banker buys expensive stocks

    TrainData trainData;
    TestData testData;
    TestData devData;

    FspRerankerApp reranker;

    @Before
    public void setUp() throws Exception {
        String tokens[] = "the rich banker purchases expensive stocks".split("\\w+");
        String posTags[] = "DT ADJ NN VB ADJ NN".split("\\w+");
        Argument arg1 = new Argument("type", 1, 1);
        Argument arg2 = new Argument("type", 4, 4);
        Argument arg3 = new Argument("plural", 5, 5);
        Argument arg4 = new Argument("buyer", 0, 2);
        Argument arg5 = new Argument("goods", 4, 5);

        Set<String> frameIds = Sets.newHashSet("BANKER", "STOCK", "BUY");
        Set<String> argIds = Sets.newHashSet(
                StringUtils.makeFrameArgId("BANKER", "type"),
                StringUtils.makeFrameArgId("STOCK", "type"),
                StringUtils.makeFrameArgId("STOCK", "plural"),
                StringUtils.makeFrameArgId("BUY", "buyer"),
                StringUtils.makeFrameArgId("BUY", "goods"));
        FrameSemParse frame1 = new FrameSemParse("BANKER", 2, 2, "banker.n", "banker", Sets.newHashSet(arg1), 1.5);
        FrameSemParse frame2 = new FrameSemParse("STOCK", 5, 5, "stocks.n", "stocks", Sets.newHashSet(arg2, arg3),
                1.3);
        FrameSemParse frame3 = new FrameSemParse("BUY", 5, 5, "purchases.v", "purchases", Sets.newHashSet(arg4,
                arg5), 2.0);
        FrameSemParse frame4 = new FrameSemParse("STOCK", 5, 5, "stocks.n", "stocks", Sets.newHashSet(arg2), 0.8);
        FrameSemParse frame5 = new FrameSemParse("BUY", 5, 5, "purchases.v", "purchases", Sets.newHashSet(arg4),
                1.0);

        FrameSemAnalysis fsp1 = new FrameSemAnalysis(Arrays.asList(frame1, frame2, frame3));
        FrameSemAnalysis fsp2 = new FrameSemAnalysis(Arrays.asList(frame1, frame4, frame3));
        FrameSemAnalysis fsp3 = new FrameSemAnalysis(Arrays.asList(frame1, frame2, frame5));
        Scored<FrameSemAnalysis> scored1 = new Scored<FrameSemAnalysis>(fsp1, new FsaScore(5,
                5, 5, 5), 20.0, 1);
        Scored<FrameSemAnalysis> scored2 = new Scored<FrameSemAnalysis>(fsp2, new FsaScore(4,
                5, 4, 5), 20.0, 2);
        Scored<FrameSemAnalysis> scored3 = new Scored<FrameSemAnalysis>(fsp3, new FsaScore(4,
                5, 4, 5), 20.0, 3);

        List<Scored<FrameSemAnalysis>> sortedParses = Lists.newArrayList();
        sortedParses.add(scored1);
        sortedParses.add(scored2);
        sortedParses.add(scored3);
        TrainInstance instance = new TrainInstance(tokens, posTags, sortedParses);
        FrameNetVocabs vocabs = new FrameNetVocabs(
                Sets.newHashSet(tokens), Sets.newHashSet(posTags),
                frameIds, argIds);
        trainData = new TrainData(Arrays.asList(instance), sortedParses.size());

        List<Scored<FrameSemAnalysis>> unsortedParses = Arrays.asList(scored2, scored3, scored1);
        TestInstance testInst = new TestInstance(tokens, posTags, unsortedParses);
        List<TestInstance> testInstances = Lists.newArrayList();
        testInstances.add(0, testInst);
        testData = new TestData(testInstances, sortedParses.size());

        devData = new TestData(testInstances, sortedParses.size());
        reranker = new FspRerankerApp(new AllRerankingData(trainData, testData, devData, vocabs),
                new PairwiseLoss());
    }

    @Test
    public void testDoDeepDecoding() {
        Map<Integer, Scored<FrameSemAnalysis>> result = reranker.doDeepDecoding(testData);
        for (int ex : result.keySet()) {
            System.err.println(ex + "\t" + result.get(ex).toString());
        }
    }

}
