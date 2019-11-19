package vowel.apk.bootstrapActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;

import vowel.apk.R;
import vowel.apk.callActivity.CallActivity;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.locationActivity.LocationActivity;
import vowel.apk.loginActivity.LoginActivity;
import vowel.apk.notificationActivityy.NotificationActivity;
import vowel.apk.onCallActivityy.OnCallActivity;

public class BootstrapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
        TextView userId;
        String userEc;
        DatabaseHelper db;
        Cursor cursor;
        BootstrapFragment bootstrapFragment;
        ArrayList<String> ecList;
        ArrayList<String> myIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inflating the view
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initializing the SharedPreferences
        ecNumber = PreferenceManager.getDefaultSharedPreferences(this);
        editor = ecNumber.edit();
        editor.apply();
        userEc = getPref();

        ecList = new ArrayList<>();
        db = new DatabaseHelper(this);
        ecList = db.getEcList();
        Log.i("Check EC", userEc);
        ecList.remove(userEc);
        myIP = new ArrayList<>();
        for(int i =0; i<ecList.size(); i++){
            Collections.addAll(myIP, db.getIP(ecList.get(i)));
        }

        //getting the permission to write to external storage at runtimee
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
            } else {
                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }


            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TAG", "Permission is granted");
                } else {
                    Log.v("TAG", "Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            }

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TAG", "Permission is granted");
                } else {
                    Log.v("TAG", "Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }

        }

        // Initializing the fragment that appears on the screen for the activity
        bootstrapFragment = new BootstrapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_map, bootstrapFragment);
        fragmentTransaction.commit();

        //inflating the drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        userId = header.findViewById(R.id.myusername);


        isLogged();
    }

    //Checking if the user is logged in
    private void isLogged() {
        userEc = getPref();
        if(userEc != null){
            cursor = db.getUsername(userEc);
            String name = null;
            String surname = null;
            if (cursor.moveToFirst()) {
                name = cursor.getString(0);
                surname = cursor.getString(1);}
            String username = name + " "+ surname;
            userId.setText(username);
            db.close();
        }
        else{
            //Starting the Login intent if the user is not logged in
            Intent loginIntent = new Intent(BootstrapActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }



//Close the Navigation drawer if a back button is clicked
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //If this  Item is clicked, send  data to the next activty
        if (id == R.id.nav_broadcast) {
            Intent intent = new Intent(BootstrapActivity.this, OnCallActivity.class);
            intent.putStringArrayListExtra("ECs",ecList);
            intent.putStringArrayListExtra("IPs", myIP);

            intent.putExtra("CALLER", userEc);
//start the onCall Activity
            startActivity(intent);

        }

        //if this item is selected, send data to the next activity
        else if(id == R.id.nav_broadcastMessages){
            Intent intent = new Intent(BootstrapActivity.this, NotificationActivity.class);
            intent.putStringArrayListExtra("ECs",ecList);
            intent.putStringArrayListExtra("IPs", myIP);

            intent.putExtra("Sender", userEc);
// Start the Notification Activity
            startActivity(intent);
        }
        else if (id == R.id.nav_calls) {
            Intent callIntent = new Intent(BootstrapActivity.this, CallActivity.class);
            startActivity(callIntent);
        }

        //if this item is selected, start the  notification activity
        else if (id == R.id.nav_notifications) {
            Intent notificationIntent = new Intent(BootstrapActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        }

        //start the Location activity
        else if (id == R.id.nav_location) {
            Intent bootstrapIntent = new Intent(BootstrapActivity.this, LocationActivity.class);
            startActivity(bootstrapIntent);
        }

        //change password
        else if (id == R.id.nav_change_p) {
            //inflate the dialog
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.dialog_update_password, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = promptsView
                    .findViewById(R.id.editTextDialogUserInputNewP);

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("UPDATE PASSWORD",
                            (dialog, idx) -> {
                                // get user input and set it to result
                                // edit text
                                db.updatePassword(getPref(), String.valueOf(userInput.getText()));
                                deletePref();
                            })
                    .setNegativeButton("CANCEL",
                            (dialog, idx) -> dialog.cancel());

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

            isLogged();
        }

        //delete account
        else if (id == R.id.nav_delete_a) {
                showAlertDialog();
        }
        //logout
        else if (id == R.id.nav_logout) {
            deletePref();
            isLogged();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
//Show the dialog to delete an account

    private void showAlertDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("DELETE ACCOUNT");
        builder.setMessage("Are you sure you want to delete your account?");
        builder.setPositiveButton("YES", (dialogInterface, which) -> {
            db.deleteData(getPref());
            db.close();
            deletePref();
            isLogged();
            finish();
        });
        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.create().show();
    }



        SharedPreferences ecNumber;
        SharedPreferences.Editor editor;

//get the shared preference
        String getPref() {
            return ecNumber.getString("ecNumber", null);
        }
//delete the shared preference
        void deletePref() {
            editor.remove("ecNumber");
            editor.apply();

        }


}
