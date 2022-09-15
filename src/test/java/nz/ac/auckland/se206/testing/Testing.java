package nz.ac.auckland.se206.testing;

import nz.ac.auckland.se206.Users;

public class Testing {

  public static void main(String[] args) {

    // testLoadingJSONFile();
    testCreatingJSONFile();
    // testDoesItContainSpecialCharacters();
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

  }

  public static void testCreatingJSONFile() {

    // Note - do not use special characters when testing
    Users.createUser("//");// Error in creating due to special characters
    Users.createUser("thelegend27");// Error for first one as there is already a file with same name
    Users.createUser("thelegend28");// Note - must delete file after running, might have to refresh
                                    // users folder to see new created file
    System.out.println("Username " + Users.getUserName());

  }

  public static void testDoesItContainSpecialCharacters() {
    // Comes up true as it contains special characters
    System.out.println(Users.isValidUsername(";./,'{}[]"));
    // Comes up false as it does not contain special characters
    System.out.println(
        Users.isValidUsername("qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM"));

  }

}
