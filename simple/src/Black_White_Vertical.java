import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_White_Vertical implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Black White Vertical");
        gd.addNumericField("Line Width", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ByteProcessor(w, h);
        byte[] pixels = (byte[]) ip.getPixels();

        int lineWidth = (int) gd.getNextNumber();

        int i = 0;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                byte color = Math.floorDiv(x, lineWidth) % 2 == 0 ? (byte) 0 : (byte) 255;
                pixels[i++] = color;
            }
        }

        new ImagePlus("Black White Vertical", ip).show();
    }
}
