package vowel.apk.callActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import vowel.apk.R;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.notificationActivityy.NotificationActivity;
import vowel.apk.onCallActivityy.OnCallActivity;

public class CallActivity extends Activity implements View.OnClickListener {

    FloatingActionButton floatingActionButtonCall;
    FloatingActionButton floatingActionButtonMessage;
    ListView listview;

    ArrayList<Contactlist> mProducts;
    InteractiveArrayAdapter<Contactlist> mAdapter;
    DatabaseHelper myDb;
    ArrayList<String> contactList;
    ArrayList<String> ecList;
    SharedPreferences ecNumber;
    String loginData;


    //inflate the view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_contacts);
        myDb = new DatabaseHelper(this);

        ecList = myDb.getEcList();
        ecNumber = PreferenceManager.getDefaultSharedPreferences(this);
        loginData = getPref();


        bindComponents();

        init();

        addListeners();



    }

//set the layout
    private void bindComponents() {
        listview = findViewById(R.id.contacts);
        floatingActionButtonCall = findViewById(R.id.fabCall);
        floatingActionButtonMessage = findViewById(R.id.fabMessage);

    }
//initialize the arrays and databases
    private void init() {
        mProducts = new ArrayList<>();
        contactList = new ArrayList<>();
        contactList = myDb.getContactNames();
        if(!contactList.isEmpty()){

            for (int i = 0; i < contactList.size(); i++) {
            mProducts.add(new Contactlist(contactList.get(i)));

        }
        }
        mAdapter = new InteractiveArrayAdapter<>(this, mProducts);
        listview.setAdapter(mAdapter);
    }
//add click listerner
    private void addListeners() {
        floatingActionButtonCall.setOnClickListener(this);
        floatingActionButtonMessage.setOnClickListener(this);
    }
    public void onClick(View v) {

        if (mAdapter != null) {

            ArrayList<Contactlist> mArrayProducts = mAdapter.getCheckedItems();
            ArrayList<Integer> mArrayIndex = mAdapter.getIndex();

            ArrayList<String> myEc = new ArrayList<>();
            ArrayList<String> usefulEc = new ArrayList<>();
            ArrayList<String> myIP = new ArrayList<>();
            for (int i = 0; i < mArrayIndex.size(); i++) {

                if(mArrayIndex.get(i)==1) {
                    Collections.addAll(myEc, ecList.get(i));
                    Collections.addAll(usefulEc, ecList.get(i));
                    Collections.addAll(myIP, myDb.getIP(myEc.get(i)));

                }else{
                    Collections.addAll(myEc, ecList.get(i));

                }
            }

//send data to the next acytivity and start it
            switch (v.getId()){
                case(R.id.fabCall):{
                    if(!usefulEc.isEmpty() && !myIP.isEmpty()){
                    Intent intent = new Intent(CallActivity.this, OnCallActivity.class);
                    intent.putStringArrayListExtra("ECs",usefulEc);
                    intent.putStringArrayListExtra("IPs", myIP);

                    intent.putExtra("CALLER", loginData);

                    startActivity(intent);}
                    break;
                }
                case (R.id.fabMessage):{

                    Intent intent = new Intent(CallActivity.this, NotificationActivity.class);
                    intent.putStringArrayListExtra("ECs",usefulEc);
                    intent.putStringArrayListExtra("IPs", myIP);

                    intent.putExtra("CALLER", loginData);

                    startActivity(intent);
                    break;
                }
            }


            }
        }

        //get the shared preference
    String getPref() {
        return ecNumber.getString("ecNumber", null);
    }


    }

