import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 06.05.2015.
 */
public class Growing_Circles implements PlugIn
{    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Growing Circles");
        gd.addNumericField("Start Radius", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();

        int circleWidth = (int) gd.getNextNumber();
        int lastRadius = 0;
        int color = 255;

        circles:
        while (true)
        {
            int maxVal = lastRadius + circleWidth;
            for (int y = 0; y < maxVal && y < h; y++)
            {
                for (int x = 0; x < maxVal && x < w; x++)
                {
                    double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

                    if (r <= circleWidth + lastRadius && r > lastRadius)
                    {
                        int i = (y * w) + x;
                        pixels[i] = (byte) color;

                        if (x == (w - 1) && y == (h - 1))
                        {
                            break circles;
                        }
                    }
                }
            }

            color = 255 - color;
            lastRadius += circleWidth;
            circleWidth -= 1;

            if (circleWidth < 1)
            {
                circleWidth = 1;
            }
        }

        new ImagePlus("Growing Circles", ip).show();
    }
}