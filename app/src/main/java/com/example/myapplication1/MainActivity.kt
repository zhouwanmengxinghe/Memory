package com.example.myapplication1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication1.data.MemoryEvent
import com.example.myapplication1.ui.screens.*
import com.example.myapplication1.ui.theme.MyApplication1Theme
import com.example.myapplication1.viewmodel.AnniversaryViewModel
import com.example.myapplication1.viewmodel.MemoryViewModel
import com.example.myapplication1.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                MemoryApp()
            }
        }
    }
}

@Composable
fun MemoryApp() {
    val navController = rememberNavController()
    val viewModel: MemoryViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAdd = {
                    navController.navigate("add_edit")
                },
                onNavigateToEdit = { event ->
                    viewModel.updateEditingEvent(event)
                    navController.navigate("add_edit")
                },
                onNavigateToDetail = { event ->
                    viewModel.updateEditingEvent(event)
                    navController.navigate("detail")
                },
                onNavigateToUserProfile = {
                    navController.navigate("user_profile")
                },
                onNavigateToAnniversary = {
                    navController.navigate("anniversary")
                }
            )
        }
        
        composable("add_edit") {
            val editingEvent by viewModel.uiState.collectAsState()
            AddEditEventScreen(
                viewModel = viewModel,
                editingEvent = editingEvent.editingEvent,
                onNavigateBack = {
                    viewModel.updateEditingEvent(null)
                    navController.popBackStack()
                }
            )
        }
        
        composable("detail") {
            val editingEvent by viewModel.uiState.collectAsState()
            editingEvent.editingEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    viewModel = viewModel,
                    onNavigateBack = {
                        viewModel.updateEditingEvent(null)
                        navController.popBackStack()
                    },
                    onNavigateToEdit = {
                        navController.navigate("add_edit")
                    }
                )
            }
        }
        
        composable("user_profile") {
            val userViewModel: UserViewModel = hiltViewModel()
            UserProfileScreen(
                userViewModel = userViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("anniversary") {
            val anniversaryViewModel: AnniversaryViewModel = hiltViewModel()
            AnniversaryScreen(
                viewModel = anniversaryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}