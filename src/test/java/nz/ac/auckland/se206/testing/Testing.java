package nz.ac.auckland.se206.testing;

import nz.ac.auckland.se206.Users;

public class Testing {

  public static void main(String[] args) {
    // Testing loading the JSON file
    Users.loadUser("thelegend27");
    System.out.println("Fastest time " + Users.getFastestTime());
    System.out.println("Wins " + Users.getWins());
    System.out.println("Username " + Users.getUserName());
    System.out.println("Losses " + Users.getLosses());
    System.out.println("Word History " + Users.getWordHistory());
  }

}
