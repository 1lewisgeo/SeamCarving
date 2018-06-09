import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Point2D
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser
import tornadofx.*
import java.net.URL
import javax.imageio.ImageIO
import kotlin.reflect.jvm.reflect

/**
 * Utility method to choose an image and fire the event to trigger a change
 */
fun chooseImage() {

    chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")), mode = FileChooserMode.Single).takeIf { it.isNotEmpty() }?.let {
        with (find(MainController::class)) {
            image = Image(it.first().inputStream()).writable()
        }
        FX.eventbus.fire(ImageSetEvent(it.first().toPath().toUri().toURL()))
    }

}

/**
 * Utility method to save the current image
 */
fun saveImage() {

    chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")), mode = FileChooserMode.Save).firstOrNull()?.let { file ->

        find(MainController::class).imageProp.get()?.let { image ->

            SwingFXUtils.fromFXImage(image, null).let {

                ImageIO.write(it, file.extension, file)

            }

        }

    }

}

/**
 * Turn a regular Image into a WritableImage
 * @return The same image, but writable
 */
fun Image.writable(): WritableImage {

    val w = WritableImage(width.toInt(), height.toInt())

    for (x in 0 until width.toInt()) {

        for (y in 0 until height.toInt()) {

            w.pixelWriter.setColor(x, y, pixelReader.getColor(x, y))

        }

    }

    return w

}

/**
 * Square a number
 */
fun Int.square(): Int = this * this

/**
 * Remove a list of pixels from a WritableImage, reducing the width by widthr
 * @param pixels the list of pixels to remove
 * @param widthr the reduction in width
 * @return the new writableimage
 */
fun WritableImage.removeV(pixels: Array<Point2D>, widthr: Int): WritableImage {

    val wv = WritableImage(width.toInt() - widthr, height.toInt())

    //println("Removing ${Arrays.toString(pixels)}")

    for (y in 0 until wv.height.toInt()) {

        for (x in 0 until wv.width.toInt()) {

            val count = pixels.filter { it.yi == y && x >= it.xi }.count()

            // println("Removing pixels ${pixels.filter { it.second == y && it.first >= x }}, translating (${x + count}, $y) onto ($x, $y)")

            wv.pixelWriter.setColor(x, y, this.pixelReader.getColor(x + count, y))

        }

    }

    return wv

}

/**
 * Remove a list of pixels from a WritableImage, reducing the height by heightr
 * @param pixels the list of pixels to remove
 * @param heightr the reduction in height
 * @return the new writableimage
 */
fun WritableImage.removeH(pixels: Array<Point2D>, heightr: Int = 0): WritableImage {

    val wh = WritableImage(width.toInt(), height.toInt() - heightr)

    for (x in 0 until wh.width.toInt()) {

        for (y in 0 until wh.height.toInt()) {

            val count = pixels.filter { y >= it.yi && x == it.xi }.count()

            wh.pixelWriter.setColor(x, y, pixelReader.getColor(x, y + count))

        }

    }

    return wh

}

/**
 * Extension method to benchmark an arbitrary function, unfunctional and unused
 */
fun <T> Function<T>.benchmark(vararg args: Any): Pair<Long, T?> {

    val time = System.currentTimeMillis()

    val res = this.reflect()?.call(*args)

    return (System.currentTimeMillis() - time) to res

}

/**
 * Creates a copy of a WritableImage
 * @return a copy of the image
 */
fun WritableImage.copy(): WritableImage {

    val w = WritableImage(width.toInt(), height.toInt())

    for (x in 0 until width.toInt()) {
        for (y in 0 until height.toInt()) {
            w.pixelWriter.setColor(x, y, pixelReader.getColor(x, y))
        }
    }

    return w

}


// Utility extension methods

val Point2D.xi: Int
    get() = this.x.toInt()

val Point2D.yi: Int
    get() = this.y.toInt()

fun kc(combination: String): KeyCombination = KeyCombination.keyCombination(combination)

// true if the url is a local file
val URL.localFile: Boolean
    get() = this.toString().startsWith("file:/")

// Events
// object -> event carries no additional information
// class -> event has parameters and can be used to transfer data

enum class Type { V, H }

// Sent when a seam needs to be removed
class SeamRequestEvent(val t: Type): FXEvent()

// Sent upon termination of the program
object StopEvent: FXEvent()

// Sent when a new image is set
class ImageSetEvent(val location: URL): FXEvent()

// Sent when the image is modified in any way
object ImageModifiedEvent: FXEvent()

// Sent when the popup is opened or closed
class PopupEvent(val open: Boolean): FXEvent()