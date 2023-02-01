import java.io.File
import kotlin.math.exp
import kotlin.random.Random

interface InputSource {
    val value: Double
}


class Synapse(val input: InputSource, val weight: Double) {
    fun asGraphvizLinkTo(neuron: Neuron): String {
        return "\"${System.identityHashCode(input)}\" -> \"${
            System.identityHashCode(
                neuron
            )
        }\" [label=\"${String.format("%.2f", weight)}\"]"
    }
}


class Neuron(
    private val synapses: List<Synapse>,
    private val bias: Double,
) : InputSource {
    private var _value: Double = 0.0
    private var nextValue: Double? = null
    private val activationFunction: (Double) -> Double = { 1 / (1 + exp(-it)) }

    override val value: Double
        get() = _value

    fun compute() {
        val sum = synapses.sumOf { it.input.value * it.weight } + bias
        nextValue = activationFunction(sum)
    }

    fun update() {
        _value = nextValue ?: throw IllegalStateException("Neuron not computed")
        nextValue = null
    }

    fun asGraphvizNode(color: String? = null): String {
        val id = System.identityHashCode(this)
        return "\"${id}\" [label=\"${
            listOf(
//                id,
                bias.let { String.format("%.2f", it) },
                _value.let { String.format("%.2f", it) },
            ).joinToString("\\n")
        }\", color=${color ?: "blue"}]"
    }

    fun asGraphvizLinks(): List<String> {
        return synapses.map { it.asGraphvizLinkTo(this) }
    }

    companion object {
        fun buildRandom(inputNodes: List<InputSource>): Neuron {
            return Neuron(
                inputNodes.map { Synapse(it, Random.nextDouble()) },
                Random.nextDouble(),
            )
        }
    }
}

class NetworkInput(initialValue: Double) : InputSource {
    private var _value: Double = initialValue
    override val value: Double
        get() = _value

    fun set(value: Double) {
        _value = value
    }

    fun asGraphvizNode(): String {
        val id = System.identityHashCode(this)
        return "\"${id}\" [label=\"${
            listOf(
//                id,
                value.let { String.format("%.2f", it) },
            ).joinToString("\\n")
        }\", color=green]"
    }
}

class NeuralNetwork(
    val inputNodes: List<NetworkInput>,
    private val hiddenNeurons: List<Neuron>,
) {
    fun compute() {
        (hiddenNeurons).onEach { it.compute() }.forEach { it.update() }
    }

    private fun asGraphviz(): String {
        val rows = mutableListOf("digraph {rankdir=LR")
        inputNodes.forEach { rows.add(it.asGraphvizNode()) }
        hiddenNeurons.forEach { neuron ->
            rows.add(neuron.asGraphvizNode())
            rows.addAll(neuron.asGraphvizLinks())
        }
        rows.add("}")
        return rows.joinToString("\n")
    }

    fun printGraphPNG(fileName: String, removeDotFile: Boolean) {
        val dotFile = File("$fileName.dot")
        dotFile.writeText(asGraphviz())

        // for some unknown reasons, the png file is not generated , except if we try a lot of times (8 most of the time)
        val pngFile = File("$fileName.png")
        pngFile.delete()
        do {
            Runtime.getRuntime().exec("dot -Tpng $fileName.dot -o $fileName.png")
        } while (!pngFile.exists())

        if (removeDotFile) {
            dotFile.deleteOnExit()
        }
    }


    companion object {
        fun buildFeedForward(
            inputSize: Int,
            inputLayerSize: Int,
            outputLayerSize: Int,
        ): NeuralNetwork {
            val inputNodes = (0 until inputSize).map { NetworkInput(0.0) }
            val inputLayer =
                (0 until inputLayerSize).map { Neuron.buildRandom(inputNodes) }
            val outputLayer =
                (0 until outputLayerSize).map { Neuron.buildRandom(inputLayer) }
            return NeuralNetwork(inputNodes, inputLayer + outputLayer)
        }
    }
}