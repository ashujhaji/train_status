package io.tnine.trainstatus.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import io.tnine.trainstatus.Models.ModelCancelledTrains.CancelledTrainsModel;
import io.tnine.trainstatus.Models.ModelFareEnquiry.FareEnquiryModel;
import io.tnine.trainstatus.Models.ModelLiveStationStatus.LiveStationStatusModel;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrainStatusModel;
import io.tnine.trainstatus.Models.ModelPnrStatus.PnrStatusModel;
import io.tnine.trainstatus.Models.ModelRescheduledTrains.RescheduledTrainsModel;
import io.tnine.trainstatus.Models.ModelSeatAvailability.SeatAvailabilityModel;
import io.tnine.trainstatus.Models.ModelTrainRoute.TrainRouteModel;
import io.tnine.trainstatus.Models.ModelTrainsBetweenStations.TrainsBetweenStationsModel;
import io.tnine.trainstatus.R;


public class Config {
    public static boolean decision = true;
    public static LiveTrainStatusModel liveTrainStatusModel;
    public static LiveStationStatusModel liveStationStatusModel;
    public static TrainsBetweenStationsModel trainsBetweenStationsModel;
    public static RescheduledTrainsModel rescheduledTrainsModel;
    public static SeatAvailabilityModel seatAvailabilityModel;
    public static String myApiKey;
    public static String ADMOB_MAIN_ID = "ca-app-pub-3940256099942544/6300978111";

    public static String getMyApiKey() {
        return myApiKey;
    }

    public static void setMyApiKey(String myApiKey) {
        Config.myApiKey = myApiKey;
    }

    public static SeatAvailabilityModel getSeatAvailabilityModel() {
        return seatAvailabilityModel;
    }

    public static void setSeatAvailabilityModel(SeatAvailabilityModel seatAvailabilityModel) {
        Config.seatAvailabilityModel = seatAvailabilityModel;
    }



    public static FareEnquiryModel getFareEnquiryModel() {
        return fareEnquiryModel;
    }

    public static void setFareEnquiryModel(FareEnquiryModel fareEnquiryModel) {
        Config.fareEnquiryModel = fareEnquiryModel;
    }

    public static FareEnquiryModel fareEnquiryModel;

    public static TrainRouteModel getTrainRouteModel() {
        return trainRouteModel;
    }

    public static void setTrainRouteModel(TrainRouteModel trainRouteModel) {
        Config.trainRouteModel = trainRouteModel;
    }

    public static TrainRouteModel trainRouteModel;
    public static CancelledTrainsModel cancelledTrainsModel;

    public static CancelledTrainsModel getCancelledTrainsModel() {
        return cancelledTrainsModel;
    }

    public static void setCancelledTrainsModel(CancelledTrainsModel cancelledTrainsModel) {
        Config.cancelledTrainsModel = cancelledTrainsModel;
    }

    public static RescheduledTrainsModel getRescheduledTrainsModel() {
        return rescheduledTrainsModel;
    }

    public static void setRescheduledTrainsModel(RescheduledTrainsModel rescheduledTrainsModel) {
        Config.rescheduledTrainsModel = rescheduledTrainsModel;
    }

    public static PnrStatusModel getPnrStatusModel() {
        return pnrStatusModel;
    }

    public static void setPnrStatusModel(PnrStatusModel pnrStatusModel) {
        Config.pnrStatusModel = pnrStatusModel;
    }

    public static PnrStatusModel pnrStatusModel;

    public static LiveStationStatusModel getLiveStationStatusModel() {
        return liveStationStatusModel;
    }

    public static void setLiveStationStatusModel(LiveStationStatusModel liveStationStatusModel) {
        Config.liveStationStatusModel = liveStationStatusModel;
    }

    public static TrainsBetweenStationsModel getTrainsBetweenStationsModel() {
        return trainsBetweenStationsModel;
    }

    public static void setTrainsBetweenStationsModel(TrainsBetweenStationsModel trainsBetweenStationsModel) {
        Config.trainsBetweenStationsModel = trainsBetweenStationsModel;
    }

    public static boolean isDecision() {
        return decision;
    }

    public static void setDecision(boolean decision) {
        Config.decision = decision;
    }

    public static LiveTrainStatusModel getLiveTrainStatusModel() {
        return liveTrainStatusModel;
    }

    public static void setLiveTrainStatusModel(LiveTrainStatusModel liveTrainStatusModel) {
        Config.liveTrainStatusModel = liveTrainStatusModel;
    }


    public static boolean isNumeric(String str){
        try{
            double d = Double.parseDouble(str);
        }catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }


    public static void dialogPopup(Activity context){
        final View view = (context).getLayoutInflater().inflate(R.layout.dialog_user_msg, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);

        ImageView close = dialog.findViewById(R.id.close);
        Button got_it = dialog.findViewById(R.id.got_it);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        got_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }




}
