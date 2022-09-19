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

  public enum Difficulty {
    E,
    M,
    H,
    MS
  }

  private Map<Difficulty, List<String>> difficulty2categories;

  public CategorySelector() throws IOException, URISyntaxException, CsvException {
    difficulty2categories = new HashMap<>();
    for (Difficulty difficulty : Difficulty.values()) {
      difficulty2categories.put(difficulty, new ArrayList<>());
    }
    for (String[] line : getLines()) {
      difficulty2categories.get(Difficulty.valueOf(line[1])).add(line[0]);
    }
  }

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

  public String getEasyMediumHardCategory() {
    int number = new Random().nextInt(3);
    if (number == 0) {
      return getRandomCategory(Difficulty.E);
    } else if (number == 1) {
      return getRandomCategory(Difficulty.M);
    } else {
      return getRandomCategory(Difficulty.H);
    }
  }

  public String getMasterCategory() {
    return getRandomCategory(Difficulty.H);
  }

  public HashMap<Difficulty, String> getSelection() {
    HashMap<Difficulty, String> selection = new HashMap<Difficulty, String>();
    selection.put(Difficulty.E, getEasyCategory());
    selection.put(Difficulty.M, getEasyMediumCategory());
    selection.put(Difficulty.H, getEasyMediumHardCategory());
    selection.put(Difficulty.MS, getMasterCategory());
    return selection;
  }

  protected List<String[]> getLines() throws IOException, CsvException, URISyntaxException {
    File fileName =
        new File(CategorySelector.class.getResource("/category_difficulty.csv").toURI());

    try (FileReader fr = new FileReader(fileName, StandardCharsets.UTF_8);
        CSVReader reader = new CSVReader(fr)) {
      return reader.readAll();
    }
  }
}
