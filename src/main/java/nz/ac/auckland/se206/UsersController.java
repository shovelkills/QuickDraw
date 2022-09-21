package nz.ac.auckland.se206;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UsersController {

	@FXML
	private ImageView userProfileImage1;
	@FXML
	private ImageView userProfileImage2;
	@FXML
	private ImageView userProfileImage3;
	@FXML
	private Button createProfileButton1;
	@FXML
	private Button createProfileButton2;
	@FXML
	private Button createProfileButton3;
	@FXML
	private TextField userNameInput1;
	@FXML
	private TextField userNameInput2;
	@FXML
	private TextField userNameInput3;
	@FXML
	private Label userName1;
	@FXML
	private Label userName2;
	@FXML
	private Label userName3;

	private List<String> usersList = Users.getUserList();
	private ArrayList<TextField> userNameInputs = new ArrayList<TextField>();
	private ArrayList<ImageView> userProfileImages = new ArrayList<ImageView>();
	private ArrayList<Button> userCreateButtons = new ArrayList<Button>();
	private ArrayList<Label> userNameLabels = new ArrayList<Label>();
	private File defaultImageFile;
	private Image defaultImage;
	private File userImageFile;
	private Image userImage;

	public void initialize() {
		defaultImageFile = new File("src/main/resources/users/default.png");
		defaultImage = new Image(defaultImageFile.toURI().toString());
		userImageFile = new File("src/main/resources/users/happy.png");
		userImage = new Image(userImageFile.toURI().toString());

		Collections.addAll(userProfileImages, userProfileImage1, userProfileImage2, userProfileImage3);
		Collections.addAll(userNameInputs, userNameInput1, userNameInput2, userNameInput3);
		Collections.addAll(userCreateButtons, createProfileButton1, createProfileButton2, createProfileButton3);
		Collections.addAll(userNameLabels, userName1, userName2, userName3);
		for (ImageView userProfileImage : userProfileImages) {
			userProfileImage.setImage(defaultImage);

		}
		for (int i = 0; i < usersList.size(); i++) {
			userProfileImages.get(i).setImage(userImage);
			userNameLabels.get(i).setText(usersList.get(i));
			userCreateButtons.get(i).setVisible(false);
			userNameInputs.get(i).setVisible(false);
		}

	}

	@FXML
	private void onCreateProfile(ActionEvent event) {
		Button button = (Button) event.getSource();
		String string = button.getId().toString();

		int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1)))) - 1;
		String username = userNameInputs.get(number).getText();

		if (!Users.createUser(username)) {
			System.out.println("Not allowed");
			return;
		}
		userProfileImages.get(number).setImage(userImage);
		userCreateButtons.get(number).setVisible(false);
		userNameInputs.get(number).setVisible(false);
		userNameLabels.get(number).setText(usersList.get(number));

		;
//		// Get the scene currently in
//		Button button = (Button) event.getSource();
//		Scene sceneButtonIsIn = button.getScene();
//		// Move to the next scene
//		sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
	}

	@FXML
	private void onExitSelection(ActionEvent event) {
		// Get the scene currently in
		Button button = (Button) event.getSource();
		Scene sceneButtonIsIn = button.getScene();
		// Move to the next scene
		sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
	}

}
