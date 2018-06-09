import javafx.application.Platform
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.stage.FileChooser
import tornadofx.*
import java.net.URL

/**
 * The menu bar at the top
 */
class Menu(): View() {

    override val root = menubar {

        menu("File") {

            menu("New") {

                item("From File...", "Ctrl+O").action { chooseImage() }

                item("From Url... (EXPERIMENTAL)").action { // this doesnt actually work properly

                    TextInputDialog().run {
                        title = "Input URL"
                        headerText = "Input an Image URL"
                        contentText = "URL:"
                        dialogPane.lookupButton(ButtonType.OK).isDisable = true
                        val context = ValidationContext().also {
                            it.addValidator(editor, editor.textProperty()) {
                                if (it?.let { it.matches(Regex("https?://.*\\.(jpg|png|jpeg)")) } ?: false) null else error("Not a valid imageProp url")
                            }.valid.onChange {
                                this.dialogPane.lookupButton(ButtonType.OK).isDisable = !it
                            }
                        }
                        this
                    }.showAndWait().ifPresent { it -> find(MainController::class).imageProp.set(Image(it, false).writable()); fire(ImageSetEvent(URL(it))) }

                }

            }

            item("Save Image...", "Ctrl+S").action { saveImage() }

            separator()

            item("Exit", "Ctrl+Q").action { Platform.exit(); System.exit(0) }

        }

    }

}