package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambdaInstance
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Purple40
import com.example.myapplication.ui.theme.Purple80
import com.example.myapplication.ui.theme.PurpleGrey40
import com.example.myapplication.ui.theme.PurpleGrey80
import java.nio.Buffer
import java.nio.ByteBuffer
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
private const val CIDLen = 3
private const val DriStartByteOffset = 4
private const val ScanTimerInterval = 2
private val DRI_CID = byteArrayOf(0xFA.toByte(), 0x0B.toByte(), 0xBC.toByte())
private const val VendorTypeLen = 1
private const val VendorTypeValue = 0x0D
data class BasicInfo(val id: Int, val type: String, val idType: String)
data class LocationInfo(
    val latitude: Double=0.0,
    val longitude: Double=0.0,
    val altitude: Double=0.0,
    val altitudeGeo:Double=0.0,
    val height:Double=0.0,
    val heightOver:Double=0.0,
    val timestamp: Long=System.currentTimeMillis())
data class PilotInfo(val pilotId: Int, val pilotName: String, val experienceYears: Int)
data class ConnectionInfo(
    val rssi: String="",
    val mac:String="",
    val startedTime : String="",
    val lastSeenTime: String="",
    val wifiDistance : Int=0)

data class DroneIDRT(val basicInfo: BasicInfo,val locationInfo: LocationInfo,val pilotInfo: PilotInfo,val connectionInfo: ConnectionInfo)





class MainActivity : ComponentActivity() {
    private lateinit var wifiManager: WifiManager

    var basicInfoList = mutableStateListOf(
        BasicInfo(1, "Alice", "30"),

        )

    var connectionInfoList = mutableStateListOf(
        ConnectionInfo("2")
    )

    var locationInfoList = mutableStateListOf(
        LocationInfo(34.0522, -118.2437, 100.0),

        )

    var pilotInfoList = mutableStateListOf(
        PilotInfo(1, "Dan", 5),
        PilotInfo(2, "Eve", 3),
        PilotInfo(3, "Frank", 7)
    )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    UavInformationTable()
                    Button(onClick = {

                    }) { }
                }
            }
        }

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiScanReceiver = object : BroadcastReceiver() {
//
//            override fun onReceive(context: Context, intent: Intent) {
//                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                if (success) {
//                    scanSuccess()
//                } else {
//                    scanFailure()
//                }
//            }
//        }
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        this.registerReceiver(wifiScanReceiver, intentFilter)

        requestPermissions()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            // 已经获取权限，开始扫描
            scanWifi()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun scanWifi() {
        // 开始扫描
        wifiManager.startScan()

        // 获取扫描结果
        val wifiList = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        } else {
            wifiManager.scanResults

        }
        for (scanResult in wifiList) {
            parseBeacon(scanResult)
            // 处理扫描结果，例如打印SSID和信号强度
            scanResult.informationElements.iterator()
        }

    }


    @Preview(showBackground = true)
    @Composable
    fun UavInformationTable() {
        val modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(4.dp))
            .background(color = PurpleGrey80)
            .padding(16.dp)
            .fillMaxWidth()


        MyApplicationTheme {
            Column {
                UavConnectionSec(modifier)
                UavIDSec(modifier)
                UavLocationSec(modifier)
                UavOperatorSec(modifier)
                Button(onClick = { scanWifi() }) { Text(text = "刷新") }
            }
        }
    }


    @Preview
    @Composable
    fun BasicInfor() {


        Column(modifier = Modifier.padding(16.dp)) {
            Text("基础信息", style = MaterialTheme.typography.titleLarge)
            basicInfoList.forEach { info ->
                Text("ID: ${info.id}, 类型: ${info.type}, ID类型: ${info.idType}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("位置信息", style = MaterialTheme.typography.titleLarge)
            locationInfoList.forEach { location ->
                Text("Latitude: ${location.latitude}, Longitude: ${location.longitude}, Altitude: ${location.altitude}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("飞手信息", style = MaterialTheme.typography.titleLarge)
            pilotInfoList.forEach { pilot ->
                Text("Pilot ID: ${pilot.pilotId}, Pilot Name: ${pilot.pilotName}, Experience: ${pilot.experienceYears} years")
            }
        }
    }


    @Composable
    fun UavLocationSec(modifier: Modifier) {
        Card(modifier) {
            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text("位置信息", style = MaterialTheme.typography.titleLarge)
                locationInfoList.forEach { info ->
                    Row {
                        Text("latitude: ${info.latitude}", modifier = Modifier.padding(2.dp))
                        Text(
                            "longitude: ${info.longitude}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                    Row {
                        Text("高度: ${info.height}", modifier = Modifier.padding(2.dp))
                        Text(
                            "高度（起飞点）: ${info.heightOver}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                    Row {
                        Text("altitudeGeo: ${info.altitudeGeo}", modifier = Modifier.padding(2.dp))
                        Text(
                            "timestamp: ${info.timestamp}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun UavOperatorSec(modifier: Modifier) {
        Card(modifier)
        {
            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text("飞手信息", style = MaterialTheme.typography.titleLarge)
                basicInfoList.forEach { info ->
                    Row {
                        Text("ID: ${info.id}", modifier = Modifier.padding(2.dp))
                        Text(
                            "ID类型: ${info.idType}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                    Text(
                        "类型:  ${info.type}", modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth(), textAlign = TextAlign.Left
                    )
                }
            }
        }
    }

    @Composable
    fun UavIDSec(modifier: Modifier) {
        Card(modifier) {
            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text("ID", style = MaterialTheme.typography.titleLarge)
                basicInfoList.forEach { info ->
                    Text("ID: ${info.id}, 类型: ${info.type}, ID类型: ${info.idType}")
                }
            }
        }
    }

    @Composable
    fun UavConnectionSec(modifier: Modifier) {
        //val siInfo = remember { connectionInfoList }
        Card(modifier = modifier) {
            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text("基础信息", style = MaterialTheme.typography.titleLarge)
                connectionInfoList.forEach { info ->
                    Row {
                        Text("rssi: ${info.rssi}", modifier = Modifier.padding(2.dp))
                        Text(
                            "mac: ${info.mac}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                    Row {
                        Text("高度: ${info.startedTime}", modifier = Modifier.padding(2.dp))
                        Text(
                            "高度（起飞点）: ${info.lastSeenTime}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                    }
                    Row {
                        Text("altitudeGeo: ${info.wifiDistance}", modifier = Modifier.padding(2.dp))

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun parseBeacon(scanResult: ScanResult) {
        // 根据特定协议解析Beacon信息
        // 示例：检查特定的UUID、Major、Minor等
        val sWifiValue = scanResult.informationElements
        Log.d("ddss", "tt")
        if (sWifiValue == null)
            return
        for (element in sWifiValue) {
            if (element == null) {
                continue
            }
            val valueId = element.id
            if (valueId == null) {
                continue
            }
            if (valueId == 221) {

                val valueBytes = element.bytes
                val testSSID = scanResult.SSID ?: continue
                if (testSSID.isEmpty())
                {
                    continue
                }
                if (testSSID[0] !='U'){
                    continue
                }
                //Log.d("ddss1","tt1")
                //Log.d("ddss1","SSID: ${scanResult.getWifiSsid()}, RSSI: ${scanResult.level}")

                //pilotInfoList.set(1, PilotInfo(2,valueBytes.toString(),5))
                //Log.d("ddddd1",scanResult.SSID)
                Log.d("DRI_CID[0].toInt()", DRI_CID[0].toInt().toString())

                processRemoteId(scanResult, buf = valueBytes)

            }
        }
    }


    fun processRemoteId(scanResult: ScanResult, buf: ByteBuffer) {
        Log.d("ddddd2", scanResult.SSID)
        if (buf.remaining() < 30) return
        Log.d("dfsfw", buf.toString())
        val driCID = ByteArray(3)


        buf.get(driCID, 0, 3)
        val vendorType = ByteArray(1)
        Log.d("ddddd", scanResult.SSID)

        buf.get(vendorType)
        if ((driCID[0].toInt() and 0xFF) == DRI_CID[0].toUByte().toInt() &&
            (driCID[1].toInt() and 0xFF) == DRI_CID[1].toUByte().toInt() &&
            (driCID[2].toInt() and 0xFF) == DRI_CID[2].toUByte().toInt() &&
            vendorType[0] == VendorTypeValue.toByte()
        ) {
            val ar1 = ByteArray(buf.remaining())
          var arr = ByteArray(buf.remaining()-1)

            buf.position(DriStartByteOffset) // 设置位置以读取剩余数据
            buf.get(ar1,0, buf.remaining())  // 读取剩余数据
            arr = ar1.copyOfRange(1,ar1.size-1)
            val parser = WifiBeaconParser()

            try {
                parser.parse(arr)

                // 获取并处理解析后的消息
                parser.getMessages().forEach { message ->
                    when (message) {
                        is ParsedMessage.BasicMessage -> {
                            println("Parsed Basic Message: ID Type: ${message.idType}, UAS ID: ${message.uasId}")
                        }

                        is ParsedMessage.PositionVectorMessage -> locationInfoList[0] = locationInfoList[0].copy(latitude = message.droneLat, height = message.height)
                        is ParsedMessage.ReservedMessage -> println("Parsed Reserved Message with content:")
                        is ParsedMessage.RunningDescriptionMessage -> println("Parsed Running Description Message with content: ")
                        is ParsedMessage.SystemMessage -> println("Parsed System Message with content: ")
                        is ParsedMessage.RunningIdMessage -> println("Parsed Running ID Message with content: ")
                    }
                }
            } catch (e: IllegalArgumentException) {
                // 处理解析错误
                e.printStackTrace()
            }

            Log.d("daadd2s", scanResult.BSSID)
            connectionInfoList.add(1, ConnectionInfo(rssi = "test"))
            connectionInfoList[0] = connectionInfoList[0].copy(rssi = scanResult.SSID)

//        connectionInfoList.set(0,
//            ConnectionInfo(scanResult.getWifiSsid().toString(), scanResult.BSSID, wifiDistance = scanResult.level ))
        }

    }
}









@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MyApplicationTheme {
//        Column {
//            Row {
//                Greeting("Android1")
//                Greeting("Android2")
//            }
//            Column {
//                Greeting("Android3")
//                Greeting("Android4")
//            }
//        }
//        //Greeting("Android")
//
//    }
//}


