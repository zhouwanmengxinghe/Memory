package com.example.myapplication1.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication1.data.MemoryEvent
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.utils.ExportImportUtils
import com.example.myapplication1.utils.FileUtils
import com.example.myapplication1.viewmodel.MemoryViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MemoryViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (MemoryEvent) -> Unit,
    onNavigateToDetail: (MemoryEvent) -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToAnniversary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val events by viewModel.allEvents.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val currentEvent by viewModel.currentEvent.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var showDeleteDialog by remember { mutableStateOf<MemoryEvent?>(null) }
    var showRandomEventDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    // 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isImporting = true
                try {
                    val importedData = ExportImportUtils.importMemories(context, it)
                    if (importedData != null) {
                        // 导入成功，更新数据
                        importedData.memories.forEach { memory ->
                            // 更新照片路径为新的本地路径
                            val updatedPhotoPaths = memory.photoPaths.map { photoPath ->
                                val fileName = File(photoPath).name
                                File(FileUtils.getImagesDirectory(context), fileName).absolutePath
                            }
                            
                            // 更新音频路径为新的本地路径
                            val updatedAudioPath = memory.audioPath?.let { audioPath ->
                                val fileName = File(audioPath).name
                                File(FileUtils.getAudioDirectory(context), fileName).absolutePath
                            }
                            
                            val updatedMemory = memory.copy(
                                photoPaths = updatedPhotoPaths,
                                audioPath = updatedAudioPath
                            )
                            
                            viewModel.insertEvent(updatedMemory)
                        }
                        
                        // 注意：纪念日功能需要单独的ViewModel处理
                        // importedData.anniversaries.forEach { anniversary ->
                        //     anniversaryRepository.insertAnniversary(anniversary)
                        // }
                        
                        // 显示成功消息
                        Toast.makeText(context, "导入成功！", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "导入失败，请检查文件格式", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "导入失败：${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }
    
    // 监听随机事件状态
    LaunchedEffect(uiState.showRandomEvent) {
        showRandomEventDialog = uiState.showRandomEvent
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(LavenderBlush, Seashell)
                    )
                )
        ) {
            // 顶部栏
            TopAppBar(
                title = {
                    Text(
                        "我们的回忆 💕",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    // 纪念日按钮
                    IconButton(onClick = onNavigateToAnniversary) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = "纪念日",
                            tint = RomanticPink
                        )
                    }
                    
                    // 随机播放按钮
                    IconButton(
                        onClick = { 
                            if (events.isNotEmpty()) {
                                viewModel.getRandomEvent()
                            }
                        },
                        enabled = events.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "随机回忆",
                            tint = if (events.isNotEmpty()) RomanticPink else Color.Gray
                        )
                    }
                    
                    // 更多选项
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = RomanticPink
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("导出备份") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val backupFile = ExportImportUtils.exportMemories(context, events, emptyList())
                                        backupFile?.let { file ->
                                            // 显示成功消息或Toast
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Download, contentDescription = null)
                            },
                            enabled = !isExporting && events.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("分享备份") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val backupFile = ExportImportUtils.exportMemories(context, events, emptyList())
                                        backupFile?.let { file ->
                                            ExportImportUtils.shareBackupFile(context, file)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            },
                            enabled = !isExporting && events.isNotEmpty()
                        )
                        DropdownMenuItem(
                            text = { Text("导入备份") },
                            onClick = {
                                showMenu = false
                                // 触发文件选择器
                                importLauncher.launch("application/zip")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Upload, contentDescription = null)
                            },
                            enabled = !isExporting && !isImporting
                        )
                        DropdownMenuItem(
                            text = { Text("导出文本") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    try {
                                        val textFile = ExportImportUtils.exportToText(context, events, emptyList())
                                        textFile?.let { file ->
                                            ExportImportUtils.shareBackupFile(context, file)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TextSnippet, contentDescription = null)
                            },
                            enabled = events.isNotEmpty()
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("用户管理") },
                            onClick = {
                                showMenu = false
                                onNavigateToUserProfile()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.People, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("清空所有数据") },
                            onClick = {
                                showMenu = false
                                // 这里可以添加确认对话框
                                viewModel.clearAllData()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // 内容区域
            if (events.isEmpty()) {
                EmptyStateView(
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(events) { event ->
                        MemoryEventCard(
                            event = event,
                            onCardClick = { onNavigateToDetail(event) },
                            onEditClick = { onNavigateToEdit(event) },
                            onDeleteClick = { showDeleteDialog = event },
                            onPlayAudio = {
                                event.audioPath?.let { audioPath ->
                                    scope.launch {
                                        if (uiState.isPlayingAudio) {
                                            viewModel.stopAudio()
                                        } else {
                                            viewModel.playAudio(audioPath)
                                        }
                                    }
                                }
                            },
                            isPlayingAudio = uiState.isPlayingAudio
                        )
                    }
                }
            }
        }
        
        // 浮动添加按钮
        FloatingHeartButton(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
    
    // 删除确认对话框
    showDeleteDialog?.let { event ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除回忆") },
            text = { Text("确定要删除「${event.title}」这个回忆吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(event)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = DeepPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 随机事件对话框
    if (showRandomEventDialog && currentEvent != null) {
        AlertDialog(
            onDismissRequest = { 
                showRandomEventDialog = false
                viewModel.hideRandomEvent()
            },
            title = { 
                Text(
                    "💫 随机回忆",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                ) 
            },
            text = {
                Column {
                    Text(
                        currentEvent!!.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RomanticPink
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentEvent!!.message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onNavigateToDetail(currentEvent!!)
                        showRandomEventDialog = false
                        viewModel.hideRandomEvent()
                    }
                ) {
                    Text("查看详情", color = RomanticPink)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRandomEventDialog = false
                        viewModel.hideRandomEvent()
                    }
                ) {
                    Text("关闭")
                }
            }
        )
    }
}