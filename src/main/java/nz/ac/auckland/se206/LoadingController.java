package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class LoadingController extends SoundsController {

  // Define elements
  @FXML
  private ProgressBar progressBar;

  public ProgressBar getProgressBar() {
    return progressBar;
  }
}
