package vowel.apk.notificationActivityy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import vowel.apk.R;


public class NotificationActivity extends AppCompatActivity {

    FloatingActionButton fab;
    ViewPager viewPager;
    TabLayout tabs;


    Fragment fragment;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if the fragment is an instance of the right fragment
        if (fragment instanceof FragmentMessage) {
            FragmentMessage my = (FragmentMessage) fragment;
            // Pass intent or its data to the fragment's method
           // my.processNFC(intent.getStringExtra());

        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        viewPager = findViewById(R.id.view_pager);
        setupViewPager(viewPager);


        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        fab = findViewById(R.id.fabCall);


    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentMessage(), "MESSAGES        ");
        adapter.addFragment(new FragmentMissedCall(), "     MISSED CALLS");
        viewPager.setAdapter(adapter);

    }


}
        class ViewPagerAdapter extends androidx.fragment.app.FragmentPagerAdapter {
            private final List<Fragment> mFragmentList = new ArrayList<>();
            private final List<String> mFragmentTitleList = new ArrayList<>();

            //ViewPagerAdapter deprecated-----shifting from FragmentPagerAdapter
            ViewPagerAdapter(FragmentManager manager) {
                super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            }


            @Override
            @NonNull
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }

            void addFragment(Fragment fragment, String title) {
                mFragmentList.add(fragment);
                mFragmentTitleList.add(title);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentTitleList.get(position);
            }

        }