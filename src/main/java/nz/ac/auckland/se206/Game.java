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
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

/**
 * Controls the game logic through a reusable service. <br>
 * Keeps track of the timer, prompt, and difficulty variables of the game.<br>
 * Runs the prediction model to determine win condition.
 */
public class Game {
  // Declare difficulty field
  private static Difficulty difficulty;

  public static Difficulty getDifficulty() {
    return difficulty;
  }

  // Declare fields used in the game
  private DoodlePrediction model;
  private CanvasController canvas;
  private HashMap<Difficulty, String> currentSelection;
  private StringProperty currentPrompt = new SimpleStringProperty(" ");
  private IntegerProperty timer;
  private int gameTime;
  private int topMatch;
  private float confidence;
  private boolean spoken = false;
  private boolean hasWon = false;
  private GameMode currentGame;

  // Initialise the speech task
  private Task<Void> speechTask =
      new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          // Initialise a text to speech instance
          TextToSpeech textToSpeech = new TextToSpeech();
          // Run indefinitely
          while (true) {
            // When starting speak that its starting
            if (timer.get() == gameTime - 1) {
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
                              model.getPredictions(canvas.getCurrentSnapshot(), 10);
                          // Update the predictions
                          canvas.updatePredictionGridDisplay(currentPredictions);
                          for (int i = 0; i < topMatch; i++) {
                            // Check if the top words are what we are drawing based on difficulty
                            if (currentPredictions.get(i).getProbability() > confidence
                                && getCurrentPrompt()
                                    .equals(
                                        currentPredictions
                                            .get(i)
                                            .getClassName()
                                            .replace("_", " "))) {
                              Users.addTimeHistory(timer.getValue().intValue(), getCurrentPrompt());
                              // End the game
                              hasWon = true;
                              endGame(true);
                              return;
                            }
                          }
                        }
                      } catch (TranslateException | InterruptedException e) {
                        e.printStackTrace();
                      }
                    });
              }
              System.out.println("LOST IN TASK");
              Users.addTimeHistory(0, getCurrentPrompt());
              // End the game
              endGame(false);
              return null;
            }
          };
        }
        ;
      };

  /**
   * Game will set up a game based on the presets selected
   *
   * @param canvas takes in the canvas controller for the game
   * @throws IOException reading/writing exception
   * @throws URISyntaxException converting to link exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws ModelException doodle prediction exception
   */
  public Game(CanvasController canvas, GameMode gameMode) throws ModelException, IOException {
    // Set the current game mode
    currentGame = gameMode;
    switch (gameMode) {
      case DEFINITION:
        // TODO add in hidden word gamemode
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
      default:
        // Default game set
        setNormalGame(canvas);
        break;
    }
  }

  private void setProfileCanvas(CanvasController canvas) {
    this.canvas = canvas;
    timer = new SimpleIntegerProperty(gameTime);
  }

  private void setNormalGame(CanvasController canvas) throws ModelException, IOException {
    // Set the canvas
    this.canvas = canvas;
    model = new DoodlePrediction();
    // Get the current difficulty's word
    currentSelection = CategorySelector.getWordSelection();
    String word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
    gameTime = CategorySelector.getTime();
    topMatch = CategorySelector.getAccuracy();
    confidence = ((float) CategorySelector.getConfidence() / 100);
    timer = new SimpleIntegerProperty(gameTime);
    currentPrompt.setValue(word);
    // Start the text to speech thread
    Thread ttsThread = new Thread(speechTask);
    ttsThread.setDaemon(true);
    ttsThread.start();
  }

  private void setZenGame(CanvasController canvas) throws ModelException, IOException {
    // Set the canvas
    this.canvas = canvas;
    model = new DoodlePrediction();
    // Get the current difficulty's word
    currentSelection = CategorySelector.getWordSelection();
    String word = currentSelection.get(DifficultyBuilder.getWordsDifficulty());
    // Set the difficulty settings
    timer = null;
    topMatch = 0;
    confidence = 1;
    currentPrompt.setValue(word);
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

  public StringProperty getCurrentPromptProperty() {
    return currentPrompt;
  }

  public String getCurrentPrompt() {
    return currentPrompt.get();
  }

  public int getTimeRemaining() {
    return timer.get();
  }

  public StringBinding getTimeRemainingAsStringBinding() {
    return timer.asString();
  }

  /** startGame will initialize the game */
  public void startGame() {
    // Set these fields to false so that tts only speaks once
    spoken = false;
    hasWon = false;
    service.start();
  }

  /**
   * Useful for restarting the game without creating a new Game object. <br>
   * Resets the concurrent game service. Resets the timer. Gets a new selection of prompts.
   */
  public void resetGame() {
    // Check if the service is running
    if (service.isRunning()) {
      service.cancel();
    }
    // Reset the service so the game is reset
    service.reset();
    resetTimer(difficulty);
    // currentSelection = CategorySelector.getWordSelection();
  }

  /**
   * Resets the game's timer
   *
   * @param difficulty
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
    speechTask.cancel();
    service.cancel();
  }
}
