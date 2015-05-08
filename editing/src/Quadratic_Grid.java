import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.*;

/**
 * Created by prayzzz on 08.05.2015.
 */
public class Quadratic_Grid implements PlugInFilter
{
    private ImagePlus imagePlus;

    public int setup(String s, ImagePlus imagePlus)
    {
        this.imagePlus = imagePlus;
        return DOES_8G;
    }

    public void run(ImageProcessor ip)
    {
        GenericDialog gd = new GenericDialog("Quadratic Grid");
        gd.addNumericField("Size", 20, 0);
        gd.showDialog();

        if (gd.wasCanceled())
        {
            return;
        }

        ColorProcessor colorProcessor = ip.convertToColorProcessor();
        colorProcessor.setColor(Color.red);

        int h = ip.getHeight();
        int w = ip.getWidth();

        int size = (int) gd.getNextNumber();

        for (int i = size; i <= w; i += size)
        {
            colorProcessor.drawLine(i, 0, i, h);
            colorProcessor.drawLine(0, i, w, i);
        }

        imagePlus.setProcessor(colorProcessor);
    }
}
