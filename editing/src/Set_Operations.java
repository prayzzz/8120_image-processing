import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.RGBStackMerge;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by prayzzz on 08.05.2015.
 */
public class Set_Operations implements PlugInFilter
{
    private enum Operators
    {
        Union,
        Intersection,
        Set_Difference,
        Symmetric_Difference
    }

    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_8G;
    }

    public void run(ImageProcessor imageProcessor)
    {
        int[] ids = WindowManager.getIDList();
        List<String> choices = new ArrayList<>();

        for (int id : ids)
        {
            choices.add(WindowManager.getImage(id).getTitle());
        }

        GenericDialog gd = new GenericDialog("Set Operations");
        gd.addChoice("Image 1", choices.toArray(new String[choices.size()]), choices.get(0));
        gd.addChoice("Image 2", choices.toArray(new String[choices.size()]), choices.get(0));
        gd.addChoice("Operation", getNames(Operators.class), choices.get(0));
        gd.showDialog();

        if (gd.wasCanceled())
        {
            return;
        }

        ImagePlus image1 = WindowManager.getImage(gd.getNextChoice());
        ImagePlus image2 = WindowManager.getImage(gd.getNextChoice());
        String operator = gd.getNextChoice();

        ImagePlus result = null;
        switch (Operators.valueOf(operator))
        {
            case Union:
                result = executeOr(image1, image2);
                break;
            case Intersection:
                result = executeAnd(image1, image2);
                break;
            case Set_Difference:
                result = executeSubtract(image1, image2);
                break;
            case Symmetric_Difference:
                result = executeXor(image1, image2);
                break;
        }

        if (result == null)
        {
            return;
        }

        colorize(image1, image2, result);
        result.show();
    }

    private ImagePlus executeXor(ImagePlus image1, ImagePlus image2)
    {
        return new ImageCalculator().run("XOR create", image1, image2);
    }

    private ImagePlus executeSubtract(ImagePlus image1, ImagePlus image2)
    {
        return new ImageCalculator().run("Subtract create", image1, image2);
    }

    private ImagePlus executeAnd(ImagePlus image1, ImagePlus image2)
    {
        return new ImageCalculator().run("AND create", image1, image2);
    }

    private ImagePlus executeOr(ImagePlus image1, ImagePlus image2)
    {
        return new ImageCalculator().run("OR create", image1, image2);
    }

    private void colorize(ImagePlus image1, ImagePlus image2, ImagePlus result)
    {
        ImagePlus fullImage = executeOr(image1, image2);
        ImagePlus greenNegative = executeSubtract(fullImage, result);

        ImageStack stack = RGBStackMerge.mergeStacks(greenNegative.getImageStack(), fullImage.getImageStack(), greenNegative.getImageStack(), true);
        result.setStack(stack);
    }

    public static String[] getNames(Class<? extends Enum<?>> e)
    {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
    }
}
