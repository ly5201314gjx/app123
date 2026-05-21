package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class AgentTool(val id: String, val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val description: String)

val coreAgentTools = listOf(
    AgentTool("web_search", "Web Search", Icons.Default.Search, "Search the public web for real-time information."),
    AgentTool("read_file", "Read File", Icons.Default.InsertDriveFile, "Read contents of a specific file."),
    AgentTool("write_file", "Write File", Icons.Default.Edit, "Write content to a file."),
    AgentTool("run_code", "Run Code", Icons.Default.Code, "Execute Python/Node.js code in a sandbox."),
    AgentTool("shell_cmd", "Shell Command", Icons.Default.Terminal, "Execute safe shell commands."),
    AgentTool("database_query", "SQL Query", Icons.Default.Storage, "Execute queries against standard databases."),
    AgentTool("calculator", "Calculator", Icons.Default.Calculate, "Evaluate complex mathematical expressions."),
    AgentTool("send_email", "Send Email", Icons.Default.Email, "Draft and send emails via SMTP."),
    AgentTool("calendar_sync", "Calendar Integration", Icons.Default.Event, "Read & write calendar events."),
    AgentTool("image_gen", "Image Generation", Icons.Default.Image, "Generate images from text prompts."),
    AgentTool("fetch_url", "Fetch URL Content", Icons.Default.Link, "Extract text from a specific URL."),
    AgentTool("github_api", "GitHub API", Icons.Default.AccountTree, "Create PRs, read issues, manage repos."),
    AgentTool("git_commit", "Git Commit", Icons.Default.Commit, "Commit and push local repository changes."),
    AgentTool("weather_api", "Weather Info", Icons.Default.Cloud, "Get current and forecasted weather."),
    AgentTool("translate", "Translation Service", Icons.Default.Translate, "Translate text between languages."),
    AgentTool("jira_ticket", "Jira Management", Icons.Default.BugReport, "Create or transition Jira tickets."),
    AgentTool("slack_msg", "Slack Notifier", Icons.Default.Chat, "Post messages to Slack channels."),
    AgentTool("maps_api", "Maps & Routing", Icons.Default.Map, "Calculate directions and location context."),
    AgentTool("vision_analysis", "Vision Analysis", Icons.Default.Visibility, "Analyze images and detect objects."),
    AgentTool("pdf_reader", "PDF Parser", Icons.Default.PictureAsPdf, "Extract text and tables from PDFs."),
    AgentTool("system_info", "System Metrics", Icons.Default.Memory, "Read CPU, RAM, and disk usage."),
    AgentTool("docker_mgr", "Docker Engine", Icons.Default.ViewInAr, "Manage local docker containers."),
    AgentTool("k8s_control", "Kubernetes Ctrl", Icons.Default.DeviceHub, "Interact with K8s clusters."),
    AgentTool("aws_lambda", "Deploy Lambda", Icons.Default.CloudUpload, "Deploy code to AWS serverless."),
    AgentTool("notion_api", "Notion Workspace", Icons.Default.Notes, "Read and write Notion pages/databases."),
    AgentTool("youtube_transcript", "YT Transcripts", Icons.Default.OndemandVideo, "Fetch video subtitles and metadata."),
    AgentTool("stock_market", "Stock Quotes", Icons.Default.TrendingUp, "Get real-time market data ticks."),
    AgentTool("cron_scheduler", "Cron Jobs", Icons.Default.Schedule, "Schedule delayed or recurring tasks."),
    AgentTool("voice_synthesis", "TTS Speech", Icons.Default.RecordVoiceOver, "Convert LLM output to lifelike speech."),
    AgentTool("memory_vault", "Memory Vault", Icons.Default.Lock, "Persist learned facts into secure vault.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsManageScreen(insetsPadding: PaddingValues) {
    Scaffold(
        containerColor = PremiumBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "工具链矩阵 (Toolchain)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp,
                    color = PremiumTextPrimary
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = insetsPadding.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(coreAgentTools) { tool ->
                    ToolRowItem(tool)
                }
            }
        }
    }
}

@Composable
fun ToolRowItem(tool: AgentTool) {
    var isEnabled by remember { mutableStateOf(false) } // In a real app, bind to ViewModel or DB

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PremiumSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isEnabled) PremiumPrimary.copy(alpha = 0.2f) else PremiumHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = tool.name, tint = if (isEnabled) PremiumPrimary else PremiumTextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.name, color = PremiumTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(tool.description, color = PremiumTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PremiumPrimary, uncheckedTrackColor = PremiumBorder)
            )
        }
    }
}
