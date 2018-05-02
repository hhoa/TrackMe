package com.example.hhoa.trackme;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    private ArrayList<TrackActivity> data = new ArrayList<>();
    private Context c;
    private int myPos = -1;

    public RecyclerViewAdapter(Context c, ArrayList<TrackActivity> data) {
        this.c = c;
        this.data = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        long[] t = Util.splitTimes(data.get(position).getTime());

        holder.txtDistance.setText(c.getString(R.string.display_distance, String.format("%.02f", data.get(position).getDistance())));
        holder.txtSpeed.setText(c.getString(R.string.display_speed, String.format("%.02f", data.get(position).getSpeed())));
        holder.txtTime.setText(c.getString(R.string.display_time,
                String.format("%02d", t[0]), String.format("%02d", t[1]), String.format("%02d", t[2])));
        myPos = position;

        holder.myMapView.onCreate(new Bundle());
        holder.myMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMinZoomPreference(10.0f);
                googleMap.setMaxZoomPreference(20.0f);

                ArrayList<Double[]> loc = data.get(myPos).getListLoc();

                LatLng startPoint = new LatLng(loc.get(0)[0], loc.get(0)[1]);
                googleMap.addMarker(new MarkerOptions().position(startPoint)
                        .title("Current Marker"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint , 16.0f));

                for (int i = 1; i < loc.size(); i++) {
                    googleMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(loc.get(i - 1)[0], loc.get(i - 1)[1]), new LatLng(loc.get(i)[0], loc.get(i)[1]))
                            .width(8)
                            .color(Color.RED));
                }
                holder.myMapView.onResume();
            }
        });

        holder.line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.line.setBackgroundColor(ContextCompat.getColor(c, R.color.colorYellow));
            }
        });

        holder.line.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(position);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView txtDistance;
        TextView txtSpeed;
        TextView txtTime;
        MapView myMapView;
        LinearLayout line;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.ll_item);
            txtDistance = itemView.findViewById(R.id.txt_distance1);
            txtSpeed = itemView.findViewById(R.id.txt_speed1);
            txtTime = itemView.findViewById(R.id.txt_time1);
            myMapView = itemView.findViewById(R.id.fragment_embedded_map_view_mapview);
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(String username);
    }

    public interface OnItemLongClickedListener {
        void onItemLongClick(int pos);
    }

    private OnItemClickedListener onItemClickedListener;
    private RecyclerViewAdapter.OnItemLongClickedListener onItemLongClickListener;

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setOnItemLongClickListener(RecyclerViewAdapter.OnItemLongClickedListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}
