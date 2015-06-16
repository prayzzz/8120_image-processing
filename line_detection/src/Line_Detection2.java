import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.measure.Measurements;
import ij.plugin.ChannelSplitter;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Patrick on 16.06.2015.
 */
public class Line_Detection2 implements PlugInFilter
{
    private final int ScanLineSpacing = 10;
    private final int MaxColorDistance = 30;
    private final int BrightDistance = 50;

    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor sourceImageProcessor)
    {
        ImagePlus medianImage = new ImagePlus("Median", sourceImageProcessor.convertToColorProcessor());
        ImageProcessor medianImageProcessor = medianImage.getProcessor();

        RankFilters rankFilter = new RankFilters();
        rankFilter.rank(medianImageProcessor, 2.0, 2);
        rankFilter.rank(medianImageProcessor, 5.0, 4);

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

        ImagePlus greenChannelImage = ChannelSplitter.split(medianImage)[1];
        greenChannelImage.setRoi(0, sourceImageProcessor.getHeight() / 2, sourceImageProcessor.getWidth(), sourceImageProcessor.getHeight() / 2);
        double greenColor = greenChannelImage.getStatistics(Measurements.MEDIAN).median;

        IJ.log("Image Median (Green)");
        IJ.log(Double.toString(greenColor));

        ImagePlus highlightedLineImage = Merge(greenChannelImage, skeletonImage);
        highlightedLineImage = new ImagePlus("Highlighted Lines", highlightedLineImage.getProcessor().convertToByteProcessor());

        ArrayList<Line> foundLines = DetectPossibleLines(highlightedLineImage.getProcessor(), (int) Math.round(greenColor));

        IJ.log("Found Lines:");
        ImagePlus lineImage = new ImagePlus("Lines", sourceImageProcessor.convertToColorProcessor());
        ImageProcessor lineImageProcessor = lineImage.getProcessor();
        for (Line l : foundLines)
        {
            lineImageProcessor.setColor(Color.RED);
            lineImageProcessor.drawLine(l.x1, l.y1, l.x2, l.y2);
        }

        lineImage.show();
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
