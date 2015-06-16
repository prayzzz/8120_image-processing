import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by Patrick on 16.06.2015.
 */
public class Line_Detection2 implements PlugInFilter
{
    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor sourceImageProcessor)
    {
        ImagePlus medianImage = new ImagePlus("Median", sourceImageProcessor.convertToColorProcessor());
        ImageProcessor medianImageProcessor = medianImage.getProcessor();

        RankFilters rankFilter = new RankFilters();
        rankFilter.rank(medianImageProcessor, 5.0, 4);

        medianImage.show();
    }
}
