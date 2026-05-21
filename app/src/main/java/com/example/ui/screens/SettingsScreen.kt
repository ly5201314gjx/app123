package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PremiumCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(insetsPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBg)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "功能矩阵",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                letterSpacing = 1.sp,
                color = PremiumTextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = insetsPadding.calculateBottomPadding() + 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("管理枢纽", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PremiumTextSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            PremiumCard(
                title = "工具链管理",
                subtitle = "配置与桥接外部执行工具",
                icon = Icons.Default.Build
            )
            PremiumCard(
                title = "MCP 控制规范",
                subtitle = "管理模型控制协议与安全域",
                icon = Icons.Default.Storage
            )
            PremiumCard(
                title = "技能网络",
                subtitle = "神经技能训练与激活检视",
                icon = Icons.Default.Extension
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("底层架构", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PremiumTextSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            
            PremiumCard(
                title = "终端直连",
                subtitle = "底层 CLI 绑定与后台权限交互",
                icon = Icons.Default.Terminal
            )
            PremiumCard(
                title = "依赖包配置",
                subtitle = "安装与热更新代理生态插件",
                icon = Icons.Default.Code
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
