package nz.ac.auckland.se206;

import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import nz.ac.auckland.se206.GameSelectController.GameMode;
import nz.ac.auckland.se206.dict.DictionaryLookup;
import nz.ac.auckland.se206.dict.WordEntry;
import nz.ac.auckland.se206.dict.WordInfo;
import nz.ac.auckland.se206.dict.WordNotFoundException;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

/**
 * Controls the game logic through a reusable service. <br>
 * Keeps track of the timer, prompt, and difficulty variables of the game.<br>
 * Runs the prediction model to determine win condition.
 */
public class Game extends SoundsController {

  public enum Indicator {
    CLOSER,
    FURTHER,
    SAME,
    NOT_FOUND
  }

  // Declare difficulty field
  private static DoodlePrediction model;
  private static Difficulty difficulty;
  private static boolean textToSpeech = true;
  private static boolean spoken = false;
  private static int blitzCounter;
  private static int blitzTime = CategorySelector.getTime();

  /**
   * createModel will create the doodle prediction model
   *
   * @throws ModelException model not found
   * @throws IOException read write error
   */
  public static void createModel() throws ModelException, IOException {
    // Creates the model
    model = new DoodlePrediction();
  }

  /**
   * getTextToSpeech is getting the text to speech boolean
   *
   * @return a boolean either true or false
   */
  public static boolean getTextToSpeech() {
    return textToSpeech;
  }

  /** Swaps textToSpeech's boolean from true to false or from false to true */
  public static void toggleTextToSpeech() {
    textToSpeech = !textToSpeech;
    spoken = !spoken;
  }

  /** resetBlitzCounter will reset the the counter after the game ends */
  public static void resetBlitzCounter() {
    blitzCounter = 0;
  }

  public static int getBlitzCounter() {
    return blitzCounter;
  }

  /** resetBlitzTime will reset the the counter after the game ends */
  public static void resetBlitzTime() {
    blitzTime = CategorySelector.getTime();
  }

  public static int getBlitzTime() {
    return blitzTime;
  }

  public static Difficulty getDifficulty() {
    return difficulty;
  }

  // Declare fields used in the game

  private CanvasController canvas;
  private HashMap<Difficulty, String> currentSelection;
  private StringProperty currentPrompt = new SimpleStringProperty(" ");
  private String currentWord;
  private String definition;
  private IntegerProperty timer;
  private int gameTime;
  private int topMatch;
  private float confidence;
  private boolean hasWon = false;
  private GameMode currentGame;
  private boolean isGhostGame;
  private int currentPos = 100;
  private int newPos;

  private Service<Void> ttsService =
      new Service<Void>() {
        protected Task<Void> createTask() {
          return new Task<Void>() {
            protected Void call() throws InterruptedException {
              // Initialise a text to speech instance
              TextToSpeech textToSpeech = new TextToSpeech();
              // Run indefinitely
              while (true) {
                if (Game.getTextToSpeech()) {
                  // When starting speak that its starting (Unless ghost game)
                  if (timer.get() == gameTime - 1 && !hasWon && !isGhostGame) {
                    textToSpeech.speak("Starting");
                  } else if (timer.get() == (gameTime / 2) + 1) {
                    // When half way speak thats it is halfway
                    textToSpeech.speak(
                        String.format("%s Seconds Remaining"), Integer.toString(gameTime / 2));
                  }
                  // Speak if the person has won
                  if (hasWon && !spoken) {
                    textToSpeech.speak("You Won!");
                    // Set that it has spoken
                    spoken = true;

                  } else if (timer.get() == 0 && !spoken) {
                    // Speak if the person has lost
                    textToSpeech.speak("YOU LOST!");
                    // Set that it has spoken
                    spoken = true;
                  }
                  // Sleep for 10 ms
                  Thread.sleep(10);
                }
              }
            }
          };
        }
      };

  // Initialise a service routine
  private Service<Void> service =
      new Service<Void>() {
        // Create the task to handle the game
        protected Task<Void> createTask() {
          // Main game loop thread
          return new Task<Void>() {
            protected Void call() throws InterruptedException, TranslateException {

              // Check that the timer is running
              while (timer == null || timer.intValue() > 1) {
                // Wait 1 second
                Thread.sleep(1000);
                Platform.runLater(
                    () -> {
                      // For all game modes other than zen
                      if (currentGame != GameMode.ZEN) {
                        // Decrement timer
                        timer.set(timer.get() - 1);
                        // Decrement timer bar
                        canvas.decrementTimerBar();
                      }

                      try {
                        // Check if the player is currently drawing
                        if (canvas.getIsDrawing()) {
                          // Get the top 10 predictions
                          List<Classifications.Classification> currentPredictions =
                              model.getPredictions(canvas.getCurrentSnapshot(), 100);
                          // Update the predictions
                          canvas.updatePredictionGridDisplay(currentPredictions);
                          updateIndicator(currentPredictions);
                          for (int i = 0; i < topMatch; i++) {
                            // Check if the top words are what we are drawing based on difficulty
                            if (currentPredictions.get(i).getProbability() > confidence
                                && getCurrentWord()
                                    .equals(
                                        currentPredictions
                                            .get(i)
                                            .getClassName()
                                            .replace("_", " "))) {
                              Users.addTimeHistory(timer.getValue().intValue(), getCurrentWord());
                              // Check if playing BLITZ
                              if (currentGame == GameMode.BLITZ) {
                                onDingEffect(null);
                                blitzTime = timer.getValue().intValue();
                                currentSelection = CategorySelector.getWordSelection();
                                String word =
                                    currentSelection.get(DifficultyBuilder.getWordsDifficulty());
                                // Reset the current prompt
                                setCurrentWord(word);
                                currentPrompt.setValue(word);
                                canvas.onClear();
                                // increment counter
                                blitzCounter++;
                              }
                              // End the game
                              if (!isGhostGame && currentGame != GameMode.BLITZ) {
                                hasWon = true;
                                endGame(true);
                              }
                            }
                          }
                        }
                      } catch (TranslateException | InterruptedException e) {
                        e.printStackTrace();
                      }
                    });
              }

              // End the game
              if (currentGame == GameMode.BLITZ && blitzCounter > 0) {
                endGame(true);
                return null;
              } else if (currentGame == GameMode.BLITZ && blitzCounter == 0) {
                endGame(false);
                return null;
              }
              System.out.println("LOST IN TASK");
              if (!isGhostGame) {
                endGame(false);
              }
              return null;
            }
          };
        }
      };

  /**
   * Game will set up a game based on the presets selected
   *
   * @param canvas takes in the canvas controller for the game
   * @throws IOException reading/writing exception
   * @throws URISyntaxException converting to link exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws ModelException doodle prediction exception
   * @throws WordNotFoundException word was not found exception
   */
  public Game(CanvasController canvas, GameMode gameMode)
      throws ModelException, IOException, WordNotFoundException {
    // Set the current game mode
    currentGame = gameMode;
    switch (currentGame) {
      case HIDDEN_WORD:
        setHiddenWordGame(canvas);
        break;
      case NORMAL:
        // Set the normal game
        setNormalGame(canvas);
        break;
      case ZEN:
        // Set the zen game
        setZenGame(canvas);
        break;
      case PROFILE:
        // Set the profile picture canvas
        setProfileCanvas(canvas);
        break;
      case BLITZ:
        // Set the blitz canvas
        setBlitzGame(canvas);
        break;
      default:
        // Default game set
        setNormalGame(canvas);
        break;
    }
    // Update the tool tip
    App.getCanvasController().updateToolTip();
  }

  private void setProfileCanvas(CanvasController canvas) {
    this.canvas = canvas;
    timer = new SimpleIntegerProperty(gameTime);
  }

  public boolean getIsGhostGame() {
    return isGhostGame;
  }

  /**
   * While a game is a ghost game, it cannot be won or lost. Use this method for pre-loading the
   * game services to avoid accidental wins or losses during loading.
   *
   * @param isGhost the value of isGhostGame to set
   */
  public void setIsGhostGame(boolean isGhost) {
    isGhostGame = isGhost;
  }

  /**
   * updateIndicator will update the the image and text to see how close or far the player is
   * getting
   *
   * @param predictions takes in the current predictions
   */
  private void updateIndicator(List<Classifications.Classification> predictions) {

    // Reset the new position
    newPos = -1;
    // Loop through the predictions to see if the current word is in there
    for (int i = 0; i < predictions.size(); i++) {
      if (getCurrentWord().equals(predictions.get(i).getClassName().replace("_", " "))) {
        // Get the index in the prediction list
        newPos = i;
        break;
      }
    }

    // Check if an index was found
    if (newPos == -1) {
      App.getCanvasController().updateIndicator(Indicator.NOT_FOUND);

    } else if (newPos == currentPos) {
      // Check if the position is the same
      App.getCanvasController().updateIndicator(Indicator.SAME);
    } else if (newPos < currentPos) {
      // Check if the player is closer to winning
      App.getCanvasController().updateIndicator(Indicator.CLOSER);
    } else if (currentPos < newPos) {
      App.getCanvasController().updateIndicator(Indicator.FURTHER);
    }
    // update the current position
    currentPos = newPos;
  }

  /**
   * setNormalGame will set up the backend of the normal game
   *
   * @param canvas takes in the normal canvas
   * @throws ModelException doodle prediction exception
   * @throws IOException reading/writing exception
   */
  private void setNormalGame(CanvasController canvas) throws ModelException, IOException {
    // Set the canvas
    this.canvas = canvas;
    // Get the current difficulty's word
    currentSelection = CategorySelector.getWordSelection();
    String word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
    gameTime = CategorySelector.getTime();
    topMatch = CategorySelector.getAccuracy();
    confidence = ((float) CategorySelector.getConfidence() / 100);
    timer = new SimpleIntegerProperty(gameTime);
    setCurrentWord(word);
    currentPrompt.setValue(word);
  }

  /**
   * setBlitzGame will set up the backend of the blitz game
   *
   * @param canvas takes in the normal canvas
   * @throws ModelException doodle prediction exception
   * @throws IOException reading/writing exception
   */
  private void setBlitzGame(CanvasController canvas) throws ModelException, IOException {
    // Set the canvas
    this.canvas = canvas;
    // Get the current difficulty's word
    currentSelection = CategorySelector.getWordSelection();
    String word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
    gameTime = CategorySelector.getTime();
    topMatch = CategorySelector.getAccuracy();
    confidence = ((float) CategorySelector.getConfidence() / 100);
    timer = new SimpleIntegerProperty(gameTime);
    setCurrentWord(word);
    currentPrompt.setValue(word);
  }

  /**
   * setZenGame will set up the backend of the zen game
   *
   * @param canvas takes in the normal canvas
   * @throws ModelException doodle prediction exception
   * @throws IOException reading/writing exception
   */
  private void setZenGame(CanvasController canvas) throws ModelException, IOException {
    // Set the canvas
    this.canvas = canvas;
    // Get the current difficulty's word
    currentSelection = CategorySelector.getWordSelection();
    String word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
    // Set the difficulty settings
    timer = null;
    topMatch = 0;
    confidence = 1;
    setCurrentWord(word);
    currentPrompt.setValue(word);
  }

  /**
   * setHiddenWordGame will set up the backend of the hidden word game
   *
   * @param canvas takes in the normal canvas
   * @throws ModelException doodle prediction exception
   * @throws IOException reading/writing exception
   */
  private void setHiddenWordGame(CanvasController canvas) throws ModelException, IOException {
    // Initialize variables
    WordInfo wordResult = null;
    String word;
    // Set the canvas
    this.canvas = canvas;
    gameTime = CategorySelector.getTime();
    topMatch = CategorySelector.getAccuracy();
    confidence = ((float) CategorySelector.getConfidence() / 100);
    timer = new SimpleIntegerProperty(gameTime);
    while (true) {
      // Get the current difficulty's word
      currentSelection = CategorySelector.getWordSelection();
      word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
      try {
        // Look up the word in dictionary
        wordResult = DictionaryLookup.searchWordInfo(word);
        break;
      } catch (WordNotFoundException e) {
        System.out.println("No word definition was found!");
        continue;
      }
    }
    setCurrentWord(word);
    List<WordEntry> entries = wordResult.getWordEntries();
    definition = entries.get(0).getDefinitions().get(0);
    currentPrompt.setValue("Guess the word then draw it!");
  }

  /**
   * Changes the difficulty of the game by selecting the prompt from that difficulty as the current
   * prompt.
   *
   * @param newDifficulty The Difficulty to change the game setting to
   */
  public void setWordDifficulty(Difficulty newDifficulty) {
    difficulty = newDifficulty;
    currentPrompt.setValue(currentSelection.get(difficulty));
  }

  public void setCurrentPrompt(String prompt) {
    currentPrompt.setValue(prompt);
  }

  public StringProperty getCurrentPromptProperty() {
    return currentPrompt;
  }

  public String getCurrentPrompt() {
    return currentPrompt.get();
  }

  public int getTimeRemaining() {
    return timer.get();
  }

  public String getDefinition() {
    return definition;
  }

  public StringBinding getTimeRemainingAsStringBinding() {
    return timer.asString();
  }

  public String getCurrentWord() {
    return currentWord;
  }

  public void setCurrentWord(String currentWord) {
    this.currentWord = currentWord;
  }

  /** startGame will initialize the game */
  public void startGame() {
    // Set these fields to false so that tts only speaks once
    spoken = false;
    hasWon = false;
    service.start();
    ttsService.start();
  }

  /**
   * Useful for restarting the game without creating a new Game object. <br>
   * Resets the concurrent game service. Resets the timer. Gets a new selection of prompts.
   */
  public void resetGame() {
    ttsService.cancel();
    service.cancel();

    resetTimer(difficulty);
  }

  /**
   * Resets the game's timer
   *
   * @param difficulty takes in a difficulty enum
   */
  public void resetTimer(Difficulty difficulty) {
    // Check the current game mode
    if (currentGame != GameMode.ZEN && currentGame != GameMode.PROFILE) {
      timer.set(CategorySelector.getTime());
    }
  }

  /**
   * endGame will finish the game correctly
   *
   * @param isWin checks if the user has won
   * @throws InterruptedException interrupt exception stops the function
   */
  private void endGame(boolean isWin) throws InterruptedException {
    // Cancel service and end game
    canvas.onEndGame(isWin);
    service.cancel();
  }
}
