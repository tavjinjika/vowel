package vowel.apk.onCallActivityy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import vowel.apk.R;
import vowel.apk.databaseHelpers.DatabaseHelper;


public class OnCallActivity extends AppCompatActivity {


    private static final String LOG_TAG = "MakeCall";
    private static final int BROADCAST_PORT = 50002;
    private static final int BUF_SIZE = 1024;
    private String displayName;
    private String contactName="";
    private ArrayList<String> contactIp;
    private ArrayList<String> ec;
    private boolean LISTEN = true;
    private boolean IN_CALL = false;
    private AudioCall call;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_call);

        getSupportActionBar().setTitle("Calling ..."); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }

        databaseHelper = new DatabaseHelper(this);

        ec = new ArrayList<>();
        contactIp = new ArrayList<>();
        try {
            socket = new DatagramSocket(BROADCAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }


        Intent intent = getIntent();
       ec = intent.getStringArrayListExtra("ECs");
     contactIp =   intent.getStringArrayListExtra("IPs");
       // ec = intent.getStringExtra("EXTRA_ECS");
        displayName = intent.getStringExtra("CALLER");

        Log.i("What we got",ec.toString());
        for(int i =0; i< ec.size();i++){

          contactName +=databaseHelper.getUsernameDetail(ec.get(i))+", ";
        }
       // contactName = databaseHelper.getUsernameDetail(ec);


        Log.d("DB Results",contactName);
        String textViewText = "Calling:     " + contactName;
        Log.d("DB Results",textViewText);

        TextView textView = findViewById(R.id.textviewcalling);
        textView.setText(textViewText);

        startListener();
        makeCall();

        Button endButton = findViewById(R.id.buttonEndCall);
        endButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Button to end the call has been pressed
                endCall();
            }
        });
    }

    private void makeCall() {
        // Send a request to start a call
      sendMessage("CAL:"+displayName, 12930);
            }

    private void endCall() {
        // Ends the chat sessions
        stopListener();
        if(IN_CALL) {

            call.endCall();
        }
        sendMessage("END:", BROADCAST_PORT);
        socket.close();
        finish();
    }

    DatagramSocket socket;
    private void startListener() {
        // Create listener thread
        LISTEN = true;
        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    Log.i(LOG_TAG, "Listener started!");

                    socket.setReuseAddress(true);
                    socket.setSoTimeout(15000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {

                        try {

                            Log.i(LOG_TAG, "Listening for packets");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);
                            String action = data.substring(0, 4);
                            if(action.equals("ACC:")) {
                                // Accept notification received. Start call

                                call = new AudioCall(packet.getAddress());
                                 call.startCall();

                                IN_CALL = true;
                            }
                            else if(action.equals("REJ:")) {
                                // Reject notification received. End call
                                endCall();
                            }
                            else if(action.equals("END:")) {
                                // End call notification received. End call
                                endCall();
                            }
                            else {
                                // Invalid notification received
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
                        }
                        catch(SocketTimeoutException e) {
                            if(!IN_CALL) {
                                sendMessage("MISS"+displayName, BROADCAST_PORT);
                                Log.i(LOG_TAG, "No reply from contact. Ending call");
                                endCall();
                                return;
                            }
                        }
                        catch(IOException e) {

                        }
                    }
                    Log.i(LOG_TAG, "Listener ending");
                    socket.disconnect();
                    socket.close();
                    return;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException in Listener");
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

    private void sendMessage(final String message, final int port) {
        // Creates a thread used for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    DatagramSocket socket = null;
                    for(int i =0;i<contactIp.size() ;i++) {
                        InetAddress address = InetAddress.getByName(contactIp.get(i));
                        byte[] data = message.getBytes();
                        socket = new DatagramSocket();
                        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                        socket.send(packet);
                    }
                    Log.i(LOG_TAG, "Sent message( " + message + " ) to " + contactIp);
                    assert socket != null;
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

}
