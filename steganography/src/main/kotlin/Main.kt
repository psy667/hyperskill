package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor
import kotlin.system.exitProcess

fun xorMessage(message: ByteArray, password: ByteArray): ByteArray {
    return message.mapIndexed{ index, byte -> byte.xor(password[index.mod(password.size)]) }.toByteArray()
}

fun writeMessageToImage(image: BufferedImage, message: ByteArray): BufferedImage {
    val messageIterator = message.iterator()
    var currentByte = messageIterator.next().toInt()
    var bitIndex = 0

    loop@ for(y in 0 until image.height) {
        for(x in 0 until image.width) {
            val color = image.getRGB(x, y)

            if (bitIndex == 8) {
                if(messageIterator.hasNext()) {
                    bitIndex = 0
                    currentByte = messageIterator.next().toInt()
                } else {
                    break@loop
                }
            }

            val currentBit = currentByte shl bitIndex and 0x80 shr 7
            val newColor = color and 0xFFFFFE or currentBit

            image.setRGB(x, y, newColor)
            bitIndex++
        }
    }

    return image
}


fun getMessageFromImage(inputImage: BufferedImage): ByteArray {
    val bytes = mutableListOf<Byte>()
    var currentByte = 0x0
    var bitIndex = 0
    val nullString = listOf((0x0).toByte(), (0x0).toByte(), (0x3).toByte())
    loop@ for (y in 0 until inputImage.height) {
        for(x in 0 until inputImage.width) {
            val color = inputImage.getRGB(x, y)
            val currentBit = (color and 0x1)

            currentByte = currentByte or currentBit
            currentByte = currentByte shl 1
            bitIndex++

            if (bitIndex == 8) {
                bitIndex = 0
                bytes.add((currentByte shr 1).toByte())
                currentByte = 0x0

                if (bytes.takeLast(3) == nullString) {
                    bytes.removeLast()
                    bytes.removeLast()
                    bytes.removeLast()
                    break@loop
                }
            }
        }
    }
    return bytes.toByteArray()
}

fun hide() {
    println("Input image file:")
    val inputFilePath = readLine()!!

    println("Output image file:")
    val outputFilePath = readLine()!!

    println("Message to hide:")
    val message = readLine()!!.toByteArray()

    println("Password:")
    val password = readLine()!!.toByteArray()

    val encryptedMessage = xorMessage(message, password)
    val messageToHide = encryptedMessage.plus(0x0).plus(0x0).plus(0x3) // null-terminated string

    val inputFile = File(inputFilePath)
    val outputFile = File(outputFilePath)

    if(!inputFile.exists()) {
        println("Can't read input file!")
        return
    }

    val inputImage = ImageIO.read(inputFile)

    val imageCapacity = inputImage.width * inputImage.height

    if(messageToHide.size * 8 > imageCapacity) {
        println("The input image is not large enough to hold this message.")
        return
    }

    var newImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
    newImage.data = inputImage.data

    newImage = writeMessageToImage(newImage, messageToHide)

    ImageIO.write(newImage, "png", outputFile)

    println("Message saved in $outputFilePath image.")
}



fun show() {
    println("Input image file:")
    val inputFilePath = readLine()!!

    println("Password:")
    val password = readLine()!!.toByteArray()

    val inputFile = File(inputFilePath)
    val inputImage = ImageIO.read(inputFile)

    val bytes = getMessageFromImage(inputImage)

    val str = xorMessage(bytes, password).toString(Charsets.UTF_8)
    println("Message:")
    println(str)
}

fun exit() {
    println("Bye!")
    exitProcess(1)
}

fun handleTask(input: String) {
    when (input) {
        "hide" -> hide()
        "show" -> show()
        "exit" -> exit()
        else -> println("Wrong task: $input")
    }
}

fun main() {
    while(true) {
        println("Task (hide, show, exit):")
        val input = readLine()!!

        handleTask(input)
    }
}
