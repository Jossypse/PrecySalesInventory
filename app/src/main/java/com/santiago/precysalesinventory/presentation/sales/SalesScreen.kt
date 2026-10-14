package com.santiago.precysalesinventory.presentation.sales

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.santiago.precysalesinventory.data.local.entity.Product
import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem
import com.santiago.precysalesinventory.presentation.cart.CartDialog
import com.santiago.precysalesinventory.ui.theme.SoftGray
import com.santiago.precysalesinventory.ui.theme.TextGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: SalesViewModel, isTargetPage: Boolean, isScrolling: Boolean) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val availableDates by viewModel.availableSalesDates.collectAsState()

    var showCart by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isTargetPage, isScrolling) {
        if (!isTargetPage || isScrolling) {
            drawerState.close()
        }
    }

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedSaleForDetail by remember { mutableStateOf<Sale?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(allProducts, searchQuery) {
        if (searchQuery.isBlank()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val filteredSales = remember(allSales, selectedDate) {
        if (selectedDate == null) {
            allSales
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            allSales.filter { sdf.format(Date(it.timestamp)) == selectedDate }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sales Log", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                        var showDateFilter by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showDateFilter = !showDateFilter }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                            }
                            DropdownMenu(expanded = showDateFilter, onDismissRequest = { showDateFilter = false }) {
                                DropdownMenuItem(
                                    text = { Text("All Time") },
                                    onClick = {
                                        selectedDate = null
                                        showDateFilter = false
                                    }
                                )
                                availableDates.forEach { date ->
                                    DropdownMenuItem(
                                        text = { Text(date) },
                                        onClick = {
                                            selectedDate = date
                                            showDateFilter = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedDate != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Date: $selectedDate",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredSales) { sale ->
                            SaleHistoryItem(sale) {
                                selectedSaleForDetail = sale
                                scope.launch { drawerState.close() }
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Sales", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (viewModel.cart.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                        Text(viewModel.cart.size.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showCart = true }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search products...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(product = product, onDoubleTap = { viewModel.addToCart(product) })
                    }
                }
            }

            if (showCart) {
                CartDialog(viewModel = viewModel, onDismiss = { showCart = false })
            }

            if (selectedSaleForDetail != null) {
                SaleDetailDialog(
                    sale = selectedSaleForDetail!!,
                    viewModel = viewModel,
                    onDismiss = { selectedSaleForDetail = null }
                )
            }
        }
    }
}

@Composable
fun SaleHistoryItem(sale: Sale, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = sdf.format(Date(sale.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Transaction #${sale.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(time, style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Text(
                "₱${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SoftGray)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(product: Product, onDoubleTap: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onDoubleClick = onDoubleTap
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₱${product.sellingPrice}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        color = when {
                            product.quantity <= 0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            product.quantity <= 5 -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        },
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = if (product.quantity > 0) " Stock: ${product.quantity} " else " OUT OF STOCK ",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                product.quantity <= 0 -> MaterialTheme.colorScheme.error
                                product.quantity <= 5 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleDetailDialog(sale: Sale, viewModel: SalesViewModel, onDismiss: () -> Unit) {
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    LaunchedEffect(sale.id) {
        items = viewModel.getItemsForSale(sale.id)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sale Receipt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaction ID: #${sale.id}", style = MaterialTheme.typography.labelLarge, color = TextGray)
                        Text("Date: ${sdf.format(Date(sale.timestamp))}", style = MaterialTheme.typography.bodySmall, color = TextGray)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("Purchase Items", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                                        Text("₱${item.priceAtSale} x ${item.quantity}", style = MaterialTheme.typography.labelSmall, color = TextGray)
                                    }
                                    Text("₱${String.format(Locale.getDefault(), "%.2f", item.priceAtSale * item.quantity)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", fontWeight = FontWeight.Bold)
                            Text("₱${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cash Payment", style = MaterialTheme.typography.bodyMedium)
                            Text("₱${String.format(Locale.getDefault(), "%.2f", sale.paymentAmount)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Change", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("₱${String.format(Locale.getDefault(), "%.2f", sale.change)}", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Close Details")
                }
            }
        }
    }
}
