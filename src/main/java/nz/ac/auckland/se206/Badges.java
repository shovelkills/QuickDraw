package nz.ac.auckland.se206;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class Badges {
  private static boolean isUserPictureDrawn;
  private static boolean isGuest = true;
  private static List<String> difficulties = Arrays.asList("E", "M", "H", "MS");

  /**
   * winBadge Changes the badges hash map so that the user has won the badge
   *
   * @param category the category of the badge
   * @param level the level of the badge
   */
  public static void winBadge(String category, String level) {
    if (isGuest) {
      return;
    }
    if (!Users.getBadges().get(category).get(level)) {
      String nameOfBadge;
      System.out.println(level);
      switch (level) {
        case "E":
          // Set up easy badge
          nameOfBadge = "Easy";
          break;
        case "M":
          // Set up medium badge
          nameOfBadge = "Medium";
          break;
        case "H":
          // Set up hard badge
          nameOfBadge = "Hard";
          break;
        case "MS":
          // Set up master badge
          nameOfBadge = "Master";
          break;
        default:
          nameOfBadge = level;
          break;
      }
      // Get the badges and then save the user
      Users.getBadges().get(category).put(level, true);
      Users.saveUser();

      // Set up a new alert notifying the player
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Won Badge");
      alert.setHeaderText(
          "You have won the " + nameOfBadge + " badge in the " + category + " category");
      // Gets the badge image
      Image image =
          new Image(
              Users.folderDirectory
                  + "/src/main/resources/images/badges/"
                  + category.replaceAll("\\s", "_")
                  + "/"
                  + level.replaceAll("\\s", "_")
                  + ".png");
      ImageView imageView = new ImageView(image);
      alert.setGraphic(imageView);
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
    // Check if the time is less than 1 seconds
    if (time <= 2) {
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
   * @param difficulty the difficulty that is being checked
   */
  public static void checkDifficulty(String difficulty) {
    // This function comments probably need a rewrite
    // Checks if the Accuracy, words, time and confidence levels are the are all won
    if (!difficulty.equals("MS")
        && Users.getBadges().get("Accuracy").get(difficulty)
        && Users.getBadges().get("Words").get(difficulty)
        && Users.getBadges().get("Time").get(difficulty)
        && Users.getBadges().get("Confidence").get(difficulty)) {
      // Win the badge for all difficulties
      // Checks the Accuracy, word, time and confidence
      winBadge("All Difficulties", difficulty);
    } else if (Users.getBadges().get("Words").get(difficulty)
        && Users.getBadges().get("Time").get(difficulty)
        && Users.getBadges().get("Confidence").get(difficulty)) {
      // Checks the Words, Time and Confidence for the Master Difficulty
      winBadge("All Difficulties", difficulty);
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
    if (isUserPictureDrawn) {
      // Give the draw profile picture badge
      winBadge("Misc", "Draw User Profile");
      isUserPictureDrawn = false;
    }
  }

  /**
   * setDrawUserPicture will update the draw users picture badge
   *
   * @param drawUserPicture takes in a true or false to update badge
   */
  public static void setDrawUserPicture(boolean drawUserPicture) {
    Badges.isUserPictureDrawn = drawUserPicture;
  }

  public static void setGuest(boolean isGuest) {
    Badges.isGuest = isGuest;
  }
}
