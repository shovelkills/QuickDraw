package nz.ac.auckland.se206;

import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class Badges {

  /**
   * Changes the badges hasmap so that the user has won the badge
   * 
   * @param catergory the category of the badge
   * @param level the level of the badge
   */
  public static void winBadge(String catergory, String level) {
    Users.getBadges().get(catergory).put(level, true);
    Users.saveUser();
  }

  /**
   * Checks if the user has met any of the time requirements that the user has won
   * 
   * @param time the time it took for the user to win the badge
   */
  public static void checkWinTime(int time) {
    if (time <= 10) {
      winBadge("Timed Wins", "10 Seconds");
    }
    if (time <= 30) {
      winBadge("Timed Wins", "30 Seconds");
    }
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
  public static void winDifficultyBadges(Difficulty accuracy, Difficulty word, Difficulty time,
      Difficulty confidence) {

    Users.getBadges().get("Accuracy").put(accuracy.toString(), true);
    Users.getBadges().get("Words").put(word.toString(), true);
    Users.getBadges().get("Time").put(time.toString(), true);
    Users.getBadges().get("Confidence").put(confidence.toString(), true);
    checkAllDifficultiesBadge("E");
    checkAllDifficultiesBadge("M");
    checkAllDifficultiesBadge("H");
    checkAllDifficultiesBadge("MS");

  }

  /**
   * Checks if the difficulties accross all aspects of the game are meet
   */
  public static void checkAllDifficultiesBadge(String Difficulty) {
    // Checks if the Accuracy, words, time and confidence levels are the are all won
    if (!Difficulty.equals("MS") && Users.getBadges().get("Accuracy").get(Difficulty)
        && Users.getBadges().get("Words").get(Difficulty)
        && Users.getBadges().get("Time").get(Difficulty)
        && Users.getBadges().get("Confidence").get(Difficulty)) {
      winBadge("All difficulties", Difficulty);
    } else if (Users.getBadges().get("Words").get(Difficulty)
        && Users.getBadges().get("Time").get(Difficulty)
        && Users.getBadges().get("Confidence").get(Difficulty)) {
      winBadge("All difficulties", Difficulty);
    }


  }

}
