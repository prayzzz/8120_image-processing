import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_Yellow_Horizontal implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Black Yellow Horizontal");
        gd.addNumericField("Line Width", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();

        int lineWidth = (int) gd.getNextNumber();
        int i = 0;
        for (int y = 0; y < h; y++)
        {
            int c = Math.floorDiv(y, lineWidth) % 2;
            int color = c == 0 ? 0 : (255 << 16) + (255 << 8);

            for (int x = 0; x < w; x++)
            {
                pixels[i++] = color;
            }
        }

        new ImagePlus("Black Yellow Horizontal", ip).show();
    }

}