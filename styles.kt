import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Styles(): Stylesheet() {

    companion object {

        val controls by cssid() // VBox

        val info by cssid() // BorderPane

        // Colors

        val background = c("#282a36")

        val text = c("#f8f8f2")

        val hoverc = c("#44475a")

    }

    init {

        root {

            fontFamily = "Ubuntu"

            backgroundColor += background

            textFill = text

        }

        s(label) {
            textFill = text
        }

        s(button) {

            and(hover) {

                backgroundColor += hoverc

            }

        }

        menuButton {

            and(focused, selected, hover) {

                backgroundColor += hoverc

            }

        }

        menuItem {

            backgroundColor += background

            and(focused, selected, hover) {

                backgroundColor += hoverc

            }

        }

        button {

            borderWidth += box(1.px)

            borderRadius += box(0.px)

            borderColor += box(text)

            backgroundColor += Color.TRANSPARENT

            textFill = text

        }

        menuBar {

            backgroundColor += hoverc

            padding = box(0.px)

        }

        separator {

            backgroundColor += background

        }

        contextMenu {

            padding = box(0.px)

        }

        controls {

            alignment = Pos.CENTER

            spacing = 20.px

            padding = box(0.px, 0.px, 10.px, 0.px)

            //backgroundColor += Color.RED

            minWidth = 300.px

            maxWidth = 300.px

        }

        info {

            child("#url") {

                borderWidth += box(1.px, 1.px, 1.px, 1.px)

                borderColor += box(text)

                padding = box(3.px, 3.px, 3.px, 3.px)

            }

            child("#infobox") {

                spacing = 10.px

                padding = box(10.px, 0.px, 0.px, 0.px)

                child(label) {

                    padding = box(3.px, 3.px, 3.px, 3.px)

                    borderWidth += box(1.px, 1.px, 1.px, 1.px)

                    borderColor += box(text)

                }

            }

        }

        //println("Stylesheet loaded, ${LocalDateTime.from(Instant.now()).format(DateTimeFormatter.ISO_DATE_TIME)}")

    }

}