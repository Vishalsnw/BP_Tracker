package com.bptracker.utils

import android.content.Context
import android.content.Intent
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.GlucoseEntry
import com.bptracker.data.model.Medication
import com.bptracker.data.model.Reminder
import com.bptracker.data.model.UserProfile
import com.bptracker.data.model.WeightEntry
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val version: Int = 1,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val readings: List<BloodPressureReading> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val profiles: List<UserProfile> = emptyList(),
    val weightEntries: List<WeightEntry> = emptyList(),
    val glucoseEntries: List<GlucoseEntry> = emptyList()
)

data class BackupMetadata(
    val id: String,
    val name: String,
    val timestamp: LocalDateTime,
    val size: Long
)

sealed class BackupState {
    object Idle : BackupState()
    object SignedOut : BackupState()
    data class SignedIn(val email: String) : BackupState()
    object BackingUp : BackupState()
    object Restoring : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

class LocalDateTimeTypeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }
    
    override fun read(reader: JsonReader): LocalDateTime? {
        val value = reader.nextString()
        return if (value.isNullOrBlank()) null else LocalDateTime.parse(value, formatter)
    }
}

@Singleton
class CloudBackupManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val APP_FOLDER_NAME = "BPTrackerBackups"
        private const val BACKUP_FILE_PREFIX = "bp_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }
    
    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state.asStateFlow()
    
    private val _backups = MutableStateFlow<List<BackupMetadata>>(emptyList())
    val backups: StateFlow<List<BackupMetadata>> = _backups.asStateFlow()
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var driveService: Drive? = null
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .create()
    
    init {
        initGoogleSignIn()
        checkSignInStatus()
    }
    
    private fun initGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
    }
    
    private fun checkSignInStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            initDriveService(account)
            _state.value = BackupState.SignedIn(account.email ?: "Unknown")
        } else {
            _state.value = BackupState.SignedOut
        }
    }
    
    fun getSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }
    
    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            initDriveService(account)
            _state.value = BackupState.SignedIn(account.email ?: "Unknown")
        } else {
            _state.value = BackupState.Error("Sign in failed")
        }
    }
    
    fun handleSignInError(errorMessage: String) {
        _state.value = BackupState.Error(errorMessage)
    }
    
    private fun initDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("BP Tracker")
            .build()
    }
    
    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            googleSignInClient?.signOut()?.addOnCompleteListener {
                driveService = null
                _state.value = BackupState.SignedOut
            }
        }
    }
    
    suspend fun createBackup(data: BackupData): Boolean {
        val drive = driveService ?: run {
            _state.value = BackupState.Error("Not signed in")
            return false
        }
        
        _state.value = BackupState.BackingUp
        
        return withContext(Dispatchers.IO) {
            try {
                val folderId = getOrCreateAppFolder(drive)
                
                val jsonData = gson.toJson(data)
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val fileName = "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
                
                val fileMetadata = File()
                fileMetadata.name = fileName
                fileMetadata.parents = listOf(folderId)
                fileMetadata.mimeType = "application/json"
                
                val content = ByteArrayContent.fromString("application/json", jsonData)
                
                drive.files().create(fileMetadata, content)
                    .setFields("id")
                    .execute()
                
                _state.value = BackupState.Success("Backup created successfully")
                refreshBackupList()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupState.Error("Backup failed: ${e.message}")
                false
            }
        }
    }
    
    suspend fun restoreBackup(backupId: String): BackupData? {
        val drive = driveService ?: run {
            _state.value = BackupState.Error("Not signed in")
            return null
        }
        
        _state.value = BackupState.Restoring
        
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = ByteArrayOutputStream()
                drive.files().get(backupId)
                    .executeMediaAndDownloadTo(outputStream)
                
                val jsonData = outputStream.toString("UTF-8")
                val backupData = gson.fromJson(jsonData, BackupData::class.java)
                
                _state.value = BackupState.Success("Backup restored successfully")
                backupData
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = BackupState.Error("Restore failed: ${e.message}")
                null
            }
        }
    }
    
    suspend fun refreshBackupList() {
        val drive = driveService ?: return
        
        withContext(Dispatchers.IO) {
            try {
                val folderId = getOrCreateAppFolder(drive)
                
                val result: FileList = drive.files().list()
                    .setQ("'$folderId' in parents and mimeType='application/json'")
                    .setSpaces("drive")
                    .setFields("files(id, name, createdTime, size)")
                    .setOrderBy("createdTime desc")
                    .execute()
                
                val backupList = result.files?.mapNotNull { file ->
                    try {
                        val timestamp = parseBackupTimestamp(file.name)
                        BackupMetadata(
                            id = file.id,
                            name = file.name,
                            timestamp = timestamp,
                            size = file.getSize() ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                _backups.value = backupList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun deleteBackup(backupId: String): Boolean {
        val drive = driveService ?: return false
        
        return withContext(Dispatchers.IO) {
            try {
                drive.files().delete(backupId).execute()
                refreshBackupList()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    private fun getOrCreateAppFolder(drive: Drive): String {
        val query = "name='$APP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false"
        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .execute()
        
        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val folderMetadata = File()
            folderMetadata.name = APP_FOLDER_NAME
            folderMetadata.mimeType = "application/vnd.google-apps.folder"
            
            val folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            folder.id
        }
    }
    
    private fun parseBackupTimestamp(fileName: String): LocalDateTime {
        val timestampStr = fileName
            .removePrefix(BACKUP_FILE_PREFIX)
            .removeSuffix(BACKUP_FILE_EXTENSION)
        
        return LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    }
    
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
}
