package vowel.apk.networkingServices;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import timber.log.Timber;
import vowel.apk.R;
import vowel.apk.bootstrapActivity.BootstrapActivity;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.databaseHelpers.DatabaseLocation;
import vowel.apk.databaseHelpers.DatabaseOnlineStatus;
import vowel.apk.locationActivity.LocationActivity;
import vowel.apk.volumeKeyObserver.VolumeKeyObserver;

import static com.android.volley.VolleyLog.TAG;

public class NetworkingService extends Service {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    DatagramPacket dp;
    DatagramSocket datagramSocket;
    DatagramSocket broadcastSocket;
    String loginData;
    SharedPreferences ecNumber;
    DatabaseOnlineStatus databaseOnlineStatus;
    DatabaseHelper databaseHelper;
    DatagramSocket locSocket;

    //get ec number from  shared preferences
    String getPref() {
        return ecNumber.getString("ecNumber", null);
    }

    WifiManager wifi;
    WifiManager.MulticastLock lock;
    DhcpInfo dhcpInfo;
    String gateway;
    int broadcast;
    String broadcastAddress;
    public String getUserDetail(){
        return loginData+"__~~__"+databaseHelper.getUsernameDetail(loginData);
    }
    DatabaseLocation databaseLocation;



    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //initialize
        ecNumber = PreferenceManager.getDefaultSharedPreferences(this);
        loginData = getPref();
        databaseHelper = new DatabaseHelper(this);
        databaseOnlineStatus = new DatabaseOnlineStatus(this);
        databaseOnlineStatus.deleteData();
        databaseLocation = new DatabaseLocation(this);
        init();



    }
    ListeningThread listeningThread;
    DatagramReceiveThread datagramReceiveThread;
    LocationReceiveThread locationReceiveThread;
    SendingThread sendingThread;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startService(new Intent());
        Toast.makeText(this, "service starting", Toast.LENGTH_LONG).show();

// get permissions  at runntime
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( new BootstrapActivity(),
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);}
        else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED){
            wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null){
                lock = wifi.createMulticastLock("multicastLock");

                lock.acquire();
                dhcpInfo =  wifi.getDhcpInfo();
                //get the  broadcast address from dhcp
                broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
            }

            gateway = intToIp(dhcpInfo.gateway);
            broadcastAddress = intToIp(broadcast);

            //start threads
            listeningThread = new ListeningThread();
            sendingThread = new SendingThread();
            locationReceiveThread = new LocationReceiveThread();
            datagramReceiveThread =new DatagramReceiveThread();

            try {

                datagramSocket = new DatagramSocket();
                broadcastSocket = new DatagramSocket();
                locSocket = new DatagramSocket();

                datagramSocket.setReuseAddress(true);
                broadcastSocket.setReuseAddress(true);
                locSocket.setReuseAddress(true);

                //bind sockets
                datagramSocket = new DatagramSocket(7523);
                broadcastSocket = new DatagramSocket(7579);
                locSocket = new DatagramSocket(7544);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        OnlineRefresher onlineRefresher = new OnlineRefresher();
        onlineRefresher.start();

        listeningThread.start();
        sendingThread.start();
        locationReceiveThread.start();
        datagramReceiveThread.start();

        return super.onStartCommand(intent,flags,startId);
    }



    @Override
    public void onDestroy(){

        mVolumeKeyObserver.stopListen();

        if(!listeningThread.isAlive()&&
        !datagramReceiveThread.isAlive()&&
        !locationReceiveThread.isAlive()&&
        !sendingThread.isAlive())
        {

        broadcastSocket.close();
        datagramSocket.close();
        locSocket.close();
        }
        super.onDestroy();

    }

    //show the UNDER ATTACK notification on the screen
    public void notifySos(String ip){
        Timber.tag("Vibrate---->").i("Just called vibrate");
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE));
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
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.loc_red_notif);
        builder.setContentTitle("MAN DOWN");
        builder.setContentText(databaseHelper.ipToUsername(ip)
                 +" is under attack");
        builder.setContentInfo("Please click the notification to view the location");
        builder.setWhen(System.currentTimeMillis());

        Intent activityIntent = new Intent(this, LocationActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activityIntent.putExtra("EC",databaseHelper.ipToEC(ip));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        mNotification = builder.build();
        startForeground(FOREGROUND_ID, mNotification);

    }

//add a user to database
        public void addToDB(String [] message) {
        if(!isInDB(message[1].split("__~~__")[0])) {
            databaseHelper.insertData(message[1].split("__~~__")[1].split(" ")[0],
                    message[1].split("__~~__")[1].split(" ")[1],
                    message[1].split("__~~__")[0],
                    "UserPassword", message[0]);
        }
    }


//update the ip address for user
    public void updateIPs(String[] message) {
        if (isInDB(message[1])){

        databaseHelper.updateIP(message[1], message[0]);
            Timber.tag("BreakPointCheck").d("Trying to update");
        }
        if (!message[1].equals(loginData)) {
            Timber.tag("BreakPointCheck").d("Updated");
            databaseOnlineStatus.insertStatus(message[1], message[0]);
        }
    }

//send a request message
    public void sendDetailRequest(String [] message) throws IOException {
        sendDatagramMessage("REQ", InetAddress.getByName(message[0]));
    }
    //send a message with user details
    public void sendDetail(String [] message) throws IOException {
        sendDatagramMessage(getUserDetail(), InetAddress.getByName(message[0]));
    }

//check if user is logged on
    public boolean isLogged(){

        return getPref() != null;
    }
//check if user is in databasee
    public boolean isInDB(String message) {
        Cursor cursor = databaseHelper.getUsername(message);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
         return true;

    }



//convert the ip address obtain from the dhcp to a string
    public String intToIp(int i) {
        String ip = ((i >> 24 ) & 0xFF ) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ( i & 0xFF) ;
        String[] array =ip.split("\\.");

        return array[3]+"."+array[2]+"."+array[1]+"."+array[0];
    }



//send a datagram message

    public void     sendDatagramMessage(String message, InetAddress group) throws IOException {
        byte [] messageByte = message.getBytes(StandardCharsets.UTF_8);
        dp = new DatagramPacket(messageByte, messageByte.length, group,7523);
        datagramSocket.send(dp);
    }

    public void sendBroadcastMessage(String message, InetAddress group) throws IOException {
        byte [] messageByte = message.getBytes(StandardCharsets.UTF_8);
        dp = new DatagramPacket(messageByte, messageByte.length, group,7579);
        DatagramSocket socket = new DatagramSocket();
        socket.send(dp);
    }



    //get datagram message
    public String[] getDatagramMessage() throws IOException {
        byte[] buffer = new byte[2400];
        DatagramPacket receivedData = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(receivedData);
        Timber.tag("Datagram_Locator").d("Inside Datagram after receiving");
        String [] content = new String[2];
        InetAddress source = receivedData.getAddress();
        content[0] = source.toString();
        content[1] = new String(receivedData.getData(), 0, receivedData.getLength());
        Log.i("Thread_Exit_Lcocate","Decoding in datagram, .... Sender Datagram:  " + content[0] + "  /tMessage:  " + content[1]);
        return content;


    }

    public String[] getBroadcastMessage() throws IOException {
        byte[] buffer = new byte[2400];
        DatagramPacket receivedData = new DatagramPacket(buffer, buffer.length);
        broadcastSocket.receive(receivedData);
        Timber.tag("Datagram_Locator").d("Inside FakeMulticast after receiving");
        String [] content = new String[2];
        InetAddress source = receivedData.getAddress();
        content[0] = source.toString();
        content[1] = new String(receivedData.getData(), 0, receivedData.getLength());
        Log.d("Thread_Exit_Lcocate","Multicast Sender:  " + content[0] + "  tMessage:  " + content[1]);
        return content;

    }

    public String[] getLocation() throws IOException {
        byte[] buffer = new byte[2400];
        DatagramPacket receivedData = new DatagramPacket(buffer, buffer.length);
        locSocket.receive(receivedData);
        String [] content = new String[4];
        String message = new String(receivedData.getData(), 0, receivedData.getLength());
        content[0] = message.substring(0,3);
        content[1] = message.substring(3, 8);
        content[2] = message.substring(8).split("~~__")[0];
        content[3] = message.substring(8).split("~~__")[1];
        Log.i("Location Message","----------------------------------------------------->"+Arrays.toString(content));
        return content;

    }



    class ListeningThread extends Thread {

        ListeningThread() { }

        public void run() {

            Timber.tag("DebugTag").d("The listening activity is now running");
            while (true) {

                if (isLogged()) {

                    String[] contentMulticast = new String[2];


                    try {


                        if (getBroadcastMessage() != null){
                            contentMulticast = getBroadcastMessage();
                        contentMulticast[0] = contentMulticast[0].split("/")[1];

                    }
                    } catch (SocketException e ) {
                        Timber.tag("MessageTag").d("Failed" + e);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Timber.tag("MessageTag").d("Failed: " + e);
                    }


                        if (contentMulticast[1].equals("SOS")) {//vibrate and notify
                            if(!databaseHelper.getUsernameDetail(loginData).equals(databaseHelper.ipToUsername(contentMulticast[0])))
                            notifySos(contentMulticast[0]);
                        }
                        else if ( !isInDB(contentMulticast[1])){       //
                                try {
                                    sendDetailRequest(contentMulticast);
                                    updateIPs(contentMulticast);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        else {
                            updateIPs(contentMulticast);
                        }



                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    class SendingThread extends Thread {

        SendingThread() {}

        public void run() {
//
            Timber.tag("DebugTag").d("The Sending activity is now running");
            while (true) {

                if ((isLogged())){

                try {

                    Timber.tag("DebugTag").d("About to start sending");


                    sendBroadcastMessage(loginData, InetAddress.getByName(broadcastAddress));

                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Timber.tag("DebugTag").d("Sending failed" + ": " + e);
                }

            }

        }
    }
    }

    class DatagramReceiveThread extends Thread{
        DatagramReceiveThread(){}

        @Override
        public void run() {

            while (true){
                if (isLogged()) {


                    String[] contentDatagram = new String[2];


                    Timber.tag("DatagramThread").d("Thread Started");
                    try {
                        if (getDatagramMessage() != null){
                            if (!datagramSocket.isClosed()) contentDatagram = getDatagramMessage();
                        contentDatagram[0] = contentDatagram[0].split("/")[1];
                        //contentDatagram = getBroadcastMessage();


                    }
                    } catch (SocketException e) {
                        Timber.tag("MessageTag").d("Failed" + e);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Timber.tag("MessageTag").d("Failed: " + e);
                    }
                    if (contentDatagram[1].equals("REQ")) {
                        try {
                            sendDetail(contentDatagram);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!contentDatagram[1].equals(loginData)){
                        if (!contentDatagram[1].split("__~~__")[1].isEmpty()) {
                            addToDB(contentDatagram);
                        }
                }
                }
                }
        }
    }
//refress the list of online users
    class OnlineRefresher extends Thread{
        OnlineRefresher(){}

        @Override
        public void run() {
            while(true){

                try{    databaseOnlineStatus.deleteData();
                        Thread.sleep(900000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private VolumeKeyObserver mVolumeKeyObserver;

    private void init() {
    mVolumeKeyObserver = new VolumeKeyObserver(this);
        mVolumeKeyObserver.setVolumeKeyListener(new VolumeKeyObserver.OnVolumeKeyListener() {
        @Override
        public void onVolumeKeyPressed() {
            Timber.tag(TAG).i("----> Volume button");
            System.out.println("----> Volume button");


        }
    });

        mVolumeKeyObserver.startListen();
        }


    class LocationReceiveThread extends Thread{
        LocationReceiveThread(){}

        @Override
        public void run() {

            while (true){
                if (isLogged()) {


                    String[] contentDatagram;

                    try {


                            contentDatagram = getLocation();
                        if(contentDatagram[0].equals("coL")){
                            if(!databaseLocation.isInDatabase(contentDatagram[1]))
                                databaseLocation.insertLocation(contentDatagram[1], Double.parseDouble(contentDatagram[2]), Double.parseDouble(contentDatagram[3]));
                            else databaseLocation.updateLocation(contentDatagram[1], Double.parseDouble(contentDatagram[2]), Double.parseDouble(contentDatagram[3]));
                        }
                        Thread.sleep(1000);
                    } catch (SocketException e) {
                        Timber.tag("MessageTag").d("Failed" + e);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Timber.tag("MessageTag").d("Failed: " + e);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

}

