import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import tornadofx.*

class popup(): View() {

    override val root = anchorpane()

    val controller = find(MainController::class)

    override fun onDock() {

        // Notify subscribers a popup has been opened
        fire(PopupEvent(true))

        val stage = currentStage!!

        stage.width = controller.image.width + 10

        stage.height = controller.image.height + 10

        stage.minWidth = controller.image.width + 10

        stage.minHeight = controller.image.width + 10

        with (root) {

            val anchor = this

            with (find(MainController::class).image) {
                modalStage?.width = width
                modalStage?.height = height
            }

            imageview().let { it.imageProperty().bind(controller.imageProp) ; it }

            // Listen for changes in the size of the window

            stage.widthProperty()?.addListener { observableValue, old, nnew ->

                for (x in 0..(nnew.toInt() - old.toInt())) {

                    fire(SeamRequestEvent(Type.V))

                }

            }

            stage?.heightProperty()?.addListener { observableValue, old, nnew ->

                for (x in 0..(nnew.toInt() - old.toInt())) {

                    fire(SeamRequestEvent(Type.H))

                }

            }

        }


    }

    override fun onUndock() {
        // Inform subscribers the popup has been closed
        fire(PopupEvent(false))
    }

}