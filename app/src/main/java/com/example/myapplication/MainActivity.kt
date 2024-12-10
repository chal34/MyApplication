package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.nio.Buffer


var basicInfoList = mutableListOf(
    BasicInfo(1, "Alice", 30),
    BasicInfo(2, "Bob", 25),
    BasicInfo(3, "Charlie", 35)
)

var locationInfoList = mutableListOf(
    LocationInfo(34.0522, -118.2437, 100.0),
    LocationInfo(40.7128, -74.0060, 200.0),
    LocationInfo(37.7749, -122.4194, 150.0)
)

var pilotInfoList = mutableListOf(
    PilotInfo(1, "Dan", 5),
    PilotInfo(2, "Eve", 3),
    PilotInfo(3, "Frank", 7)
)


class MainActivity : ComponentActivity() {
    private lateinit var wifiManager: WifiManager





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
                    BasicInfor()
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
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        } else {
            // 已经获取权限，开始扫描
            scanWifi()
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun scanWifi() {
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
            println("SSID: ${scanResult.getWifiSsid()}, RSSI: ${scanResult.level}")
            scanResult.informationElements.iterator()
            Log.d("dd","SSID: ${scanResult.getWifiSsid()}, RSSI: ${scanResult.level}")
        }

    }
}


@RequiresApi(Build.VERSION_CODES.R)
fun parseBeacon(scanResult: ScanResult) {
    // 根据特定协议解析Beacon信息
    // 示例：检查特定的UUID、Major、Minor等
    val sWifiValue= scanResult.informationElements
    Log.d("ddss","tt")
    if (sWifiValue == null)
        return
    for (element in sWifiValue )
    {
        if (element == null){continue}
        val valueId = element.id
        if (valueId==null){
            continue
        }
        if (valueId==221){

            val valueBytes = element.bytes
            if (valueBytes==null){
                continue
            }
            //Log.d("ddss1","tt1")
            //Log.d("ddss1","SSID: ${scanResult.getWifiSsid()}, RSSI: ${scanResult.level}")
            pilotInfoList.set(0,PilotInfo(1, "${scanResult.getWifiSsid()}", 5))
            pilotInfoList.set(1, PilotInfo(2,valueBytes.toString(),5))

            processRemoteId(scanResult, buf = valueBytes)
        }
    }
}


fun processRemoteId(scanResult: ScanResult,buf: Buffer)
{
    if (buf.remaining() < 30) return
    val dri_CID = ByteArray(3)
    val arr = ByteArray(buf.remaining())


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


@Preview(showBackground = true)
@Composable
fun UavInformationTable() {
    MyApplicationTheme {
        Column {
            BasicInfor()
        }
    }
}
data class BasicInfo(val id: Int, val name: String, val age: Int)
data class LocationInfo(val latitude: Double, val longitude: Double, val altitude: Double)
data class PilotInfo(val pilotId: Int, val pilotName: String, val experienceYears: Int)
@Composable
fun BasicInfor(){


    Column(modifier = Modifier.padding(16.dp)) {
        Text("基础信息", style = MaterialTheme.typography.titleLarge)
        basicInfoList.forEach { info ->
            Text("ID: ${info.id}, Name: ${info.name}, Age: ${info.age}")
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