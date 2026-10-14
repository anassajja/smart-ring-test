package com.luffy.smartringtest

/**
 * A single 5-minute health reading pulled from the ring's history.
 * Every field comes from the SDK's OriginData class.
 */
data class HealthReading(
    val date: String,          // "2026-10-13"
    val time: String,          // "14:35"
    val heartRate: Int,        // bpm (0 = no reading)
    val steps: Int,            // steps in this 5-min window
    val distanceMeters: Double,
    val calories: Double,
    val sportValue: Int,       // activity intensity 0..65535
    val wear: Int              // 0/1 = ring on/off
)