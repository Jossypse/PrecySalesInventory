package com.santiago.precysalesinventory.domain.model

import com.santiago.precysalesinventory.data.local.entity.Product

data class CartItem(
    val product: Product,
    var quantity: Int
)
