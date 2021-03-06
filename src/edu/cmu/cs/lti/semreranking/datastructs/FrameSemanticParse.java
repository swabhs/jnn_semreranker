package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.List;

public class FrameSemanticParse {

    public List<Frame> frames;

    public int numFrames;
    public int numFrameArgs;

    public double semaforScore;

    public FrameSemanticParse(List<Frame> frames) {
        this.frames = frames;
        numFrames = frames.size();
        numFrameArgs = 0;
        semaforScore = 0.0;
        for (Frame f : frames) {
            numFrameArgs += f.numArgs;
            semaforScore += f.score;
        }
        semaforScore /= numFrames;
    }

    public String toString(int exNum) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Frame frame : frames) {
            builder.append(frame.toString(exNum));
            i++;
            if (i != frames.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public void print() {
        for (Frame frame : frames) {
            frame.print();
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
        FrameSemanticParse other = (FrameSemanticParse) obj;
        if (frames == null) {
            if (other.frames != null)
                return false;
        } else if (!frames.equals(other.frames))
            return false;
        if (numFrameArgs != other.numFrameArgs)
            return false;
        if (numFrames != other.numFrames)
            return false;

        return true;
    }

}
