import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.measure.Measurements;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Patrick on 16.06.2015.
 */
public class Line_Detection2 implements PlugInFilter
{
    private int ScanLineSpacing = 10;
    private int playGroundHeightPercent = 50;
    private final int MaxColorDistance = 30;
    private final int BrightDistance = 50;
    private boolean showIntermediateSteps;

    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor sourceImageProcessor)
    {
        GenericDialog gd = new GenericDialog("Line Detection Settings");
        gd.addCheckbox("Maximum Filter", true);
        gd.addCheckbox("Median Filter", true);
        gd.addNumericField("Scanline Distance", 10.0, 0);
        gd.addCheckbox("Use Field Detection", true);
        gd.addNumericField("Estimated Field Height %", 50.0, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        // Read Dialog
        boolean maximum = gd.getNextBoolean();
        boolean median = gd.getNextBoolean();
        ScanLineSpacing = (int) Math.round(gd.getNextNumber());
        boolean fieldDetection = gd.getNextBoolean();
        playGroundHeightPercent = (int) Math.round(gd.getNextNumber());

        long start = System.currentTimeMillis();
        ImagePlus medianImage = new ImagePlus("Median", sourceImageProcessor.convertToColorProcessor());
        ImageProcessor medianImageProcessor = medianImage.getProcessor();

        RankFilters rankFilter = new RankFilters();
        if (maximum)
        {
            rankFilter.rank(medianImageProcessor, 2.0, RankFilters.MAX);
        }

        if (median)
        {
            rankFilter.rank(medianImageProcessor, 5.0, RankFilters.MEDIAN);
        }

        ArrayList<Line> foundLines = nonMaskedLineDetection(sourceImageProcessor, ChannelSplitter.split(medianImage)[1], medianImageProcessor, rankFilter);
        ArrayList<Line> filteredLines = new ArrayList<>();
        if (fieldDetection)
        {
            ImagePlus mask = new Field_Detection().detectField(sourceImageProcessor);
            ImageProcessor maskProcessor = mask.getProcessor();

            Binary binaryPlugin = new Binary();
            binaryPlugin.setup("fill", null);
            binaryPlugin.run(maskProcessor);

            binaryPlugin.setup("close", null);
            binaryPlugin.run(maskProcessor);
            if (showIntermediateSteps)
                mask.setTitle("Field Detection");
            mask.show();

            for (Line line : foundLines)
            {
                if (maskProcessor.getPixel(line.x1, line.y1) == 0 || maskProcessor.getPixel(line.x2, line.y2) == 0 || line.getLength() < 3.0)
                {
                    continue;
                }

                filteredLines.add(line);
            }

        }
        else
        {
            foundLines.stream().filter(x -> x.getLength() > 3.0).forEach(x -> filteredLines.add(x));
        }

        long end = System.currentTimeMillis();
        IJ.log("Time: " + Long.toString(end - start) + " ms");

        IJ.log("Number of lines: " + filteredLines.size());
        ImagePlus lineImage = new ImagePlus("Lines", sourceImageProcessor.convertToColorProcessor());
        ImageProcessor lineImageProcessor = lineImage.getProcessor();

        try
        {
            FileWriter file = new FileWriter(new File("result.txt"));
            BufferedWriter bufferWriter = new BufferedWriter(file);

            for (Line l : filteredLines)
            {
                lineImageProcessor.setColor(Color.RED);
                lineImageProcessor.drawLine(l.x1, l.y1, l.x2, l.y2);

                bufferWriter.write(String.format("%d,%d;%d,%d\n", l.x1, l.y1, l.x2, l.y2));
            }

            bufferWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        lineImage.show();
    }

    private ArrayList<Line> nonMaskedLineDetection(ImageProcessor sourceImageProcessor, ImagePlus greenChannelImage, ImageProcessor medianImageProcessor, RankFilters rankFilter)
    {
        ImagePlus lineDectectionImage = new ImagePlus("LineDectectionImage", medianImageProcessor.convertToColorProcessor());

        //Apply variance
        rankFilter.rank(lineDectectionImage.getProcessor(), 1, 3);

        Prefs.blackBackground = false;

        //Convert to Binary
        IJ.runPlugIn(lineDectectionImage, "ij.plugin.Thresholder", "");

        // Skeletonize
        IJ.runPlugIn(lineDectectionImage, "ij.plugin.filter.Binary", "skel");
        lineDectectionImage.getProcessor().invert();

        greenChannelImage.setRoi(0, sourceImageProcessor.getHeight() - (int) Math.round(sourceImageProcessor.getHeight() * (playGroundHeightPercent / 100.0)), sourceImageProcessor.getWidth(), (int) Math.round(sourceImageProcessor.getHeight() * (playGroundHeightPercent / 100.0)));
        double greenColor = greenChannelImage.getStatistics(Measurements.MEDIAN).median;

        ImagePlus highlightedLineImage = Merge(greenChannelImage, lineDectectionImage);
        highlightedLineImage = new ImagePlus("Highlighted Lines", highlightedLineImage.getProcessor().convertToByteProcessor());

        return DetectPossibleLines(highlightedLineImage.getProcessor(), (int) Math.round(greenColor));
    }

    private ArrayList<Line> DetectPossibleLines(ImageProcessor ip, int greenColor)
    {
        int scanLineStart = ScanLineSpacing / 2;
        ArrayList<Line> foundLines = new ArrayList<>();

        // Vertical Search
        for (int x = scanLineStart; x < ip.getWidth(); x += ScanLineSpacing)
        {
            boolean greenMode = false;
            boolean lineMode = false;
            boolean lineFinished = false;
            Point lineEntryPoint = null;

            for (int y = 0; y < ip.getHeight(); y++)
            {
                int px = ip.getPixel(x, y);

                if (px >= greenColor - MaxColorDistance && px <= greenColor + MaxColorDistance)
                {
                    if (lineFinished)
                    {
                        Line line = new Line(lineEntryPoint.getX(), lineEntryPoint.getY() + 2, x, y - 2);
                        foundLines.add(line);
                    }

                    lineEntryPoint = null;
                    lineFinished = false;
                    lineMode = false;
                    greenMode = true;
                    continue;
                }

                if (px == 255 && greenMode)
                {
                    if (lineMode)
                    {
                        lineFinished = true;
                        continue;
                    }

                    lineMode = true;
                    lineEntryPoint = new Point(x, y);
                    continue;
                }

                if (greenMode && lineMode && px > greenColor + MaxColorDistance && px < 255)
                {
                    continue;
                }

                lineEntryPoint = null;
                greenMode = false;
                lineMode = false;
                lineFinished = false;
            }
        }

        // Horizontal Search
        for (int y = scanLineStart; y < ip.getHeight(); y += ScanLineSpacing)
        {
            boolean greenMode = false;
            boolean lineMode = false;
            boolean lineFinished = false;
            Point lineEntryPoint = null;

            for (int x = 0; x < ip.getWidth(); x++)
            {
                int px = ip.getPixel(x, y);

                if (px >= greenColor - MaxColorDistance && px <= greenColor + MaxColorDistance)
                {
                    if (lineFinished)
                    {
                        Line line = new Line(lineEntryPoint.getX() + 2, lineEntryPoint.getY(), x - 2, y);
                        foundLines.add(line);
                    }

                    lineEntryPoint = null;
                    lineFinished = false;
                    lineMode = false;
                    greenMode = true;
                    continue;
                }

                if (px == 255 && greenMode)
                {
                    if (lineMode)
                    {
                        lineFinished = true;
                        continue;
                    }

                    lineMode = true;
                    lineEntryPoint = new Point(x, y);
                    continue;
                }

                if (greenMode && lineMode && px > greenColor + MaxColorDistance && px < 255)
                {
                    continue;
                }

                lineEntryPoint = null;
                greenMode = false;
                lineMode = false;
                lineFinished = false;
            }
        }

        return foundLines;
    }

    private Color getMedian(ImageProcessor ip)
    {
        return new Color(40, 84, 57);
    }

    private ImagePlus Merge(ImagePlus image1, ImagePlus image2)
    {
        ImageRoi roi = new ImageRoi(0, 0, image2.getBufferedImage());
        roi.setZeroTransparent(true);

        Overlay overlay = new Overlay(roi);
        image1.setOverlay(overlay);

        return image1.flatten();
    }
}
