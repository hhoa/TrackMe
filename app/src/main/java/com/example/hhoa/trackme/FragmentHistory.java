package com.example.hhoa.trackme;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentHistory.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentHistory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHistory extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM_UID = "param1";
    private String mUID;

    private ArrayList<TrackActivity> userActivity = new ArrayList<>();
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private LinearLayout linearLayout;
    private RecyclerViewAdapter mRcvAdapter;

    public FragmentHistory() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentHistory.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHistory newInstance(String param1) {
        FragmentHistory fragment = new FragmentHistory();
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
        View v = inflater.inflate(R.layout.fragment_fragment_history, container, false);

        if (userActivity == null || userActivity.isEmpty())
            getDataUserFromFirebase(mUID);
        mySortUserActivity();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mRcvAdapter = new RecyclerViewAdapter(getActivity(), userActivity);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRcvAdapter);
    }

    public void getDataUserFromFirebase(String userID) {
        Log.i(TAG, "getDataUserFromFirebase: " + userID);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.i(TAG, "getDataUserFromFirebase: " + database.toString());
        final DatabaseReference myRef = database.getReference("lib").child(userID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    getSingleValue(childDataSnapshot);
                }
                mySortUserActivity();

                if (mRcvAdapter != null)
                    mRcvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSingleValue(DataSnapshot childDataSnapshot) {
        long date = Long.parseLong(childDataSnapshot.child("date").getValue().toString());
        if (checkExistKey(date))
            return;
        double distance = Double.parseDouble(childDataSnapshot.child("distance").getValue().toString());
        double speed = Double.parseDouble(childDataSnapshot.child("speed").getValue().toString());
        long time = Long.parseLong(childDataSnapshot.child("time").getValue().toString());
        ArrayList<Double[]> listLoc = new ArrayList<>();
        //get all user location
        for (DataSnapshot locSnapshot: childDataSnapshot.child("location").getChildren()) {
            listLoc.add(new Double[]{Double.parseDouble(locSnapshot.child("latitude").getValue().toString()),
                    Double.parseDouble(locSnapshot.child("longitude").getValue().toString())});
        }

        userActivity.add(new TrackActivity(date, distance, speed, time, listLoc));
        Log.i(TAG, "getSingleValue : " + userActivity.toString());
    }

    private void mySortUserActivity() {
        Log.i(TAG, "mySortUserActivity: " + userActivity.toString());
        Collections.sort(userActivity, new Comparator<TrackActivity>() {
            public int compare(TrackActivity one, TrackActivity other) {
                return Long.compare(other.getDate(), one.getDate());
            }
        });
        Log.i(TAG, "mySortUserActivity: " + userActivity.toString());
    }

    private boolean checkExistKey(long key) {
        for (int i = 0; i < userActivity.size(); i++){
            if (userActivity.get(i).getDate() == key)
                return true;
        }
        return false;
//        Iterator it = userActivity.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            System.out.println(pair.getKey() + " = " + pair.getValue());
//            if (pair.getKey().equals(key))
//                return true;
//            //it.remove(); // avoids a ConcurrentModificationException
//        }
//
//        return false;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteractionHistory();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onFragmentInteractionHistory();
    }
}
