package edu.cmu.cs.lti.semreranking.utils;


public class CodeGrave {

    // public static void writeSortedInstancesToFile(
    // Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances) {
    //
    // DataPaths dataPaths = new DataPaths(true, true, SemRerankerMain.model);
    //
    // int i = 0;
    // try {
    // PrintWriter outFe;
    // PrintWriter outXml;
    // for (int ex : sortedTrainInstances.keySet()) {
    // int rank = 0;
    // for (Scored<FrameSemanticParse> instance : sortedTrainInstances.get(ex)) {
    // String outFeFileName = dataPaths.feDir + rank + DataPaths.FE_FILE_EXTN;
    // String outXmlFileName = dataPaths.xmlDir + rank + DataPaths.XML_FILE_EXTN;
    //
    // if (i == 0) {
    // outFe = new PrintWriter(
    // new BufferedWriter(new FileWriter(outFeFileName, false)));
    //
    // outXml = new PrintWriter(
    // new BufferedWriter(new FileWriter(outXmlFileName, false)));
    // outXml.println("Sentence ID\tSORTED Recall\tSORTED Precision\tSORTED Fscore");
    // } else {
    // outFe = new PrintWriter(
    // new BufferedWriter(new FileWriter(outFeFileName, true)));
    //
    // outXml = new PrintWriter(
    // new BufferedWriter(new FileWriter(outXmlFileName, true)));
    // }
    //
    // outFe.println(instance.entity.toString(ex));
    //
    // outXml.println(ex + "\t"
    // + SemRerankerMain.formatter.format(instance.rNum / instance.rDenom)
    // + "("
    // + (instance.rNum) + "/"
    // + (instance.rDenom) + ")\t"
    // + SemRerankerMain.formatter.format(instance.pNum / instance.pDenom)
    // + "("
    // + (instance.pNum) + "/"
    // + (instance.pDenom) + ")\t"
    // + instance.fscore);
    // outFe.close();
    // outXml.close();
    // rank++;
    // }
    // i++;
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    //
    // }
    //
    // public void sortTrainingInsts() {
    // DataPaths dataPaths = new DataPaths(false, false, SemRerankerMain.model);
    // Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances = FileUtils
    // .readAndSortTrain(dataPaths.xmlDir, dataPaths.feDir, new FeReader());
    // CodeGrave.writeSortedInstancesToFile(sortedTrainInstances);
    // }
}
