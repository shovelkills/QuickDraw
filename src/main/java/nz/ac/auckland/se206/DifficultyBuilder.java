package nz.ac.auckland.se206;

import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class DifficultyBuilder {
  private static Difficulty accDifficulty = Difficulty.E;
  private static Difficulty wordsDifficulty = Difficulty.E;
  private static Difficulty timeDifficulty = Difficulty.E;
  private static Difficulty confDifficulty = Difficulty.E;

  /**
   * difficultySetter will set the game's difficulty
   *
   * @param accuracy the difficulty for the accuracy
   * @param words the difficulty for the word choice
   * @param time the difficulty for the time
   * @param confidence the difficulty for the confidence
   */
  public static void difficultySetter(String accuracy, String words, String time,
      String confidence) {
    // Convert all the String difficulties to actual difficulties
    setAccDifficulty(difficultySelect(accuracy));
    setWordsDifficulty(difficultySelect(words));
    setTimeDifficulty(difficultySelect(time));
    setConfDifficulty(difficultySelect(confidence));
  }

  /** Set the game difficulty through the UI dropdown, update the word label */
  private static Difficulty difficultySelect(String difficulty) {
    Difficulty returnDiff;
    // gets the current difficulty we are on from the menu
    switch (difficulty) {
      case "EASY":
        // Sets the difficulty of the game to easy
        returnDiff = Difficulty.E;
        break;
      case "MEDIUM":
        // Sets the difficulty of the game to medium
        returnDiff = Difficulty.M;
        break;
      case "HARD":
        // Sets the difficulty of the game to hard
        returnDiff = Difficulty.H;
        break;
      case "MASTER":
        // Sets the difficulty of the game to master
        returnDiff = Difficulty.MS;
        break;
      default:
        returnDiff = null;
        break;
    }

    return returnDiff;
  }

  public static Difficulty getAccDifficulty() {
    return accDifficulty;
  }

  public static void setAccDifficulty(Difficulty accDifficulty) {
    DifficultyBuilder.accDifficulty = accDifficulty;
  }

  public static Difficulty getWordsDifficulty() {
    return wordsDifficulty;
  }

  public static void setWordsDifficulty(Difficulty wordsDifficulty) {
    DifficultyBuilder.wordsDifficulty = wordsDifficulty;
  }

  public static Difficulty getTimeDifficulty() {
    return timeDifficulty;
  }

  public static void setTimeDifficulty(Difficulty timeDifficulty) {
    DifficultyBuilder.timeDifficulty = timeDifficulty;
  }

  public static Difficulty getConfDifficulty() {
    return confDifficulty;
  }

  public static void setConfDifficulty(Difficulty confDifficulty) {
    DifficultyBuilder.confDifficulty = confDifficulty;
  }
}
