package nz.ac.auckland.se206.words;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import nz.ac.auckland.se206.Users;

public class CategorySelector {

  // Global constnats of the difficulties
  public enum Difficulty {
    E,
    M,
    H,
    MS
  }

  private static Map<Difficulty, List<String>> difficulty2categories =
      new HashMap<Difficulty, List<String>>();

  /**
   * CategorySelector will instantiate a category selector object to get words for the game
   *
   * @throws IOException exception to do with input output
   * @throws URISyntaxException reading and writing file exception
   * @throws CsvException reading spreadsheets exception
   */
  public static void loadCategories() throws IOException, CsvException, URISyntaxException {
    // Declare a hashmap
    for (Difficulty difficulty : Difficulty.values()) {
      // Add all the difficulties in with empty lists
      difficulty2categories.put(difficulty, new ArrayList<>());
    }
    for (String[] line : getLines()) {
      // Place the words in the hash map
      difficulty2categories.get(Difficulty.valueOf(line[1])).add(line[0]);
    }
  }

  /**
   * getLines will read the lines from the CSV file
   *
   * @return
   * @throws IOException
   * @throws CsvException
   * @throws URISyntaxException
   */
  protected static List<String[]> getLines() throws IOException, CsvException, URISyntaxException {
    // Get the CSV File
    File fileName =
        new File(CategorySelector.class.getResource("/category_difficulty.csv").toURI());
    // Read in the word
    try (FileReader fr = new FileReader(fileName, StandardCharsets.UTF_8);
        CSVReader reader = new CSVReader(fr)) {
      return reader.readAll();
    }
  }

  private static boolean listCompare(List<String> list1, List<String> list2) {
    return new HashSet<>(list1).equals(new HashSet<>(list2));
  }

  /**
   * getRandomCategory will get a random category's word
   *
   * @param difficulty takes in the game's difficulty
   * @return String: a word in that category or lower
   */
  public static String getRandomCategory(Difficulty difficulty) {
    // Get the difficulty size
    int difficultySize = difficulty2categories.get(difficulty).size();
    // Set the random word to nothing
    String randomWord = null;
    List<String> usersWords = Users.getWordHistory();
    List<String> easyWords = difficulty2categories.get(Difficulty.E);
    if (usersWords == null || listCompare(usersWords, easyWords)) {
      return randomWord =
          difficulty2categories.get(difficulty).get(new Random().nextInt(difficultySize));
    }
    // Keep getting new word until the word is not in the user's history
    while (randomWord == null || usersWords.contains(randomWord)) {
      // Get new word
      randomWord = difficulty2categories.get(difficulty).get(new Random().nextInt(difficultySize));
    }
    // Return the word
    return randomWord;
  }

  public static String getEasyCategory() {
    return getRandomCategory(Difficulty.E);
  }

  public static String getEasyMediumCategory() {
    return getRandomCategory(new Random().nextInt(2) == 0 ? Difficulty.E : Difficulty.M);
  }

  /**
   * getEasyMediumHardCategory will get a word from any of the three categories
   *
   * @return
   */
  public static String getEasyMediumHardCategory() {
    // Choose a random category
    int number = new Random().nextInt(3);
    // Get the easy word
    if (number == 0) {
      return getRandomCategory(Difficulty.E);
    }
    // Get the medium word
    else if (number == 1) {
      return getRandomCategory(Difficulty.M);
    }
    // Get the hard word
    else {
      return getRandomCategory(Difficulty.H);
    }
  }

  public static String getMasterCategory() {
    return getRandomCategory(Difficulty.H);
  }

  /**
   * getSelection creates a hash map of a selection of words from different difficulties
   *
   * @return HashMap with different words based on their difficulty
   */
  public static HashMap<Difficulty, String> getSelection() {
    // Initialises a new hash map with key difficulty and a word associated with it
    HashMap<Difficulty, String> selection = new HashMap<Difficulty, String>();
    // Stores the words with the difficulty
    selection.put(Difficulty.E, getEasyCategory());
    selection.put(Difficulty.M, getEasyMediumCategory());
    selection.put(Difficulty.H, getEasyMediumHardCategory());
    selection.put(Difficulty.MS, getMasterCategory());
    return selection;
  }
}
