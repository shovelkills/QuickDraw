package nz.ac.auckland.se206;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.opencsv.exceptions.CsvException;

import ai.djl.ModelException;
import ai.djl.modality.Classifications.Classification;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

/**
 * This is the controller of the canvas. You are free to modify this class and
 * the corresponding FXML file as you see fit. For example, you might no longer
 * need the "Predict" button because the DL model should be automatically
 * queried in the background every second.
 *
 * <p>
 * !! IMPORTANT !!
 *
 * <p>
 * Although we added the scale of the image, you need to be careful when
 * changing the size of the drawable canvas and the brush size. If you make the
 * brush too big or too small with respect to the canvas size, the ML model will
 * not work correctly. So be careful. If you make some changes in the canvas and
 * brush sizes, make sure that the prediction works fine.
 */
public class CanvasController {

	// Define FXML fields
	@FXML
	private Canvas canvas;
	@FXML
	private Label titleLabel;
	@FXML
	private Label wordLabel;
	@FXML
	private Label timerLabel;
	@FXML
	private GridPane predictionGrid;
	@FXML
	private Button startButton;
	@FXML
	private Button restartButton;
	@FXML
	private Button brushButton;
	@FXML
	private Pane canvasPane;
	@FXML
	private Button clearButton;
	@FXML
	private ChoiceBox<String> difficultyMenu;

	// Define game object
	private Game game;

	// Define other UI data
	private GraphicsContext graphic;
	private final HashMap<Difficulty, String> difficultySettingsMap = new HashMap<Difficulty, String>();
	private boolean brush;
	private boolean isDrawing;
	private double currentX;
	private double currentY;
	private Color predictionListColor = Color.DARKSLATEBLUE;
	private Color predictionHighlightColor = Color.web("#008079");
	private Color predictionTextColor = Color.WHITE;

	// Task for alternating colour of the title and word label concurrently
	private Task<Void> alternateColoursTask = new Task<Void>() {

		@Override
		protected Void call() throws Exception {
			// Set two labels to alternate between colours
			alternateColours(titleLabel, Color.BEIGE, Color.RED);
			alternateColours(wordLabel, Color.web("#56d6ff"), Color.web("b669ff"));
			return null;
		}
	};

	/**
	 * JavaFX calls this method once the GUI elements are loaded. In our case we
	 * create a listener for the drawing, and we load the ML model.
	 *
	 * @throws ModelException     If there is an error in reading the input/output
	 *                            of the DL model.
	 * @throws IOException        If the model cannot be found on the file system.
	 * @throws URISyntaxException If the URI cannot be found
	 * @throws CsvException       If the Csv cannot be found
	 */
	public void initialize() throws IOException, URISyntaxException, CsvException, ModelException {
		// Instantiate a new game object on first opening the scene
		game = new Game(this);
		// Build difficulty settings map for the dropdown
		difficultySettingsMap.put(Difficulty.E, "EASY");
		difficultySettingsMap.put(Difficulty.M, "MEDIUM");
		difficultySettingsMap.put(Difficulty.H, "HARD");
		difficultySettingsMap.put(Difficulty.MS, "MASTER");
		// Populate difficulty dropdown
		difficultyMenu.getItems().add(difficultySettingsMap.get(Difficulty.E));
		difficultyMenu.getItems().add(difficultySettingsMap.get(Difficulty.M));
		difficultyMenu.getItems().add(difficultySettingsMap.get(Difficulty.H));
		difficultyMenu.getItems().add(difficultySettingsMap.get(Difficulty.MS));
		// Get the graphic from the canvas
		graphic = canvas.getGraphicsContext2D();
		// Add the canvasPane's css styling to the object
		canvasPane.getStyleClass().add("canvasPane");
		// Load in the font
		Font font = Font.loadFont("file:src/main/resources/fonts/somethingwild-Regular.ttf", 100);
		// Update title's font
		titleLabel.setFont(font);

		// Set up the pre-game UI elements that are in common with restarting the game
		setPreGameInterface();

		// Create a new thread for the alternating colours task
		Thread colourThread = new Thread(alternateColoursTask);
		// Allow the task to be cancelled on closing of application
		colourThread.setDaemon(true);
		// Start the colour task
		colourThread.start();
	}

	/**
	 * Sets UI elements which are common to the initialisation of the scene and
	 * restarting the game.
	 */
	private void setPreGameInterface() {
		// Bind label properties to game properties
		wordLabel.textProperty().bind(game.getCurrentPromptProperty());
		timerLabel.textProperty().bind(game.getTimeRemainingAsStringBinding());
		// Set UI elements for pre-game
		canvas.setDisable(true);
		restartButton.setVisible(false);
		startButton.setVisible(true);
		startButton.setDisable(false);
		brushButton.setVisible(false);
		difficultyMenu.setVisible(true);
		// Select last played difficulty (default EASY if new game)
		difficultyMenu.setValue(difficultySettingsMap.get(Game.getDifficulty()));
		onDifficultySelect();
	}

	/**
	 * Returns whether the user has started drawing.
	 *
	 * @return true if the canvas has been clicked since the game start or last
	 *         onClear.
	 */
	public boolean getIsDrawing() {
		return isDrawing;
	}

	/** Set the game difficulty through the UI dropdown, update the word label */
	@FXML
	private void onDifficultySelect() {
		// gets the current difficulty we are on from the menu
		switch (difficultyMenu.getValue()) {
		// Sets the difficulty of the game to easy
		case "EASY":
			game.setDifficulty(Difficulty.E);
			break;
		// Sets the difficulty of the game to medium
		case "MEDIUM":
			game.setDifficulty(Difficulty.M);
			break;
		// Sets the difficulty of the game to hard
		case "HARD":
			game.setDifficulty(Difficulty.H);
			break;
		// Sets the difficulty of the game to master
		case "MASTER":
			game.setDifficulty(Difficulty.MS);
			break;
		default:
			game.setDifficulty(Difficulty.E);
		}
	}

	/** This function toggles from brush to eraser */
	@FXML
	private void onBrushChange() {
		// Toggle the brush
		brush = !brush;
		brushButton.setText(brush ? "Eraser" : "Brush");
	}

	/**
	 * This method is called when the "Clear" button is pressed. Resets brush to
	 * "Brush" mode.
	 */
	@FXML
	private void onClear() {
		// Clear the canvas
		graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		clearPredictionGrid();
		isDrawing = false;
		if (brushButton.getText().equals("Brush")) {
			onBrushChange();
		}
	}

	/**
	 * Updates the prediction grid display to show the top 10 entries of the input
	 * List.
	 *
	 * @param predictionList List of classifications from the machine learning model
	 */
	public void updatePredictionGridDisplay(List<Classification> predictionList) {
		clearPredictionGrid();
		for (int i = 0; i < predictionList.size(); i++) {
			String prediction = predictionList.get(i).getClassName();
			// Check if the prediction is the prompt word to determine label color
			boolean isPrompt = prediction.replaceAll("_", " ").equals(game.getCurrentPrompt()) ? true : false;
			// Create a formatted String for the prediction label
			String predictionEntry = (i + 1) + ". " + predictionList.get(i).getClassName().replaceAll("_", " ");
			// Create and add the prediction label to its cell in the grid, row order is top
			// to bottom :
			// highest to lowest rank
			predictionGrid.add(createPredictionLabel(predictionEntry, isPrompt), 0, i);
		}
	}

	/**
	 * Creates a label for the prediction grid. The background of the label will
	 * fill the whole cell of the grid.
	 *
	 * @param labelText  The text value for the label
	 * @param labelColor The color for the label background
	 * @return Label object
	 */
	private Label createPredictionLabel(String labelText, boolean isPrompt) {
		// Set color for label depending on whether it is the prompt word
		Color labelColor = isPrompt ? predictionHighlightColor : predictionListColor;
		// Create new label and configure formatting
		Label label = new Label();
		label.setText(labelText);
		label.setTextFill(predictionTextColor);
		label.setFont(Font.font(Font.getDefault().getFamily(), isPrompt ? FontWeight.BOLD : FontWeight.MEDIUM, 16));
		label.setPadding(new Insets(0, 0, 0, 10));
		// Ensure the label background fills the entire cell
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		label.setBackground(new Background(new BackgroundFill(labelColor, null, null)));
		return label;
	}

	/**
	 * Clears the prediction grid of entries and fills it with empty string entries.
	 */
	private void clearPredictionGrid() {
		// Remove all child nodes from the GridPane
		predictionGrid.getChildren().clear();
		// Fill the GridPane with empty Label nodes
		for (int i = 0; i < predictionGrid.getRowCount(); i++) {
			predictionGrid.add(createPredictionLabel(" ", false), 0, i);
		}
	}

	/**
	 * This method will restart the game once the player presses the button
	 *
	 * @throws ModelException
	 * @throws CsvException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@FXML
	private void onRestartGame() throws IOException, URISyntaxException, CsvException, ModelException {

		// Clear the canvas
		onClear();
		// Clear the prediction grid
		clearPredictionGrid();
		// Reset game variables and concurrent service
		game.resetGame();
		// Reset UI elements
		setPreGameInterface();
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
		final BufferedImage imageBinary = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);

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
		// Create a file chooser
		FileChooser fileChooser = new FileChooser();

		// Set extension filter for bmp files
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("BMP files (*.bmp)", "*.bmp");
		fileChooser.getExtensionFilters().add(extFilter);
		// Set the initial file name
		fileChooser.setInitialFileName("snapshot" + System.currentTimeMillis());
		// Set the initial directory
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fileChooser.setInitialDirectory(new File(currentPath));
		// Show save file dialog
		File file = fileChooser.showSaveDialog(null);
		// Write it to the location
		if (file != null) {
			ImageIO.write(getCurrentSnapshot(), "bmp", file);
		}
		return file;
	}

	/**
	 * Styling Function: Alternate colours of a label between two colours
	 *
	 * @param label   the label that will alternate between colours
	 * @param colour1 the first colour it will be
	 * @param colour2 the second colour it will become
	 */
	private void alternateColours(Label label, Color colour1, Color colour2) {
		// Create a new timeline object
		Timeline timeline = new Timeline();
		// Loop three times
		for (int i = 0; i < 3; i++) {
			// Add a new keyframe accordingly
			timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2.5 * i),
					new KeyValue(label.textFillProperty(), i != 1 ? colour1 : colour2)));
		}
		// Play the animation indefinitely
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	/**
	 * Called when the game ends. <br>
	 * - Updates UI to give feedback to user on whether they won or lost, plus their
	 * time to draw if won <br>
	 * - Prompts user with a pop-up giving them the option to save their drawing
	 *
	 * @return isWin The win flag from the game controller, is true if the game was
	 *         won and false if lost
	 * @throws InterruptedException
	 */
	public void onEndGame(boolean isWin) {
		Platform.runLater(() -> {
			// Set UI elements for post-game
			canvas.setDisable(true);
			brushButton.setVisible(false);
			clearButton.setVisible(false);
			restartButton.setVisible(true);

			// Unbind label properties bound to game properties
			wordLabel.textProperty().unbind();
			timerLabel.textProperty().unbind();

			// Update the word label to display a win or loss message for the user at the
			// end of the
			// game.
			if (isWin) {
				wordLabel.setText(getWinMessage());
			} else {
				System.out.println("LOST");
				wordLabel.setText(getLossMessage());
			}

			// Create a new alert
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			// Set up the alert accordingly
			alert.setTitle("Save File");
			alert.setHeaderText("Would you like to save your image?");
			alert.setResizable(false);
			alert.setContentText("Select OK or Cancel.");
			// Show the alert
			Optional<ButtonType> result = alert.showAndWait();
			// Check if the person presses OK
			if (result.get() == ButtonType.OK) {
				try {
					// Save the current canvas as a file in the tmp folder
					saveCurrentSnapshotOnFile();
				} catch (IOException e) {

					e.printStackTrace();
				}
			} else if (result.get() == ButtonType.CANCEL) {
				// Do nothing if cancelled is pressed
				return;
			}
		});
	}

	/**
	 * Everyone loves a winner. Also calculates the time taken to draw the winning
	 * image.
	 *
	 * @return A string informing the user they have won and how much time they
	 *         took.
	 */
	private String getWinMessage() {
		return "You won! You drew " + game.getCurrentPrompt() + " in " + (60 - game.getTimeRemaining()) + " seconds!";
	}

	/**
	 * Tells 'em to try again.
	 *
	 * @return A string that is polite, professional, and honest.
	 */
	private String getLossMessage() {
		return "You lost! Press restart to try another word!";
	}

	/** This method sets up a new game to be started */
	@FXML
	private void onStartGame() {
		game.startGame();

		canvas.setDisable(false);
		// Turn on to brush mode regardless of what it was
		brush = true;
		brushButton.setText("Eraser");

		// Change the visibilities of buttons according to brief
		clearButton.setVisible(true);
		startButton.setVisible(false);
		brushButton.setVisible(true);
		difficultyMenu.setVisible(false);

		// Get eraser colour
		Background currentBackground = canvasPane.getBackground();
		Paint eraserColour = currentBackground.getFills().get(0).getFill();
		canvas.setOnMousePressed(e -> {
			currentX = e.getX();
			currentY = e.getY();
			isDrawing = true;
		});

		canvas.setOnMouseDragged(e -> {

			// Brush size (you can change this, it should not be too small or too large).
			double size = 5.0;

			final double x = e.getX() - size / 2;
			final double y = e.getY() - size / 2;

			// This is the colour of the brush.
			if (brush) {
				graphic.setFill(Color.BLACK);
				graphic.setLineWidth(size);
				graphic.strokeLine(currentX, currentY, x, y);

			} else {

				graphic.setFill(eraserColour);
				size = 10.0;
				graphic.fillOval(x, y, size, size);
			}
			currentX = x;
			currentY = y;
		});
	}
}
