package com.example.myapplication1.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication1.data.MemoryEvent
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.utils.FileUtils
import com.example.myapplication1.viewmodel.MemoryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEditEventScreen(
    viewModel: MemoryViewModel,
    editingEvent: MemoryEvent? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 表单状态
    var title by remember { mutableStateOf(editingEvent?.title ?: "") }
    var message by remember { mutableStateOf(editingEvent?.message ?: "") }
    var selectedDate by remember { mutableStateOf(editingEvent?.date ?: LocalDate.now()) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentImagePaths by remember { mutableStateOf(editingEvent?.photoPaths ?: emptyList()) }
    var currentAudioPath by remember { mutableStateOf(editingEvent?.audioPath) }
    
    // 权限
    val recordPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val readImagesPermission = rememberPermissionState(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )
    
    // 权限申请状态
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf("") }
    
    // 图片选择器 - 支持多张照片
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImageUris = uris
    }
    
    // 日期选择器
    val dateDialogState = rememberMaterialDialogState()
    
    // 保存事件
    fun saveEvent() {
        if (title.isBlank() || message.isBlank()) return
        
        scope.launch {
            try {
                // 处理多张图片
                val newImagePaths = selectedImageUris.mapNotNull { uri ->
                    FileUtils.saveImageFromUri(context, uri)
                }
                val finalImagePaths = currentImagePaths + newImagePaths
                
                val event = if (editingEvent != null) {
                    editingEvent.copy(
                        title = title,
                        message = message,
                        date = selectedDate,
                        photoPaths = finalImagePaths,
                        audioPath = currentAudioPath
                    )
                } else {
                    MemoryEvent(
                        title = title,
                        message = message,
                        date = selectedDate,
                        photoPaths = finalImagePaths,
                        audioPath = currentAudioPath
                    )
                }
                
                if (editingEvent != null) {
                    viewModel.updateEvent(event)
                } else {
                    viewModel.insertEvent(event)
                }
                
                onNavigateBack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Column(
        modifier = modifier
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
                    if (editingEvent != null) "编辑回忆" else "添加回忆",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                TextButton(
                    onClick = { saveEvent() },
                    enabled = title.isNotBlank() && message.isNotBlank()
                ) {
                    Text(
                        "保存",
                        color = if (title.isNotBlank() && message.isNotBlank()) RomanticPink else Color.Gray,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        // 表单内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("回忆标题") },
                placeholder = { Text("例如：第一次旅行") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RomanticPink,
                    focusedLabelColor = RomanticPink
                )
            )
            
            // 日期选择
            OutlinedTextField(
                value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                onValueChange = { },
                label = { Text("日期") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { dateDialogState.show() },
                trailingIcon = {
                    IconButton(onClick = { dateDialogState.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RomanticPink,
                    focusedLabelColor = RomanticPink
                )
            )
            
            // 留言输入
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("留言") },
                placeholder = { Text("写下你想说的话...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RomanticPink,
                    focusedLabelColor = RomanticPink
                )
            )
            
            // 照片选择
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "添加照片",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Button(
                            onClick = { 
                                when {
                                    readImagesPermission.status.isGranted -> {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                    else -> {
                                        permissionType = "相册"
                                        showPermissionDialog = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftPink
                            )
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择照片")
                        }
                    }
                    
                    // 显示所有照片
                    val allImagePaths = currentImagePaths + selectedImageUris.map { it.toString() }
                    if (allImagePaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 照片网格
                        val chunkedImages = allImagePaths.chunked(2)
                        chunkedImages.forEach { rowImages ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowImages.forEach { imagePath ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    ) {
                                        AsyncImage(
                                            model = imagePath,
                                            contentDescription = "照片",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // 删除按钮
                                        IconButton(
                                            onClick = {
                                                if (imagePath in currentImagePaths) {
                                                    currentImagePaths = currentImagePaths - imagePath
                                                } else {
                                                    val uri = selectedImageUris.find { it.toString() == imagePath }
                                                    uri?.let {
                                                        selectedImageUris = selectedImageUris - it
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(
                                                    Color.Black.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                                .size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "删除",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                
                                // 如果这一行只有一张图片，添加空白占位
                                if (rowImages.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            
                            if (rowImages != chunkedImages.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // 清空所有照片按钮
                        if (allImagePaths.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    selectedImageUris = emptyList()
                                    currentImagePaths = emptyList()
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = DeepPink)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("清空所有照片", color = DeepPink)
                            }
                        }
                    }
                }
            }
            
            // 语音录制
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "录制语音",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 录音按钮
                        Button(
                            onClick = {
                                when {
                                    recordPermission.status.isGranted -> {
                                        if (uiState.isRecording) {
                                            viewModel.stopRecording()
                                            currentAudioPath = uiState.recordingPath
                                        } else {
                                            val audioDir = FileUtils.getAudioDirectory(context)
                                            val fileName = FileUtils.generateAudioFileName()
                                            val audioFile = File(audioDir, fileName)
                                            viewModel.startRecording(audioFile.absolutePath)
                                        }
                                    }
                                    else -> {
                                        permissionType = "录音"
                                        showPermissionDialog = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isRecording) DeepPink else RomanticPink
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uiState.isRecording) "停止录音" else "开始录音")
                        }
                        
                        // 播放按钮（如果有音频）
                        if (currentAudioPath != null && FileUtils.fileExists(currentAudioPath)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        if (uiState.isPlayingAudio) {
                                            viewModel.stopAudio()
                                        } else {
                                            viewModel.playAudio(currentAudioPath!!)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftPink
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (uiState.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (uiState.isPlayingAudio) "暂停" else "播放")
                            }
                        }
                    }
                    
                    // 删除音频按钮
                    if (currentAudioPath != null && FileUtils.fileExists(currentAudioPath)) {
                        TextButton(
                            onClick = {
                                FileUtils.deleteFile(currentAudioPath)
                                currentAudioPath = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = DeepPink)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("删除语音", color = DeepPink)
                        }
                    }
                    
                    // 录音状态提示
                    if (uiState.isRecording) {
                        Text(
                            "🎤 正在录音...",
                            color = DeepPink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    
    // 日期选择对话框
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }
    ) {
        datepicker(
            initialDate = selectedDate,
            title = "选择日期"
        ) { date ->
            selectedDate = date
        }
    }
    
    // 权限说明对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要${permissionType}权限") },
            text = { 
                Text(
                    when (permissionType) {
                        "相册" -> "为了让您能够选择照片记录美好回忆，我们需要访问您的相册权限。"
                        "录音" -> "为了让您能够录制语音留言，我们需要录音权限。"
                        else -> "应用需要相应权限才能正常工作。"
                    }
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        when (permissionType) {
                            "相册" -> readImagesPermission.launchPermissionRequest()
                            "录音" -> recordPermission.launchPermissionRequest()
                        }
                    }
                ) {
                    Text("授权", color = RomanticPink)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}