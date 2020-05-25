package com.example.takvimuygulamasi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.widget.Toast;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

// Alarm çalma eventlerini dinleyen broadcastReceiver classıdır.
public class HatirlatmaAlarmReceiver extends BroadcastReceiver {

    public static Ringtone ringtone;
    SharedPreferences sharedPreferences;
    public static final String VARSAYILAN_AYARLAR = "Varsayilan_Ayarlar";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Alarm çalacağı zaman ilgili etkinliğin adını alır ve bildirim olarak gösterir.
        String etkinlik_adi = intent.getStringExtra("etkinlik_adi");
        int ReqCode = intent.getIntExtra("id",0);
        // Varsayılan ayarlardan ringtone kaydetmiştik. Bu kaydedilen ringtone u shared preferences vasıtasıyla alıyoruz.
        sharedPreferences = context.getSharedPreferences(VARSAYILAN_AYARLAR, Context.MODE_PRIVATE);
        String ringtone_str = sharedPreferences.getString("ringtone",null);

        // Eğer kaydedilen bir ayar yok ise bunun kontrolünü yapıyoruz.
        if(ringtone_str != null){
            Uri ringtone_uri = Uri.parse(ringtone_str);
            ringtone = RingtoneManager.getRingtone(context, ringtone_uri);
        }else{
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null)
            {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(context, alarmUri);
        }

        // Alarm çalınır.
        ringtone.play();
        Toast.makeText(context, "Alarm Vakti Geldi!", Toast.LENGTH_LONG).show();

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {

            v.vibrate(2000);
        }

        // Alarm çalarken bir dialog gösterilir. Bunun için kendi yazdığım dialog classı çağırılır ve parametre olarak etkinlik adı verilir.
        Intent mIntent = new Intent(context,DialogBoxAlarmDurdurma.class);
        mIntent.putExtra("etkinlik_adi",etkinlik_adi);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);

        Intent i = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), ReqCode, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context.getApplicationContext());

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notifications)
                .setTicker("Hearty365")
                .setContentTitle("Etkinlik Hatırlatma Servisi")
                .setContentText(etkinlik_adi+ " adlı etkinliğiniz bulunmakta!")
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentInfo("Info");


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(ReqCode, b.build());
    }

    // Alarm çalışıyorken gelen dialog ekranından alarmı kapatabilmek için her yerden erişilebilir bir alarmı kapat fonksiyonu tanımladım.
    public static void AlarmKapat(){
        if(ringtone!=null)
            ringtone.stop();
    }
}
