import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.*;

/**
 * Created by prayzzz on 08.05.2015.
 */
public class Mark_Points implements PlugInFilter
{
    private ImagePlus imagePlus;

    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        this.imagePlus = imagePlus;
        return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        GenericDialog gd = new GenericDialog("Mark Points");
        gd.addStringField("Points", "12,12;350,200");
        gd.showDialog();

        if (gd.wasCanceled())
        {
            return;
        }

        ColorProcessor cp = ip.convertToColorProcessor();
        cp.setColor(Color.yellow);

        Integer pointNumber = 1;

        for (String pointString : gd.getNextString().split(";"))
        {
            String[] coords = pointString.split(",");

            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);

            cp.drawRect(x - 1, y - 1, 3, 3);
            cp.drawString(pointNumber.toString(), x - 3, y);

            pointNumber++;
        }

        imagePlus.setProcessor(cp);
    }
}