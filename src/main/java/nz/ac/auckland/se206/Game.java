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
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

/**
 * Controls the game logic through a reusable service. <br>
 * Keeps track of the timer, prompt, and difficulty variables of the game.<br>
 * Runs the prediction model to determine win condition.
 */
public class Game {
  private static Difficulty difficulty;
  private DoodlePrediction model;
  private CanvasController canvas;
  private HashMap<Difficulty, String> currentSelection;
  private StringProperty currentPrompt = new SimpleStringProperty(" ");
  private IntegerProperty timer = new SimpleIntegerProperty(60);
  private CategorySelector cs;

  public Game(CanvasController canvas)
      throws IOException, URISyntaxException, CsvException, ModelException {
    this.canvas = canvas;
    difficulty = Difficulty.E;
    cs = new CategorySelector();
    model = new DoodlePrediction();
    currentSelection = cs.getSelection();
    currentPrompt.setValue(currentSelection.get(difficulty));
  }

  public static Difficulty getDifficulty() {
    return difficulty;
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

  public void startGame() {
    service.start();
  }

  /**
   * Useful for restarting the game without creating a new Game object. <br>
   * Resets the concurrent game service. Resets the timer. Gets a new selection of prompts.
   */
  public void resetGame() {
    service.reset();
    resetTimer(difficulty);
    currentSelection = cs.getSelection();
  }

  /**
   * @param difficulty
   */
  public void resetTimer(Difficulty difficulty) {
    timer.set(60);
  }

  private Service<Void> service =
      new Service<Void>() {
        protected Task<Void> createTask() {
          return new Task<Void>() {
            protected Void call() throws InterruptedException {
              while (timer.intValue() > 1) {
                Thread.sleep(1000);
                Platform.runLater(
                    () -> {
                      timer.set(timer.get() - 1);

                      try {
                        if (canvas.getIsDrawing()) {
                          List<Classifications.Classification> currentPredictions =
                              model.getPredictions(canvas.getCurrentSnapshot(), 10);
                          canvas.updatePredictionGridDisplay(currentPredictions);
                          for (int i = 0; i < 3; i++) {
                            if (getCurrentPrompt()
                                .equals(
                                    currentPredictions.get(i).getClassName().replace("_", " "))) {
                              Users.addTimeHistory(timer.getValue().intValue(), getCurrentPrompt());
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
              endGame(false);
              return null;
            }
          };
        }
        ;
      };

  private void endGame(boolean isWin) throws InterruptedException {
    canvas.onEndGame(isWin);
    service.cancel();
  }
}
