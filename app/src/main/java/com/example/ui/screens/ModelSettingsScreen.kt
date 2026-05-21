package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsEntity
import com.example.ui.AgentViewModel
import com.example.ui.theme.*

@Composable
fun ModelSettingsScreen(viewModel: AgentViewModel, insetsPadding: PaddingValues) {
    val settingsState by viewModel.settings.collectAsState()
    val settings = settingsState ?: SettingsEntity()

    var baseUrl by remember(settings) { mutableStateOf(settings.baseUrl) }
    var apiKey by remember(settings) { mutableStateOf(settings.apiKey) }
    var activeModel by remember(settings) { mutableStateOf(settings.activeModel) }
    var temperature by remember(settings) { mutableStateOf(settings.temperature) }
    var maxTokens by remember(settings) { mutableStateOf(settings.maxTokens) }
    var memoryLength by remember(settings) { mutableStateOf(settings.memoryLength) }
    var isStreamResponse by remember(settings) { mutableStateOf(settings.isStreamResponse) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = PremiumBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPaddingScaffold ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "大模型中枢",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = 1.sp,
                    color = PremiumTextPrimary
                )
                Button(
                    onClick = {
                        viewModel.saveSettings(
                            SettingsEntity(
                                id = 1,
                                baseUrl = baseUrl,
                                apiKey = apiKey,
                                activeModel = activeModel,
                                temperature = temperature,
                                maxTokens = maxTokens,
                                memoryLength = memoryLength,
                                isStreamResponse = isStreamResponse
                            )
                        )
                        // Launch snackbar conceptually, or let DB handle it 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("应用配置", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = insetsPadding.calculateBottomPadding() + 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("网络与通信协议", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PremiumTextSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
                PremiumTextField(
                    label = "基建地址 (Base URL)",
                    value = baseUrl,
                    onValueChange = { baseUrl = it }
                )
                PremiumTextField(
                    label = "核心密钥 (API Key)",
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    isPassword = true
                )
                PremiumTextField(
                    label = "源核名称 (Model Name)",
                    value = activeModel,
                    onValueChange = { activeModel = it }
                )

                Spacer(modifier = Modifier.height(32.dp))
                Text("突触调参", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PremiumTextSecondary, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = PremiumSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("发散度 (Temperature): $temperature", color = PremiumTextPrimary, fontWeight = FontWeight.Bold)
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            valueRange = 0f..2f,
                            colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary, inactiveTrackColor = PremiumHighlight)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = PremiumSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("视界广度 (Max Tokens): $maxTokens", color = PremiumTextPrimary, fontWeight = FontWeight.Bold)
                        Slider(
                            value = maxTokens.toFloat(),
                            onValueChange = { maxTokens = it.toInt() },
                            valueRange = 100f..32000f,
                            colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary, inactiveTrackColor = PremiumHighlight)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = PremiumSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("记忆深潜 (Context Limits): $memoryLength", color = PremiumTextPrimary, fontWeight = FontWeight.Bold)
                        Slider(
                            value = memoryLength.toFloat(),
                            onValueChange = { memoryLength = it.toInt() },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = PremiumPrimary, activeTrackColor = PremiumPrimary, inactiveTrackColor = PremiumHighlight)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = PremiumSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("流式超导 (Stream)", color = PremiumTextPrimary, fontWeight = FontWeight.Bold)
                            Text("以字符流实时渲染响应矩阵", color = PremiumTextSecondary, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isStreamResponse,
                            onCheckedChange = { isStreamResponse = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PremiumPrimary, uncheckedTrackColor = PremiumBorder, uncheckedThumbColor = PremiumTextSecondary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PremiumTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        color = PremiumSurface
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = PremiumTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = PremiumTextPrimary,
                unfocusedTextColor = PremiumTextPrimary
            )
        )
    }
}

