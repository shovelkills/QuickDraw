package nz.ac.auckland.se206.testing;

import java.util.HashMap;
import java.util.Map;
import nz.ac.auckland.se206.Users;

public class Testing {

  private static Map<String, Map<String, Boolean>> badges;

  public static void main(String[] args) {

    // testLoadingJSONFile();
    // testCreatingJSONFile();
    // testDoesItContainSpecialCharacters();
    // testSavingToJSONFile();
    // testUserList();
    // testDeletingUser();
    // testDeleteProfilePicture();

    createBadges();
    System.out.println(getBadges().get("Accuracy").get("EASY"));
    getBadges().get("Accuracy").put("EASY", true);
    System.out.println(getBadges().get("Accuracy").get("EASY"));
  }

  private static void testDeleteProfilePicture() {
    Users.loadUser("thelegend27");
    Users.deleteProfilePicture("thelegend27");
  }

  public static void testLoadingJSONFile() {

    // Test if it detects special characters
    Users.loadUser("/.");
    // Test if file not found
    Users.loadUser("XXXezclapXXX");
    // Testing loading the JSON file
    Users.loadUser("thelegend27");
    System.out.println("Fastest time " + Users.getFastestTime());
    System.out.println("Wins " + Users.getWins());
    System.out.println("Username " + Users.getUserName());
    System.out.println("Losses " + Users.getLosses());
    System.out.println("Word History " + Users.getWordHistory());
    System.out.println("Fastest word " + Users.getFastestWord());
    System.out.println("Time History" + Users.getTimeHistory());
  }

  public static void testCreatingJSONFile() {

    // Note - do not use special characters when testing
    Users.createUser("//"); // Error in creating due to special characters
    Users.createUser("thelegend27"); // Error for first one as there is already a file with same
    // name
    Users.createUser("thelegend28"); // Note - must delete file after running, might have to refresh
    // users folder to see new created file
    System.out.println("Username " + Users.getUserName());
  }

  public static void testDoesItContainSpecialCharacters() {
    // Comes up true as it contains special characters
    System.out.println(Users.isValidUsername(";./,'{}[]"));
    // Comes up false as it does not contain special characters
    System.out.println(
        Users.isValidUsername("qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM-"));
  }

  public static void testSavingToJSONFile() {
    // Loads users and increases the wins by 2
    Users.loadUser("xXXcoolguy45XXx");
    // Shows wins before increasing wins
    System.out.println("Wins " + Users.getWins());
    Users.increaseWins();
    System.out.println("Wins " + Users.getWins());
    // Check file to see if the wins have changed
    Users.saveUser();
    // Increases win again
    Users.increaseWins();
    System.out.println("Wins " + Users.getWins());
    Users.saveUser();
  }

  public static void testUserList() {
    // Lists to test on
    // {
    // "recentList": ["thelegend27", "widepeepoHappy", "xXXcoolguy45XXx", "", ""],
    // "userList": ["sadge", "thelegend27", "widepeepoHappy", "xXXcoolguy45XXx", "blank"]
    // }
    Users.loadUsersFromList();
    System.out.println("Users" + Users.getUserList());
    System.out.println("Recent" + Users.getRecentList());
    Users.loadUser("widepeepoHappy");
    System.out.println("Recent" + Users.getRecentList());
    Users.loadUser("sadge");
    System.out.println("Recent" + Users.getRecentList());
    // Makes sure to delete JSON file when testing again
    Users.createUser("GIGACHAD");
    System.out.println("Users" + Users.getUserList());
    System.out.println("Recent" + Users.getRecentList());
  }

  public static void testDeletingUser() {
    Users.loadUsersFromList();
    Users.createUser("BroBait");
    System.out.println(Users.getUserList());
    System.out.println(Users.getUserName());
    Users.deleteUser("BroBait");
    System.out.println(Users.getUserList());
  }

  public static void createBadges() {
    Map<String, Map<String, Boolean>> badgeList = new HashMap<>();
    Map<String, Boolean> difficulty = new HashMap<>();
    // Generates the badges
    difficulty.put("EASY", false);
    difficulty.put("MEDIUM", false);
    difficulty.put("HARD", false);
    badgeList.put("Accuracy", difficulty);
    difficulty.put("MASTER", false);
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
    badges = badgeList;
  }

  public static Map<String, Map<String, Boolean>> getBadges() {
    return badges;
  }
}
