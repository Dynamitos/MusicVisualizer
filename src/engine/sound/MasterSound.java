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
	private Minim minim;
	private AudioPlayer song;
	private FFT fft;
	private float[][] prevValues;
	private int SMOOTHING_LEVEL = 8;
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
		prevValues = new float[SMOOTHING_LEVEL][];
		for (int i = 0; i < SMOOTHING_LEVEL; i++) {
			prevValues[i] = new float[values.length];
			for (int j = 0; j < prevValues.length; j++) {
				prevValues[i][j] = 0;
			}
		}
	}

	public void run() {
		fft.forward(song.mix);
		Arrays.fill(values, 0);
		for (int i = 0; i < MasterRenderer.NUM_SAMPLES; i++) {
			float value = fft.getBand(i);
			bands[i] = value / 1000;
		}
		
		int offset = TESS_LEVEL / 2;
		for(int i = 0; i < bands.length; ++i)
		{
			float value = bands[i];
			int valueIndex = i * TESS_LEVEL + offset;
			//for(int j = 0; j < values.length; ++j)
			{
				int j = values.length/2;
				//float distance = j - valueIndex;
				//float newVal = (float) (value - Math.pow(distance, 2)/10000.f);
				float signum = 1;
				double part1 = 1.f/(Math.sqrt(2*Math.PI)*signum);
				double exponent = -0.5f*Math.pow(((j-valueIndex)/signum), 2.f);
				double distribution = part1 * Math.pow(Math.E, exponent);
				values[j] = (float) Math.max(values[j], distribution);
			}
		}
		for(int i = 0; i < values.length; ++i)
		{
			values[i] -= 0.1f * DisplayManager.getFrameTimeSeconds();
		}
		
		/*for (int i = 0; i < MasterRenderer.NUM_SAMPLES - 1; i++) {
			float value = values[i * TESS_LEVEL + TESS_LEVEL / 2];
			for (int j = -TESS_LEVEL / 2; j <= TESS_LEVEL / 2+1; j++) {
				float temp = (float) (value - Math.pow(j * 1.0f, 2f)/10.f);
				if (j != 0)
					values[i * TESS_LEVEL + TESS_LEVEL / 2 + j] = Math.max(temp,
							values[i * TESS_LEVEL + TESS_LEVEL / 2 + j]);
			}
		}*/
		
		
	
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
