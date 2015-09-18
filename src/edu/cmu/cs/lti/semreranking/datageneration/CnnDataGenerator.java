package edu.cmu.cs.lti.semreranking.datageneration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.Data;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.datastructs.SemevalScore;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader.AllRerankingData;
import edu.cmu.cs.lti.semreranking.utils.FrequencySet;

public class CnnDataGenerator {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    public static boolean useMini = false;

    @Parameter(names = "-ranks", description = "number of ranks")
    public static int numRanks = 50;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    private FrequencySet labelSet = new FrequencySet(0);

    private final String NOT_AN_ARG = "*";
    private final String PRED = "PRED";

    public static void main(String[] args) {
        new JCommander(new CnnDataGenerator(), args);

        AllRerankingData data = DataFilesReader.readAllRerankingData(useMini);
        CnnDataGenerator gen = new CnnDataGenerator();
        gen.prepareTagListForCnn(data.trainData);
        gen.prepareTagListForCnn(data.devData);
        gen.labelSet.freeze();
        gen.writeCnnForCorpus(data.trainData, "corpus.fsp");
        gen.writeCnnForCorpus(data.devData, "dev.fsp");
    }

    public void prepareTagListForCnn(Data data) {
        for (int ex = 0; ex < data.tokens.size(); ex++) {
            for (FrameIdentifier id : data.getFramesInEx(ex)) {
                int numRanks = data.getInstance(ex, id).numUniqueParses;
                for (int rank = 0; rank < numRanks; rank++) {
                    Scored<FrameSemParse> parse = data.getInstance(ex, id).getParseAtRank(rank);
                    addLabelsForParseToSet(parse);
                }
            }
        }
    }

    public void writeCnnForCorpus(Data data, String cnnFileName) {
        List<String> lines = Lists.newArrayList();
        for (int ex = 0; ex < data.tokens.size(); ex++) {
            String[] tokens = data.tokens.get(ex);
            for (FrameIdentifier id : data.getFramesInEx(ex)) {
                StringBuilder builder = new StringBuilder();
                joinArrayStrings(builder, tokens, true);
                int numRanks = data.getInstance(ex, id).numUniqueParses;
                for (int rank = 0; rank < numRanks; rank++) {
                    Scored<FrameSemParse> scoredParse = data.getInstance(ex, id).getParseAtRank(
                            rank);
                    String[] labels = generateLabelsForParse(scoredParse, tokens.length);
                    if (rank == numRanks - 1) {
                        joinArrayStrings(builder, labels, false);
                        joinScoreStrings(builder, scoredParse.semevalScore, true);
                    } else {
                        joinArrayStrings(builder, labels, false);
                        joinScoreStrings(builder, scoredParse.semevalScore, false);
                    }
                }
                lines.add(builder.toString());
            }
        }
        BasicFileWriter.writeStrings(lines, cnnFileName);
    }

    private void addLabelsForParseToSet(Scored<FrameSemParse> scoredParse) {
        labelSet.addKeyIfNotFrozen(NOT_AN_ARG); // scoredParse.entity.id + NOT_AN_ARG);
        labelSet.addKeyIfNotFrozen(PRED); // scoredParse.entity.id + PRED

        for (Argument arg : scoredParse.entity.arguments) {
            if (arg.start == -1) { // frame with no arguments
                continue;
            }
            labelSet.addKeyIfNotFrozen(arg.id); // scoredParse.entity.id + "_" + arg.id
        }
    }

    private String[] generateLabelsForParse(Scored<FrameSemParse> scoredParse, int sz) {
        String[] labels = new String[sz];
        // default case -- token is not an argument for the frame
        for (int pos = 0; pos < sz; pos++) {
            labels[pos] = labelSet.returnKeyAfterFreezing(NOT_AN_ARG); // scoredParse.entity.id +
                                                                       // NOT_AN_ARG
        }

        // predicate tokens
        for (int pos = scoredParse.entity.predStartPos; pos <= scoredParse.entity.predEndPos; pos++) {
            labels[pos] = labelSet.returnKeyAfterFreezing(PRED);// scoredParse.entity.id + PRED
        }

        // argument tokens
        for (Argument arg : scoredParse.entity.arguments) {
            if (arg.start == -1) { // frame with no arguments
                continue;
            }
            for (int pos = arg.start; pos <= arg.end; pos++) {
                labels[pos] = labelSet.returnKeyAfterFreezing(arg.id);// scoredParse.entity.id + "_"
                                                                      // + arg.id
            }
        }

        return labels;
    }

    public void joinArrayStrings(
            StringBuilder builder,
            String[] labels,
            boolean isFirst) {
        builder.append("<s>\t");
        for (String ele : labels) {
            builder.append(ele);
            builder.append("\t");
        }
        builder.append("<\\s>\t");
        if (isFirst) {
            builder.append("||||\t");
        }
    }

    public void joinScoreStrings(
            StringBuilder builder,
            SemevalScore score,
            boolean isLast) {
        builder.append("||\t");
        builder.append(score.pnum);
        builder.append("\t");
        builder.append(score.pdenom);
        builder.append("\t");
        builder.append(score.rnum);
        builder.append("\t");
        builder.append(score.rdenom);
        if (!isLast) {
            builder.append("\t|||\t");
        }
    }
}
