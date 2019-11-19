package vowel.apk.chatService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.databaseHelpers.DatabaseMessage;
import vowel.apk.notificationActivityy.NotificationActivity;


public class ChatService  extends Service {
    boolean LISTEN = false;
    String LOG_TAG = "CALL-SERVICE";
    int LISTENER_PORT = 15930;
    private static final int BUF_SIZE = 1024;
    DatabaseMessage databaseMessage;
    DatabaseHelper databaseHelper;
    Uri notification;
    Ringtone r;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCallListener();
        databaseMessage = new DatabaseMessage(this);
        databaseHelper = new DatabaseHelper(this);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        return super.onStartCommand(intent, flags, startId);
    }


    //Start listerning for incoming messages
    private void startCallListener() {

        // Creates the listener thread
        LISTEN = true;
        Thread listener = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    // Set up the socket and packet to receive
                    Log.i(LOG_TAG, "Incoming message listener started");
                    DatagramSocket socket = new DatagramSocket(LISTENER_PORT);
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {
                        // Listen for incoming message requests
                        try {
                 //           Log.i(LOG_TAG, "Listening for messages");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);

                            //Add contents to database
                            String sender = data.split("__~~__")[0];
                            String message = data.split("__~~__")[1];
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd  'at' HH.mm.ss");      //removed G
                            String timestamp = simpleDateFormat.format(new Date());
                            databaseMessage.insertMessage(timestamp, sender, message, true);
                            notifyNewMessage(message, sender);
                            InetAddress address = packet.getAddress();
                            //Send an acknowledgement
                            sendMessage(address, "ACC:");



                        }
                        catch(Exception e) {}
                    }
                    Log.i(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException in listener " + e);
                }
            }
        });
        listener.start();
    }

    //Stop listerning
    private void stopCallListener() {
        // Ends the listener thread
        LISTEN = false;
    }

    @Override
    public void onDestroy() {
        stopCallListener();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //Post a notification to the screen
    public void notifyNewMessage(String content, String sender) {
        r.play();
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(5000);
        }
        int FOREGROUND_ID = 1000;
        Notification mNotification;
        String CHANNEL_ID = "VowelNotifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(android.R.drawable.stat_notify_chat);
        builder.setContentTitle("NEW MESSAGE");
        builder.setContentText(content);
        builder.setContentInfo(databaseHelper.getUsernameDetail(sender));
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        Intent activityIntent = new Intent(this, NotificationActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotification = builder.build();
        startForeground(FOREGROUND_ID, mNotification);

    }
//send a datagram message
    private void sendMessage(final InetAddress address, final String message) {
        // Creates a thread for sending a control messsgae
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {


                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, 46789);
                    socket.send(packet);
                    Log.i(LOG_TAG, "Sent message( " + message + " ) to " + address);
                    socket.disconnect();
                    socket.close();
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + address);
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in sendMessage: " + e);
                }
                catch(IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in sendMessage: " + e);
                }
            }
        });
        replyThread.start();
    }
}
