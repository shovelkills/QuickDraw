package nz.ac.auckland.se206;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Users {
  // Stores the information in the JSON file as individual variables

  private static int fastestTime;
  private static int losses;
  private static int wins;

  private static List<Double> timeHistory;
  private static List<String> userList;
  private static List<String> wordHistory;

  private static Map<?, ?> userInfo;
  private static Map<String, Map<String, Boolean>> badges;
  private static Map<String, String> gameDifficulty;

  private static String fastestWord;
  private static String folderDirectory = System.getProperty("user.dir");
  private static String profilePicture;

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
      setRecentUser(username);
      return;
    }
    if (isValidUsername(username)) {
      // Change to show in GUI
      System.err.println("You cannot have special characters in username");
      return;
    }
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

      wordHistory = (List<String>) userInfo.get("wordHistory");

      timeHistory = (List<Double>) userInfo.get("timeHistory");

      gameDifficulty = (Map<String, String>) userInfo.get("gameDifficulty");

      badges = (Map<String, Map<String, Boolean>>) userInfo.get("Badges");

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
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("username", username);
      userMap.put("gamesWon", 0);
      userMap.put("gamesLost", 0);
      userMap.put("wordHistory", new ArrayList<String>());
      userMap.put("fastestGame", -1);
      userMap.put("fastestWord", " ");
      userMap.put("timeHistory", new ArrayList<Double>());

      // Creates the default difficulty
      Map<String, String> difficulty = new HashMap<>();
      difficulty.put("accuracyDifficulty", "easy");
      difficulty.put("wordsDifficulty", "easy");
      difficulty.put("timeDifficulty", "easy");
      difficulty.put("confidenceDifficulty", "easy");
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
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
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
    Map<String, Map<String, Boolean>> badgeList = new HashMap<>();
    Map<String, Boolean> difficulty = new HashMap<>();
    // Generates the badges
    difficulty.put("Easy", false);
    difficulty.put("Medium", false);
    difficulty.put("Hard", false);
    badgeList.put("Accuracy", difficulty);
    difficulty.put("Master", false);
    // Adds to badge list based of difficulty
    badgeList.put("Words", difficulty);
    badgeList.put("Time", difficulty);
    badgeList.put("Confidence", difficulty);
    badgeList.put("All difficulties", difficulty);
    // Generate hashMap for badges based on time wins
    Map<String, Boolean> timedWins = new HashMap<>();
    timedWins.put("10 Seconds", false);
    timedWins.put("30 Seconds", false);
    timedWins.put("Last Second", false);
    badgeList.put("Timed Wins", timedWins);
    // Generate hashMap for badges based on Wins
    Map<String, Boolean> wins = new HashMap<>();
    wins.put("First Win", false);
    wins.put("2 consecutive wins", false);
    wins.put("5 consecutive wins", false);
    badgeList.put("Wins", wins);
    // Generates hashMap for badges on Misc actions
    Map<String, Boolean> misc = new HashMap<>();
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
   *         special characters in the string
   */
  public static boolean isValidUsername(String username) {

    Pattern validCharacters = Pattern.compile("[^a-z0-9-]", Pattern.CASE_INSENSITIVE);
    Matcher matchCharacters = validCharacters.matcher(username);
    return matchCharacters.find();
  }

  /**
   * Gets the sum of the remain time that the user has to draw each word
   *
   * @return the average time remaining that it user has when the game recognizes the word
   */
  public static double getAverageTime() {
    double sum = 0;
    // Checks if the list is empty and finds the average time
    if (!timeHistory.isEmpty()) {
      for (Double time : timeHistory) {
        sum += time;
      }
      return sum / timeHistory.size();
    }
    return sum;
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
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("username", userName);
      userMap.put("gamesWon", wins);
      userMap.put("gamesLost", losses);
      userMap.put("wordHistory", wordHistory);
      userMap.put("fastestGame", fastestTime);
      userMap.put("fastestWord", fastestWord);
      userMap.put("timeHistory", timeHistory);
      userMap.put("gameDifficulty", gameDifficulty);
      userMap.put("Badges", badges);

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
    File dir = new File(folderDirectory + "/src/main/resources/users/");
    try (Writer writer = new FileWriter(new File(dir, "userlist.json"))) {
      Map<String, Object> saveToJson = new HashMap<>();
      saveToJson.put("recentUser", recentUser);
      saveToJson.put("userList", userList);
      // Creates a writer object to write and save the file
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(saveToJson, writer);
      writer.close();

    } catch (IOException e) {
      // TODO Auto-generated catch block
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
   * @return the image of the profile picture
   */
  public static Image loadProfilePicture(String user) {
    File profileFile =
        new File(folderDirectory + "/src/main/resources/profilepictures/" + user + ".bmp");
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
        new BufferedImage(snapshot.getWidth(), snapshot.getHeight(), BufferedImage.TYPE_INT_RGB);

    final Graphics2D graphics = imageBinary.createGraphics();

    graphics.drawImage(snapshot, 0, 0, null);

    // To release memory we dispose.
    graphics.dispose();

    // Write it to file
    File file =
        new File(folderDirectory + "/src/main/resources/profilepictures/" + userName + ".bmp");
    try {
      ImageIO.write(imageBinary, "bmp", file);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Deletes the profile picture of the user */
  public static void deleteProfilePicture(String user) {
    File file = new File(folderDirectory + "/src/main/resources/profilepictures/" + user + ".bmp");
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
    fastestWord = guestPlayer.getFastestWord();
    wins = guestPlayer.getWins();
    losses = guestPlayer.getLosses();
    fastestTime = guestPlayer.getFastestTime();
    wordHistory = guestPlayer.getWordHistroy();
    timeHistory = guestPlayer.getTimeHistory();
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
    if (time <= Users.fastestTime || (Users.fastestTime == -1 && time != 60)) {
      Users.fastestTime = time;
      setFastestWord(word);
    }
    addWordHistory(word);
    // Checks if the user is a guest
    if (recentUser == "Guest") {
      guestPlayer.saveGuest();
    }
  }

  // updates the userList by adding new user to list
  public static void addUserList(String user) {
    userList.add(user);
    // Save the user List
    saveUserList();
  }

  // Increases current user's wins by 1
  public static void increaseWins() {
    Users.wins++;
  }

  // Increases current user's losses by 1
  public static void increaseLosses() {
    Users.losses++;
  }

  /**
   * Sets the difficulty of each aspect of the game to the most recent difficulties
   * 
   * @param accuracydifficulty the accuracy difficulty
   * @param worddifficulty the word difficulty
   * @param timedifficulty the time difficulty
   * @param confidencedifficulty the confidence difficulty
   */
  public static void setGameDifficulty(String accuracydifficulty, String worddifficulty,
      String timedifficulty, String confidencedifficulty) {
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

  public static void addTimeHistory(int time, String word) {
    int solvetime = 60 - time;
    Users.timeHistory.add((double) solvetime);
    checkFastestTime(solvetime, word);
  }

  // Getters and setter methods below.

  public static void setRecentUser(String recentuser) {
    recentUser = recentuser;
    saveUserList();
  }

  public static int getWins() {
    return wins;
  }

  public static int getLosses() {
    return losses;
  }

  public static List<String> getWordHistory() {
    return wordHistory;
  }

  public static int getFastestTime() {
    return fastestTime;
  }

  public static String getUserName() {
    return userName;
  }

  public static List<Double> getTimeHistory() {
    return timeHistory;
  }

  public static String getFastestWord() {
    return fastestWord;
  }

  public static void setFastestWord(String fastestWord) {
    Users.fastestWord = fastestWord;
  }

  public static String getRecentList() {
    return recentUser;
  }

  public static List<String> getUserList() {
    return userList;
  }

  public static String getProfilePicture() {
    return profilePicture;
  }

  public static void setProfilePicture(String profilePicture) {
    Users.profilePicture = profilePicture;
  }

  /**
   * To get each difficulty, do Map.get(difficult) e.g. Map.get("timedifficulty") to get the time
   * difficulty
   * 
   * @return
   */
  public static Map<String, String> getGameDifficulty() {
    return gameDifficulty;
  }

  public static Map<String, Map<String, Boolean>> getBadges() {
    return badges;
  }

}
