package vowel.apk.loginActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import vowel.apk.R;
import vowel.apk.bootstrapActivity.BootstrapActivity;
import vowel.apk.databaseHelpers.DatabaseHelper;


public class LoginActivity extends AppCompatActivity {
    EditText fname;
    EditText s_name;
    EditText p_word;
    EditText ec;
    Button bsign, b_log;
    DatabaseHelper myDb;
    TextView welcomeNote;
    RelativeLayout editText;
    String loginData;


//iflate view and initialize
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            fname = findViewById(R.id.f_name);
            s_name = findViewById(R.id.s_name);
            p_word = findViewById(R.id.password);
            bsign = findViewById(R.id.btnSign_up);
            b_log = findViewById(R.id.btnLogin);
            ec = findViewById(R.id.ec_number);
            myDb = new DatabaseHelper(this);
            welcomeNote = findViewById(R.id.w_note);
            editText = findViewById(R.id.edit_text_layout);

            ecNumber = PreferenceManager.getDefaultSharedPreferences(this);
            editor = ecNumber.edit();
            editor.apply();
            myDb = new DatabaseHelper(this);
            isLogged();
    }


    //check if the text provided is valid
    private boolean checkValidity(ViewGroup myEditText) {
        for (int i = 0; i < myEditText.getChildCount(); i++) {
            View view = myEditText.getChildAt(i);
            if ((view instanceof EditText)) {
                String checkText = ((EditText) view).getText().toString();
                if (checkText.equals("")) {
                    ((EditText) view).setError("Please Enter " + ((EditText) view).getHint());
                    return false;
                }
            }
        }
        return true;
    }

    //the ip address of the device
    public String getIP() {
        String myip;
        try {
            InetAddress address = InetAddress.getLocalHost();
            myip = address.getHostAddress();
            return myip;
        } catch (Exception e) {
            myip = "0.0.0.0";
            e.printStackTrace();
            return myip;
        }
    }

    public void onClick(View view) {
        final boolean b;
        final String[] ip = new String[1];
        checkValidity(editText);
        b = checkValidity(editText);
         switch(view.getId()){
             //Add to database and move to next activity
            case(R.id.btnSign_up):
                String first_name = fname.getText().toString().trim();
                String surname = s_name.getText().toString().trim();
                String ecNumber = ec.getText().toString().trim();
                String password = p_word.getText().toString().trim();

                //inflate dialog to prompt for passphrase
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.prompt_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // set prompts.xml to alert dialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("ENTER",
                                (dialog, id) -> {
                                    // get user input and set it to result
                                    // edit text
                                    if(getResources().getString(R.string.passphrase).contentEquals(userInput.getText())){
                                        try {

                                            ip[0] = new networkingTask().execute().get();

                                            if (b) {
                                                myDb.insertData(first_name, surname, ecNumber, password, ip[0]);
                                                myDb.close();
                                                loginData = (ecNumber);
                                                putPref(loginData);
                                                Intent mapIntent = new Intent(LoginActivity.this, BootstrapActivity.class);
                                                startActivity(mapIntent);
                                            }
                                        }catch ( ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                (dialog, id) -> dialog.cancel());

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();



                break;

                // Login
            case(R.id.btnLogin):
                first_name = fname.getText().toString().trim();
                surname = s_name.getText().toString().trim();
                ecNumber = ec.getText().toString().trim();
                password = p_word.getText().toString().trim();
                String name ;
                String sName;
                String passWord;
                if (b) {
                    Cursor data = myDb.getRow(ecNumber);
                    if (data.moveToFirst()) {
                        name = data.getString(0);
                        sName = data.getString(1);
                        passWord = data.getString(3);
                        myDb.close();

                        //check if the provided info is the same as that in the database
                        if ((name.equalsIgnoreCase(first_name)) && (surname.equalsIgnoreCase(sName)) && (password.equals(passWord))) {
                            if(b_log.getText().toString().equals("Save")){
                                myDb.updatePassword(ecNumber, password);
                                myDb.close();
                                Toast.makeText(this, "New Password saved!!!", Toast.LENGTH_LONG).show();
                                isLogged();
                            }
                            loginData = ecNumber;
                            putPref(loginData);
                            Intent mapIntent = new Intent(LoginActivity.this, BootstrapActivity.class);
                            startActivity(mapIntent);
                        }
                        else{
                            Toast.makeText(this, "Incorrect Log in details!!!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(this,"Incorrect Login details!!!",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(this,"Log in Failed, fill in the details as specified!",Toast.LENGTH_LONG).show();
                }

                break;
        }
    }



//check if the user is logged on
    public void isLogged(){
       // loginData = mySharedPref.getPref();
        if(getPref() != null){
            Intent mapIntent = new Intent(LoginActivity.this, BootstrapActivity.class);
            startActivity(mapIntent);
    }
        else {
            Toast.makeText(this,"Please Log in!",Toast.LENGTH_SHORT).show();
        }
    }




       SharedPreferences ecNumber;
        SharedPreferences.Editor editor;

//add ec number to shared preference
        void putPref(String inputValue){
            editor.putString("ecNumber", inputValue);
            editor.apply();
        }

        //get the ec number from shared preferences
        String getPref() {
            return ecNumber.getString("ecNumber", null);
        }


        //get the ip address in background
    private class networkingTask extends AsyncTask<String, String, String>{
            protected String doInBackground(String ... ip){
                return getIP();
            }
}


    }

