package com.example.chatapp.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.chatapp.data.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import io.socket.client.IO
import io.socket.client.Socket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel : ViewModel() {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private lateinit var socket: Socket
    private val gson = Gson()

    init {
        initializeSocket()
    }

    private fun initializeSocket() {
        try {
            socket = IO.socket("https://bakklovechat-production.up.railway.app/")
            setupSocketListeners()
            socket.connect()
        } catch (e: Exception) {
            println("Socket initialization error: ${e.message}")
        }
    }

    private fun setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            println("Socket connected")
        }

        socket.on("previousMessages") { args ->
            viewModelScope.launch {
                try {
                    args.firstOrNull()?.let { data ->
                        // Convierte el JSON a una lista de mensajes
                        val type = object : TypeToken<List<Message>>() {}.type
                        val previousMessages: List<Message> = gson.fromJson(data.toString(), type)

                        _messages.clear()
                        _messages.addAll(previousMessages)
                    }
                } catch (e: Exception) {
                    println("Error parsing previous messages: ${e.message}")
                }
            }
        }

        socket.on("message") { args ->

            viewModelScope.launch {
                try {

                    args.firstOrNull()?.let { data ->
                        println( gson.fromJson(data.toString(), Message::class.java))
                        val message: Message = gson.fromJson(data.toString(), Message::class.java)
                        _messages.add(message)
                    }
                } catch (e: Exception) {
                    println("Error parsing message: ${e.message}")
                }
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            println("Socket disconnected")
        }

    }

    fun sendMessage(content: String) {
        if (content.isNotBlank()) {
            val message = Message(
                id = messages.size + 1,
                text = content,
                sender = "me", // o el identificador que uses para el remitente
                time = getCurrentTime()
            )

        // Emite el mensaje al socket
            socket.emit("message", JSONObject().apply {
                put("message", message.text)
                put("sender", message.sender)
            })

            // Opcionalmente, puedes agregar el mensaje localmente
            // si el servidor no hace eco del mensaje
            _messages.add(message)
        }
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
    }
}