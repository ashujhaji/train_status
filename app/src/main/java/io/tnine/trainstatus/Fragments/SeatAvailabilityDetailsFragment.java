package io.tnine.trainstatus.Fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.tnine.trainstatus.Adapters.TrainAutoCompleteAdapter;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelSeatAvailability.SeatAvailabilityModel;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrain;
import io.tnine.trainstatus.Models.ModelTrainRoute.TrainRouteModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.DelayAutoCompleteTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatAvailabilityDetailsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    ApiInterface apiInterface;
    private LiveTrain train;
    private String trainCode;
    private TrainRouteModel model;
    EditText editTextDate;
    Calendar c;
    SimpleDateFormat df;
    DatePickerDialog.OnDateSetListener mdate;
    List classCodes = new ArrayList<String>();
    List srcStations = new ArrayList<String>();
    List destStations = new ArrayList<String>();

    String[] quotaCodes;
    ArrayAdapter adapterSpinClass;
    ArrayAdapter adapterSpinSrc;
    ArrayAdapter adapterSpinDest;
    ArrayAdapter adapterSpinQuota;

    String src;
    String dest;

    String quota;
    String travelClass;
    ProgressDialog progressDoalog;
    private Spinner spinnerSrc, spinnerDest,spinnerClass,spinnerQuota;
    private RelativeLayout cancel_seat_txt;
    private ProgressBar fill_prog;
    private AdView mAdView;

    public SeatAvailabilityDetailsFragment() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        quotaCodes = new String[]{"GENERAL", "TATKAL", "PREMIUM TATKAL"};
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_seat_availability_details, container, false);

         spinnerSrc = view.findViewById(R.id.spin_train_source);
         spinnerDest = view.findViewById(R.id.spin_train_dest);
         spinnerClass = view.findViewById(R.id.spin_train_class);
         spinnerQuota = view.findViewById(R.id.spin_train_quota);
        fill_prog = view.findViewById(R.id.fill_prog);

        final AutoCompleteTextView autoTrainCode = view.findViewById(R.id.acmtv_seat_train_code);
        editTextDate = view.findViewById(R.id.et_seat_pick_date);
        final Button btnGetseat = view.findViewById(R.id.btn_get_seat);
        cancel_seat_txt = (RelativeLayout)view.findViewById(R.id.cancel_seat_txt);

        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        btnGetseat.setEnabled(false);
        c = Calendar.getInstance();
        df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        editTextDate.setText(formattedDate);

        mdate = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                editTextDate.setText(df.format(c.getTime()));
            }

        };

        editTextDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new DatePickerDialog(getActivity(), mdate, c
                        .get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        autoTrainCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().length() == 3) {
                    ((MainActivity)getActivity()).callMe(charSequence, R.id.acmtv_seat_train_code, "train");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        adapterSpinClass = new ArrayAdapter(getContext(), R.layout.simple_spinner_dropdown_item, classCodes);
        adapterSpinSrc = new ArrayAdapter(getContext(), R.layout.simple_spinner_dropdown_item, srcStations);
        adapterSpinDest = new ArrayAdapter(getContext(), R.layout.simple_spinner_dropdown_item, destStations);
        adapterSpinQuota = new ArrayAdapter(getContext(), R.layout.simple_spinner_dropdown_item, quotaCodes);


        autoTrainCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                fill_prog.setVisibility(View.VISIBLE);
                String item = adapterView.getItemAtPosition(position).toString();
                trainCode = item.substring(0, item.indexOf("-")).trim();
                btnGetseat.setEnabled(true);

                Call<TrainRouteModel> call = apiInterface.getTrainRoute(trainCode, Config.myApiKey);
                call.enqueue(new Callback<TrainRouteModel>() {
                    @Override
                    public void onResponse(Call<TrainRouteModel> call, Response<TrainRouteModel> response) {
                        if(response.isSuccessful()) {
                            if(response.body().getResponseCode() == 200) {
                                Log.e("seat Train Code Res", "Data loaded");
                                model = response.body();
                                Config.setTrainRouteModel(model);
                                for(int i = 0; i < Config.getTrainRouteModel().getTrain().getClasses().size(); i++){

                                    if (Config.getTrainRouteModel().getTrain().getClasses().get(i).getAvailable().equals("Y")) {
                                        classCodes.add(Config.getTrainRouteModel().getTrain().getClasses().get(i).getCode());
                                    }

                                }

                                adapterSpinClass.notifyDataSetChanged();

                                int s = Config.getTrainRouteModel().getRoute().size();
                                for (int i = 0; i < s; i++){

                                    srcStations.add(Config.getTrainRouteModel().getRoute().get(i).getStation().getName());
                                    destStations.add(Config.getTrainRouteModel().getRoute().get(s-(i+1)).getStation().getName());

                                }

                                adapterSpinSrc.notifyDataSetChanged();
                                adapterSpinDest.notifyDataSetChanged();

                                fill_prog.setVisibility(View.GONE);

                            }else {
                                fill_prog.setVisibility(View.GONE);
                                Toast.makeText(getActivity(),"Problem in getting data",Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TrainRouteModel> call, Throwable t) {
                        Toast.makeText(getActivity(), "Request didn't go through", Toast.LENGTH_LONG).show();
                        Log.e("seat res failed", t.toString());
                    }
                });
            }
        });

        adapterSpinClass.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinSrc.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinDest.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        adapterSpinQuota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerClass.setAdapter(adapterSpinClass);
        spinnerDest.setAdapter(adapterSpinDest);
        spinnerSrc.setAdapter(adapterSpinSrc);
        spinnerQuota.setAdapter(adapterSpinQuota);


        spinnerClass.setOnItemSelectedListener(this);

        spinnerQuota.setOnItemSelectedListener(this);
        spinnerDest.setOnItemSelectedListener(this);
        spinnerSrc.setOnItemSelectedListener(this);

        btnGetseat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (autoTrainCode.getText().toString().isEmpty()||autoTrainCode==null){
                    autoTrainCode.setError("Field is empty");
                    autoTrainCode.requestFocus();
                }else {
                    progressDoalog = new ProgressDialog(getActivity());
                    progressDoalog.setCanceledOnTouchOutside(false);
                    progressDoalog.setMessage("loading....");
                    progressDoalog.setTitle("Seat Availability");
                    progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDoalog.show();
                    Call<SeatAvailabilityModel> call = apiInterface.getSeatAvailability(trainCode,
                            src, dest, editTextDate.getText().toString(), travelClass, quota, Config.myApiKey);
                    call.enqueue(new Callback<SeatAvailabilityModel>() {
                        @Override
                        public void onResponse(Call<SeatAvailabilityModel> call, Response<SeatAvailabilityModel> response) {


                            if (response.isSuccessful()) {
                                Log.d("seat",String.valueOf(response.body().getResponseCode()));

                                Log.e("seat hit", "success");

                                if(response.body().getResponseCode() == 200){
                                    Log.e("seat res", "data loaded");
                                    progressDoalog.dismiss();
                                    Config.setSeatAvailabilityModel(response.body());
                                    Fragment seatAvailability = new SeatAvailability();
                                    ((MainActivity)getActivity()).performTransaction(seatAvailability,"seatAvailability");
                                }else {
                                    Toast.makeText(getActivity(),"Problem in getting data",Toast.LENGTH_LONG).show();
                                    progressDoalog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SeatAvailabilityModel> call, Throwable t) {
                            progressDoalog.dismiss();
                            Log.e("seat res ", "failed");

                        }
                    });
                }

            }
        });

        cancel_seat_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTrainCode.setText("");
                srcStations.clear();
                destStations.clear();
                classCodes.clear();

                adapterSpinSrc.notifyDataSetChanged();
                adapterSpinDest.notifyDataSetChanged();
                adapterSpinClass.notifyDataSetChanged();

            }
        });


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


        int id = adapterView.getId();
        switch (id)
        {
            case R.id.spin_train_source:
                Log.e("spinner", "src");
//                ((TextView)adapterView.getChildAt(i)).setTextColor(Color.WHITE);
//                ((TextView)adapterView.getChildAt(i)).setTextSize(13);
                src = Config.getTrainRouteModel().getRoute().get(i).getStation().getCode();
                break;

            case R.id.spin_train_dest:
                Log.e("spinner", "dest");
//                ((TextView)adapterView.getChildAt(i)).setTextColor(Color.WHITE);
//                ((TextView)adapterView.getChildAt(i)).setTextSize(13);
                int size;
                size = Config.getTrainRouteModel().getRoute().size();
                dest = Config.getTrainRouteModel().getRoute().get(size - (i+1)).getStation().getCode();
                break;

            case R.id.spin_train_class:
                Log.e("spinner", "class");
//                ((TextView)adapterView.getChildAt(i)).setTextColor(Color.WHITE);
//                ((TextView)adapterView.getChildAt(i)).setTextSize(13);
                travelClass = (String) adapterSpinClass.getItem(i);
                break;

            case R.id.spin_train_quota:
                Log.e("spinner", "quota");
//                ((TextView)adapterView.getChildAt(i)).setTextColor(Color.WHITE);
//                ((TextView)adapterView.getChildAt(i)).setTextSize(13);
                if (i == 0) {
                    quota = "GN";
                } else if (i == 1) {
                    quota = "TQ";
                } else if (i == 2)
                    quota = "PT";
                break;


        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
