package vowel.apk.volumeKeyObserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class VolumeKeyObserver{

    private String TAG_VOLUME = "VOLUME";
    private Context mContext;
    private IntentFilter mIntentFilter;
    private VolumeKeyObserver.OnVolumeKeyListener mOnVolumeKeyListener;
    private VolumeKeyObserver.VolumeKeyBroadcastReceiver mVolumeKeyBroadcastReceiver;

    public VolumeKeyObserver(Context context) {
        this.mContext = context;

    }

    //Registered Broadcast Receiver
    public void startListen(){
        //mIntentFilter = new IntentFilter(Intent.ACTION_VOICE_COMMAND);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        mVolumeKeyBroadcastReceiver=new VolumeKeyObserver.VolumeKeyBroadcastReceiver();
        mContext.registerReceiver(mVolumeKeyBroadcastReceiver, mIntentFilter);
        System.out.println("VolumeKey----> Start monitoring");


    }

    public void stopListen(){
        if (mVolumeKeyBroadcastReceiver!=null) {
            mContext.unregisterReceiver(mVolumeKeyBroadcastReceiver);
            System.out.println("VolumeKey----> Stop listening");
        }
    }

    // Exposure Interface
    public void setVolumeKeyListener(VolumeKeyObserver.OnVolumeKeyListener VolumeKeyListener) {
        mOnVolumeKeyListener = VolumeKeyListener;
    }

    // Callback interface
    public interface OnVolumeKeyListener {
        void onVolumeKeyPressed();
    }


    private static void sendSOS(InetAddress group) throws IOException {
        DatagramSocket broadcastAddress = new DatagramSocket();
        DatagramPacket dp;
        byte [] messageByte = "SOS".getBytes(StandardCharsets.UTF_8);
        dp = new DatagramPacket(messageByte, messageByte.length, group,7579);
        broadcastAddress.send(dp);
    }

    private static void sendSOS() throws IOException {

        sendSOS(InetAddress.getByName("255.255.255.255"));
    }


    private  class networkingTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String ... ip){
            try {
                sendSOS();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }





    //Broadcast receiver
    class VolumeKeyBroadcastReceiver extends BroadcastReceiver {

        private boolean flag;// = false;
        long startTime =0, endTime =0, dif =0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "android.media.VOLUME_CHANGED_ACTION")) {
                Log.i(TAG_VOLUME, "VolumeKey----> Hearing the volume adjustment");
                System.out.println("VolumeKey----> Hearing the volume adjustment");
                networkingTask nt = new networkingTask();


                if(!flag){startTime = System.currentTimeMillis();
                    dif = (startTime - endTime);
                    if(dif<0)dif =dif*-1;
                    System.out.println("The difference in if ----->"+ dif);
                flag = true;}
                else if(flag){
                    endTime = System.currentTimeMillis();
                    if (dif<0)dif = dif*-1;
                    System.out.println("The difference in else----->"+ dif);
                    flag = false;
                }
                if(dif<2000)nt.execute();
                System.out.println("The difference ----->"+ dif);
            }

        }


    }
}


