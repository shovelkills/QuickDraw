package nz.ac.auckland.se206;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class GraphController {

  private static String difficulty = null;
  private static String gameOutcome;
  @FXML
  private Button accuracyButton;
  @FXML
  private Button confidenceButton;
  @FXML
  private Button menuButton;
  @FXML
  private Button timeButton;
  @FXML
  private Button wordButton;
  @FXML
  private CategoryAxis xAxis;
  @FXML
  private Label indvidualDifficultyLabel;
  @FXML
  private LineChart<String, Number> timeLineChart;
  @FXML
  private NumberAxis yAxis;
  @FXML
  private PieChart individualDifficultyPieChart;
  @FXML
  private PieChart winsLossPieChart;
  @FXML
  private Tooltip toolTip;

  /** JavaFX will call this method to initialize the UI */
  public void initialize() {
    loadGraphData();
    toolTip.setShowDelay(Duration.ZERO);
    toolTip.setWrapText(true);
    toolTip.setAutoFix(true);
  }

  /** Loads the data from the user to the graph */
  public void loadGraphData() {
    // Loads the data onto the list and disables the x axis
    timeLineChart.getData().clear();
    timeLineChart.getData().add(Graph.getLineChartData());
    timeLineChart.getXAxis().setTickLabelsVisible(false);
    winsLossPieChart.setData(Graph.getWinsLossPieChart());
    // Enables the difficulty buttons
    timeButton.setDisable(true);
    wordButton.setDisable(true);
    accuracyButton.setDisable(true);
    confidenceButton.setDisable(true);
    // Makes it so when pressing on the winloss piechart is changes the difficulty piegraph
    winsLossPieChart.getData().get(0).getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
        new EventHandler<MouseEvent>() {
          public void handle(MouseEvent e) {
            onShowWins();
          }
        });
    winsLossPieChart.getData().get(1).getNode().addEventHandler(MouseEvent.MOUSE_PRESSED,
        new EventHandler<MouseEvent>() {
          public void handle(MouseEvent e) {
            onShowLoss();
          }
        });
  }

  /** onShowWins will show the wins pie chart */
  private void onShowWins() {
    gameOutcome = "Win";
    loadIndividualDifficulyPieChart();
  }

  /** onShowLoss will show the loss pie chart */
  private void onShowLoss() {
    gameOutcome = "Loss";
    loadIndividualDifficulyPieChart();
  }

  /** Checks if there is any loaded difficulty. If there isn't just loads accuracy by default */
  private void loadIndividualDifficulyPieChart() {
    if (difficulty == null) {
      difficulty = "accuracy";
    }
    // Disable the difficulty buttons
    timeButton.setDisable(false);
    wordButton.setDisable(false);
    accuracyButton.setDisable(false);
    confidenceButton.setDisable(false);
    indvidualDifficultyLabel.setText(difficulty);
    individualDifficultyPieChart
        .setData(Graph.getIndividualDifficultyPieChart(gameOutcome, difficulty));
  }

  /** onShowAccuracyDifficulty will show the accuracy pie chart */
  @FXML
  private void onShowAccuracyDifficulty() {
    difficulty = "accuracy";
    loadIndividualDifficulyPieChart();
  }

  /** onShowWordDifficulty will show the word pie chart */
  @FXML
  private void onShowWordDifficulty() {
    difficulty = "word";
    loadIndividualDifficulyPieChart();
  }

  /** onShowTimeDifficulty will show the time pie chart */
  @FXML
  private void onShowTimeDifficulty() {
    difficulty = "time";
    loadIndividualDifficulyPieChart();
  }

  /** onShowConfidenceDifficulty will show the confidence pie chart */
  @FXML
  private void onShowConfidenceDifficulty() {
    difficulty = "confidence";
    loadIndividualDifficulyPieChart();
  }

  /**
   * Takes the user back to the menu button
   *
   * @param event the event when the button is pressed
   */
  @FXML
  public void onMenu(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }
}
