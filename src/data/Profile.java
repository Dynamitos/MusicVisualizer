package data;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import engine.renderEngine.Dimension;
import image.Line;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class Profile {
	private File musicFile;
	private String image;
	private Dimension resolution;
	private float intensityScale, intensityOffset;
	private boolean scaling;
	private boolean vSync;
	private String name;
	private String overlay;
	private List<Line> lines;
	private int numSamples;
	//private FontType font;
	@XmlTransient
	private List<String> texts;
	public Profile(){}
	/**
	 *
	 * @param name Name of the profile
	 * @param musicFile The location of the mp3 file to play
	 * @param lines List of Line objects
	 * @param image The location of the background image
	 * @param overlay The file representing the overlay image
	 * @param resolution Resolution of the window
	 * @param intensityScale The multiplier for the bass-indication
	 * @param intensityOffset The offset for the bass-indication
	 * @param scaling Flag to indicate if bass-indication is turned on
	 * @param vSync Flag to indicate if VSync is turned on
	 * @param numSamples The number of points per fft-line
	 */
	public Profile(String name, File musicFile, List<Line> lines, String image, String overlay, Dimension resolution,
			float intensityScale, float intensityOffset, boolean scaling, boolean vSync, int numSamples) {
		this.setNumSamples(numSamples);
		this.musicFile = musicFile;
		this.image = image;
		this.overlay = overlay;
		this.lines = lines;
		this.resolution = resolution;
		this.intensityScale = intensityScale;
		this.intensityOffset = intensityOffset;
		this.scaling = scaling;
		this.vSync = vSync;
		this.name = name;
		texts = Arrays.asList(lyrics);
	}

	public List<String> getText()
	{
		return texts;
	}

	@XmlElement
	@XmlJavaTypeAdapter(FileNameAdapter.class)
	public File getMusicFile() {
		return musicFile;
	}

	public void setMusicFile(File musicFile) {
		this.musicFile = musicFile;
	}

	@XmlElement
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Dimension getResolution() {
		return resolution;
	}

	public void setResolution(Dimension resolution) {
		this.resolution = resolution;
	}

	public float getIntensityScale() {
		return intensityScale;
	}

	public void setIntensityScale(float intensityScale) {
		this.intensityScale = intensityScale;
	}

	public float getIntensityOffset() {
		return intensityOffset;
	}

	public void setIntensityOffset(float intensityOffset) {
		this.intensityOffset = intensityOffset;
	}

	public boolean isScaling() {
		return scaling;
	}

	public void setScaling(boolean scaling) {
		this.scaling = scaling;
	}

	public boolean isvSync() {
		return vSync;
	}

	public void setvSync(boolean vSync) {
		this.vSync = vSync;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Line> getLines() {
		return lines;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}

	public String getOverlay() {
		return overlay;
	}

	public void setOverlay(String overlay) {
		this.overlay = overlay;
	}

		String lyrics[] = {"You were the shadow to my light",
				"Did you feel us",
				"Another Start",
				"You fade away",
				"Afraid our aim is out of sight",
				"Wanna se us",
				"Alight",

				"Where are you now",
				"Where are you now",
				"Where are you now",
				"Was it all in my fantasy",
				"Where are you now",
				"Were you only imaginary",
				"Where are you now",

				"Atlantis",
				"Under the sea",
				"Under the sea",
				"Where are you now",
				"Another dream",
				"The monsters running wild inside of me",

				"I'm faded",
				"I'm faded",
				"So lost,",
				"I'm faded",
				"I'm faded",
				"So lost,",
				"I'm faded",
				"These shallow waters, never met",
				"What i needed",
				"I'm letting go",
				"A deeper dive",
				"Eternal silence of the sea",
				"I'm breathing",
				"Alive",

				"Where are you now",
				"Where are you now",

				"Under the bright",
				"But faded lights",
				"You set my heart on fire",
				"Where are you now",
				"Where are you now",

				"Where are you now",
				"Atlantis Under the sea",
				"Under the sea",
				"Where are you now",
				"Another dream",
				"The monsters running wild inside of me",
				"I'm faded",
				"I'm faded",
				"So lost,",
				"I'm faded",
				"I'm faded",
				"So lost,",
				"I'm faded."};
}
