package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.GlobalSettings
import com.example.settings.AppThemeSettings
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsFlow = remember { GlobalSettings.getSettings(context) }
    val settings by settingsFlow.collectAsState(initial = AppThemeSettings())

    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            scope.launch {
                GlobalSettings.updateSettings(context) { this[GlobalSettings.BG_IMAGE_URI] = it.toString() }
            }
        }
    }

    val bubbleBgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            scope.launch {
                GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_IMAGE_URI] = it.toString() }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBg)
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("个性化设置", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PremiumTextPrimary)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section 1: Background Settings
            Text("全局背景", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PremiumPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = MaterialTheme.shapes.large, color = PremiumSurface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { bgLauncher.launch(arrayOf("image/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumHighlight, contentColor = PremiumPrimary)
                    ) {
                        Text(if (settings.bgImageUri != null) "更换背景图" else "上传背景图")
                    }
                    if (settings.bgImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { scope.launch { GlobalSettings.updateSettings(context) { remove(GlobalSettings.BG_IMAGE_URI) } } }) {
                            Text("清除背景图", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("背景透明度", color = PremiumTextSecondary, fontSize = 14.sp)
                    Slider(
                        value = settings.bgAlpha,
                        onValueChange = { scale -> scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BG_ALPHA] = scale } } },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("背景模糊度", color = PremiumTextSecondary, fontSize = 14.sp)
                    Slider(
                        value = settings.bgBlur,
                        onValueChange = { scale -> scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BG_BLUR] = scale } } },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Bubble Settings
            Text("聊天气泡风格", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PremiumPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = MaterialTheme.shapes.large, color = PremiumSurface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Bubble Style
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.bubbleStyle == "glass",
                            onClick = { scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_STYLE] = "glass" } } },
                            label = { Text("毛玻璃") }
                        )
                        FilterChip(
                            selected = settings.bubbleStyle == "plain",
                            onClick = { scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_STYLE] = "plain" } } },
                            label = { Text("普通") }
                        )
                        FilterChip(
                            selected = settings.bubbleStyle == "custom_image",
                            onClick = { scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_STYLE] = "custom_image" } } },
                            label = { Text("自定义图") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (settings.bubbleStyle == "plain") {
                        Text("文字颜色", color = PremiumTextSecondary, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            val colors = listOf(0xFFFFFFFFL, 0xFF000000L, 0xFF6200EAL, 0xFF03DAC5L, 0xFFE91E63L)
                            colors.forEach { col ->
                                Box(modifier = Modifier
                                    .size(32.dp)
                                    .background(androidx.compose.ui.graphics.Color(col), androidx.compose.foundation.shape.CircleShape)
                                    .fillMaxWidth()
                                    .wrapContentSize()
                                    .clickable {
                                        scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_TEXT_COLOR] = col } }
                                    }
                                ) {
                                    if (settings.bubbleTextColor == col) {
                                        Icon(Icons.Default.Done, contentDescription = null, tint = if (col == 0xFFFFFFFFL) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (settings.bubbleStyle == "custom_image") {
                        Button(
                            onClick = { bubbleBgLauncher.launch(arrayOf("image/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumHighlight, contentColor = PremiumPrimary)
                        ) {
                            Text(if (settings.bubbleImageUri != null) "更换气泡图片" else "上传气泡图片")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("气泡透明度", color = PremiumTextSecondary, fontSize = 14.sp)
                        Slider(
                            value = settings.bubbleAlpha,
                            onValueChange = { scale -> scope.launch { GlobalSettings.updateSettings(context) { this[GlobalSettings.BUBBLE_ALPHA] = scale } } },
                            valueRange = 0.1f..1f,
                            colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary)
                        )
                    }
                }
            }
        }
    }
}
