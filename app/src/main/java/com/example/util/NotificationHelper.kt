package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_VISITORS = "shafi_channel_visitors"
    const val CHANNEL_EMERGENCY = "shafi_channel_emergency"
    const val CHANNEL_AMENITIES = "shafi_channel_amenities"
    const val CHANNEL_NOTICES = "shafi_channel_notices"

    private const val NOTIFICATION_ID_VISITOR = 1001
    private const val NOTIFICATION_ID_EMERGENCY = 1002
    private const val NOTIFICATION_ID_AMENITY = 1003
    private const val NOTIFICATION_ID_NOTICE = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val visitorChannel = NotificationChannel(
                CHANNEL_VISITORS,
                "Gate Visitor & Security Arrivals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alerts when guests, delivery boys, or cabs arrive at the gate"
                enableVibration(true)
                setShowBadge(true)
            }

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency SOS & Society Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical building alarms, lift rescue, and security alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            val amenityChannel = NotificationChannel(
                CHANNEL_AMENITIES,
                "Amenity & Facility Bookings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Facility reservation confirmations and reminders"
            }

            val noticeChannel = NotificationChannel(
                CHANNEL_NOTICES,
                "Society Notices & Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Managing committee notices and building circulars"
            }

            notificationManager.createNotificationChannels(
                listOf(visitorChannel, emergencyChannel, amenityChannel, noticeChannel)
            )
        }
    }

    private fun getLaunchPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showVisitorArrivalNotification(
        context: Context,
        visitorName: String,
        purpose: String,
        unitNumber: String,
        passcode: String
    ) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_VISITORS)
            .setSmallIcon(R.drawable.ic_building_logo)
            .setContentTitle("🔔 Visitor at Main Gate: $visitorName")
            .setContentText("$purpose for $unitNumber (Passcode: $passcode)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🔔 Visitor at Gate #1\nName: $visitorName\nPurpose: $purpose\nDestination: $unitNumber\nStatus: Checked In & Granted Entry")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent(context))
            .setColor(0xFF4F46E5.toInt())

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_VISITOR, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showEmergencySosNotification(
        context: Context,
        alertTitle: String,
        details: String
    ) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
            .setSmallIcon(R.drawable.ic_building_logo)
            .setContentTitle("🚨 EMERGENCY SOS: $alertTitle")
            .setContentText(details)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 Shafi Homes Arcade Emergency Alert!\n$alertTitle\n$details\nTap to open the Emergency SOS Hub.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent(context))
            .setColor(0xFFEF4444.toInt())

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_EMERGENCY, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showAmenityConfirmedNotification(
        context: Context,
        amenityName: String,
        timeSlot: String,
        residentName: String
    ) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_AMENITIES)
            .setSmallIcon(R.drawable.ic_building_logo)
            .setContentTitle("🏛️ Amenity Confirmed: $amenityName")
            .setContentText("Reserved for $timeSlot by $residentName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent(context))
            .setColor(0xFF0D9488.toInt())

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_AMENITY, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showNoticePublishedNotification(
        context: Context,
        noticeTitle: String,
        author: String
    ) {
        if (!hasNotificationPermission(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_NOTICES)
            .setSmallIcon(R.drawable.ic_building_logo)
            .setContentTitle("📢 Society Notice: $noticeTitle")
            .setContentText("Posted by $author on the Digital Notice Board")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getLaunchPendingIntent(context))
            .setColor(0xFFD97706.toInt())

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NOTICE, builder.build())
        } catch (_: SecurityException) {}
    }
}
