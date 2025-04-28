package com.example.artistshottest.ui.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artist_shot_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate if needed
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ✅ Solo la clase de TypeConverters aparte (sin AppDatabase dentro)
class Converters {
    // Para convertir de Long a Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // Para convertir de List<String> a String
    @TypeConverter
    fun fromTagsList(tags: List<String>): String {
        return tags.joinToString(separator = ",")
    }

    @TypeConverter
    fun toTagsList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split(",")
    }
}