package com.santiago.precysalesinventory.presentation.inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import com.santiago.precysalesinventory.data.local.entity.Product
import com.santiago.precysalesinventory.presentation.sales.SalesViewModel
import com.santiago.precysalesinventory.ui.theme.SoftGray
import com.santiago.precysalesinventory.ui.theme.TextGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: SalesViewModel, isTargetPage: Boolean, isScrolling: Boolean) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val allProducts by viewModel.allProducts.collectAsState()
    val inventoryLogs by viewModel.allInventoryLogs.collectAsState()
    val availableDates by viewModel.availableInventoryDates.collectAsState()

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var selectedLogForDetail by remember { mutableStateOf<InventoryLog?>(null) }
    var productToRestock by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productSearchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(allProducts, productSearchQuery) {
        if (productSearchQuery.isBlank()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(productSearchQuery, ignoreCase = true) }
        }
    }

    val filteredLogs = remember(inventoryLogs, selectedDate) {
        if (selectedDate == null) {
            inventoryLogs
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            inventoryLogs.filter { sdf.format(Date(it.timestamp)) == selectedDate }
        }
    }

    LaunchedEffect(isTargetPage, isScrolling) {
        if (!isTargetPage || isScrolling) {
            drawerState.close()
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
                        Text("Stock Log", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
                                "Showing: $selectedDate",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredLogs) { log ->
                            LogItemRow(log) {
                                selectedLogForDetail = log
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
                    title = { Text("Inventory", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    windowInsets = WindowInsets.statusBars
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                InventoryForm(viewModel)

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Product List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${filteredProducts.size} Items", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = productSearchQuery,
                    onValueChange = { productSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search products...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (productSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { productSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                filteredProducts.forEach { product ->
                    ProductItem(
                        product = product,
                        onRestock = { productToRestock = product },
                        onEdit = { selectedProductForEdit = product },
                        onDelete = { productToDelete = product }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (productToDelete != null) {
                DeleteConfirmationDialog(
                    product = productToDelete!!,
                    onDismiss = { productToDelete = null },
                    onConfirm = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    }
                )
            }

            if (selectedProductForEdit != null) {
                EditProductDialog(
                    product = selectedProductForEdit!!,
                    onDismiss = { selectedProductForEdit = null },
                    onSave = { updatedProduct ->
                        viewModel.updateProduct(updatedProduct)
                        selectedProductForEdit = null
                    }
                )
            }

            if (selectedLogForDetail != null) {
                LogDetailDialog(
                    log = selectedLogForDetail!!,
                    onDismiss = { selectedLogForDetail = null }
                )
            }

            if (productToRestock != null) {
                RestockDialog(
                    product = productToRestock!!,
                    onDismiss = { productToRestock = null },
                    onConfirm = { quantity ->
                        viewModel.restockProduct(productToRestock!!, quantity)
                        productToRestock = null
                    }
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(product: Product, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
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
                    Text(
                        "Delete Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    "Are you sure you want to delete \"${product.name}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RestockDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableStateOf("") }
    val addAmount = quantity.toIntOrNull() ?: 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
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
                    Column {
                        Text(
                            "Add Stock",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Available Stock ", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        }
                        Text(
                            "${product.quantity} units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity to Add") },
                    placeholder = { Text("Enter quantity ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (addAmount > 0) onConfirm(addAmount)
                    },
                    enabled = addAmount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONFIRM RESTOCK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LogItemRow(log: InventoryLog, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = sdf.format(Date(log.timestamp))

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
                    .width(80.dp)
                    .height(30.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Stocked: ${log.quantityAdded} units", style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SoftGray)
        }
    }
}

@Composable
fun LogDetailDialog(log: InventoryLog, onDismiss: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(log.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registration Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model = log.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(log.productName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Time Added: $dateString", style = MaterialTheme.typography.bodySmall, color = TextGray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Initial Stock: ${log.quantityAdded}", style = MaterialTheme.typography.bodyMedium)
                    Text("Capital Cost: ₱${log.capitalPrice}", style = MaterialTheme.typography.bodyMedium)
                    Text("Selling Price: ₱${log.sellingPrice}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Record")
            }
        }
    )
}

@Composable
fun InventoryForm(viewModel: SalesViewModel) {
    var name by remember { mutableStateOf("") }
    var capital by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Register New Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = capital,
                    onValueChange = { capital = it },
                    label = { Text("Capital") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text("Add Product Photo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && capital.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()) {
                        viewModel.addProduct(
                            name,
                            capital.toDoubleOrNull() ?: 0.0,
                            price.toDoubleOrNull() ?: 0.0,
                            quantity.toIntOrNull() ?: 0,
                            imageUri?.toString()
                        )
                        name = ""
                        capital = ""
                        price = ""
                        quantity = ""
                        imageUri = null
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Inventory", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onRestock: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("₱${product.sellingPrice}", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text("Stock: ${product.quantity}", style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Row {
                IconButton(onClick = onRestock) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Restock", tint = MaterialTheme.colorScheme.tertiary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditProductDialog(product: Product, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(product.name) }
    var capital by remember { mutableStateOf(product.capitalPrice.toString()) }
    var price by remember { mutableStateOf(product.sellingPrice.toString()) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var imageUri by remember { mutableStateOf<Uri?>(product.imageUri?.let { Uri.parse(it) }) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
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
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = capital,
                        onValueChange = { capital = it },
                        label = { Text("Cost") },
                        prefix = { Text("₱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        prefix = { Text("₱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Available Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSave(product.copy(
                            name = name,
                            capitalPrice = capital.toDoubleOrNull() ?: product.capitalPrice,
                            sellingPrice = price.toDoubleOrNull() ?: product.sellingPrice,
                            quantity = quantity.toIntOrNull() ?: product.quantity,
                            imageUri = imageUri?.toString()
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Apply Changes", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Discard Changes", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
