package image;

import engine.math.Vector2f;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class EdgeDetector {
	public static Vector2f[] calculate(File f){
		BufferedImage original = null;
		try {
			original = ImageIO.read(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedImage image = new BufferedImage(original.getWidth()+2, original.getHeight()+2, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.drawImage(original, 1, 1, original.getWidth(), original.getHeight(), null);
		g.dispose();
		for(int x = 1; x < original.getWidth(); x++)
		{
			for(int y = 1; y < original.getHeight(); y++)
			{

			}
		}
		return null;
	}
	@SuppressWarnings("unused")
	private int[] getNearPixels(BufferedImage image, int startX, int startY)
	{
		int[] pixels = new int[8];
		pixels[0] = image.getRGB(startX-1, startY-1);
		pixels[1] = image.getRGB(startX, startY-1);
		pixels[2] = image.getRGB(startX+1, startY-1);
		pixels[3] = image.getRGB(startX-1, startY);
		pixels[4] = image.getRGB(startX+1, startY);
		pixels[5] = image.getRGB(startX-1, startY+1);
		pixels[6] = image.getRGB(startX, startY+1);
		pixels[7] = image.getRGB(startX+1, startY+1);
		return pixels;
	}
}
