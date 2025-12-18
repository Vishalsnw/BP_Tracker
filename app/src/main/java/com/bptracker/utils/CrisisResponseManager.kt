package com.bptracker.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bptracker.data.model.BloodPressureReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val Context.crisisDataStore by preferencesDataStore(name = "crisis_settings")

data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val isPrimary: Boolean = false
)

data class CrisisSettings(
    val isEnabled: Boolean = true,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val autoCall911: Boolean = false,
    val sendSmsAlert: Boolean = true,
    val includeLocation: Boolean = false,
    val customMessage: String = ""
)

@Singleton
class CrisisResponseManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val EMERGENCY_CONTACTS_KEY = stringPreferencesKey("emergency_contacts")
        private val AUTO_CALL_911_KEY = stringPreferencesKey("auto_call_911")
        private val SEND_SMS_ALERT_KEY = stringPreferencesKey("send_sms_alert")
        private val CUSTOM_MESSAGE_KEY = stringPreferencesKey("custom_message")
        private val IS_ENABLED_KEY = stringPreferencesKey("crisis_enabled")
        
        private const val DEFAULT_SMS_MESSAGE = "HEALTH ALERT: Blood pressure reading indicates a hypertensive crisis. "
        
        fun formatCrisisMessage(reading: BloodPressureReading): String {
            val timestamp = reading.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
            return """
                |BLOOD PRESSURE EMERGENCY ALERT
                |
                |Reading: ${reading.systolic}/${reading.diastolic} mmHg
                |Pulse: ${reading.pulse} bpm
                |Time: $timestamp
                |Category: ${reading.category.label}
                |
                |This is a hypertensive crisis reading. Please check on the person who sent this alert.
            """.trimMargin()
        }
    }
    
    fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == 
            PackageManager.PERMISSION_GRANTED
    }
    
    fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == 
            PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun saveEmergencyContacts(contacts: List<EmergencyContact>) {
        val contactsJson = contacts.joinToString(";") { 
            "${it.name}|${it.phoneNumber}|${it.isPrimary}" 
        }
        context.crisisDataStore.edit { prefs ->
            prefs[EMERGENCY_CONTACTS_KEY] = contactsJson
        }
    }
    
    fun getEmergencyContacts(): Flow<List<EmergencyContact>> {
        return context.crisisDataStore.data.map { prefs ->
            val contactsStr = prefs[EMERGENCY_CONTACTS_KEY] ?: ""
            if (contactsStr.isBlank()) {
                emptyList()
            } else {
                contactsStr.split(";").mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size >= 2) {
                        EmergencyContact(
                            name = parts[0],
                            phoneNumber = parts[1],
                            isPrimary = parts.getOrNull(2)?.toBoolean() ?: false
                        )
                    } else null
                }
            }
        }
    }
    
    suspend fun saveCrisisSettings(settings: CrisisSettings) {
        context.crisisDataStore.edit { prefs ->
            prefs[IS_ENABLED_KEY] = settings.isEnabled.toString()
            prefs[AUTO_CALL_911_KEY] = settings.autoCall911.toString()
            prefs[SEND_SMS_ALERT_KEY] = settings.sendSmsAlert.toString()
            prefs[CUSTOM_MESSAGE_KEY] = settings.customMessage
        }
        saveEmergencyContacts(settings.emergencyContacts)
    }
    
    fun getCrisisSettings(): Flow<CrisisSettings> {
        return context.crisisDataStore.data.map { prefs ->
            CrisisSettings(
                isEnabled = prefs[IS_ENABLED_KEY]?.toBoolean() ?: true,
                autoCall911 = prefs[AUTO_CALL_911_KEY]?.toBoolean() ?: false,
                sendSmsAlert = prefs[SEND_SMS_ALERT_KEY]?.toBoolean() ?: true,
                customMessage = prefs[CUSTOM_MESSAGE_KEY] ?: ""
            )
        }
    }
    
    fun initiateEmergencyCall() {
        if (!hasCallPermission()) return
        
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:911")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:911")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
    
    fun callEmergencyContact(contact: EmergencyContact) {
        if (!hasCallPermission()) {
            dialNumber(contact.phoneNumber)
            return
        }
        
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            dialNumber(contact.phoneNumber)
        }
    }
    
    private fun dialNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    @Suppress("DEPRECATION")
    fun sendSmsAlert(contact: EmergencyContact, reading: BloodPressureReading, customMessage: String = "") {
        if (!hasSmsPermission()) return
        
        val message = if (customMessage.isNotBlank()) {
            "$customMessage\n\n${formatCrisisMessage(reading)}"
        } else {
            formatCrisisMessage(reading)
        }
        
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                contact.phoneNumber,
                null,
                parts,
                null,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun triggerCrisisResponse(reading: BloodPressureReading) {
        val settings = getCrisisSettings().first()
        val contacts = getEmergencyContacts().first()
        
        if (!settings.isEnabled) return
        
        if (settings.sendSmsAlert && hasSmsPermission()) {
            contacts.forEach { contact ->
                sendSmsAlert(contact, reading, settings.customMessage)
            }
        }
        
        if (settings.autoCall911 && hasCallPermission()) {
            initiateEmergencyCall()
        } else {
            contacts.find { it.isPrimary }?.let { primaryContact ->
                callEmergencyContact(primaryContact)
            }
        }
    }
    
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
    }
    
    fun createEmergencyDialIntent(): Intent {
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:911")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    fun createShareBPDataIntent(reading: BloodPressureReading): Intent {
        val message = formatCrisisMessage(reading)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Blood Pressure Emergency Alert")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
