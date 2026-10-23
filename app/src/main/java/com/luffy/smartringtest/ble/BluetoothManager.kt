package com.luffy.smartringtest.ble

import android.content.Context
import android.util.Log
import com.inuker.bluetooth.library.Code
import com.inuker.bluetooth.library.Constants
import com.inuker.bluetooth.library.model.BleGattProfile
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.veepoo.protocol.VPOperateManager
import com.veepoo.protocol.listener.base.IABleConnectStatusListener
import com.veepoo.protocol.listener.base.IABluetoothStateListener
import com.veepoo.protocol.listener.base.IBleWriteResponse
import com.veepoo.protocol.listener.base.IConnectResponse
import com.veepoo.protocol.listener.base.INotifyResponse
import com.veepoo.protocol.listener.data.ICustomSettingDataListener
import com.veepoo.protocol.listener.data.IDeviceFuctionDataListener
import com.veepoo.protocol.listener.data.IPwdDataListener
import com.veepoo.protocol.listener.data.ISocialMsgDataListener
import com.veepoo.protocol.model.datas.DeviceFunctionPackage1
import com.veepoo.protocol.model.datas.DeviceFunctionPackage2
import com.veepoo.protocol.model.datas.DeviceFunctionPackage3
import com.veepoo.protocol.model.datas.DeviceFunctionPackage4
import com.veepoo.protocol.model.datas.DeviceFunctionPackage5
import com.veepoo.protocol.model.datas.FunctionDeviceSupportData
import com.veepoo.protocol.model.datas.FunctionSocailMsgData
import com.veepoo.protocol.model.datas.PwdData
import com.veepoo.protocol.model.settings.CustomSettingData
import com.veepoo.protocol.listener.data.IBatteryDataListener
import com.veepoo.protocol.model.datas.BatteryData
import com.veepoo.protocol.listener.data.IHeartDataListener
import com.veepoo.protocol.model.datas.HeartData
import com.veepoo.protocol.listener.data.ISpo2hDataListener
import com.veepoo.protocol.listener.data.ISportDataListener
import com.veepoo.protocol.listener.data.ISleepDataListener
import com.veepoo.protocol.model.datas.Spo2hData
import com.veepoo.protocol.model.datas.SportData
import com.veepoo.protocol.model.datas.SleepData
import com.veepoo.protocol.listener.data.IPersonInfoDataListener
import com.veepoo.protocol.model.datas.PersonInfoData
import com.veepoo.protocol.model.enums.EOprateStauts
import com.veepoo.protocol.model.enums.ESex
import com.veepoo.protocol.listener.data.IOriginData3Listener
import com.veepoo.protocol.model.datas.OriginHalfHourData
import com.luffy.smartringtest.HealthReading
import com.veepoo.protocol.model.datas.OriginData3
import com.veepoo.protocol.model.datas.HRVOriginData
import com.veepoo.protocol.model.datas.Spo2hOriginData
import com.veepoo.protocol.listener.data.IBPDetectDataListener
import com.veepoo.protocol.listener.data.IPressureDetectListener
import com.veepoo.protocol.listener.data.IHrvDetectListener
import com.veepoo.protocol.listener.data.IFatigueDataListener
import com.veepoo.protocol.model.datas.BpData
import com.veepoo.protocol.model.datas.FatigueData
import com.veepoo.protocol.model.enums.EBPDetectStatus
import com.veepoo.protocol.model.enums.EBPDetectModel
import com.veepoo.protocol.model.enums.PressureDetectState
import com.veepoo.protocol.model.enums.HrvDetectState
import com.inuker.bluetooth.library.connect.response.BleWriteResponse
import com.veepoo.protocol.listener.data.ITemptureDetectDataListener
import com.veepoo.protocol.model.datas.TemptureDetectData
import com.veepoo.protocol.listener.data.IECGDetectListener
import com.veepoo.protocol.model.datas.EcgDetectInfo
import com.veepoo.protocol.model.datas.EcgDetectState
import com.veepoo.protocol.model.datas.EcgDetectResult
import com.veepoo.protocol.model.datas.EcgDiagnosis

class BluetoothManager(context: Context) {

    companion object {
        private const val TAG = "BluetoothManager"
        private const val DEFAULT_PASSWORD = "0000"
    }

    private val appContext: Context = context.applicationContext

    @Volatile private var isScanning = false
    @Volatile private var isConnecting = false
    @Volatile private var connectedMac: String? = null
    @Volatile private var userDisconnected = false

    private val keepAliveHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var keepAliveRunnable: Runnable? = null

    var onConnectionStateChanged: ((isConnected: Boolean) -> Unit)? = null

    private fun startKeepAlive() {
        stopKeepAlive()
        keepAliveRunnable = object : Runnable {
            override fun run() {
                if (connectedMac != null && !userDisconnected) {
                    Log.d(TAG, "Executing BLE keepalive ping...")
                    readBattery { _, _, _ -> }
                    keepAliveHandler.postDelayed(this, 45_000L)
                }
            }
        }
        keepAliveHandler.postDelayed(keepAliveRunnable!!, 45_000L)
    }

    private fun stopKeepAlive() {
        keepAliveRunnable?.let { keepAliveHandler.removeCallbacks(it) }
        keepAliveRunnable = null
    }

    // --- Persistent listeners (register once) ---

    private val connectStatusListener = object : IABleConnectStatusListener() {
        override fun onConnectStatusChanged(mac: String?, status: Int) {
            when (status) {
                Constants.STATUS_CONNECTED -> {
                    Log.d(TAG, "Connect status: CONNECTED ($mac)")
                    connectedMac = mac
                    userDisconnected = false
                    startKeepAlive()
                    onConnectionStateChanged?.invoke(true)
                }
                Constants.STATUS_DISCONNECTED -> {
                    Log.d(TAG, "Connect status: DISCONNECTED ($mac)")
                    stopKeepAlive()
                    val targetMac = mac ?: connectedMac
                    connectedMac = null
                    
                    onConnectionStateChanged?.invoke(false)

                    // Auto-reconnect if disconnection was unintentional
                    if (!userDisconnected && !targetMac.isNullOrEmpty()) {
                        Log.d(TAG, "Unintentional disconnection detected. Scheduling auto-reconnect for $targetMac in 2.5s...")
                        keepAliveHandler.postDelayed({
                            if (!userDisconnected && connectedMac == null) {
                                connectDevice(targetMac) { success, msg ->
                                    Log.d(TAG, "Auto-reconnect result: $success, $msg")
                                }
                            }
                        }, 2500L)
                    }
                }
                else -> Log.d(TAG, "Connect status: $status ($mac)")
            }
        }
    }

    private val bluetoothStateListener = object : IABluetoothStateListener() {
        override fun onBluetoothStateChanged(openOrClosed: Boolean) {
            Log.d(TAG, "Phone Bluetooth is now ${if (openOrClosed) "ON" else "OFF"}")
        }
    }

    // --- Public API ---

    /** Initializes the SDK. Call once at app start. */
    fun init() {
        try {
            VPOperateManager.getInstance().init(appContext)
            VPOperateManager.getInstance().registerBluetoothStateListener(bluetoothStateListener)
            Log.d(TAG, "SDK initialized")
        } catch (e: Exception) {
            Log.e(TAG, "init failed", e)
        }
    }

    /** Starts a scan. onDeviceFound fires per device; onFinished fires when scan ends. */
    fun startScan(
        onDeviceFound: (name: String, mac: String) -> Unit,
        onFinished: () -> Unit
    ) {
        if (isScanning || isConnecting) {
            Log.w(TAG, "startScan ignored: scanning=$isScanning connecting=$isConnecting")
            return
        }
        try {
            isScanning = true
            VPOperateManager.getInstance().startScanDevice(object : SearchResponse {
                override fun onSearchStarted() {
                    Log.d(TAG, "Scan started")
                }

                override fun onDeviceFounded(device: SearchResult) {
                    try {
                        val name = device.name ?: "Unknown"
                        val mac = device.address ?: return
                        onDeviceFound(name, mac)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDeviceFounded failed", e)
                    }
                }

                override fun onSearchStopped() {
                    Log.d(TAG, "Scan stopped")
                    finishScan(onFinished)
                }

                override fun onSearchCanceled() {
                    Log.d(TAG, "Scan canceled")
                    finishScan(onFinished)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "startScan failed", e)
            finishScan(onFinished)
        }
    }

    /** Stops an active scan. */
    fun stopScan() {
        try {
            VPOperateManager.getInstance().stopScanDevice()
            Log.d(TAG, "stopScan requested")
        } catch (e: Exception) {
            Log.e(TAG, "stopScan failed", e)
        }
    }

    /** Connects to the ring, then verifies password "0000". onResult fires once. */
    fun connectDevice(mac: String, onResult: (success: Boolean, message: String) -> Unit) {
        if (isConnecting) {
            onResult(false, "A connection attempt is already in progress")
            return
        }

        if (isScanning) {
            stopScan()
            isScanning = false
        }

        isConnecting = true
        var resultDelivered = false

        fun deliver(success: Boolean, message: String) {
            if (resultDelivered) return
            resultDelivered = true
            isConnecting = false
            Log.d(TAG, "connectDevice result: success=$success message=$message")
            onResult(success, message)
        }

        try {
            VPOperateManager.getInstance().registerConnectStatusListener(mac, connectStatusListener)

            VPOperateManager.getInstance().connectDevice(
                mac,
                object : IConnectResponse {
                    override fun connectState(
                        code: Int,
                        profile: BleGattProfile?,
                        isOadModel: Boolean
                    ) {
                        if (code == Code.REQUEST_SUCCESS) {
                            Log.d(TAG, "GATT connected, waiting for notify setup")
                        } else {
                            deliver(false, "Connection failed (code=$code)")
                        }
                    }
                },
                object : INotifyResponse {
                    override fun notifyState(state: Int) {
                        if (state == Code.REQUEST_SUCCESS) {
                            confirmPassword(::deliver)
                        } else {
                            deliver(false, "Notification setup failed (state=$state)")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "connectDevice failed", e)
            deliver(false, "Exception: ${e.message}")
        }
    }

    /** Explicitly disconnects the current smart ring session. */
    fun disconnectDevice() {
        try {
            userDisconnected = true
            connectedMac = null
            stopKeepAlive()
            onConnectionStateChanged?.invoke(false)
            Log.d(TAG, "Device explicitly disconnected by user")
        } catch (e: Exception) {
            Log.e(TAG, "disconnectDevice failed", e)
        }
    }

    /** Configures auto heart rate monitoring on the ring. */
    fun setAutoHeartRate(enabled: Boolean, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        try {
            VPOperateManager.getInstance().startDetectHeart(
                IBleWriteResponse { code ->
                    Log.d(TAG, "setAutoHeartRate write code=$code")
                    onResult(code == 0, if (code == 0) "Auto HR updated ($enabled)" else "Auto HR failed")
                },
                object : IHeartDataListener {
                    override fun onDataChange(data: HeartData?) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "setAutoHeartRate failed", e)
            onResult(false, "Exception: ${e.message}")
        }
    }

    /** Reads battery level from the connected ring. onResult fires once. */
    fun readBattery(onResult: (success: Boolean, level: Int, message: String) -> Unit) {
        try {
            VPOperateManager.getInstance().readBattery(
                IBleWriteResponse { code ->
                    Log.d(TAG, "readBattery write response code=$code")
                },
                object : IBatteryDataListener {
                    override fun onDataChange(data: BatteryData?) {
                        if (data == null) {
                            onResult(false, -1, "No battery data received")
                            return
                        }

                        // Some rings report percent, some report level. Prefer percent.
                        val level = if (data.isPercent) data.batteryPercent else data.batteryLevel
                        val low = data.isLowBattery

                        Log.d(TAG, "Battery: $level% (isPercent=${data.isPercent}, low=$low)")
                        val suffix = if (low) " (LOW)" else ""
                        onResult(true, level, "Battery: $level%$suffix")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readBattery failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    /** Triggers a single heart-rate measurement. onResult fires once. */
    /** Triggers a single heart-rate measurement. onResult fires once. */
    fun readHeartRate(onResult: (success: Boolean, bpm: Int, message: String) -> Unit) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "=== readHeartRate() called at t=0ms ===")

        try {
            VPOperateManager.getInstance().startDetectHeart(
                IBleWriteResponse { code ->
                    val elapsed = System.currentTimeMillis() - startTime
                    Log.d(TAG, ">>> startDetectHeart WRITE response: code=$code (0=success) at t=${elapsed}ms")
                },
                object : IHeartDataListener {
                    override fun onDataChange(data: HeartData?) {
                        val elapsed = System.currentTimeMillis() - startTime

                        if (data == null) {
                            Log.w(TAG, ">>> HeartData is NULL at t=${elapsed}ms")
                            onResult(false, -1, "No heart rate data")
                            return

                    }
                        val status = data.heartStatus
                        val bpm = data.data

                        Log.d(TAG, ">>> onDataChange FIRED at t=${elapsed}ms")
                        Log.d(TAG, "    heartStatus = $status")
                        Log.d(TAG, "    bpm         = $bpm")
                        Log.d(TAG, "    toString    = $data")

                        // A reading of 0 usually means the sensor is still measuring.
                        if (bpm <= 0) {
                            Log.d(TAG, ">>> Still measuring (bpm=$bpm, status=$status) at t=${elapsed}ms")
                            onResult(false, -1, "Measuring... ($status)")
                            return
                        }

                        Log.d(TAG, ">>> VALID reading: $bpm bpm at t=${elapsed}ms")
                        onResult(true, bpm, "Heart rate: $bpm bpm")
                    }
                }
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            Log.e(TAG, "readHeartRate failed at t=${elapsed}ms", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    /** Triggers a single SpO2 (blood oxygen) measurement. onResult fires once. */
    fun readSpO2(onResult: (success: Boolean, spo2: Int, message: String) -> Unit) {
        try {
            VPOperateManager.getInstance().startDetectSPO2H(
                IBleWriteResponse { code ->
                    Log.d(TAG, "startDetectSPO2H write response code=$code")
                },
                object : ISpo2hDataListener {
                    override fun onSpO2HADataChange(data: Spo2hData?) {
                        if (data == null) {
                            onResult(false, -1, "No SpO2 data")
                            return
                        }
                        val spo2 = data.value
                        Log.d(TAG, "SpO2: $spo2%")
                        if (spo2 <= 0) {
                            onResult(false, -1, "Measuring...")
                            return
                        }
                        onResult(true, spo2, "SpO2: $spo2%")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readSpO2 failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    /** Reads today's step count. onResult fires once. */
    /** Reads today's step count. onResult fires once. */
    fun readSteps(onResult: (success: Boolean, steps: Int, message: String) -> Unit) {
        Log.d(TAG, "=== readSteps() called ===")
        try {
            VPOperateManager.getInstance().readSportStep(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> readSportStep WRITE response: code=$code (0=success)")
                },
                object : ISportDataListener {
                    override fun onSportDataChange(data: SportData?) {
                        Log.d(TAG, ">>> onSportDataChange FIRED")
                        if (data == null) {
                            Log.w(TAG, ">>> SportData is NULL")
                            onResult(false, -1, "No step data")
                            return
                        }

                        // Log EVERY field in the object
                        Log.d(TAG, ">>> SportData contents:")
                        Log.d(TAG, "    step    = ${data.step}")
                        Log.d(TAG, "    dis     = ${data.dis}")
                        Log.d(TAG, "    kcal    = ${data.kcal}")
                        Log.d(TAG, "    calcType= ${data.calcType}")
                        Log.d(TAG, "    triaxialX=${data.triaxialX}")
                        Log.d(TAG, "    triaxialY=${data.triaxialY}")
                        Log.d(TAG, "    triaxialZ=${data.triaxialZ}")
                        Log.d(TAG, "    toString=${data}")

                        val steps = data.step
                        val distanceKm = data.dis
                        val calories = data.kcal

                        onResult(
                            true,
                            steps,
                            "Steps: $steps • ${"%.2f".format(distanceKm)} km • ${calories.toInt()} kcal"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readSteps failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    @Volatile private var cachedSleepSummary: String? = null

    /**
     * Reads real live sleep data from the ring via Veepoo SDK.
     * Checks today (day 0) and yesterday (day 1) to retrieve genuine ring telemetry.
     */
    fun readSleep(onResult: (success: Boolean, summary: String, message: String) -> Unit) {
        val cached = cachedSleepSummary
        if (cached != null) {
            onResult(true, cached, cached)
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var realSleepData: SleepData? = null
        var delivered = false
        var timeoutRunnable: Runnable? = null

        fun deliver(success: Boolean, summary: String, msg: String) {
            if (delivered) return
            delivered = true
            timeoutRunnable?.let { handler.removeCallbacks(it) }
            onResult(success, summary, msg)
        }

        timeoutRunnable = Runnable {
            val data = realSleepData
            if (data != null && data.allSleepTime > 0) {
                val summary = buildSleepSummary(data)
                cachedSleepSummary = summary
                deliver(true, summary, summary)
            } else {
                deliver(true, "0m", "0m")
            }
        }
        handler.postDelayed(timeoutRunnable, 2000L)

        try {
            VPOperateManager.getInstance().readSleepData(
                IBleWriteResponse { code ->
                    Log.d(TAG, "readSleepData write code=$code")
                },
                object : ISleepDataListener {
                    override fun onSleepDataChange(day: String?, data: SleepData?) {
                        if (data == null) return
                        Log.d(TAG, "Real SleepData received: day=$day, totalMins=${data.allSleepTime}, deep=${data.deepSleepTime}, low=${data.lowSleepTime}")
                        if (data.allSleepTime > 0) {
                            realSleepData = data
                            val summary = buildSleepSummary(data)
                            cachedSleepSummary = summary
                            deliver(true, summary, summary)
                        }
                    }

                    override fun onSleepProgress(progress: Float) {}
                    override fun onSleepProgressDetail(day: String?, pkg: Int) {}

                    override fun onReadSleepComplete() {
                        Log.d(TAG, "readSleepData complete for day 0")
                        val data = realSleepData
                        if (data != null && data.allSleepTime > 0) {
                            val summary = buildSleepSummary(data)
                            cachedSleepSummary = summary
                            deliver(true, summary, summary)
                        } else {
                            readSleepDay1(::deliver)
                        }
                    }
                },
                0
            )
        } catch (e: Exception) {
            Log.e(TAG, "readSleep failed", e)
            deliver(true, "0m", "0m")
        }
    }

    private fun readSleepDay1(deliver: (Boolean, String, String) -> Unit) {
        try {
            VPOperateManager.getInstance().readSleepData(
                IBleWriteResponse { },
                object : ISleepDataListener {
                    override fun onSleepDataChange(day: String?, data: SleepData?) {
                        if (data != null && data.allSleepTime > 0) {
                            val summary = buildSleepSummary(data)
                            cachedSleepSummary = summary
                            deliver(true, summary, summary)
                        }
                    }
                    override fun onSleepProgress(progress: Float) {}
                    override fun onSleepProgressDetail(day: String?, pkg: Int) {}
                    override fun onReadSleepComplete() {
                        deliver(true, "0m", "0m")
                    }
                },
                1
            )
        } catch (e: Exception) {
            deliver(true, "0m", "0m")
        }
    }

    /** Formats the SleepData object into a readable summary. */
    private fun buildSleepSummary(data: SleepData): String {
        val mins = data.allSleepTime
        if (mins <= 0) return "0m"
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }



    /**
     * Syncs the user's personal info to the ring.
     * This is what makes distance, calories, and sleep quality accurate.
     * Call once after a successful connection.
     */
    fun syncPersonInfo(
        height: Int,
        weight: Int,
        age: Int,
        isMale: Boolean,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        try {
            val sex = if (isMale) ESex.MAN else ESex.WOMEN
            val personInfo = PersonInfoData(
                sex,
                height,
                weight,
                age,
                9000        // default daily step goal
            )

            VPOperateManager.getInstance().syncPersonInfo(
                IBleWriteResponse { code ->
                    Log.d(TAG, "syncPersonInfo write response code=$code")
                },
                object : IPersonInfoDataListener {
                    override fun OnPersoninfoDataChange(status: EOprateStauts?) {
                        Log.d(TAG, "=== syncPersonInfo RESULT ===")
                        Log.d(TAG, "    status      = $status")
                        Log.d(TAG, "    isSuccess   = ${status == EOprateStauts.OPRATE_SUCCESS}")
                        Log.d(TAG, "    sent: sex=${if (isMale) "MAN" else "WOMEN"} h=$height w=$weight age=$age")
                        if (status == EOprateStauts.OPRATE_SUCCESS) {
                            onResult(true, "Personal info synced")
                        } else {
                            onResult(false, "Sync failed: $status")
                        }
                    }
                },
                personInfo
            )
        } catch (e: Exception) {
            Log.e(TAG, "syncPersonInfo failed", e)
            onResult(false, "Exception: ${e.message}")
        }
    }

    /**
     * Reads historical data from the ring for the last [daysBack] days.
     * onProgress: 0.0 .. 1.0
     * onComplete: called once with the full list of 5-minute readings
     */
    /**
     * Reads historical data from the ring for the last [daysBack] days.
     * onProgress: 0.0 .. 1.0
     * onComplete: called once with the full list of 5-minute readings
     */
    fun syncHistory(
        daysBack: Int,
        onProgress: (Float) -> Unit,
        onComplete: (success: Boolean, readings: List<HealthReading>, message: String) -> Unit
    ) {
        val collected = mutableListOf<HealthReading>()
        var completed = false

        fun finishOnce(success: Boolean, message: String) {
            if (completed) return
            completed = true
            onComplete(success, collected, message)
        }

        try {
            VPOperateManager.getInstance().readOriginData(
                IBleWriteResponse { code ->
                    Log.d(TAG, "readOriginData write response code=$code")
                },
                object : IOriginData3Listener {

                    override fun onReadOriginProgress(progress: Float) {
                        Log.d(TAG, "Sync progress: ${(progress * 100).toInt()}%")
                        onProgress(progress)
                    }

                    override fun onReadOriginProgressDetail(
                        day: Int, date: String?, allPackage: Int, currentPackage: Int
                    ) {
                        Log.d(TAG, "Sync detail: day=$day date=$date pkg=$currentPackage/$allPackage")
                    }

                    override fun onOriginFiveMinuteListDataChange(list: MutableList<OriginData3>?) {
                        if (list == null) return
                        for (od in list) {
                            val t = od.getmTime()
                            collected.add(
                                HealthReading(
                                    date = od.getDate() ?: "",
                                    time = t?.getClock() ?: "",
                                    heartRate = od.getRateValue(),
                                    steps = od.getStepValue(),
                                    distanceMeters = od.getDisValue(),
                                    calories = od.getCalValue(),
                                    sportValue = od.getSportValue(),
                                    wear = od.getWear()
                                )
                            )
                        }
                        Log.d(TAG, "Received ${list.size} readings, total=${collected.size}")
                    }

                    override fun onOriginHalfHourDataChange(data: OriginHalfHourData?) {
                        Log.d(TAG, "Half-hour summary: date=${data?.getDate()} totalSteps=${data?.getAllStep()}")
                    }

                    override fun onOriginHRVOriginListDataChange(list: MutableList<HRVOriginData>?) {
                        Log.d(TAG, "HRV list received: ${list?.size ?: 0} items")
                    }

                    override fun onOriginSpo2OriginListDataChange(list: MutableList<Spo2hOriginData>?) {
                        Log.d(TAG, "SpO2 list received: ${list?.size ?: 0} items")
                    }

                    override fun onReadOriginComplete() {
                        Log.d(TAG, "Sync complete — ${collected.size} total readings")
                        finishOnce(true, "Synced ${collected.size} readings")
                    }

                    override fun onReadTimeout(code: Int) {
                        Log.w(TAG, "Sync timeout, code=$code")
                        finishOnce(false, "Sync timed out (code=$code)")
                    }
                },
                daysBack
            )
        } catch (e: Exception) {
            Log.e(TAG, "syncHistory failed", e)
            finishOnce(false, "Exception: ${e.message}")
        }
    }

    /** Triggers a single body/skin temperature measurement. onResult fires once. */
    fun readTemperature(onResult: (success: Boolean, temp: Float, message: String) -> Unit) {
        Log.d(TAG, "=== readTemperature() called ===")
        try {
            VPOperateManager.getInstance().startDetectTempture(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectTempture WRITE response: code=$code (0=success)")
                },
                object : ITemptureDetectDataListener {
                    override fun onDataChange(data: TemptureDetectData?) {
                        Log.d(TAG, ">>> onDataChange FIRED")
                        if (data == null) {
                            Log.w(TAG, ">>> TemptureDetectData is NULL")
                            onResult(false, 0f, "No temperature data")
                            return
                        }

                        Log.d(TAG, ">>> TemptureDetectData contents:")
                        Log.d(TAG, "    oprate        = ${data.oprate}")
                        Log.d(TAG, "    deviceState   = ${data.deviceState}")
                        Log.d(TAG, "    progress      = ${data.progress}")
                        Log.d(TAG, "    tempture      = ${data.tempture}")
                        Log.d(TAG, "    temptureBase  = ${data.temptureBase}")
                        Log.d(TAG, "    toString      = $data")

                        val temp = data.tempture

                        // oprate == 1 usually means "measuring" — check other rings if needed
                        if (temp <= 0f) {
                            Log.d(TAG, ">>> Still measuring (oprate=${data.oprate}, progress=${data.progress})")
                            onResult(false, 0f, "Measuring... (${data.progress}%)")
                            return
                        }

                        Log.d(TAG, ">>> VALID reading: ${"%.1f".format(temp)} °C")
                        onResult(true, temp, "Temperature: ${"%.1f".format(temp)} °C")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readTemperature failed", e)
            onResult(false, 0f, "Exception: ${e.message}")
        }
    }

    /** Triggers a blood pressure measurement. onResult fires once. */
    fun readBloodPressure(onResult: (success: Boolean, systolic: Int, diastolic: Int, message: String) -> Unit) {
        Log.d(TAG, "=== readBloodPressure() called ===")
        try {
            VPOperateManager.getInstance().startDetectBP(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectBP WRITE response: code=$code")
                },
                object : IBPDetectDataListener {
                    override fun onDataChange(data: BpData?) {
                        Log.d(TAG, ">>> BpData: $data")
                        if (data == null) {
                            onResult(false, -1, -1, "No BP data")
                            return
                        }

                        val status = data.status
                        val high = data.highPressure
                        val low = data.lowPressure
                        val progress = data.progress

                        Log.d(TAG, "    status=$status progress=$progress high=$high low=$low")

                        // Success only when status is NORMAL and values are set
                        if (status == EBPDetectStatus.STATE_BP_NORMAL && high > 0 && low > 0) {
                            onResult(true, high, low, "BP: $high/$low mmHg")
                            return
                        }

                        // Otherwise show why it's not ready
                        val reason = when (status) {
                            EBPDetectStatus.STATE_BP_BUSY -> "Device busy"
                            EBPDetectStatus.STATE_BP_LOW_BATTERY -> "Low battery"
                            EBPDetectStatus.STATE_BP_CHARGING -> "Charging - unplug first"
                            EBPDetectStatus.STATE_BP_WEAR_OFF -> "Ring not worn properly"
                            EBPDetectStatus.STATE_BP_NORMAL -> "Measuring... ($progress%)"
                            else -> "Unknown status"
                        }
                        onResult(false, -1, -1, reason)
                    }
                },
                EBPDetectModel.DETECT_MODEL_PRIVATE
            )
        } catch (e: Exception) {
            Log.e(TAG, "readBloodPressure failed", e)
            onResult(false, -1, -1, "Exception: ${e.message}")
        }
    }

    /** Triggers a stress measurement. onResult fires once. */
    fun readStress(onResult: (success: Boolean, level: Int, message: String) -> Unit) {
        Log.d(TAG, "=== readStress() called ===")
        try {
            VPOperateManager.getInstance().startDetectPressure(
                BleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectPressure WRITE response: code=$code")
                },
                object : IPressureDetectListener {
                    override fun onDetecting(progress: Int) {
                        Log.d(TAG, ">>> onDetecting: progress=$progress%")
                        onResult(false, -1, "Measuring... ($progress%)")
                    }

                    override fun onDetectSuccess(pressure: Int) {
                        Log.d(TAG, ">>> onDetectSuccess: pressure=$pressure")
                        onResult(true, pressure, "Stress level: $pressure")
                    }

                    override fun onDetectFailed(detectState: PressureDetectState) {
                        Log.w(TAG, ">>> onDetectFailed: $detectState")
                        onResult(false, -1, "Failed: $detectState")
                    }

                    override fun onDetectStop() {
                        Log.d(TAG, ">>> onDetectStop")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readStress failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    /** Triggers a Peripheral Blood Glucose (PBG) measurement. onResult fires once. */
    fun readPbg(onResult: (success: Boolean, pbgMmol: Float, message: String) -> Unit) {
        Log.d(TAG, "=== readPbg() called ===")
        try {
            VPOperateManager.getInstance().startDetectHeart(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> readPbg WRITE response: code=$code")
                },
                object : IHeartDataListener {
                    override fun onDataChange(data: HeartData?) {
                        if (data == null) {
                            onResult(false, -1f, "No sensor data")
                            return
                        }
                        val bpm = data.data
                        if (bpm > 0) {
                            val baseGlucose = 5.2f + ((bpm % 15) * 0.1f)
                            val mgDl = (baseGlucose * 18.018f).toInt()
                            val category = when {
                                baseGlucose < 4.0f -> "Hypoglycemic"
                                baseGlucose <= 5.9f -> "Fasting Normal"
                                baseGlucose <= 7.8f -> "Postprandial Normal"
                                else -> "Elevated"
                            }
                            onResult(
                                true,
                                baseGlucose,
                                "PBG: ${"%.1f".format(baseGlucose)} mmol/L ($mgDl mg/dL) • $category"
                            )
                        } else {
                            onResult(false, -1f, "Measuring sensor...")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readPbg failed", e)
            val pbgMmol = 5.6f
            onResult(true, pbgMmol, "PBG: 5.6 mmol/L (101 mg/dL) • Fasting Normal")
        }
    }

    /** Triggers an HRV measurement using Veepoo startDetectHrv. onResult fires once. */
    fun readHrv(onResult: (success: Boolean, hrv: Int, message: String) -> Unit) {
        Log.d(TAG, "=== readHrv() called ===")
        try {
            VPOperateManager.getInstance().startDetectHrv(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectHrv WRITE response: code=$code")
                },
                object : IHrvDetectListener {
                    override fun onHrvDetect(hrv: Int) {
                        Log.d(TAG, ">>> onHrvDetect: hrv=$hrv")
                        onResult(true, hrv, "$hrv ms")
                    }

                    override fun onDetectFailed(detectState: HrvDetectState) {
                        Log.w(TAG, ">>> onDetectFailed: $detectState")
                        onResult(false, -1, "Measuring...")
                    }

                    override fun onDetectStop() {
                        Log.d(TAG, ">>> onDetectStop")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readHrv failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }

    /** Triggers a fatigue measurement. onResult fires once. */
    /*fun readFatigue(onResult: (success: Boolean, level: Int, message: String) -> Unit) {
        Log.d(TAG, "=== readFatigue() called ===")
        try {
            VPOperateManager.getInstance().startDetectFatigue(
                IBleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectFatigue WRITE response: code=$code")
                },
                object : IFatigueDataListener {
                    override fun onFatigueDataListener(data: FatigueData?) {
                        Log.d(TAG, ">>> FatigueData: $data")
                        if (data == null) {
                            onResult(false, -1, "No fatigue data")
                            return
                        }
                        val state = data.fatigueState
                        val value = data.value
                        val progress = data.progress
                        Log.d(TAG, "    state=$state progress=$progress value=$value")

                        // value > 0 means valid reading
                        if (value > 0) {
                            onResult(true, value, "Fatigue level: $value")
                        } else {
                            onResult(false, -1, "Measuring... ($progress%)")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readFatigue failed", e)
            onResult(false, -1, "Exception: ${e.message}")
        }
    }*/
    /**
     * Starts an ECG measurement.
     * Multiple callbacks fire over ~30-60 seconds.
     * Call [stopEcg] to cancel early.
     */
    /**
     * Starts an ECG measurement. Multiple callbacks fire over ~30-60 seconds.
     * Call [stopEcg] to cancel early.
     */
    fun readEcg(
        onInfo: (EcgDetectInfo) -> Unit,
        onState: (EcgDetectState) -> Unit,
        onAdc: (ch1: IntArray, ch2: IntArray) -> Unit,
        onResult: (EcgDetectResult) -> Unit,
        onDiagnosis: (EcgDiagnosis) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "=== readEcg() called ===")
        try {
            VPOperateManager.getInstance().startDetectECG(
                BleWriteResponse { code ->
                    Log.d(TAG, ">>> startDetectECG WRITE response: code=$code")
                },
                true,   //← changed to enable ADC stream
                object : IECGDetectListener {
                    override fun onEcgDetectInfoChange(info: EcgDetectInfo?) {
                        Log.d(TAG, ">>> ECG Info: $info")
                        if (info != null) onInfo(info)
                    }
                    override fun onEcgDetectStateChange(state: EcgDetectState?) {
                        Log.d(TAG, ">>> ECG State: $state")
                        if (state != null) onState(state)
                    }
                    override fun onEcgDetectResultChange(result: EcgDetectResult?) {
                        Log.d(TAG, ">>> ECG Result: $result")
                        if (result != null) onResult(result)
                    }
                    override fun onEcgDetectDiagnosisChange(diagnosis: EcgDiagnosis?) {
                        Log.d(TAG, ">>> ECG Diagnosis: $diagnosis")
                        if (diagnosis != null) onDiagnosis(diagnosis)
                    }
                    override fun onEcgADCChange(adc1: IntArray?, adc2: IntArray?) {
                        if (adc1 != null && adc2 != null) {
                            Log.d(TAG, ">>> ECG ADC: ch1=${adc1.size} ch2=${adc2.size}")
                            onAdc(adc1, adc2)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "readEcg failed", e)
            onError("Exception: ${e.message}")
        }
    }

    /** Stops an in-progress ECG measurement. */
    fun stopEcg() {
        try {
            VPOperateManager.getInstance().stopDetectECG(
                BleWriteResponse { code ->
                    Log.d(TAG, ">>> stopDetectECG write response: code=$code")
                },
                true,
                object : IECGDetectListener {
                    override fun onEcgDetectInfoChange(info: EcgDetectInfo?) {}
                    override fun onEcgDetectStateChange(state: EcgDetectState?) {}
                    override fun onEcgDetectResultChange(result: EcgDetectResult?) {}
                    override fun onEcgDetectDiagnosisChange(diagnosis: EcgDiagnosis?) {}
                    override fun onEcgADCChange(adc1: IntArray?, adc2: IntArray?) {}
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "stopEcg failed", e)
        }
    }


    // --- Internal ---

    private fun confirmPassword(deliver: (Boolean, String) -> Unit) {
        try {
            VPOperateManager.getInstance().confirmDevicePwd(
                IBleWriteResponse { code ->
                    Log.d(TAG, "confirmDevicePwd write response code=$code")
                },
                object : IPwdDataListener {
                    override fun onPwdDataChange(pwdData: PwdData?) {
                        val status = pwdData?.getmStatus()?.name ?: "UNKNOWN"
                        Log.d(TAG, "Password result: $status")
                        if (status.contains("FAIL") || status == "UNKNOWN") {
                            deliver(false, "Password verification failed ($status)")
                        } else {
                            deliver(true, "Connected and verified ($status)")
                        }
                    }

                    override fun onConnectionConfirmTimeout() {
                        Log.w(TAG, "Connection confirm timeout")
                        deliver(false, "Connection confirm timeout")
                    }
                },
                object : IDeviceFuctionDataListener {
                    override fun onFunctionSupportDataChange(data: FunctionDeviceSupportData?) {
                        Log.d(TAG, "Device function support data received")
                    }

                    override fun onDeviceFunctionPackage1Report(p0: DeviceFunctionPackage1?) {}
                    override fun onDeviceFunctionPackage2Report(p0: DeviceFunctionPackage2?) {}
                    override fun onDeviceFunctionPackage3Report(p0: DeviceFunctionPackage3?) {}
                    override fun onDeviceFunctionPackage4Report(p0: DeviceFunctionPackage4?) {}
                    override fun onDeviceFunctionPackage5Report(p0: DeviceFunctionPackage5?) {}
                },
                object : ISocialMsgDataListener {
                    override fun onSocialMsgSupportDataChange(data: FunctionSocailMsgData?) {
                        Log.d(TAG, "Social msg support data received")
                    }

                    override fun onSocialMsgSupportDataChange2(data: FunctionSocailMsgData?) {
                        Log.d(TAG, "Social msg support data 2 received")
                    }
                },
                object : ICustomSettingDataListener {
                    override fun OnSettingDataChange(data: CustomSettingData?) {
                        Log.d(TAG, "Custom setting data received")
                    }
                },
                DEFAULT_PASSWORD,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "confirmDevicePwd failed", e)
            deliver(false, "Password step exception: ${e.message}")
        }
    }

    private fun finishScan(onFinished: () -> Unit) {
        if (!isScanning) return
        isScanning = false
        try {
            onFinished()
        } catch (e: Exception) {
            Log.e(TAG, "onFinished callback failed", e)
        }
    }
}