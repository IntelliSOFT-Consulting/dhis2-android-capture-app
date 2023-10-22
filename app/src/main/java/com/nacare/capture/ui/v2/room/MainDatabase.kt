package com.nacare.capture.ui.v2.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities =
    [OrganizationData::class, ProgramDataValues::class, TrackedEntityValues::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
public abstract class MainDatabase : RoomDatabase() {

    abstract fun roomDao(): RoomDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext, MainDatabase::class.java, "nacare_database"
                    )
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
