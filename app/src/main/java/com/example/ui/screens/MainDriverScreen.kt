package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.example.data.local.model.OrderWithItems
import com.example.data.model.AppNotification
import com.example.data.model.NotificationCategory
import com.example.ui.components.BarcodeScannerModal
import com.example.ui.components.PrinterDeviceDialog
import com.example.ui.components.RackAssignmentDialog
import com.example.ui.components.SettlementDialog
import com.example.ui.components.SyncQueueDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.DriverViewModel
import com.example.utils.FarsiUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainDriverScreen(viewModel: DriverViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()
    val pendingQueueItems by viewModel.pendingQueueItems.collectAsState()
    val pendingQueueCount by viewModel.pendingQueueCount.collectAsState()

    val orders by viewModel.ordersList.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isGpsActive by viewModel.isGpsActive.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncToastMessage.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val recentGpsLogs by viewModel.gpsLogs.collectAsState()
    val initiatedSettlementOrderIds by viewModel.initiatedSettlementOrderIds.collectAsState()
    val selectedOrderId by viewModel.selectedOrderId.collectAsState()
    val supportPhoneNumber by viewModel.supportPhoneNumber.collectAsState()

    val showScannerDialog by viewModel.showScannerDialog.collectAsState()
    val scanStage by viewModel.scanStage.collectAsState()

    val connectedPrinter by viewModel.connectedPrinter.collectAsState()
    val availablePrinters by viewModel.availablePrinters.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()
    val driverApiKey by viewModel.driverApiKey.collectAsState()
    val isTestingConnection by viewModel.isTestingConnection.collectAsState()
    val connectionTestResult by viewModel.connectionTestResult.collectAsState()
    val backupInfo by viewModel.backupInfo.collectAsState()
    val isBgServiceRunning by viewModel.isBackgroundServiceRunning.collectAsState()
    val bgLastSyncTime by viewModel.backgroundLastSyncTime.collectAsState()
    val tariffsResult by viewModel.tariffsState.collectAsState()
    val workshopName by viewModel.workshopName.collectAsState()

    val notificationsList by viewModel.notificationsList.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val isBellGlowing by viewModel.isBellGlowing.collectAsState()

    val infiniteGlowTransition = rememberInfiniteTransition(label = "bell_glow_anim")
    val bellGlowAlpha by infiniteGlowTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val bellGlowScale by infiniteGlowTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    var showPrinterDialog by remember { mutableStateOf(false) }
    var showSyncQueueDialog by remember { mutableStateOf(false) }
    var showMenuBottomSheet by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var rackDialogOrderId by remember { mutableStateOf<String?>(null) }
    var settlementOrder by remember { mutableStateOf<OrderWithItems?>(null) }

    // از اندروید ۱۲ (API 31) به بعد، BLUETOOTH_CONNECT/BLUETOOTH_SCAN باید
    // در زمان اجرا (Runtime) درخواست بشن، صرفاً تعریف‌شون توی Manifest کافی
    // نیست. بدون این درخواست، اسکن/اتصال به پرینتر واقعی روی این نسخه‌ها
    // همیشه با SecurityException شکست می‌خورد (که الان دیگه واقعی گزارش
    // می‌شه، پس این مجوز باید درست گرفته بشه، وگرنه دکمه‌ی پرینتر عملاً
    // روی گوشی‌های جدید کار نمی‌کنه).
    fun hasBluetoothRuntimePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            showMenuBottomSheet = false
            viewModel.scanBluetoothPrinters(context)
            showPrinterDialog = true
        } else {
            Toast.makeText(context, "برای مدیریت پرینتر بلوتوثی، مجوز بلوتوث لازم است.", Toast.LENGTH_LONG).show()
        }
    }

    fun openPrinterDialogWithPermissionCheck() {
        if (hasBluetoothRuntimePermission()) {
            showMenuBottomSheet = false
            viewModel.scanBluetoothPrinters(context)
            showPrinterDialog = true
        } else {
            bluetoothPermissionLauncher.launch(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            )
        }
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearToastMessage()
        }
    }

    if (showPrinterDialog) {
        PrinterDeviceDialog(
            connectedPrinter = connectedPrinter,
            availablePrinters = availablePrinters,
            onScan = { viewModel.scanBluetoothPrinters(context) },
            onConnect = { viewModel.connectPrinter(it) },
            onDisconnect = { /* handled in manager */ },
            onDismiss = { showPrinterDialog = false }
        )
    }

    if (showSyncQueueDialog) {
        SyncQueueDialog(
            isOnline = isOnline,
            pendingQueue = pendingQueueItems,
            isSyncing = isSyncing,
            onDismiss = { showSyncQueueDialog = false },
            onSyncNow = { viewModel.syncWithWebPanel() }
        )
    }

    // Quick Notifications Dialog (پیام‌ها و رویدادهای مهم سیستم)
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notificationsList.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                viewModel.clearNotificationHistory()
                                Toast.makeText(context, "🗑️ تاریخچه اعلان‌ها پاک شد", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = CleanRedError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پاک‌سازی تاریخچه", color = CleanRedError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = { showNotificationsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("بستن", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CleanGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پیام‌ها و اعلان‌های مهم",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleanLightOnSurface
                        )
                    }
                    if (notificationsList.isNotEmpty()) {
                        Surface(
                            color = CleanGreenPrimaryLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${FarsiUtils.toFarsiDigits(notificationsList.size.toString())} مورد",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CleanGreenPrimaryDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = CleanGreenPrimaryDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "راهنما: با نگه داشتن انگشت روی زنگوله تاریخچه پاک می‌شود.",
                                fontSize = 10.5.sp,
                                color = CleanLightOnSurfaceMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (notificationsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = CleanLightOnSurfaceMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تاریخچه اعلان‌ها خالی است",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanLightOnSurfaceMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "پیام‌های جدید سرور، پشتیبانی و ماموریت‌ها اینجا قرار می‌گیرند.",
                                    fontSize = 11.sp,
                                    color = CleanLightOnSurfaceMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notificationsList, key = { it.id }) { notif ->
                                NotificationItemCard(notif)
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Menu Bottom Sheet for Secondary Navigation & Settings
    if (showMenuBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CleanGreenPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = CleanGreenPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("منوی دسترسی سریع سفیر", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            // قبلاً اینجا "قالیشویی $workshopName" نوشته می‌شد؛ چون
                            // نام برند در تنظیمات پنل معمولاً از قبل کلمه‌ی
                            // "قالیشویی" را دارد (مثلاً "قالیشویی صبا")، این پیشوند
                            // باعث تکرار می‌شد ("قالیشویی قالیشویی صبا"). حذف شد —
                            // نام دقیقاً همان‌طور که در پنل ثبت شده نمایش داده می‌شود.
                            Text("$workshopName • نسخه ۳.۲", fontSize = 11.sp, color = CleanLightOnSurfaceMuted)
                        }
                    }
                    IconButton(onClick = { showMenuBottomSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CleanLightOutline)
                Spacer(modifier = Modifier.height(12.dp))

                // Menu items
                NavigationDrawerItem(
                    icon = {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = CleanGreenPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CleanGreenPrimary)
                        }
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("بروزرسانی دستی سفارشات از پنل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            if (isSyncing) {
                                Text("در حال دریافت...", fontSize = 11.sp, color = CleanGreenPrimary)
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        showMenuBottomSheet = false
                        viewModel.syncWithWebPanel(silent = false)
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.GpsFixed, contentDescription = null, tint = CleanGreenPrimary) },
                    label = { Text("نقشه و ردیابی لحظه‌ای GPS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    selected = activeTab == 5,
                    onClick = {
                        viewModel.setActiveTab(5)
                        showMenuBottomSheet = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CleanGreenPrimaryLight,
                        unselectedContainerColor = Color.Transparent
                    )
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Print, contentDescription = null, tint = CleanGreenPrimary) },
                    label = { Text("مدیریت چاپگر بلوتوثی فاکتور", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        openPrinterDialogWithPermissionCheck()
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Sync, contentDescription = null, tint = CleanGreenPrimary) },
                    label = { Text("صف همگام‌سازی آفلاین (${FarsiUtils.toFarsiDigits(pendingQueueCount.toString())})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        showMenuBottomSheet = false
                        showSyncQueueDialog = true
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = CleanGreenPrimary) },
                    label = { Text("تنظیمات سرور و برنامه", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    selected = activeTab == 6,
                    onClick = {
                        viewModel.setActiveTab(6)
                        showMenuBottomSheet = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CleanGreenPrimaryLight,
                        unselectedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CleanLightOutline)
                Spacer(modifier = Modifier.height(12.dp))

                // Logout
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CleanRedContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenuBottomSheet = false
                            viewModel.logoutDriver()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = CleanRedError, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("خروج از حساب کاربری سفیر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CleanRedError)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    val activeRackOrderId = rackDialogOrderId
    if (activeRackOrderId != null) {
        val currentOrder = orders.find { it.order.id == activeRackOrderId }
        RackAssignmentDialog(
            orderId = activeRackOrderId,
            currentRackCode = currentOrder?.order?.rackCode ?: "",
            onDismiss = { rackDialogOrderId = null },
            onConfirm = { rackCode ->
                viewModel.assignRackCode(activeRackOrderId, rackCode)
                rackDialogOrderId = null
            }
        )
    }

    val activeSettlementOrder = settlementOrder
    if (activeSettlementOrder != null) {
        SettlementDialog(
            orderWithItems = activeSettlementOrder,
            onDismiss = { settlementOrder = null },
            onConfirmSettlement = { paid, discount, method, print ->
                viewModel.settlePayment(activeSettlementOrder.order.id, paid, discount, method)
                if (print) {
                    viewModel.printOrderReceipt(
                        title = "رسید تسویه حساب و تحویل فرش",
                        orderWithItems = activeSettlementOrder,
                        paymentMethod = method,
                        discountOverride = discount,
                        paidOverride = paid
                    )
                }
                settlementOrder = null
            }
        )
    }

    if (showScannerDialog) {
        BarcodeScannerModal(
            expectedOrder = selectedOrder,
            allOrders = orders,
            scanStage = scanStage,
            onDismiss = { viewModel.closeScanner() },
            onConfirmVerification = { success -> viewModel.handleScanSuccess(success) },
            onReportMismatchToDispatch = { alertText -> viewModel.reportScanMismatchToDispatch(alertText) }
        )
    }

    val pendingPickupCount = orders.count {
        val isPickup = it.order.orderType.equals("COLLECTION", ignoreCase = true) ||
                       it.order.orderType.equals("PICKUP", ignoreCase = true) ||
                       it.order.orderType.isBlank()
        isPickup && (it.order.status == "ASSIGNED" || it.order.status == "pickup_assigned")
    }
    val pendingWarehouseCount = orders.count {
        it.order.status == "COLLECTED_IN_INSPECTION"
    }
    val pendingDeliveryCount = orders.count {
        val isDelivery = it.order.orderType.equals("DELIVERY", ignoreCase = true) ||
                         it.order.status == "READY_FOR_DELIVERY"
        isDelivery &&
                it.order.status != "DELIVERED_SETTLED" &&
                it.order.status != "OFFICE_SETTLED" &&
                it.order.status != "RETURNED_TO_CLEAN_WAREHOUSE" &&
                it.order.status != "DELIVERED_TO_WORKSHOP" &&
                it.order.status != "COLLECTED_IN_INSPECTION" &&
                it.order.status != "WASHING"
    }
    val pendingSettlementCount = orders.count {
        val isDelivery = it.order.orderType.equals("DELIVERY", ignoreCase = true) ||
                         it.order.status == "READY_FOR_DELIVERY"
        isDelivery &&
                initiatedSettlementOrderIds.contains(it.order.id) &&
                it.order.status != "DELIVERED_SETTLED" &&
                it.order.status != "OFFICE_SETTLED" &&
                it.order.status != "RETURNED_TO_CLEAN_WAREHOUSE" &&
                it.order.status != "DELIVERED_TO_WORKSHOP" &&
                it.order.status != "COLLECTED_IN_INSPECTION" &&
                it.order.status != "WASHING"
    }

    ZomorrodDriverTheme(darkTheme = isDarkMode) {
        if (!isLoggedIn) {
            DriverLoginScreen(
                onSendOtp = { phone -> viewModel.requestOtp(phone) },
                onVerifyOtp = { phone, code -> viewModel.verifyOtp(phone, code) },
                onResetOtp = { viewModel.resetOtpState() },
                otpSent = otpSent,
                isLoading = authLoading,
                errorMessage = authError,
                generatedOtpHint = generatedOtp,
                serverUrl = serverUrl,
                driverApiKey = driverApiKey,
                isTestingConnection = isTestingConnection,
                connectionTestResult = connectionTestResult,
                onTestConnection = { url, key -> viewModel.testServerConnection(url, key) },
                onSaveServerConfig = { url, key -> viewModel.updateServerConfig(url, key) }
            )
        } else {
            Scaffold(
                containerColor = DarkCanvasStart,
                topBar = {
                    // Curved Rich Emerald Green Top Header (Modern Gradient)
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                        color = Color.Transparent,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = ModernHeaderGradient,
                                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Right Side (in RTL - Leading): Menu Hamburger in rounded box
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { showMenuBottomSheet = true }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Menu,
                                                contentDescription = "منوی اصلی",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    // Screen Title and Connection Status
                                    val currentScreenTitle = when (activeTab) {
                                        0 -> "مسیر تحویل مشتریان"
                                        1 -> "جمع‌آوری و ثبت فاکتور"
                                        2 -> "تحویل به انبار قالیشویی"
                                        3 -> "تسویه حساب و فاکتورها"
                                        4 -> "پشتیبانی و گفتگو"
                                        5 -> "موقعیت مکانی GPS"
                                        6 -> "تنظیمات نرم‌افزار"
                                        99 -> "صدور پیش‌فاکتور دریافت"
                                        else -> workshopName
                                    }

                                    Column {
                                        Text(
                                            text = currentScreenTitle,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isOnline) Color(0xFF34D399) else Color(0xFFFBBF24))
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isOnline) "سفیر $workshopName • متصل به سرور" else "حالت آفلاین ناوگان",
                                                fontSize = 11.sp,
                                                color = Color(0xFFD1FAE5),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Left Side (in RTL - Trailing): Scanner & Notification Bell with Red Dot
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Quick QR / Barcode Scanner Icon
                                    IconButton(
                                        onClick = { viewModel.openScanner(com.example.data.model.ScanStage.DELIVERY) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "اسکن بارکد / QR کد",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Notification Bell with Red Dot, Glow Animation and Long-Press to Clear
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .then(
                                                if (isBellGlowing) {
                                                    Modifier.drawBehind {
                                                        drawCircle(
                                                            color = Color(0xFFFFD54F).copy(alpha = bellGlowAlpha * 0.45f),
                                                            radius = (size.maxDimension / 2f) * bellGlowScale
                                                        )
                                                    }
                                                } else Modifier
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isBellGlowing) {
                                                    Color(0xFFFFB300).copy(alpha = 0.35f + bellGlowAlpha * 0.25f)
                                                } else {
                                                    Color.White.copy(alpha = 0.15f)
                                                }
                                            )
                                            .border(
                                                width = if (isBellGlowing) 2.dp else 1.dp,
                                                color = if (isBellGlowing) Color(0xFFFFE082).copy(alpha = bellGlowAlpha) else Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    viewModel.markNotificationsAsRead()
                                                    showNotificationsDialog = true
                                                },
                                                onLongClick = {
                                                    try {
                                                        val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                                        } else {
                                                            @Suppress("DEPRECATION")
                                                            vibrator?.vibrate(100)
                                                        }
                                                    } catch (_: Exception) {}
                                                    viewModel.clearNotificationHistory()
                                                    Toast.makeText(context, "🗑️ تاریخچه اعلان‌ها با موفقیت پاک شد", Toast.LENGTH_SHORT).show()
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "اعلان‌ها (نگه داشتن جهت پاک‌سازی تاریخچه)",
                                            tint = if (isBellGlowing) Color(0xFFFFEA00) else Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        // Red dot indicator or glowing badge
                                        if (unreadNotificationsCount > 0 || isBellGlowing) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 7.dp, end = 7.dp)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isBellGlowing) Color(0xFFFFD54F) else CleanRedError)
                                                    .border(1.dp, Color.White, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                },
                bottomBar = {
                    // Unified Premium Bottom Navigation Bar (Modern Dark Gradient)
                    Surface(
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = DarkSurfaceBase,
                        shadowElevation = 12.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. تحویل (Delivery)
                            UnifiedNavItem(
                                title = "تحویل",
                                icon = Icons.Default.LocalShipping,
                                isSelected = activeTab == 0,
                                badgeCount = pendingDeliveryCount,
                                badgeColor = CleanGreenPrimary,
                                onClick = { viewModel.setActiveTab(0) }
                            )

                            // 2. جمع‌آوری (Collection)
                            UnifiedNavItem(
                                title = "جمع‌آوری",
                                icon = Icons.Default.EditNote,
                                isSelected = activeTab == 1 || activeTab == 99,
                                badgeCount = pendingPickupCount,
                                badgeColor = CleanOrangeAccent,
                                onClick = { viewModel.setActiveTab(1) }
                            )

                            // 3. انبار (Warehouse)
                            UnifiedNavItem(
                                title = "انبار",
                                icon = Icons.Default.Warehouse,
                                isSelected = activeTab == 2,
                                badgeCount = pendingWarehouseCount,
                                badgeColor = CleanGreenAccent,
                                onClick = { viewModel.setActiveTab(2) }
                            )

                            // 4. تسویه (Settlement)
                            UnifiedNavItem(
                                title = "تسویه",
                                icon = Icons.Default.AccountBalanceWallet,
                                isSelected = activeTab == 3,
                                badgeCount = pendingSettlementCount,
                                badgeColor = CleanGreenPrimary,
                                onClick = { viewModel.setActiveTab(3) }
                            )

                            // 5. پشتیبانی (Support)
                            UnifiedNavItem(
                                title = "پشتیبانی",
                                icon = Icons.Default.HeadsetMic,
                                isSelected = activeTab == 4,
                                badgeCount = 0,
                                badgeColor = CleanGreenPrimary,
                                onClick = { viewModel.setActiveTab(4) }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ModernAppBackgroundGradient)
                        .padding(paddingValues)
                ) {
                    when (activeTab) {
                        0 -> DeliveryRouteScreen(
                            orders = orders,
                            onSelectOrderForSettlement = { orderWithItems ->
                                viewModel.initiateSettlementForOrder(orderWithItems.order.id)
                            },
                            onOpenScanner = { orderId ->
                                viewModel.openScanner(com.example.data.model.ScanStage.DELIVERY, orderId)
                            },
                            onReturnToCleanWarehouse = { orderId, cleanRack, reason ->
                                viewModel.returnToCleanWarehouse(orderId, cleanRack, reason)
                            },
                            onRefreshOrders = { viewModel.syncWithWebPanel(silent = false) },
                            isSyncing = isSyncing
                        )
                        1 -> CollectionRouteScreen(
                            orders = orders,
                            onSelectOrderForInvoice = { orderWithItems ->
                                viewModel.selectOrder(orderWithItems.order.id)
                                viewModel.setActiveTab(99)
                            },
                            onFinalizeInvoice = { orderId ->
                                viewModel.finalizeInvoiceRegistration(orderId)
                            },
                            onRefreshOrders = { viewModel.syncWithWebPanel(silent = false) },
                            isSyncing = isSyncing
                        )
                        2 -> WarehouseHandoverScreen(
                            orders = orders,
                            isPrinting = isPrinting,
                            onConfirmWarehouseHandover = { orderId, rackCode ->
                                viewModel.confirmWarehouseHandover(orderId, rackCode)
                            },
                            onPrintWarehouseReceipt = { orderWithItems ->
                                viewModel.printOrderReceipt("رسید تحویل و نگهداری انباردار", orderWithItems)
                            },
                            onOpenScanner = { targetId ->
                                viewModel.openScanner(com.example.data.model.ScanStage.WORKSHOP, targetId)
                            },
                            onLoadCarpetIntoVan = { orderId ->
                                viewModel.loadOrderIntoVanForRedelivery(orderId)
                            }
                        )
                        3 -> DeliverySettlementScreen(
                            orders = orders,
                            initiatedSettlementOrderIds = initiatedSettlementOrderIds,
                            selectedOrderId = selectedOrderId,
                            isPrinting = isPrinting,
                            onSettlePayment = { id, paid, discount, method ->
                                viewModel.settlePayment(id, paid, discount, method)
                            },
                            onPrintReceipt = { orderWithItems, method ->
                                viewModel.printOrderReceipt("رسید تسویه حساب و تحویل فرش", orderWithItems, method)
                            },
                            onOpenScanner = { targetId ->
                                viewModel.openScanner(com.example.data.model.ScanStage.DELIVERY, targetId)
                            },
                            onSettleWithOffice = {
                                viewModel.settleWithOffice()
                            },
                            onPrintDailySettlementReport = {
                                viewModel.printDailySettlementReport(settledOrders = orders.filter { it.order.status == "DELIVERED_SETTLED" })
                            },
                            onSignatureCaptured = { orderId, signatureData ->
                                viewModel.captureCustomerSignature(orderId, signatureData)
                            },
                            onReturnToCleanWarehouse = { orderId, cleanRack, reason ->
                                viewModel.returnToCleanWarehouse(orderId, cleanRack, reason)
                            }
                        )
                        4 -> DispatchChatScreen(
                            messages = chatMessages,
                            supportPhoneNumber = supportPhoneNumber,
                            onSendMessage = { text -> viewModel.sendChatMessage(text) }
                        )
                        5 -> GpsTrackingScreen(
                            isGpsActive = isGpsActive,
                            unsyncedCount = unsyncedCount,
                            isSyncing = isSyncing,
                            recentGpsLogs = recentGpsLogs,
                            onToggleGps = { viewModel.toggleGpsTracking() },
                            onSyncNow = { viewModel.syncWithWebPanel() }
                        )
                        6 -> SettingsScreen(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            connectedPrinterName = connectedPrinter?.name,
                            onOpenPrinterDialog = {
                                openPrinterDialogWithPermissionCheck()
                            },
                            onPrintTestReceipt = { viewModel.printTestReceipt() },
                            onSyncNow = { viewModel.syncWithWebPanel() },
                            savedServerUrl = serverUrl,
                            savedApiKey = driverApiKey,
                            isTestingConnection = isTestingConnection,
                            connectionTestResult = connectionTestResult,
                            onUpdateServerUrl = { viewModel.updateServerUrl(it) },
                            onUpdateServerConfig = { url, key -> viewModel.updateServerConfig(url, key) },
                            onTestConnection = { url, key -> viewModel.testServerConnection(url, key) },
                            tariffSyncResult = tariffsResult,
                            onRefreshTariffs = { viewModel.refreshTariffs() },
                            backupInfo = backupInfo,
                            onBackupDatabase = { viewModel.backupDatabase() },
                            onRestoreDatabase = { viewModel.restoreDatabase() },
                            savedSupportPhone = supportPhoneNumber,
                            onUpdateSupportPhone = { viewModel.updateSupportPhoneNumber(it) },
                            onLogout = { viewModel.logoutDriver() }
                        )
                        99 -> CarpetRegistrationScreen(
                            orderWithItems = selectedOrder,
                            isPrinting = isPrinting,
                            tariffSyncResult = tariffsResult,
                            onRefreshTariffs = { viewModel.refreshTariffs() },
                            onBack = { viewModel.setActiveTab(1) },
                            onAddCarpetItem = { type, len, wid, price, servs, defs, notes, tag ->
                                selectedOrder?.let {
                                    viewModel.addCarpetItem(it.order.id, type, len, wid, price, servs, defs, notes, tag)
                                }
                            },
                            onDeleteCarpetItem = { itemId ->
                                selectedOrder?.let {
                                    viewModel.deleteCarpetItem(itemId, it.order.id)
                                }
                            },
                            onPrintReceipt = {
                                selectedOrder?.let {
                                    viewModel.printOrderReceipt("پیش‌فاکتور اولیه دریافت فرش", it)
                                }
                            },
                            onProceedToWorkshop = {
                                selectedOrder?.let {
                                    viewModel.finalizeInvoiceRegistration(it.order.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unified Custom Bottom Navigation Item matching the reference design:
 * When active: Light green pill background (#E7F7F1), dark green icon & text (#087A5A)
 * When inactive: Gray icon & text
 */
@Composable
private fun UnifiedNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    badgeColor: Color = CleanGreenPrimary,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CleanGreenPrimaryLight else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) CleanGreenAccent else DarkOnSurfaceMuted,
                    modifier = Modifier.size(24.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = FarsiUtils.toFarsiDigits(badgeCount.toString()),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) CleanGreenAccent else DarkOnSurfaceMuted
            )
        }
    }
}

@Composable
private fun NotificationItemCard(notification: AppNotification) {
    val bgColor: Color
    val borderColor: Color
    val accentColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val categoryLabel: String

    when (notification.category) {
        NotificationCategory.MISSION -> {
            bgColor = CleanGreenPrimaryLight.copy(alpha = 0.5f)
            borderColor = CleanGreenPrimary.copy(alpha = 0.35f)
            accentColor = CleanGreenPrimaryDark
            icon = Icons.Default.LocalShipping
            categoryLabel = "ماموریت"
        }
        NotificationCategory.SUPPORT -> {
            bgColor = Color(0xFFEFF6FF)
            borderColor = Color(0xFF93C5FD)
            accentColor = Color(0xFF0284C7)
            icon = Icons.Default.HeadsetMic
            categoryLabel = "پشتیبانی"
        }
        NotificationCategory.SYNC -> {
            bgColor = Color(0xFFF0FDF4)
            borderColor = Color(0xFF86EFAC)
            accentColor = Color(0xFF16A34A)
            icon = Icons.Default.Sync
            categoryLabel = "همگام‌سازی"
        }
        NotificationCategory.SYSTEM -> {
            bgColor = CleanWarningBg.copy(alpha = 0.6f)
            borderColor = CleanWarningText.copy(alpha = 0.5f)
            accentColor = CleanWarningText
            icon = Icons.Default.Info
            categoryLabel = "سیستم"
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = accentColor,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = categoryLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = notification.message,
                fontSize = 11.5.sp,
                color = CleanLightOnSurface,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!notification.orderId.isNullOrBlank()) {
                    Text(
                        text = "سفارش: #${FarsiUtils.toFarsiDigits(notification.orderId)}",
                        fontSize = 10.5.sp,
                        color = CleanLightOnSurfaceMuted,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text = "ساعت ${notification.timeFormatted}",
                    fontSize = 10.5.sp,
                    color = CleanLightOnSurfaceMuted
                )
            }
        }
    }
}
