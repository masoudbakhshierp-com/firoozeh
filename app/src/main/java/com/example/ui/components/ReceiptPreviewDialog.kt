package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.OrderWithItems
import com.example.ui.theme.CleanPurpleAccent
import com.example.ui.theme.CleanPurpleContainer
import com.example.utils.FarsiUtils
import com.example.utils.PrinterManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewDialog(
    title: String,
    orderWithItems: OrderWithItems,
    paymentMethodLabel: String = "پیش‌فاکتور اولیه",
    isPrinting: Boolean,
    supportPhoneOverride: String? = null,
    onDismiss: () -> Unit,
    onPrintConfirm: () -> Unit
) {
    val order = orderWithItems.order
    val receiptTaxAmount = com.example.utils.PricingUtils.calcTax(order.totalAmount)
    val finalPayable = com.example.utils.PricingUtils.calcFinalPayable(order.totalAmount, order.discountAmount)
    val workshopName = com.example.data.WorkshopNameHolder.current.ifBlank { "قالیشویی هوشمند یاس" }
    
    val effectiveSupportPhone = (supportPhoneOverride ?: com.example.data.WorkshopNameHolder.supportPhone).trim().ifBlank {
        "۰۹۱۲-۰۰۰-۰۰۰۰"
    }

    var printTwoCopies by remember { mutableStateOf(PrinterManager.defaultTwoCopies) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // سربرگ دیالوگ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = CleanPurpleAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پیش‌نمایش فاکتور رسمی چاپگر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                // کارت نام کارگاه و وضعیت جدول‌بندی
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanPurpleContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = CleanPurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = workshopName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = CleanPurpleAccent,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "جدول‌بندی مهندسی اقلام • ثبت شماره پشتیبانی • بهینه‌سازی رول حرارتی",
                            fontSize = 10.5.sp,
                            color = Color(0xFF374151),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // انتخاب تعداد نسخه‌ها جهت کنترل مصرف کاغذ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = printTwoCopies,
                        onClick = {
                            printTwoCopies = true
                            PrinterManager.defaultTwoCopies = true
                        },
                        label = {
                            Text(
                                text = "۲ نسخه (مشتری + راننده)",
                                fontSize = 11.5.sp,
                                fontWeight = if (printTwoCopies) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (printTwoCopies) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = !printTwoCopies,
                        onClick = {
                            printTwoCopies = false
                            PrinterManager.defaultTwoCopies = false
                        },
                        label = {
                            Text(
                                text = "تک‌نسخه اقتصادی (صرفه‌جویی)",
                                fontSize = 11.5.sp,
                                fontWeight = if (!printTwoCopies) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (!printTwoCopies) {
                                Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // شبیه‌ساز واقعی رول کاغذ حرارتی (Thermal POS Paper Roll)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8)), // پس‌زمینه شکیل و طبیعی کاغذ حرارتی
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- نسخه ۱: نسخه مشتری ---
                        ReceiptPaperView(
                            workshopName = workshopName,
                            receiptTitle = title.ifBlank { "فاکتور خدمات قالیشویی" },
                            copyLabel = if (printTwoCopies) "نسخه مشتری" else "نسخه تک‌برگ اقتصادی",
                            orderWithItems = orderWithItems,
                            paymentMethodLabel = paymentMethodLabel,
                            taxAmount = receiptTaxAmount,
                            finalPayable = finalPayable,
                            supportPhone = effectiveSupportPhone
                        )

                        // در صورت انتخاب ۲ نسخه، نسخه راننده با خط برش متحرک نمایش داده می‌شود
                        if (printTwoCopies) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF9CA3AF))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "  محل برش کاغذ پرینتر (نسخه راننده)  ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                                Icon(
                                    Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF9CA3AF))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // --- نسخه ۲: نسخه راننده و بایگانی ---
                            ReceiptPaperView(
                                workshopName = workshopName,
                                receiptTitle = title.ifBlank { "فاکتور خدمات قالیشویی" },
                                copyLabel = "نسخه راننده و بایگانی",
                                orderWithItems = orderWithItems,
                                paymentMethodLabel = paymentMethodLabel,
                                taxAmount = receiptTaxAmount,
                                finalPayable = finalPayable,
                                supportPhone = effectiveSupportPhone
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        PrinterManager.defaultTwoCopies = printTwoCopies
                        onPrintConfirm()
                    },
                    enabled = !isPrinting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CleanPurpleAccent)
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (printTwoCopies) "در حال چاپ ۲ نسخه فاکتور..." else "در حال چاپ تک‌نسخه...",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (printTwoCopies) {
                                "ارسال و چاپ فاکتور ۲ نسخه‌ای (مشتری + راننده)"
                            } else {
                                "ارسال و چاپ فاکتور اقتصادی تک‌نسخه (صرفه‌جویی)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * کامپوننت گرافیکی و دقیق فاکتور حرارتی به همراه جدول‌بندی استاندارد و بدون به‌هم‌ریختگی
 */
@Composable
private fun ReceiptPaperView(
    workshopName: String,
    receiptTitle: String,
    copyLabel: String,
    orderWithItems: OrderWithItems,
    paymentMethodLabel: String,
    taxAmount: Long,
    finalPayable: Long,
    supportPhone: String
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // QR Code و شماره سفارش
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(6.dp)
            ) {
                QrCodeView(code = "ORD-${order.id}", size = 70.dp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ORD-${FarsiUtils.toFarsiDigits(order.id)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // سربرگ درشت و کادر دولایه نام کارگاه
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, Color(0xFF111827)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "★ $workshopName ★",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = receiptTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "[ $copyLabel ]",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // مشخصات سفارش و تاریخ/ساعت
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "سفارش: ORD-${FarsiUtils.toFarsiDigits(order.id)}",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Text(
                text = FarsiUtils.formatCurrentTimeFarsi(),
                fontSize = 10.sp,
                color = Color(0xFF4B5563)
            )
        }

        // مشخصات مشتری
        if (order.customerName.isNotBlank() || order.customerPhone.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مشتری: ${order.customerName.ifBlank { "مشتری گرامی" }}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = FarsiUtils.toFarsiDigits(order.customerPhone),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
        }

        // نشانی
        if (order.address.isNotBlank()) {
            Text(
                text = "نشانی: ${order.address}",
                fontSize = 10.sp,
                color = Color(0xFF4B5563),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                textAlign = TextAlign.Start
            )
        }

        // کد قفسه انبار در صورت وجود
        val rack = if (order.cleanRackCode.isNotBlank()) order.cleanRackCode else order.rackCode
        if (rack.isNotBlank()) {
            Text(
                text = "قفسه انبار: $rack",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ════════════════════════════════════════════════════════════
        // جدول‌کشی استاندارد و مهندسی اقلام فاکتور (Compose Items Table)
        // ════════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFF374151)),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // سرستون جدول
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5E7EB))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رد",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(22.dp)
                    )
                    Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF9CA3AF)))
                    Text(
                        text = "شرح فرش / خدمات",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                    Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF9CA3AF)))
                    Text(
                        text = "متراژ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                    Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF9CA3AF)))
                    Text(
                        text = "مبلغ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(62.dp)
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = Color(0xFF374151))

                // سطرهای اقلام
                if (items.isNotEmpty()) {
                    items.forEachIndexed { index, item ->
                        val area = item.lengthMeter * item.widthMeter
                        val areaText = if (area > 0) "${FarsiUtils.toFarsiDigits(String.format(java.util.Locale.US, "%.0f", area))}م" else "-"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = FarsiUtils.toFarsiDigits((index + 1).toString()),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(22.dp)
                            )
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = item.carpetType.ifBlank { "فرش" },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                if (item.requestedServicesJson.isNotBlank() && item.requestedServicesJson != "[]") {
                                    val cleanServices = item.requestedServicesJson
                                        .replace("[", "").replace("]", "").replace("\"", "").replace(",", " + ")
                                        .trim()
                                    if (cleanServices.isNotBlank()) {
                                        Text(
                                            text = "+ $cleanServices",
                                            fontSize = 9.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }
                                if (item.barcodeTag.isNotBlank()) {
                                    Text(
                                        text = "تگ: ${item.barcodeTag}",
                                        fontSize = 8.5.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                if (item.defectsJson.isNotBlank() && item.defectsJson != "[]" && item.defectsJson != "بدون ایراد") {
                                    val cleanDef = item.defectsJson
                                        .replace("[", "").replace("]", "").replace("\"", "").replace(",", "، ")
                                        .trim()
                                    if (cleanDef.isNotBlank()) {
                                        Text(
                                            text = "ایراد: $cleanDef",
                                            fontSize = 8.5.sp,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))
                            Text(
                                text = areaText,
                                fontSize = 10.sp,
                                color = Color(0xFF374151),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))
                            Text(
                                text = FarsiUtils.formatPriceShort(item.totalPrice),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827),
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(62.dp)
                            )
                        }

                        if (index < items.size - 1) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))
                        }
                    }
                } else {
                    Text(
                        text = "هنوز فرشی برای این فاکتور ثبت نشده است",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ════════════════════════════════════════════════════════════
        // جدول تسویه مالی فاکتور
        // ════════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color(0xFF374151)),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "جمع کل خدمات:", fontSize = 10.5.sp, color = Color(0xFF374151))
                    Text(
                        text = "${FarsiUtils.formatPrice(order.totalAmount)} تومان",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                }

                if (order.discountAmount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "تخفیف ویژه:", fontSize = 10.5.sp, color = Color(0xFF059669))
                        Text(
                            text = "- ${FarsiUtils.formatPrice(order.discountAmount)} تومان",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }

                if (taxAmount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مالیات ارزش افزوده:", fontSize = 10.sp, color = Color(0xFF4B5563))
                        Text(
                            text = "+ ${FarsiUtils.formatPrice(taxAmount)} تومان",
                            fontSize = 10.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Color(0xFF374151))

                // سطر متمایز و درشت مبلغ قابل پرداخت
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مبلغ قابل پرداخت:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${FarsiUtils.formatPrice(finalPayable)} تومان",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // وضعیت تسویه
        Text(
            text = "وضعیت تسویه: $paymentMethodLabel",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1F2937),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ════════════════════════════════════════════════════════════
        // شماره تماس پشتیبانی از بخش تنظیمات + سامانه پیگیری
        // ════════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFF9FAFB),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = CleanPurpleAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تلفن مرکز پشتیبانی و هماهنگی: ${FarsiUtils.toFarsiDigits(supportPhone)}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "سامانه پیگیری آنلاین سفارش: panel.yaselectrical.ir",
                    fontSize = 9.5.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // امضای تحویل‌دهنده و مشتری
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "امضای تحویل‌دهنده", fontSize = 9.5.sp, color = Color(0xFF4B5563))
                Text(text = ".......................", fontSize = 9.sp, color = Color(0xFF9CA3AF))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "امضای مشتری", fontSize = 9.5.sp, color = Color(0xFF4B5563))
                Text(text = ".......................", fontSize = 9.sp, color = Color(0xFF9CA3AF))
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "اجناس صحیح و سالم تحویل گردید.",
            fontSize = 9.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}


