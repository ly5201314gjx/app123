package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsEntity
import com.example.ui.AgentViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(viewModel: AgentViewModel, insetsPadding: PaddingValues) {
    val allSettings by viewModel.allSettings.collectAsState()
    val activeSettingsId by viewModel.activeSettingsId.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = PremiumBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveSettings(SettingsEntity(modelNameDisplay = "New Model"))
                },
                containerColor = PremiumPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Model")
            }
        }
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
                    text = "Engine Matrix",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    letterSpacing = 1.sp,
                    color = PremiumTextPrimary
                )
            }

            val groupedSettings = allSettings.groupBy { it.groupName }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = insetsPadding.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedSettings.forEach { (groupName, settingsInGroup) ->
                    item {
                        Text(groupName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PremiumPrimary, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp))
                    }
                    items(settingsInGroup, key = { it.id }) { setting ->
                        ModelAccordionItem(
                            settingsGroup = setting,
                            isActive = setting.id == activeSettingsId,
                            onActivate = { viewModel.setActiveSettingsId(setting.id) },
                            onSave = { updated -> viewModel.saveSettings(updated) },
                            onDelete = { viewModel.deleteSettings(setting) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelAccordionItem(
    settingsGroup: SettingsEntity,
    isActive: Boolean,
    onActivate: () -> Unit,
    onSave: (SettingsEntity) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    var groupName by remember(settingsGroup) { mutableStateOf(settingsGroup.groupName) }
    var modelNameDisplay by remember(settingsGroup) { mutableStateOf(settingsGroup.modelNameDisplay) }
    var activeModel by remember(settingsGroup) { mutableStateOf(settingsGroup.activeModel) }
    var baseUrl by remember(settingsGroup) { mutableStateOf(settingsGroup.baseUrl) }
    var apiKey by remember(settingsGroup) { mutableStateOf(settingsGroup.apiKey) }
    var temperature by remember(settingsGroup) { mutableStateOf(settingsGroup.temperature) }
    var maxTokens by remember(settingsGroup) { mutableStateOf(settingsGroup.maxTokens) }
    var memoryLength by remember(settingsGroup) { mutableStateOf(settingsGroup.memoryLength) }
    var isStreamResponse by remember(settingsGroup) { mutableStateOf(settingsGroup.isStreamResponse) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) PremiumHighlight else PremiumSurface,
        shadowElevation = if (isActive) 8.dp else 2.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = PremiumPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = modelNameDisplay.ifBlank { "Unnamed Engine" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PremiumTextPrimary
                        )
                        if (isActive) {
                            Text("● Active Runtime", color = PremiumPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Inactive", color = PremiumTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isActive) {
                        TextButton(onClick = onActivate) {
                            Text("Activate", color = PremiumPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = PremiumTextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                ) {
                    HorizontalDivider(color = PremiumBorder, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

                    CompactTextField(label = "Group Name", value = groupName, onValueChange = { groupName = it })
                    CompactTextField(label = "Display Name", value = modelNameDisplay, onValueChange = { modelNameDisplay = it })
                    CompactTextField(label = "Engine Source (Model ID)", value = activeModel, onValueChange = { activeModel = it })
                    CompactTextField(label = "Base URL", value = baseUrl, onValueChange = { baseUrl = it })
                    CompactTextField(label = "API Key", value = apiKey, onValueChange = { apiKey = it }, isPassword = true)

                    Spacer(modifier = Modifier.height(16.dp))

                    CompactSliderRow(label = "Temperature", value = temperature, range = 0f..2f, onValueChange = { temperature = it })
                    CompactSliderRow(label = "Context Limits (Tokens)", value = maxTokens.toFloat(), range = 100f..32000f, onValueChange = { maxTokens = it.toInt() }, displayValue = maxTokens.toString())
                    CompactSliderRow(label = "Memory Depth (Turns)", value = memoryLength.toFloat(), range = 0f..100f, onValueChange = { memoryLength = it.toInt() }, displayValue = memoryLength.toString())

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Streaming Render", color = PremiumTextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = isStreamResponse,
                            onCheckedChange = { isStreamResponse = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PremiumPrimary, uncheckedTrackColor = PremiumBorder, uncheckedThumbColor = PremiumTextSecondary),
                            modifier = Modifier.scale(0.8f)      
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                        
                        Button(
                            onClick = {
                                onSave(
                                    settingsGroup.copy(
                                        groupName = groupName,
                                        modelNameDisplay = modelNameDisplay,
                                        activeModel = activeModel,
                                        baseUrl = baseUrl,
                                        apiKey = apiKey,
                                        temperature = temperature,
                                        maxTokens = maxTokens,
                                        memoryLength = memoryLength,
                                        isStreamResponse = isStreamResponse
                                    )
                                )
                                isExpanded = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text("Apply", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, color = PremiumTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PremiumBg, // Darker inner bg
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                if (value.isEmpty()) Text("...", color = PremiumTextSecondary.copy(alpha = 0.5f))
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = androidx.compose.ui.text.TextStyle(color = PremiumTextPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun CompactSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    displayValue: String = String.format("%.2f", value)
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = PremiumTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            Text(displayValue, color = PremiumTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = PremiumPrimary, 
                activeTrackColor = PremiumPrimary, 
                inactiveTrackColor = PremiumBg
            ),
            modifier = Modifier.padding(horizontal = 4.dp).height(24.dp)
        )
    }
}
