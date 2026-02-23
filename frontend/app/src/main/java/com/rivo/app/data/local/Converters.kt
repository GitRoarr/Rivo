package com.rivo.app.data.local

import androidx.room.TypeConverter
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.data.model.NotificationType
import com.rivo.app.data.model.FeaturedType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromUserType(userType: UserType?): String {
        return userType?.name ?: UserType.LISTENER.name
    }

    @TypeConverter
    fun toUserType(userType: String?): UserType {
        return try {
            userType?.let { UserType.valueOf(it) } ?: UserType.LISTENER
        } catch (e: Exception) {
            UserType.LISTENER
        }
    }

    @TypeConverter
    fun fromVerificationStatus(status: VerificationStatus?): String {
        return status?.name ?: VerificationStatus.UNVERIFIED.name
    }

    @TypeConverter
    fun toVerificationStatus(status: String?): VerificationStatus {
        return try {
            status?.let { VerificationStatus.valueOf(it) } ?: VerificationStatus.UNVERIFIED
        } catch (e: Exception) {
            VerificationStatus.UNVERIFIED
        }
    }

    @TypeConverter
    fun fromMusicApprovalStatus(status: MusicApprovalStatus?): String {
        return status?.name ?: MusicApprovalStatus.PENDING.name
    }

    @TypeConverter
    fun toMusicApprovalStatus(status: String?): MusicApprovalStatus {
        return try {
            status?.let { MusicApprovalStatus.valueOf(it) } ?: MusicApprovalStatus.PENDING
        } catch (e: Exception) {
            MusicApprovalStatus.PENDING
        }
    }

    @TypeConverter
    fun fromNotificationType(type: NotificationType?): String {
        return type?.name ?: NotificationType.SYSTEM.name
    }

    @TypeConverter
    fun toNotificationType(type: String?): NotificationType {
        return try {
            type?.let { NotificationType.valueOf(it) } ?: NotificationType.SYSTEM
        } catch (e: Exception) {
            NotificationType.SYSTEM
        }
    }

    @TypeConverter
    fun fromFeaturedType(type: FeaturedType?): String {
        return type?.name ?: FeaturedType.SONG.name
    }

    @TypeConverter
    fun toFeaturedType(type: String?): FeaturedType {
        return try {
            type?.let { FeaturedType.valueOf(it) } ?: FeaturedType.SONG
        } catch (e: Exception) {
            FeaturedType.SONG
        }
    }

    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromIntMap(map: Map<String, Int>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toIntMap(json: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromFloatMap(map: Map<String, Float>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toFloatMap(json: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}