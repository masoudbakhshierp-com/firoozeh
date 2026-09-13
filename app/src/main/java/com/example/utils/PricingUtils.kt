package com.example.utils

/**
 * محاسبه‌ی مبلغ کل/مالیات/مبلغ نهایی — باید همیشه دقیقاً با تابع
 * computeOrderTotals در supabase/functions/_shared/orderMapping.ts (سمت
 * سرور، تک منبع حقیقتِ نهایی) یکی بماند. قبل از این فایل، هیچ‌جای اپ
 * مالیات را محاسبه نمی‌کرد؛ ستون taxAmount در OrderEntity از قبل وجود
 * داشت ولی همیشه ۰ می‌ماند و هیچ‌جا استفاده نمی‌شد.
 */
object PricingUtils {

    /** باید دقیقاً با VAT_RATE سمت سرور و OrdersView.tsx (پنل) یکی باشد. */
    const val VAT_RATE: Double = 0.09

    fun calcTax(totalAmount: Long): Long =
        Math.round(totalAmount * VAT_RATE)

    /** مبلغ نهایی قابل‌پرداخت = مبلغ کل + مالیات - تخفیف (هرگز منفی نمی‌شود). */
    fun calcFinalPayable(totalAmount: Long, discountAmount: Long): Long {
        val tax = calcTax(totalAmount)
        return (totalAmount + tax - discountAmount).coerceAtLeast(0L)
    }
}
