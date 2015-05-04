import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Vertical_Lines_Sinus implements PlugIn
{
    public void run(String arg)
    {
        long start = System.currentTimeMillis();
        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();
        int i = 0;

        double widthSplit = (2 * Math.PI) / w;

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int intensity = (int) Math.round((Math.sin(widthSplit * x) + 1) * (255 / 2));

                int color = (intensity << 16) + (intensity << 8) + intensity;
                pixels[i++] = color;
            }
        }

        new ImagePlus("Vertical Lines Sinus", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }
}