package io.tnine.trainstatus;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrain;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrainsModel;
import io.tnine.trainstatus.Models.Station;
import io.tnine.trainstatus.Models.StationAutoCompleteSuggestions.StationAutoCompleteSuggest;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RemoteData {
    private Context context;
    List<LiveTrain> trainSuggestions;
    List<Station> stationSuggestions;
    ApiInterface apiInterface;


    public RemoteData(Context contextIn) {

        context = contextIn;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    public void getSuggestionsData(CharSequence constraint, final int resId){



        Call<LiveTrainsModel> call = apiInterface.getTrainSuggestions(constraint.toString(), Config.myApiKey);
        call.enqueue(new Callback<LiveTrainsModel>() {
            @Override
            public void onResponse(Call<LiveTrainsModel> call, Response<LiveTrainsModel> response) {
                LiveTrainsModel suggest = response.body();

                try{
                    if(suggest != null) {
                        if (response.body().getResponseCode()==200) {
                            trainSuggestions = suggest.getTrains();
                            List<String> str = new ArrayList<String>();
                            for(LiveTrain s : trainSuggestions){
                                str.add(s.getNumber() + " - " + s.getName());
                                Log.e("name", str.get(0));
                            }

                            AutoCompleteTextView autoTrain = ((Activity)context).findViewById(resId);

                            ArrayAdapter<String> adapteo = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, str);
                            autoTrain.setAdapter(adapteo);
                        }else {
                            Toast.makeText(((Activity)context),"Problem in getting data",Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (NullPointerException e){
                    Log.e("Exception at adapter", e.toString());
                }
                //Log.e("train name", stationSuggestions.get(0).getName().toString());


            }



            @Override
            public void onFailure(Call<LiveTrainsModel> call, Throwable t) {
                Log.e("Response failed suggest", t.toString());
            }
        });

    }

    public void getStationSuggestionsData(final CharSequence constraint, final int resId){

        final Call<StationAutoCompleteSuggest> codeCall = apiInterface.getStationSuggestions(constraint, Config.myApiKey);
        codeCall.enqueue(new Callback<StationAutoCompleteSuggest>() {
            @Override
            public void onResponse(Call<StationAutoCompleteSuggest> call, Response<StationAutoCompleteSuggest> response) {

                if (response.isSuccessful()) {
                    Log.e("Request hit", "is successful");

                    if (response.body().getResponseCode() == 200 && response.body().getStations().size() > 0) {
                        Log.e("List is", "Not null");
                        stationSuggestions = response.body().getStations();
                        List<String> str = new ArrayList<String>();
                        for(Station s : stationSuggestions){
                            str.add(s.getCode() + " - " + s.getName());
                            Log.e("name", str.get(0));
                        }

                        AutoCompleteTextView autoStation = ((Activity)context).findViewById(resId);

                        ArrayAdapter<String> adapteo = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, str);
                        autoStation.setAdapter(adapteo);

                    }else {
                        Toast.makeText(((Activity)context),"Problem in getting data",Toast.LENGTH_SHORT).show();
                    }
                }



            }

            @Override
            public void onFailure(Call<StationAutoCompleteSuggest> call, Throwable t) {

                Log.e("station auto", "failed");

            }
        });

    }

}
