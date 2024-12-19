package com.example.myapplication

data class BeaconMessage(
    val messageType: Int,
    val protocolVersion: Int,
    val data: ByteArray
)

sealed class ParsedMessage {
    data class BasicMessage(
        val idType: Int,
        val uasId:String
    ) : ParsedMessage()
    data class PositionVectorMessage(
        val status:Int,
        val heightType:Int,
        val EWDirection:Int,
        val speedMult:Int,
        val Direction:Int,
        var speedHori: Int = 0,
        var speedVert: Int = 0,
        var droneLat: Int = 0,
        var droneLon: Int = 0,
        var altitudePressure: Int = 0,
        var altitudeGeodetic: Int = 0,
        var height: Int = 0,
        var horizontalAccuracy: Int = 0,
        var verticalAccuracy: Int = 0,
        var baroAccuracy: Int = 0,
        var speedAccuracy: Int = 0,
        var timestamp: Int = 0,
        var timeAccuracy: Int = 0,
        var distance: Float = 0f
    ) : ParsedMessage()
    data class ReservedMessage(val content: ByteArray) : ParsedMessage()
    data class RunningDescriptionMessage(val content: ByteArray) : ParsedMessage()
    data class SystemMessage(val content: ByteArray) : ParsedMessage()
    data class RunningIdMessage(val content: ByteArray) : ParsedMessage()
}

class WifiBeaconParser {

    companion object {
        private const val HEADER_SIZE = 3 // 版本号、报文长度、报文数量
        private const val MESSAGE_SIZE = 25 // 每条报文的大小
    }

    private val messages = mutableListOf<ParsedMessage>()

    fun parse(beaconData: ByteArray) {
        // 检查数据长度是否足够
        if (beaconData.size < HEADER_SIZE) {
            throw IllegalArgumentException("Insufficient data length.")
        }

        val versionAndType = beaconData[0]
        val protocolVersion = versionAndType.toInt() and 0x0F // 取 3-0 位
        val messageLength = beaconData[1].toInt() // 报文长度
        val messageCount = beaconData[2].toInt() // 报文数量

        if (messageLength != MESSAGE_SIZE) {
            throw IllegalArgumentException("Invalid message length.")
        }

        // 从第四个字节开始读取报文内容
        for (i in 0 until messageCount) {
            val startIndex = HEADER_SIZE + i * MESSAGE_SIZE
            if (startIndex + MESSAGE_SIZE > beaconData.size) {
                throw IllegalArgumentException("Insufficient data for message $i.")
            }

            val data = beaconData.copyOfRange(startIndex, startIndex + MESSAGE_SIZE)
            val messageType = versionAndType.toInt() shr 4 // 取 7-4 位

            // 根据报文类型解析内容
            val parsedMessage = when (data[0].toInt() and 0x0F) { // 取 3-0 位
                0x1 -> parseBasicMessage(data) // 基本报文
                0x2 -> ParsedMessage.PositionVectorMessage(data) // 位置向量报文
                0x3 -> ParsedMessage.ReservedMessage(data) // 预留报文
                0x4 -> ParsedMessage.RunningDescriptionMessage(data) // 运行描述报文
                0x5 -> ParsedMessage.SystemMessage(data) // 系统报文
                0x6 -> ParsedMessage.RunningIdMessage(data) // 运行人ID报文
                else -> throw IllegalArgumentException("Unknown message type.")
            }


            messages.add(parsedMessage)

        }
    }

    private fun parseBasicMessage(data: ByteArray):ParsedMessage.BasicMessage{
        val idType=(data[0].toInt() shr 4) and 0x0F
        val uasId = data.decodeToString(1,21)

        return ParsedMessage.BasicMessage(idType,uasId)
    }
    private fun parsePositionVectorMessage(data: ByteArray):ParsedMessage.BasicMessage{
        val idType=(data[0].toInt() shr 4) and 0x0F
        val uasId = data.decodeToString(1,21)

        return ParsedMessage.BasicMessage(idType,uasId)
    }
    fun getMessages(): MutableList<ParsedMessage> {
        return messages
    }
}

// 示例用法
fun main() {
    val parser = WifiBeaconParser()
    val exampleBeaconData = ByteArray(100) { it.toByte() } // 示例数据
    // 假设你已经填充了 exampleBeaconData 数据
    parser.parse(exampleBeaconData)


}