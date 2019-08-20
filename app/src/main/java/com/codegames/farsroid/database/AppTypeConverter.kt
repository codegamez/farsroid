package com.codegames.farsroid.database

import androidx.room.TypeConverter
import com.codegames.farsroid.gson
import com.google.gson.reflect.TypeToken


@Suppress("unused")
class AppTypeConverter {

    @TypeConverter
    fun stringArrayToJson(arr: Array<String>): String {
        return gson.toJson(arr)
    }

    @TypeConverter
    fun jsonToStringArray(json: String): Array<String> {
        return try {
            gson.fromJson(json, Array<String>::class.java)
        } catch (t: Throwable) {
            arrayOf()
        }
    }

    @TypeConverter
    fun pairStringArrayToJson(arr: Array<Pair<String, String>>): String {
        return gson.toJson(arr)
    }

    @TypeConverter
    fun jsonToPairStringArray(json: String): Array<Pair<String, String>> {
        val type = object : TypeToken<Array<Pair<String, String>>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (t: Throwable) {
            arrayOf()
        }
    }

}