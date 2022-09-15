package nz.ac.auckland.se206;

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

  /**
   * This method finds the local files stored for the users by inputting their username
   * 
   * @param username the username of the user
   */

  public static void loadUser(String username) {

    if (isValidUsername(username)) {
      // Change to show in GUI
      System.err.println("You cannot have special characters in username");
      return;
    }

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

  public static void createUser(String username) {
    if (isValidUsername(username)) {
      // Change to show in GUI
      System.err.println("You cannot have special characters in username");
      return;
    }

    folderDirectory = System.getProperty("user.dir");
    try (FileReader reader =
        new FileReader(folderDirectory + "/src/main/resources/users/" + username + ".json")) {
      // Change to show on GUI
      System.err.println("Username taken, try a different username");

    } catch (FileNotFoundException e) {
      // Creates a hashMap to add information to the JSON file
      Map<String, Object> userMap = new HashMap<>();
      userMap.put("username", username);
      userMap.put("gamesWon", 0);
      userMap.put("gamesLost", 0);
      userMap.put("wordHistory", new ArrayList<String>());
      userMap.put("fastestGame", 0);

      // Gets the file directory to save to and runs the load user method
      File dir = new File(folderDirectory + "/src/main/resources/users/");
      try {
        // Creates a writer object to write and save the file
        Writer writer = new FileWriter(new File(dir, username + ".json"));
        new Gson().toJson(userMap, writer);
        writer.close();
        loadUser(username);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }

  /**
   * Checks if there are any special characters in the username
   * 
   * @param username the username the user inputted
   * @return boolean of if there are special characters in the username. True means there are
   *         special characters
   */
  public static boolean isValidUsername(String username) {
    Pattern validCharacters = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
    Matcher matchCharacters = validCharacters.matcher(username);
    return matchCharacters.find();

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
