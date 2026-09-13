package com.example.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

data class BluetoothPrinterDevice(
    val name: String,
    val address: String,
    val isConnected: Boolean = false
)

object PrinterManager {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null

    private val _connectedPrinter = MutableStateFlow<BluetoothPrinterDevice?>(null)
    val connectedPrinter: StateFlow<BluetoothPrinterDevice?> = _connectedPrinter

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting

    private val _availablePrinters = MutableStateFlow<List<BluetoothPrinterDevice>>(emptyList())
    val availablePrinters: StateFlow<List<BluetoothPrinterDevice>> = _availablePrinters

    /** فقط دستگاه‌های بلوتوث واقعیِ Paired (جفت‌شده با گوشی) را برمی‌گرداند.
     * قبلاً اگر هیچ پرینتری با اسم Thermal/POS/MTP/BTP پیدا نمی‌شد، سه
     * دستگاه ساختگی با آدرس MAC جعلی به لیست اضافه می‌شد که راننده می‌توانست
     * به آن‌ها «وصل» شود بدون این‌که واقعاً پرینتری وجود داشته باشد. */
    fun scanPrinters(context: Context) {
        val list = mutableListOf<BluetoothPrinterDevice>()
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            @SuppressLint("MissingPermission")
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                pairedDevices?.forEach { device ->
                    @SuppressLint("MissingPermission")
                    list.add(BluetoothPrinterDevice(device.name ?: "پرینتر بلوتوثی", device.address))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _availablePrinters.value = list
    }

    /** به یک پرینتر بلوتوثی واقعی وصل می‌شود. اگر بلوتوث خاموش باشد، آدرس
     * نامعتبر باشد، یا اتصال RFCOMM رد شود، false برمی‌گرداند — دیگر هیچ
     * حالت fallbackِ «همیشه موفق» وجود ندارد؛ قبلاً حتی وقتی اتصال واقعی
     * شکست می‌خورد، بعد از یک تأخیر مصنوعی true برگردانده می‌شد و راننده
     * فکر می‌کرد پرینتر وصل شده، در حالی که هیچ سوکتی باز نشده بود. */
    @SuppressLint("MissingPermission")
    suspend fun connectPrinter(device: BluetoothPrinterDevice): Boolean {
        return withContext(Dispatchers.IO) {
            disconnectPrinter()
            try {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    return@withContext false
                }
                if (!BluetoothAdapter.checkBluetoothAddress(device.address)) {
                    return@withContext false
                }
                val realDevice = bluetoothAdapter.getRemoteDevice(device.address)
                bluetoothAdapter.cancelDiscovery()
                val socket = realDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect()
                bluetoothSocket = socket
                _connectedPrinter.value = device.copy(isConnected = true)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                bluetoothSocket = null
                _connectedPrinter.value = null
                false
            }
        }
    }

    fun disconnectPrinter() {
        try {
            bluetoothSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bluetoothSocket = null
        _connectedPrinter.value = null
    }

    const val RECEIPT_WIDTH = 32
    const val BORDER_DOUBLE = "╔══════════════════════════════╗"
    const val BORDER_DOUBLE_BOTTOM = "╚══════════════════════════════╝"
    const val DIVIDER_DOUBLE = "================================"
    const val DIVIDER_SINGLE = "--------------------------------"
    const val DIVIDER_DASH   = "- - - - - - - - - - - - - - - -"
    const val DIVIDER_CUT    = "- - - - - ✂ برش کاغذ ✂ - - - - -"

    var defaultTwoCopies: Boolean = true

    fun centerText(text: String, width: Int = RECEIPT_WIDTH): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.lines().joinToString("\n") { line ->
            val l = line.trim()
            if (l.length >= width) {
                l.take(width)
            } else {
                val totalPad = width - l.length
                val left = totalPad / 2
                " ".repeat(left) + l
            }
        }
    }

    fun padCell(text: String, width: Int, alignRight: Boolean = false): String {
        val clean = text.replace("\u200C", " ").trim()
        val len = clean.length
        return if (len >= width) {
            clean.take(width)
        } else {
            val spaces = " ".repeat(width - len)
            if (alignRight) spaces + clean else clean + spaces
        }
    }

    private fun buildItemsTable(
        orderWithItems: com.example.data.local.model.OrderWithItems?,
        carpetItemsSummary: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append(DIVIDER_SINGLE).append("\n")
        sb.append("رد  شرح اقلام و خدمت   مترا   مبلغ\n")
        sb.append(DIVIDER_SINGLE).append("\n")

        val items = orderWithItems?.items
        if (!items.isNullOrEmpty()) {
            items.forEachIndexed { index, item ->
                val idx = FarsiUtils.toFarsiDigits((index + 1).toString())
                val typeName = item.carpetType.ifBlank { "فرش" }
                val area = item.lengthMeter * item.widthMeter
                val areaStr = if (area > 0) "${FarsiUtils.toFarsiDigits(String.format(java.util.Locale.US, "%.0f", area))}م" else "-"
                val priceStr = FarsiUtils.formatPriceShort(item.totalPrice)

                val desc = if (typeName.length > 13) typeName.take(13) else typeName
                val line = String.format("%-2s %-14s %-4s %7s", idx, desc, areaStr, priceStr)
                sb.append(line).append("\n")

                if (item.requestedServicesJson.isNotBlank() && item.requestedServicesJson != "[]") {
                    val cleanServices = item.requestedServicesJson
                        .replace("[", "").replace("]", "").replace("\"", "").replace(",", " + ")
                        .trim()
                    if (cleanServices.isNotBlank()) {
                        sb.append("   + ${cleanServices.take(25)}\n")
                    }
                }
                if (item.barcodeTag.isNotBlank()) {
                    sb.append("   تگ: ${item.barcodeTag}\n")
                }
                if (item.defectsJson.isNotBlank() && item.defectsJson != "[]" && item.defectsJson != "بدون ایراد") {
                    val cleanDefects = item.defectsJson
                        .replace("[", "").replace("]", "").replace("\"", "").replace(",", "، ")
                        .trim()
                    if (cleanDefects.isNotBlank()) {
                        sb.append("   ایراد: ${cleanDefects.take(22)}\n")
                    }
                }
            }
        } else if (carpetItemsSummary.isNotEmpty()) {
            carpetItemsSummary.forEachIndexed { index, itemStr ->
                val idx = FarsiUtils.toFarsiDigits((index + 1).toString())
                val parts = itemStr.split("-")
                val title = parts.getOrNull(0)?.trim() ?: itemStr
                val price = parts.lastOrNull()?.replace("تومان", "")?.trim() ?: ""
                val line = String.format("%-2s %-16s %9s", idx, title.take(16), price.take(9))
                sb.append(line).append("\n")
            }
        } else {
            sb.append(centerText("هنوز قلمی ثبت نشده است")).append("\n")
        }
        sb.append(DIVIDER_SINGLE).append("\n")
        return sb.toString()
    }

    private fun buildTotalsTable(
        totalPrice: Long,
        discount: Long,
        taxAmount: Long,
        netPayable: Long
    ): String {
        val sb = StringBuilder()
        sb.append("جمع کل خدمات:        ").append(padCell(FarsiUtils.formatPrice(totalPrice), 11, true)).append("\n")
        if (discount > 0) {
            sb.append("تخفیف ویژه:          ").append(padCell("-" + FarsiUtils.formatPrice(discount), 11, true)).append("\n")
        }
        if (taxAmount > 0) {
            sb.append("مالیات ارزش افزوده:  ").append(padCell("+" + FarsiUtils.formatPrice(taxAmount), 11, true)).append("\n")
        }
        sb.append(DIVIDER_DOUBLE).append("\n")
        sb.append("مبلغ نهایی قابل پرداخت:\n")
        sb.append(centerText(FarsiUtils.formatPrice(netPayable))).append("\n")
        sb.append(DIVIDER_DOUBLE).append("\n")
        return sb.toString()
    }

    suspend fun printReceipt(
        title: String,
        orderId: String,
        customerName: String,
        customerPhone: String,
        address: String,
        carpetDetails: String,
        totalPrice: Long,
        discount: Long,
        finalPrice: Long,
        paymentStatus: String,
        rackCode: String,
        taxAmount: Long = 0L,
        orderWithItems: com.example.data.local.model.OrderWithItems? = null,
        includeTwoCopies: Boolean = defaultTwoCopies
    ): Boolean {
        return withContext(Dispatchers.IO) {
            _isPrinting.value = true
            var success = false
            try {
                val receiptText = buildEscPosThermalReceiptText(
                    title = title,
                    orderId = orderId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    address = address,
                    carpetItemsSummary = carpetDetails.split("\n").filter { it.isNotBlank() },
                    totalPrice = totalPrice,
                    discount = discount,
                    netPayable = finalPrice,
                    paymentMethod = paymentStatus,
                    rackCode = rackCode,
                    includeTwoCopies = includeTwoCopies,
                    taxAmount = taxAmount,
                    orderWithItems = orderWithItems
                )

                val socket = bluetoothSocket
                if (socket != null && socket.isConnected) {
                    val outputStream: OutputStream = socket.outputStream
                    val initPrinter = byteArrayOf(0x1B, 0x40) // ESC @ (Init)
                    val selectCodePage = byteArrayOf(0x1B, 0x74, 0x16) // UTF-8 code page
                    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01) // ESC a 1 (Center alignment)
                    val boldOn = byteArrayOf(0x1B, 0x45, 0x01, 0x1B, 0x47, 0x01) // Bold & Double strike ON
                    val boldOff = byteArrayOf(0x1B, 0x45, 0x00, 0x1B, 0x47, 0x00) // Bold OFF
                    val feedAndCut = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // Cut paper

                    outputStream.write(initPrinter)
                    outputStream.write(selectCodePage)
                    outputStream.write(alignCenter)
                    outputStream.write(boldOn)
                    outputStream.write(receiptText.toByteArray(Charsets.UTF_8))
                    outputStream.write(boldOff)
                    outputStream.write(byteArrayOf(0x0A, 0x0A)) // فقط ۲ خط فاصله جهت صرفه‌جویی کاغذ
                    outputStream.write(feedAndCut)
                    outputStream.flush()
                    success = true
                } else {
                    success = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try { bluetoothSocket?.close() } catch (_: Exception) {}
                bluetoothSocket = null
                _connectedPrinter.value = _connectedPrinter.value?.copy(isConnected = false)
            } finally {
                _isPrinting.value = false
            }
            success
        }
    }

    suspend fun printDailySettlementReport(
        driverName: String,
        date: String,
        settledCount: Int,
        totalCash: Long,
        totalPos: Long,
        totalCardToCard: Long,
        totalAmount: Long,
        orderIds: List<String>
    ): Boolean {
        return withContext(Dispatchers.IO) {
            _isPrinting.value = true
            var success = false
            try {
                val sb = StringBuilder()
                val workshop = com.example.data.WorkshopNameHolder.current.ifBlank { "قالیشویی تخصصی زمرد" }
                val shortWorkshop = if (workshop.length > 26) workshop.take(26) else workshop

                sb.append(BORDER_DOUBLE).append("\n")
                sb.append("║").append(padCell(centerText("★ $shortWorkshop ★", 30), 30, false)).append("║\n")
                sb.append(BORDER_DOUBLE_BOTTOM).append("\n")
                sb.append(centerText("گزارش تسویه حساب روزانه سفیر")).append("\n")
                sb.append(DIVIDER_SINGLE).append("\n")
                sb.append(centerText("سفیر: $driverName | $date")).append("\n")
                sb.append(centerText("زمان چاپ: ${FarsiUtils.formatCurrentTimeFarsi()}")).append("\n")
                sb.append(centerText("تعداد سفارش‌های تحویلی: ${FarsiUtils.toFarsiDigits(settledCount.toString())} فاکتور")).append("\n")

                // جدول مبالغ تسویه
                sb.append("┌───────────────────┬──────────┐\n")
                sb.append("│").append(padCell("دریافتی نقدی:", 19, false))
                    .append("│").append(padCell(FarsiUtils.formatPriceShort(totalCash), 10, true)).append("│\n")
                sb.append("│").append(padCell("کارتخوان سیار POS:", 19, false))
                    .append("│").append(padCell(FarsiUtils.formatPriceShort(totalPos), 10, true)).append("│\n")
                if (totalCardToCard > 0) {
                    sb.append("│").append(padCell("کارت به کارت:", 19, false))
                        .append("│").append(padCell(FarsiUtils.formatPriceShort(totalCardToCard), 10, true)).append("│\n")
                }
                sb.append("├───────────────────┼──────────┤\n")
                sb.append("│").append(padCell("جمع کل تسویه:", 19, false))
                    .append("│").append(padCell(FarsiUtils.formatPriceShort(totalAmount) + " ت", 10, true)).append("│\n")
                sb.append("└───────────────────┴──────────┘\n")

                sb.append(centerText("لیست سفارش‌های تسویه‌شده:")).append("\n")
                orderIds.chunked(2).forEach { chunk ->
                    val line = chunk.joinToString(" | ") { id ->
                        "ORD-${FarsiUtils.toFarsiDigits(id)}"
                    }
                    sb.append(centerText(line)).append("\n")
                }
                sb.append(DIVIDER_SINGLE).append("\n")
                sb.append("امضای سفیر           امضای صندوق\n")
                sb.append("..............       ..............\n")
                sb.append(centerText("سامانه یکپارچه panel.yaselectrical.ir")).append("\n")
                sb.append(BORDER_DOUBLE_BOTTOM).append("\n")

                val socket = bluetoothSocket
                if (socket != null && socket.isConnected) {
                    val outputStream = socket.outputStream
                    val initPrinter = byteArrayOf(0x1B, 0x40)
                    val selectCodePage = byteArrayOf(0x1B, 0x74, 0x16)
                    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                    val boldOn = byteArrayOf(0x1B, 0x45, 0x01, 0x1B, 0x47, 0x01)
                    val boldOff = byteArrayOf(0x1B, 0x45, 0x00, 0x1B, 0x47, 0x00)
                    val feedAndCut = byteArrayOf(0x1D, 0x56, 0x42, 0x00)

                    outputStream.write(initPrinter)
                    outputStream.write(selectCodePage)
                    outputStream.write(alignCenter)
                    outputStream.write(boldOn)
                    outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
                    outputStream.write(boldOff)
                    outputStream.write(byteArrayOf(0x0A, 0x0A))
                    outputStream.write(feedAndCut)
                    outputStream.flush()
                    success = true
                } else {
                    success = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPrinting.value = false
            }
            success
        }
    }

    suspend fun printRawText(text: String): Boolean {
        return withContext(Dispatchers.IO) {
            _isPrinting.value = true
            var success = false
            try {
                val socket = bluetoothSocket
                if (socket != null && socket.isConnected) {
                    val outputStream = socket.outputStream
                    val initPrinter = byteArrayOf(0x1B, 0x40)
                    val selectCodePage = byteArrayOf(0x1B, 0x74, 0x16)
                    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
                    val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
                    val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
                    val feedAndCut = byteArrayOf(0x1D, 0x56, 0x42, 0x00)

                    outputStream.write(initPrinter)
                    outputStream.write(selectCodePage)
                    outputStream.write(alignCenter)
                    outputStream.write(boldOn)
                    outputStream.write(text.toByteArray(Charsets.UTF_8))
                    outputStream.write(boldOff)
                    outputStream.write(byteArrayOf(0x0A, 0x0A))
                    outputStream.write(feedAndCut)
                    outputStream.flush()
                    success = true
                } else {
                    success = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPrinting.value = false
            }
            success
        }
    }

    fun buildEscPosThermalReceiptText(
        title: String,
        orderId: String,
        customerName: String,
        customerPhone: String,
        address: String,
        carpetItemsSummary: List<String>,
        totalPrice: Long,
        discount: Long,
        netPayable: Long,
        paymentMethod: String,
        rackCode: String,
        includeTwoCopies: Boolean = defaultTwoCopies,
        taxAmount: Long = 0L,
        orderWithItems: com.example.data.local.model.OrderWithItems? = null,
        singleCopyTitle: String = "نسخه تک برگ"
    ): String {
        fun buildSingleCopy(copyTitle: String): String {
            val sb = StringBuilder()
            val workshop = com.example.data.WorkshopNameHolder.current.ifBlank { "قالیشویی تخصصی زمرد" }
            val shortWorkshop = if (workshop.length > 26) workshop.take(26) else workshop

            // سربرگ درشت، بولد و گرافیکی با کادر دولایه
            sb.append(BORDER_DOUBLE).append("\n")
            val paddedWorkshop = centerText("★ $shortWorkshop ★", 30)
            sb.append("║").append(padCell(paddedWorkshop, 30, false)).append("║\n")
            sb.append(BORDER_DOUBLE_BOTTOM).append("\n")

            // عنوان و نسخه فاکتور
            sb.append(centerText(title.ifBlank { "فاکتور خدمات قالیشویی" })).append("\n")
            sb.append(centerText("[ $copyTitle ]")).append("\n")
            sb.append(DIVIDER_SINGLE).append("\n")

            // مشخصات فشرده شماره سفارش و زمان در یک سطر
            val orderNum = "ORD-${FarsiUtils.toFarsiDigits(orderId)}"
            val currentTime = FarsiUtils.formatCurrentTimeFarsi()
            sb.append(centerText("$orderNum | $currentTime")).append("\n")

            // مشتری و شماره تماس
            if (customerName.isNotBlank() || customerPhone.isNotBlank()) {
                val cName = customerName.take(15).ifBlank { "مشتری گرامی" }
                val cPhone = FarsiUtils.toFarsiDigits(customerPhone)
                sb.append(centerText("$cName | $cPhone")).append("\n")
            }

            // نشانی مشتری (فشرده)
            if (address.isNotBlank()) {
                val cleanAddr = address.replace("\n", " ").trim()
                cleanAddr.chunked(30).forEach { chunk ->
                    sb.append(centerText(chunk)).append("\n")
                }
            }

            // کد قفسه انبار
            if (rackCode.isNotBlank()) {
                sb.append(centerText("قفسه انبار: $rackCode")).append("\n")
            }

            // جدول شکیل و بهینه اقلام فاکتور
            sb.append(buildItemsTable(orderWithItems, carpetItemsSummary))

            // جدول مجموع مبالغ و تسویه
            sb.append(buildTotalsTable(totalPrice, discount, taxAmount, netPayable))

            val cleanPayment = when {
                paymentMethod.contains("unpaid", ignoreCase = true) || paymentMethod.isBlank() -> "در انتظار پرداخت در محل"
                paymentMethod.contains("cash", ignoreCase = true) || paymentMethod.contains("نقد") -> "تسویه نقدی"
                paymentMethod.contains("pos", ignoreCase = true) || paymentMethod.contains("پوز") || paymentMethod.contains("کارتخوان") -> "تسویه با دستگاه کارتخوان (POS)"
                paymentMethod.contains("card", ignoreCase = true) || paymentMethod.contains("کارت") -> "تسویه کارت به کارت"
                paymentMethod.contains("online", ignoreCase = true) || paymentMethod.contains("آنلاین") -> "پرداخت آنلاین"
                else -> paymentMethod
            }
            sb.append(centerText("وضعیت تسویه: $cleanPayment")).append("\n")

            // شماره تماس مرکز پشتیبانی (تنظیمات)
            val supportPhone = com.example.data.WorkshopNameHolder.supportPhone.trim()
            if (supportPhone.isNotBlank()) {
                sb.append(centerText("تلفن پشتیبانی: ${FarsiUtils.toFarsiDigits(supportPhone)}")).append("\n")
            } else {
                sb.append(centerText("پشتیبانی: panel.yaselectrical.ir")).append("\n")
            }
            sb.append(centerText("سامانه پیگیری: panel.yaselectrical.ir")).append("\n")
            sb.append(DIVIDER_DASH).append("\n")

            // امضا و تضمین بهینه (بدون خطوط خالی اضافه)
            sb.append("امضای تحویل‌دهنده      امضای مشتری\n")
            sb.append("..............       ..............\n")
            sb.append(centerText("اجناس صحیح و سالم تحویل گردید.")).append("\n")
            sb.append(BORDER_DOUBLE_BOTTOM).append("\n")

            return sb.toString()
        }

        return if (includeTwoCopies) {
            buildSingleCopy("نسخه مشتری") +
                    "\n" +
                    DIVIDER_CUT + "\n\n" +
                    buildSingleCopy("نسخه راننده و بایگانی")
        } else {
            buildSingleCopy(singleCopyTitle)
        }
    }
}
