package com.example.chatapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.Message
import com.example.chatapp.viewmodel.ChatViewModel
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.chatapp.R

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer

// Definimos colores personalizados
private val PinkPrimary = Color(0xFF9D174D)      // Rosa claro
private val PinkSecondary = Color(0xFFFCE7F3)    // Rosa más suave
private val PinkDark = Color(0xFFA855F7)         // Rosa más intenso
private val BackgroundColor = Color(0xFFF472B6)   // Rosa muy claro para el fondo

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.messages
    var newMessageText by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 1500f,
        targetValue = -1500f, // Ajusta este valor según el tamaño de tu imagen
        animationSpec = infiniteRepeatable(
            animation = tween(800000, easing = LinearEasing), //50000
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX
                    scaleX = 3.8f
                    scaleY = 3.8f
                },

        )

        Column(
            modifier = Modifier.fillMaxSize()
        ){
        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            items(messages) { message ->
                message?.text?.let { text ->
                    if (text.isNotBlank()) {
                        ChatMessage(message = message)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(39.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Escribe un mensaje...", color = Color.Black) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            IconButton(
                onClick = {
                    viewModel.sendMessage(newMessageText)
                    newMessageText = ""
                },
                modifier = Modifier
                    .background(PinkPrimary, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
} }

@Composable
fun ChatMessage(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.sender == "other") Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.sender == "other") PinkDark else PinkSecondary,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.sender == "other") 16.dp else 4.dp,
                bottomEnd = if (message.sender == "other") 4.dp else 16.dp
            ),
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.sender == "other") 16.dp else 4.dp,
                        bottomEnd = if (message.sender == "other") 4.dp else 16.dp
                    ),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .widthIn(max = 300.dp)

        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    fontWeight = FontWeight.Bold,
                    color = if (message.sender == "other") Color.White else PinkPrimary
                )
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.sender == "other")
                        Color.White.copy(alpha = 0.7f)
                    else
                        PinkPrimary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}