package image;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javafx.scene.control.*;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;
import org.lwjgl.glfw.GLFW;

import engine.math.Vector2f;
import engine.math.Vector4f;
import engine.renderEngine.Dimension;
import engine.renderEngine.DisplayManager;
import engine.toolbox.Input;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class MusicController{
	private FileChooser imageChooser;
	private FileChooser chooser;
	private boolean isRunning = false;
	public static String SHADER_PATH = "";
	private File musicFile;
	private String image;
	private Profile currentProfile;
	private Map<String, Profile> profiles;
	@FXML
	private Button btMusic;
	@FXML
    private Button btImage;
	@FXML
    private Button btDefaultCover;
	@FXML
    private Button btOverlay;
	@FXML
    private TextField tfProfile;
	@FXML
    private Slider slIntensityOff;
	@FXML
    private Slider slIntensity;
	@FXML
    private ComboBox<String> cbProfileNames;
	@FXML
    private CheckBox cbScaling;
	@FXML
    private CheckBox cbVSync;
	@FXML
    private ComboBox<Dimension> cbResolution;

    public void init()
    {
        imageChooser = new FileChooser();
        imageChooser.setInitialDirectory(new File("./"));
        imageChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Portable Network Graphics (*.png)", "*.png"));
        chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MPEG3 Files (*.mp3)", "*.mp3"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wavefront Files (*.wav)", "*.wav"));
        currentProfile = loadProfile(new File("res/tex/Unknown.prof"));
        profiles = new HashMap<>();
        updateComponents();

    }


	@FXML
	public void onOverlayAction(ActionEvent e){
		OverlayManager.startManager(currentProfile);

	}
	@FXML
	public void onMusicAction(ActionEvent e){
		setMusicFile(chooser.showOpenDialog(null));
		update();

	}
	@FXML
	public void onImageAction(ActionEvent e){
		try {
			setImage(imageChooser.showOpenDialog(null).getAbsolutePath());
		} catch (NullPointerException ex) {
			return;
		}
		update();

	}
	@FXML
	public void onDefaultCoverAction(ActionEvent e){
		currentProfile.setImage(null);
		setMusicFile(currentProfile.getMusicFile());
	}

	@FXML
	public void onLoadProfileAction(ActionEvent e){
		FileChooser profileSaver = new FileChooser();
		profileSaver.setInitialDirectory(new File("./"));
		profileSaver.getExtensionFilters().add(new ExtensionFilter("Profile Files (*.prof)", "*.prof"));
		File file = profileSaver.showOpenDialog(null);
		if (file == null)
			return;
		currentProfile = loadProfile(file);

		cbProfileNames.getItems().add(currentProfile.getName());
		profiles.put(currentProfile.getName(), currentProfile);
		System.out.println("V-Sync: " + currentProfile.isvSync());
		updateComponents();

	}
	@FXML
	public void onSaveProfileAction(ActionEvent e){
		update();
		FileChooser profileOpener = new FileChooser();
		profileOpener.setInitialDirectory(new File("./"));
		profileOpener.initialFileNameProperty().set(currentProfile.getName() + ".prof");
		profileOpener.getExtensionFilters().add(new ExtensionFilter("Profile Files (*.prof)", "*.prof"));
		File f = profileOpener.showSaveDialog(null);
		if (f == null)
			return;
		System.out.println(f);
		saveProfile(currentProfile, f);

	}
	@FXML
	public void onFinishAction(ActionEvent e){
		OverlayManager.close();
		update();
		if (!isRunning)
			initializeRenderer();
		else
			updateRenderer();
	}
	@FXML
	public void onCloseWindowAction(ActionEvent e){
		OverlayManager.close();
		System.exit(0);

	}

	private void updateRenderer() {
		MasterRenderer.updateProfile(currentProfile);
	}

	private void initializeRenderer() {
		if (!checkProfile())
			return;
		DisplayManager.setDimension(currentProfile.getResolution());
		MasterRenderer renderer = new MasterRenderer(currentProfile);
		isRunning = true;
		while (!Input.keys[GLFW.GLFW_KEY_ESCAPE] && !DisplayManager.shouldClose()) {
			renderer.render();
		}
		renderer.terminate();
		isRunning = false;
		Input.keys[GLFW.GLFW_KEY_ESCAPE] = false;
	}

	private boolean checkProfile() {
		if (currentProfile.getMusicFile() == null || currentProfile.getMusicFile().getPath().isEmpty()) {
			// JOptionPane.showMessageDialog(null, "Please choose a song with
			// the 'Browse' Button.");
			System.out.println("Please select a file");
			return false;
		}
		if (currentProfile.getImage() == null || currentProfile.getImage().isEmpty()) {
			System.out.println("Please select a background");
			// JOptionPane.showConfirmDialog(null, "Please select a background
			// image with the 'Browse' Button");
			return false;
		}
		if (currentProfile.getOverlay() == null || currentProfile.getOverlay().isEmpty()) {
			System.out.println("Please select an overlay");
			// JOptionPane.showMessageDialog(null, "Please select an overlay
			// image from the Overlay Manager");
			return false;
		}
		return true;
	}

	private Profile loadProfile(File f) {
		Profile p = null;
		try {

			BufferedReader br = new BufferedReader(new FileReader(f));
			String data = br.readLine();
			String[] tokens = data.split("#");
			int counter = 0;
			String name = tokens[counter++];
			File music = tokens[counter++].isEmpty() ? null : new File(tokens[counter-1]).getAbsoluteFile();
			String image = tokens[counter++].isEmpty() ? null : tokens[counter-1];
			Dimension resolution = new Dimension(Integer.parseInt(tokens[counter++]),
					Integer.parseInt(tokens[counter++]));
			float intensityScale = Float.parseFloat(tokens[counter++]);
			float intensityOffset = Float.parseFloat(tokens[counter++]);
			boolean scaling = Boolean.parseBoolean(tokens[counter++]);
			boolean vSync = Boolean.parseBoolean(tokens[counter++]);
			String overlay = tokens[counter++];
			this.musicFile = music;
			this.image = image;
			int numSamples = Integer.parseInt(tokens[counter++]);
			int numLines = Integer.parseInt(tokens[counter++]);
			List<Line> lines = new ArrayList<>(numLines);
			for (int i = 0; i < numLines; i++) {
				Line line = new Line();
				float startX = Float.parseFloat(tokens[counter++]);
				float startY = Float.parseFloat(tokens[counter++]);
				float endX = Float.parseFloat(tokens[counter++]);
				float endY = Float.parseFloat(tokens[counter++]);
				float height = Float.parseFloat(tokens[counter++]);
				float colorX = Float.parseFloat(tokens[counter++]);
				float colorY = Float.parseFloat(tokens[counter++]);
				float colorZ = Float.parseFloat(tokens[counter++]);
				float colorW = Float.parseFloat(tokens[counter++]);
				line.start = new Vector2f(startX, startY);
				line.end = new Vector2f(endX, endY);
				line.height = height;
				line.color = new Vector4f(colorX, colorY, colorZ, colorW);
				lines.add(line);
			}
			p = new Profile(name, music, lines, image, overlay, resolution, intensityScale, intensityOffset, scaling,
					vSync, numSamples);
			br.close();
		} catch (IOException e) {
			System.err.println("Message: " + e.getMessage());
		}
		return p;
	}

	private void saveProfile(Profile p, File f) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			String data = "";
			data += p.getName() + "#";
			data += p.getMusicFile() + "#";
			data += p.getImage() + "#";
			data += p.getResolution().getWIDTH() + "#" + p.getResolution().getHEIGHT() + "#";
			data += p.getIntensityScale() + "#";
			data += p.getIntensityOffset() + "#";
			data += p.isScaling() + "#";
			data += p.isvSync() + "#";
			data += p.getOverlay() + "#";
			data += p.getNumSamples() + "#";
			data += p.getLines().size() + "#";
			for (Line line : p.getLines()) {
				data += line.toString();
			}

			bw.write(data);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateComponents() {
		System.out.println("CurrentProfile: " + currentProfile.isvSync());
		slIntensityOff.setValue(currentProfile.getIntensityOffset());
		slIntensity.setValue(currentProfile.getIntensityScale());
		cbProfileNames.setValue(currentProfile.getName());
		cbScaling.setSelected(currentProfile.isScaling());
		cbVSync.setSelected(currentProfile.isvSync());
		System.out.println("CurrentProfile: " + currentProfile.isvSync());
		cbResolution.setValue(currentProfile.getResolution());
		// System.out.println("Music: " + currentProfile.getMusicFile());
		tfProfile.setText(currentProfile.getName());
		if (currentProfile.getMusicFile() != null) {
			chooser.setInitialDirectory(currentProfile.getMusicFile().getAbsoluteFile().getParentFile());
			chooser.setInitialFileName(currentProfile.getMusicFile().getAbsoluteFile().getName());
		}
		if (currentProfile.getImage() != null) {
			imageChooser.setInitialDirectory(new File(currentProfile.getImage()).getAbsoluteFile().getParentFile());
			imageChooser.setInitialFileName(new File(currentProfile.getImage()).getAbsoluteFile().getName());
		}
	}

	private void update() {

		currentProfile.setImage(image);
		currentProfile.setIntensityOffset((float) slIntensityOff.getValue());
		currentProfile.setIntensityScale((float) slIntensity.getValue());
		currentProfile.setName(cbProfileNames.getValue());
		currentProfile.setResolution(cbResolution.getValue());
		currentProfile.setScaling(cbScaling.isSelected());
		currentProfile.setMusicFile(musicFile);
		currentProfile.setvSync(cbVSync.isSelected());
		currentProfile.setName(tfProfile.getText());
	}

	private void setMusicFile(File f) {
		if (f == null)
			return;

		this.musicFile = f;
		if (currentProfile.getImage() == null) {
			try {
				AudioFile audioFile = AudioFileIO.read(f);
				Tag t = audioFile.getTag();
				Artwork artwork = t.getFirstArtwork();
				File out = new File(t.getFirst(FieldKey.ARTIST) + ".png");
				ImageIO.write(artwork.getImage(), "PNG", out);
				this.image = out.getAbsolutePath();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (currentProfile.getName().equals("Unknown")) {
			try {

				AudioFile audioFile = AudioFileIO.read(f);
				Tag t = audioFile.getTag();
				cbProfileNames.setValue(t.getFirst(FieldKey.TITLE));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setImage(String image) {
		this.image = image;
	}

}
