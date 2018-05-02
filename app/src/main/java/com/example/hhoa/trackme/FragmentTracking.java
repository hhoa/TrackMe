package com.example.hhoa.trackme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentTracking.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentTracking#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTracking extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_UID = "param1";
    private static final long DEFAULT_MIN_TIME = 1000;      // IN MILLISEC
    private static final float DEFAULT_MIN_DISTANCE = 1;    // IN METER


    // TODO: Rename and change types of parameters
    private String mUID;
    private String locationProvider;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap myMap;
    private Location prevLoc = null;
    private SupportMapFragment mapFragment;

    private OnFragmentInteractionListener mListener;
    private int currState;
    private ImageView btnRecordPause;
    private ImageView btnReplay;
    private ImageView btnStop;
    private Marker firstMarker;
    private boolean firstFlagRecord = false;
    private TextView txtDistance;
    private TextView txtSpeed;
    private TextView txtTime;

    private double currDistance;
    private double currSpeed;
    private long currTime;
    private Timer myTimer;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private ArrayList<Double[]> listLoc;
    private ArrayList<Double> listSpeed;

    public FragmentTracking() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment FragmentTracking.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTracking newInstance(String param1) {
        FragmentTracking fragment = new FragmentTracking();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_UID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUID = getArguments().getString(ARG_PARAM_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_tracking, container, false);
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int stateDefaultValue = getResources().getInteger(R.integer.saved_state_default_key);
        currState = sharedPref.getInt(getString(R.string.saved_state_key), stateDefaultValue);

        int distanceDefaultValue = getResources().getInteger(R.integer.saved_distance_default_key);
        currDistance = sharedPref.getInt(getString(R.string.saved_state_key), distanceDefaultValue);

        int speedDefaultValue = getResources().getInteger(R.integer.saved_speed_default_key);
        currSpeed = sharedPref.getInt(getString(R.string.saved_state_key), speedDefaultValue);

        int timeDefaultValue = getResources().getInteger(R.integer.saved_state_default_key);
        currTime = sharedPref.getInt(getString(R.string.saved_state_key), timeDefaultValue);

        btnRecordPause = v.findViewById(R.id.btn_rec_pause);
        btnRecordPause.setOnClickListener(this);
        btnReplay = v.findViewById(R.id.btn_replay);
        btnReplay.setOnClickListener(this);
        btnStop = v.findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);

        txtDistance = v.findViewById(R.id.txt_distance);
        txtSpeed = v.findViewById(R.id.txt_speed);
        txtTime = v.findViewById(R.id.txt_time);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("lib").child(mUID);

        listLoc = new ArrayList<>();
        listSpeed = new ArrayList<>();


        updateButtonUI();

        return v;
    }

    private void updateButtonUI() {
        switch (currState) {
            case 1:
                btnReplay.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnRecordPause.setVisibility(View.VISIBLE);
                btnRecordPause.setImageResource(R.drawable.ic_record);
                break;
            case 2:
                btnReplay.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnRecordPause.setVisibility(View.VISIBLE);
                btnRecordPause.setImageResource(R.drawable.ic_pause);
                break;
            case 3:
                btnRecordPause.setVisibility(View.INVISIBLE);
                btnReplay.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        userLocation();
        mapFragment.getMapAsync(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteractionTracking();
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        updateState();
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (NullPointerException e) {
                Log.e(TAG, "Error while attempting removeUpdates, ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    private void updateState() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_state_key), currState);
        editor.apply();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker
        // and move the map's camera to the same location.
        googleMap.setMinZoomPreference(10.0f);
        googleMap.setMaxZoomPreference(20.0f);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (prevLoc == null)
            prevLoc = locationManager.getLastKnownLocation(locationProvider);

        LatLng sydney = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
        firstMarker = googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Current Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney , 16.0f));
        myMap = googleMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: mRequestingLocationUpdates
        updateButtonUI();
//        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//        int stateDefaultValue = getResources().getInteger(R.integer.saved_state_default_key);
//        currState = sharedPref.getInt(getString(R.string.saved_state_key), stateDefaultValue);
//        switch (currState) {
//            case 1:
//
//                break;
//            case 2:
//                btnReplay.setVisibility(View.INVISIBLE);
//                btnStop.setVisibility(View.INVISIBLE);
//                btnRecordPause.setVisibility(View.VISIBLE);
//                btnRecordPause.setImageResource(R.drawable.ic_pause);
//                break;
//            case 3:
//                btnRecordPause.setVisibility(View.INVISIBLE);
//                btnReplay.setVisibility(View.VISIBLE);
//                btnStop.setVisibility(View.VISIBLE);
//                break;
//        }
    }
//
//    private void startLocationUpdates() {
//        locationManager.requestLocationUpdates(locationProvider,
//                mLocationCallback,
//                null /* Looper */);
//    }
//
    public void userLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                makeUseOfNewLocation(location);
                LatLng myNewLoc = new LatLng(location.getLatitude(), location.getLongitude());
                if (firstFlagRecord) {
                    firstFlagRecord = false;
                    firstMarker.remove();
                    myMap.addMarker(new MarkerOptions().position(myNewLoc)
                            .title("Start point"));
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myNewLoc , 16.0f));
                }
                if (prevLoc == null) {
                    prevLoc = location;
                    return;
                }
                if (currState == getResources().getInteger(R.integer.saved_state_pause_key)) {
                    listLoc.add(new Double[]{location.getLatitude(), location.getLongitude()});
                    double deltaDistance = Util.distance(location.getLatitude(), prevLoc.getLatitude(),
                            location.getLongitude(), prevLoc.getLongitude(),
                            location.getAltitude(), prevLoc.getAltitude());
                    currDistance += deltaDistance;
                    if (currDistance == 0) {
                        txtDistance.setText(getString(R.string.display_distance, "--"));
                    } else {
                        txtDistance.setText(getString(R.string.display_distance, String.format("%.02f", currDistance)));
                    }

                    if (currTime == 0) {
                        txtSpeed.setText(getString(R.string.display_speed, "--"));
                    } else {
                        currSpeed = currDistance / (double) currTime;
                        listSpeed.add(currSpeed);
                        txtSpeed.setText(getString(R.string.display_speed, String.format("%.02f", currSpeed)));
                    }

                    boolean flag = CheckLocation.isBetterLocation(location, prevLoc);
                    if (flag) {
                        LatLng myOldLoc = new LatLng(prevLoc.getLatitude(), prevLoc.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLng(myNewLoc));
                        myMap.addPolyline(new PolylineOptions()
                                .add(myOldLoc, myNewLoc)
                                .width(8)
                                .color(Color.RED));
                        prevLoc = location;
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

//        locationProvider = LocationManager.NETWORK_PROVIDER;
        locationProvider = LocationManager.GPS_PROVIDER;
        listenToLocation();
    }

    private void listenToLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(locationProvider, DEFAULT_MIN_TIME, DEFAULT_MIN_DISTANCE, locationListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rec_pause:
                if (currState == 1) {
                    record();
                } else if (currState == 2) {
                    pause();
                }

                break;
            case R.id.btn_replay:
                replay();
                break;
            case R.id.btn_stop:
                stop();
                break;
        }
    }

    private void stop() {
        currState = getResources().getInteger(R.integer.saved_state_default_key);
        updateButtonUI();
        stopTimer();
        locationManager.removeUpdates(locationListener);

        // TODO: update database in firebase
        uploadDataToFirebase();

        currDistance = 0;
        currSpeed = 0;
        currTime = 0;
        listLoc.clear();
        listSpeed.clear();
        setDefaultParameter();
    }

    private void uploadDataToFirebase() {
        DatabaseReference childRef = myRef.push();
        childRef.child("date").setValue(ServerValue.TIMESTAMP);
        childRef.child("distance").setValue(Double.toString(currDistance));
        double avgSpeed = Util.average(listSpeed);
        childRef.child("speed").setValue(Double.toString(avgSpeed));
        childRef.child("time").setValue(Long.toString(currTime));
        DatabaseReference locChildRef = childRef.child("location");
        int count = 0;
        for(Double []t: listLoc) {
            locChildRef.child(Integer.toString(count)).child("latitude").setValue(t[0]);
            locChildRef.child(Integer.toString(count)).child("longitude").setValue(t[1]);
            count += 1;
        }
        mListener.onFragmentInteractionTracking();
    }

    private void replay() {
        currState = getResources().getInteger(R.integer.saved_state_pause_key);
        updateButtonUI();
        startTimer();
        listenToLocation();
    }

    private void pause() {
        currState = getResources().getInteger(R.integer.saved_state_replay_stop_key);
        updateButtonUI();
        stopTimer();
        locationManager.removeUpdates(locationListener);

    }

    private void record() {
        currState = getResources().getInteger(R.integer.saved_state_pause_key);
        firstFlagRecord = true;

        listLoc = new ArrayList<>();
        listSpeed = new ArrayList<>();
        setDefaultParameter();
        locationManager.removeUpdates(locationListener);
        listenToLocation();
        updateButtonUI();
        startTimer();
    }

    private void setDefaultParameter() {
        txtDistance.setText(getString(R.string.display_distance, "--"));
        txtSpeed.setText(getString(R.string.display_speed, "--"));
    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            long[] t = Util.splitTimes(currTime);
            txtTime.setText(getString(R.string.display_time,
                    String.format("%02d", t[0]), String.format("%02d", t[1]), String.format("%02d", t[2])));
        }
    };

    private void startTimer() {
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //here you can write the code for send the message
                currTime += 1;
                mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if(myTimer != null) {
            myTimer.cancel();
            myTimer = null;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteractionTracking();
    }
}
