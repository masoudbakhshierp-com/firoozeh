package com.example.data.model

enum class NotificationCategory {
    MISSION,     // سفارش جدید، جمع‌آوری، تحویل
    SUPPORT,     // پیام دریافتی از مرکز پشتیبانی
    SYNC,        // گزارش همگام‌سازی با سرور مرکزی
    SYSTEM       // هشدارها و رویدادهای سیستم
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeFormatted: String,
    val timestamp: Long,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val orderId: String? = null
)
