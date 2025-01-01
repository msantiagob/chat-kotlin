package com.example.chatapp.viewmodel

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.data.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> = _messages

    private lateinit var socket: Socket
    private val gson = Gson()

    // Notificaciones
    private val context = application.applicationContext
    private val CHANNEL_ID = "chat_messages"
    private val NOTIFICATION_ID = 1

    init {
        initializeSocket()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Messages"
            val descriptionText = "Notificaciones de mensajes nuevos"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: Message) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Asegúrate de tener este ícono
            .setContentTitle("Nuevo mensaje")
            .setContentText(message.text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                println("No hay permiso para mostrar notificaciones: ${e.message}")
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
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
                        println(gson.fromJson(data.toString(), Message::class.java))
                        val message: Message = gson.fromJson(data.toString(), Message::class.java)
                        _messages.add(message)
                        println(message)
                        println(!isAppInForeground() && message.sender != "other")
                        // Mostrar notificación solo si la app está en segundo plano
                        // y el mensaje no es del usuario actual
                        if (!isAppInForeground() && message.sender != "other") {
                            showNotification(message)
                        }
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
                sender = "other",
                time = getCurrentTime()
            )

            socket.emit("message", JSONObject().apply {
                put("message", message.text)
                put("sender", message.sender)
            })


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