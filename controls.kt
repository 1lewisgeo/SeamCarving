import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Some UI controls
 */
class Controls : View() {

    val controller: MainController by inject()

    override val root: Parent = hbox {

        id = "controlroot"

        vbox {

            id = "controls"

            hgrow = Priority.ALWAYS

            button("Horizontal Seam Carve (H Button)") {
                action { fire(SeamRequestEvent(Type.H)) }
                accelerators.put(kc("H")) { fire() }
                enableWhen { controller.imageProp.isNotNull }
            }

            button("Vertical Seam Carve (V Button)") {
                action { fire(SeamRequestEvent(Type.V)) }
                accelerators.put(kc("V")) { fire() }
                enableWhen { controller.imageProp.isNotNull }
            }

            checkbox("Show Seams") { controller.showSeams.bind(selectedProperty()) ; isSelected = true }

            button("Resizable Pop-up (experimental & buggy)") {

                action { find(popup::class).openWindow(resizable = true)?.let { it.width = controller.image.width ; it.height = controller.image.height } }

                enableWhen { controller.imageProp.isNotNull }

            }

            button("v insert (experimental)") {
                action { controller.verticalInsert(10) }
                enableWhen { controller.imageProp.isNotNull }
            }

            button("Toggle energy (stub)") {
                action { controller.energyView.set(!controller.energyView.get()) }
                enableWhen { controller.imageProp.isNotNull }
            }

            button("Save Image...") {
                action { saveImage() }
                enableWhen { controller.imageProp.isNotNull }
            }

            button("Choose Image...") {
                action { chooseImage() }
            }

        }

        borderpane {

            id = "info"

            hgrow = Priority.ALWAYS

            //enableWhen { controller.imageProp.isNotNull }

            top = label() { textProperty().bind(find(MainController::class).locationProp.stringBinding { if (it == null) "No Image" else it.toString() }) ; id = "url" ; borderpaneConstraints { alignment = Pos.TOP_CENTER } }

            center = vbox {

                    // It's structured like this because the file location was meant to be the

                    id = "infobox"

                    alignment = Pos.TOP_CENTER

                    //style { backgroundColor += Color.ORANGE }

                    label("No Image") { subscribe<ImageModifiedEvent> { text = "Image Dimensions: ${controller.image.width.toInt()} x ${controller.image.height.toInt()}" } /*; style { backgroundColor += Color.YELLOW }*/ }

                }

        }
    }
}