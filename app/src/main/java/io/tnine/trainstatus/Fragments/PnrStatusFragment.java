package io.tnine.trainstatus.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;

import io.tnine.trainstatus.Adapters.PnrStatusAdapter;
import io.tnine.trainstatus.Models.ModelPnrStatus.Passenger;
import io.tnine.trainstatus.Models.ModelPnrStatus.PnrStatusModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.Config;


public class PnrStatusFragment extends Fragment {

    private RecyclerView passen_status_recy;
    PnrStatusModel model;
    PnrStatusAdapter pnrStatusAdapter;
    private RecyclerView.LayoutManager mlayoutManager;
    private InterstitialAd mInterstitialAd;


    public PnrStatusFragment() {
        model = Config.getPnrStatusModel();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pnr_status, container, false);


        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-7591469242682758/6367481893");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        TextView textTrainNameVal = view.findViewById(R.id.tv_train_name_val);
        TextView textJourneyclassVal = view.findViewById(R.id.tv_journey_class_val);
        TextView textReservationfromVal = view.findViewById(R.id.tv_reservation_from_val);
        TextView textReservationtoVal = view.findViewById(R.id.tv_reservation_to_val);
        TextView textBoardingpointVal = view.findViewById(R.id.tv_boarding_point_val);
        TextView texttDojVal = view.findViewById(R.id.tv_doj_val);
        TextView textChartpreparedVal = view.findViewById(R.id.tv_chart_prepared_val);
        passen_status_recy = (RecyclerView)view.findViewById(R.id.passen_status_recy);

        List<Passenger> pList = model.getPassengers();
        pnrStatusAdapter = new PnrStatusAdapter(pList, getActivity());
        passen_status_recy.setHasFixedSize(true);
        mlayoutManager = new LinearLayoutManager(getActivity());
        passen_status_recy.setLayoutManager(mlayoutManager);
        passen_status_recy.setAdapter(pnrStatusAdapter);


        List<Passenger> passengers = model.getPassengers();
//        Toast.makeText(getActivity(),String.valueOf(passengers.get(0).getNo()),Toast.LENGTH_SHORT).show();

        textTrainNameVal.setText(model.getTrain().getName());
//        textBookingstatusVal.setText(model.);
        textBoardingpointVal.setText(model.getBoardingPoint().getName());
        textJourneyclassVal.setText(model.getJourneyClass().getCode());
        textReservationfromVal.setText(model.getFromStation().getName());
        textReservationtoVal.setText(model.getToStation().getName());
        texttDojVal.setText(model.getDoj());

        if (model.getChartPrepared()) {
            textChartpreparedVal.setText("Chart prepared");
        } else textChartpreparedVal.setText("Chart not prepared");







        return view;
    }

    @Override
    public void onPause() {
        mInterstitialAd.show();
        super.onPause();
    }
}
