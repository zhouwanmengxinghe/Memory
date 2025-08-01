package com.example.myapplication1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication1.data.User
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.viewmodel.UserViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by userViewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val allUsers by userViewModel.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showUserNameDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        userViewModel.initializeDefaultUsers()
    }
    
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
                "用户管理",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepPink
            )
            
            IconButton(onClick = { showAddUserDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "添加用户", tint = RomanticPink)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 当前用户信息
        currentUser?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightPink)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 头像
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.avatar != null) {
                            AsyncImage(
                                model = user.avatar,
                                contentDescription = "头像",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = RomanticPink),
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "当前用户",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepPink
                        )
                    }
                    
                    IconButton(onClick = { 
                        newUserName = user.name
                        showUserNameDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = RomanticPink)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 其他用户列表
        Text(
            "共同管理用户",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allUsers.filter { !it.isCurrentUser }) { user ->
                UserItem(
                    user = user,
                    onSetAsCurrent = { userViewModel.setCurrentUser(user.id) },
                    onDelete = { userViewModel.deleteUser(user) }
                )
            }
        }
    }
    
    // 添加用户对话框
    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("添加共同管理用户") },
            text = {
                Column {
                    Text("请输入用户名称：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        placeholder = { Text("例如：女朋友的名字") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RomanticPink,
                            focusedLabelColor = RomanticPink
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUserName.isNotBlank()) {
                            userViewModel.addUser(newUserName)
                            newUserName = ""
                            showAddUserDialog = false
                        }
                    }
                ) {
                    Text("添加", color = RomanticPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddUserDialog = false
                    newUserName = ""
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 编辑用户名对话框
    if (showUserNameDialog) {
        AlertDialog(
            onDismissRequest = { showUserNameDialog = false },
            title = { Text("编辑用户名") },
            text = {
                OutlinedTextField(
                    value = newUserName,
                    onValueChange = { newUserName = it },
                    placeholder = { Text("输入新的用户名") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RomanticPink,
                        focusedLabelColor = RomanticPink
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUserName.isNotBlank()) {
                            currentUser?.let { user ->
                                userViewModel.updateUser(user.copy(name = newUserName))
                            }
                            showUserNameDialog = false
                        }
                    }
                ) {
                    Text("保存", color = RomanticPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserNameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onSetAsCurrent: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = SoftPink),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    "共同管理用户",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            // 操作按钮
            Row {
                IconButton(onClick = onSetAsCurrent) {
                    Icon(
                        Icons.Default.PersonPin, 
                        contentDescription = "设为当前用户",
                        tint = RomanticPink
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "删除",
                        tint = DeepPink
                    )
                }
            }
        }
    }
}