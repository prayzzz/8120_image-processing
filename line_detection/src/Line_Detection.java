import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.BinaryProcessor;
import ij.process.ImageProcessor;

import java.awt.*;

/**
 * Created by Patrick on 01.06.2015.
 */
public class Line_Detection implements PlugInFilter
{
    public int setup(String arg, ImagePlus imp)
    {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip)
    {
        RankFilters rankFilter = new RankFilters();
        rankFilter.rank(ip, 2.0, 2);
        rankFilter.rank(ip, 5, 4);
        rankFilter.rank(ip, 1, 3);

        Prefs.blackBackground = false;
        ImagePlus imp = new ImagePlus("result", ip);
        IJ.runPlugIn(imp, "ij.plugin.Thresholder", "");
        IJ.runPlugIn(imp, "ij.plugin.filter.Binary", "skel");

        imp.show();
    }
}