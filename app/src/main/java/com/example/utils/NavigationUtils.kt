package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object NavigationUtils {

    const val NESHAN_API_KEY = "service.eb686e96487f482e862564535b04f38f"

    /**
     * تولید لینک مستقیم تحت وب نقشه نشان با قرارگیری پین در مقصد و زوم ۱۸ (فرمت استاندارد اشتراک‌گذاری نشان)
     * نمونه: https://neshan.org/maps/share/36.213,58.796#c36.213-58.796-18z-0p
     */
    fun getNeshanWebUrl(lat: Double, lng: Double): String {
        val latStr = String.format(Locale.US, "%.5f", lat).trimEnd('0').trimEnd('.')
        val lngStr = String.format(Locale.US, "%.5f", lng).trimEnd('0').trimEnd('.')
        return "https://neshan.org/maps/share/$latStr,$lngStr#c$latStr-$lngStr-18z-0p"
    }

    /**
     * تولید لینک کوتاه رسمی نشان (nshn.ir) جهت باز شدن مستقیم در اپ یا نسخه وب
     * نمونه: https://nshn.ir/?lat=36.213&lng=58.796
     */
    fun getNeshanShortUrl(lat: Double, lng: Double): String {
        val latStr = String.format(Locale.US, "%.5f", lat).trimEnd('0').trimEnd('.')
        val lngStr = String.format(Locale.US, "%.5f", lng).trimEnd('0').trimEnd('.')
        return "https://nshn.ir/?lat=$latStr&lng=$lngStr"
    }

    /**
     * هدایت راننده به مسیریابی با نشان.
     * ۱. ابتدا تلاش می‌کند اپلیکیشن نصب‌شده نشان را باز کند.
     * ۲. در صورت عدم نصب، لینک وب رسمی نشان با پین و زوم ۱۸ را در مرورگر باز می‌کند.
     */
    fun launchNeshan(context: Context, lat: Double, lng: Double, address: String) {
        val latStr = String.format(Locale.US, "%.5f", lat).trimEnd('0').trimEnd('.')
        val lngStr = String.format(Locale.US, "%.5f", lng).trimEnd('0').trimEnd('.')

        // پکیج‌های رسمی مسیریاب نشان در اندروید (کافه‌بازار و گوگل‌پلی)
        val neshanPackages = listOf(
            "org.rajman.neshan.traffic.tehran.navigator",
            "org.rajman.neshan"
        )

        var isAppLaunched = false

        // ۱. تلاش برای باز کردن مستقیم در پکیج رسمی اپلیکیشن نشان
        for (pkg in neshanPackages) {
            try {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("nshn:$latStr,$lngStr")).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(appIntent)
                isAppLaunched = true
                break
            } catch (_: Exception) {
                // پکیج وجود ندارد یا فعال نیست، بسته بعدی بررسی می‌شود
            }
        }

        // ۲. در صورت نیافتن پکیج خاص، تلاش با پروتکل عمومی nshn:
        if (!isAppLaunched) {
            try {
                val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("nshn:$latStr,$lngStr")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(genericIntent)
                isAppLaunched = true
            } catch (_: Exception) {
                // اپ نشان نصب نیست
            }
        }

        // ۳. در صورت عدم نصب اپلیکیشن، باز کردن لینک استاندارد تحت وب نشان با نشانگر و زوم ۱۸
        if (!isAppLaunched) {
            try {
                val webUrl = getNeshanWebUrl(lat, lng)
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                isAppLaunched = true
            } catch (_: Exception) {
                // ۴. آخرین سنگر: فراخوانی نقشه پیش‌فرض دستگاه (geo:)
                launchGenericGeo(context, lat, lng, address)
            }
        }
    }

    private fun launchGenericGeo(context: Context, lat: Double, lng: Double, label: String) {
        try {
            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
            val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "برنامه مسیریاب یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "امکان برقراری تماس وجود ندارد", Toast.LENGTH_SHORT).show()
        }
    }
}
