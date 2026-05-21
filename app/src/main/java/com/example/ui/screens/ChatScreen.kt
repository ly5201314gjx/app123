package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
        
        // Floating Input Area Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = insetsPadding.calculateBottomPadding() + 16.dp)
                .padding(horizontal = 24.dp)
        ) {
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
                }
            }
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
                    Text(
                        text = msg.content,
                        color = Color(settings.bubbleTextColor),
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
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
                    Text(
                        text = msg.content,
                        color = if (isUser) Color.White else PremiumTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> { // "glass"
                Surface(
                    shape = bubbleShape,
                    color = (if (isUser) PremiumPrimary else PremiumSurface).copy(alpha = 0.70f),
                    shadowElevation = if (isUser) 0.dp else 4.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Text(
                        text = msg.content,
                        color = if (isUser) Color.White else PremiumTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

