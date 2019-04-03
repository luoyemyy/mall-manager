package com.github.luoyemyy.mall.manager.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.luoyemyy.mall.manager.bean.User
import java.util.*

//@Database(entities = [], version = 1)
//@TypeConverters(DateConverters::class)
abstract class Db : RoomDatabase() {

    companion object {

        @Volatile
        private var instance: Db? = null

        private const val DB_NAME = "mall"

        private fun createDb(appContext: Context): Db {
            return Room.databaseBuilder(appContext, Db::class.java, DB_NAME).addCallback(object : Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {

                }
            }).build()
        }

        fun getInstance(context: Context): Db {
            return instance ?: synchronized(this) {
                instance ?: createDb(context).also { instance = it }
            }
        }
    }
}

class DateConverters {
    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toLong(date: Date?): Long? = date?.time
}