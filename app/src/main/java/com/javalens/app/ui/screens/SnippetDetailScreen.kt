package com.javalens.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.domain.utils.ClipboardUtils
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetDetailScreen(
    snippetId: Long,
    repository: SnippetRepository,
    onChatWithSnippet: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snippet by repository.getSnippetById(snippetId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SNIPPET DETAIL", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Chat Button
                    IconButton(onClick = {
                        snippet?.let { onChatWithSnippet(it.codeContent) }
                    }) {
                        Icon(Icons.Default.Chat, "Ask AI", tint = NeonIndigo)
                    }

                    // Share Button
                    IconButton(onClick = {
                        snippet?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, it.title)
                                putExtra(Intent.EXTRA_TEXT, it.codeContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Code via"))
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share", tint = Color.Gray)
                    }

                    // Delete Button
                    IconButton(onClick = {
                        scope.launch {
                            repository.deleteSnippetById(snippetId)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        if (snippet == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonIndigo)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    color = CyberSlate,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = snippet!!.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = snippet!!.category.uppercase(),
                            color = NeonIndigo,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = snippet!!.description,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SOURCE CODE", color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { ClipboardUtils.copyToClipboard(context, snippet!!.codeContent) }) {
                        Icon(Icons.Default.ContentCopy, "Copy", tint = NeonEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = snippet!!.codeContent,
                        modifier = Modifier.padding(16.dp),
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
