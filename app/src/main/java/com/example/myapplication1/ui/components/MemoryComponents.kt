package com.example.myapplication1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication1.data.MemoryEvent
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.utils.FileUtils
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryEventCard(
    event: MemoryEvent,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlayAudio: () -> Unit,
    isPlayingAudio: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = RomanticPink
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = DeepPink
                        )
                    }
                }
            }
            
            Text(
                text = event.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 照片
            if (event.photoPaths.isNotEmpty()) {
                val existingPhotos = event.photoPaths.filter { FileUtils.fileExists(it) }
                if (existingPhotos.isNotEmpty()) {
                    if (existingPhotos.size == 1) {
                        // 单张照片，全宽显示
                        AsyncImage(
                            model = existingPhotos.first(),
                            contentDescription = "回忆照片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 多张照片，网格显示
                        val chunkedPhotos = existingPhotos.take(4).chunked(2) // 最多显示4张
                        chunkedPhotos.forEach { rowPhotos ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPhotos.forEach { photoPath ->
                                    AsyncImage(
                                        model = photoPath,
                                        contentDescription = "回忆照片",
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                
                                // 如果这一行只有一张图片，添加空白占位
                                if (rowPhotos.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            
                            if (rowPhotos != chunkedPhotos.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        // 如果有更多照片，显示提示
                        if (existingPhotos.size > 4) {
                            Text(
                                text = "还有 ${existingPhotos.size - 4} 张照片...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            // 留言
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.padding(bottom = 12.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            // 音频播放按钮
            event.audioPath?.let { audioPath ->
                if (FileUtils.fileExists(audioPath)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LightPink, SoftPink)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onPlayAudio() }
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingAudio) "暂停" else "播放",
                            tint = TextLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingAudio) "正在播放..." else "播放语音",
                            color = TextLight,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingHeartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = RomanticPink,
        contentColor = TextLight,
        shape = CircleShape
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = "添加回忆",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = SoftPink
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有回忆哦",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Light
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角的爱心按钮\n开始记录你们的美好时光吧 💕",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}