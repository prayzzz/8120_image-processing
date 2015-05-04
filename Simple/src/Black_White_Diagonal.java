import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_White_Diagonal implements PlugIn
{
    public void run(String arg)
    {
        long start = System.currentTimeMillis();
        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();

        int i = 0;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                Point p = rotateInverse(new Point(x, y));
                int c = Math.floorDiv(p.Y, 20) % 2;
                int color = c == 0 ? 0 : (255 << 16) + (255 << 8) + 255;

                pixels[i++] = color;
            }
        }

        new ImagePlus("Black White Diagonal", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }

    private Point rotateInverse(Point point)
    {
        int x = (int) Math.round(point.X * Math.cos(45) + point.Y * Math.sin(45));
        int y = (int) Math.round(point.Y * Math.cos(45) - point.X * Math.sin(45));

        return new Point(x, y);
    }

    private class Point
    {
        public int X;
        public int Y;

        public Point(int x, int y)
        {
            this.X = x;
            this.Y = y;
        }
    }
}
