package com.example.utils

import android.content.Context
import com.example.data.model.AppNotification
import com.example.data.model.NotificationCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object NotificationHistoryManager {
    private const val PREFS_NAME = "zomorrod_notifications_prefs"
    private const val KEY_NOTIFICATIONS = "notifications_json_v1"
    private const val MAX_NOTIFICATIONS = 50

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized && _notifications.value.isNotEmpty()) return
        isInitialized = true
        loadFromPrefs(context)
    }

    private fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_NOTIFICATIONS, null)
        if (jsonStr.isNullOrBlank()) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val farsiNow = FarsiUtils.toFarsiDigits(timeFormat.format(Date()))
            val initialList = listOf(
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "سیستم هوشمند ناوگان آماده به کار است",
                    message = "همگام‌سازی خودکار سفارشات هر ۱ دقیقه انجام می‌پذیرد. پیام‌ها و رویدادهای مهم در این بخش ثبت می‌شوند.",
                    timeFormatted = farsiNow,
                    timestamp = System.currentTimeMillis(),
                    category = NotificationCategory.SYSTEM,
                    isRead = false
                ),
                AppNotification(
                    id = UUID.randomUUID().toString(),
                    title = "ارتباط با مرکز پشتیبانی برقرار است",
                    message = "پیام‌های دریافتی از مرکز پشتیبانی و هماهنگی ناوگان بلافاصله در اینجا ثبت می‌گردند.",
                    timeFormatted = farsiNow,
                    timestamp = System.currentTimeMillis() - 1000,
                    category = NotificationCategory.SUPPORT,
                    isRead = false
                )
            )
            _notifications.value = initialList
            _unreadCount.value = initialList.count { !it.isRead }
            saveToPrefs(context, initialList)
            return
        }

        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<AppNotification>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AppNotification(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", "اعلان مهم"),
                        message = obj.optString("message", ""),
                        timeFormatted = obj.optString("timeFormatted", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        category = try {
                            NotificationCategory.valueOf(obj.optString("category", NotificationCategory.SYSTEM.name))
                        } catch (_: Exception) {
                            NotificationCategory.SYSTEM
                        },
                        isRead = obj.optBoolean("isRead", false),
                        orderId = obj.optString("orderId").takeIf { it.isNotBlank() }
                    )
                )
            }
            _notifications.value = list
            _unreadCount.value = list.count { !it.isRead }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveToPrefs(context: Context, list: List<AppNotification>) {
        try {
            val jsonArray = JSONArray()
            list.take(MAX_NOTIFICATIONS).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("message", item.message)
                    put("timeFormatted", item.timeFormatted)
                    put("timestamp", item.timestamp)
                    put("category", item.category.name)
                    put("isRead", item.isRead)
                    put("orderId", item.orderId ?: "")
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_NOTIFICATIONS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addNotification(
        context: Context,
        title: String,
        message: String,
        category: NotificationCategory,
        orderId: String? = null
    ) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val farsiTime = FarsiUtils.toFarsiDigits(timeFormat.format(Date()))
        val newItem = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timeFormatted = farsiTime,
            timestamp = System.currentTimeMillis(),
            category = category,
            isRead = false,
            orderId = orderId
        )
        val current = _notifications.value.toMutableList()
        current.add(0, newItem)
        val trimmed = current.take(MAX_NOTIFICATIONS)
        _notifications.value = trimmed
        _unreadCount.value = trimmed.count { !it.isRead }
        saveToPrefs(context, trimmed)
    }

    fun markAllAsRead(context: Context) {
        val current = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = current
        _unreadCount.value = 0
        saveToPrefs(context, current)
    }

    fun clearHistory(context: Context) {
        _notifications.value = emptyList()
        _unreadCount.value = 0
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_NOTIFICATIONS)
            .apply()
    }
}
