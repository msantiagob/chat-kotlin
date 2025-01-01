package com.example.chatapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatapp.data.Message
import com.example.chatapp.viewmodel.ChatViewModel
import androidx.compose.material.icons.automirrored.filled.Send

// Definimos colores personalizados
private val PinkPrimary = Color(0xFFFFC0CB)      // Rosa claro
private val PinkSecondary = Color(0xFFFFB6C1)    // Rosa más suave
private val PinkDark = Color(0xFFFF69B4)         // Rosa más intenso
private val BackgroundColor = Color(0xFFFFF0F5)   // Rosa muy claro para el fondo

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.messages
    var newMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
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
                .background(Color.White, RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessageText,
                onValueChange = { newMessageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
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
                    .background(PinkDark, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatMessage(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.sender == "me") Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.sender == "me") PinkDark else PinkSecondary,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.sender == "me") 16.dp else 4.dp,
                bottomEnd = if (message.sender == "me") 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.sender == "me") Color.White else Color.Black
                )
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.sender == "me")
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}