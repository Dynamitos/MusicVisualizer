package engine.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
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
	private BeatDetect beatDetect;
	//private float[][] prevValues;
	//private int SMOOTHING_LEVEL = 8;
	private float bassGain = 0;
	private float rawBassGain;
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
		beatDetect = new BeatDetect();
		beatDetect.detectMode(BeatDetect.FREQ_ENERGY);
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
			bands[i] = value / 1000;
			if (value > values[i * TESS_LEVEL]) {
				values[i * TESS_LEVEL + TESS_LEVEL / 2] = value;
			} else {
				values[i * TESS_LEVEL + TESS_LEVEL / 2] -= 0.0001f;
			}
		}
		for (int i = 0; i < MasterRenderer.NUM_SAMPLES - 1; i++) {
			float value = values[i * TESS_LEVEL + TESS_LEVEL / 2];
			for (int j = -TESS_LEVEL / 2; j <= TESS_LEVEL / 2; j++) {
				float temp = (float) (value - Math.pow(j * 1.0f, 2f));
				if (j != 0)
					values[i * TESS_LEVEL + TESS_LEVEL / 2 + j] = Math.max(temp,
							values[i * TESS_LEVEL + TESS_LEVEL / 2 + j]);
			}
		}
		float tempGain = 0;
		for (int i = 0; i < bands.length; i++) {
			tempGain += bands[i] / (1 + i * i);
		}

		rawBassGain = tempGain;

		if (tempGain < bassGain)
			bassGain -= 0.1f*DisplayManager.getFrameTimeSeconds();
		else
			bassGain = tempGain;

		for (int i = 0; i < values.length; i++) {
			values[i] /= 1000.0f;
			if (values[i] < 0)
				values[i] = 0;
		}
	}

	public float getBass() {
		return bassGain;
	}

	public float getRawBassGain(){return rawBassGain;}

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
