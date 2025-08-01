package com.example.myapplication1.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication1.data.Anniversary
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.viewmodel.AnniversaryViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnniversaryViewModel = hiltViewModel()
) {
    val anniversaries by viewModel.allAnniversaries.collectAsStateWithLifecycle(initialValue = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAnniversary by remember { mutableStateOf<Anniversary?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            
            Text(
                "纪念日",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepPink
            )
            
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加纪念日")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (anniversaries.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = SoftPink
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "还没有纪念日",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击右上角的 + 号添加第一个纪念日",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight
                    )
                }
            }
        } else {
            // 纪念日列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(anniversaries) { anniversary ->
                    AnniversaryCard(
                        anniversary = anniversary,
                        daysSince = viewModel.calculateDaysSince(anniversary.date),
                        onEditClick = {
                            selectedAnniversary = anniversary
                            viewModel.updateEditingAnniversary(anniversary)
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedAnniversary = anniversary
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // 添加纪念日对话框
    if (showAddDialog) {
        AddEditAnniversaryDialog(
            anniversary = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { anniversary ->
                viewModel.insertAnniversary(anniversary)
                showAddDialog = false
            }
        )
    }
    
    // 编辑纪念日对话框
    if (showEditDialog && selectedAnniversary != null) {
        AddEditAnniversaryDialog(
            anniversary = selectedAnniversary,
            onDismiss = { 
                showEditDialog = false
                selectedAnniversary = null
                viewModel.updateEditingAnniversary(null)
            },
            onConfirm = { anniversary ->
                viewModel.updateAnniversary(anniversary)
                showEditDialog = false
                selectedAnniversary = null
                viewModel.updateEditingAnniversary(null)
            }
        )
    }
    
    // 删除确认对话框
    if (showDeleteDialog && selectedAnniversary != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedAnniversary = null
            },
            title = { Text("删除纪念日") },
            text = { Text("确定要删除「${selectedAnniversary!!.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAnniversary(selectedAnniversary!!)
                        showDeleteDialog = false
                        selectedAnniversary = null
                    }
                ) {
                    Text("删除", color = DeepPink)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedAnniversary = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversaryCard(
    anniversary: Anniversary,
    daysSince: Long,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        anniversary.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepPink
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        anniversary.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight
                    )
                    
                    if (anniversary.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            anniversary.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = SoftPink
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 天数显示
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = RomanticPink.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (daysSince >= 0) {
                        Text(
                            "已经过去 $daysSince 天",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = RomanticPink
                        )
                    } else {
                        Text(
                            "还有 ${-daysSince} 天",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = RomanticPink
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditAnniversaryDialog(
    anniversary: Anniversary?,
    onDismiss: () -> Unit,
    onConfirm: (Anniversary) -> Unit
) {
    var title by remember { mutableStateOf(anniversary?.title ?: "") }
    var description by remember { mutableStateOf(anniversary?.description ?: "") }
    var selectedDate by remember { mutableStateOf(anniversary?.date ?: LocalDate.now()) }
    var isImportant by remember { mutableStateOf(anniversary?.isImportant ?: false) }
    
    val dateDialogState = rememberMaterialDialogState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (anniversary == null) "添加纪念日" else "编辑纪念日") 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("纪念日名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    onValueChange = { },
                    label = { Text("日期") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { dateDialogState.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it }
                    )
                    Text("重要纪念日")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val newAnniversary = Anniversary(
                            id = anniversary?.id ?: 0,
                            title = title,
                            date = selectedDate,
                            description = description,
                            isImportant = isImportant
                        )
                        onConfirm(newAnniversary)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
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
}