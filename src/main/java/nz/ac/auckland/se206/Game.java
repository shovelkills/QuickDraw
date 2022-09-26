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
  private IntegerProperty timer = new SimpleIntegerProperty(60);
  private boolean spoken = false;
  private boolean hasWon = false;

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
            if (timer.get() == 59) {
              textToSpeech.speak("Starting");
            } else if (timer.get() == 31) {
              // When half way speak thats it is halfway
              textToSpeech.speak("Thirty Seconds Remaining");
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
            protected Void call() throws InterruptedException {
              // Check that the timer is running
              while (timer.intValue() > 1) {
                // Wait 1 second
                Thread.sleep(1000);
                Platform.runLater(
                    () -> {
                      // Decrement timer
                      timer.set(timer.get() - 1);
                      // Decrement timer bar
                      canvas.decrementTimerBar();

                      try {
                        // Check if the player is currently drawing
                        if (canvas.getIsDrawing()) {
                          // Get the top 10 predictions
                          List<Classifications.Classification> currentPredictions =
                              model.getPredictions(canvas.getCurrentSnapshot(), 10);
                          // Update the predictions
                          canvas.updatePredictionGridDisplay(currentPredictions);
                          for (int i = 0; i < 3; i++) {
                            // Check if the top 3 words are what we are drawing
                            if (getCurrentPrompt()
                                .equals(
                                    currentPredictions.get(i).getClassName().replace("_", " "))) {
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

  public Game(CanvasController canvas)
      throws IOException, URISyntaxException, CsvException, ModelException {
    // Set the canvas
    this.canvas = canvas;
    // Set default difficulty to easy
    difficulty = Difficulty.E;
    model = new DoodlePrediction();
    // Get the current difficulty's word
    currentSelection = CategorySelector.getSelection();
    String word = currentSelection.get(difficulty);
    currentPrompt.setValue(word);
    // Start the text to speech thread
    Thread ttsThread = new Thread(speechTask);
    ttsThread.setDaemon(true);
    ttsThread.start();
  }

  /**
   * Changes the difficulty of the game by selecting the prompt from that difficulty as the current
   * prompt.
   *
   * @param newDifficulty The Difficulty to change the game setting to
   */
  public void setDifficulty(Difficulty newDifficulty) {
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
    service.reset();
    resetTimer(difficulty);
    currentSelection = CategorySelector.getSelection();
  }

  /**
   * Resets the game's timer
   *
   * @param difficulty
   */
  public void resetTimer(Difficulty difficulty) {
    timer.set(60);
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
