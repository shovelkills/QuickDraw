package nz.ac.auckland.se206;

import static nz.ac.auckland.se206.ml.DoodlePrediction.printPredictions;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.opencsv.exceptions.CsvException;

import ai.djl.ModelException;
import ai.djl.modality.Classifications;
import ai.djl.modality.Classifications.Classification;
import ai.djl.translate.TranslateException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import nz.ac.auckland.se206.ml.DoodlePrediction;
import nz.ac.auckland.se206.speech.TextToSpeech;
import nz.ac.auckland.se206.words.CategorySelector;
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
	// Define fields and variables used in the controller
	private String currentWord;
	private GraphicsContext graphic;
	private DoodlePrediction model;
	private int timerCount;
	private boolean brush;
	private List<Classification> predictionResults = null;
	private boolean startedDrawing = false;
	private boolean spoken = false;
	private double currentX;
	private double currentY;

	private Task<Void> speechTask = new Task<Void>() {
		@Override
		protected Void call() throws Exception {
			// Initialise a text to speech instance
			TextToSpeech textToSpeech = new TextToSpeech();
			// Run indefinitely
			while (true) {
				// When starting speak that its starting
				if (timerCount == 59) {
					textToSpeech.speak("Starting");
				} else if (timerCount == 31) {
					// When half way speak thats it is halfway
					textToSpeech.speak("Thirty Seconds Remaining");
				}
				// Speak if the person has won
				if (wordLabel.getText().equals("YOU WIN!") && !spoken) {
					textToSpeech.speak("You Won!");
					// Set that it has spoken
					spoken = true;

				} else if (wordLabel.getText().equals("YOU LOST!") && !spoken) {
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

	// Create a reusable service
	private Service<Void> service = new Service<Void>() {
		// Create a task method
		protected Task<Void> createTask() {
			// Create a new task
			return new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// Initialise a timer starting at 60
					timerCount = 60;
					while (timerCount != 0) {

						Platform.runLater(() -> {
							try {
								// Get the current top 10 predictions
								predictionResults = model.getPredictions(getCurrentSnapshot(), 10);

								// Update the timer counter
								wordLabel.setText(String.format("%s\n%s", currentWord, Integer.toString(timerCount)));

								// Check if the user has started drawing
								if (startedDrawing) {
									// Display the top 10 predictions
									predictText(predictionResults);
									// Check if the player has won
									if (isWin(model.getPredictions(getCurrentSnapshot(), 3))) {
										wordLabel.setText("YOU WIN!");
										// Handle the end game
										onEnd();
										return;
									}
								}
							} catch (InterruptedException | TranslateException e) {
								e.printStackTrace();
							}
						});
						// Decrease timer count
						timerCount--;
						// Sleep for 1 second
						Thread.sleep(1000);
					}
					// Check if player has lost
					if (timerCount == 0) {

						Platform.runLater(() -> {
							wordLabel.setText("YOU LOST!");
							try {
								// Handle the end game
								onEnd();
							} catch (InterruptedException e) {

								e.printStackTrace();
							}
						});
					}
					return null;
				}
			};
		}
	};

	// Create an alternating colour background task
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
	public void initialize() throws ModelException, IOException, URISyntaxException, CsvException {
		// Disable the canvas on start up and turn off game features
		canvas.setDisable(true);
		brushButton.setVisible(false);
		// Create a new task that alternates between colours
		Thread colourTask = new Thread(alternateColoursTask);
		// Allow the task to be cancelled on closing of application
		colourTask.setDaemon(true);
		// Start the colour task
		colourTask.start();
		Thread speechBackgroundTask = new Thread(speechTask);
		speechBackgroundTask.setDaemon(true);
		speechBackgroundTask.start();
		// Get the graphic from the canvas
		graphic = canvas.getGraphicsContext2D();
		// Add the canvasPane's css styling to the object
		canvasPane.getStyleClass().add("canvasPane");
		// Load in the font
		Font font = Font.loadFont("file:src/main/resources/fonts/somethingwild-Regular.ttf", 150);
		// Update title's font
		titleLabel.setFont(font);
		// Get a random word
		getRandomWord();
	}

	private void getRandomWord() throws IOException, URISyntaxException, CsvException, ModelException {
		// Get a new model
		model = new DoodlePrediction();
		// Instantiate a category selector object
		CategorySelector categorySelector = new CategorySelector();
		// Choose a random with from the easy category
		String randomWord = categorySelector.getRandomCategory(Difficulty.E);
		// Set the label in the GUI to display the random word
		wordLabel.setText(randomWord);
		// set the target word to the random word generated
		currentWord = randomWord;
	}

	/** This method is called when the "Clear" button is pressed. */
	@FXML
	private void onClear() {
		// Clear the canvas
		graphic.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		startedDrawing = false;
	}

	/**
	 * This method will restart the game once the player presses the button
	 *
	 * @throws ModelException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws CsvException
	 */
	@FXML
	private void onRestartGame() throws ModelException, IOException, URISyntaxException, CsvException {
		// Clear the canvas
		onClear();
		// Reset the started Drawing boolean
		startedDrawing = false;
		// Call the getRandomWord method that will generate a random word
		getRandomWord();
		// Reset all the visibilities of the buttons
		clearButton.setVisible(false);
		startButton.setVisible(true);
		restartButton.setVisible(false);
		brushButton.setVisible(false);
		service.cancel();
		canvas.setDisable(true);
	}

	/**
	 * This method predicts the text that will be displayed in the GUI
	 *
	 * @param predictions
	 * @throws TranslateException
	 */
	private void predictText(List<Classification> predictions) throws TranslateException {
		// Creates a new string builder
		final StringBuilder sb = new StringBuilder();
		// Appends a line separator
		sb.append(System.lineSeparator());
		int counter = 1;
		// Loops through all the predictions given
		for (final Classifications.Classification classification : predictions) {
			// Appends the predictions into the string builder
			String className = classification.getClassName();
			sb.append(String.format("%d. %s", counter,
					className.contains("_") ? className.replace("_", " ") : className));
			sb.append(System.lineSeparator());
			counter++;
		}

		//TODO Update the predictions grid
	}
	
	private void updatePredictionGridDisplay(List<Classification> predictions) {
		
	}

	/**
	 * This method executes when the user clicks the "Predict" button. It gets the
	 * current drawing, queries the DL model and prints on the console the top 5
	 * predictions of the DL model and the elapsed time of the prediction in
	 * milliseconds.
	 *
	 * @throws TranslateException If there is an error in reading the input/output
	 *                            of the DL model.
	 */
	@FXML
	private void onPredict() throws TranslateException {
		// Print out the top 5 predictions
		System.out.println("==== PREDICTION  ====");
		System.out.println("Top 5 predictions");
		// Get the start time
		final long start = System.currentTimeMillis();
		// Get the top 3 predictions
		List<Classification> predictionResults = model.getPredictions(getCurrentSnapshot(), 3);
		// Print the top 5 predictions
		printPredictions(model.getPredictions(getCurrentSnapshot(), 5));
		// Print win or lost
		System.out.println(isWin(predictionResults) ? "WIN" : "LOST");
		// Print how long the prediction took
		System.out.println("prediction performed in " + (System.currentTimeMillis() - start) + " ms");
	}

	/**
	 * Method that checks if the player has won the game
	 *
	 * @param classifications
	 * @return
	 */
	private boolean isWin(List<Classification> classifications) {
		// Loop through all the predictions
		for (Classification classification : classifications) {
			// Corrects word if it contains an underscore
			String className = classification.getClassName();
			className = className.contains("_") ? className.replace("_", " ") : className;
			// Check if the current word is prediction
			if (className.equals(currentWord)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the current snapshot of the canvas.
	 *
	 * @return The BufferedImage corresponding to the current canvas content.
	 */
	private BufferedImage getCurrentSnapshot() {
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
	 * This method is called when the game ends and asks if they want to save their
	 * current canvas as a file
	 *
	 * @throws InterruptedException
	 */
	@FXML
	private void onEnd() throws InterruptedException {
		canvas.setDisable(true);
		service.cancel();
		brushButton.setVisible(false);
		spoken = false;
		Platform.runLater(() -> {
			// Turn off the visibility for the clear button
			clearButton.setVisible(false);
			restartButton.setVisible(true);
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

	/** This function toggles from brush to eraser */
	@FXML
	private void onBrushChange() {
		// Toggle the brush
		brush = !brush;

		Platform.runLater(() -> {
			// Change the text for the brush button
			if (!brush) {
				brushButton.setText("Brush");
			} else {
				brushButton.setText("Eraser");
			}
		});
	}

	/** This method sets up a new game to be started */
	@FXML
	private void onStartGame() {
		canvas.setDisable(false);
		// Turn on to brush mode regardless of what it was
		brush = true;
		brushButton.setText("Eraser");
		// Change the visibilities of buttons according to brief
		clearButton.setVisible(true);
		startButton.setVisible(false);
		brushButton.setVisible(true);

		// Reset and start the service
		service.reset();
		service.start();
		// Get eraser colour
		Background currentBackground = canvasPane.getBackground();
		Paint eraserColour = currentBackground.getFills().get(0).getFill();
		canvas.setOnMousePressed(e -> {
			currentX = e.getX();
			currentY = e.getY();
		});
		canvas.setOnMouseDragged(e -> {
			startedDrawing = true;

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
