package io.tnine.trainstatus.Fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrain;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrainStatusModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.RemoteData;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class LiveTrainFragment extends Fragment {

    private FragmentTransaction ft;
    private LiveTrain train;
    private String trainCode;
    private LiveTrainStatusModel model;
    private String dateForResponse;
    ApiInterface apiInterface;
    private RelativeLayout cancel_live_txt;
    private AdView mAdView;


    public LiveTrainFragment() {

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live_train, container, false);

        final AutoCompleteTextView autoTrain = (AutoCompleteTextView) view.findViewById(R.id.acmtv_live_train_code);
        final Button btnGetLiveStatus = (Button) view.findViewById(R.id.btn_get_live_status);
        final EditText editTextLiveTrainStatusDate= (EditText) view.findViewById(R.id.et_pick_live_train_date);

        cancel_live_txt = (RelativeLayout)view.findViewById(R.id.cancel_live_txt);
        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        btnGetLiveStatus.setEnabled(false);

        final Calendar myCalendar = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String formattedDate = df.format(c.getTime());
        editTextLiveTrainStatusDate.setText(formattedDate);
        dateForResponse = editTextLiveTrainStatusDate.getText().toString();

        final DatePickerDialog.OnDateSetListener mdate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                editTextLiveTrainStatusDate.setText(df.format(myCalendar.getTime()));
            }

        };

        editTextLiveTrainStatusDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new DatePickerDialog(getActivity(), mdate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editTextLiveTrainStatusDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dateForResponse = editTextLiveTrainStatusDate.getText().toString();


                Log.e("date changed", dateForResponse);


            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        autoTrain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 3) {
                    ((MainActivity)getActivity()).callMe(charSequence, R.id.acmtv_live_train_code, "train" );
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        autoTrain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Log.e("clickitem", adapterView.getItemAtPosition(position) + "");
                String item = adapterView.getItemAtPosition(position).toString();
              trainCode = item.substring(0, item.indexOf("-")).trim();
                btnGetLiveStatus.setEnabled(true);

            }
        });


        btnGetLiveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (autoTrain.getText().toString().isEmpty()||autoTrain==null){
                    autoTrain.setError("Field is empty");
                    autoTrain.requestFocus();

                }else {
                    InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                    final ProgressDialog mProgress = new ProgressDialog(getActivity());
                        mProgress.setTitle("Live Trains");
                        mProgress.setMessage("Live train status is loading...");
                        mProgress.show();
                    Call<LiveTrainStatusModel> call = apiInterface.getLiveTrainStatus(trainCode, dateForResponse, Config.myApiKey);
                    call.enqueue(new Callback<LiveTrainStatusModel>() {
                        @Override
                        public void onResponse(Call<LiveTrainStatusModel> call, Response<LiveTrainStatusModel> response) {
                            if(response.isSuccessful()) {
                                if(response.body().getResponseCode() == 200) {
                                    Log.e("OMG!", "IT IS FROM HERE");
                                    model = response.body();
                                    Config.setLiveTrainStatusModel(model);

                                    Fragment liveStatusTrain = new LiveStatusTrain();
                                    Bundle bundle = new Bundle();
                                    try {
                                        String fromAutoCompleteTextViewText = autoTrain.getText().toString();
                                    }catch (NullPointerException e){

                                        Log.e("Error", e.toString());
                                    }

                                    bundle.putString("live_train_code", trainCode);
                                    liveStatusTrain.setArguments(bundle);
                                    ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_main, liveStatusTrain).addToBackStack(null).commit();
                                    mProgress.dismiss();
                                }

                                else if(response.body().getResponseCode() == 210){
                                    mProgress.dismiss();
                                    Logger.d(new Gson().toJson(response.body()));
                                    Log.e("date", dateForResponse);
                                    Toast.makeText(getActivity(), "Train doesn't run on the selected date", Toast.LENGTH_LONG).show();
                                    autoTrain.setText("");
                                    //editTextLiveTrainStatusDate.setText("");
                                }else if (response.body().getResponseCode() == 404){
                                    mProgress.dismiss();
                                    Toast.makeText(getActivity(),"No data available",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    mProgress.dismiss();
                                    Log.e("Finally failed", response.body().getResponseCode() + "");
                                }

                            }

                        }

                        @Override
                        public void onFailure(Call<LiveTrainStatusModel> call, Throwable t) {
                            Toast.makeText(getActivity(), "Request didn't go through", Toast.LENGTH_LONG);
                            Log.e("No response", t.toString());
                        }
                    });
                }




            }
        });

        cancel_live_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTrain.setText("");
            }
        });

        return view;
    }

}
