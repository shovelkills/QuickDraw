package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Special guest player class that is loaded as a session instance rather than from file. */
public class GuestPlayer {
  // Declare the guest's fields
  private int fastestTime;
  private int losses;
  private int wins;

  private Map<String, Map<String, Boolean>> badges;
  private Map<String, String> gamedifficulty;

  private List<Double> timeHistory;
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
    timeHistory = new ArrayList<Double>();
    wordHistory = new ArrayList<String>();
    fastestWord = " ";
    Map<String, String> difficulty = new HashMap<>();
    difficulty.put("accuracyDifficulty", "EASY");
    difficulty.put("wordsDifficulty", "EASY");
    difficulty.put("timeDifficulty", "EASY");
    difficulty.put("confidenceDifficulty", "EASY");
    gamedifficulty = difficulty;
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

  /** Saves all relevant current user stats to this instance of guestPlayer */
  public void saveGuest() {
    // Save all of the attributes into the guest temporary stats
    fastestTime = Users.getFastestTime();
    losses = Users.getLosses();
    wins = Users.getLosses();
    timeHistory = Users.getTimeHistory();
    wordHistory = Users.getWordHistory();
    fastestWord = Users.getFastestWord();
    gamedifficulty = Users.getIndividualDifficulty();
    badges = Users.getBadges();
  }
}
