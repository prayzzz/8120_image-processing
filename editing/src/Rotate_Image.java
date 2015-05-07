import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by Patrick on 07.05.2015.
 */
public class Rotate_Image implements PlugInFilter
{
    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        GenericDialog gd = new GenericDialog("Rotate Image");
        gd.addNumericField("Rotation", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        ip.rotateRight();
        ip.rotate((int) gd.getNextNumber());
    }
}
