package com.bptracker.ui.screens.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.model.Reminder
import com.bptracker.data.repository.ReminderRepository
import com.bptracker.utils.ReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    init {
        loadReminders()
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            repository.getAllReminders().collect { reminders ->
                _uiState.update { it.copy(reminders = reminders, isLoading = false) }
            }
        }
    }
    
    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            if (reminder.isEnabled) {
                scheduleReminder(reminder.copy(id = id))
            }
        }
    }
    
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            cancelReminder(reminder.id)
            if (reminder.isEnabled) {
                scheduleReminder(reminder)
            }
        }
    }
    
    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            repository.updateReminder(updated)
            
            if (updated.isEnabled) {
                scheduleReminder(updated)
            } else {
                cancelReminder(updated.id)
            }
        }
    }
    
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            cancelReminder(reminder.id)
            repository.deleteReminder(reminder)
        }
    }
    
    private fun scheduleReminder(reminder: Reminder) {
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek
        
        val nextDay = reminder.daysOfWeek
            .map { day ->
                val daysUntil = (day.value - currentDayOfWeek.value + 7) % 7
                if (daysUntil == 0) {
                    val todayDateTime = LocalDateTime.of(today, reminder.time)
                    if (todayDateTime.isAfter(LocalDateTime.now())) {
                        0
                    } else {
                        7
                    }
                } else {
                    daysUntil
                }
            }
            .minOrNull() ?: return
        
        val nextDate = today.plusDays(nextDay.toLong())
        val nextDateTime = LocalDateTime.of(nextDate, reminder.time)
        val triggerTime = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_label", reminder.label)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    private fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
