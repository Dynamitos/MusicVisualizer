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
	private float[] targets;
	private float[] bands;
	private Minim minim;
	private AudioPlayer song;
	private FFT fft;
	private BeatDetect beatDetect;
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
		song = minim.loadFile(f.getAbsolutePath(), 2048);
		song.loop();
		fft = new FFT(song.bufferSize(), song.sampleRate());
		MasterRenderer.NUM_SAMPLES = 128;
		values = new float[MasterRenderer.NUM_SAMPLES * TESS_LEVEL];
		targets = new float[values.length];
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
		Arrays.fill(targets, 0);
		for(int i = 0; i < MasterRenderer.NUM_SAMPLES; ++i)
		{
			bands[i] = fft.getBand(i)/1000f;
		}
		int offset = TESS_LEVEL / 2;
		float j = -offset;
		for(int i = 0; i < values.length; ++i)
		{
			float value = bands[i/TESS_LEVEL];
			targets[i] = value;// Math.max((float)(value - Math.pow(j / TESS_LEVEL, 2.0f)), 0);
			j++;
			if(j >= offset)
			{
				j = -offset;
			}
		}
		for(int i = 0; i < values.length; ++i)
		{
			if(targets[i] > values[i])
			{
				values[i] = targets[i];
			}
			else if(values[i] - targets[i] > 0.01f)
			{
				values[i] -= (values[i] - targets[i])*DisplayManager.getFrameTimeSeconds()*10f;
			}
		}
		/*for (int i = 0; i < MasterRenderer.NUM_SAMPLES; i++) {
			float value = fft.getBand(i);
			bands[i] = value / 1000;
			if (value > values[i * TESS_LEVEL]) {
				values[i * TESS_LEVEL + TESS_LEVEL / 2] = value;
			} else {
				values[i * TESS_LEVEL + TESS_LEVEL / 2] -= 0.0001f;
			}
		}
		for (int i = 0; i < MasterRenderer.NUM_SAMPLES; i++) {
			float value = values[i * TESS_LEVEL + TESS_LEVEL / 2];
			for (int j = -TESS_LEVEL / 2; j <= TESS_LEVEL / 2-1; j++) {
				float temp = value;//(float) (value - Math.pow(j * 1.0f, 2f));
				if (j != 0)
					values[i * TESS_LEVEL + TESS_LEVEL / 2 + j] = Math.max(temp,
							values[i * TESS_LEVEL + TESS_LEVEL / 2 + j]);
			}
		}*/
		float tempGain = 0;
		for (int i = 0; i < bands.length; i++) {
			tempGain += bands[i] / (1 + i * i);
		}

		rawBassGain = tempGain;

		if (tempGain < bassGain)
			bassGain -= 0.1f*DisplayManager.getFrameTimeSeconds();
		else
			bassGain = tempGain;

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
