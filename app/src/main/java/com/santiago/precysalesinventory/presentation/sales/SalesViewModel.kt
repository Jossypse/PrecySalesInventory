package com.santiago.precysalesinventory.presentation.sales

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.santiago.precysalesinventory.data.local.AppDatabase
import com.santiago.precysalesinventory.data.local.entity.InventoryLog
import com.santiago.precysalesinventory.data.local.entity.Product
import com.santiago.precysalesinventory.data.local.entity.Sale
import com.santiago.precysalesinventory.data.local.entity.SaleItem
import com.santiago.precysalesinventory.data.local.storage.LocalImageStorageManager
import com.santiago.precysalesinventory.data.repository.InventoryLogRepositoryImpl
import com.santiago.precysalesinventory.data.repository.ProductRepositoryImpl
import com.santiago.precysalesinventory.data.repository.SaleRepositoryImpl
import com.santiago.precysalesinventory.domain.model.CartItem
import com.santiago.precysalesinventory.domain.repository.ImageStorageManager
import com.santiago.precysalesinventory.domain.repository.InventoryLogRepository
import com.santiago.precysalesinventory.domain.repository.ProductRepository
import com.santiago.precysalesinventory.domain.repository.SaleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel @JvmOverloads constructor(
    application: Application,
    private val productRepository: ProductRepository = ProductRepositoryImpl(
        AppDatabase.getDatabase(application).productDao()
    ),
    private val saleRepository: SaleRepository = SaleRepositoryImpl(
        AppDatabase.getDatabase(application).saleDao()
    ),
    private val logRepository: InventoryLogRepository = InventoryLogRepositoryImpl(
        AppDatabase.getDatabase(application).inventoryLogDao()
    ),
    private val imageStorageManager: ImageStorageManager = LocalImageStorageManager(application)
) : AndroidViewModel(application) {

    val allProducts: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSales: StateFlow<List<Sale>> = saleRepository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allInventoryLogs: StateFlow<List<InventoryLog>> = logRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSaleItems: StateFlow<List<SaleItem>> = saleRepository.getAllSaleItems()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cart = mutableStateListOf<CartItem>()

    fun addProduct(name: String, capital: Double, price: Double, quantity: Int, imageUriStr: String?) {
        viewModelScope.launch {
            val finalImageUri = imageUriStr?.let { uriStr ->
                if (uriStr.startsWith("content://")) {
                    imageStorageManager.saveImageToInternalStorage(Uri.parse(uriStr)) ?: uriStr
                } else uriStr
            } ?: imageUriStr

            val product = Product(
                name = name,
                capitalPrice = capital,
                sellingPrice = price,
                quantity = quantity,
                imageUri = finalImageUri
            )
            productRepository.insertProduct(product)

            logRepository.insertLog(
                InventoryLog(
                    productName = name,
                    capitalPrice = capital,
                    sellingPrice = price,
                    quantityAdded = quantity,
                    imageUri = finalImageUri
                )
            )
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val currentProduct = productRepository.getProductById(product.id)
            val finalUri = if (product.imageUri != null && product.imageUri != currentProduct?.imageUri && product.imageUri.startsWith("content://")) {
                imageStorageManager.saveImageToInternalStorage(Uri.parse(product.imageUri)) ?: product.imageUri
            } else {
                product.imageUri
            }
            productRepository.updateProduct(product.copy(imageUri = finalUri))
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun restockProduct(product: Product, additionalQuantity: Int) {
        viewModelScope.launch {
            productRepository.addStock(product.id, additionalQuantity)

            logRepository.insertLog(
                InventoryLog(
                    productName = product.name,
                    capitalPrice = product.capitalPrice,
                    sellingPrice = product.sellingPrice,
                    quantityAdded = additionalQuantity,
                    imageUri = product.imageUri
                )
            )
        }
    }

    fun addToCart(product: Product) {
        if (product.quantity <= 0) return

        val existingItem = cart.find { it.product.id == product.id }
        if (existingItem != null) {
            if (existingItem.quantity < product.quantity) {
                existingItem.quantity++
                val index = cart.indexOf(existingItem)
                cart[index] = existingItem.copy(quantity = existingItem.quantity)
            }
        } else {
            cart.add(CartItem(product, 1))
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        cart.remove(cartItem)
    }

    fun updateCartQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
        } else {
            val originalProduct = allProducts.value.find { it.id == cartItem.product.id }
            val maxStock = originalProduct?.quantity ?: cartItem.product.quantity

            val finalQuantity = if (newQuantity > maxStock) maxStock else newQuantity

            val index = cart.indexOf(cartItem)
            if (index != -1) {
                cart[index] = cartItem.copy(quantity = finalQuantity)
            }
        }
    }

    val cartTotal: Double
        get() = cart.sumOf { it.product.sellingPrice * it.quantity }

    fun checkout(payment: Double) {
        val total = cartTotal
        val change = payment - total
        if (change < 0) return

        val sale = Sale(
            totalAmount = total,
            paymentAmount = payment,
            change = change
        )

        val saleItems = cart.map {
            SaleItem(
                saleId = 0,
                productId = it.product.id,
                productName = it.product.name,
                priceAtSale = it.product.sellingPrice,
                capitalAtSale = it.product.capitalPrice,
                quantity = it.quantity
            )
        }

        viewModelScope.launch {
            saleRepository.completeTransaction(sale, saleItems)
            saleItems.forEach { item ->
                productRepository.deductStock(item.productId, item.quantity)
            }
            cart.clear()
        }
    }

    suspend fun getItemsForSale(saleId: Int): List<SaleItem> {
        return saleRepository.getItemsForSale(saleId)
    }

    val availableSalesDates: StateFlow<List<String>> = allSales.map { sales ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sales.map { sdf.format(Date(it.timestamp)) }.distinct()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableInventoryDates: StateFlow<List<String>> = allInventoryLogs.map { logs ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        logs.map { sdf.format(Date(it.timestamp)) }.distinct()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
