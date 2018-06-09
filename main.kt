import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCombination
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import tornadofx.*
import java.net.URL

class KApp(): App(MainView::class, Styles::class) {

    init { FX.stylesheets.add("http://fonts.googleapis.com/css?family=Ubuntu") }

    override fun stop() { fire(StopEvent) }

}

class MainView(): View() {

    private val controller: MainController by inject()

    private val controls: Controls by inject()

    private val menu: Menu by inject()

    private val imageView = ImageView()

    override val root = BorderPane()

    init {

        with (root) {

            top = menu.root

            center = imageView.run {
                subscribe<PopupEvent> { pe ->
                    if (pe.open) {
                        imageProperty().unbind()
                    } else {
                        imageProperty().bind(controller.imageProp)
                    }
                }
                this
            }

            imageView.imageProperty().bind(controller.imageProp)

            controller.energyView.onChange {
                if (it) {
                    controller.image = controller.energyImage()
                } else {
                    imageView.image = controller.image
                }
            }

            bottom = controls.root

        }

    }

}

fun main(args: Array<String>) {
    Application.launch(KApp::class.java, *args)
}