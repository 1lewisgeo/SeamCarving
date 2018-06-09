import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import tornadofx.Controller
import tornadofx.alert
import tornadofx.onChange
import tornadofx.point
import java.net.URL
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock

class MainController(): Controller() {

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

    internal val width: Int
        get() = imageProp.get().width.toInt()

    internal val height: Int
        get() = imageProp.get().height.toInt()

    internal val cache = emptyMap<Pair<Int, Int>, Int>().toMutableMap()

    internal val lock = ReentrantLock()

    internal val executor = Executors.newWorkStealingPool()

    internal fun pixel(x: Int, y: Int): Array<Int> = imageProp.get().pixelReader.getColor(x, y).run { arrayOf((red*255).toInt(), (green*255).toInt(), (blue*255).toInt()) }

    internal fun check(x: Int = 0, y: Int = 0) = !(x < 0 || y < 0 || x >= width-1 || y >= height-1)

    internal fun r(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[0] else 0
    internal fun g(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[1] else 0
    internal fun b(x: Int, y: Int): Int = if (check(x, y)) pixel(x, y)[2] else 0

    public fun energy(x: Int, y: Int): Int {mutableMapOf<Pair<Int, Int>, Int>(0 to 0 to 0)

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

        return executor.invokeAll<Pair<Array<Int>, Int>>((0 until width).map { Callable<Pair<Array<Int>, Int>> { hseam(it) } }.toMutableList()).map { it.get() }.map { it.first.mapIndexed { index: Int, i: Int -> point(i, index) }.toTypedArray() to it.second }.toTypedArray()

    }

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

        return executor.invokeAll<Pair<Array<Int>, Int>>((0 until height).map { Callable<Pair<Array<Int>, Int>> { vseam(it) } }.toMutableList()).map { it.get() }.map { it.first.mapIndexed { index: Int, i: Int -> point(index, i) }.toTypedArray() to it.second }.toTypedArray()

    }

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

    fun energyImage(): WritableImage {

        val w = WritableImage(width, height)

        val indexes = (0 until width).map { out -> (0 until height).map { it to out } }.flatMap { it.asIterable() }

        val max = indexes.map { energy(it.first, it.second) }.max()!!

        indexes.forEach { w.pixelWriter.setColor(it.first, it.second, Color.gray(energy(it.first, it.second) / max.toDouble())) }

        return w

    }

    init {

        imageProp.onChange { cache.clear() ; fire(ImageModifiedEvent) }

        subscribe<SeamRequestEvent> {

            if (imageProp.get() == null) {
                alert(Alert.AlertType.WARNING, "Warning", "You must select an image before removing seams.").run { return@subscribe }
            }

            if (showSeams.get()) {

                runAsync {
                    val t = it.t
                    lock.lock()
                    val a = if (it.t == Type.V) verticalSeam() else horizontalSeam()
                    /*this.completedProperty.onChange {
                        //runAsync {
                            Thread.sleep(500)

                            image = if (t == Type.V) image.removeV(a, 1) else image.removeH(a, 1)

                            lock.unlock()
                        //}
                    }*/
                    for (point in a) {
                        //println("setting $x ${a[x]} ; ${controller.width} x ${controller.height}")
                        if (it.t == Type.V) image.pixelWriter.setColor(point.xi, point.yi, Color.RED) else image.pixelWriter.setColor(point.xi, point.yi, Color.RED)
                        //controller.imageProp.set(controller.imageProp.get().remove(it.mapIndexed { index: Int, i: Int -> i to index }.toTypedArray(), 1))
                    }

                    Thread.sleep(500)

                    image = if (t == Type.V) image.removeV(a, 1) else image.removeH(a, 1)

                    lock.unlock()

                }

            } else {

                lock.lock()

                image = if (it.t == Type.V) image.removeV(verticalSeam(), 1) else image.removeH(horizontalSeam(), 1)

                lock.unlock()

            }

        }

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