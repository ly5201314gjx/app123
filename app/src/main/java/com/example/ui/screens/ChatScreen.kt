package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import com.example.data.MessageEntity
import com.example.ui.AgentViewModel
import com.example.ui.theme.*

import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.example.settings.GlobalSettings
import com.example.settings.AppThemeSettings

@Composable
fun ChatScreen(viewModel: AgentViewModel, insetsPadding: PaddingValues) {
    val messages by viewModel.allMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingMessage by viewModel.streamingMessage.collectAsState()
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val themeSettings by remember { GlobalSettings.getSettings(context) }.collectAsState(initial = AppThemeSettings())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBg)
    ) {
        // Global Background Image
        if (themeSettings.bgImageUri != null) {
            AsyncImage(
                model = Uri.parse(themeSettings.bgImageUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = themeSettings.bgAlpha
                    }
                    .then(
                        if (themeSettings.bgBlur > 0f) Modifier.blur(themeSettings.bgBlur.dp) else Modifier
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            // App Bar
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LGX AI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = 1.sp,
                        color = PremiumPrimary
                    )
                    TextButton(
                        onClick = { viewModel.clearChat() },
                        colors = ButtonDefaults.textButtonColors(contentColor = PremiumTextSecondary)
                    ) {
                        Text("清空记忆", fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = insetsPadding.calculateBottomPadding() + 100.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(msg, themeSettings)
                }
                if (streamingMessage != null) {
                    item {
                        MessageBubble(MessageEntity(role = "assistant", content = streamingMessage!!), themeSettings)
                    }
                }
                if (isGenerating && streamingMessage == null) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = PremiumPrimary,
                            trackColor = PremiumHighlight
                        )
                    }
                }
            }
        }
        
        var showToolsMenu by remember { mutableStateOf(false) }

        // Floating Input Area Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = insetsPadding.calculateBottomPadding() + 16.dp)
                .padding(horizontal = 24.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -5f) {
                            viewModel.isBottomBarVisible = true
                        } else if (dragAmount > 5f) {
                            viewModel.isBottomBarVisible = false
                        }
                    }
                }
        ) {
            Column(horizontalAlignment = Alignment.End) {
                // Tools Popup Menu
                AnimatedVisibility(
                    visible = showToolsMenu,
                    enter = expandVertically(animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )) + fadeIn(),
                    exit = shrinkVertically(animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    )) + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = PremiumSurface.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("快捷调用工具链", color = PremiumPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                items(com.example.ui.screens.coreAgentTools.take(5)) { tool ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { 
                                                input += "<tool_call name='${tool.id}'></tool_call>"
                                                showToolsMenu = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(tool.icon, contentDescription = null, tint = PremiumTextSecondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(tool.name, color = PremiumTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = PremiumSurface.copy(alpha = 0.85f),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showToolsMenu = !showToolsMenu },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PremiumHighlight.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (showToolsMenu) Icons.Default.ExpandMore else Icons.Default.Build,
                                contentDescription = "工具链",
                                tint = PremiumPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        TextField(
                        value = input,
                        onValueChange = { input = it },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            viewModel.sendMessage(input)
                            input = ""
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(32.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = PremiumTextPrimary,
                            unfocusedTextColor = PremiumTextPrimary
                        ),
                        placeholder = { Text("输入指令或问题...", color = PremiumTextSecondary) }
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(input)
                            input = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (input.isNotBlank()) Brush.linearGradient(listOf(PremiumPrimary, PremiumGradientEnd)) else SolidColor(PremiumHighlight)
                            ),
                        enabled = !isGenerating && input.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (input.isNotBlank()) Color.White else PremiumTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp).padding(start = 2.dp) // Optical alignment
                        )
                    }
                } // End Row
            } // End Surface
            } // End Column
        } // End Box (Floating Input)
    } // End Box (Outer)
}

@Composable
fun MessageContentParser(content: String, color: Color, isUser: Boolean, collapseThinkDefault: Boolean) {
    val regex = "(?s)<think>(.*?)</think>|(?s)<tool_call\\s+name=[\"'](.*?)[\"']>(.*?)</tool_call>".toRegex()
    var currentIndex = 0
    
    Column {
        val matches = regex.findAll(content)
        for (match in matches) {
            val preText = content.substring(currentIndex, match.range.first)
            if (preText.isNotBlank()) {
                Text(
                    text = preText.trim(),
                    color = color,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            val thinkContent = match.groups[1]?.value
            val toolName = match.groups[2]?.value
            val toolArgs = match.groups[3]?.value
            
            if (thinkContent != null) {
                var isExpanded by remember { mutableStateOf(!collapseThinkDefault) }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUser) Color.White.copy(alpha = 0.15f) else PremiumHighlight.copy(alpha = 0.5f),
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("思考过程 (Think)", color = color.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Think",
                                tint = color.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )),
                            exit = shrinkVertically(animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                            ))
                        ) {
                            Text(
                                text = thinkContent.trim(),
                                color = color.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else if (toolName != null && toolArgs != null) {
                var isExpanded by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = PremiumPrimary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PremiumPrimary.copy(alpha = 0.3f)),
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = PremiumPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("调用工具: $toolName", color = PremiumPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Tool",
                                tint = PremiumPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )),
                            exit = shrinkVertically(animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                            ))
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text(
                                    text = toolArgs.trim(),
                                    color = Color.Green.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            currentIndex = match.range.last + 1
        }
        
        val postText = content.substring(currentIndex)
        if (postText.isNotBlank()) {
            Text(
                text = postText.trim(),
                color = color,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(msg: MessageEntity, settings: AppThemeSettings) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(listOf(PremiumPrimary, PremiumGradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        
        val bubbleShape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = if (isUser) 24.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 24.dp
        )
        
        when (settings.bubbleStyle) {
            "plain" -> {
                Box(
                    modifier = Modifier.widthIn(max = 280.dp).padding(16.dp)
                ) {
                    MessageContentParser(msg.content, Color(settings.bubbleTextColor), isUser, settings.collapseThink)
                }
            }
            "custom_image" -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(bubbleShape)
                ) {
                    if (settings.bubbleImageUri != null) {
                        AsyncImage(
                            model = Uri.parse(settings.bubbleImageUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().graphicsLayer { alpha = settings.bubbleAlpha }
                        )
                    } else {
                        Spacer(modifier = Modifier.matchParentSize().background(if (isUser) PremiumPrimary else PremiumSurface))
                    }
                    Box(modifier = Modifier.padding(16.dp)) {
                        MessageContentParser(msg.content, if (isUser) Color.White else PremiumTextPrimary, isUser, settings.collapseThink)
                    }
                }
            }
            else -> { // "glass"
                Surface(
                    shape = bubbleShape,
                    color = (if (isUser) PremiumPrimary else PremiumSurface).copy(alpha = 0.70f),
                    shadowElevation = if (isUser) 0.dp else 4.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        MessageContentParser(msg.content, if (isUser) Color.White else PremiumTextPrimary, isUser, settings.collapseThink)
                    }
                }
            }
        }
    }
}

