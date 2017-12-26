package engine.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
	private float[] beats;
	private Minim minim;
	private AudioPlayer song;
	private FFT fft;
	private BeatDetect beatDetect;
	private float bassGain = 0;
	private float rawBassGain;
	public static final int TESS_LEVEL = 32;

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
	private void smooth(float[] data, float distribution)
	{
		for(int i = 1; i < data.length; ++i)
		{
			data[i] = data[i-1]*distribution + data[i]*(1-distribution);
		}
	}
	private void reversesmooth(float[] data, float distribution)
	{
		for(int i = data.length-2; i >= 0; --i)
		{
			data[i] = data[i+1]*distribution + data[i]*(1-distribution);
		}
	}
	public MasterSound(File f) {
		minim = new Minim(this);
		song = minim.loadFile(f.getAbsolutePath(), 2048);
		fft = new FFT(song.bufferSize(), song.sampleRate());
		MasterRenderer.NUM_SAMPLES = 16;
		targets = new float[MasterRenderer.NUM_SAMPLES * TESS_LEVEL];
		values = new float[targets.length*2];
		bands = new float[MasterRenderer.NUM_SAMPLES];
		beats = new float[bands.length];
		beatDetect = new BeatDetect();
		beatDetect.detectMode(BeatDetect.FREQ_ENERGY);
		song.play();
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
		for(int i = 0; i < bands.length; ++i)
		{
			bands[i] = fft.getBand(i*2)/1000f;
		}
		float prevValue, nextValue;
		int offset = TESS_LEVEL / 2;
		int prevIndex = 0, nextIndex = offset, bandIndex = 0;
		prevValue = bands[bandIndex];
		nextValue = bands[++bandIndex];
		for(int i = 0; i < targets.length; ++i)
		{
			float weight = (1.f * i - prevIndex) / (nextIndex - prevIndex);
			targets[i] = prevValue * (1-weight) + nextValue * weight;
			if(i == nextIndex)
			{
				prevValue = nextValue;
				nextValue = (++bandIndex < bands.length) ? bands[bandIndex] : 0;
				prevIndex = nextIndex;
				nextIndex += TESS_LEVEL;
			}
		}
		smooth(targets, 0.9f);
		for(int i = 0; i < targets.length; ++i)
		{
			values[i] -= (values[i] - targets[i])*(DisplayManager.getFrameTimeSeconds()*10f);
			values[values.length-i-1] = values[i];
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

	}
	public int getDataLength()
	{
		return values.length;
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
