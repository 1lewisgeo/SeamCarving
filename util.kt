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

fun chooseImage() {

    chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")), mode = FileChooserMode.Single).takeIf { it.isNotEmpty() }?.let {
        with (find(MainController::class)) {
            image = Image(it.first().inputStream()).writable()
        }
        FX.eventbus.fire(ImageSetEvent(it.first().toPath().toUri().toURL()))
    }

}

fun saveImage() {

    chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")), mode = FileChooserMode.Save).firstOrNull()?.let { file ->

        find(MainController::class).imageProp.get()?.let { image ->

            SwingFXUtils.fromFXImage(image, null).let {

                ImageIO.write(it, file.extension, file)

            }

        }

    }

}

fun Image.writable(): WritableImage {

    val w = WritableImage(width.toInt(), height.toInt())

    for (x in 0 until width.toInt()) {

        for (y in 0 until height.toInt()) {

            w.pixelWriter.setColor(x, y, pixelReader.getColor(x, y))

        }

    }

    return w

}

fun Int.square(): Int = this * this

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

fun <T> Function<T>.benchmark(vararg args: Any): Pair<Long, T?> {

    val time = System.currentTimeMillis()

    val res = this.reflect()?.call(*args)

    return (System.currentTimeMillis() - time) to res

}

fun WritableImage.copy(): WritableImage {

    val w = WritableImage(width.toInt(), height.toInt())

    for (x in 0 until width.toInt()) {
        for (y in 0 until height.toInt()) {
            w.pixelWriter.setColor(x, y, pixelReader.getColor(x, y))
        }
    }

    return w

}

val Point2D.xi: Int
    get() = this.x.toInt()

val Point2D.yi: Int
    get() = this.y.toInt()

fun kc(combination: String): KeyCombination = KeyCombination.keyCombination(combination)

val URL.localFile: Boolean
    get() = this.toString().startsWith("file:/")

enum class Type { V, H }

class SeamRequestEvent(val t: Type): FXEvent()

object StopEvent: FXEvent()

class ImageSetEvent(val location: URL): FXEvent()

object ImageModifiedEvent: FXEvent()

class PopupEvent(val open: Boolean): FXEvent()