package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;

public class GraphController {
  @FXML
  private Button accuracyButton;

  @FXML
  private Button wordButton;

  @FXML
  private Button timeButton;

  @FXML
  private Button confidenceButton;

  @FXML
  private Button lossButton;

  @FXML
  private Button winButton;

  @FXML
  private LineChart<String, Number> timeLineChart;

  @FXML
  private PieChart winsLossPieChart;

  @FXML
  private PieChart individualDifficultyPieChart;

  private static String difficulty = null;

  private static String gameOutcome;


  public void initialize() {
    loadGraphData();
  }

  // Load Graph Data
  public void loadGraphData() {
    timeLineChart.getData().add(Graph.getLineChartData());
    winsLossPieChart.setData(Graph.getWinsLossPieChart());
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
    individualDifficultyPieChart
        .setData(Graph.getIndividualDifficultyPieChart(gameOutcome, difficulty));

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

}
