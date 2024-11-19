package com.maugarcia.pokeapp.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
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

    private var shouldContinue = true  // Controla si el servicio debe seguir funcionando

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // Aquí se debe llamar a createNotification() para crear la notificación y poner el servicio en primer plano
        startForeground(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        // Crea un canal de notificación para dispositivos con Android O y versiones posteriores
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pokemon Updates",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        // Construye la notificación que se muestra al usuario
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pokemon Update Service")
            .setContentText("Buscando nuevos Pokémon...")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Obtener el parámetro de la intención para saber si debemos continuar o no
        shouldContinue = intent?.getBooleanExtra("should_continue", true) ?: true

        if (!shouldContinue) {
            stopSelf()  // Detener el servicio si el parámetro es false
            return START_NOT_STICKY
        }

        serviceScope.launch {
            while (shouldContinue) {
                try {
                    val currentCount = repository.getPokemonCount()
                    repository.fetchAndStorePokemons(10, currentCount)
                    showUpdateNotification()
                    showToastIfAppIsForeground()  // Esta función asegura que el Toast se muestra en primer plano
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000) // 30 segundos
            }
        }
        return START_STICKY
    }

    private fun showUpdateNotification() {
        // Muestra una notificación para informar al usuario que se han agregado nuevos Pokémon
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Nuevos Pokémon!")
            .setContentText("Se han agregado 10 nuevos Pokémon a la lista")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
    }

    private fun showToastIfAppIsForeground() {
        val activityManager = getSystemService(ActivityManager::class.java)
        val runningAppProcesses = activityManager.runningAppProcesses

        runningAppProcesses?.let {
            val isAppInForeground = it.any { process ->
                process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }

            // Si la app está en primer plano, mostrar el Toast
            if (isAppInForeground) {
                // Mostrar el Toast en el contexto de la aplicación
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "Se han agregado 10 nuevos Pokémon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "pokemon_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val UPDATE_NOTIFICATION_ID = 2
    }
}
