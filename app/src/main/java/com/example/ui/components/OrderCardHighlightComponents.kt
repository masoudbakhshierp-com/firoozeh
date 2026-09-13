package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.FarsiUtils
import com.example.utils.PricingUtils

/**
 * بلوک نمایش آدرس مشتری با کنتراست فوق‌العاده بالا، خوانایی استاندارد و تایپوگرافی بهینه فارسی
 */
@Composable
fun HighContrastAddressSection(
    address: String,
    modifier: Modifier = Modifier,
    onClickNavigate: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF101920), // رنگ تیره یکدست با کنتراست ماکزیمم
        border = BorderStroke(1.2.dp, Color(0xFF263847)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // پین برجسته موقعیت مکانی
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF381818),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "آدرس مشتری",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نشانی دقیق مشتری:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )

                    if (onClickNavigate != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF12342A),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onClickNavigate() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TurnRight,
                                    contentDescription = null,
                                    tint = CleanGreenPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نشان",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanGreenPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // متن آدرس با کنتراست ۱۰۰٪ سفید و ارتفاع خط بهینه جهت عدم برش حروف فارسی
                Text(
                    text = address.ifBlank { "آدرس در پرونده ثبت نشده است" },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

/**
 * بلوک نمایش وضعیت مالی و مبلغ فاکتور با ارقام درشت فارسی و کنتراست حداکثری
 */
@Composable
fun HighContrastInvoiceSection(
    totalAmount: Long,
    paidAmount: Long,
    discountAmount: Long = 0L,
    isPickupMode: Boolean = false,
    paymentMethod: String = "",
    isSettled: Boolean = false,
    invoiceStatus: String? = null,
    modifier: Modifier = Modifier
) {
    val finalPayable = PricingUtils.calcFinalPayable(totalAmount, discountAmount)
    val remaining = maxOf(0L, finalPayable - paidAmount)
    val hasCalculatedAmount = totalAmount > 0

    // تبدیل روش پرداخت به فارسی و فیلتر کردن مقدار 'unpaid'
    val rawMethod = paymentMethod.trim().lowercase()
    val isUnpaidOrEmpty = rawMethod == "unpaid" || rawMethod == "none" || rawMethod.isBlank()
    val methodFarsi = when {
        isUnpaidOrEmpty -> ""
        rawMethod.contains("cash") || rawMethod.contains("نقد") -> "نقدی"
        rawMethod.contains("pos") || rawMethod.contains("پوز") || rawMethod.contains("کارتخوان") -> "دستگاه پوز"
        rawMethod.contains("card") || rawMethod.contains("کارت") -> "کارت به کارت"
        rawMethod.contains("online") || rawMethod.contains("آنلاین") -> "پرداخت آنلاین"
        else -> ""
    }

    // پس‌زمینه زمردی/خاکستری تیره با حاشیه تفکیک‌شده
    val containerBg = if (hasCalculatedAmount) Color(0xFF09221C) else Color(0xFF1A1F26)
    val borderStrokeColor = if (hasCalculatedAmount) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF2C3947)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerBg,
        border = BorderStroke(1.2.dp, borderStrokeColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // سطر نمایش وضعیت فاکتور در صورت ارسال مقدار
            if (invoiceStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "وضعیت فاکتور:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    val isDriverDone = invoiceStatus.contains("فاکتور شده")
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDriverDone) Color(0xFF133E33) else Color(0xFF332A15),
                        border = BorderStroke(1.dp, if (isDriverDone) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = invoiceStatus,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDriverDone) Color(0xFF34D399) else Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFF183B31), thickness = 0.6.dp)
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (hasCalculatedAmount) {
                // سطر مبالغ فاکتور و مانده قابل دریافت
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ستون سمت راست: مبلغ کل فاکتور
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "مبلغ فاکتور:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = FarsiUtils.formatPrice(totalAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    }

                    // ستون سمت چپ: مانده یا وضعیت تسویه با هایلایت چشمگیر
                    Column(horizontalAlignment = Alignment.End) {
                        when {
                            isSettled || (paidAmount >= finalPayable && finalPayable > 0) -> {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF133E33),
                                    border = BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (methodFarsi.isNotBlank()) "تسویه کامل ($methodFarsi)" else "تسویه کامل",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6EE7B7)
                                        )
                                    }
                                }
                            }
                            remaining > 0 -> {
                                Text(
                                    text = "مانده قابل دریافت:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = FarsiUtils.formatPrice(remaining),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF34D399) // رنگ سبز فسفری روشن با کنتراست بالا
                                )
                            }
                            else -> {
                                Text(
                                    text = "مبلغ قابل پرداخت:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = FarsiUtils.formatPrice(finalPayable),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }

                // در صورت وجود تخفیف یا پیش‌پرداخت
                if (discountAmount > 0 || (paidAmount > 0 && remaining > 0)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0xFF183B31), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (discountAmount > 0) {
                            Text(
                                text = "تخفیف: ${FarsiUtils.formatPrice(discountAmount)}",
                                fontSize = 10.5.sp,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        if (paidAmount > 0 && remaining > 0) {
                            Text(
                                text = "پیش‌پرداخت شده: ${FarsiUtils.formatPrice(paidAmount)}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            } else {
                // حالت جمع‌آوری بدون مبلغ نهایی قبلی
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2C2415),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (invoiceStatus != null) "مبلغ فاکتور: در انتظار ثبت" else "مبلغ فاکتور:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2B2011),
                        border = BorderStroke(1.dp, Color(0xFFB45309).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isPickupMode) "صدور فاکتور در محل مشتری" else "نیاز به اندازه‌گیری",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
