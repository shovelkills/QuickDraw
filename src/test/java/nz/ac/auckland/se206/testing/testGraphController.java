package nz.ac.auckland.se206.testing;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import nz.ac.auckland.se206.Graph;

public class testGraphController {

  @FXML
  private LineChart<String, Number> timeLineChart;

  public void initialize() {
    timeLineChart.getData().add(Graph.getLineChartData());
  }

}
