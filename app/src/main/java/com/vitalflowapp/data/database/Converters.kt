package com.vitalflowapp.data.database

import androidx.room.TypeConverter
import com.vitalflowapp.data.model.AchievementType
import com.vitalflowapp.data.model.ArmPosition
import com.vitalflowapp.data.model.BodyPosition
import com.vitalflowapp.data.model.GlucoseType
import com.vitalflowapp.data.model.GoalType
import com.vitalflowapp.data.model.InsightAction
import com.vitalflowapp.data.model.InsightType
import com.vitalflowapp.data.model.MedicationFrequency
import com.vitalflowapp.data.model.ReadingTag
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
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
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
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
    
    @TypeConverter
    fun fromGoalType(value: GoalType): String = value.name
    
    @TypeConverter
    fun toGoalType(value: String): GoalType = GoalType.valueOf(value)
    
    @TypeConverter
    fun fromGlucoseType(value: GlucoseType): String = value.name
    
    @TypeConverter
    fun toGlucoseType(value: String): GlucoseType = GlucoseType.valueOf(value)
    
    @TypeConverter
    fun fromInsightType(value: InsightType): String = value.name
    
    @TypeConverter
    fun toInsightType(value: String): InsightType = InsightType.valueOf(value)
    
    @TypeConverter
    fun fromInsightAction(value: InsightAction?): String? = value?.name
    
    @TypeConverter
    fun toInsightAction(value: String?): InsightAction? = value?.let { InsightAction.valueOf(it) }
    
    @TypeConverter
    fun fromAchievementType(value: AchievementType): String = value.name
    
    @TypeConverter
    fun toAchievementType(value: String): AchievementType = AchievementType.valueOf(value)
}
