package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
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
        Frame frame1 = new Frame("BANKER", 2, 2, "banker.n", "banker", Sets.newHashSet(arg1), 1.5);
        Frame frame2 = new Frame("STOCK", 5, 5, "stocks.n", "stocks", Sets.newHashSet(arg2, arg3),
                1.3);
        Frame frame3 = new Frame("BUY", 5, 5, "purchases.v", "purchases", Sets.newHashSet(arg4,
                arg5), 2.0);
        Frame frame4 = new Frame("STOCK", 5, 5, "stocks.n", "stocks", Sets.newHashSet(arg2), 0.8);
        Frame frame5 = new Frame("BUY", 5, 5, "purchases.v", "purchases", Sets.newHashSet(arg4),
                1.0);

        FrameSemanticParse fsp1 = new FrameSemanticParse(Arrays.asList(frame1, frame2, frame3));
        FrameSemanticParse fsp2 = new FrameSemanticParse(Arrays.asList(frame1, frame4, frame3));
        FrameSemanticParse fsp3 = new FrameSemanticParse(Arrays.asList(frame1, frame2, frame5));
        Scored<FrameSemanticParse> scored1 = new Scored<FrameSemanticParse>(fsp1, 5, 5, 5, 5, 20.0);
        Scored<FrameSemanticParse> scored2 = new Scored<FrameSemanticParse>(fsp2, 4, 5, 4, 5, 20.0);
        Scored<FrameSemanticParse> scored3 = new Scored<FrameSemanticParse>(fsp3, 4, 5, 4, 5, 20.0);

        TreeMultiset<Scored<FrameSemanticParse>> sortedParses = TreeMultiset.create();
        sortedParses.add(scored1);
        sortedParses.add(scored2);
        sortedParses.add(scored3);
        TrainInstance instance = new TrainInstance(tokens, posTags, sortedParses);
        FrameNetVocabs vocabs = new FrameNetVocabs(
                Sets.newHashSet(tokens), Sets.newHashSet(posTags), Sets.newHashSet("n", "v"),
                frameIds, argIds);
        trainData = new TrainData(Arrays.asList(instance));

        List<Scored<FrameSemanticParse>> unsortedParses = Arrays.asList(scored2, scored1, scored3);
        TestInstance testInst = new TestInstance(tokens, posTags, unsortedParses);
        Map<Integer, TestInstance> testInstances = Maps.newHashMap();
        testInstances.put(0, testInst);
        testData = new TestData(testInstances);

        devData = new TestData(testInstances);
        reranker = new FspRerankerApp(trainData, testData, devData, vocabs);
    }

    @Test
    public void testDoDeepDecoding() {
        Map<Integer, Integer> result = reranker.doDeepDecoding(testData);
        for (int ex : result.keySet()) {
            System.err.println(ex + "\t" + result.get(ex));
        }
    }

}
