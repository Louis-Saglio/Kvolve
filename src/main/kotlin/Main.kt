import java.awt.Color
import kotlin.concurrent.thread
import kotlin.random.Random

fun main() {
    NeuralNetwork.buildFeedForward(3, 5, 2).apply {
        inputNodes.forEach { it.set(Random.nextDouble()) }
        compute()
        printGraphPNG("test.png", false)
    }
    val gui = GUI()
    thread(start = true) {
        repeat(1000) {
            Thread.sleep(500)
            gui.refresh((0..100).map { it }) {
                GridItem((0..50).random(), (0..50).random(), Color.BLUE)
            }
        }
    }
}