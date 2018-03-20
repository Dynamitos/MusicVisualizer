package image;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import data.Profile;
import engine.math.Vector2f;
import engine.math.Vector4f;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import engine.renderEngine.Dimension;
import engine.renderEngine.DisplayManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MusicController{
	private FileChooser imageChooser;
	private FileChooser songChooser;
	private boolean isRunning = false;
	public static String SHADER_PATH = "";
	private File musicFile;
	private String image;
	private Profile currentProfile;
	private Map<String, Profile> profiles;
	private Stage recordingStage = new Stage();
	private RenderMode renderMode;

	@FXML
	private Button btMusic;
	@FXML
    private Button btImage;
	@FXML
    private Button btDefaultCover;
	@FXML
    private Button btRecording;
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
        songChooser = new FileChooser();
        songChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MPEG3 Files (*.mp3)", "*.mp3"));
        songChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wavefront Files (*.wav)", "*.wav"));
        currentProfile = loadProfile(Class.class.getResourceAsStream("/tex/Unknown.prof"));

		java.awt.Dimension temp = Toolkit.getDefaultToolkit().getScreenSize();
		Set<Dimension> dimensions = new HashSet<>();
		dimensions.add(new Dimension((int) temp.getWidth(), (int) temp.getHeight()));
		dimensions.add(new Dimension(640, 360));
		dimensions.add(new Dimension(960, 540));
		dimensions.add(new Dimension(1024, 600));
		dimensions.add(new Dimension(1280, 720));
		dimensions.add(new Dimension(1600, 900));
		dimensions.add(new Dimension(1920, 1080));
		dimensions.add(new Dimension(3840, 2160));
		cbResolution.getItems().addAll(dimensions);
        profiles = new HashMap<>();
        updateComponents();
    }
	@FXML
	public void onRecordingAction(ActionEvent ev){
    	Parent root;
		try {
			FXMLLoader loader = new FXMLLoader(MusicController.class.getResource("/gui/Recording.fxml"));
			root = loader.load();
			((RecordingController)loader.getController()).setCurrentProfile(currentProfile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Scene scene = new Scene(root);
		recordingStage.setScene(scene);
		recordingStage.show();
	}
	@FXML
	public void onMusicAction(ActionEvent e){
		setMusicFile(songChooser.showOpenDialog(null));
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
		try {
			currentProfile = loadProfile(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

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
		renderMode.terminate();
		System.exit(0);
	}

	private void updateRenderer() {
		MasterRenderer.updateProfile(currentProfile);
	}

	private void initializeRenderer() {
		if (!checkProfile())
			return;
		DisplayManager.setDimension(currentProfile.getResolution());
		renderMode = RenderModeFactory.createRenderMode(currentProfile);
		renderMode.launch();
	}

	private boolean checkProfile() {
		Alert alert = null;
		boolean result = true;
    	if (currentProfile.getMusicFile() == null || currentProfile.getMusicFile().getPath().isEmpty()) {
			alert = new Alert(Alert.AlertType.WARNING, "Please choose a song with the 'Browse' Button");
			result =  false;
		}
		if (currentProfile.getImage() == null || currentProfile.getImage().isEmpty()) {
			alert = new Alert(Alert.AlertType.WARNING, "Please select a background image with the 'Browse' Button");
			result = false;
		}
		if (currentProfile.getOverlay() == null || currentProfile.getOverlay().isEmpty()) {
			alert = new Alert(Alert.AlertType.WARNING, "Please select an overlay image from the Overlay Manager");
			result =  false;
		}
		if(!result)
		{
			alert.showAndWait();
		}
		return result;
	}

	public Profile loadProfile(InputStream in) {
    	Profile p = null;
		try {
			JAXBContext context = JAXBContext.newInstance(Profile.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			p = (Profile)unmarshaller.unmarshal(in);
		}
		catch(Exception e)
		{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
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
				p = new Profile(name, music, lines, image, overlay, resolution, intensityScale, intensityOffset, false, scaling,
						vSync, numSamples);
				br.close();
			} catch (IOException ex) {
				System.err.println("Message: " + ex.getMessage());
			}
		}
		this.musicFile = p.getMusicFile();
		this.image = p.getImage();
		return p;
	}

	private void saveProfile(Profile p, File f) {
    	try
		{
			JAXBContext context = JAXBContext.newInstance(Profile.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(p, f);
		}
		catch (Exception e)
		{
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
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void updateComponents() {
		//System.out.println("CurrentProfile: " + currentProfile.isvSync());
		slIntensityOff.setValue(currentProfile.getIntensityOffset());
		slIntensity.setValue(currentProfile.getIntensityScale());
		cbProfileNames.setValue(currentProfile.getName());
		cbScaling.setSelected(currentProfile.isScaling());
		cbVSync.setSelected(currentProfile.isvSync());
		//System.out.println("CurrentProfile: " + currentProfile.isvSync());
		cbResolution.setValue(currentProfile.getResolution());
		// System.out.println("Music: " + currentProfile.getMusicFile());
		tfProfile.setText(currentProfile.getName());
		if (currentProfile.getMusicFile() != null) {
			songChooser.setInitialDirectory(currentProfile.getMusicFile().getAbsoluteFile().getParentFile());
			songChooser.setInitialFileName(currentProfile.getMusicFile().getAbsoluteFile().getName());
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
