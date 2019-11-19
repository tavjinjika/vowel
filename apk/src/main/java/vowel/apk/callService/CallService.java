package vowel.apk.callService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import timber.log.Timber;
import vowel.apk.onCallReceivedActivity.OnCallReceivedActivity;

public class CallService extends Service {
    boolean LISTEN = false;
    String LOG_TAG = "CALL-SERVICE";
    int LISTENER_PORT = 12930;
    static final int BUF_SIZE = 1024;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCallListener();
        return super.onStartCommand(intent, flags, startId);
    }

    //Start to listen for incoming calls
    private void startCallListener() {

        // Creates the listener thread
        LISTEN = true;
        Thread listener = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    // Set up the socket and packet to receive
                    Log.i(LOG_TAG,"Incoming call listener started");
                    DatagramSocket socket = new DatagramSocket(LISTENER_PORT);
                    socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while(LISTEN) {
                        // Listen for incoming call requests
                        try {
                            Timber.tag(LOG_TAG).i("Listening for incoming calls");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);

                            String action = data.substring(0, 4);
                            if(action.equals("CAL:")) {
                                // Received a call request. Start the ReceiveCallActivity
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());


//start the onCallreceived activity
                                Intent intent = new Intent(CallService.this, OnCallReceivedActivity.class);
                                intent.putExtra("EXTRA_ECS", name);
                                intent.putExtra("EXTRA_IP", address.substring(1, address.length()));
                                //LISTEN = false;
                                //stopCallListener();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Log.i(LOG_TAG, "Packet received from "+ packet.getAddress().toString().substring(1) +" with contents: " + data);
                            }
                            else {
                                // Received an invalid request
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
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

    //stop the listerner
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
}
