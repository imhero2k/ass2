package com.example.fit5046_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fit5046_app.ui.theme.FIT5046_appTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046_appTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var showStartingScreen by remember { mutableStateOf(true) }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Check authentication state on app start
    LaunchedEffect(Unit) {
        isAuthenticated = FirebaseAuth.getInstance().currentUser != null
        if (isAuthenticated) {
            showStartingScreen = false
        }
    }

    // Listen for auth state changes - but don't auto-authenticate during registration
    LaunchedEffect(authState.user) {
        println("DEBUG: MainActivity LaunchedEffect triggered: authState.user=${authState.user?.email}, isAuthenticated=$isAuthenticated")

        if (authState.user != null) {
            // User is signed in - but let AuthenticationScreen handle the flow
            println("DEBUG: MainActivity NOT auto-authenticating - letting AuthenticationScreen handle it")
            // Don't set isAuthenticated = true here - let AuthenticationScreen handle it
        } else {
            // User is signed out
            if (isAuthenticated) {
                println("DEBUG: MainActivity setting isAuthenticated = false - user signed out")
                isAuthenticated = false
                showStartingScreen = true
            }
        }
    }

    when {
        isAuthenticated -> {
            ExpandedModalNavigationRail(authViewModel = authViewModel)
        }
        showStartingScreen -> {
            StartingScreen(
                onStartClick = {
                    showStartingScreen = false
                },
                onLoginClick = {
                    showStartingScreen = false
                }
            )
        }
        else -> {
            AuthenticationScreen(
                onAuthSuccess = {
                    isAuthenticated = true
                },
                onBackToStart = {
                    showStartingScreen = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedModalNavigationRail(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen =
        Destination.entries.find { it.route == navBackStackEntry?.destination?.route }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationRailItem(
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.close_24px
                            ),
                            contentDescription = "Close drawer",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Destination.entries.forEach { destination ->
                    NavigationRailItem(
                        selected = currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                painterResource(destination.icon), contentDescription =
                                    destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentScreen?.label ?: "My Sugar",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else
                                    drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destination.DASHBOARD.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destination.DASHBOARD.route) { Dashboard() }
                composable(Destination.DIARY.route) { Diary(getMockLogs()) }
                composable(Destination.LOG.route) { Log() }
                composable(Destination.REPORTS.route) { Reports() }
                composable(Destination.HEALTHBLOG.route) { HealthBlog() }
                composable(Destination.USERSETTING.route) {
                    UserSetting(
                        authViewModel = authViewModel,
                        onNavigateToProfile = { navController.navigate(SubRoutes.USERPROFILE) },
                        onNavigateToReminders = { navController.navigate(SubRoutes.REMINDERMANAGEMENT) },
                        onNavigateToAppInfo = { navController.navigate(SubRoutes.APPINFORMATION) }
                    )
                }
                composable(SubRoutes.USERPROFILE) {
                    UserProfile(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(SubRoutes.REMINDERMANAGEMENT) {
                    ReminderManagement(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}