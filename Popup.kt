import javafx.scene.Parent
import tornadofx.*

class popup(): View() {

    override val root = anchorpane {

        println("Create popup")

        fire(PopupEvent(true))

        val anchor = this

        with (find(MainController::class).image) {
            modalStage?.width = width
            modalStage?.height = height
        }

        imageview(find(MainController::class).image)

        modalStage?.widthProperty()?.addListener { observableValue, old, nnew ->

            for (x in 0..(nnew.toInt() - old.toInt())) {

                fire(SeamRequestEvent(Type.V))

            }

        }

        modalStage?.let {
            it.widthProperty().onChange {
                println(it)
            }
        }

        modalStage?.heightProperty()?.addListener { observableValue, old, nnew ->

            for (x in 0..(nnew.toInt() - old.toInt())) {

                fire(SeamRequestEvent(Type.H))

            }

        }

    }

    override fun onCreate() {


    }

    override fun onDelete() {
        println("Delete popup")
        fire(PopupEvent(false))
    }

}