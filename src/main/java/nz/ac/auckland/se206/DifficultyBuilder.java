package nz.ac.auckland.se206;

import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class DifficultyBuilder {
  private Difficulty accDifficulty;
  private Difficulty wordsDifficulty;
  private Difficulty timeDifficulty;
  private Difficulty confDifficulty;

  public DifficultyBuilder(String accDifficulty, String wordsDifficulty, String timeDifficulty,
      String confDifficulty) {

    this.accDifficulty = difficultySelect(accDifficulty);
    this.wordsDifficulty = difficultySelect(wordsDifficulty);
    this.timeDifficulty = difficultySelect(timeDifficulty);
    this.confDifficulty = difficultySelect(confDifficulty);
  }

  /** Set the game difficulty through the UI dropdown, update the word label */
  private Difficulty difficultySelect(String difficulty) {
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
        returnDiff = Difficulty.E;
    }

    return returnDiff;
  }
}
