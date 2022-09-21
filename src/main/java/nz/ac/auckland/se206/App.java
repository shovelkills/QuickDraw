package nz.ac.auckland.se206;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;

/**
 * This is the entry point of the JavaFX application, while you can change this
 * class, it should remain as the class that runs the JavaFX application.
 */
public class App extends Application {
	public static void main(final String[] args) {
		launch();
	}

	/**
	 * Returns the node associated to the input file. The method expects that the
	 * file is located in "src/main/resources/fxml".
	 *
	 * @param fxml The name of the FXML file (without extension).
	 * @return The node of the input file.
	 * @throws IOException If the file is not found.
	 */
	private static Parent loadFxml(final String fxml) throws IOException {
		return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
	}

	/**
	 * This method is invoked when the application starts. It loads and shows the
	 * "Canvas" scene.
	 *
	 * @param stage The primary stage of the application.
	 * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
	 */
	@Override
	public void start(final Stage stage) throws IOException {
		// Adds the menu and canvas UI to the scene manager
		SceneManager.addUi(AppUi.MAIN_MENU, loadFxml("menu"));
		SceneManager.addUi(AppUi.GAME, loadFxml("canvas"));
		SceneManager.addUi(AppUi.USERSELECT, loadFxml("users"));
		// Creates a new scene that starts on the main menu in windowed mode
		final Scene scene = new Scene(SceneManager.getUiRoot(AppUi.MAIN_MENU), SceneManager.getMaxWindowedWidth(),
				SceneManager.getMaxWindowedHeight());
		// Adds the css formatting to the scenes
		scene.getStylesheets().add("/css/menu.css");
		scene.getStylesheets().add("/css/canvas.css");

		// Set the scene and then show
		stage.setTitle("Draw Game");
		stage.setScene(scene);
		stage.show();
	}
}
