package com.example.hhoa.trackme;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Tracker extends AppCompatActivity implements
        FragmentAccount.OnFragmentInteractionListener,
        FragmentTracking.OnFragmentInteractionListener,
        FragmentHistory.OnFragmentInteractionListener{

    //firebase auth
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;
    private int flag;
    private static FragmentHistory fragmentHistory;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        if (mFirebaseUser != null)
            uID = mFirebaseUser.getUid();
        else
            uID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final FragmentTracking fragmentTracking = FragmentTracking.newInstance(uID);
        final FragmentAccount fragmentAccount = FragmentAccount.newInstance();
        fragmentHistory = FragmentHistory.newInstance(uID);

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
    public void onFragmentInteractionTracking() {
        // TODO: error when not moving to history at first
        fragmentHistory.getDataUserFromFirebase(uID);
    }

    @Override
    public void onFragmentInteractionHistory() {

    }

    @Override
    public void onBackPressed() {
        boolean onSave = true;
        if(onSave) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.confirm_text);
            builder.setMessage(R.string.quit_text);

            builder.setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
//                    finishAndRemoveTask();
                    finish();
                }
            });

            builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
        }
    }
}
