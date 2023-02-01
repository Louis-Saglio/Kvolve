import java.awt.*

data class GridItem(val x: Int, val y: Int, val color: Color)

class GUI {
    private val frame = Frame("Kvolve")
    private val canvas = CustomCanvas()

    init {
        frame.apply {
            Dimension(1000, 500).also {
                size = it
                canvas.size = it
            }
            add(canvas)
            isVisible = true
        }
    }

    fun <T> refresh(data: List<T>, adapt: (T) -> GridItem) {
        canvas.items = data.map { adapt(it) }
        canvas.repaint()
    }
}

private class CustomCanvas : Canvas() {
    var items = listOf<GridItem>()
    private val scale: Int = 3

    override fun paint(g: Graphics) {
        super.paint(g)
        items.forEach { item ->
            g.drawRect(item.x * scale, item.y * scale, scale, scale)
        }
    }
}