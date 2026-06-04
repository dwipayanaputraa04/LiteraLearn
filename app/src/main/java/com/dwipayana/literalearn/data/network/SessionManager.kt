package com.dwipayana.literalearn.data.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("USER_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("USER_TOKEN", null)
    }

    fun saveUserUuid(uuid: String) {
        prefs.edit().putString("USER_UUID", uuid).apply()
    }

    fun getUserUuid(): String? {
        return prefs.getString("USER_UUID", null)
    }

    /**
     * Menyimpan skor kuis ke indeks tertentu (berdasarkan urutan Bab 1-8)
     * Data disimpan per User UUID agar tetap ada saat login ulang.
     */
    fun saveScoreByOrder(order: Int, score: Float, moduleTitle: String? = null) {
        val uuid = getUserUuid() ?: "DEFAULT"
        val currentHistory = getQuizHistory().toMutableList()
        val index = (order - 1).coerceIn(0, 7)
        currentHistory[index] = score
        
        val historyString = currentHistory.joinToString(",")
        
        val editor = prefs.edit()
        editor.putString("QUIZ_HISTORY_$uuid", historyString)
        
        // Simpan info kuis terakhir untuk rekomendasi realtime (dalam sesi aktif)
        editor.putFloat("LAST_QUIZ_SCORE", score)
        if (moduleTitle != null) {
            editor.putString("LAST_QUIZ_MODULE", moduleTitle)
        }
        
        editor.apply()
    }

    fun getLastQuizScore(): Float {
        return prefs.getFloat("LAST_QUIZ_SCORE", 0f)
    }

    fun getLastQuizModule(): String? {
        return prefs.getString("LAST_QUIZ_MODULE", null)
    }

    /**
     * Menambahkan poin ke total poin user berdasarkan UUID
     */
    fun addPoints(points: Int) {
        val uuid = getUserUuid() ?: "DEFAULT"
        val currentPoints = getTotalPoints()
        prefs.edit().putInt("TOTAL_POINTS_$uuid", currentPoints + points).apply()
    }

    /**
     * Mengambil total poin user berdasarkan UUID. Default 0.
     */
    fun getTotalPoints(): Int {
        val uuid = getUserUuid() ?: "DEFAULT"
        return prefs.getInt("TOTAL_POINTS_$uuid", 0)
    }

    /**
     * Mengambil 8 skor kuis berdasarkan UUID.
     */
    fun getQuizHistory(): List<Float> {
        val uuid = getUserUuid() ?: "DEFAULT"
        val historyString = prefs.getString("QUIZ_HISTORY_$uuid", "") ?: ""
        if (historyString.isEmpty()) {
            return listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }
        return historyString.split(",").map { it.toFloat() }
    }

    /**
     * Menghapus sesi aktif (token & uuid), tetapi tetap menyimpan data poin/history di memori.
     */
    fun clearSession() {
        prefs.edit()
            .remove("USER_TOKEN")
            .remove("USER_UUID")
            .remove("LAST_QUIZ_SCORE")
            .remove("LAST_QUIZ_MODULE")
            .apply()
    }
}
