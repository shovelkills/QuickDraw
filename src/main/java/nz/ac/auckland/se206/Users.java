package nz.ac.auckland.se206;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;



public class Users {
  // Stores the information in the JSON file as individual variables

  private static int fastestTime;
  private static int losses;
  private static int wins;


  private static List<String> wordHistory;

  private static Map<?, ?> userInfo;

  private static String folderDirectory;// Gets the current directory of the folder
  private static String userName;

  public static void loadUser(String username) {

    // Creates a jsonParser to read the file
    folderDirectory = System.getProperty("user.dir");
    try (FileReader reader =
        new FileReader(folderDirectory + "/src/main/resources/users/" + username + ".json")) {
      // Reads the JSON file and casts and stores it as a Map
      Gson gson = new Gson();
      userInfo = gson.fromJson(reader, Map.class);

      // Add the information from JSON file to variables through casting
      userName = (String) userInfo.get("username");

      wins = (int) (double) userInfo.get("gamesWon");

      losses = (int) (double) userInfo.get("gamesLost");

      wordHistory = (List<String>) userInfo.get("wordHistory");

      fastestTime = (int) (double) userInfo.get("fastestGame");

    } catch (FileNotFoundException e) {
      System.err.println("User Not Found"); // Would change to show error on UI
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }


  // Getters and setter methods below
  public static int getWins() {
    return wins;
  }

  public static void setWins(int wins) {
    Users.wins = wins;
  }

  public static int getLosses() {
    return losses;
  }

  public static void setLosses(int losses) {
    Users.losses = losses;
  }

  public static List<String> getWordHistory() {
    return wordHistory;
  }

  public static void setWordHistory(List<String> wordHistory) {
    Users.wordHistory = wordHistory;
  }

  public static int getFastestTime() {
    return fastestTime;
  }

  public static void setFastestTime(int fastestTime) {
    Users.fastestTime = fastestTime;
  }


  public static String getUserName() {
    return userName;
  }



}
