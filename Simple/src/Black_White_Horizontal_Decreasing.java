import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_White_Horizontal_Decreasing implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Black White Horizontal Decreasing");
        gd.addNumericField("Start Height", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();

        int currentHeight = (int) gd.getNextNumber();
        int doneHeight = 0;
        int color = 0;

        int i = 0;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pixels[i++] = (byte) color;
            }

            if (y == doneHeight + currentHeight)
            {
                color = 255 - color;
                doneHeight += currentHeight;
                currentHeight--;

                if (currentHeight < 1)
                {
                    currentHeight = 1;
                }
            }
        }

        new ImagePlus("Black White Horizontal Decreasing", ip).show();
    }
}