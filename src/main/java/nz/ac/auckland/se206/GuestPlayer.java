package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Special guest player class that is loaded as a session instance rather than from file. */
public class GuestPlayer {
  // Declare the guest's fields
  private int consistentWins;
  private int fastestTime;
  private int losses;
  private int wins;

  private Map<String, Map<String, Boolean>> badges;
  private Map<String, String> gamedifficulty;

  private List<Boolean> winHistory;
  private List<Double> timeHistory;
  private List<String> accuracyDifficultyHistory;
  private List<String> timeDifficultyHistory;
  private List<String> wordDifficultyHistory;
  private List<String> confidenceDifficultyHistory;
  private List<String> wordHistory;

  private String fastestWord;

  /**
   * Default constructor, used for instantiating a new guest with stats reflecting no games played
   */
  public GuestPlayer() {
    // Set up the guest's attributes
    fastestTime = -1;
    losses = 0;
    wins = 0;
    consistentWins = 0;
    timeHistory = new ArrayList<Double>();
    wordHistory = new ArrayList<String>();
    fastestWord = " ";
    Map<String, String> difficulty = new HashMap<>();
    difficulty.put("accuracyDifficulty", "EASY");
    difficulty.put("wordsDifficulty", "EASY");
    difficulty.put("timeDifficulty", "EASY");
    difficulty.put("confidenceDifficulty", "EASY");
    gamedifficulty = difficulty;
    accuracyDifficultyHistory = new ArrayList<String>();
    timeDifficultyHistory = new ArrayList<String>();
    wordDifficultyHistory = new ArrayList<String>();
    confidenceDifficultyHistory = new ArrayList<String>();
    winHistory = new ArrayList<Boolean>();
    badges = Users.createBadges();
  }

  public int getFastestTime() {
    return fastestTime;
  }

  public int getLosses() {
    return losses;
  }

  public int getWins() {
    return wins;
  }

  public List<Double> getTimeHistory() {
    return timeHistory;
  }

  public List<String> getWordHistroy() {
    return wordHistory;
  }

  public String getFastestWord() {
    return fastestWord;
  }

  public Map<String, String> getGamedifficulty() {
    return gamedifficulty;
  }

  public void setGamedifficulty(Map<String, String> gamedifficulty) {
    this.gamedifficulty = gamedifficulty;
  }

  public Map<String, Map<String, Boolean>> getBadges() {
    return badges;
  }

  public void setBadges(Map<String, Map<String, Boolean>> badges) {
    this.badges = badges;
  }

  public int getConsistentWins() {
    return consistentWins;
  }

  public void setConsistentWins(int consistentWins) {
    this.consistentWins = consistentWins;
  }

  public List<String> getAccuracyDifficultyHistory() {
    return accuracyDifficultyHistory;
  }

  public List<String> getTimeDifficultyHistory() {
    return timeDifficultyHistory;
  }

  public List<String> getWordDifficultyHistory() {
    return wordDifficultyHistory;
  }

  public List<String> getConfidenceDifficultyHistory() {
    return confidenceDifficultyHistory;
  }

  public List<Boolean> getWinHistory() {
    return winHistory;
  }

  /** Saves all relevant current user stats to this instance of guestPlayer */
  public void saveGuest() {
    // Saves the fastest time, losses, and wins
    fastestTime = Users.getFastestTime();
    losses = Users.getLosses();
    wins = Users.getLosses();
    // Saves the time and word histories, as well as fastest word
    timeHistory = Users.getTimeHistory();
    wordHistory = Users.getWordHistory();
    fastestWord = Users.getFastestWord();
    // Saves the game difficulty, the badges, and consistent wins
    gamedifficulty = Users.getIndividualDifficulty();
    badges = Users.getBadges();
    consistentWins = Users.getConsistentWins();
    // Gets all of the users difficulty histories
    accuracyDifficultyHistory = Users.getAccuracyDifficultyHistory();
    timeDifficultyHistory = Users.getTimeDifficultyHistory();
    wordDifficultyHistory = Users.getWordDifficultyHistory();
    confidenceDifficultyHistory = Users.getConfidenceDifficultyHistory();
    winHistory = Users.getWinHistory();
  }
}
