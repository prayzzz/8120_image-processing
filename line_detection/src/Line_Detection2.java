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
        gd.addCheckbox("Show Intermediate Steps", true);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        boolean maximum = gd.getNextBoolean();
        boolean median = gd.getNextBoolean();

        ScanLineSpacing = (int) Math.round(gd.getNextNumber());

        boolean fieldDetction = gd.getNextBoolean();

        playGroundHeightPercent = (int) Math.round(gd.getNextNumber());
        
        showIntermediateSteps = gd.getNextBoolean();

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
        if (fieldDetction)
        {
            ImagePlus mask = new Field_Detection().detectField(sourceImageProcessor);
            ImageProcessor maskProcessor = mask.getProcessor();

            Binary binaryPlugin = new Binary();
            binaryPlugin.setup("fill", null);
            binaryPlugin.run(maskProcessor);

            binaryPlugin.setup("close", null);
            binaryPlugin.run(maskProcessor);
            if(showIntermediateSteps)
                mask.setTitle("Field Detection");
                mask.show();

            for (Line line : foundLines)
            {
                IJ.log(Integer.toString(maskProcessor.getPixel(line.x1, line.y1)));
                if (maskProcessor.getPixel(line.x1, line.y1) == 0 || maskProcessor.getPixel(line.x2, line.y2) == 0 || line.getLength() < 3.0)
                {
                    continue;
                }

                filteredLines.add(line);
            }

        }
        else
        {
            for (Line line : foundLines)
            {
                if (line.getLength() < 3.0)
                {
                    continue;
                }
                filteredLines.add(line);
            }
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
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        lineImage.show();
    }

    private ArrayList<Line> nonMaskedLineDetection(ImageProcessor sourceImageProcessor, ImagePlus imagePlus, ImageProcessor medianImageProcessor, RankFilters rankFilter)
    {
        ImagePlus varianceImage = new ImagePlus("VarianceImage", medianImageProcessor.convertToColorProcessor());
        rankFilter.rank(varianceImage.getProcessor(), 1, 3);

        Prefs.blackBackground = false;

        //Convert to Binary
        ImagePlus binaryImage = new ImagePlus("Binary", varianceImage.getProcessor().convertToColorProcessor());
        IJ.runPlugIn(binaryImage, "ij.plugin.Thresholder", "");

        // Skeletonize
        ImagePlus skeletonImage = new ImagePlus("Skeleton", binaryImage.getProcessor().convertToByteProcessor());
        IJ.runPlugIn(skeletonImage, "ij.plugin.filter.Binary", "skel");
        skeletonImage.getProcessor().invert();

        ImagePlus greenChannelImage = imagePlus;
        greenChannelImage.setRoi(0, sourceImageProcessor.getHeight() - (int) Math.round(sourceImageProcessor.getHeight() * (playGroundHeightPercent / 100.0)), sourceImageProcessor.getWidth(), (int) Math.round(sourceImageProcessor.getHeight() * (playGroundHeightPercent / 100.0)));
        double greenColor = greenChannelImage.getStatistics(Measurements.MEDIAN).median;

        IJ.log("Image Median (Green)" + greenColor);

        ImagePlus highlightedLineImage = Merge(greenChannelImage, skeletonImage);
        highlightedLineImage = new ImagePlus("Highlighted Lines", highlightedLineImage.getProcessor().convertToByteProcessor());
        if(showIntermediateSteps)
            highlightedLineImage.show();

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
                        IJ.log(String.format("Finished line at %d, %d", x, y));
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
