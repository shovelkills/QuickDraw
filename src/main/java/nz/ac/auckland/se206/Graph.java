package nz.ac.auckland.se206;

import java.util.List;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class Graph {


  /**
   * Gets the sum of the remain time that the user has to draw each word
   *
   * @return the average time remaining that it user has when the game recognizes the word
   */
  public static double getAverageTime() {
    double sum = 0;
    // Checks if the list is empty and finds the average time
    List<Double> timeHistory = Users.getTimeHistory();
    if (!timeHistory.isEmpty()) {
      for (Double time : timeHistory) {
        sum += time;
      }
      return sum / timeHistory.size();
    }
    return sum;
  }

  /**
   * Generates the data for the time of each game played
   * 
   * @return the generated graph from the data
   */
  public static XYChart.Series<String, Number> getLineChartData() {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Time taken per Game");
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    for (int i = 0; i < Users.getWordHistory().size(); i++) {
      System.out.println(i);
      series.getData().add(
          new Data<String, Number>(Users.getWordHistory().get(i), Users.getTimeHistory().get(i)));
    }

    return series;
  }
}
