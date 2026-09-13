package com.example.data

/**
 * نگه‌دارنده سراسری نام کارگاه و تلفن پشتیبانی برای لایه‌هایی که مستقیماً به ViewModel دسترسی ندارند
 * (مانند Repository، NotificationManager، PrinterManager و SupabaseManager).
 */
object WorkshopNameHolder {
    var current: String = "کارگاه"
    var supportPhone: String = ""
}
