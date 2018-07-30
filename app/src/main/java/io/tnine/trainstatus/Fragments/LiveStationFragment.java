package io.tnine.trainstatus.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import io.tnine.trainstatus.Adapters.StationAutoCompleteAdapter;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelLiveStationStatus.LiveStationStatusModel;
import io.tnine.trainstatus.Models.Station;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.DelayAutoCompleteTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LiveStationFragment extends Fragment {
    ApiInterface apiInterface;
    private RelativeLayout cancel_stn_txt;
    private ProgressDialog progressDialog;
    private AdView mAdView;

    String stationCode;
    public LiveStationFragment() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_live_station, container, false);

        final AutoCompleteTextView autoStation = view.findViewById(R.id.acmtv_live_station_code);
        final Button btnGetTrainArrivals = view.findViewById(R.id.btn_get_train_arrivals);
        cancel_stn_txt = (RelativeLayout) view.findViewById(R.id.cancel_stn_txt);
        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        btnGetTrainArrivals.setEnabled(false);
        autoStation.setText("");
        autoStation.setThreshold(2);
        autoStation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!Config.isNumeric(charSequence.toString())) {
                    if (charSequence.toString().length() == 3) {
                        ((MainActivity) getActivity()).callMe(charSequence, R.id.acmtv_live_station_code, "station");

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        autoStation.setOnItemClickListener(new AdapterView.OnItemClickListener() {                     //From Station AutoCompleteTextView
                                               @Override
                                               public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                                                   String item = adapterView.getItemAtPosition(position).toString();
                                                   stationCode = item.substring(0, item.indexOf("-")).trim();
                                                   btnGetTrainArrivals.setEnabled(true);
                                               }
                                           });

        btnGetTrainArrivals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoStation.getText().toString().isEmpty()||autoStation==null){
                    autoStation.setError("Field is empty");
                    autoStation.requestFocus();
                }else {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("loading....");
                    progressDialog.setTitle("Trains from station");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();

                    Call<LiveStationStatusModel> call = apiInterface.getTrainArrivals(stationCode, "4", Config.myApiKey);
                    call.enqueue(new Callback<LiveStationStatusModel>() {
                        @Override
                        public void onResponse(Call<LiveStationStatusModel> call, Response<LiveStationStatusModel> response) {
                            if (response.isSuccessful()){
                                if(response.body().getResponseCode() == 200){
                                    Config.setLiveStationStatusModel(response.body());
                                    String fromAutoCompleteTextViewText = autoStation.getText().toString();
                                    String substrStation = fromAutoCompleteTextViewText.substring(fromAutoCompleteTextViewText.lastIndexOf("-") + 2);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("stn_code", substrStation);

                                    Fragment liveStationStatus = new LiveStationStatusFragment();
                                    liveStationStatus.setArguments(bundle);

                                    ((MainActivity) getActivity()).performTransaction(liveStationStatus,"liveStationStatus");
                                    progressDialog.dismiss();
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(),"Try again",Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<LiveStationStatusModel> call, Throwable t) {
                            progressDialog.dismiss();
                            Log.e("Exception Live station", t.toString());

                        }


                    });
                }
            }
        });

        cancel_stn_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoStation.setText("");
                Config.setLiveStationStatusModel(null);
            }
        });












        return view;
    }


}
