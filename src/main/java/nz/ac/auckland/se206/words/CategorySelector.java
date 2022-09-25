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
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CategorySelector {

  // Global constnats of the difficulties
  public enum Difficulty {
    E,
    M,
    H,
    MS
  }

  private Map<Difficulty, List<String>> difficulty2categories;

  /**
   * CategorySelector will instantiate a category selector object to get words for the game
   *
   * @throws IOException exception to do with input output
   * @throws URISyntaxException reading and writing file exception
   * @throws CsvException reading spreadsheets exception
   */
  public CategorySelector() throws IOException, URISyntaxException, CsvException {
    // Declare a hashmap
    difficulty2categories = new HashMap<>();
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
   * getRandomCategory will get a random category's word
   *
   * @param difficulty takes in the game's difficulty
   * @return String: a word in that category or lower
   */
  public String getRandomCategory(Difficulty difficulty) {
    return difficulty2categories
        .get(difficulty)
        .get(new Random().nextInt(difficulty2categories.get(difficulty).size()));
  }

  public String getEasyCategory() {
    return getRandomCategory(Difficulty.E);
  }

  public String getEasyMediumCategory() {
    return getRandomCategory(new Random().nextInt(2) == 0 ? Difficulty.E : Difficulty.M);
  }

  /**
   * getEasyMediumHardCategory will get a word from any of the three categories
   *
   * @return
   */
  public String getEasyMediumHardCategory() {
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

  public String getMasterCategory() {
    return getRandomCategory(Difficulty.H);
  }

  /**
   * getSelection creates a hash map of a selection of words from different difficulties
   *
   * @return HashMap with different words based on their difficulty
   */
  public HashMap<Difficulty, String> getSelection() {
    // Initialises a new hash map with key difficulty and a word associated with it
    HashMap<Difficulty, String> selection = new HashMap<Difficulty, String>();
    // Stores the words with the difficulty
    selection.put(Difficulty.E, getEasyCategory());
    selection.put(Difficulty.M, getEasyMediumCategory());
    selection.put(Difficulty.H, getEasyMediumHardCategory());
    selection.put(Difficulty.MS, getMasterCategory());
    return selection;
  }

  /**
   * getLines will read the lines from the CSV file
   *
   * @return
   * @throws IOException
   * @throws CsvException
   * @throws URISyntaxException
   */
  protected List<String[]> getLines() throws IOException, CsvException, URISyntaxException {
    // Get the CSV File
    File fileName =
        new File(CategorySelector.class.getResource("/category_difficulty.csv").toURI());
    // Read in the word
    try (FileReader fr = new FileReader(fileName, StandardCharsets.UTF_8);
        CSVReader reader = new CSVReader(fr)) {
      return reader.readAll();
    }
  }
}
