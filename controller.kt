import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.control.Alert
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import tornadofx.Controller
import tornadofx.alert
import tornadofx.onChange
import tornadofx.point
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

/**
 * Program business logic
 */
class MainController(): Controller() {

    // UI bindings

    val imageProp = SimpleObjectProperty<WritableImage>()

    val locationProp = SimpleObjectProperty<URL>()

    val energyView = SimpleBooleanProperty(false)

    val showSeams = SimpleBooleanProperty(false)

    var popImage: WritableImage? = null

    var location: URL
        get() = locationProp.get()
        set(value) = locationProp.set(value)

    var image: WritableImage
        get() = imageProp.get()
        set(value) = imageProp.set(value)

    // Used internally

    internal val width: Int
        get() = imageProp.get().width.toInt()

    internal val height: Int
        get() = imageProp.get().height.toInt()

    internal val cache = emptyMap<Pair<Int, Int>, Int>().toMutableMap()

    internal val lock = ReentrantLock()

    internal val executor = Executors.newWorkStealingPool()

    // Utility methods

    /**
     * Gets the rgb information of a pixel
     * @return rgb data array
     */
    internal fun pixel(x: Int, y: Int): Array<Int> = imageProp.get().pixelReader.getColor(x, y).run { arrayOf((red*255).toInt(), (green*255).toInt(), (blue*255).toInt()) }

    /**
     * Checks that coordinates are within bounds
     * @return true if within bounds
     */
    internal fun check(x: Int = 0, y: Int = 0): Boolean = !(x < 0 || y < 0 || x >= width-1 || y >= height-1)

    /**
     * Gets the red value of the given pixel
     * @return the red value
     */
    internal fun r(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[0] else 0

    /**
     * Gets the green value of the given pixel
     * @return the green value
     */
    internal fun g(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[1] else 0

    /**
     * Gets the blue value of the given pixel
     * @return the blue value
     */
    internal fun b(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[2] else 0

    /**
     * Calculate the energy of a pixel
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the energy of the pixel
     */
    fun energy(x: Int, y: Int): Int {

        //mutableMapOf<Pair<Int, Int>, Int>(0 to 0 to 0)

//        if (x to y in cache) {
//            println(cache)
//            return cache[x to y]!!
//        } else {

        val xe = (r(x + 1, y) - r(x - 1, y)).square() + (g(x + 1, y) - g(x - 1, y)).square() + (b(x + 1, y) - b(x - 1, y)).square()

        val ye = (r(x, y + 1) - r(x, y - 1)).square() + (g(x, y + 1) - g(x, y - 1)).square() + (b(x, y + 1) - b(x, y - 1)).square()

        //Oprintln("($x, $y): ${xe+ye} | ${(r(x + 1, y) - r(x - 1, y)).square()} + ${(g(x + 1, y) - g(x - 1, y)).square()} + ${(b(x + 1, y) - b(x - 1, y)).square()}")

        val result = xe + ye

        cache.put(x to y, result)

        return result

//        }
    }

    /**
     * Calculates and returns all the horizontal seams
     * @return a list of seams and their energy value
     */
    internal fun allVSeams(): Array<Pair<Array<Point2D>, Int>> {

        fun hseam(x: Int): Pair<Array<Int>, Int> {

            var energy = 0

            val arr = mutableListOf(x)

            for (y in 1 until height) {

                val x = arr.last()

                val list = mutableListOf<Pair<Int, Int>>()

                if (x > 0) {
                    list.add(energy(x - 1, y) to -1)
                }

                if (x < width - 1) {
                    list.add(energy(x + 1, y) to 1)
                }

                list.add(energy(x, y) to 0)

                val min = list.minBy { it.first }

                //println(min)

                arr.add(x + min!!.second)

                energy += min.first

            }

            return arr.toTypedArray() to energy

        }


        // Threaded execution of seam finding
        return executor.invokeAll<Pair<Array<Int>, Int>>((0 until width).map { Callable<Pair<Array<Int>, Int>> { hseam(it) } }.toMutableList()).map { it.get() }.map { it.first.mapIndexed { index: Int, i: Int -> point(i, index) }.toTypedArray() to it.second }.toTypedArray()

    }

    /**
     * Get the lowest energy vertical seam
     * @return the lowest energy vertical seam
     */
    public fun verticalSeam(): Array<Point2D> {

        return allVSeams().minBy { it.second }!!.first

//        for (y in 0 until width) {
//
//            //println("$width : $y")
//
//            options[y] = hseam(y)
//
//        }
//
//        //println(Arrays.deepToString(options.map { "(${Arrays.toString(it.first)}, ${it.second}" }.toTypedArray()))
//
//        return options.minBy { it.second }!!.first.mapIndexed { index: Int, i: Int -> point(i, index) }.toTypedArray()

    }

    /**
     * Calculates all horizontal seams
     * @return all horizontal seams and their energy value
     */
    internal fun allHSeams(): Array<Pair<Array<Point2D>, Int>> {

        fun vseam(x: Int): Pair<Array<Int>, Int> {

            //println(Thread.currentThread().name)

            var energy = 0

            val arr = mutableListOf(x)

            for (x in 1 until width) {

                val y = arr.last()

                val list = mutableListOf<Pair<Int, Int>>()

                if (y > 0) {
                    list.add(energy(x, y - 1) to -1)
                }

                if (y < height - 1) {
                    list.add(energy(x, y + 1) to 1)
                }

                list.add(energy(x, y) to 0)

                val min = list.minBy { it.first }

                //println(min)

                arr.add(y + min!!.second)

                energy += min.first

            }

            return arr.toTypedArray() to energy

        }

        // Threaded execution
        return executor.invokeAll<Pair<Array<Int>, Int>>((0 until height).map { Callable<Pair<Array<Int>, Int>> { vseam(it) } }.toMutableList()).map { it.get() }.map { it.first.mapIndexed { index: Int, i: Int -> point(index, i) }.toTypedArray() to it.second }.toTypedArray()

    }

    /**
     * Find the lowest energy horizontal seam
     * @return the lowest energy horizontal seam
     */
    public fun horizontalSeam(): Array<Point2D> {

        return allHSeams().minBy { it.second }!!.first

        //return min

        /*(for (y in 0 until height) {

            options[y] = vseam(y)

        }*/

        //println(Arrays.deepToString(options.map { "(${Arrays.toString(it.first)}, ${it.second}" }.toTypedArray()))

        //return options.minBy { it.second }!!.first.mapIndexed { index: Int, i: Int -> point(index, i) }.toTypedArray()

    }

    fun contentAmplify(h: Int, v: Int) {

        // stub

    }

    /**
     * EXPERIMENTAL
     * Inserts a vertical seam
     * @param n the number of seams to insert
     */
    fun verticalInsert(n: Int) {

        val seams = allVSeams().also { it.sortBy { it.second } }.filterIndexed { index, pair -> index < n }.map { it.first }

        fun doThing(seam: Array<Point2D>, image: WritableImage): WritableImage {

            val w = WritableImage(width + 1, height)

            for (y in 0 until height) {

                for (x in 0 until width+1) {

                    val count = seam.filter { x >= it.xi && y == it.yi }.count()

                    w.pixelWriter.setColor(x, y, image.pixelReader.getColor(x - count, y))

                }

            }

            return w

        }

        var w = image

        for (seam in seams) {
            w = doThing(seam, w)
        }

        image = w

    }

    /**
     * BROKEN
     * Get a grayscale energy heatmap of the image
     */
    fun energyImage(): WritableImage {

        val w = WritableImage(width, height)

        // get a list of all the indexes
        val indexes = (0 until width).map { out -> (0 until height).map { it to out } }.flatMap { it.asIterable() }

        val max = indexes.map { energy(it.first, it.second) }.max()!!

        // Calculate corresponding color values
        indexes.forEach { w.pixelWriter.setColor(it.first, it.second, Color.gray(energy(it.first, it.second) / max.toDouble())) }

        return w

    }

    init {

        // Event bindings for ui interaction

        imageProp.onChange { cache.clear() ; fire(ImageModifiedEvent) }

        subscribe<SeamRequestEvent> {

            if (imageProp.get() == null) {
                alert(Alert.AlertType.WARNING, "Warning", "You must select an image before removing seams.").run { return@subscribe }
            }

            if (height == 0 || width == 0) {
                alert(Alert.AlertType.ERROR, "Error", "Image width/height cannot be 0!").run { return@subscribe }
            }

            if (showSeams.get()) {

                // remove seams with preview

                runAsync {
                    lock.lock()
                    val seam = if (it.t == Type.V) verticalSeam() else horizontalSeam()

                    for (point in seam) {
                        // Draw seam
                        if (it.t == Type.V) image.pixelWriter.setColor(point.xi, point.yi, Color.RED) else image.pixelWriter.setColor(point.xi, point.yi, Color.RED)
                    }

                    // Wait
                    Thread.sleep(500)

                    // Do actual seam removal
                    image = if (it.t == Type.V) image.removeV(seam, 1) else image.removeH(seam, 1)

                    lock.unlock()

                }

            } else {

                // Remove seams wihtout preview

                lock.lock()

                // Remove seam
                image = if (it.t == Type.V) image.removeV(verticalSeam(), 1) else image.removeH(horizontalSeam(), 1)

                lock.unlock()

            }

        }

        // Shutdown executor properly
        subscribe<StopEvent> { executor.shutdown() }

        subscribe<ImageSetEvent> { location = it.location }

        subscribe<PopupEvent> { event ->
            if (event.open) {
                popImage = image.copy()
            } else {
                popImage?.let {
                    image = it
                }
            }
        }

    }

}