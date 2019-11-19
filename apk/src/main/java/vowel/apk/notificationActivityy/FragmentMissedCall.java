package vowel.apk.notificationActivityy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import vowel.apk.R;
import vowel.apk.callActivity.CallActivity;
import vowel.apk.databaseHelpers.DatabaseCall;
import vowel.apk.databaseHelpers.DatabaseHelper;

public class FragmentMissedCall  extends Fragment implements View.OnClickListener {
    public FragmentMissedCall() {
    }
    private FloatingActionButton delete;
    private ArrayList<String> timeList;
    DatabaseCall myCDb;
    DatabaseHelper myDb;
    ArrayAdapterCallNotification arrayAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calls_listview_notifn, container, false);
        ListView missedCallList = view.findViewById(R.id.missedCallList);

        timeList = new ArrayList<>();
        ecNumber = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = ecNumber.edit();
        editor.apply();

        delete = view.findViewById(R.id.delete_missed_call);
        delete.hide();

        myCDb = new DatabaseCall(getContext());
        myDb = new DatabaseHelper(getContext());
        ArrayList missedCallListArray = myCDb.getAllCalls();
        myDb.close();
        Collections.reverse(missedCallListArray);
        ArrayList<MissedCallsPOJO> mProduct = new ArrayList<>();

        if(!missedCallListArray.isEmpty()){
            for (int i = 0; i < missedCallListArray.size(); i++) {
                mProduct.add(new MissedCallsPOJO(myDb.getUsernameDetail(missedCallListArray.get(i).toString().split("_~break")[0].trim()),
                        missedCallListArray.get(i).toString().split("_~break")[1].trim()));
                myDb.close();
            }
        }
        arrayAdapter = new ArrayAdapterCallNotification(getContext(), mProduct);
        missedCallList.setAdapter(arrayAdapter);

        FloatingActionButton fab = view.findViewById(R.id.makeCall);
        fab.setOnClickListener(this);

        timeList = myCDb.getAllTimeStamp();
        Collections.reverse(timeList);
        missedCallList.setClickable(true);
        missedCallList.setOnItemClickListener((arg0, arg1, pos, id) -> {
            // TODO Auto-generated method stub
            delete.show();
            delete.setOnClickListener(v -> {
                myCDb.deleteMissedCall(timeList.get(pos));
                delete.hide();
                Toast.makeText(getContext(), "Missed call deleted", Toast.LENGTH_LONG).show();
            });
            Log.v("long clicked","pos: " + pos);


        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), CallActivity.class);
        startActivity(intent);
    }
    private SharedPreferences ecNumber;

}
