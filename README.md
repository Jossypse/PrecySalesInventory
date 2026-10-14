📱 User Documentation

Precy Sales & Inventory
An all-in-one sales, inventory tracking, and business intelligence system for small to medium enterprises.

🎯 Purpose
Precy Sales & Inventory is designed to simplify daily business operations by providing real-time inventory management, fast point-of-sale checkout, detailed transaction history, and time-based revenue and profit reporting—all working offline on your mobile device.


✨ Key Features for Users
1.📦 Inventory Management

  ◦Product Registration: Register items with product photos, name, cost price, selling price, and stock count.

  ◦Image Storage: Uploaded photos are stored permanently on your device so they never disappear.

  ◦Quick Restock: Easily add new stock to existing products using the + (Add Stock) button.

  ◦Real-time Search & Filtering: Instant search bar to quickly locate items in large inventories.

  ◦Stock Audit Log: View a historical timeline of every stock registration or restock event.

  ◦Delete Safeguard: Deleting products requires confirmation to prevent accidental loss.

2.🛒 Point of Sale (POS) & Checkout

  ◦Double-Tap to Cart: Double-tap any product card to add it directly to your shopping cart.

  ◦Stock Level Warnings: Displays live stock badges (Stock: X, OUT OF STOCK, or low stock warnings).

  ◦Smart Quantity Limits: Prevents adding more items to the cart than what is currently in stock.

  ◦Checkout & Change Calculator: Input customer cash payment to automatically calculate change due.

  ◦Automated Deduction: Completing a sale automatically deducts quantities from your live product inventory.

3.📊 Business Reports & Analytics

  ◦Revenue & Profit Breakdown: Track your business performance filtered by Day, Month, or Year.

  ◦Detailed Sales Insights: Tap report period card to open a detailed receipt showing exact products sold, unit prices, quantities, and grand total revenue.

  ◦Paginated View: High-volume sales details are paginated by 10 items for fast and clean navigation.

  ◦12-Hour Format: Timestamps across receipts and logs are formatted in user-friendly 12-hour AM/PM time.



💻 Developer Documentation

Architecture Overview
The app is built using Clean Architecture, MVVM (Model-View-ViewModel), and SOLID engineering principles for high maintainability, testability, and scalability.

com.santiago.precysalesinventory/

├── data/                               # Data Layer

│   ├── local/

│   │   ├── dao/                        # Room DAOs (ProductDao, SaleDao, InventoryLogDao)

│   │   ├── entity/                     # Room Entities (Product, Sale, SaleItem, InventoryLog)

│   │   ├── storage/                    # File I/O (LocalImageStorageManager)

│   │   └── AppDatabase.kt              # Room Database (v4) & Migrations (1→2, 2→3, 3→4)

│   └── repository/                     # Repository Implementations

│       ├── InventoryLogRepositoryImpl.kt

│       ├── ProductRepositoryImpl.kt

│       └── SaleRepositoryImpl.kt

├── domain/                             # Domain Layer (Pure Business Contracts)

│   ├── model/                          # Domain Models (CartItem)

│   └── repository/                     # Abstract Repository Contracts

│       ├── ImageStorageManager.kt

│       ├── InventoryLogRepository.kt

│       ├── ProductRepository.kt

│       └── SaleRepository.kt

├── presentation/                       # Presentation Layer

│   ├── cart/                           # CartDialog.kt

│   ├── inventory/                      # InventoryScreen.kt

│   ├── reports/                        # ReportsScreen.kt

│   ├── sales/                          # SalesScreen.kt & SalesViewModel.kt

│   └── theme/                          # Material 3 Color, Theme, Type

└── MainActivity.kt                     # Single Activity Entry Point



Language - Kotlin 2.2.10

UI Framework - Jetpack Compose + Material 3

Database - Room 2.8.4 + KSP 
