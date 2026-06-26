package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.db.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive or loading state changes
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Frosted Glass Dark Canvas background with beautiful glowing ambient mesh gradients
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // 1. Dark background canvas (#0F1115)
                drawRect(color = Color(0xFF0F1115))

                // 2. Top-Left glowing blue orb (bg-blue-600/20 blur-[100px])
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2B2563EB), // Blue 600 with 17% alpha
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(-size.width * 0.1f, -size.height * 0.05f)
                )

                // 3. Middle-Right glowing purple orb (bg-purple-600/20 blur-[120px])
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2B9333EA), // Purple 600 with 17% alpha
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 1.0f,
                    center = Offset(size.width * 1.1f, size.height * 0.45f)
                )

                // 4. Bottom-Left glowing emerald orb (bg-emerald-600/10 blur-[80px])
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1410B981), // Emerald 600 with 8% alpha
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.8f,
                    center = Offset(size.width * 0.1f, size.height * 0.9f)
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Jawaab AI Star Icon",
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jawaab AI",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF34D399), CircleShape) // Emerald 400
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ONLINE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399), // Emerald 400
                                    fontSize = 9.sp,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    },
                    actions = {
                        if (messages.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearChat() },
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Chat History",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0x33000000), // backdrop bg-black/20
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier.drawBehind {
                        // Thin bottom border to simulate frosted edge (border-b border-white/10)
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Messages List or Empty Suggestion view
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        EmptySuggestionsView(
                            onSuggestionClicked = { suggestion ->
                                viewModel.onInputTextChanged(suggestion)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    modifier = Modifier.testTag("chat_message_${message.id}")
                                )
                            }
                            if (isLoading) {
                                item {
                                    ThinkingIndicator()
                                }
                            }
                        }
                    }
                }

                // Bottom Input Footer Section (backdrop-blur-2xl bg-black/40 border-t border-white/5)
                Surface(
                    color = Color(0x66000000), // bg-black/40
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Border top (border-t border-white/5)
                            drawLine(
                                color = Color(0x0DFFFFFF), // White/5
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Input container (bg-white/5 border border-white/10 rounded-full)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(28.dp))
                                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(28.dp))
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = inputText,
                                    onValueChange = { viewModel.onInputTextChanged(it) },
                                    placeholder = {
                                        Text(
                                            text = "Sawaal likhein...",
                                            color = Color(0xFF64748B), // Slate 500
                                            fontSize = 14.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("question_input"),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color(0xFFF1F5F9), // Slate 100
                                        unfocusedTextColor = Color(0xFFF1F5F9),
                                        cursorColor = Color(0xFF3B82F6),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            viewModel.sendMessage()
                                            keyboardController?.hide()
                                        }
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Send button (w-14 h-14 rounded-full bg-blue-500 shadow-lg shadow-blue-500/30)
                            val isSendEnabled = inputText.trim().isNotEmpty() && !isLoading
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSendEnabled) Color(0xFF3B82F6) else Color(0x1AFFFFFF)
                                    )
                                    .clickable(enabled = isSendEnabled) {
                                        viewModel.sendMessage()
                                        keyboardController?.hide()
                                    }
                                    .testTag("send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Bhejein",
                                    tint = if (isSendEnabled) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Home indicator visual cue matching design
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp, bottom = 8.dp)
                                .width(80.dp)
                                .height(4.dp)
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    // Exact bubble shapes based on sender
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
    }

    // Exact backgrounds based on sender:
    // User message: bg-blue-600/30 backdrop-blur-xl border border-blue-400/20 shadow-lg
    // Assistant message: bg-white/10 backdrop-blur-2xl border border-white/10 shadow-xl
    val (bubbleBg, borderColor) = if (message.isUser) {
        Pair(Color(0x4D2563EB), Color(0x3360A5FA)) // bg-blue-600/30, border-blue-400/20
    } else {
        Pair(Color(0x1AFFFFFF), Color(0x1AFFFFFF)) // bg-white/10, border-white/10
    }

    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 295.dp)
                .background(color = bubbleBg, shape = bubbleShape)
                .border(width = 1.dp, color = borderColor, shape = bubbleShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            MarkdownText(
                text = message.text,
                isUser = message.isUser
            )
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    isUser: Boolean
) {
    // Slate 100 for assistant, White for user
    val textColor = if (isUser) Color.White else Color(0xFFF1F5F9)

    val annotatedString = remember(text) {
        buildAnnotatedString {
            var currentIndex = 0
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            val matches = boldRegex.findAll(text)

            for (match in matches) {
                val start = match.range.first
                val end = match.range.last + 1
                val content = match.groupValues[1]

                if (start > currentIndex) {
                    append(text.substring(currentIndex, start))
                }

                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else Color(0xFF60A5FA)
                    )
                ) {
                    append(content)
                }

                currentIndex = end
            }

            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp,
            fontSize = 15.sp
        ),
        color = textColor
    )
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = Color(0x1AFFFFFF), // bg-white/10
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
                )
                .border(width = 1.dp, color = Color(0x1AFFFFFF), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI soch raha hai",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF60A5FA).copy(alpha = alpha1), CircleShape)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF60A5FA).copy(alpha = alpha2), CircleShape)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF60A5FA).copy(alpha = alpha3), CircleShape)
                )
            }
        }
    }
}

@Composable
fun EmptySuggestionsView(
    onSuggestionClicked: (String) -> Unit
) {
    val suggestions = listOf(
        "Bharat ka pratham rashtrapati kaun tha?",
        "Paani ka scientific naam kya hai?",
        "Explain quantum computing in simple terms.",
        "Chai peene ke fayde aur nuksaan kya hain?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Star logo with subtle pulse glow
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Star Glow Icon",
            tint = Color(0xFF60A5FA),
            modifier = Modifier
                .size(56.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "Kucch bhi poochhein!",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Main har sawaal ka behtareen jawaab doonga.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Suggestions:",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF60A5FA),
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp, start = 4.dp)
        )

        suggestions.forEachIndexed { index, suggestion ->
            // Glass card suggestions (bg-white/5 border border-white/10)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x0DFFFFFF) // bg-white/5
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .clickable { onSuggestionClicked(suggestion) }
                    .testTag("suggestion_card_$index")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "➔",
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

