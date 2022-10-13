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
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class GraphController {

  @FXML private Button accuracyButton;

  @FXML private Button confidenceButton;

  @FXML private Button lossButton;

  @FXML private Button menuButton;

  @FXML private Button timeButton;

  @FXML private Button wordButton;

  @FXML private Button winButton;

  @FXML private CategoryAxis xAxis;

  @FXML private LineChart<String, Number> timeLineChart;

  @FXML private NumberAxis yAxis;

  @FXML private PieChart individualDifficultyPieChart;

  @FXML private PieChart winsLossPieChart;

  private static String difficulty = null;

  private static String gameOutcome;

  public void initilise() {
    loadGraphData();
  }

  /** Loads the data from the user to the graph */
  public void loadGraphData() {
    // Loads the data onto the list and disables the x axis
    timeLineChart.getData().clear();
    timeLineChart.getData().add(Graph.getLineChartData());
    timeLineChart.getXAxis().setTickLabelsVisible(false);
    winsLossPieChart.setData(Graph.getWinsLossPieChart());

    // Makes it so when pressing on the winloss piechart is changes the difficulty piegraph
    winsLossPieChart
        .getData()
        .get(0)
        .getNode()
        .addEventHandler(
            MouseEvent.MOUSE_PRESSED,
            new EventHandler<MouseEvent>() {
              public void handle(MouseEvent e) {
                onShowWins();
              }
            });
    winsLossPieChart
        .getData()
        .get(1)
        .getNode()
        .addEventHandler(
            MouseEvent.MOUSE_PRESSED,
            new EventHandler<MouseEvent>() {
              public void handle(MouseEvent e) {
                onShowLoss();
              }
            });
  }

  @FXML
  public void onShowWins() {
    gameOutcome = "Win";
    loadIndividualDifficulyPieChart();
  }

  @FXML
  public void onShowLoss() {
    gameOutcome = "Loss";
    loadIndividualDifficulyPieChart();
  }

  /** Checks if there is any loaded difficulty. If there isn't just loads accuracy by default */
  public void loadIndividualDifficulyPieChart() {
    if (difficulty == null) {
      difficulty = "accuracy";
    }
    individualDifficultyPieChart.setData(
        Graph.getIndividualDifficultyPieChart(gameOutcome, difficulty));
  }

  @FXML
  public void onShowAccuracyDifficulty() {
    difficulty = "accuracy";
    loadIndividualDifficulyPieChart();
  }

  @FXML
  public void onShowWordDifficulty() {
    difficulty = "word";
    loadIndividualDifficulyPieChart();
  }

  @FXML
  public void onShowTimeDifficulty() {
    difficulty = "time";
    loadIndividualDifficulyPieChart();
  }

  @FXML
  public void onShowConfidenceDifficulty() {
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
