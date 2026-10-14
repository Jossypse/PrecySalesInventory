package com.santiago.precysalesinventory.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.santiago.precysalesinventory.presentation.sales.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ProductReportDetail(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
    val timestamp: Long,
    val saleId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: SalesViewModel) {
    val allSales by viewModel.allSales.collectAsState()
    val allSaleItems by viewModel.allSaleItems.collectAsState()

    var reportType by remember { mutableStateOf("Day") }
    val reportOptions = listOf("Day", "Month", "Year")

    var selectedPeriodDetail by remember { mutableStateOf<Pair<String, List<ProductReportDetail>>?>(null) }

    val dateFormat = when (reportType) {
        "Day" -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        "Month" -> SimpleDateFormat("yyyy-MM", Locale.getDefault())
        else -> SimpleDateFormat("yyyy", Locale.getDefault())
    }

    val groupedSales = remember(allSales, allSaleItems, reportType) {
        allSales.groupBy { dateFormat.format(Date(it.timestamp)) }
            .mapValues { entry ->
                val totalRevenue = entry.value.sumOf { it.totalAmount }
                val saleIds = entry.value.map { it.id }
                val itemsInGroup = allSaleItems.filter { it.saleId in saleIds }
                val totalProfit = itemsInGroup.sumOf { (it.priceAtSale - it.capitalAtSale) * it.quantity }

                val productDetails = itemsInGroup.map { item ->
                    val sale = entry.value.find { it.id == item.saleId }
                    ProductReportDetail(
                        name = item.productName,
                        quantity = item.quantity,
                        unitPrice = item.priceAtSale,
                        total = item.priceAtSale * item.quantity,
                        timestamp = sale?.timestamp ?: 0L,
                        saleId = item.saleId
                    )
                }.sortedByDescending { it.timestamp }

                Triple(totalRevenue, totalProfit, productDetails)
            }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                reportOptions.forEach { type ->
                    val selected = reportType == type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { reportType = type },
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = type,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (groupedSales.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(groupedSales) { (date, stats) ->
                        ReportCard(
                            date = date,
                            revenue = stats.first,
                            profit = stats.second,
                            onClick = { selectedPeriodDetail = date to stats.third }
                        )
                    }
                }
            }

            if (selectedPeriodDetail != null) {
                PeriodDetailDialog(
                    period = selectedPeriodDetail!!.first,
                    details = selectedPeriodDetail!!.second,
                    onDismiss = { selectedPeriodDetail = null }
                )
            }
        }
    }
}

@Composable
fun ReportCard(date: String, revenue: Double, profit: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tap to see details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Revenue",
                    value = revenue,
                    icon = Icons.Default.Payments,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Profit",
                    value = profit,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: Double, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.05f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₱${String.format(Locale.getDefault(), "%.2f", value)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun PeriodDetailDialog(period: String, details: List<ProductReportDetail>, onDismiss: () -> Unit) {
    val totalRevenue = details.sumOf { it.total }
    val totalQty = details.sumOf { it.quantity }

    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10
    val pageCount = (details.size + pageSize - 1) / pageSize
    val currentItems = details.drop(currentPage * pageSize).take(pageSize)

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp),
        title = {
            Column {
                Text("Sales Details", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(period, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Product", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                        Text("Qty", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Total", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(currentItems) { detail ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(detail.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("${timeFormat.format(Date(detail.timestamp))} - Trans #${detail.saleId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("₱${String.format(Locale.getDefault(), "%.2f", detail.unitPrice)} / unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${detail.quantity}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Text("₱${String.format(Locale.getDefault(), "%.2f", detail.total)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1.3f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        }
                    }
                }

                if (pageCount > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }
                        Text(
                            "Page ${currentPage + 1} of $pageCount",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { if (currentPage < pageCount - 1) currentPage++ },
                            enabled = currentPage < pageCount - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Items Sold", style = MaterialTheme.typography.bodySmall)
                            Text("$totalQty", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total Revenue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₱${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Close Details")
            }
        }
    )
}
