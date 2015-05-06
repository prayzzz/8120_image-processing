import ij.ImagePlus;
import ij.gui.GenericDialog;
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
        GenericDialog gd = new GenericDialog("Vertical Lines Sinus");
        gd.addNumericField("Width", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();
        int i = 0;

        int lineWidth = (int) gd.getNextNumber();

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                double sin = Math.sin(2 * Math.PI * (x % lineWidth) / lineWidth);
                int intensity = (int) Math.round((sin + 1) * (255 / 2));

                int color = (intensity << 16) + (intensity << 8) + intensity;
                pixels[i++] = color;
            }
        }

        new ImagePlus("Vertical Lines Sinus", ip).show();
    }
}