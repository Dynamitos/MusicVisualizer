package image;

import data.Profile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class RecordingController {

    @FXML
    private CheckBox cbRecording;
    @FXML
    private TextField tfOutputPath;
    @FXML
    private Button btOutputPath;

    private Profile currentProfile;

    public void setCurrentProfile(Profile profile)
    {
        this.currentProfile = profile;
        cbRecording.setSelected(profile.isRecording());
        tfOutputPath.setText(profile.getOutputPath());
    }

    @FXML
    public void onRecordingFinish(ActionEvent e)
    {
        currentProfile.setRecording(cbRecording.isSelected());
        currentProfile.setOutputPath(tfOutputPath.getText());

        Stage stage = (Stage) btOutputPath.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void onBrowseOutputPath(ActionEvent ev)
    {
        FileChooser outputChooser = new FileChooser();
        outputChooser.setInitialDirectory(new File("./"));
        outputChooser.initialFileNameProperty().set(currentProfile.getName() + ".mp4");
        outputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Profile Files (*.prof)", "*.mp4"));
        File f = outputChooser.showSaveDialog(null);

        tfOutputPath.setText(f.getAbsolutePath());
    }
}
