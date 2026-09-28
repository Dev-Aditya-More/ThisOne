package com.aditya1875.thisone.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aditya1875.thisone.data.model.MemeResult
import com.aditya1875.thisone.ui.theme.*
import com.aditya1875.thisone.util.shareMeme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ThisOne",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = ".",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(Modifier.width(6.dp))
                        BounceEmoji("🎭")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {

            // ── Situation input ────────────────────────────────────────────
            SituationInputSection(
                text = formState.situationText,
                onTextChanged = viewModel::onSituationChanged,
                onFocusChanged = viewModel::onFocusChanged,
                onFind = {
                    focusManager.clearFocus()
                    viewModel.onFindMeme()
                },
                isLoading = uiState is HomeUiState.Matching || uiState is HomeUiState.LoadingTemplates,
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300))) togetherWith
                        (fadeOut(tween(150)) + scaleOut(targetScale = 1.02f, animationSpec = tween(150)))
                },
                label = "home_content",
            ) { state ->
                when (state) {
                    is HomeUiState.Idle -> IdleHint(
                        onSuggestionClick = viewModel::onSituationChanged,
                    )

                    is HomeUiState.LoadingTemplates -> LoadingState()

                    is HomeUiState.Matching -> LoadingState()

                    is HomeUiState.Success -> ResultsList(
                        results = state.results,
                        isSaved = viewModel::isSaved,
                        onSave = viewModel::onSaveMeme,
                        onReset = viewModel::onReset,
                        onCardClick = { result ->
                            viewModel.onMemeClick(result)
                            onNavigateToDetail()
                        },
                    )

                    is HomeUiState.Error -> ErrorState(
                        message = state.message,
                        onRetry = viewModel::onFindMeme,
                    )
                }
            }
        }
    }
}

// ── Little playful bits ──────────────────────────────────────────────────────

@Composable
private fun BounceEmoji(emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce_emoji")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wiggle",
    )
    Text(text = emoji, modifier = Modifier.rotate(rotation), style = MaterialTheme.typography.titleLarge)
}

private val LOADING_LINES = listOf(
    "Consulting the meme council…",
    "Digging through the vault…",
    "Cooking up something unhinged…",
    "Summoning peak comedy…",
    "Vibe-checking every template…",
)

// ── Situation input section ──────────────────────────────────────────────────

private val SITUATION_SUGGESTIONS = listOf(
    "when the meeting could've been an email",
    "when I say \"one more episode\" for the 5th time",
    "when my code works and I don't know why",
    "when someone chews with their mouth open",
    "when the WiFi cuts out mid-boss-fight",
)

@Composable
private fun SituationInputSection(
    text: String,
    onTextChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onFind: () -> Unit,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = "What's the vibe? ✨",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            placeholder = {
                Text(
                    "e.g. when the meeting gets cancelled 10 min in",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onFind() }
            ),
            maxLines = 4,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        BouncyButton(
            onClick = onFind,
            enabled = text.isNotBlank() && !isLoading,
        ) {
            if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "spin")
                val spin by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                    ),
                    label = "spin_angle",
                )
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).rotate(spin),
                )
                Spacer(Modifier.width(10.dp))
            } else {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (isLoading) "Finding…" else "Find ThisOne",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** A button that squishes down on press for a tactile, playful feel. */
@Composable
private fun BouncyButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale",
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        content = content,
    )
}

// ── Results list ─────────────────────────────────────────────────────────────

@Composable
private fun ResultsList(
    results: List<MemeResult>,
    isSaved: (String) -> Boolean,
    onSave: (MemeResult) -> Unit,
    onReset: () -> Unit,
    onCardClick: (MemeResult) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Text(
                text = "🎉 ${results.size} matches found",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        itemsIndexed(results, key = { _, result -> result.template.id }) { index, result ->
            PopIn(delayMillis = index * 80) {
                MemeResultCard(
                    result = result,
                    saved = isSaved(result.template.id),
                    onSave = { onSave(result) },
                    onClick = { onCardClick(result) },
                )
            }
        }

        item {
            TextButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("🔁 Try a different situation")
            }
        }
    }
}

/** Small pop/scale-in entrance for list items, staggered by [delayMillis]. */
@Composable
private fun PopIn(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
    ) {
        content()
    }
}

// ── Meme result card ─────────────────────────────────────────────────────────

@Composable
fun MemeResultCard(
    result: MemeResult,
    saved: Boolean,
    onSave: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeColor = when {
        result.vibeScore >= 8 -> Go500
        result.vibeScore >= 5 -> Spark500
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {

            // Meme image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            ) {
                AsyncImage(
                    model = result.template.url,
                    contentDescription = result.template.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Vibe score badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.95f),
                ) {
                    Text(
                        text = "✦ ${result.vibeScore}/10",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            // Caption + actions
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = result.template.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(6.dp))

                if (result.topText.isNotBlank()) {
                    Text(
                        text = result.topText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (result.bottomText.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = result.bottomText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Match reason chip
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "💬 ${result.matchReason}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Actions row
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Save
                    val saveScaleAnim = remember { Animatable(1f) }
                    LaunchedEffect(saved) {
                        if (saved) {
                            saveScaleAnim.animateTo(1.4f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                            saveScaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }
                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (saved) "Saved" else "Save",
                            tint = if (saved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.scale(saveScaleAnim.value),
                        )
                    }

                    val context = LocalContext.current
                    IconButton(onClick = { shareMeme(context, result) }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Supporting composables ───────────────────────────────────────────────────

@Composable
private fun IdleHint(onSuggestionClick: (String) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float_y",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = "✦",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(y = offsetY.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Describe the situation.\nWe'll find THE meme. 🫡",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Need inspo? Tap one 👇",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(SITUATION_SUGGESTIONS) { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = { Text(suggestion, maxLines = 1) },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    var lineIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1400)
            lineIndex = (lineIndex + 1) % LOADING_LINES.size
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "spin_big")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
            ),
            label = "spin_big_angle",
        )
        Text(
            text = "🎲",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.rotate(rotation),
        )

        Spacer(Modifier.height(16.dp))

        // Pulsing dots
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dot_alpha",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (i == 1) alpha else alpha * 0.6f
                            ),
                            shape = RoundedCornerShape(50),
                        )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = lineIndex,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "loading_line",
        ) { idx ->
            Text(
                text = LOADING_LINES[idx],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text(text = "😬", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        BouncyButton(onClick = onRetry, enabled = true) {
            Text("Try again 🔄")
        }
    }
}
