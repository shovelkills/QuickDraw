package nz.ac.auckland.se206;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import com.opencsv.exceptions.CsvException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import nz.ac.auckland.se206.GameSelectController.GameMode;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.dict.WordNotFoundException;
import nz.ac.auckland.se206.words.CategorySelector;

/**
 * This is the controller of the canvas. You are free to modify this class and the corresponding
 * FXML file as you see fit. For example, you might no longer need the "Predict" button because the
 * DL model should be automatically queried in the background every second.
 *
 * <p>!! IMPORTANT !!
 *
 * <p>Although we added the scale of the image, you need to be careful when changing the size of the
 * drawable canvas and the brush size. If you make the brush too big or too small with respect to
 * the canvas size, the ML model will not work correctly. So be careful. If you make some changes in
 * the canvas and brush sizes, make sure that the prediction works fine.
 */
public class CanvasController {

  // Define FXML fields
  @FXML private Canvas canvas;
  @FXML private Label titleLabel;
  @FXML private Label wordLabel;
  @FXML private Label timerBarLabel;
  @FXML private Label timerLabel;
  @FXML private Label predictionLabel;
  @FXML private GridPane predictionGrid;
  @FXML private HBox canvasPane;
  @FXML private Button startButton;
  @FXML private Button brushButton;
  @FXML private Button eraseButton;
  @FXML private Button clearButton;
  @FXML private Button restartButton;
  @FXML private Button backToMenuButtonStart;
  @FXML private Button backToMenuButtonEnd;
  @FXML private Button saveButton;
  @FXML private HBox preGameHBox;
  @FXML private HBox postGameHBox;
  @FXML private VBox drawingToolsVBox;
  @FXML private ColorPicker colourPicker;
  @FXML private Tooltip gameToolTip;
  @FXML private Label gameToolTipLabel;

  // Define game object
  private Game game;

  // Define other UI data
  private GraphicsContext graphic;
  private boolean brush;
  private boolean isDrawing;
  private double currentX;
  private double currentY;
  private Color predictionListColor = Color.DARKSLATEBLUE;
  private Color predictionHighlightColor = Color.web("#008079");
  private Color predictionTextColor = Color.WHITE;
  private GameMode currentGameMode;
  private Font maybeNext;
  private boolean savedImage = false;

  // Task for alternating colour of the title and word label concurrently
  private Task<Void> alternateColoursTask =
      new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          // Set two labels to alternate between colours
          alternateColours(titleLabel, Color.web("#006969"), Color.web("#E20F58"));
          return null;
        }
      };

  /**
   * JavaFX calls this method once the GUI elements are loaded. In our case we create a listener for
   * the drawing, and we load the ML model.
   *
   * @throws ModelException If there is an error in reading the input/output of the DL model.
   * @throws IOException If the model cannot be found on the file system.
   * @throws URISyntaxException If the URI cannot be found
   * @throws CsvException If the Csv cannot be found
   */
  public void initialize() throws IOException, URISyntaxException, CsvException, ModelException {
    Game.createModel();
    // Get the graphic from the canvas
    graphic = canvas.getGraphicsContext2D();
    // Load font
    maybeNext = Font.loadFont(App.class.getResourceAsStream("/fonts/Maybe-Next.ttf"), 0);
    // Create a new thread for the alternating colours task
    Thread colourThread = new Thread(alternateColoursTask);
    // Allow the task to be cancelled on closing of application
    colourThread.setDaemon(true);
    // Start the colour task
    colourThread.start();
    // Set up the tool tip
    gameToolTip.setShowDelay(Duration.ZERO);
    gameToolTip.setWrapText(true);
    gameToolTip.setAutoFix(true);
  }

  public Game getGame() {
    return game;
  }

  /**
   * Sets UI elements which are common to the initialisation of the scene and restarting the game.
   *
   * @throws URISyntaxException
   * @throws CsvException
   * @throws IOException
   * @throws ModelException
   * @throws WordNotFoundException
   */
  public void setPreGameInterface()
      throws IOException, CsvException, URISyntaxException, ModelException, WordNotFoundException {
    currentGameMode = GameSelectController.getCurrentGameMode();
    // Instantiate a new game object on first opening the scene
    CategorySelector.loadCategories();
    // Reset the timer bar's css
    timerBarLabel.getStyleClass().clear();
    timerBarLabel.getStyleClass().add("timerBarDefault");
    colourPicker.setValue(Color.BLACK);
    colourPicker.setVisible(true);
    game = new Game(this, currentGameMode);

    if (currentGameMode != GameMode.PROFILE) {
      Platform.runLater(
          () -> {
            titleLabel.setText("Quick, Draw!");
            isDrawing = false;
            gameToolTipLabel.setVisible(true);
            // Bind label properties to game properties
            wordLabel.textProperty().bind(game.getCurrentPromptProperty());
            if (currentGameMode != GameMode.ZEN) {
              timerBarLabel.setPrefWidth(600.0);
              timerLabel.textProperty().bind(game.getTimeRemainingAsStringBinding());
              timerLabel.setVisible(true);
              colourPicker.setVisible(false);
            } else {
              timerBarLabel.setPrefWidth(20000.0);
              timerLabel.setVisible(false);
              // Set visible to color picker
              colourPicker.setVisible(true);
            }
            // Set UI elements for pre-game
            canvas.setDisable(true);
            // Enable pre-game button panel
            preGameHBox.setMouseTransparent(false);
            preGameHBox.setVisible(true);
            // Disable post-game button panel
            postGameHBox.setMouseTransparent(true);
            postGameHBox.setVisible(false);
            // Disable drawing tools
            drawingToolsVBox.setDisable(true);
            // Set timer bar max width
            timerBarLabel.setMaxWidth(600.0);

            timerBarLabel.setVisible(true);
            predictionLabel.setVisible(true);
            predictionGrid.setVisible(true);
            restartButton.setVisible(true);
          });
    } else {
      Platform.runLater(
          () -> {
            setProfile();
          });
    }
  }

  private void setProfile() {
    // Set the drawing to true instantly
    isDrawing = true;
    // Allow the player to use the canvas
    canvas.setDisable(false);
    // Hide other things
    timerLabel.setVisible(false);
    // Enable post-game button panel
    postGameHBox.setMouseTransparent(false);
    postGameHBox.setVisible(true);
    // Hide some more
    timerBarLabel.setVisible(false);
    predictionLabel.setVisible(false);
    predictionGrid.setVisible(false);
    restartButton.setVisible(false);
    // Change the text of the title
    titleLabel.setText("Draw a Picture!");
    gameToolTipLabel.setVisible(false);
    // Enable them to draw

    onStartGame();
  }

  /**
   * Returns whether the user has started drawing.
   *
   * @return true if the canvas has been clicked since the game start or last onClear.
   */
  public boolean getIsDrawing() {
    return isDrawing;
  }

  /** This function toggles from brush to eraser */
  @FXML
  private void onBrush() {
    // Enable the brush
    brush = true;
    // Update button styles to show selection
    brushButton.getStyleClass().clear();
    brushButton.getStyleClass().add("brushButtonSelected");
    eraseButton.getStyleClass().clear();
    eraseButton.getStyleClass().add("eraserButton");
  }

  @FXML
  private void onErase() {
    // Disable the brush (Enables erase)
    brush = false;
    brushButton.getStyleClass().clear();
    brushButton.getStyleClass().add("brushButton");
    eraseButton.getStyleClass().clear();
    eraseButton.getStyleClass().add("eraserButtonSelected");
  }

  /** This method is called when the "Clear" button is pressed. Resets brush to "Brush" mode. */
  @FXML
  private void onClear() {
    // Clear the canvas
    graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    clearPredictionGrid();
    isDrawing = false;
    // Switch to brush
    onBrush();
  }

  /**
   * Updates the prediction grid display to show the top 10 entries of the input List.
   *
   * @param predictionList List of classifications from the machine learning model
   */
  public void updatePredictionGridDisplay(List<Classification> predictionList) {
    clearPredictionGrid();
    for (int i = 0; i < predictionList.size(); i++) {
      String prediction = predictionList.get(i).getClassName();
      // Check if the prediction is the prompt word to determine label color
      boolean isPrompt =
          prediction.replaceAll("_", " ").equals(game.getCurrentWord()) ? true : false;
      // Create a formatted String for the prediction label
      String predictionEntry =
          (i + 1) + ". " + predictionList.get(i).getClassName().replaceAll("_", " ");
      // Create and add the prediction label to its cell in the grid, row order is top
      // to bottom :
      // highest to lowest rank
      predictionGrid.add(createPredictionLabel(predictionEntry, isPrompt, i), 0, i);
    }
  }

  /**
   * Creates a label for the prediction grid. The background of the label will fill the whole cell
   * of the grid.
   *
   * @param labelText The text value for the label
   * @param labelColor The color for the label background
   * @return Label object
   */
  private Label createPredictionLabel(String labelText, boolean isPrompt, int index) {
    // Create new label and configure formatting
    Label label = new Label();
    label.getStyleClass().add("predictionGridLabel");
    label.setText(labelText);
    label.setTextFill(predictionTextColor);
    label.setPadding(new Insets(0, 0, 0, 10));
    // Configure special formatting for top and bottom labels in the grid
    if (index == 0 && !isPrompt) {
      label.getStyleClass().add("predictionGridLabelTop");
    }
    if (index == 9 && !isPrompt) {
      label.getStyleClass().add("predictionGridLabelBottom");
    }
    // Colour label depending on whether it is the prompt word
    label
        .getStyleClass()
        .add(isPrompt ? "predictionGridLabelIsPrompt" : "predictionGridLabelDefault");
    // Ensure the label background fills the entire cell
    label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    return label;
  }

  /** Clears the prediction grid of entries and fills it with empty string entries. */
  private void clearPredictionGrid() {
    // Remove all child nodes from the GridPane
    predictionGrid.getChildren().clear();
    // Fill the GridPane with empty Label nodes
    for (int i = 0; i < predictionGrid.getRowCount(); i++) {
      predictionGrid.add(createPredictionLabel(" ", false, i), 0, i);
    }
  }

  /**
   * This method will restart the game once the player presses the button
   *
   * @throws ModelException
   * @throws CsvException
   * @throws URISyntaxException
   * @throws IOException
   * @throws WordNotFoundException
   */
  @FXML
  public void onRestartGame()
      throws IOException, URISyntaxException, CsvException, ModelException, WordNotFoundException {
    // Clear the canvas
    onClear();
    // Reset game variables and concurrent service
    game.resetGame();
    // Reset UI elements (NOTE: CREATES NEW GAME OBJECT!)
    setPreGameInterface();
    // Reset Timer
    resetTimerBar();
  }

  /**
   * Get the current snapshot of the canvas.
   *
   * @return The BufferedImage corresponding to the current canvas content.
   */
  public BufferedImage getCurrentSnapshot() {
    final Image snapshot = canvas.snapshot(null, null);
    final BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);

    // Convert into a binary image.
    final BufferedImage imageBinary =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);

    final Graphics2D graphics = imageBinary.createGraphics();

    graphics.drawImage(image, 0, 0, null);

    // To release memory we dispose.
    graphics.dispose();

    return imageBinary;
  }

  /**
   * Save the current snapshot on a bitmap file.
   *
   * @return The file of the saved image.
   * @throws IOException If the image cannot be saved.
   */
  private File saveCurrentSnapshotOnFile() throws IOException {
    File file = null;
    if (currentGameMode != GameMode.PROFILE) {
      // Create a file chooser
      FileChooser fileChooser = new FileChooser();
      // Set extension filter for bmp files
      FileChooser.ExtensionFilter extFilter =
          new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
      fileChooser.getExtensionFilters().add(extFilter);
      // Set the initial file name
      fileChooser.setInitialFileName("snapshot" + System.currentTimeMillis());
      // Set the initial directory
      String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
      fileChooser.setInitialDirectory(new File(currentPath));
      // Show save file dialog
      file = fileChooser.showSaveDialog(null);
    } else {
      // Gets the file directory to save to and runs the load user method
      File dir = new File(Users.folderDirectory + "/src/main/resources/images/");
      // We save the image to a file in the tmp folder.
      file = new File(Users.folderDirectory + "/src/main/resources/images/tempImage.bmp");
      if (!file.exists()) {
        dir.mkdir();
        file.createNewFile();
      }
      savedImage = true;
    }
    // Write it to the location
    if (file != null) {
      ImageIO.write(getCurrentSnapshot(), "bmp", file);
    }
    return file;
  }

  /**
   * Styling Function: Alternate colours of a label between two colours
   *
   * @param label the label that will alternate between colours
   * @param colour1 the first colour it will be
   * @param colour2 the second colour it will become
   */
  private void alternateColours(Label label, Color colour1, Color colour2) {
    // Create a new timeline object
    Timeline timeline = new Timeline();
    // Loop three times
    for (int i = 0; i < 3; i++) {
      // Add a new keyframe accordingly
      timeline
          .getKeyFrames()
          .add(
              new KeyFrame(
                  Duration.seconds(2.5 * i),
                  new KeyValue(label.textFillProperty(), i != 1 ? colour1 : colour2)));
    }
    // Play the animation indefinitely
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  public void decrementTimerBar() {
    timerBarLabel.setPrefWidth(timerBarLabel.getWidth() - (600 - wordLabel.getWidth()) / 60);
  }

  public void resetTimerBar() {
    timerBarLabel.getStyleClass().clear();
    timerBarLabel.getStyleClass().add("timerBarDefault");
    timerBarLabel.setPrefWidth(600.0);
  }

  /**
   * Called when the game ends. <br>
   * - Updates UI to give feedback to user on whether they won or lost, plus their time to draw if
   * won <br>
   * - Prompts user with a pop-up giving them the option to save their drawing
   *
   * @return isWin The win flag from the game controller, is true if the game was won and false if
   *     lost
   * @throws InterruptedException
   */
  public void onEndGame(boolean isWin) {
    Platform.runLater(
        () -> {
          // Stop drawing and prediction
          isDrawing = false;
          canvas.setDisable(true);
          // Set UI elements for post-game
          // Enable post-game button panel
          postGameHBox.setMouseTransparent(false);
          postGameHBox.setVisible(true);
          // Disable pre-game button panel
          preGameHBox.setMouseTransparent(true);
          preGameHBox.setVisible(false);
          // Disable drawing tools
          drawingToolsVBox.setDisable(true);
          // Unbind label properties bound to game properties
          wordLabel.textProperty().unbind();
          timerLabel.textProperty().unbind();

          // Update the word label to display a win or loss message for the user at the
          // end of the
          // game.
          if (isWin) {
            wordLabel.setText(getWinMessage());
            timerBarLabel.getStyleClass().clear();
            timerBarLabel.getStyleClass().add("timerBarWin");
            Badges.winBadge("Wins", "First Win");
            Badges.winDifficultyBadges(
                DifficultyBuilder.getAccDifficulty(),
                DifficultyBuilder.getWordsDifficulty(),
                DifficultyBuilder.getTimeDifficulty(),
                DifficultyBuilder.getConfDifficulty());
            Users.increaseWins();
          } else {
            System.out.println("LOST");
            wordLabel.setText(getLossMessage());
            timerBarLabel.getStyleClass().clear();
            timerBarLabel.getStyleClass().add("timerBarLoss");
            Users.increaseLosses();
            Users.setConsistentWins(0);
          }
          timerBarLabel.setPrefWidth(20000.0);
          Users.saveUser();
        });
  }

  /**
   * Everyone loves a winner. Also calculates the time taken to draw the winning image.
   *
   * @return A string informing the user they have won and how much time they took.
   */
  private String getWinMessage() {
    return "You won! You drew "
        + game.getCurrentWord()
        + " in "
        + (CategorySelector.getTime() - game.getTimeRemaining())
        + " seconds!";
  }

  /**
   * Tells 'em to try again.
   *
   * @return A string that is polite, professional, and honest.
   */
  private String getLossMessage() {
    // Create custom loss message
    if (currentGameMode == GameMode.HIDDEN_WORD) {
      return String.format("Out of time! The word was %s", game.getCurrentWord());
    }
    return "Out of time! Play again?";
  }

  /** This method sets up a new game to be started */
  @FXML
  public void onStartGame() {
    // Ghost game pretends it is drawing to load and call first update of game services to pre-load
    // them
    if (game.getIsGhostGame()) {
      isDrawing = true;
    }

    if (currentGameMode != GameMode.PROFILE) {
      game.startGame();
    }
    if (currentGameMode == GameMode.ZEN) {
      postGameHBox.setMouseTransparent(false);
      postGameHBox.setVisible(true);
    }

    canvas.setDisable(false);
    // Turn on to brush mode regardless of what it was
    onBrush();

    // Enable drawing tools
    drawingToolsVBox.setDisable(false);

    // Hide pre-game buttons
    preGameHBox.setVisible(false);

    // Get eraser colour
    Paint eraserColour = Color.web("#FCEFE8");
    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
          isDrawing = true;
        });

    canvas.setOnMouseDragged(
        e -> {

          // Brush size (you can change this, it should not be too small or too large).
          double size = 5.0;

          final double x = e.getX() - size / 2;
          final double y = e.getY() - size / 2;
          if (isDrawing) {
            // This is the colour of the brush.
            if (brush) {
              graphic.setStroke(colourPicker.getValue());
              graphic.setLineWidth(size);
              graphic.strokeLine(currentX, currentY, x, y);

            } else {

              graphic.setFill(eraserColour);
              size = 20.0;
              graphic.fillOval(x, y, size, size);
            }
            currentX = x;
            currentY = y;
          }
        });
  }

  /**
   * Takes the user back to the main menu. Also resets the game in the canvas scene so loading a new
   * game from the menu will present the user with a fresh game.
   *
   * @param event
   * @throws IOException
   * @throws URISyntaxException
   * @throws CsvException
   * @throws ModelException
   * @throws InterruptedException
   * @throws WordNotFoundException
   */
  @FXML
  private void onBackToMenuStart(ActionEvent event)
      throws IOException, URISyntaxException, CsvException, ModelException, InterruptedException,
          WordNotFoundException {

    // If not in zen mode, cancelling the game counts as a loss
    if (currentGameMode != GameMode.ZEN) {
      // Create a new alert
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      // Set up the alert accordingly
      alert.setTitle("Quit To Menu");
      alert.setHeaderText("Quitting now will count as a loss! Are you sure?");
      alert.setResizable(false);
      alert.setContentText("Select OK or Cancel.");

      // Show the alert
      Optional<ButtonType> result = alert.showAndWait();
      // Check if the person presses yes
      if (result.get() == ButtonType.OK) {
        onEndGame(false);
        onBack(event);
      } else if (result.get() == ButtonType.CANCEL) {
        // Do nothing if no is pressed
        return;
      }
    } else {
      // For profile go back
      onBack(event);
    }
  }

  @FXML
  private void onBack(ActionEvent event)
      throws IOException, URISyntaxException, CsvException, ModelException, InterruptedException,
          WordNotFoundException {

    if (currentGameMode == GameMode.PROFILE) {
      // Check if the player saved the image
      if (savedImage) {
        // update the user's image in the create scene
        App.getCreationController().updateImage();
        savedImage = false;
      }
      // Go back to the creation scene
      onBackToCreation(event);
    } else {
      // Go back to main menu
      onBackToMenu(event);
    }
    // Clear the canvas
    onClear();
    // Reset game variables and concurrent service
    game.resetGame();
    resetTimerBar();
  }

  @FXML
  private void onBackToMenu(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }

  @FXML
  private void onBackToCreation(ActionEvent event)
      throws IOException, URISyntaxException, CsvException, ModelException {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.USERCREATE));
  }

  /**
   * onSaveImage will ask the user if they want to save their image and then allow the user to save
   * the image on their computer
   *
   * @param event the Action Event taken in from FXML
   * @throws IOException
   */
  @FXML
  private void onSaveImage(ActionEvent event) throws IOException {
    if (currentGameMode != GameMode.PROFILE) {
      // Create a new alert
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      // Set up the alert accordingly
      alert.setTitle("Save File");
      alert.setHeaderText("Would you like to save your image?");
      alert.setResizable(false);
      alert.setContentText("Select OK or Cancel.");

      // Show the alert
      Optional<ButtonType> result = alert.showAndWait();
      // Check if the person presses yes
      if (result.get() == ButtonType.OK) {
        // Save the current canvas as a file in the tmp folder
        saveCurrentSnapshotOnFile();
      } else if (result.get() == ButtonType.CANCEL) {
        // Do nothing if no is pressed
        return;
      }
    } else {
      Badges.setDrawUserPicture(true);
      saveCurrentSnapshotOnFile();
    }
  }

  /** Update the tool tip in the hidden word game */
  public void updateToolTip() {
    switch (currentGameMode) {
      case HIDDEN_WORD:
        gameToolTip.setText(game.getCurrentPrompt());
        break;
      case NORMAL:
        gameToolTip.setText("Draw the word and try to win!");
        break;
      case PROFILE:
        gameToolTip.setText("Draw your profile picture!");
        break;
      case ZEN:
        gameToolTip.setText("Feel free to draw!!!");
        break;
      default:
        gameToolTip.setText("Draw the word and try to win!");
        break;
    }
  }
}
