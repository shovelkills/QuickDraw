package nz.ac.auckland.se206;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import nz.ac.auckland.se206.GameSelectController.GameMode;
import nz.ac.auckland.se206.words.CategorySelector;

public class Users {
  // Stores the information in the JSON file as individual variables

  private static int consistentWins;
  private static int fastestTime;
  private static int losses;
  private static int wins;

  private static List<Boolean> winHistory;
  private static List<Double> timeHistory;
  private static List<String> accuracyDifficultyHistory;
  private static List<String> confidenceDifficultyHistory;
  private static List<String> timeDifficultyHistory;
  private static List<String> wordDifficultyHistory;
  private static List<String> userList;
  private static List<String> wordHistory;

  private static Map<?, ?> userInfo;
  private static Map<String, Map<String, Boolean>> badges;
  private static Map<String, String> gameDifficulty;

  private static String fastestWord;
  private static String profilePicture;
  protected static String folderDirectory = System.getProperty("user.dir");

  private static String recentUser;
  private static String userName;
  protected static Image userImage = ProfileBuilder.getUserImage();

  // Only one guest player can be referenced through Users at one time
  private static GuestPlayer guestPlayer;

  /**
   * This method finds the local files stored for the users by inputting their username
   *
   * @param username the username of the user
   */
  public static void loadUser(String username) {
    // Check for guest profile
    if (username == "Guest") {
      loadGuest(guestPlayer);
      Badges.setGuest(true);
      setRecentUser(username);
      return;
    }
    if (isValidUsername(username)) {
      // Change to show in GUI
      System.err.println("You cannot have special characters in username");
      return;
    }
    // Set the guest tag off if not currently logged in as guest
    Badges.setGuest(false);
    // Creates a jsonParser to read the file
    try (FileReader reader =
        new FileReader(folderDirectory + "/src/main/resources/users/" + username + ".json")) {
      // Reads the JSON file and casts and stores it as a Map
      Gson gson = new Gson();
      userInfo = gson.fromJson(reader, Map.class);

      // Add the information from JSON file to variables through casting
      userName = (String) userInfo.get("username");

      fastestWord = (String) userInfo.get("fastestWord");

      wins = (int) (double) userInfo.get("gamesWon");

      losses = (int) (double) userInfo.get("gamesLost");

      fastestTime = (int) (double) userInfo.get("fastestGame");

      consistentWins = (int) (double) userInfo.get("consistentWins");

      wordHistory = (List<String>) userInfo.get("wordHistory");

      timeHistory = (List<Double>) userInfo.get("timeHistory");

      gameDifficulty = (Map<String, String>) userInfo.get("gameDifficulty");

      badges = (Map<String, Map<String, Boolean>>) userInfo.get("Badges");

      accuracyDifficultyHistory = (List<String>) userInfo.get("accuracyDifficultyHistory");

      wordDifficultyHistory = (List<String>) userInfo.get("wordDifficultyHistory");

      timeDifficultyHistory = (List<String>) userInfo.get("timeDifficultyHistory");

      confidenceDifficultyHistory = (List<String>) userInfo.get("confidenceDifficultyHistory");

      winHistory = (List<Boolean>) userInfo.get("winHistory");

      setRecentUser(username);

      reader.close();

    } catch (FileNotFoundException e) {
      System.err.println("User Not Found"); // Change to show on GUI
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Creates a json file with the user input of username so that it can store data
   *
   * @param username the username the user wants to use
   * @return returns a boolean either true or false
   */
  public static boolean createUser(String username) {
    if (isValidUsername(username)) {
      // Change to show in GUI
      System.err.println("You cannot have special characters in username");
      return false;
    }
    try (FileReader reader =
        new FileReader(folderDirectory + "/src/main/resources/users/" + username + ".json")) {
      // Change to show on GUI
      System.err.println("Username taken, try a different username");
      return false;

    } catch (FileNotFoundException e) {
      // Creates a hashMap to add information to the JSON file
      Map<String, Object> userMap = new LinkedHashMap<>();
      userMap.put("username", username);
      userMap.put("gamesWon", 0);
      userMap.put("gamesLost", 0);
      userMap.put("wordHistory", new ArrayList<String>());
      userMap.put("fastestGame", -1);
      userMap.put("fastestWord", " ");
      userMap.put("timeHistory", new ArrayList<Double>());
      userMap.put("consistentWins", 0);
      userMap.put("accuracyDifficultyHistory", new ArrayList<String>());
      userMap.put("wordDifficultyHistory", new ArrayList<String>());
      userMap.put("timeDifficultyHistory", new ArrayList<String>());
      userMap.put("confidenceDifficultyHistory", new ArrayList<String>());
      userMap.put("winHistory", new ArrayList<Boolean>());

      // Creates the default difficulty
      Map<String, String> difficulty = new LinkedHashMap<>();
      difficulty.put("accuracyDifficulty", "EASY");
      difficulty.put("wordsDifficulty", "EASY");
      difficulty.put("timeDifficulty", "EASY");
      difficulty.put("confidenceDifficulty", "EASY");
      userMap.put("gameDifficulty", difficulty);

      userMap.put("Badges", createBadges());

      // Gets the file directory to save to and runs the load user method
      File dir = new File(folderDirectory + "/src/main/resources/users/");
      try {
        // Creates a writer object to write and save the file
        Writer writer = new FileWriter(new File(dir, username + ".json"));
        // Makes the JSON file easier to read
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(userMap, writer);
        writer.close();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(userImage, null);
        addUserList(username);
        loadUser(username);
        // Save the user's image selected
        saveProfilePicture(bufferedImage);
        Badges.checkDrawnUserPicture();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * Generates a HashMap of all the badges
   *
   * @return the badge HashMap of all the badges
   */
  public static Map<String, Map<String, Boolean>> createBadges() {
    // Creates a new HashMap the contains all the badges
    Map<String, Map<String, Boolean>> badgeList = new LinkedHashMap<>();
    Map<String, Boolean> difficulty = new LinkedHashMap<>();
    // Generates the badges
    difficulty.put("E", false);
    difficulty.put("M", false);
    difficulty.put("H", false);
    badgeList.put("Accuracy", difficulty);
    difficulty = new LinkedHashMap<>();
    difficulty.put("E", false);
    difficulty.put("M", false);
    difficulty.put("H", false);
    difficulty.put("MS", false);

    // Adds to badge list based of difficulty
    badgeList.put("Words", difficulty);
    badgeList.put("Time", difficulty);
    badgeList.put("Confidence", difficulty);
    badgeList.put("All Difficulties", difficulty);
    // Generate hashMap for badges based on time wins
    Map<String, Boolean> timedWins = new LinkedHashMap<>();
    timedWins.put("30 Seconds", false);
    timedWins.put("10 Seconds", false);
    timedWins.put("Last Second", false);
    badgeList.put("Timed Wins", timedWins);
    // Generate hashMap for badges based on Wins
    Map<String, Boolean> wins = new LinkedHashMap<>();
    wins.put("First Win", false);
    wins.put("2 consecutive wins", false);
    wins.put("5 consecutive wins", false);
    badgeList.put("Wins", wins);
    // Generates hashMap for badges on Misc actions
    Map<String, Boolean> misc = new LinkedHashMap<>();
    misc.put("Draw User Profile", false);
    misc.put("Play Zen Mode", false);
    misc.put("View Stats Page", false);
    misc.put("View Badges Page", false);
    badgeList.put("Misc", misc);

    return badgeList;
  }

  /**
   * Checks if there are any special characters in the username
   *
   * @param username the username the user inputted
   * @return boolean of if there are special characters in the username. True means there are
   *     special characters in the string
   */
  public static boolean isValidUsername(String username) {
    Pattern validCharacters = Pattern.compile("[^a-z0-9-]", Pattern.CASE_INSENSITIVE);
    Matcher matchCharacters = validCharacters.matcher(username);
    return matchCharacters.find();
  }

  /** Saves the data of the user to a JSON file after a game finishes */
  public static void saveUser() {
    if (userName == "Guest") {
      guestPlayer.saveGuest();
      return;
    }
    File dir = new File(folderDirectory + "/src/main/resources/users/");
    try (Writer writer = new FileWriter(new File(dir, userName + ".json"))) {

      // Creates map to store user info
      Map<String, Object> userMap = new LinkedHashMap<>();
      userMap.put("username", userName);
      userMap.put("gamesWon", wins);
      userMap.put("gamesLost", losses);
      userMap.put("wordHistory", wordHistory);
      userMap.put("fastestGame", fastestTime);
      userMap.put("fastestWord", fastestWord);
      userMap.put("timeHistory", timeHistory);
      userMap.put("gameDifficulty", gameDifficulty);
      userMap.put("Badges", badges);
      userMap.put("consistentWins", consistentWins);
      userMap.put("accuracyDifficultyHistory", accuracyDifficultyHistory);
      userMap.put("wordDifficultyHistory", wordDifficultyHistory);
      userMap.put("timeDifficultyHistory", timeDifficultyHistory);
      userMap.put("confidenceDifficultyHistory", confidenceDifficultyHistory);
      userMap.put("winHistory", winHistory);

      // Creates a writer object to write and save the file
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(userMap, writer);
      writer.close();

    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  /** Gets the userlist and recent user list from the JSON file */
  public static void loadUsersFromList() {
    // Reads the userlist file
    try (FileReader reader =
        new FileReader(folderDirectory + "/src/main/resources/users/userlist.json")) {

      Gson gson = new Gson();
      Map<String, ?> listOfUsers = gson.fromJson(reader, Map.class);
      recentUser = (String) listOfUsers.get("recentUser");
      userList = (List<String>) listOfUsers.get("userList");

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Saves the List of Users and the recent lists */
  public static void saveUserList() {
    // Create a new user file
    File dir = new File(folderDirectory + "/src/main/resources/users/");
    try (Writer writer = new FileWriter(new File(dir, "userlist.json"))) {
      // Create the hash map
      Map<String, Object> saveToJson = new HashMap<>();
      saveToJson.put("recentUser", recentUser);
      saveToJson.put("userList", userList);
      // Creates a writer object to write and save the file
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(saveToJson, writer);
      writer.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Deletes the user JSON file and also removes it from the userlist
   *
   * @param username the username that wants to be deleted
   */
  public static void deleteUser(String username) {
    // Finds the json file and deletes it
    File userFile = new File(folderDirectory + "/src/main/resources/users/" + username + ".json");
    userFile.delete();
    // removes user from the userlist and if its the recent user
    int index = userList.indexOf(username);
    userList.remove(index);
    if (recentUser.equals(username)) {
      recentUser = "";
    }
    saveUserList();
  }

  /**
   * Loads the profile picture that the user has drawn
   *
   * @param user takes in a user's profile picture image
   * @return the image of the profile picture
   */
  public static Image loadProfilePicture(String user) {
    File profileFile =
        new File(folderDirectory + "/src/main/resources/profilepictures/" + user + ".png");
    return new Image(profileFile.toURI().toString());
  }

  /**
   * Saves the profile picture as a bmp in the profilepicture folder
   *
   * @param snapshot the profile picture
   */
  public static void saveProfilePicture(BufferedImage snapshot) {
    // Convert into a binary image.
    final BufferedImage imageBinary =
        new BufferedImage(snapshot.getWidth(), snapshot.getHeight(), BufferedImage.TYPE_INT_ARGB);

    final Graphics2D graphics = imageBinary.createGraphics();

    graphics.drawImage(snapshot, 0, 0, null);

    // To release memory we dispose.
    graphics.dispose();

    // Write it to file
    File file =
        new File(folderDirectory + "/src/main/resources/profilepictures/" + userName + ".png");
    try {
      ImageIO.write(imageBinary, "png", file);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Deletes the profile picture of the user
   *
   * @param user the user that wants their picture to be deleted
   */
  public static void deleteProfilePicture(String user) {
    File file = new File(folderDirectory + "/src/main/resources/profilepictures/" + user + ".png");
    file.delete();
  }

  /**
   * Loads the guest statistics into the current player statistics, allowing for a guest profile to
   * retain their statistics for that session even if another profile has also played during this
   * session
   *
   * @param sessionGuest The current GuestPlayer object for this session
   */
  private static void loadGuest(GuestPlayer sessionGuest) {
    // Add the information to the guest
    userName = "Guest";
    // Get the fastest word, wins, and losses
    fastestWord = guestPlayer.getFastestWord();
    wins = guestPlayer.getWins();
    losses = guestPlayer.getLosses();
    // Get the fastest time, word history, time history, and game difficulty
    fastestTime = guestPlayer.getFastestTime();
    wordHistory = guestPlayer.getWordHistroy();
    timeHistory = guestPlayer.getTimeHistory();
    gameDifficulty = guestPlayer.getGamedifficulty();
    // Get badges and all difficulty histories
    badges = guestPlayer.getBadges();
    accuracyDifficultyHistory = guestPlayer.getAccuracyDifficultyHistory();
    timeDifficultyHistory = guestPlayer.getTimeDifficultyHistory();
    wordDifficultyHistory = guestPlayer.getWordDifficultyHistory();
    confidenceDifficultyHistory = guestPlayer.getConfidenceDifficultyHistory();
    // get the player's win history
    winHistory = guestPlayer.getWinHistory();
    // Update the most recent user
    setRecentUser(userName);
  }

  /** Creates a new guest player instance with reset stats (as if no games played) */
  public static void createNewGuest() {
    guestPlayer = new GuestPlayer();
  }

  /**
   * Checks if the most recent game has the fastest time
   *
   * @param time the time it took to draw the word
   * @param word the word the user had to draw
   */
  public static void checkFastestTime(int time, String word) {
    // Checks if it is the fastest time
    if (Game.getWon() && (time <= Users.fastestTime || (Users.fastestTime == -1 && time != 60))) {
      Users.fastestTime = time;
      setFastestWord(word);
    }
    addWordHistory(word);
    // Checks if the user is a guest
    if (recentUser == "Guest") {
      guestPlayer.saveGuest();
    }
  }

  /**
   * Adds the difficulty to the history
   *
   * @param accuracy the accuracy difficulty
   * @param word the word difficulty
   * @param time the time difficulty
   * @param confidence the confidence difficulty
   */
  public static void addGameDifficultyHistory(
      String accuracy, String word, String time, String confidence) {

    // Adds params into the history
    accuracyDifficultyHistory.add(accuracy);
    wordDifficultyHistory.add(word);
    timeDifficultyHistory.add(time);
    confidenceDifficultyHistory.add(confidence);
  }

  /**
   * Updates the userList by adding new user to list
   *
   * @param user takes in a user by their user name
   */
  public static void addUserList(String user) {
    userList.add(user);
    // Save the user List
    saveUserList();
  }

  /**
   * Increases current user's wins by 1, consistentWins by 1 and check the number of consistent wins
   */
  public static void increaseWins() {
    Users.wins++;
    Users.consistentWins++;
    Users.addWinHistory(true);
    Badges.checkConsistentWins(consistentWins);
  }

  /** Increases current user's losses by 1 */
  public static void increaseLosses() {
    Users.losses++;
    Users.addWinHistory(false);
  }

  /**
   * Sets the difficulty of each aspect of the game to the most recent difficulties
   *
   * @param accuracydifficulty the accuracy difficulty
   * @param worddifficulty the word difficulty
   * @param timedifficulty the time difficulty
   * @param confidencedifficulty the confidence difficulty
   */
  public static void setGameDifficulty(
      String accuracydifficulty,
      String worddifficulty,
      String timedifficulty,
      String confidencedifficulty) {
    // Sets each of the difficulty
    Users.gameDifficulty.put("accuracyDifficulty", accuracydifficulty);
    Users.gameDifficulty.put("wordsDifficulty", worddifficulty);
    Users.gameDifficulty.put("timeDifficulty", timedifficulty);
    Users.gameDifficulty.put("confidenceDifficulty", confidencedifficulty);
    // Saves the difficulty
    Users.saveUser();
  }

  // Adds the word and time to the list
  public static void addWordHistory(String word) {
    Users.wordHistory.add(word);
  }

  /**
   * addTimeHistory will add how long the player took to their history stats
   *
   * @param time is the time remaining
   * @param word is the word they were drawing
   */
  public static void addTimeHistory(int time, String word) {
    // Calculate time difference
    int solveTime;
    if (time != 0) {
      // Check if game mode is blitz
      if (GameSelectController.getCurrentGameMode() == GameMode.BLITZ) {
        // get the last blitz time minus current time
        solveTime = Game.getBlitzTime() - time;
      } else {
        solveTime = CategorySelector.getTime() - time;
      }
    } else {
      // Set the solve time to 0
      solveTime = time;
    }
    Users.timeHistory.add((double) solveTime);
    checkFastestTime(solveTime, word);
  }

  // Getters and setter methods below.

  /**
   * setRecentUser will update who the most recent user is
   *
   * @param recentuser the most recent user who was selected
   */
  public static void setRecentUser(String recentuser) {
    // Set the recent user
    recentUser = recentuser;
    saveUserList();
  }

  /**
   * getWins will obtain the user's wins
   *
   * @return an integer on the amount of wins
   */
  public static int getWins() {
    return wins;
  }

  /**
   * getWins will obtain the user's losses
   *
   * @return an integer on the amount of losses
   */
  public static int getLosses() {
    return losses;
  }

  /**
   * getWordHistory will get all of user's words
   *
   * @return a list full of strings
   */
  public static List<String> getWordHistory() {
    return wordHistory;
  }

  /**
   * getFastestTime will obtain the user's fastest time
   *
   * @return an integer
   */
  public static int getFastestTime() {
    return fastestTime;
  }

  /**
   * getUserName will obtain the user's name
   *
   * @return a string
   */
  public static String getUserName() {
    return userName;
  }

  /**
   * getTimeHistory will get all of user's times
   *
   * @return a list full of doubles
   */
  public static List<Double> getTimeHistory() {
    return timeHistory;
  }

  /**
   * getFastestTime will obtain the user's fastest word
   *
   * @return a string
   */
  public static String getFastestWord() {
    return fastestWord;
  }

  /**
   * setFastestWord will update the user's fastest word
   *
   * @param fastestword a string
   */
  public static void setFastestWord(String fastestword) {
    Users.fastestWord = fastestword;
  }

  /**
   * getRecentList will find out the most recent player
   *
   * @return a string
   */
  public static String getRecentList() {
    return recentUser;
  }

  /**
   * getUserList will get a list of users
   *
   * @return a list full of strings
   */
  public static List<String> getUserList() {
    return userList;
  }

  /**
   * getProfilePicture will obtain the user's profile picture
   *
   * @return a string leading to the profile picture
   */
  public static String getProfilePicture() {
    return profilePicture;
  }

  /**
   * setProfilePicture will update the user's profile picture
   *
   * @param profilepicture takes in a string
   */
  public static void setProfilePicture(String profilepicture) {
    // Update the user's profile picture
    Users.profilePicture = profilepicture;
  }

  /**
   * Adds the win to the winhistory array
   *
   * @param win takes in if user won or not
   */
  public static void addWinHistory(Boolean win) {
    winHistory.add(win);
  }

  /**
   * To get each difficulty, do Map.get(difficult) e.g. Map.get("timedifficulty") to get the time
   * difficulty
   *
   * @return a string of the player's difficulty
   */
  public static String getIndividualDifficulty(String difficulty) {
    return gameDifficulty.get(difficulty);
  }

  public static Map<String, String> getIndividualDifficulty() {
    return gameDifficulty;
  }

  public static Map<String, Map<String, Boolean>> getBadges() {
    return badges;
  }

  public static void getIndividualBadge(String badgecategory, String badgelevel) {
    Users.badges.get(badgecategory).get(badgelevel);
  }

  public static int getConsistentWins() {
    return consistentWins;
  }

  public static void setConsistentWins(int consistentWins) {
    Users.consistentWins = consistentWins;
  }

  public static List<String> getAccuracyDifficultyHistory() {
    return accuracyDifficultyHistory;
  }

  public static List<String> getConfidenceDifficultyHistory() {
    return confidenceDifficultyHistory;
  }

  public static List<String> getTimeDifficultyHistory() {
    return timeDifficultyHistory;
  }

  public static List<String> getWordDifficultyHistory() {
    return wordDifficultyHistory;
  }

  public static List<Boolean> getWinHistory() {
    return winHistory;
  }
}
