package jpiccoli.mt.crosscorrelation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WaveformPrinter {

	public static void printWaveform(final String filePath, final float[] waveform, final int signalPosition, final int signalLength) throws IOException {
		
		int signalEnd = signalPosition + signalLength;
		int start = Math.max(signalPosition - 500, 0);
		int end = Math.min(signalEnd + 500, waveform.length);
		
		BufferedImage image = new BufferedImage(end - start, 800, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		int verticalCenter = image.getHeight() / 2;
		
		graphics.setColor(Color.BLACK);
		for (int index = start + 1; index < signalPosition; index++) {
			int graphicsIndex = index - start;
			graphics.drawLine(graphicsIndex - 1, (int) (verticalCenter - waveform[index - 1]), graphicsIndex, (int) (verticalCenter - waveform[index]));
		}
		
		graphics.setColor(Color.RED);
		for (int index = signalPosition + 1; index < signalEnd; index++) {
			int graphicsIndex = index - start;
			graphics.drawLine(graphicsIndex - 1, (int) (verticalCenter - waveform[index - 1]), graphicsIndex, (int) (verticalCenter - waveform[index]));
		}
		
		graphics.setColor(Color.BLACK);
		for (int index = signalEnd + 1; index < end; index++) {
			int graphicsIndex = index - start;
			graphics.drawLine(graphicsIndex - 1, (int) (verticalCenter - waveform[index - 1]), graphicsIndex, (int) (verticalCenter - waveform[index]));
		}
		
		graphics.dispose();
		
		ImageIO.write(image, "JPEG", new File(filePath));
		
	}
	
}
