package nz.ac.auckland.se206;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class Badges {
  private static boolean drawUserPicture;
  private static List<String> difficulties = Arrays.asList("E", "M", "H", "MS");

  /**
   * Changes the badges hasmap so that the user has won the badge
   *
   * @param catergory the category of the badge
   * @param level the level of the badge
   */
  public static void winBadge(String catergory, String level) {
    if (!Users.getBadges().get(catergory).get(level)) {
      String nameOfBadge;
      switch (level) {
        case "E":
          nameOfBadge = "Easy";
          break;
        case "M":
          nameOfBadge = "Medium";
          break;
        case "H":
          nameOfBadge = "Hard";
          break;
        case "MS":
          nameOfBadge = "Master";
          break;
        default:
          nameOfBadge = level;
          break;
      }
      Users.getBadges().get(catergory).put(level, true);
      Users.saveUser();

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Won Badge");
      alert.setHeaderText(
          "You have won the " + nameOfBadge + " badge in the " + catergory + " category");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        return;
      }
    }
  }

  /**
   * Checks if the user has met any of the time requirements that the user has won
   *
   * @param time the time it took for the user to win the badge
   */
  public static void checkWinTime(int time) {

    // Check if the time is less than 10 seconds

    if (time <= 10) {
      winBadge("Timed Wins", "10 Seconds");
    }
    // Check if the time is less than 30 seconds
    if (time <= 30) {
      winBadge("Timed Wins", "30 Seconds");
    }
    // Check if the time is less than 60 seconds
    if (time == 59) {
      winBadge("Timed Wins", "Last Second");
    }
  }

  /**
   * Wins badges depending on the different difficulties that the user has selected
   *
   * @param accuracy the accuracy difficulty
   * @param word the word difficulty
   * @param time the time difficulty
   * @param confidence the confidence difficulty
   */
  public static void winDifficultyBadges(
      Difficulty accuracy, Difficulty word, Difficulty time, Difficulty confidence) {

    // Get the badges hash map
    // Store the current difficulty badges in the user's profile
    winBadge("Accuracy", accuracy.toString());
    winBadge("Words", word.toString());
    winBadge("Time", time.toString());
    winBadge("Confidence", confidence.toString());
    // Check all the badges
    for (String difficulty : difficulties) {
      checkDifficulty(difficulty);
    }
  }

  /**
   * Checks if the difficulties across all aspects of the game are meet
   *
   * @param Difficulty the difficulty that is being checked
   */
  public static void checkDifficulty(String Difficulty) {
    // This function comments probably need a rewrite
    // Checks if the Accuracy, words, time and confidence levels are the are all won
    if (!Difficulty.equals("MS")
        && Users.getBadges().get("Accuracy").get(Difficulty)
        && Users.getBadges().get("Words").get(Difficulty)
        && Users.getBadges().get("Time").get(Difficulty)
        && Users.getBadges().get("Confidence").get(Difficulty)) {
      // Win the badge for all difficulties
      // Checks the Accuracy, word, time and confidence
      winBadge("All difficulties", Difficulty);
    } else if (Users.getBadges().get("Words").get(Difficulty)
        && Users.getBadges().get("Time").get(Difficulty)
        && Users.getBadges().get("Confidence").get(Difficulty)) {
      // Checks the Words, Time and Confidence for the Master Difficulty
      winBadge("All difficulties", Difficulty);
    }
  }

  /**
   * checkConsistentWins will check for player's consecutive wins
   *
   * @param consistentwins amount of wins they have consecutively
   */
  public static void checkConsistentWins(int consistentwins) {
    // Check if they have 2 consecutive wins

    if (consistentwins == 2) {
      winBadge("Wins", "2 consecutive wins");
    } else if (consistentwins == 5) {
      // Check if they have 5 consecutive wins
      winBadge("Wins", "5 consecutive wins");
    }
  }

  /** checkDrawnUserPicture will check if the user has drawn a profile picture for their badge */
  public static void checkDrawnUserPicture() {
    if (drawUserPicture) {
      // Give the draw profile picture badge
      winBadge("Misc", "Draw User Profile");
    }
  }

  public static void setDrawUserPicture(boolean drawUserPicture) {
    Badges.drawUserPicture = drawUserPicture;
  }
}
