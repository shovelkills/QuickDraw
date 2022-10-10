package nz.ac.auckland.se206;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;

public class GraphController {
  @FXML
  private LineChart<String, Number> timeLineChart;

  public void initialize() {
    timeLineChart.getData().add(Graph.getLineChartData());
  }
}
