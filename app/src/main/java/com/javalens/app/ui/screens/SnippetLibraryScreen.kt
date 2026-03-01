package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.data.SnippetEntity
import com.javalens.app.ui.components.SnippetCard
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetLibraryScreen(
    snippets: List<SnippetEntity>,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // AI-Powered search: filters based on title, category, or AI-generated description
    val filteredSnippets = remember(searchQuery, snippets) {
        if (searchQuery.isBlank()) snippets
        else snippets.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
    ) {
        // Vault Header
        Text(
            text = "SNIPPET VAULT",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Search Bar (Cyber Slate Look)
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search snippets (e.g., 'API', 'Security')", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonIndigo) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CyberSlate,
                unfocusedContainerColor = CyberSlate,
                focusedIndicatorColor = NeonIndigo,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NeonIndigo,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Snippet List
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredSnippets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No snippets found in the vault.", color = Color.Gray)
                    }
                }
            } else {
                items(filteredSnippets) { snippet ->
                    SnippetCard(snippet = snippet)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
