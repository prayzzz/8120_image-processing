import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by Patrick on 07.05.2015.
 */
public class Invert_Grey implements PlugInFilter
{
    public int setup(String arg, ImagePlus imp)
    {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip)
    {
        ip.invert();
    }
}