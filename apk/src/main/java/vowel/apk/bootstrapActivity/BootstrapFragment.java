package vowel.apk.bootstrapActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import vowel.apk.R;
import vowel.apk.callService.CallService;
import vowel.apk.chatService.ChatService;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.databaseHelpers.DatabaseOnlineStatus;
import vowel.apk.loginActivity.LoginActivity;
import vowel.apk.networkingServices.NetworkingService;

public class BootstrapFragment extends Fragment implements View.OnClickListener  {

  public BootstrapFragment(){}

  Button connectB;
  String loginData;
  DatabaseHelper myDb;
  DatabaseOnlineStatus databaseOnlineStatus;
  ArrayList onlineUsers;
  ListView availableUser;


  //inflate the fragment
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.activity_bootstrap, container, false);
    connectB = view.findViewById(R.id.btnConnect);
    connectB.setOnClickListener(this);
    availableUser = view.findViewById(R.id.available);


//initialize the databases
    myDb = new DatabaseHelper(getContext());
    databaseOnlineStatus = new DatabaseOnlineStatus(getContext());



    ecNumber = PreferenceManager.getDefaultSharedPreferences(getContext());
    loginData = getPref();
    onlineUsers = databaseOnlineStatus.getAllOnlineUsers();

    String detail;



    ArrayList<OnlinePojo> mProduct = new ArrayList<>();
    if(!onlineUsers.isEmpty()){
      for(int i=0; i<onlineUsers.size();i++){
        detail = myDb.getUsernameDetail(onlineUsers.get(i).toString());
        mProduct.add(new OnlinePojo(detail));
      }
    }
    arrayAdapter = new BootstrapAdapter(getContext(), mProduct);
    availableUser.setAdapter(arrayAdapter);

    checkConnection();


    isLogged();

    return view;
  }
  BootstrapAdapter arrayAdapter;

//Check if the Database has Online users present
  private void checkConnection() {
    if(!onlineUsers.isEmpty()){
      connectB.setText(getString(R.string.disconnect));
    }
    else {
      connectB.setText(getString(R.string.connect));
    }
  }

  public void onClick(View view) {
//Start the background services
    if(connectB.getText().toString().equals("Connect")){
      Toast.makeText(getContext(), String.valueOf(onlineUsers.size()), Toast.LENGTH_LONG).show();
      getActivity().startService(new Intent(getContext(), NetworkingService.class));
      getActivity().startService(new Intent(getContext(), CallService.class));
      getActivity().startService(new Intent(getContext(), ChatService.class));
      connectB.setText(getString(R.string.disconnect));
    }
    //stop the background services
    else{
        getActivity().stopService(new Intent(getContext(), NetworkingService.class));
        getActivity().stopService(new Intent(getContext(), CallService.class));
        getActivity().stopService(new Intent(getContext(), ChatService.class));
        databaseOnlineStatus.deleteData();
        connectB.setText(getString(R.string.connect));
    }
  }

// check if the user is logged on
  private void isLogged() {
    if (loginData == null) {
      Intent mapIntent = new Intent(getContext(), LoginActivity.class);

      startActivity(mapIntent);
    }
  }



  private SharedPreferences ecNumber;
  //get the shared preference
  private String getPref() {
    return ecNumber.getString("ecNumber", null);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

  }


}
