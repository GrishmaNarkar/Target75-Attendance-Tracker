package com.example.target75;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {

    // 🟢 SAFE ZONE (> 80%)
    private final String[] chillTitles = {
            "Topper Vibes ✨",
            "Full Aish! 😎",
            "King/Queen Behavior 👑",
            "Attendance = 💯"
    };
    private final String[] chillTexts = {
            "Bhai 80+ chal raha hai, aaj bunk marle koi tension nahi!",
            "Ekdum safe zone! Canteen mein Vada Pav chalega aaj?",
            "Professor bhi khush, tum bhi khush. Aaj dosto ke paas chill kar!",
            "Fast local chhod, aaram se ja college. Attendance sorted hai!"
    };

    // 🟡 WARNING ZONE (75% - 80%)
    private final String[] warnTitles = {
            "Borderline Case ⚠️",
            "Bhai Sambhal Ja",
            "Khatre Ke Kareeb",
            "No More Bunks!"
    };
    private final String[] warnTexts = {
            "75 ke paas atak raha hai, chale jaa bhai warna lene ke dene padenge.",
            "Ek aur bunk aur sidha defaulter list mein. Chupchap bag utha aur nikal!",
            "Margin pe jee raha hai tu! Aaj ka lecture galti se bhi miss mat karna.",
            "Risk mat le bhai, station se seedha college ki taraf rukh kar le!"
    };

    // 🔴 DANGER ZONE (< 75%)
    private final String[] dangerTitles = {
            "Abbe Oye! Padhle 💀",
            "75% Ro Raha Hai 😭",
            "Defaulter Alert 🚨",
            "MSBTE Dean On Fire 💥",
            "System Hil Gaya 📉"
    };
    private final String[] dangerTexts = {
            "Attendance teri patal mein gir rahi hai, aur tu reels dekh raha hai?",
            "Proxy lagwane ka time aa gaya hai, uth ja mere bhai!",
            "Exam hall ke bahar baithna hai kya? Jaa class mein bhag ke!",
            "Kurla station pe bheed kam hai, par teri attendance usse bhi kam hai!",
            "Ghar pe letter pakka aayega ab toh, bacha le khud ko!",
            "K Scheme mein waise hi waat lagi hai, attendance toh theek rakh!"
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "target75_periodic_notifications";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Automated Attendance Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Every 45 mins smart reminders");
            notificationManager.createNotificationChannel(channel);
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        // 🔥 GET CURRENT PERCENTAGE FROM STORAGE
        android.content.SharedPreferences prefs = context.getSharedPreferences("Target75Prefs", Context.MODE_PRIVATE);
        int currentPercentage = prefs.getInt("overall_percentage", 100); // Default 100 maan lete hain agar app naya hai

        // 🔥 SELECT MESSAGE BASED ON PERCENTAGE
        Random random = new Random();
        String title = "";
        String text = "";

        if (currentPercentage > 80) {
            // Chill Messages
            int index = random.nextInt(chillTitles.length);
            title = chillTitles[index];
            text = chillTexts[index];
        } else if (currentPercentage >= 75 && currentPercentage <= 80) {
            // Warning Messages
            int index = random.nextInt(warnTitles.length);
            title = warnTitles[index];
            text = warnTexts[index];
        } else {
            // Danger Messages
            int index = random.nextInt(dangerTitles.length);
            title = dangerTitles[index];
            text = dangerTexts[index];
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher)) // 🔥 YEH LAGA TUMHARA APP LOGO!
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int notificationId = random.nextInt(10000);
        notificationManager.notify(notificationId, builder.build());
    }
}