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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import nz.ac.auckland.se206.Game.Indicator;
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
public class CanvasController extends SoundsController {

  // Define the scaling in hovering
  private static final String IDLE_STYLE = "-fx-scale-x: 1; -fx-scale-y: 1";
  private static final String HOVERED_STYLE = "-fx-scale-x: 1.2; -fx-scale-y: 1.2";
  // Define higher or lower images
  private static final Image INDICATOR_CLOSER =
      new Image(Users.folderDirectory + "/src/main/resources/images/indicatorCloser.png");
  private static final Image INDICATOR_FURTHER =
      new Image(Users.folderDirectory + "/src/main/resources/images/indicatorFurther.png");
  // Define preset brush and eraser sizes
  private static final double BRUSH_SMALL = 5;
  private static final double BRUSH_MEDIUM = 8;
  private static final double BRUSH_LARGE = 12;
  private static final double ERASER_SMALL = 6;
  private static final double ERASER_MEDIUM = 9;
  private static final double ERASER_LARGE = 13;
  private static ImageView predictionImage = new ImageView();
  private static GameMode currentGameMode;
  private static int hints = 0;

  public static GameMode getCurrentGameMode() {
    return currentGameMode;
  }

  public static void setCurrentGameMode(GameMode currentGameMode) {
    CanvasController.currentGameMode = currentGameMode;
  }

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
  @FXML private HBox preGameBox;
  @FXML private HBox postGameBox;
  @FXML private HBox drawUserBox;
  @FXML private VBox drawingToolsBox;
  @FXML private ColorPicker colourPicker;
  @FXML private Tooltip gameToolTip;
  @FXML private Label gameToolTipLabel;
  @FXML private VBox aboveVbox;
  @FXML private VBox rightPanelBox;
  @FXML private Label cornerLabel;
  @FXML private VBox brushOptionsBox;
  @FXML private VBox eraserOptionsBox;
  @FXML private Button brushSmallButton;
  @FXML private Button brushMediumButton;
  @FXML private Button brushLargeButton;
  @FXML private Button eraserSmallButton;
  @FXML private Button eraserMediumButton;
  @FXML private Button eraserLargeButton;
  @FXML private Button textSpeechButton;

  // Define game object
  private Game game;

  // Define other UI data
  private GraphicsContext graphic;
  private boolean brush;
  private boolean isDrawing;
  private double currentX;
  private double currentY;
  private Color predictionTextColor = Color.WHITE;
  private boolean savedImage = false;
  private Label definitionLabel;
  private ArrayList<Integer> wordCharacters = new ArrayList<Integer>();
  private double brushSize;
  private double eraserSize;
  private Timeline timeline = new Timeline();
  private int initialTime;

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
    Font.loadFont(App.class.getResourceAsStream("/fonts/Maybe-Next.ttf"), 0);

    // Set up the tool tip
    gameToolTip.setShowDelay(Duration.ZERO);
    gameToolTip.setWrapText(true);
    gameToolTip.setAutoFix(true);
    setUpDefLabel();

    // Events for hovering on tool tip
    // Event for hovering on
    gameToolTipLabel.setOnMouseEntered(
        e -> {
          gameToolTipLabel.setStyle(HOVERED_STYLE);
          // Hover effect
          playButtonHover();
          if (currentGameMode == GameMode.HIDDEN_WORD && hints < 3) {
            // Switch to hand
            gameToolTipLabel.getScene().setCursor(Cursor.HAND);
          }
        });

    // Event for hovering off
    gameToolTipLabel.setOnMouseExited(
        e -> {
          gameToolTipLabel.setStyle(IDLE_STYLE);
          // Remove hover effect
          gameToolTipLabel.getScene().setCursor(Cursor.DEFAULT);
        });
  }

  public Game getGame() {
    return game;
  }

  /**
   * Sets UI elements which are common to the initialisation of the scene and restarting the game.
   *
   * @throws IOException reading/writing exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws URISyntaxException converting to link exception
   * @throws ModelException doodle prediction exception
   * @throws WordNotFoundException word was not found exception
   */
  public void setPreGameInterface()
      throws IOException, CsvException, URISyntaxException, ModelException, WordNotFoundException {
    currentGameMode = GameSelectController.getCurrentGameMode();
    onClear();
    // Instantiate a new game object on first opening the scene
    CategorySelector.loadCategories();
    // Update the timer bar and title styles
    updateStyle();

    colourPicker.setValue(Color.BLACK);
    colourPicker.setVisible(true);
    // Create the game instance
    game = new Game(this, currentGameMode);
    // Set UI elements for pre-game
    canvas.setDisable(true);
    // Update the tool tip
    updateToolTip();
    // Update brush size to small (5px)
    brushSize = BRUSH_SMALL;
    // Updates the timeBar
    initialTime = game.getTimeRemaining();
    // Update eraser size to medium (9px)
    eraserSize = ERASER_MEDIUM;
    // Disable/Enable the definition label
    if (currentGameMode == GameMode.HIDDEN_WORD) {
      setUpDefLabel();
      definitionLabel.setVisible(true);
      definitionLabel.setText(game.getDefinition());
    } else {
      destroyDefLabel();
      definitionLabel.setVisible(false);
    }
    // Main setup
    if (currentGameMode != GameMode.PROFILE) {
      textSpeechButton.setVisible(true);
      Platform.runLater(
          () -> {
            // set visibility of the UI elements
            cornerLabel.setVisible(true);
            brushOptionsBox.setVisible(false);
            brushOptionsBox.setMouseTransparent(true);
            eraserOptionsBox.setVisible(false);
            eraserOptionsBox.setMouseTransparent(true);
            drawUserBox.setVisible(false);
            drawUserBox.setMouseTransparent(true);
            // Update sizes
            rightPanelBox.setPrefWidth(260);
            for (int i = 0; i < CategorySelector.getAccuracy(); i++) {
              predictionGrid.add(createPredictionLine(i), 0, i);
            }
            cornerLabel.setVisible(true);
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
              // reset visibilities of line
            } else {
              timerBarLabel.setPrefWidth(20000.0);
              timerLabel.setVisible(false);
              // Set visible to color picker
              colourPicker.setVisible(true);
            }
            // Enable pre-game button panel
            preGameBox.setMouseTransparent(false);
            preGameBox.setVisible(true);
            // Disable post-game button panel
            postGameBox.setMouseTransparent(true);
            postGameBox.setVisible(false);
            // Disable drawing tools
            drawingToolsBox.setDisable(true);
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

  /** updateStyle will update the styles for each game mode for timer bar label and title */
  private void updateStyle() {
    // Clear the current styles
    timerBarLabel.getStyleClass().clear();
    switch (currentGameMode) {
      case BLITZ:
        // Add the blitz styling
        timerBarLabel.getStyleClass().add("timerBarBlitz");
        alternateColours(titleLabel, Color.web("#f45b69"), Color.web("#a2224d"));
        break;
      case HIDDEN_WORD:
        // Add the hidden word styling
        timerBarLabel.getStyleClass().add("timerBarHidden");
        alternateColours(titleLabel, Color.web("#fbb13c"), Color.web("#d45500"));
        break;
      case NORMAL:
        // Add the normal styling
        timerBarLabel.getStyleClass().add("timerBarClassic");
        alternateColours(titleLabel, Color.web("#51d4d0"), Color.web("#006868"));
        break;
      case PROFILE:
        // Alternate colours
        alternateColours(titleLabel, Color.web("#51d4d0"), Color.web("#006868"));
        break;
      case ZEN:
        // Add the zen styling
        timerBarLabel.getStyleClass().add("timerBarZen");
        alternateColours(titleLabel, Color.web("#51b751"), Color.web("#307330"));
        break;
      default:
        break;
    }
  }

  /** deleteIndicator will delete the indicator image */
  private void deleteIndicator() {
    // Check if the image is active
    if (predictionLabel.getGraphic() != null) {
      // Remove the image
      predictionLabel.setGraphic(null);
    }
  }

  /** addIndicator will add a closer or further image to prediction label */
  private void addIndicator() {
    // Check that there is no image set
    if (predictionLabel.getGraphic() == null) {
      // Place the image inside the label
      predictionLabel.setGraphic(predictionImage);
    }
  }

  /**
   * updateIndicator will update the indicator on the canvas
   *
   * @param indicator closer will be true if the word is moving up the predictions list
   */
  public void updateIndicator(Indicator indicator) {
    // Switch between the indicators
    switch (indicator) {
      case CLOSER:
        addIndicator();

        // Set the image and text for closer
        predictionImage.setImage(INDICATOR_CLOSER);
        predictionLabel.setText("Closer!");
        break;
      case FURTHER:
        addIndicator();

        // Set the image and text for further
        predictionImage.setImage(INDICATOR_FURTHER);
        predictionLabel.setText("Further away!");
        break;
      case NOT_FOUND:
        deleteIndicator();
        // Set the text for not in top 100
        predictionLabel.setText("Not in top 100!");
        break;
      case SAME:
        deleteIndicator();
        // Set the text for not changed position
        predictionLabel.setText("Haven't changed!");
        break;
      default:
        break;
    }
  }

  /** setUpDefLabel will set up the definition label */
  private void setUpDefLabel() {
    // Creates a definition label
    definitionLabel = new Label();
    // Sets up all the properties
    definitionLabel.setAlignment(Pos.CENTER);
    definitionLabel.setTextAlignment(TextAlignment.CENTER);
    definitionLabel.setFont(Font.font(wordLabel.getFont().getFamily(), FontWeight.NORMAL, 20));
    // Set up the size for the label
    definitionLabel.setPrefSize(1025, 100);
    definitionLabel.setMinSize(1025, 100);
    definitionLabel.setWrapText(true);
    definitionLabel.getStyleClass().clear();
    definitionLabel.getStyleClass().add("predictionLabel");
    // Adds it to the children
    aboveVbox.getChildren().add(1, definitionLabel);
  }

  /** destroyDefLabel will destroy the definition label and remove it from the canvas */
  private void destroyDefLabel() {
    aboveVbox.getChildren().remove(definitionLabel);
  }

  /** SetProfile will set up the profile creation image process */
  private void setProfile() {
    // Update eraser
    eraserSize = ERASER_SMALL;
    // Set all java fx properties
    textSpeechButton.setVisible(false);
    cornerLabel.setVisible(false);
    brushOptionsBox.setVisible(true);
    brushOptionsBox.setMouseTransparent(false);
    eraserOptionsBox.setVisible(false);
    eraserOptionsBox.setMouseTransparent(true);
    // Set the drawing to true instantly
    isDrawing = true;
    // Allow the player to use the canvas
    canvas.setDisable(false);
    // Hide other things
    timerLabel.setVisible(false);
    // Enable post-game button panel
    postGameBox.setVisible(false);
    postGameBox.setMouseTransparent(true);
    preGameBox.setVisible(false);
    preGameBox.setMouseTransparent(true);
    drawUserBox.setVisible(true);
    drawUserBox.setMouseTransparent(false);
    // Hide some more
    timerBarLabel.setVisible(false);
    predictionLabel.setVisible(false);
    predictionGrid.setVisible(false);
    // Nudge the canvas into the middle by shrinking the prediction grid area
    rightPanelBox.setPrefWidth(170);
    restartButton.setVisible(false);
    // Change the text of the title
    titleLabel.setText("Draw Your Sticker!");
    gameToolTipLabel.setVisible(false);
    cornerLabel.setVisible(false);
    // Enable them to draw
    onSmallBrush();
    onSmallErase();

    onStartGame(null);
  }

  /**
   * Returns whether the user has started drawing.
   *
   * @return true if the canvas has been clicked since the game start or last onClear.
   */
  public boolean getIsDrawing() {
    return isDrawing;
  }

  /** Enables the brush and disables the eraser */
  @FXML
  private void onBrush() {
    // Enable the brush
    brush = true;
    // Update button styles to show selection
    brushButton.getStyleClass().clear();
    brushButton.getStyleClass().add("brushButtonSelected");
    eraseButton.getStyleClass().clear();
    eraseButton.getStyleClass().add("eraserButton");
    // Check if we are in sticker creation
    if (currentGameMode.equals(GameMode.PROFILE)) {
      // Set the brush options to visible
      brushOptionsBox.setVisible(true);
      brushOptionsBox.setMouseTransparent(false);
      // Turn off erasers
      eraserOptionsBox.setVisible(false);
      eraserOptionsBox.setMouseTransparent(true);
    }
  }

  /** Sets the brush size to small */
  @FXML
  private void onSmallBrush() {
    // Clear the brush styles
    brushSmallButton.getStyleClass().clear();
    // Add that the small brush style is on
    brushSmallButton.getStyleClass().add("brushSizeSmallSelected");
    // Change the styles of the other two brush buttons
    brushMediumButton.getStyleClass().clear();
    brushMediumButton.getStyleClass().add("brushSizeMedium");
    brushLargeButton.getStyleClass().clear();
    brushLargeButton.getStyleClass().add("brushSizeLarge");
    // Set the brush size to small
    brushSize = BRUSH_SMALL;
  }

  /** Sets the brush size to medium */
  @FXML
  private void onMediumBrush() {
    // Clear the brush styles
    brushMediumButton.getStyleClass().clear();
    // Add that the medium brush style is on
    brushMediumButton.getStyleClass().add("brushSizeMediumSelected");
    // Change the styles of the other two brush buttons
    brushSmallButton.getStyleClass().clear();
    brushSmallButton.getStyleClass().add("brushSizeSmall");
    brushLargeButton.getStyleClass().clear();
    brushLargeButton.getStyleClass().add("brushSizeLarge");
    // Set the brush size to medium
    brushSize = BRUSH_MEDIUM;
  }

  /** Sets the brush size to large */
  @FXML
  private void onLargeBrush() {
    // Clear the brush styles
    brushLargeButton.getStyleClass().clear();
    // Add that the large brush style is on
    brushLargeButton.getStyleClass().add("brushSizeLargeSelected");
    // Change the styles of the other two brush buttons
    brushMediumButton.getStyleClass().clear();
    brushMediumButton.getStyleClass().add("brushSizeMedium");
    brushSmallButton.getStyleClass().clear();
    brushSmallButton.getStyleClass().add("brushSizeSmall");
    // Set the brush size to large
    brushSize = BRUSH_LARGE;
  }

  /** Enables the eraser and disables the brush */
  @FXML
  private void onErase() {
    // Disable the brush (Enables erase)
    brush = false;
    // Clear the brush and eraser style and add the new styles
    brushButton.getStyleClass().clear();
    brushButton.getStyleClass().add("brushButton");
    eraseButton.getStyleClass().clear();
    eraseButton.getStyleClass().add("eraserButtonSelected");
    // Check if we are in sticker creation
    if (currentGameMode.equals(GameMode.PROFILE)) {
      // Set the eraser options to visible
      eraserOptionsBox.setVisible(true);
      eraserOptionsBox.setMouseTransparent(false);
      // Turn off brushes
      brushOptionsBox.setVisible(false);
      brushOptionsBox.setMouseTransparent(true);
    }
  }

  /** Sets the eraser size to small */
  @FXML
  private void onSmallErase() {
    // Clear the eraser styles
    eraserSmallButton.getStyleClass().clear();
    // Add that the small eraser style is on
    eraserSmallButton.getStyleClass().add("eraserSizeSmallSelected");
    // Change the styles of the other two buttons
    eraserMediumButton.getStyleClass().clear();
    eraserMediumButton.getStyleClass().add("eraserSizeMedium");
    eraserLargeButton.getStyleClass().clear();
    eraserLargeButton.getStyleClass().add("eraserSizeLarge");
    // Set the eraser to small
    eraserSize = ERASER_SMALL;
  }

  /** Sets the eraser size to medium */
  @FXML
  private void onMediumErase() {
    // Clear the eraser styles
    eraserMediumButton.getStyleClass().clear();
    // Add that the medium eraser style is on
    eraserMediumButton.getStyleClass().add("eraserSizeMediumSelected");
    // Change the styles of the other two buttons
    eraserSmallButton.getStyleClass().clear();
    eraserSmallButton.getStyleClass().add("eraserSizeSmall");
    eraserLargeButton.getStyleClass().clear();
    eraserLargeButton.getStyleClass().add("eraserSizeLarge");
    // Set the eraser to medium
    eraserSize = ERASER_MEDIUM;
  }

  /** Sets the eraser size to large */
  @FXML
  private void onLargeErase() {
    // Clear the eraser styles
    eraserLargeButton.getStyleClass().clear();
    // Add that the large eraser style is on
    eraserLargeButton.getStyleClass().add("eraserSizeLargeSelected");
    // Change the styles of the other two buttons
    eraserMediumButton.getStyleClass().clear();
    eraserMediumButton.getStyleClass().add("eraserSizeMedium");
    eraserSmallButton.getStyleClass().clear();
    eraserSmallButton.getStyleClass().add("eraserSizeSmall");
    // Set the eraser to medium
    eraserSize = ERASER_LARGE;
  }

  /** This method is called when the "Clear" button is pressed. Resets brush to "Brush" mode. */
  @FXML
  protected void onClear() {
    // Clear the canvas
    graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    clearPredictionGrid();
    deleteIndicator();
    ;
    predictionLabel.setText("");
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
    for (int i = 0; i < 10; i++) {
      String prediction = predictionList.get(i).getClassName();
      // Check if the prediction is the prompt word to determine label color
      boolean isPrompt =
          prediction.replaceAll("_", " ").equals(game.getCurrentWord()) ? true : false;
      // Create a formatted String for the prediction label
      String predictionEntry =
          (i + 1) + ". " + predictionList.get(i).getClassName().replaceAll("_", " ");
      Double predictionPercent = predictionList.get(i).getProbability() * 100;
      String percentString = String.format("%.2f", predictionPercent);
      // Create and add the prediction label to its cell in the grid, row order is top
      // to bottom :
      // highest to lowest rank
      predictionGrid.add(createPredictionLabel(predictionEntry, percentString, isPrompt, i), 0, i);
      if (i < CategorySelector.getAccuracy()) {
        // Add the prediction line in
        predictionGrid.add(createPredictionLine(i), 0, i);
      }
    }
  }

  private Line createPredictionLine(int index) {
    // Create a new line
    Line line = new Line();
    // Update line properties
    line.setStrokeType(StrokeType.OUTSIDE);
    line.setStartX(-100);
    line.setEndX(148);
    line.toFront();
    // Align the line
    GridPane.setValignment(line, VPos.BOTTOM);
    // Based on accuracy set the correct one to be visible
    if (index == CategorySelector.getAccuracy() - 1) {
      line.setVisible(true);
    } else {
      line.setVisible(false);
    }
    return line;
  }

  /**
   * Creates a label for the prediction grid. The background of the label will fill the whole cell
   * of the grid.
   *
   * @param labelText The text value for the label
   * @param labelColor The color for the label background
   * @return Label object
   */
  private Label createPredictionLabel(
      String labelText, String percentage, boolean isPrompt, int index) {
    // Create new label and configure formatting
    Label label = new Label();
    label.getStyleClass().add("predictionGridLabel");
    if (percentage != null) {
      label.setText(String.format("%s %s%%", labelText, percentage));
    }
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
    // Remove all child nodes from the GridPane except lines
    predictionGrid.getChildren().clear();

    // Fill the GridPane with empty Label nodes
    for (int i = 0; i < predictionGrid.getRowCount(); i++) {
      predictionGrid.add(createPredictionLabel(" ", null, false, i), 0, i);
      predictionGrid.add(createPredictionLine(i), 0, i);
    }
  }

  /** restartHiddenGame will restart the hidden game mode after taking it to the loading screen */
  private void restartHiddenGame() {

    Task<Void> restartGameTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Set up the pre-game UI elements that are in common with restarting the game
            updateProgress(0, 1);
            destroyDefLabel();
            Thread.sleep(100);
            updateProgress(0.3, 1);
            // Reset game variables and concurrent service
            Platform.runLater(() -> game.resetGame());
            updateProgress(0.5, 1);
            Thread.sleep(100);
            // Reset UI elements (NOTE: CREATES NEW GAME OBJECT!)
            Platform.runLater(
                () -> {
                  try {
                    setPreGameInterface();
                  } catch (IOException
                      | CsvException
                      | URISyntaxException
                      | ModelException
                      | WordNotFoundException e) {
                    // Print the error's message
                    e.printStackTrace();
                  }
                });
            ;
            updateProgress(0.9, 1);
            // Reset Timer
            Platform.runLater(() -> resetTimerBar());
            updateProgress(1, 1);
            Thread.sleep(100);
            return null;
          }
        };
    if (!game.getIsGhostGame()) {
      // Find progress bar on loading screen
      ProgressBar progressBar = App.getLoadingController().getProgressBar();
      progressBar.progressProperty().unbind();
      // Bind progress bar
      progressBar.progressProperty().bind(restartGameTask.progressProperty());

      Scene sceneButtonIsIn = restartButton.getScene();

      sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.LOADING));
      restartGameTask.setOnSucceeded(
          e -> {
            progressBar.progressProperty().unbind();
            // Move to the next scene
            sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
          });
    }
    Thread preGameThread = new Thread(restartGameTask);
    // Allow the task to be cancelled on closing of application
    preGameThread.setDaemon(true);
    preGameThread.start();
  }

  /**
   * This method will restart the game once the player presses the button
   *
   * @throws IOException reading/writing exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws URISyntaxException converting to link exception
   * @throws ModelException doodle prediction exception
   * @throws WordNotFoundException word was not found exception
   */
  @FXML
  protected void onRestartGame(ActionEvent event)
      throws IOException, CsvException, URISyntaxException, ModelException, WordNotFoundException {
    // Clear the canvas
    onClear();
    if (currentGameMode == GameMode.HIDDEN_WORD) {
      wordCharacters.clear();
      restartHiddenGame();
      return;
    }
    // Reset game variables and concurrent service
    game.resetGame();
    // Reset UI elements (NOTE: CREATES NEW GAME OBJECT!)
    setPreGameInterface();
    // Call the method that will restart the timer
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
    // Initialise a file
    File file;
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
      file = new File(Users.folderDirectory + "/src/main/resources/images/tempImage.png");
      if (!file.exists()) {
        dir.mkdir();
        file.createNewFile();
      }
      savedImage = true;
    }

    if (file == null) {
      return null;
    }

    if (currentGameMode == GameMode.PROFILE) {
      SnapshotParameters sp = new SnapshotParameters();
      sp.setFill(Color.TRANSPARENT);
      ImageIO.write(SwingFXUtils.fromFXImage(canvas.snapshot(sp, null), null), "png", file);
    } else {
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
    timeline.getKeyFrames().clear();
    // Create a new timeline object
    timeline = new Timeline();
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

  /** decrementTimerBar will decrease the timer bar width */
  public void decrementTimerBar() {
    // Reduce the width of the timer bar
    timerBarLabel.setPrefWidth(
        timerBarLabel.getWidth() - (600 - wordLabel.getWidth()) / initialTime);
  }

  /** resetTimerBar will reset the width and reset the css */
  public void resetTimerBar() {
    // Remove all the styling and set the default one back
    updateStyle();
    // Reset the width to 600 (back to default)
    timerBarLabel.setPrefWidth(600.0);
  }

  /**
   * Called when the game ends. <br>
   * - Updates UI to give feedback to user on whether they won or lost, plus their time to draw if
   * won <br>
   * - Prompts user with a pop-up giving them the option to save their drawing
   *
   * @param isWin The win flag from the game controller, is true if the game was won and false if
   *     lost
   * @throws InterruptedException interrupted exception will cause the thread to be interrupted
   */
  public void onEndGame(boolean isWin) {
    Platform.runLater(
        () -> {
          // reset hints
          hints = 0;
          // Stop drawing and prediction
          isDrawing = false;
          canvas.setDisable(true);
          // Set UI elements for post-game
          // Enable post-game button panel
          postGameBox.setMouseTransparent(false);
          postGameBox.setVisible(true);
          // Disable pre-game button panel
          preGameBox.setMouseTransparent(true);
          preGameBox.setVisible(false);
          // Disable drawing tools
          drawingToolsBox.setDisable(true);
          // Unbind label properties bound to game properties
          wordLabel.textProperty().unbind();
          timerLabel.textProperty().unbind();
          timerBarLabel.setPrefWidth(20000.0);
          // Update the word label to display a win or loss message for the user at the
          // end of the
          // game.
          if (isWin) {
            timerBarLabel.getStyleClass().clear();
            timerBarLabel.getStyleClass().add("timerBarWin");
            // Increase the wins
            Users.increaseWins();
            wordLabel.setText(getWinMessage());

            // First win badge
            Badges.winBadge("Wins", "First Win");
            // Difficulty badges
            Badges.winDifficultyBadges(
                DifficultyBuilder.getAccDifficulty(),
                DifficultyBuilder.getWordsDifficulty(),
                DifficultyBuilder.getTimeDifficulty(),
                DifficultyBuilder.getConfDifficulty());
            Badges.checkWinTime(game.getTimeRemaining());
          } else {
            System.out.println("LOST");
            wordLabel.setText(getLossMessage());
            // Set the loss css to the timer bar
            timerBarLabel.getStyleClass().clear();
            timerBarLabel.getStyleClass().add("timerBarLoss");
            // Increment losses and reset consecutive wins to 0
            Users.increaseLosses();
            Users.setConsistentWins(0);
            Users.addTimeHistory(0, game.getCurrentWord());
          }
          Users.addGameDifficultyHistory(
              DifficultyBuilder.getAccDifficulty().toString(),
              DifficultyBuilder.getWordsDifficulty().toString(),
              DifficultyBuilder.getTimeDifficulty().toString(),
              DifficultyBuilder.getConfDifficulty().toString());
          Users.saveUser();
          // Reset blitz objects if we are playing blitz
          Game.resetBlitzCounter();
          Game.resetBlitzTime();
        });
  }

  /**
   * Everyone loves a winner. Also calculates the time taken to draw the winning image.
   *
   * @return A string informing the user they have won and how much time they took.
   */
  private String getWinMessage() {
    // Check to see if we are playing blitz
    if (currentGameMode == GameMode.BLITZ) {
      return "You got "
          + Game.getBlitzCounter()
          + " words in "
          + (CategorySelector.getTime() - game.getTimeRemaining())
          + " seconds!";
    }
    // Otherwise for all other game modes
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
    } else if (currentGameMode == GameMode.BLITZ) {
      return "Unlucky! You got no words in " + CategorySelector.getTime() + " seconds!";
    }
    // For all other game modes
    return "Out of time! Play again?";
  }

  /** This method sets up a new game to be started */
  @FXML
  protected void onStartGame(ActionEvent event) {
    // Ghost game pretends it is drawing to load and call first update of game services to pre-load
    // them
    if (game.getIsGhostGame()) {
      isDrawing = true;
    }
    // Start the game
    if (currentGameMode != GameMode.PROFILE) {
      game.startGame();
    }
    if (currentGameMode == GameMode.ZEN) {
      postGameBox.setMouseTransparent(false);
      postGameBox.setVisible(true);
    }

    canvas.setDisable(false);
    updateToolTip();
    // Turn on to brush mode regardless of what it was
    onBrush();

    // Enable drawing tools
    drawingToolsBox.setDisable(false);

    // Hide pre-game buttons
    preGameBox.setVisible(false);

    canvas.setOnMousePressed(
        e -> {
          currentX = e.getX();
          currentY = e.getY();
          isDrawing = true;
        });

    canvas.setOnMouseDragged(
        e -> {

          // Brush size (you can change this, it should not be too small or too large).
          double size = brushSize;

          final double x = e.getX() - size / 2;
          final double y = e.getY() - size / 2;
          if (isDrawing) {
            // This is the colour of the brush.
            if (brush) {
              graphic.setStroke(colourPicker.getValue());
              graphic.setLineWidth(size);
              graphic.strokeLine(currentX, currentY, x, y);

            } else {
              // Select eraser's properties
              size = eraserSize;
              graphic.clearRect(x, y, size, size);
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
   * @param event takes in an event to return back to the menu
   * @throws IOException If the model cannot be found on the file system.
   * @throws URISyntaxException converting to link exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws ModelException doodle prediction exception
   * @throws WordNotFoundException word was not found exception
   */
  @FXML
  private void onBackToMenuStart(ActionEvent event)
      throws IOException, URISyntaxException, CsvException, ModelException {

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

  /**
   * onBack will return back to main menu or creation page
   *
   * @param event takes in an event to return back to the menu
   * @throws IOException reading/writing exception
   * @throws URISyntaxException converting to link exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws ModelException doodle prediction exception
   */
  @FXML
  private void onBack(ActionEvent event)
      throws IOException, URISyntaxException, CsvException, ModelException {

    // Clear the hidden word game mode hints and labels
    if (currentGameMode == GameMode.HIDDEN_WORD) {
      destroyDefLabel();
      wordCharacters.clear();
    }

    // Clear the profile creation game modes
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

  /**
   * onBackToMenu will go back to main menu
   *
   * @param event takes in the action event click
   */
  @FXML
  private void onBackToMenu(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }

  /**
   * onBackToCreation will take us back to profile creation scene
   *
   * @param event takes in action event click
   */
  @FXML
  private void onBackToCreation(ActionEvent event) {
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
   * @throws IOException If the model cannot be found on the file system.
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
      App.getCreationController().updateImage();
      onBackToCreation(event);
    }
  }

  /** Update the tool tip in the hidden word game */
  public void updateToolTip() {
    // Check which game mode we are in
    switch (currentGameMode) {
      case BLITZ:
        gameToolTip.setText("Draw as many words as you can in the given time!");
        break;
      case HIDDEN_WORD:
        // Check if the canvas is enabled
        if (!canvas.isDisable()) {
          if (hints < 3) {
            // Set the tool tip to say there is a hint
            gameToolTip.setText("CLICK FOR A HINT! You have " + (3 - hints) + " remaining!");
          } else {
            gameToolTip.setText("You have no hints remaining!");
          }
        } else {
          // Otherwise say the default hidden_word message
          gameToolTip.setText(
              "The word is hidden! From the definition, draw the word! Click this during the game for a hint!");
        }
        break;
      case NORMAL:
        // Normal tool tip message
        gameToolTip.setText("Draw the word and try to win!");
        break;
      case PROFILE:
        // Profile tool tip message
        gameToolTip.setText("Draw your profile picture!");
        break;
      case ZEN:
        // Zen tool tip message
        gameToolTip.setText("Feel free to draw!!!");
        break;
      default:
        // Normal tool tip message
        gameToolTip.setText("Draw the word and try to win!");
        break;
    }
  }

  /**
   * onWordHint will give a hint to the player upon request
   *
   * @param event takes in a mouse click to ask for hint
   */
  @FXML
  private void onWordHint(MouseEvent event) {
    // Initialise a random number at 0
    int randomNumber = 0;
    // Check if the person is playing hidden word gamemode
    if (!canvas.isDisable() && currentGameMode == GameMode.HIDDEN_WORD && hints < 3) {
      hints++;
      updateToolTip();
      playButtonClick();
      // Find the length of their current word
      int wordLength = game.getCurrentWord().length();
      // Place underscores the length of their word as the first hint
      String hiddenWord = String.format("%s", "_".repeat(wordLength));
      if (game.getCurrentPrompt().equals("Guess the word then draw it!")) {
        game.setCurrentPrompt(hiddenWord);
      } else {
        // loop until all the characters are filled in
        while (wordCharacters.size() != wordLength) {
          // Generate a random number not in the word characters array
          randomNumber = new Random().nextInt(wordLength);
          if (!wordCharacters.contains(randomNumber)) {
            // Add the random number into the array
            wordCharacters.add(randomNumber);
            break;
          }
        }
        // Get the corresponding letter from the word at the position
        char wordCharacter = game.getCurrentWord().charAt(randomNumber);
        // Get the current prompt (partially made up of dashes)
        String currentPrompt = game.getCurrentPrompt();
        // Set up the current prompt to reveal a letter
        currentPrompt =
            currentPrompt.substring(0, randomNumber)
                + wordCharacter
                + currentPrompt.substring(randomNumber + 1);
        // Update the current prompt
        game.setCurrentPrompt(currentPrompt);
      }
    }
  }

  /**
   * onToggleTextToSpeech will toggle the text to speech voice, by default tts is on
   *
   * @param event takes in a JavaFX action event
   */
  @FXML
  private void onToggleTextToSpeech(ActionEvent event) {
    // Clear the current style
    textSpeechButton.getStyleClass().clear();
    if (Game.getTextToSpeech()) {
      // Change the styling to off
      textSpeechButton.getStyleClass().add("voiceAssistantOff");
    } else {
      // Change the styling to on
      textSpeechButton.getStyleClass().add("voiceAssistantOn");
    }
    // Toggle the sound
    Game.toggleTextToSpeech();
  }
}
