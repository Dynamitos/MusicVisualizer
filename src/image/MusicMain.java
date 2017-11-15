package image;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MusicMain extends Application {
	private File musicFile;
	private String image;
	private Profile currentProfile;
	private Map<String, Profile> profiles;
	private FileChooser imageChooser;
	private FileChooser chooser;
	private ComboBox<String> profileNames;
	private CheckBox scalingCheckBox;
	private CheckBox vsyncCheckBox;
	private Slider intensityOffSlider;
	private Slider intensitySlider;
	private ComboBox<Dimension> resolutionBox;
	private TextField profileTextBox;
	private boolean isRunning = false;
	public static String SHADER_PATH;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("OpenGL Music Visualizer");
		profiles = new HashMap<>();
		SHADER_PATH = "";

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		
		int rowCounter = -1;

		Scene scene = new Scene(grid);
		scene.getStylesheets().add("/css/bootstrap3.css");
		primaryStage.setScene(scene);
		Label scenetitle = new Label("Presets");
		scenetitle.setId("head-line");

		grid.add(scenetitle, 0, ++rowCounter, 2, 1);

		Label musicLabel = new Label("Music File(mp3):");
		grid.add(musicLabel, 0, ++rowCounter);

		Button musicButton = new Button("Browse...");
		grid.add(musicButton, 1, rowCounter);

		Label imageLabel = new Label("Image File(png):");
		grid.add(imageLabel, 0, ++rowCounter);

		Button imageButton = new Button("Browse...");
		grid.add(imageButton, 1, rowCounter);

		Button defaultButton = new Button("Use default cover");
		grid.add(defaultButton, 1, ++rowCounter);

		Label colorLabel = new Label("Manage Overlay: ");
		grid.add(colorLabel, 0, ++rowCounter);

		Button lineButton = new Button("Manage");
		grid.add(lineButton, 1, rowCounter);

		Label resolutionLabel = new Label("Resolution: ");
		grid.add(resolutionLabel, 0, ++rowCounter);

		resolutionBox = new ComboBox<>();
		java.awt.Dimension temp = Toolkit.getDefaultToolkit().getScreenSize();
		Set<Dimension> dimensions = new HashSet<>();
		dimensions.add(new Dimension((int) temp.getWidth(), (int) temp.getHeight()));
		dimensions.add(new Dimension(640, 360));
		dimensions.add(new Dimension(960, 540));
		dimensions.add(new Dimension(1024, 600));
		dimensions.add(new Dimension(1270, 720));
		dimensions.add(new Dimension(1600, 900));
		dimensions.add(new Dimension(1920, 1080));
		dimensions.add(new Dimension(3840, 2160));
		resolutionBox.getItems().addAll(dimensions);
		resolutionBox.setValue(resolutionBox.getItems().get(0));
		grid.add(resolutionBox, 1, rowCounter);

		Label intensityLabel = new Label("Intensity Scale: ");
		grid.add(intensityLabel, 0, ++rowCounter);

		intensitySlider = new Slider(0, 5, 2);
		grid.add(intensitySlider, 1, rowCounter);

		Label intensityOffLabel = new Label("Intensity Offset: ");
		grid.add(intensityOffLabel, 0, ++rowCounter);

		intensityOffSlider = new Slider(0, 1, 0.7);
		grid.add(intensityOffSlider, 1, rowCounter);

		Label scaling = new Label("Scaling: ");
		grid.add(scaling, 0, ++rowCounter);

		scalingCheckBox = new CheckBox();
		grid.add(scalingCheckBox, 1, rowCounter);

		Label vsyncLabel = new Label("V-Sync");
		grid.add(vsyncLabel, 0, ++rowCounter);

		vsyncCheckBox = new CheckBox();
		grid.add(vsyncCheckBox, 1, rowCounter);

		Label profileLabel = new Label("Profile Name: ");
		grid.add(profileLabel, 0, ++rowCounter);

		profileTextBox = new TextField("Untitled Profile");
		grid.add(profileTextBox, 1, rowCounter);

		Label profileName = new Label("Profile: ");
		grid.add(profileName, 0, ++rowCounter);
		currentProfile = loadProfile(new File(Class.class.getResource("/Unknown.prof").getFile()));

		profileNames = new ComboBox<>();

		profileNames.getItems().add(currentProfile.getName());
		profiles.put(currentProfile.getName(), currentProfile);
		grid.add(profileNames, 1, rowCounter);

		Button loadButton = new Button("Load Profile");
		grid.add(loadButton, 0, ++rowCounter);

		Button saveButton = new Button("Save Profile");
		grid.add(saveButton, 1, rowCounter);

		Button finishButton = new Button("Finished");
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(finishButton);
		grid.add(hbBtn, 1, ++rowCounter);

		primaryStage.show();

		imageChooser = new FileChooser();
		imageChooser.setInitialDirectory(new File("./"));
		imageChooser.getExtensionFilters().add(new ExtensionFilter("Portable Network Graphics (*.png)", "*.png"));
		chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("MPEG3 Files (*.mp3)", "*.mp3"));
		chooser.getExtensionFilters().add(new ExtensionFilter("Wavefront Files (*.wav)", "*.wav"));

		lineButton.setOnAction((ActionEvent e) -> {
			OverlayManager.startManager(currentProfile);

		});
		musicButton.setOnAction((ActionEvent e) -> {
			setMusicFile(chooser.showOpenDialog(null));
			update();

		});
		imageButton.setOnAction((ActionEvent e) -> {
			try {
				setImage(imageChooser.showOpenDialog(null).getAbsolutePath());
			} catch (NullPointerException ex) {
				return;
			}
			update();

		});
		defaultButton.setOnAction((ActionEvent e) -> {
			currentProfile.setImage("");
			setMusicFile(currentProfile.getMusicFile());
		});
		loadButton.setOnAction((ActionEvent e) -> {

			FileChooser profileSaver = new FileChooser();
			profileSaver.setInitialDirectory(new File("./"));
			profileSaver.getExtensionFilters().add(new ExtensionFilter("Profile Files (*.prof)", "*.prof"));
			File file = profileSaver.showOpenDialog(null);
			if (file == null)
				return;
			currentProfile = loadProfile(file);

			profileNames.getItems().add(currentProfile.getName());
			profiles.put(currentProfile.getName(), currentProfile);
			System.out.println("V-Sync: " + currentProfile.isvSync());
			updateComponents();

		});
		saveButton.setOnAction((ActionEvent e) -> {
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

		});
		finishButton.setOnAction((ActionEvent e) -> {
			OverlayManager.close();
			update();
			if (!isRunning)
				initializeRenderer();
			else
				updateRenderer();
		});
		primaryStage.setOnCloseRequest((WindowEvent e) -> {
			OverlayManager.close();
			System.exit(0);

		});
		updateComponents();
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
			File music = tokens[counter++].isEmpty() ? null : new File(tokens[counter]).getAbsoluteFile();
			String image = tokens[counter++].isEmpty() ? null : tokens[counter];
			Dimension resolution = new Dimension(Integer.parseInt(tokens[counter++]),
					Integer.parseInt(tokens[counter++]));
			float intensityScale = Float.parseFloat(tokens[counter++]);
			float intensityOffset = Float.parseFloat(tokens[counter++]);
			boolean scaling = Boolean.parseBoolean(tokens[counter++]);
			boolean vSync = Boolean.parseBoolean(tokens[counter++]);
			String overlay = tokens[counter++];
			this.musicFile = music;
			this.image = image;
			this.profileTextBox.setText(name);
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
		intensityOffSlider.setValue(currentProfile.getIntensityOffset());
		intensitySlider.setValue(currentProfile.getIntensityScale());
		profileNames.setValue(currentProfile.getName());
		scalingCheckBox.setSelected(currentProfile.isScaling());
		vsyncCheckBox.setSelected(currentProfile.isvSync());
		System.out.println("CurrentProfile: " + currentProfile.isvSync());
		resolutionBox.setValue(currentProfile.getResolution());
		// System.out.println("Music: " + currentProfile.getMusicFile());
		profileTextBox.setText(currentProfile.getName());
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
		currentProfile.setIntensityOffset((float) intensityOffSlider.getValue());
		currentProfile.setIntensityScale((float) intensitySlider.getValue());
		currentProfile.setName(profileNames.getValue());
		currentProfile.setResolution(resolutionBox.getValue());
		currentProfile.setScaling(scalingCheckBox.isSelected());
		currentProfile.setMusicFile(musicFile);
		currentProfile.setvSync(vsyncCheckBox.isSelected());
		currentProfile.setName(profileTextBox.getText());
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
				profileNames.setValue(t.getFirst(FieldKey.TITLE));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setImage(String image) {
		this.image = image;
	}

	public static void main(String[] args) {
		System.setProperty("file.separator", "/");
		// System.setProperty("org.lwjgl.librarypath", "/target/natives");
		launch(args);
	}
}
