import java.awt.*;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.PlugIn;

/** This a prototype ImageJ plugin. */
public class All_Yellow implements PlugIn {

	public void run(String arg) {
		long start = System.currentTimeMillis();
		int w = 400, h = 400;
		ImageProcessor ip = new ColorProcessor(w, h);
		int[] pixels = (int[])ip.getPixels();
		int i = 0;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
                pixels[i++] = ((255 & 0xff)<<16) + ((255 & 0xff)<<8) + (0 & 0xff);
            }
		}

		new ImagePlus("All Yellow!", ip).show();
		IJ.showStatus(""+(System.currentTimeMillis()-start));
	 }

}

