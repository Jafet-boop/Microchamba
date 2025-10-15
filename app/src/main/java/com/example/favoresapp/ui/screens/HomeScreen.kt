
package com.example.favoresapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPublish: () -> Unit = {},
    onNavigateToFavorList: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    var showContent by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
    }

    val bottomNavItems = remember {
        listOf(
            BottomNavItem(
                title = "Publicar",
                icon = Icons.Default.Add,
                color = Color(0xFF667eea),
                onClick = onNavigateToPublish
            ),
            BottomNavItem(
                title = "Servicios",
                icon = Icons.Default.Search,
                color = Color(0xFF36d1dc),
                onClick = onNavigateToFavorList
            ),
            BottomNavItem(
                title = "Chat",
                icon = Icons.Default.Email,
                color = Color(0xFFf093fb),
                onClick = onNavigateToChat
            ),
            BottomNavItem(
                title = "Perfil",
                icon = Icons.Default.Person,
                color = Color(0xFF38A169),
                onClick = onNavigateToProfile
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(
                            animationSpec = tween(600),
                            initialOffsetY = { -it / 3 }
                        )
            ) {
                HeaderSection(onNavigateToNotifications = onNavigateToNotifications)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Stats Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(800, delayMillis = 200)
                ) + slideInVertically(
                    animationSpec = tween(800, delayMillis = 200),
                    initialOffsetY = { it / 2 }
                )
            ) {
                QuickStatsCard()
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Contenido adicional
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(800, delayMillis = 400)
                )
            ) {
                RecentActivitySection()
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom Navigation
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = tween(800, delayMillis = 600)
            ) + slideInVertically(
                animationSpec = tween(800, delayMillis = 600),
                initialOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(items = bottomNavItems)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderSection(
    onNavigateToNotifications: () -> Unit,
    notificationsViewModel: NotificationsViewModel = viewModel()
) {
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "¡Hola! 👋",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¿Cómo puedes ayudar hoy?",
                fontSize = 18.sp,
                color = Color(0xFF718096),
                fontWeight = FontWeight.Medium
            )
        }
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge { Text("$unreadCount") }
                }
            }
        ) {
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones"
                )
            }
        }
    }
}

@Composable
private fun QuickStatsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Tu Actividad",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Favorite,
                    value = "0",
                    label = "Favores\nRealizados",
                    iconColor = Color(0xFFE53E3E)
                )

                StatItem(
                    icon = Icons.Default.Star,
                    value = "0.0",
                    label = "Calificación\nPromedio",
                    iconColor = Color(0xFFD69E2E)
                )

                StatItem(
                    icon = Icons.Default.ThumbUp,
                    value = "0",
                    label = "Personas\nAyudadas",
                    iconColor = Color(0xFF38A169)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    iconColor.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A202C)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun RecentActivitySection() {
    Column {
        Text(
            text = "Actividad Reciente",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A202C)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.8f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF718096),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¡Empieza a ayudar!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A202C)
                    )

                    Text(
                        text = "Publica tu primer servicio o explora lo que otros necesitan",
                        fontSize = 14.sp,
                        color = Color(0xFF718096),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    items: List<BottomNavItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(25.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                BottomNavButton(
                    item = item,
                    index = index
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    index: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    item.onClick()
                }
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.2f),
                            item.color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center
        )
    }

    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}
