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
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader.AllRerankingData;

public class CnnDataGenerator {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semhome = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    @Parameter(names = "-trainsz", description = "trainSize")
    public static int trainSize = 100;
    @Parameter(names = "-devsz", description = "devSize")
    public static int devSize = 25;
    @Parameter(names = "-testsz", description = "testSize")
    public static int testSize = 30;
    @Parameter(names = "-ranks", description = "number of ranks")
    public static int numRanks = 50;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public static void main(String[] args) {
        new JCommander(new MiniDataGenerator(), args);

        AllRerankingData data = DataFilesReader.readAllRerankingData(true);
        writeCnnForCorpus(data.trainData, "corpus.fsp");
        writeCnnForCorpus(data.devData, "dev.fsp");
    }

    public static void writeCnnForCorpus(Data data, String cnnFileName) {
        List<String> lines = Lists.newArrayList();
        for (int ex = 0; ex < data.tokens.size(); ex++) {
            String[] tokens = data.tokens.get(ex);
            for (FrameIdentifier id : data.getFramesInEx(ex)) {
                StringBuilder builder = new StringBuilder();
                joinArrayStrings(builder, tokens, false);
                int numRanks = data.getInstance(ex, id).numUniqueParses;
                for (int rank = 0; rank < numRanks; rank++) {
                    Scored<FrameSemParse> scoredParse = data.getInstance(ex, id).getParseAtRank(
                            rank);
                    String[] labels = generateLabelsForParse(scoredParse, tokens.length);
                    if (rank == numRanks - 1) {
                        joinArrayStrings(builder, labels, true);
                    } else {
                        joinArrayStrings(builder, labels, false);
                    }
                }
                lines.add(builder.toString());
            }
        }
        BasicFileWriter.writeStrings(lines, cnnFileName);
    }

    public static String[] generateLabelsForParse(Scored<FrameSemParse> scoredParse, int sz) {
        String[] labels = new String[sz];
        for (int pos = 0; pos < sz; pos++) {
            labels[pos] = scoredParse.entity.id + "_none";
        }
        for (int pos = scoredParse.entity.predStartPos; pos < scoredParse.entity.predEndPos; pos++) {
            labels[pos] = scoredParse.entity.id + "_pred";
        }
        for (Argument arg : scoredParse.entity.arguments) {
            if (arg.start == -1) { // frame with no arguments
                continue;
            }
            for (int pos = arg.start; pos <= arg.end; pos++) {
                labels[pos] = scoredParse.entity.id + "_" + arg.id;
            }
        }
        return labels;
    }

    public static void joinArrayStrings(StringBuilder builder, String[] labels, boolean isLast) {
        builder.append("<s>\t");
        for (String ele : labels) {
            builder.append(ele);
            builder.append("\t");
        }
        builder.append("<\\s>");
        if (isLast == false) {
            builder.append("\t|||\t");
        }
    }
}
