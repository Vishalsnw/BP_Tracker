package com.vitalflowapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.BloodGlucose
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Pressure
import com.vitalflowapp.data.model.BloodPressureReading
import com.vitalflowapp.data.model.GlucoseEntry
import com.vitalflowapp.data.model.GlucoseType
import com.vitalflowapp.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

sealed class HealthConnectAvailability {
    object Available : HealthConnectAvailability()
    object NotInstalled : HealthConnectAvailability()
    object NotSupported : HealthConnectAvailability()
}

data class HealthConnectSyncResult(
    val success: Boolean,
    val syncedReadings: Int = 0,
    val errorMessage: String? = null
)

@Singleton
class HealthConnectManager @Inject constructor(
    private val context: Context
) {
    private var healthConnectClient: HealthConnectClient? = null
    
    val permissions = setOf(
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getWritePermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getWritePermission(BloodGlucoseRecord::class)
    )
    
    fun checkAvailability(): HealthConnectAvailability {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
                HealthConnectAvailability.Available
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectAvailability.NotInstalled
            }
            else -> HealthConnectAvailability.NotSupported
        }
    }
    
    fun getHealthConnectInstallIntent(): Intent {
        val uri = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        return Intent(Intent.ACTION_VIEW, uri)
    }
    
    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return permissions.all { it in granted }
    }
    
    fun createPermissionRequestContract() = PermissionController.createRequestPermissionResultContract()
    
    suspend fun writeBloodPressureReading(reading: BloodPressureReading): Boolean {
        val client = healthConnectClient ?: return false
        
        return try {
            val instant = reading.timestamp.atZone(ZoneId.systemDefault()).toInstant()
            val record = BloodPressureRecord(
                time = instant,
                zoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                systolic = Pressure.millimetersOfMercury(reading.systolic.toDouble()),
                diastolic = Pressure.millimetersOfMercury(reading.diastolic.toDouble()),
                bodyPosition = when (reading.bodyPosition.name) {
                    "SITTING" -> BloodPressureRecord.BODY_POSITION_SITTING_DOWN
                    "STANDING" -> BloodPressureRecord.BODY_POSITION_STANDING_UP
                    "LYING_DOWN" -> BloodPressureRecord.BODY_POSITION_LYING_DOWN
                    else -> BloodPressureRecord.BODY_POSITION_UNKNOWN
                },
                measurementLocation = when (reading.armPosition.name) {
                    "LEFT" -> BloodPressureRecord.MEASUREMENT_LOCATION_LEFT_UPPER_ARM
                    "RIGHT" -> BloodPressureRecord.MEASUREMENT_LOCATION_RIGHT_UPPER_ARM
                    else -> BloodPressureRecord.MEASUREMENT_LOCATION_UNKNOWN
                }
            )
            
            client.insertRecords(listOf(record))
            
            if (reading.pulse > 0) {
                val heartRateRecord = HeartRateRecord(
                    startTime = instant,
                    endTime = instant.plusSeconds(1),
                    startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                    endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                    samples = listOf(
                        HeartRateRecord.Sample(
                            time = instant,
                            beatsPerMinute = reading.pulse.toLong()
                        )
                    )
                )
                client.insertRecords(listOf(heartRateRecord))
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun writeWeightEntry(entry: WeightEntry): Boolean {
        val client = healthConnectClient ?: return false
        
        return try {
            val instant = entry.timestamp.atZone(ZoneId.systemDefault()).toInstant()
            val record = WeightRecord(
                time = instant,
                zoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                weight = Mass.kilograms(entry.weightKg)
            )
            
            client.insertRecords(listOf(record))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun writeGlucoseEntry(entry: GlucoseEntry): Boolean {
        val client = healthConnectClient ?: return false
        
        return try {
            val instant = entry.timestamp.atZone(ZoneId.systemDefault()).toInstant()
            val relationToMealValue = when (entry.type) {
                GlucoseType.FASTING -> BloodGlucoseRecord.RELATION_TO_MEAL_FASTING
                GlucoseType.POST_MEAL -> BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL
                else -> BloodGlucoseRecord.RELATION_TO_MEAL_UNKNOWN
            }
            
            val record = BloodGlucoseRecord(
                time = instant,
                zoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                level = BloodGlucose.milligramsPerDeciliter(entry.glucoseMgDl),
                specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD,
                relationToMeal = relationToMealValue
            )
            
            client.insertRecords(listOf(record))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun readBloodPressureRecords(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<BloodPressureRecord> {
        val client = healthConnectClient ?: return emptyList()
        
        return try {
            val startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant()
            val endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant()
            
            val request = ReadRecordsRequest(
                recordType = BloodPressureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant)
            )
            
            client.readRecords(request).records
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun syncAllReadings(readings: List<BloodPressureReading>): HealthConnectSyncResult {
        if (healthConnectClient == null) {
            return HealthConnectSyncResult(false, errorMessage = "Health Connect not available")
        }
        
        var synced = 0
        readings.forEach { reading ->
            if (writeBloodPressureReading(reading)) {
                synced++
            }
        }
        
        return HealthConnectSyncResult(true, synced)
    }
}
