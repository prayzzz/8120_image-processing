import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Vertical_Lines_ChainSaw implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Vertical Lines Chainsaw");
        gd.addNumericField("Width", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ByteProcessor(w, h);
        byte[] pixels = (byte[]) ip.getPixels();
        int i = 0;

        int lineWidth = (int) gd.getNextNumber();

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pixels[i++] = (byte) ((255 / lineWidth) * (x % lineWidth));
            }
        }

        new ImagePlus("Vertical Lines Chainsaw", ip).show();
    }
}
