package image;

import data.Profile;
import engine.math.Vector2f;
import engine.math.Vector4f;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OverlayManager {
	private static Image bgImage;
	private static Image overlayImage;
	private static FileChooser overlayChooser;
	private static Profile profile;
	private static GraphicsContext gc;
	private static Stage stageOverlay;
	private static List<Line> lines;
	private static Line currentLine;
	private static Color currentColor;
	private static GridPane gridOverlay;
	private static ColorPicker colorChooser;
	private static Canvas canvas;

	public static void startManager(Profile p) {
		profile = p;
		colorChooser = new ColorPicker();
		colorChooser.setValue(Color.TURQUOISE);
		overlayChooser = new FileChooser();
		overlayChooser.setInitialDirectory(new File("./"));
		overlayChooser.getExtensionFilters().add(new ExtensionFilter("Portable Network Graphics (*.png)", "*.png"));
		lines = new ArrayList<>(8);
		if (p.getLines() != null)
			lines.addAll(p.getLines());

		stageOverlay = new Stage();
		gridOverlay = new GridPane();
		gridOverlay.setAlignment(Pos.CENTER);
		gridOverlay.setHgap(10);
		gridOverlay.setVgap(10);

		try {
			if (p.getImage() != null)
				bgImage = new Image(new FileInputStream(new File(p.getImage())));
		} catch (FileNotFoundException e) {
			System.out.println(p.getImage() + " not found");
		}
		try{
			InputStream in = Class.class.getResourceAsStream(p.getOverlay());
			if(in == null)
			{
				overlayImage = new Image(new FileInputStream(p.getOverlay()));
			}
		}
		catch (FileNotFoundException e1)
		{
			InputStream in = Class.class.getResourceAsStream("/tex/Overlay.png");
			overlayImage = new Image(in);
			p.setOverlay("/tex/Overlay.png");
		}
		canvas = new Canvas(bgImage.getWidth(), bgImage.getHeight());
		gc = canvas.getGraphicsContext2D();
		repaint();
		gridOverlay.add(canvas, 0, 0);

		Scene sceneOverlay = new Scene(gridOverlay);
		stageOverlay.setScene(sceneOverlay);
		stageOverlay.setTitle("Overlay Manager");
		stageOverlay.show();

		Stage stageTools = new Stage();
		GridPane gridTools = new GridPane();
		gridTools.setAlignment(Pos.CENTER);
		gridTools.setVgap(10);
		gridTools.setHgap(10);

		Button overlayButton = new Button("Browse Overlay...");
		gridTools.add(overlayButton, 0, 0);
		Button imageButton = new Button("Browse Image...");
		gridTools.add(imageButton, 1, 0);
		gridTools.add(new Label("Choose Color: "), 0, 1);
		gridTools.add(colorChooser, 1, 1);
		gridTools.add(new Label("Edit Lyrics: "), 0, 2);
		Button textButton = new Button("Edit...");
		gridTools.add(textButton, 1, 2);
		Button clearButton = new Button("Clear");
		gridTools.add(clearButton, 0, 3);
		Button finishButton = new Button("Finish");
		gridTools.add(finishButton, 1, 3);

		clearButton.setOnAction((ActionEvent e) -> {
			lines.clear();
			repaint();
		});

		overlayButton.setOnAction((ActionEvent e) -> {
			setOverlay(overlayChooser.showOpenDialog(null).getAbsolutePath());
		});
		imageButton.setOnAction((ActionEvent e) -> {
			setImage(overlayChooser.showOpenDialog(null));
		});
		canvas.setOnMousePressed((MouseEvent event) -> {
			currentLine = new Line();
			currentLine.start = new Vector2f((float) (event.getSceneX() / canvas.getWidth()),
					(float) (event.getSceneY() / canvas.getHeight()));
			lines.add(currentLine);

		});
		canvas.setOnMouseReleased((MouseEvent event) -> {
			currentLine.end = new Vector2f((float) (event.getSceneX() / canvas.getWidth()),
					(float) (event.getSceneY() / canvas.getHeight()));
			if (event.isShiftDown())
				currentLine.end.y = currentLine.start.y;
			currentLine.height = 0.1f;
			Vector4f temp = new Vector4f();
			currentColor = colorChooser.getValue();
			temp.x = (float) currentColor.getRed();
			temp.y = (float) currentColor.getGreen();
			temp.z = (float) currentColor.getBlue();
			temp.w = 1;
			currentLine.color = temp;
			repaint();

		});
		canvas.setOnMouseDragged((MouseEvent event) -> {
			currentLine.end = new Vector2f((float) (event.getSceneX() / canvas.getWidth()),
					(float) (event.getSceneY() / canvas.getHeight()));
			if (event.isShiftDown())
				currentLine.end.y = currentLine.start.y;
			repaint();

		});

		canvas.setOnKeyPressed((KeyEvent e) -> {
			if (e.getCode() == KeyCode.Z && e.isControlDown()) {
				lines.remove(lines.size() - 1);
				System.out.println("removing line");
				repaint();
			}

		});

		Scene sceneTools = new Scene(gridTools);
		stageTools.setScene(sceneTools);
		stageTools.setTitle("Tools");
		stageTools.show();
		stageOverlay.setOnCloseRequest((WindowEvent e) -> {
			p.setLines(lines);
			if (stageTools.isShowing())
				stageTools.close();

		});
		stageTools.setOnCloseRequest((WindowEvent e) -> {
			p.setLines(lines);
			if (stageOverlay.isShowing())
				stageOverlay.close();
		});
		textButton.setOnAction((ActionEvent e) -> {

		});
	}

	public static void close() {
		if (stageOverlay == null)
			return;
		if (stageOverlay.isShowing())
			stageOverlay.close();
	}

	private static void setOverlay(String f) {
		if (f == null)
			return;
		try {
			overlayImage = new Image(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			overlayImage = new Image(Class.class.getResourceAsStream(f));
		}
		profile.setOverlay(f);
		repaint();
	}

	private static void setImage(File f) {
		if (f == null)
			return;
		try {
			bgImage = new Image(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		profile.setImage(f.getAbsolutePath());
		repaint();
	}

	public static void repaint() {

		gc.drawImage(bgImage, 0, 0, canvas.getWidth(), canvas.getHeight());
		gc.drawImage(overlayImage, 0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setStroke(currentColor);
		for (Line l : lines) {
			gc.strokeLine(l.start.x * canvas.getWidth(), l.start.y * canvas.getHeight(), l.end.x * canvas.getWidth(),
					l.end.y * canvas.getHeight());
		}
		gc.stroke();
	}

	public static Profile getProfile() {
		return profile;
	}
}
