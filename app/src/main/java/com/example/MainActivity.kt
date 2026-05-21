package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AgentViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ModelSettingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: AgentViewModel) {
    val navController = rememberNavController()
    val bottomBarHeight by animateDpAsState(
        targetValue = if (viewModel.isBottomBarVisible) 100.dp else 0.dp,
        animationSpec = tween(300),
        label = "bottomBarHeight"
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PremiumSurface.copy(alpha = 0.8f),
                modifier = Modifier.width(240.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("控制面板", color = PremiumTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 32.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("global_settings") { launchSingleTop = true }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("系统设置", color = PremiumTextPrimary, fontSize = 16.sp)
                        }
                        
                        TextButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("about") { launchSingleTop = true }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("关于我们", color = PremiumTextPrimary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        LaunchedEffect(currentDestination?.route) {
            if (currentDestination?.route != "chat") {
                viewModel.isBottomBarVisible = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PremiumBg)
        ) {
            NavHost(
                navController = navController,
                startDestination = "chat",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("chat") { ChatScreen(viewModel, PaddingValues(bottom = bottomBarHeight)) }
                composable("agent") {
                    SettingsScreen(PaddingValues(bottom = bottomBarHeight), onNavigateToTools = {
                        navController.navigate("tools")
                    })
                }
                composable("tools") { com.example.ui.screens.ToolsManageScreen(PaddingValues(bottom = bottomBarHeight)) }
                composable("model") { ModelSettingsScreen(viewModel, PaddingValues(bottom = bottomBarHeight)) }
                composable("global_settings") { com.example.ui.screens.GlobalSettingsScreen(PaddingValues(bottom = bottomBarHeight)) }
                composable("about") { com.example.ui.screens.AboutScreen(PaddingValues(bottom = bottomBarHeight)) }
            }

            // Floating Bottom Bar

        androidx.compose.animation.AnimatedVisibility(
            visible = viewModel.isBottomBarVisible,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PremiumSurface.copy(alpha = 0.95f),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavItem(
                            icon = Icons.Default.ChatBubbleOutline,
                            label = "对话",
                            selected = currentDestination?.hierarchy?.any { it.route == "chat" } == true,
                            onClick = {
                                navController.navigate("chat") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavItem(
                            icon = Icons.Default.Extension,
                            label = "组件",
                            selected = currentDestination?.hierarchy?.any { it.route == "agent" } == true,
                            onClick = {
                                navController.navigate("agent") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavItem(
                            icon = Icons.Default.Tune,
                            label = "核心",
                            selected = currentDestination?.hierarchy?.any { it.route == "model" } == true,
                            onClick = {
                                navController.navigate("model") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun RowScope.NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = selected, transitionSpec = {
            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
        }, label = "nav_item") { isSelected ->
            if (isSelected) {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(PremiumHighlight, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = label, tint = PremiumPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, color = PremiumPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, contentDescription = label, tint = PremiumTextSecondary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
