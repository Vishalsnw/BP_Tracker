package com.bptracker.ui.screens.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bptracker.data.model.ArticleCategory
import com.bptracker.data.model.HealthArticle
import com.bptracker.data.model.HealthArticlesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    onArticleClick: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ArticleCategory?>(null) }
    
    val filteredArticles = remember(selectedCategory) {
        if (selectedCategory == null) {
            HealthArticlesData.articles
        } else {
            HealthArticlesData.articles.filter { it.category == selectedCategory }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Articles", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Learn how to manage your blood pressure and maintain a healthy lifestyle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") }
                        )
                    }
                    items(ArticleCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { 
                                selectedCategory = if (selectedCategory == category) null else category 
                            },
                            label = { Text(category.label) }
                        )
                    }
                }
            }
            
            items(filteredArticles) { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article.id) }
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: HealthArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getCategoryIcon(article.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = article.category.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text("Read More")
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: ArticleCategory) = when (category) {
    ArticleCategory.DIET -> Icons.Filled.Restaurant
    ArticleCategory.EXERCISE -> Icons.Filled.FitnessCenter
    ArticleCategory.LIFESTYLE -> Icons.Filled.Spa
    ArticleCategory.MEDICATION -> Icons.Filled.Medication
    ArticleCategory.STRESS -> Icons.Filled.SelfImprovement
    ArticleCategory.UNDERSTANDING -> Icons.Filled.Psychology
}
