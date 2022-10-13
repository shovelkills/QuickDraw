package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
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
    // Initializes the Linecart X and Y values
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    // Gets the data from the list

    for (int i = 0; i < Users.getWordHistory().size(); i++) {
      series
          .getData()
          .add(
              new Data<String, Number>(
                  Users.getWordHistory().get(i), Users.getTimeHistory().get(i)));
    }
    return series;
  }

  public static ObservableList<PieChart.Data> getWinsLossPieChart() {
    // Gets the number of wins and losses and coverts it into a percentage
    int totalGames = Users.getWins() + Users.getLosses();
    double winPercentage = ((double) Users.getWins()) * 100 / totalGames;
    double lossPercentage = ((double) Users.getLosses()) * 100 / totalGames;
    // Adds data to the piechart
    ObservableList<PieChart.Data> winLossPieChart =
        FXCollections.observableArrayList(
            new PieChart.Data("Wins", winPercentage), new PieChart.Data("Losses", lossPercentage));
    return winLossPieChart;
  }

  public static ObservableList<PieChart.Data> getIndividualDifficultyPieChart(
      String gameOutcome, String Difficulty) {

    boolean wantedGameOutcome = true;
    int totalGames = Users.getWins();
    List<String> checkOutcomeDifficulty;
    // Finds which difficulty they want to look at
    if (Difficulty.equals("accuracy")) {
      checkOutcomeDifficulty = Users.getAccuracyDifficultyHistory();
    } else if (Difficulty.equals("word")) {
      checkOutcomeDifficulty = Users.getWordDifficultyHistory();
    } else if (Difficulty.equals("time")) {
      checkOutcomeDifficulty = Users.getTimeDifficultyHistory();
    } else {
      checkOutcomeDifficulty = Users.getConfidenceDifficultyHistory();
    }

    // Checks if it wants to counts wins or losses
    if (gameOutcome.equals("Loss")) {
      wantedGameOutcome = false;
      totalGames = Users.getLosses();
    }
    // Gets all the values for the respective game outcome
    List<String> gameOutcomeDifficulty = new ArrayList<String>();
    for (int i = 0; i < Users.getWinHistory().size(); i++) {
      if (Users.getWinHistory().get(i) == wantedGameOutcome) {
        gameOutcomeDifficulty.add(checkOutcomeDifficulty.get(i));
      }
    }

    // Counts the percentage of each difficulty in the game outcome and adds it to a HashMap
    HashMap<String, Double> countDifficulty = new HashMap<String, Double>();
    countDifficulty.put(
        "Easy", (double) (Collections.frequency(gameOutcomeDifficulty, "E") * 100 / totalGames));
    countDifficulty.put(
        "Medium", (double) (Collections.frequency(gameOutcomeDifficulty, "M") * 100 / totalGames));
    countDifficulty.put(
        "Hard", (double) (Collections.frequency(gameOutcomeDifficulty, "H") * 100 / totalGames));
    if (!Difficulty.equals("accuracy")) {
      countDifficulty.put(
          "Master",
          (double) (Collections.frequency(gameOutcomeDifficulty, "MS") * 100 / totalGames));
    }
    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    // Adds data to the pieChart Data
    for (Entry<String, Double> entry : countDifficulty.entrySet()) {
      pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
    }

    return pieChartData;
  }
}
