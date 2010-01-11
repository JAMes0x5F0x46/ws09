/*
 * Created on Apr 23, 2009 Version: $Id: AdaptiveStdErrProgressWriter.java 2874 2009-12-11 16:03:27Z frank $
 */

package at.tuwien.ifs.somtoolbox.util;

import java.util.Date;
import java.util.LinkedList;

/**
 * @author Jakob Frank
 * @version $Id: AdaptiveStdErrProgressWriter.java 2874 2009-12-11 16:03:27Z frank $
 */
public class AdaptiveStdErrProgressWriter extends StdErrProgressWriter {

    private LinkedList<Long> backtraceList = new LinkedList<Long>();

    private int backtraceSteps = 10;

    /**
     * @param totalSteps
     * @param message
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message) {
        super(totalSteps, message);
    }

    /**
     * @param totalSteps
     * @param message
     * @param stepWidth
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message, int stepWidth) {
        super(totalSteps, message, stepWidth);
        backtraceList.add(startDate);
        setBacktraceWindow(.1);
    }

    /**
     * @param totalSteps
     * @param message
     * @param stepWidth
     * @param newLineWidth
     */
    public AdaptiveStdErrProgressWriter(int totalSteps, String message, int stepWidth, int newLineWidth) {
        super(totalSteps, message, stepWidth, newLineWidth);
    }

    @Override
    public void progress(int currentStep) {
        if (finished)
            return;
        if (currentStep == 1 || currentStep == totalSteps || currentStep % stepWidth == 0) {
            if (backtraceList.size() == 0) {
                backtraceList.add(startDate);
            }
            this.currentStep = currentStep;
            long currentDate = System.currentTimeMillis();

            long backtraceDur = (currentDate - backtraceList.getFirst()) / backtraceList.size();
            backtraceList.addLast(currentDate);

            int stepsToGo = totalSteps - currentStep;
            long estimatedEndDate = currentDate + stepsToGo * backtraceDur;

            StringBuffer log = new StringBuffer(messageLength + 30);
            log.append("\r").append(message).append(currentStep).append(" of ").append(totalSteps).append(", ETA: ").append(
                    format.format(new Date(estimatedEndDate))).append(", ").append(DateUtils.shortFormatDuration(estimatedEndDate - currentDate)).append(
                    " remaining.").append(" ");
            System.err.print(log);

            if (newLineWidth != 0 && currentStep % newLineWidth == 0) {
                System.err.println();
            }
            if (currentStep == totalSteps) {
                System.err.println("\n\t --> Finished, took " + DateUtils.formatDuration(currentDate - startDate));
                finished = true;
            }
        }

        // Trunk the list
        while (backtraceList.size() > backtraceSteps) {
            backtraceList.removeFirst();
        }
    }

    public void setBacktraceWindow(int steps) {
        backtraceSteps = steps;
    }
    
    public void setBacktraceWindow(double window) {
        backtraceSteps = (int) ((totalSteps / stepWidth) * window);
        if (backtraceSteps < 10) {
            backtraceSteps = 10;
        }
    }
}
