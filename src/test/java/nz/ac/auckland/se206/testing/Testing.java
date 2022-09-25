package nz.ac.auckland.se206.testing;

import nz.ac.auckland.se206.Users;

public class Testing {

  public static void main(String[] args) {

    // testLoadingJSONFile();
    // testCreatingJSONFile();
    // testDoesItContainSpecialCharacters();
    // testSavingToJSONFile();
    // testUserList();
    // testDeletingUser();
    // testDeleteProfilePicture();
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
    System.out.println("Average Time " + Users.getAverageTime());
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
}
