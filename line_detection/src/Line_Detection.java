import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.hough.HoughTransformLines;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 01.06.2015.
 */
public class Line_Detection implements PlugInFilter {
    static int MaxLines = 10;            // number of strongest lines to be found
    static int MinPointsOnLine = 50;    // min. number of points on each line

    static final float[] highlightHorizontalLinesFilter = {-1, -1, -1, 2, 2, 2, -1, -1, -1};
    static final float[] highlightVerticalLinesFilter = {-1, 2, -1, -1, 2, -1, -1, 2, -1};
    final int holeTolerance = 5;
    final int minimalLineLength = 20;
    final int surroundingPixelDistance = 2;

    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    public void run(ImageProcessor sourceImageProcessor) {
        long start = System.currentTimeMillis();
        ImagePlus varianceImage = new ImagePlus("Variance", sourceImageProcessor);
        ImagePlus sourceImage = new ImagePlus("Source", sourceImageProcessor.convertToColorProcessor());

        RankFilters rankFilter = new RankFilters();
        rankFilter.rank(sourceImageProcessor, 2.0, 2);
        rankFilter.rank(sourceImageProcessor, 5, 4);
        rankFilter.rank(sourceImageProcessor, 1, 3);

        Prefs.blackBackground = false;

        //Convert to Binary
        ImagePlus binaryImage = new ImagePlus("Binary", varianceImage.getProcessor());
        IJ.runPlugIn(binaryImage, "ij.plugin.Thresholder", "");

        // Skeletonize
        ImagePlus skeletonImage = new ImagePlus("Skeleton", binaryImage.getProcessor().convertToByteProcessor());
        IJ.runPlugIn(skeletonImage, "ij.plugin.filter.Binary", "skel");

        ImageProcessor verticalProcessor = skeletonImage.getProcessor().convertToByteProcessor();
        ImageProcessor horizontalProcessor = skeletonImage.getProcessor().convertToByteProcessor();

        Convolver convolver = new Convolver();
        convolver.convolve(horizontalProcessor, highlightHorizontalLinesFilter, 3, 3);
        convolver.convolve(verticalProcessor, highlightVerticalLinesFilter, 3, 3);

        HoughTransformLines.Parameters params = new HoughTransformLines.Parameters();
        params.nAng = 256;
        params.nRad = 128;

        HoughTransformLines ht = new HoughTransformLines(horizontalProcessor, params);
        HoughTransformLines.HoughLine[] lines = ht.getLines(MinPointsOnLine, MaxLines);

        ArrayList<Line> lineParts = new ArrayList<>();

        for (HoughTransformLines.HoughLine l : lines) {
            Line foundLine = getLine(l, horizontalProcessor.getWidth(), horizontalProcessor.getHeight());
            lineParts.addAll(processLine(foundLine, binaryImage.getProcessor()));
        }

        ht = new HoughTransformLines(verticalProcessor, params);
        lines = ht.getLines(MinPointsOnLine, MaxLines);

        for (HoughTransformLines.HoughLine l : lines) {
            Line foundLine = getLine(l, horizontalProcessor.getWidth(), horizontalProcessor.getHeight());
            lineParts.addAll(processLine(foundLine, binaryImage.getProcessor()));
        }

        ImageProcessor lineImageProcessor = new ColorProcessor(sourceImageProcessor.getWidth(), sourceImageProcessor.getHeight());
        ImagePlus lineImage = new ImagePlus("Lines", lineImageProcessor);
        for (Line l : lineParts) {
            lineImageProcessor.setColor(Color.RED);
            lineImageProcessor.drawLine(l.x1, l.y1, l.x2, l.y2);
        }

        long end = System.currentTimeMillis();

        IJ.log("Zeit: " + Long.toString(end-start) + " ms");

        lineImage.show();
        skeletonImage.show();
        binaryImage.show();
        Merge(sourceImage, lineImage).show();
    }

    private ArrayList<Line> processLine(Line foundLine, ImageProcessor processor)
    {
        ArrayList<Line> lineParts = new ArrayList<>();

        //y = m * x + n
        float m = ((float) foundLine.y2 - foundLine.y1) / (foundLine.x2 - foundLine.x1);
        float n = foundLine.y1 - m * foundLine.x1;

        double degree = Math.abs(Math.toDegrees(Math.atan(m)));

        // Vertical Line
        boolean vertical = degree > 45 && degree < 135;

        Line currentLinePart = null;
        int currentTolerance = holeTolerance;

        if (vertical)
        {
            for (int y = 0; y < processor.getHeight(); y++)
            {
                int x = (int) Math.round(Math.ceil((y - n) / m));

                if (processor.getPixel(x, y) == 255 ||
                        processor.getPixel(x + surroundingPixelDistance, y) == 255 ||
                        processor.getPixel(x - surroundingPixelDistance, y) == 255)
                {
                    currentTolerance = holeTolerance;

                    if (currentLinePart == null)
                    {
                        currentLinePart = new Line(x, y, -1, -1);
                    }
                }
                else
                {
                    currentTolerance--;
                }

                if (currentTolerance == 0 && currentLinePart != null)
                {
                    currentLinePart.x2 = x;
                    currentLinePart.y2 = y;
                    if (Math.sqrt(Math.pow(currentLinePart.x1 - currentLinePart.x2, 2) + Math.pow(currentLinePart.y1 - currentLinePart.y2, 2)) > minimalLineLength)
                    {
                        lineParts.add(currentLinePart);
                    }
                    currentLinePart = null;
                }
            }

            if (currentLinePart != null)
            {
                currentLinePart.x2 = (int) Math.round(Math.ceil((processor.getHeight() - n) / m));
                currentLinePart.y2 = processor.getHeight();
                if (Math.sqrt(Math.pow(currentLinePart.x1 - currentLinePart.x2, 2) + Math.pow(currentLinePart.y1 - currentLinePart.y2, 2)) > minimalLineLength)
                {
                    lineParts.add(currentLinePart);
                }
            }
        }
        else
        {
            for (int x = 0; x < processor.getWidth(); x++)
            {
                int y = (int) Math.round(Math.ceil(m * x + n));

                if (processor.getPixel(x, y) == 255 ||
                        processor.getPixel(x, y + surroundingPixelDistance) == 255 ||
                        processor.getPixel(x, y - surroundingPixelDistance) == 255)
                {
                    currentTolerance = holeTolerance;

                    if (currentLinePart == null)
                    {
                        currentLinePart = new Line(x, y, -1, -1);
                    }
                }
                else
                {
                    currentTolerance--;
                }

                if (currentTolerance == 0 && currentLinePart != null)
                {
                    currentLinePart.x2 = x;
                    currentLinePart.y2 = y;
                    if (Math.sqrt(Math.pow(currentLinePart.x1 - currentLinePart.x2, 2) + Math.pow(currentLinePart.y1 - currentLinePart.y2, 2)) > minimalLineLength)
                    {
                        lineParts.add(currentLinePart);
                    }
                    currentLinePart = null;
                }
            }

            if (currentLinePart != null)
            {
                currentLinePart.x2 = processor.getWidth();
                currentLinePart.y2 = (int) Math.round(Math.ceil(m * processor.getWidth() + n));
                if (Math.sqrt(Math.pow(currentLinePart.x1 - currentLinePart.x2, 2) + Math.pow(currentLinePart.y1 - currentLinePart.y2, 2)) > minimalLineLength)
                {
                    lineParts.add(currentLinePart);
                }
            }
        }

        return lineParts;
    }

    private Line getLine(HoughTransformLines.HoughLine line, int imageWidth, int imageHeight) {
        final double dmax = 0.5;

        int startX, startY, endX, endY;
        startX = startY = endX = endY = -1;

        for (int x = 0; x < imageWidth; x++) {
            double d = Math.abs(line.getDistance(x, 0));
            if (d <= dmax) {
                if (startX == -1) {
                    startX = x;
                    startY = 0;
                } else {
                    endX = x;
                    endY = 0;
                    break;
                }
            }

            d = Math.abs(line.getDistance(x, imageHeight - 1));
            if (d <= dmax) {
                if (startX == -1) {
                    startX = x;
                    startY = imageHeight - 1;
                } else {
                    endX = x;
                    endY = imageHeight - 1;
                    break;
                }
            }
        }

        if (endX != -1) {
            return new Line(startX, startY, endX, endY);
        }

        for (int y = 0; y < imageHeight; y++) {
            double d = Math.abs(line.getDistance(0, y));
            if (d <= dmax) {
                if (startX == -1) {
                    startX = 0;
                    startY = y;
                } else {
                    endX = 0;
                    endY = y;
                    break;
                }
            }

            d = Math.abs(line.getDistance(imageWidth - 1, y));
            if (d <= dmax) {
                if (startX == -1) {
                    startX = imageWidth - 1;
                    startY = y;
                } else {
                    endX = imageWidth - 1;
                    endY = y;
                    break;
                }
            }
        }

        return new Line(startX, startY, endX, endY);
    }

    private List<Point> drawLine(int x1, int y1, int x2, int y2) {
        ArrayList<Point> points = new ArrayList<>();

        // delta of exact value and rounded value of the dependant variable
        int d = 0;

        int dy = Math.abs(y2 - y1);
        int dx = Math.abs(x2 - x1);

        int dy2 = (dy << 1); // slope scaling factors to avoid floating
        int dx2 = (dx << 1); // point

        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        if (dy <= dx) {
            for (; ; ) {
                points.add(new Point(x1, y1));
                if (x1 == x2)
                    break;
                x1 += ix;
                d += dy2;
                if (d > dx) {
                    y1 += iy;
                    d -= dx2;
                }
            }
        } else {
            for (; ; ) {
                points.add(new Point(x1, y1));
                if (y1 == y2)
                    break;
                y1 += iy;
                d += dx2;
                if (d > dy) {
                    x1 += ix;
                    d -= dy2;
                }
            }
        }

        return points;
    }

    private ImagePlus Merge(ImagePlus image1, ImagePlus image2) {
        ImageRoi roi = new ImageRoi(0,0,image2.getBufferedImage());
        roi.setZeroTransparent(true);

        Overlay overlay = new Overlay(roi);
        image1.setOverlay(overlay);

        return image1.flatten();
    }
}