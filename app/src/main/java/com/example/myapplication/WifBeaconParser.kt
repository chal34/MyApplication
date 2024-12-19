package com.example.myapplication

import java.nio.ByteBuffer
import kotlin.experimental.and

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
    data class SystemMessage(
        var operatorLocationType: Int = 0,
        var classificationType: Int = 0,
        var operatorLatitude: Int = 0,
        var operatorLongitude: Int = 0,
        var areaCount: Int = 0,
        var areaRadius: Int = 0,
        var areaCeiling: Int = 0,
        var areaFloor: Int = 0,
        var category: Int = 0,
        var classValue: Int = 0,
        var operatorAltitudeGeo: Int = 0,
        var systemTimestamp: Long = 0
    ) : ParsedMessage()
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
        val messageLength = beaconData[2].toInt() // 报文长度
        val messageCount = beaconData[3].toInt() // 报文数量

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
                0x2 -> parsePositionVectorMessage(data) // 位置向量报文
                0x3 -> ParsedMessage.ReservedMessage(data) // 预留报文
                0x4 -> ParsedMessage.RunningDescriptionMessage(data) // 运行描述报文
                0x5 -> parseSystemMessage(data) // 系统报文
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
    private fun parsePositionVectorMessage(data: ByteArray):ParsedMessage.PositionVectorMessage{
        val buffer1=ByteBuffer.wrap(data)
        val b: Int = buffer1.get().toInt()
        val status = (b and 0xF0) shr 4
        val heightType = (b and 0x04) shr 2
        val EWDirection = (b and 0x02) shr 1
        val speedMult = b and 0x01

        val Direction = buffer1.get().toInt() and 0xFF
        val speedHori = buffer1.get().toInt() and 0xFF
        val speedVert = buffer1.get().toInt()

        val droneLat = buffer1.getInt()
        val droneLon = buffer1.getInt()

        val altitudePressure = buffer1.getShort().toInt() and 0xFFFF
        val altitudeGeodetic = buffer1.getShort().toInt() and 0xFFFF
        val height = buffer1.getShort().toInt() and 0xFFFF

        val horiVertAccuracy: Int = buffer1.get().toInt()
        val horizontalAccuracy = horiVertAccuracy and 0x0F
        val verticalAccuracy = (horiVertAccuracy and 0xF0) shr 4
        val speedBaroAccuracy: Int = buffer1.get().toInt()
        val baroAccuracy = (speedBaroAccuracy and 0xF0) shr 4
        val speedAccuracy = speedBaroAccuracy and 0x0F
        val timestamp = buffer1.getShort().toInt() and 0xFFFF
        val timeAccuracy = buffer1.get().toInt() and 0x0F
        return ParsedMessage.PositionVectorMessage(
            status,
            heightType,
            EWDirection,
            speedMult,speedHori,
            speedVert,
            droneLat,droneLon,altitudePressure,altitudeGeodetic,height,horiVertAccuracy,horizontalAccuracy,verticalAccuracy,baroAccuracy,speedAccuracy,timestamp,timeAccuracy
            )
    }

    private fun parseSystemMessage(data: ByteArray):ParsedMessage.SystemMessage {
        val s:ParsedMessage.SystemMessage=ParsedMessage.SystemMessage()
        val dd =ByteBuffer.wrap(data)
        var b=dd.get().toInt()
        s.operatorLocationType = b and 0x03
        s.classificationType = (b and 0x1C) shr 2
        s.operatorLatitude = dd.getInt()
        s.operatorLongitude = dd.getInt()
        s.areaCount = dd.getShort().toInt() and 0xFFFF
        s.areaRadius = dd.get().toInt() and 0xFF
        s.areaCeiling = dd.getShort().toInt() and 0xFFFF
        s.areaFloor = dd.getShort().toInt() and 0xFFFF
        b = dd.get().toInt()
        s.category = (b and 0xF0) shr 4
        s.classValue = b and 0x0F
        s.operatorAltitudeGeo = dd.getShort().toInt() and 0xFFFF
        s.systemTimestamp = dd.getInt().toLong() and 0xFFFFFFFFL
    return s
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