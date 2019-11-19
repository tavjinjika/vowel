package vowel.apk.notificationActivityy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import vowel.apk.R;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.databaseHelpers.DatabaseMessage;

public class FragmentMessage extends Fragment implements View.OnClickListener {

    private DatabaseMessage myMDb;
    private DatabaseHelper myDb;
    private EditText composeText;
    private String  LOG_TAG = "FRAGMENT SENDER";
    private ArrayList<String> contactIp;
    private ArrayList<String> timeList;
    private boolean LISTEN = false;
    private int BUF_SIZE = 1024;
    private boolean IN_CALL = false;
    private ListView messageList;
    private FloatingActionButton delete;
    private ArrayList<String> undeliveredList;
    private DatagramSocket socket;
    ArrayAdapterMessage arrayAdapter;

    public FragmentMessage() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_listview_main, container, false);
        messageList = view.findViewById(R.id.messageList);
        composeText = view.findViewById(R.id.inputTextC);
        delete = view.findViewById(R.id.delete_message);
        delete.hide();
        composeText.setHint("Type Message");
        composeText.setHintTextColor(getResources().getColor(R.color.colorAccent));
        composeText.setOnClickListener(this);
        FloatingActionButton fab = view.findViewById(R.id.composeText);

        myMDb = new DatabaseMessage(getContext());
        myDb = new DatabaseHelper(getContext());
        ArrayList messageListContent = myMDb.getAllMessage();
        Collections.reverse(messageListContent);
        contactIp = new ArrayList<>();
        timeList = new ArrayList<>();
        undeliveredList = new ArrayList<>();
        undeliveredList = myMDb.getAllUdelivered();
        timeList = myMDb.getAllTimeStamp();
        Collections.reverse(timeList);

        try {
            socket = new DatagramSocket();
            socket.setReuseAddress(true);
            int BROADCAST_PORT = 46789;
            socket = new DatagramSocket(BROADCAST_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Bundle extras = getActivity().getIntent().getExtras();

           if(extras != null) contactIp = getActivity().getIntent().getStringArrayListExtra("IPs");

//        Log.d("Contents of Message",contactIp.toString());
        ArrayList<MessagePOJO> mProduct = new ArrayList<>();

        if(!messageListContent.isEmpty()){
            for (int i = 0; i < messageListContent.size(); i++) {
                mProduct.add(new MessagePOJO(myDb.getUsernameDetail(messageListContent.get(i).toString().split("_~break ")[0].trim()),
                        messageListContent.get(i).toString().split("_~break ")[1].trim(),
                        messageListContent.get(i).toString().split("_~break ")[2].trim()));
                    myDb.close();
            }
        }
        arrayAdapter = new ArrayAdapterMessage(getContext(), mProduct);
        messageList.setAdapter(arrayAdapter);


        fab.setOnClickListener(this);



        ecNumber = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = ecNumber.edit();
        editor.apply();

        messageList.setClickable(true);
        messageList.setOnItemClickListener((arg0, arg1, pos, id) -> {
            // TODO Auto-generated method stub
            delete.show();
            delete.setOnClickListener(v -> {
                myMDb.deleteMessage(timeList.get(pos));
                delete.hide();
                Toast.makeText(getContext(), "Message deleted", Toast.LENGTH_LONG).show();
            });
            Log.v("long clicked","pos: " + pos);

           // return true;
        });
        Log.d("UNDELIVERED TABLE", "-------------------------------------------------------------------> "+ undeliveredList);
        for(int i =0; i< undeliveredList.size(); i++){
            String ec = undeliveredList.get(i).split("_~break ")[0];
            String ip = myDb.getIP(ec);
            String message =  undeliveredList.get(i).split("_~break ")[1];
            resendMessage(getPref(), message,ip ,15930);
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    }
    private SharedPreferences ecNumber;

    private String getPref() {
        return ecNumber.getString("ecNumber", null);
    }
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd  'at' HH.mm.ss");      //removed G between dd and at
    private String timestamp = simpleDateFormat.format(new Date());
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case(R.id.composeText):{
                String getTextMessage = composeText.getText().toString();
                String ec = getPref();
                String time = timestamp;
                if(!contactIp.isEmpty()){
                sendMessage(ec, getTextMessage, 15930);
                startListener();
                myMDb.insertMessage(time, ec, getTextMessage, IN_CALL);}
                else
                    Toast.makeText(getContext(), "Please select the contacts", Toast.LENGTH_LONG).show();
                if(getTextMessage.equals(""))
                    Toast.makeText(getContext(), "Please type the message", Toast.LENGTH_LONG).show();
                composeText.setText("");
                messageList.setVisibility(View.VISIBLE);
                break;
            }
            case(R.id.inputTextC):{
                messageList.setVisibility(View.INVISIBLE);
                break;
            }
        }

    }
    private void sendMessage(final String sender, final String message, final int port){
        Thread replyThread = new Thread(() -> {
            if(contactIp.isEmpty()){
                return;
            }
            if(message.equals("")){
                return;
            }
            try {
                DatagramSocket socket = null;
                for(int i =0;i<contactIp.size() ;i++) {
                    InetAddress address = InetAddress.getByName(contactIp.get(i));
                    byte[] data = (sender+"__~~__"+message).getBytes();
                    socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                    myMDb.insertUndelivered(myDb.ipToEC(contactIp.get(i)), message);
                    Log.i("NEW TABLE", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+myMDb.getUndelivered(myDb.ipToEC(contactIp.get(i))));

                }
                Log.i(LOG_TAG, "Sent message( " + message + " ) to " + contactIp);

                if (socket != null) {
                    socket.disconnect();

                socket.close();}
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
        });
        replyThread.start();
    }
    private void startListener() {
        // Create listener thread
        LISTEN = true;
        Thread listenThread = new Thread(() -> {

            try {

                Log.i(LOG_TAG, "Listener started!");
                //socket = new DatagramSocket(BROADCAST_PORT);
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
                        String ip  = packet.getAddress().toString().substring(1);
                        Log.i(LOG_TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%>>>>>>>>>>>>>>>>>>>>>>> "+ip);
                        if(action.equals("ACC:")) {

                            myMDb.deleteDelivered(myDb.ipToEC(ip));
                            IN_CALL = true;
                        }

                        else {
                            // Invalid notification received
                            Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                        }
                    }
                    catch(SocketTimeoutException e) {
                        IN_CALL = false;

                            Log.i(LOG_TAG, "User not available");
                            return;

                    }
                    catch(IOException e) {
                        Log.e("IOExp",e.toString());
                    }
                }
                Log.i(LOG_TAG, "Listener ending");
                socket.disconnect();
                socket.close();
                return;
            }
            catch(SocketException e) {

                Log.e(LOG_TAG, "SocketException in Listener"+e);
                IN_CALL = false;
            }
        });
        listenThread.start();
    }

    private void resendMessage(final String sender, final String message, final String ip ,final int port){
        Thread replyThread = new Thread(() -> {

            try {
                DatagramSocket socket;

                    InetAddress address = InetAddress.getByName(ip);
                    byte[] data = (sender+"__~~__"+message).getBytes();
                    socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);


                Log.i(LOG_TAG, "Sent message( " + message + " ) to " + undeliveredList);

                if (socket != null) {
                    socket.disconnect();

                    socket.close();}
            }
            catch(UnknownHostException e) {

                Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + undeliveredList);
            }
            catch(SocketException e) {

                Log.e(LOG_TAG, "Failure. SocketException in sendMessage: " + e);
            }
            catch(IOException e) {

                Log.e(LOG_TAG, "Failure. IOException in sendMessage: " + e);
            }
        });
        replyThread.start();
        startListener();
    }

}
