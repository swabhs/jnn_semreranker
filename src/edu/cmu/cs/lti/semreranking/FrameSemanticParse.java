package edu.cmu.cs.lti.semreranking;

import java.util.List;

public class FrameSemanticParse {

    public List<Frame> frames;

    public int numFrames;
    public int numFrameArgs;

    public FrameSemanticParse(List<Frame> frames) {
        this.frames = frames;
        numFrames = frames.size();
        numFrameArgs = 0;
        for (Frame f : frames) {
            numFrameArgs += f.numArgs;
        }
    }

    public void print() {
        for (Frame frame : frames) {
            frame.print();
        }
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

}
