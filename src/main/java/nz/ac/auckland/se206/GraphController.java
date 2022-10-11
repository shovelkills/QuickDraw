package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;

public class GraphController {

  @FXML
  private Button lossAccuracyButton;

  @FXML
  private Button winAccuracyButton;

  @FXML
  private LineChart<String, Number> timeLineChart;

  @FXML
  private PieChart winsLossPieChart;

  @FXML
  private PieChart individualDifficultyPieChart;

  private static String difficulty;

  private static String gameOutcome;

  public void initialize() {
    loadGraphData();
  }

  public void loadGraphData() {
    timeLineChart.getData().add(Graph.getLineChartData());
    winsLossPieChart.setData(Graph.getWinsLossPieChart());
  }

  public void onShowWinsAccuracy() {
    gameOutcome = "Win";
    difficulty = "accuracy";
    loadIndividualDifficulyPieChart();


  }

  public void onShowLossAccuracy() {
    gameOutcome = "Loss";
    difficulty = "accuracy";
    loadIndividualDifficulyPieChart();

  }

  public void loadIndividualDifficulyPieChart() {
    individualDifficultyPieChart
        .setData(Graph.getIndividualDifficultyPieChart(gameOutcome, difficulty));

  }

}
