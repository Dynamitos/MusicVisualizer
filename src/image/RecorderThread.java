package image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

import engine.renderEngine.DisplayManager;

public class RecorderThread extends Thread {
	public volatile boolean finished = false;
	private Queue<ImageWrapper> frames;
	private Queue<PixelsWrapper> pixelsQueue;
	private int numThreads = 8;
	private IMediaWriter writer;
	private IRational frameRate = IRational.make(60, 1);
	private int frameCounter = 0;
	private AssemblerThread[] threads;
	private String tempFile = "temp/temp.mp4";

	private class ImageWrapper {
		private BufferedImage image;
		private long timeStamp;

		public BufferedImage getImage() {
			return image;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

		public ImageWrapper(BufferedImage image, long timeStamp) {
			this.image = image;
			this.timeStamp = timeStamp;
		}

	}

	private class PixelsWrapper {
		private byte[] pixels;
		private long timeStamp;

		public PixelsWrapper(byte[] pixels, long timeStamp) {
			this.pixels = pixels;
			this.timeStamp = timeStamp;
		}

		public byte[] getPixels() {
			return pixels;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

	}

	private class AssemblerThread extends Thread {
		private int[] temp = new int[DisplayManager.WIDTH * DisplayManager.HEIGHT];

		@Override
		public void run() {
			while (!finished || !pixelsQueue.isEmpty()) {
				PixelsWrapper pixels = poll();
				if (pixels == null)
					continue;
				for (int i = 0; i < temp.length; i++) {
					int b = pixels.getPixels()[i * 4 + 0];
					int g = pixels.getPixels()[i * 4 + 1];
					int r = pixels.getPixels()[i * 4 + 2];
					temp[i] = b << 16 | g | r << 8;
				}
				BufferedImage image = new BufferedImage(DisplayManager.WIDTH, DisplayManager.HEIGHT,
						BufferedImage.TYPE_3BYTE_BGR);
				image.setRGB(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT, temp, 0, DisplayManager.WIDTH);
				ImageWrapper wrapper = new ImageWrapper(image, pixels.getTimeStamp());
				pixels = null;
				System.gc();
				addFrame(wrapper);
			}
		}
	}

	private synchronized void addFrame(ImageWrapper image) {
		frames.add(image);
	}

	public void dumpFrame(byte[] pixels, long nanoTime) {
		add(pixels, nanoTime);
		frameCounter++;
	}

	private synchronized PixelsWrapper poll() {
		return pixelsQueue.poll();
	}

	private synchronized void add(byte[] pixels, long nanoTime) {
		pixelsQueue.add(new PixelsWrapper(pixels, nanoTime));
	}

	@Override
	public void start() {
		setName("RecorderThread");
		frames = new LinkedList<>();
		pixelsQueue = new LinkedList<>();
		threads = new AssemblerThread[numThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new AssemblerThread();
			threads[i].start();
		}
		writer = ToolFactory.makeWriter(tempFile);
		writer.addVideoStream(0, 0, frameRate, DisplayManager.WIDTH, DisplayManager.HEIGHT);
		// writer.addAudioStream(0, 0, 25000, 1);
		super.start();
	}

	@Override
	public void run() {
		while (!finished || frameCounter > 0) {
			while (frames.isEmpty())
				System.out.println("Waiting for new Frames");
			;
			ImageWrapper image = frames.poll();
			System.out.println("remaining Frames: " + frames.size());
			writer.encodeVideo(0, image.getImage(), image.getTimeStamp(), TimeUnit.NANOSECONDS);
			// writer.encodeAudio(0, arg1, image.timeStamp,
			// TimeUnit.NANOSECONDS);
			frameCounter--;
			image = null;
			System.gc();
		}
		System.out.println("Finished");
		pixelsQueue = null;
		frames = null;
		threads = null;
		System.gc();
		writer.close();
	}
}