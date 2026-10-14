package com.santiago.precysalesinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.santiago.precysalesinventory.presentation.inventory.InventoryScreen
import com.santiago.precysalesinventory.presentation.reports.ReportsScreen
import com.santiago.precysalesinventory.presentation.sales.SalesScreen
import com.santiago.precysalesinventory.presentation.sales.SalesViewModel
import com.santiago.precysalesinventory.ui.theme.PrecySalesInventoryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrecySalesInventoryTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val viewModel: SalesViewModel = viewModel()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("Inventory", fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal) },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Sales") },
                    label = { Text("Sales", fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal) },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Reports", fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Normal) },
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize(),
            userScrollEnabled = true,
            beyondViewportPageCount = 1
        ) { page ->
            Box(
                Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                when (page) {
                    0 -> InventoryScreen(
                        viewModel,
                        isTargetPage = pagerState.currentPage == 0,
                        isScrolling = pagerState.isScrollInProgress
                    )
                    1 -> SalesScreen(
                        viewModel,
                        isTargetPage = pagerState.currentPage == 1,
                        isScrolling = pagerState.isScrollInProgress
                    )
                    2 -> ReportsScreen(viewModel)
                }
            }
        }
    }
}
