package vowel.apk.onCallReceivedActivity;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import vowel.apk.R;
import vowel.apk.databaseHelpers.DatabaseCall;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.onCallActivityy.AudioCall;


public class OnCallReceivedActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ReceiveCall";
    private static final int BROADCAST_PORT = 50002;
    private static final int BUF_SIZE = 1024;
    private static final String CHANNEL_ID = "VowelNotifications";
    private String contactIp;
    String contactEc;
    private String contactName;
    private boolean LISTEN = true;
    private boolean IN_CALL = false;
    private AudioCall call;
    DatabaseHelper databaseHelper;
    DatabaseCall databaseCall;
    Uri notification;
    Ringtone r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_call_received);

        getSupportActionBar().setTitle("Incoming Call ..."); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseCall = new DatabaseCall(this);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }

        databaseHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        contactEc = intent.getStringExtra("EXTRA_ECS");
        contactName = databaseHelper.getUsernameDetail(contactEc);
        contactIp = intent.getStringExtra("EXTRA_IP");

        TextView textView = findViewById(R.id.textViewIncomingCall);
        String text = "Incoming call: " + contactName;
        textView.setText(text);

        final Button endButton = findViewById(R.id.buttonEndCall1);
        endButton.setVisibility(View.INVISIBLE);

        startListener();

        // ACCEPT BUTTON
        Button acceptButton = findViewById(R.id.buttonAccept);
        acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    // Accepting call. Send a notification and start the call
                    r.stop();
                    sendMessage("ACC:");
                    InetAddress address = InetAddress.getByName(contactIp);
                    Log.i(LOG_TAG, "Calling " + address.toString());
                    IN_CALL = true;
                    call = new AudioCall(address);
                    call.startCall();
                    // Hide the buttons as they're not longer required
                    Button accept = findViewById(R.id.buttonAccept);
                    accept.setEnabled(false);

                    Button reject = findViewById(R.id.buttonReject);
                    reject.setEnabled(false);

                    endButton.setVisibility(View.VISIBLE);
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "UnknownHostException in acceptButton: " + e);
                }
                catch(Exception e) {

                    Log.e(LOG_TAG, "Exception in acceptButton: " + e);
                }
            }
        });

        // REJECT BUTTON
        Button rejectButton = findViewById(R.id.buttonReject);
        rejectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Send a reject notification and end the call
                r.stop();
                sendMessage("REJ:");
                endCall();
            }
        });

        // END BUTTON
        endButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                endCall();
            }
        });
    }

    private void endCall() {
        // End the call and send a notification
        stopListener();
        if(IN_CALL) {

            call.endCall();
        }
        sendMessage("END:");
        finish();
    }

    private void startListener() {
        // Creates the listener thread
        LISTEN = true;
        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    Log.i(LOG_TAG, "Listener started!");
                    DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(1500);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {

                        try {

                            Log.i(LOG_TAG, "Listening for packets");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);
                            String action = data.substring(0, 4);
                            if(action.equals("END:")) {
                                // End call notification received. End call
                                endCall();
                            }
                            else if (action.equals("MISS")){
                                String sender = databaseHelper.getUsernameDetail(data.substring(4));
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd  'at' HH.mm.ss");      //removed G between dd and at
                                String timestamp = simpleDateFormat.format(new Date());
                                notifyMissedCall(sender, timestamp);
                                databaseCall.insertCall(timestamp,data.substring(4));
                            }
                            else {
                                // Invalid notification received.
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
                        }
                        catch(IOException e) {

                            Log.e(LOG_TAG, "IOException in Listener " + e);
                        }
                    }
                    Log.i(LOG_TAG, "Listener ending");
                    socket.disconnect();
                    socket.close();
                    return;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException in Listener " + e);
                    endCall();
                }
            }
        });
        listenThread.start();
    }

    private void stopListener() {
        // Ends the listener thread
        LISTEN = false;
    }



    private void sendMessage(final String message) {
        // Creates a thread for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    InetAddress address = InetAddress.getByName(contactIp);
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, BROADCAST_PORT);
                    socket.send(packet);
                    Log.i(LOG_TAG, "Sent message( " + message + " ) to " + contactIp);
                    socket.disconnect();
                    socket.close();
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + contactIp);
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

public void notifyMissedCall(String sender, String time){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
    builder.setSmallIcon(android.R.drawable.stat_notify_missed_call);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    builder.setContentIntent(pendingIntent);
    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
    builder.setContentTitle("MISSED CALL");
    builder.setAutoCancel(true);
    builder.setContentText("Missed call from"+ sender);
    builder.setSubText(time);

    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    // Will display the notification in the notification bar
    notificationManager.notify(1, builder.build());
}

}
