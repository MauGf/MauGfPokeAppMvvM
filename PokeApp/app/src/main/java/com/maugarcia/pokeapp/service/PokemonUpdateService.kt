package com.maugarcia.pokeapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.maugarcia.pokeapp.R
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.Provider
import javax.inject.Inject

class PokemonUpdateService : Service() {

    @Inject
    lateinit var repository: PokemonRepository  // Dependencia inyectada por Hilt

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                try {
                    val currentCount = repository.getPokemonCount()
                    repository.fetchAndStorePokemons(10, currentCount)
                    showUpdateNotification()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000) // 30 segundos
            }
        }
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pokemon Updates",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pokemon Update Service")
            .setContentText("Buscando nuevos Pokemon...")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()
    }

    private fun showUpdateNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Nuevos Pokemon!")
            .setContentText("Se han agregado 10 nuevos Pokemon a la lista")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "pokemon_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_NOTIFICATION_ID = 2
    }
}