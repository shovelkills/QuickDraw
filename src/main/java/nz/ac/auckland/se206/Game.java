package nz.ac.auckland.se206;

import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.words.CategorySelector;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class Game {
  private static Difficulty difficulty;
  private DoodlePrediction model;
  private CanvasController canvas;
  private HashMap<Difficulty, String> currentSelection;
  private String currentPrompt;
  private int timer = 60;
  private CategorySelector cs;
  private boolean isWin;

  public Game(CanvasController canvas)
      throws IOException, URISyntaxException, CsvException, ModelException {
    this.canvas = canvas;
    difficulty = Difficulty.E;
    cs = new CategorySelector();
    model = new DoodlePrediction();
    currentSelection = cs.getSelection();
    currentPrompt = currentSelection.get(difficulty);
  }

  public static Difficulty getDifficulty() {
    return difficulty;
  }

  public int getTimeRemaining() {
    return timer;
  }

  /**
   * Changes the difficulty of the game by selecting the prompt from that difficulty as the current
   * prompt.
   *
   * @param newDifficulty The Difficulty to change the game setting to
   */
  public void setDifficulty(Difficulty newDifficulty) {
    difficulty = newDifficulty;
    currentPrompt = currentSelection.get(difficulty);
  }

  public String getCurrentPrompt() {
    return currentPrompt;
  }

  public void startGame() {
	  service.reset();
	  service.start();
  }

  public void refreshSelection() {
    currentSelection = cs.getSelection();
  }

  private Service<Void> service =
      new Service<Void>() {
        protected Task<Void> createTask() {
          return new Task<Void>() {
            protected Void call() throws InterruptedException {
              timer = 1;
              while (timer >= 0) {
                Platform.runLater(
                    () -> {
                      try {
                        if (canvas.getIsDrawing()) {
                          List<Classifications.Classification> currentPredictions =
                              model.getPredictions(canvas.getCurrentSnapshot(), 10);
                          canvas.updatePredictionGridDisplay(currentPredictions);
                          for (int i = 0; i < 3; i++) {
                            if (currentPrompt.equals(currentPredictions.get(i).getClassName().replace("_", " "))) {
                              endGame(true);
                              return;
                            }
                          }
                        }
                        canvas.updateTimerLabel();
                      } catch (TranslateException | InterruptedException e) {
                        e.printStackTrace();
                      }
                    });
                Thread.sleep(1000);
                timer--;
              }
              endGame(false);
              return null;
            }
          };
        }
        ;
      };

  private void endGame(boolean isWin) throws InterruptedException {
	service.cancel();
	canvas.onEndGame(isWin);
  }
}
