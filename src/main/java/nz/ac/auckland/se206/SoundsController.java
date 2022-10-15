package nz.ac.auckland.se206;

import java.io.File;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class SoundsController {

  // Define static fields for the sounds class
  private static boolean soundEffect = true;
  private static boolean music = true;

  // Create background song
  private static Media background_song = new Media(
      new File(Users.folderDirectory + "/src/main/resources/sounds/music.mp3").toURI().toString());
  private static MediaPlayer backgroundMedia = new MediaPlayer(background_song);

  // Create audio clip for hover effect
  private static AudioClip buttonHoverAudio =
      new AudioClip(new File(Users.folderDirectory + "/src/main/resources/sounds/buttonHover.mp3")
          .toURI().toString());
  // Create audio clip for click effect
  private static AudioClip buttonClickAudio =
      new AudioClip(new File(Users.folderDirectory + "/src/main/resources/sounds/buttonClick.wav")
          .toURI().toString());
  // Create ding audio effect
  private static AudioClip dingAudio = new AudioClip(
      new File(Users.folderDirectory + "/src/main/resources/sounds/ding.mp3").toURI().toString());

  // Create volume slider object
  private static Slider volumeSlider;

  public static boolean getSoundEffect() {
    return soundEffect;
  }

  /** toggleSoundEffect will turn on the sound effect or turn off the sound effect */
  public static void toggleSoundEffect() {

    SoundsController.soundEffect = !soundEffect;
  }

  public static boolean getMusic() {
    return music;
  }

  /** playBackgroundMusic will play the music in the background throughout the app */
  public static void playBackgroundMusic() {
    backgroundMedia.play();
  }

  /** stopBackgroundMusic will stop playing the music in the background throughout the app */
  public static void stopBackgroundMusic() {
    backgroundMedia.stop();
  }

  /** playButtonHover will play button hover sound effect */
  public static void playButtonHover() {
    buttonHoverAudio.play();
  }

  /** playButtonClick will play button click sound effect */
  public static void playButtonClick() {
    buttonClickAudio.play();
  }

  /** playDingEffect will play ding sound effect */
  public static void playDingEffect() {
    dingAudio.play();
  }

  /** toggleMusic will turn on the music or turn off the music */
  public static void toggleMusic() {
    // Toggles music
    SoundsController.music = !music;
    // Plays or stops the music
    if (music) {
      playBackgroundMusic();
    } else {
      stopBackgroundMusic();
    }
  }

  /**
   * soundsInitialize will initialize the volume slider to link with the background music and also
   * loop the background song to repeat
   */
  public static void soundsInitialize() {
    // get the volume slider
    volumeSlider = MenuController.getVolumeSlider();
    volumeSlider.valueProperty().addListener(new InvalidationListener() {

      @Override
      public void invalidated(Observable observable) {
        // Link the slider properties
        backgroundMedia.setVolume(volumeSlider.getValue() / 100);
      }
    });
    backgroundMedia.setOnEndOfMedia(new Runnable() {
      public void run() {
        // Loop back to beginning
        backgroundMedia.seek(Duration.ZERO);
      }
    });
  }

  // Sound effects
  /**
   * onButtonHover will play the hover sound effect
   *
   * @param event takes in an FXML event
   */
  @FXML
  protected void onButtonHover(MouseEvent event) {
    // Check if sound effect is on
    if (soundEffect) {
      SoundsController.playButtonHover();
    }
  }

  /**
   * onButtonClick will play the button click sound effect
   *
   * @param event takes in an FXML event
   */
  @FXML
  protected void onButtonClick(MouseEvent event) {
    // Check if sound effect is on
    if (soundEffect) {
      SoundsController.playButtonClick();
    }
  }

  /**
   * onDingEffect will play the ding sound effect
   *
   * @param event takes in an FXML event
   */
  @FXML
  protected void onDingEffect(MouseEvent event) {
    // Check if sound effect is on
    if (soundEffect) {
      SoundsController.playDingEffect();
    }
  }
}
