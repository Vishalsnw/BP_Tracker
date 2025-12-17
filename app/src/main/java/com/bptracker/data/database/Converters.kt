package com.bptracker.data.database

import androidx.room.TypeConverter
import com.bptracker.data.model.ArmPosition
import com.bptracker.data.model.BodyPosition
import com.bptracker.data.model.MedicationFrequency
import com.bptracker.data.model.ReadingTag
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter)
    }
    
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }
    
    @TypeConverter
    fun fromLocalTimeList(value: List<LocalTime>?): String? {
        return value?.joinToString(",") { it.format(timeFormatter) }
    }
    
    @TypeConverter
    fun toLocalTimeList(value: String?): List<LocalTime>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").filter { it.isNotBlank() }.map { LocalTime.parse(it, timeFormatter) }
    }
    
    @TypeConverter
    fun fromDayOfWeekSet(value: Set<DayOfWeek>?): String? {
        return value?.joinToString(",") { it.name }
    }
    
    @TypeConverter
    fun toDayOfWeekSet(value: String?): Set<DayOfWeek>? {
        return value?.split(",")?.filter { it.isNotBlank() }?.map { DayOfWeek.valueOf(it) }?.toSet()
    }
    
    @TypeConverter
    fun fromReadingTag(value: ReadingTag): String = value.name
    
    @TypeConverter
    fun toReadingTag(value: String): ReadingTag = ReadingTag.valueOf(value)
    
    @TypeConverter
    fun fromArmPosition(value: ArmPosition): String = value.name
    
    @TypeConverter
    fun toArmPosition(value: String): ArmPosition = ArmPosition.valueOf(value)
    
    @TypeConverter
    fun fromBodyPosition(value: BodyPosition): String = value.name
    
    @TypeConverter
    fun toBodyPosition(value: String): BodyPosition = BodyPosition.valueOf(value)
    
    @TypeConverter
    fun fromMedicationFrequency(value: MedicationFrequency): String = value.name
    
    @TypeConverter
    fun toMedicationFrequency(value: String): MedicationFrequency = MedicationFrequency.valueOf(value)
}
