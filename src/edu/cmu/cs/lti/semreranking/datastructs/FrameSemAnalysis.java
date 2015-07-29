package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.List;

/**
 * Frame-semantic analysis for a sentence. May contain more than one frame evoked by different
 * targets in the sentence.
 * 
 * @author sswayamd
 *
 */
public class FrameSemAnalysis {

    public List<FrameSemParse> frameSemParses;

    public int numFsps;
    public int numFrameArgs;

    public double semaforScore;

    public FrameSemAnalysis(List<FrameSemParse> frames) {
        this.frameSemParses = frames;
        numFsps = frames.size();
        numFrameArgs = 0;
        semaforScore = 0.0;
        for (FrameSemParse f : frames) {
            numFrameArgs += f.numArgs;
            semaforScore += f.score;
        }
        semaforScore /= numFsps;
    }

    public String toString(int exNum) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (FrameSemParse frameSemParse : frameSemParses) {
            builder.append(frameSemParse.toString(exNum));
            i++;
            if (i != frameSemParses.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public void print() {
        for (FrameSemParse fsp : frameSemParses) {
            fsp.print();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FrameSemAnalysis other = (FrameSemAnalysis) obj;
        if (frameSemParses == null) {
            if (other.frameSemParses != null)
                return false;
        } else if (!frameSemParses.equals(other.frameSemParses))
            return false;
        if (numFrameArgs != other.numFrameArgs)
            return false;
        if (numFsps != other.numFsps)
            return false;

        return true;
    }

}
