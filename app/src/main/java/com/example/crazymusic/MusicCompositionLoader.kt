package com.example.crazymusic

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object MusicCompositionLoader {

    fun loadCompositions(context: Context): List<MusicComposition> {
        val prefs = context.getSharedPreferences("music_compositions", Context.MODE_PRIVATE)
        val compositions = mutableListOf<MusicComposition>()
        val gson = Gson()

        prefs.all.forEach { (_, value) ->
            try {
                val jsonString = value as? String ?: return@forEach
                val composition = gson.fromJson(jsonString, MusicComposition::class.java)
                compositions.add(composition)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return compositions.sortedByDescending { it.creationDate }
    }

    fun loadCompositionById(context: Context, compositionId: String): MusicComposition? {
        val prefs = context.getSharedPreferences("music_compositions", Context.MODE_PRIVATE)
        val json = prefs.getString(compositionId, null) ?: return null

        return try {
            Gson().fromJson(json, MusicComposition::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteComposition(context: Context, compositionId: String) {
        val prefs = context.getSharedPreferences("music_compositions", Context.MODE_PRIVATE)
        prefs.edit().remove(compositionId).apply()
    }

    fun saveComposition(context: Context, composition: MusicComposition) {
        val prefs = context.getSharedPreferences("music_compositions", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(composition)
        prefs.edit().putString(composition.id, json).apply()
    }
}