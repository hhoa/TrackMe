package com.example.hhoa.trackme;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tracker extends AppCompatActivity implements
        FragmentAccount.OnFragmentInteractionListener,
        FragmentTracking.OnFragmentInteractionListener {

    //firebase auth
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;
    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        final FragmentTracking fragmentTracking = FragmentTracking.newInstance("Tracking");
        final FragmentAccount fragmentAccount = FragmentAccount.newInstance();
        final FragmentHistory fragmentHistory = FragmentHistory.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentMain, fragmentTracking)
                .commit();

        flag = 1;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_tracking:
                                if (flag != 1) {
                                    flag = 1;
                                    item.setChecked(true);
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragmentMain, fragmentTracking)
                                            .commit();
                                }

                                break;
                            case R.id.action_history:
                                if (flag != 2)
                                {
                                    flag = 2;
                                    item.setChecked(true);
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragmentMain, fragmentHistory)
                                            .commit();
                                }

                                break;
                            case R.id.action_account:
                                if (flag != 3)
                                {
                                    flag = 3;
                                    item.setChecked(true);
                                    getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragmentMain, fragmentAccount)
                                            .commit();
                                }

                                break;
                        }
                        return false;
                    }
                });
    }

    @Override
    public void onFragmentInteraction() {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
