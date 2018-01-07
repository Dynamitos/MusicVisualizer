package image;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoadingScreen {

    @FXML
    private ImageView imageView;
    @FXML
    private ProgressBar progressBar;

    public void init()
    {
        imageView.setImage(new Image(LoadingScreen.class.getResourceAsStream("/tex/loading.gif")));
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }
}
