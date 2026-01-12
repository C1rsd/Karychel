package com.karychel.app.ui.library

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.karychel.app.database.MangaEntity
import com.karychel.app.database.MangaRepository
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pantalla principal de la Biblioteca.
 * Muestra los mangas en un grid de 3 columnas con estilo Glassmorphism y efecto líquido.
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(
            MangaRepository(LocalContext.current)
        )
    )
) {
    val mangas by viewModel.mangas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estado del scroll para efecto líquido reactivo
    val scrollState = rememberLazyGridState()
    val scrollOffset = remember { derivedStateOf { scrollState.firstVisibleItemIndex } }
    
    // Estado para el efecto líquido base
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_effect")
    val baseLiquidOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquid_offset"
    )
    
    // Combinar offset base con scroll para efecto reactivo
    val liquidOffset = derivedStateOf {
        (baseLiquidOffset + scrollOffset.value * 0.01f) % 1f
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Efecto líquido de fondo
        LiquidBackgroundEffect(
            modifier = Modifier.fillMaxSize(),
            offset = liquidOffset
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Biblioteca",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x1AFFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error ?: "",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                mangas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay mangas en tu biblioteca",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = scrollState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(mangas) { manga ->
                            MangaCard(
                                manga = manga,
                                onStatusChange = { status ->
                                    viewModel.updateReadingStatus(manga.id, status)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de manga con estilo Glassmorphism.
 */
@Composable
fun MangaCard(
    manga: MangaEntity,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Fondo Glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x1AFFFFFF), // 10% opacidad
                                Color(0x0DFFFFFF)  // 5% opacidad
                            )
                        )
                    )
                    .blur(radius = 1.dp)
            )

            // Borde sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x33FFFFFF), // 20% opacidad
                                Color(0x1AFFFFFF)  // 10% opacidad
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(100f, 100f)
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Portada del manga
                AsyncImage(
                    model = manga.coverUrl ?: "",
                    contentDescription = manga.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_menu_report_image),
                    placeholder = painterResource(android.R.drawable.ic_menu_gallery)
                )

                // Información del manga
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x1AFFFFFF),
                                    Color(0x0DFFFFFF)
                                )
                            )
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    manga.author?.let { author ->
                        Text(
                            text = author,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Efecto líquido de fondo usando shaders y animaciones.
 * Crea una animación fluida que responde al scroll y las interacciones.
 */
@Composable
fun LiquidBackgroundEffect(
    modifier: Modifier = Modifier,
    offset: Float
) {
    // Múltiples capas para efecto líquido más complejo
    val layer1Offset = offset
    val layer2Offset = (offset + 0.3f) % 1f
    val layer3Offset = (offset + 0.6f) % 1f

    Box(modifier = modifier) {
        // Capa 1 - Movimiento principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x0AFFFFFF),
                            Color(0x05FFFFFF),
                            Color(0x00FFFFFF)
                        ),
                        center = Offset(
                            x = 0.3f + (sin(layer1Offset * Math.PI * 2).toFloat() * 0.2f),
                            y = 0.3f + (cos(layer1Offset * Math.PI * 2).toFloat() * 0.2f)
                        ),
                        radius = 1000f
                    )
                )
        )

        // Capa 2 - Movimiento secundario
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x08FFFFFF),
                            Color(0x03FFFFFF),
                            Color(0x00FFFFFF)
                        ),
                        center = Offset(
                            x = 0.7f + (sin(layer2Offset * Math.PI * 2).toFloat() * 0.15f),
                            y = 0.7f + (cos(layer2Offset * Math.PI * 2).toFloat() * 0.15f)
                        ),
                        radius = 800f
                    )
                )
        )

        // Capa 3 - Movimiento terciario (más sutil)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x05FFFFFF),
                            Color(0x02FFFFFF),
                            Color(0x00FFFFFF)
                        ),
                        center = Offset(
                            x = 0.5f + (sin(layer3Offset * Math.PI * 2).toFloat() * 0.1f),
                            y = 0.5f + (cos(layer3Offset * Math.PI * 2).toFloat() * 0.1f)
                        ),
                        radius = 600f
                    )
                )
        )
    }
}
