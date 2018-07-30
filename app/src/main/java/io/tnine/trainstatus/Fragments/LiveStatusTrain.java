package io.tnine.trainstatus.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import io.tnine.trainstatus.Adapters.LiveTrainStatusAdapter;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrainStatusModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.Config;


public class LiveStatusTrain extends Fragment {

    private RecyclerView liveTrainStatusRoutes;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mlayoutManager;
    private LiveTrainStatusModel mData;
    private InterstitialAd mInterstitialAd;

    public LiveStatusTrain() {
        mData = Config.getLiveTrainStatusModel(); // Required empty public constructor
        //Log.e("data when buton pressed", mData.getTrain().getName());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.live_status_train, container, false);
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-7591469242682758/6367481893");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        liveTrainStatusRoutes = (RecyclerView) view.findViewById(R.id.rv_live_train);
        TextView textCurrentStation = view.findViewById(R.id.tv_current_station);
        TextView textDate = view.findViewById(R.id.tv_date);


//        Toast.makeText(getActivity(),String.valueOf(Config.getLiveTrainStatusModel()),Toast.LENGTH_SHORT).show();
        if (Config.getLiveTrainStatusModel() != null) {

            Log.e("current status", Config.getLiveTrainStatusModel().getPosition() + "");

            if (Config.getLiveTrainStatusModel() != null) {
                textCurrentStation.setText(Config.getLiveTrainStatusModel().getPosition());
                textDate.setText(Config.getLiveTrainStatusModel().getStartDate());
            }

            mAdapter = new LiveTrainStatusAdapter(mData, getActivity());
            liveTrainStatusRoutes.setHasFixedSize(true);
            mlayoutManager = new LinearLayoutManager(getActivity());
            liveTrainStatusRoutes.setLayoutManager(mlayoutManager);
            liveTrainStatusRoutes.setAdapter(mAdapter);
        }
            return view;
        }

    @Override
    public void onPause() {
        mInterstitialAd.show();
        super.onPause();
    }
}
