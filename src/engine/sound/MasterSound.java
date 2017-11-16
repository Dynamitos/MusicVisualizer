package engine.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import engine.renderEngine.DisplayManager;
import image.MasterRenderer;

public class MasterSound {
	private long duration;
	private float[] values;
	private float[] bands;
	private float[] prevBands;
	private float[] sigmas;
	private Minim minim;
	private AudioPlayer song;
	private FFT fft;
	//private float[][] prevValues;
	//private int SMOOTHING_LEVEL = 8;
	private float bassGain = 0;
	public static final int TESS_LEVEL = 16;

	public String sketchPath(String fileName) {
		return fileName;
	}

	public InputStream createInput(String fileName) {
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return is;
	}

	public MasterSound(File f) {
		minim = new Minim(this);
		song = minim.loadFile(f.getAbsolutePath(), 1024);
		song.loop();
		fft = new FFT(song.bufferSize(), song.sampleRate());
		MasterRenderer.NUM_SAMPLES = 64;
		values = new float[MasterRenderer.NUM_SAMPLES * TESS_LEVEL];
		bands = new float[MasterRenderer.NUM_SAMPLES];
		prevBands = new float[MasterRenderer.NUM_SAMPLES];
		sigmas = new float[MasterRenderer.NUM_SAMPLES];
	}
	private float distribution(float x, float mue, float signum)
	{
		double part1 = 1.f/(Math.sqrt(Math.PI * 2) * signum);
		double exponent = -0.5f*Math.pow(((x-mue)/signum), 2.f);
		return (float) (part1 * Math.pow(Math.E, exponent));
	}
	public void run() {
		fft.forward(song.mix);
		Arrays.fill(values, 0);
		for (int i = 0; i < MasterRenderer.NUM_SAMPLES; i++) {
			float value = fft.getBand(i);
			bands[i] = value / 1000.f;
		}
		
		int offset = TESS_LEVEL / 2;
		for(int i = 0; i < bands.length; ++i)
		{
			float value = bands[i];
			int mue = i * TESS_LEVEL + offset;
			if(value > prevBands[i])
			{
				sigmas[i] = 70.f;//value of maximum = 0.25
			}
			else
			{
				value = prevBands[i];
				bands[i] = prevBands[i] - 0.1f * DisplayManager.getFrameTimeSeconds();
				//sigmas[i] -= 0.1f * DisplayManager.getFrameTimeSeconds();
			}
			for(int j = 0; j < values.length; ++j)
			{
				values[j] = Math.max(values[j], distribution(j, mue, sigmas[i])*value*250.f);
			}
		}
		System.arraycopy(bands, 0, prevBands, 0, bands.length);

//		float sigma = 50.f;
//		for(int i = 0; i < values.length; ++i)
//		{
//			System.out.println(bands[0]);
//			values[i] = distribution(i, values.length/2, sigma)*bands[2]*100.f;
//		}
//		
		float tempGain = 0;
		for (int i = 0; i < bands.length; i++) {
			tempGain += bands[i] / (1 + i * i);
		}
		if (tempGain < bassGain)
			bassGain -= 0.1f*DisplayManager.getFrameTimeSeconds();
		else
			bassGain = tempGain;
		
	}

	public float getBass() {
		return bassGain;
	}

	public void end() {
		song.close();
	}

	public void play() {
		song.play();
	}

	public float[] getValues() {
		return values;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void pause() {
		song.pause();
	}

	public void resume() {
		song.play();
	}
}
