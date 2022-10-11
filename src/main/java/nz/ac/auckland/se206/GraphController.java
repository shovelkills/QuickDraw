package nz.ac.auckland.se206;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class GraphController {
  @FXML private Button accuracyButton;

  @FXML private Button wordButton;

  @FXML private Button timeButton;

  @FXML private Button confidenceButton;

  @FXML private Button lossButton;

  @FXML private Button winButton;

  @FXML private Button menuButton;

  @FXML private LineChart<String, Number> timeLineChart;

  @FXML private PieChart winsLossPieChart;

  @FXML private PieChart individualDifficultyPieChart;

  private static String difficulty = null;

  private static String gameOutcome;

  public void initialize() {
    loadGraphData();
  }

  // Load Graph Data
  public void loadGraphData() {
    timeLineChart.getData().add(Graph.getLineChartData());
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

  public void onShowWins() {
    gameOutcome = "Win";
    loadIndividualDifficulyPieChart();
  }

  public void onShowLoss() {
    gameOutcome = "Loss";
    loadIndividualDifficulyPieChart();
  }

  public void loadIndividualDifficulyPieChart() {
    if (difficulty == null) {
      difficulty = "accuracy";
    }
    individualDifficultyPieChart.setData(
        Graph.getIndividualDifficultyPieChart(gameOutcome, difficulty));
  }

  public void onAccuracyButton() {
    difficulty = "accuracy";
    loadIndividualDifficulyPieChart();
  }

  public void onWordButton() {
    difficulty = "word";
    loadIndividualDifficulyPieChart();
  }

  public void onTimeButton() {
    difficulty = "time";
    loadIndividualDifficulyPieChart();
  }

  public void onConfidenceButton() {
    difficulty = "confidence";
    loadIndividualDifficulyPieChart();
  }

  public void onMenuButton(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }
}
