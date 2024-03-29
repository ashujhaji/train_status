package io.tnine.trainstatus.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import io.tnine.trainstatus.Adapters.LiveStationStatusAdapter;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.Config;

/**
 * A simple {@link Fragment} subclass.
 */
public class LiveStationStatusFragment extends Fragment {
    RecyclerView recycleTrainArrivals;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mlayoutManager;
    private InterstitialAd mInterstitialAd;

    public LiveStationStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_live_station_status, container, false);

        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-7591469242682758/6367481893");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        recycleTrainArrivals = view.findViewById(R.id.rv_live_station);
        mAdapter = new LiveStationStatusAdapter(Config.getLiveStationStatusModel(), getActivity());
        recycleTrainArrivals.setHasFixedSize(true);
        mlayoutManager = new LinearLayoutManager(getActivity());
        recycleTrainArrivals.setLayoutManager(mlayoutManager);
        recycleTrainArrivals.setAdapter(mAdapter);



        return view;
    }

    @Override
    public void onPause() {
        mInterstitialAd.show();
        super.onPause();
    }
}
