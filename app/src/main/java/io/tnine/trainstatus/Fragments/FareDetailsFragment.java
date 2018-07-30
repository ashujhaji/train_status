package io.tnine.trainstatus.Fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import io.tnine.trainstatus.Models.ModelFareEnquiry.FareEnquiryModel;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrain;
import io.tnine.trainstatus.Models.ModelTrainRoute.TrainRouteModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.DelayAutoCompleteTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FareDetailsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

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
    String[] ageGroups;
    String[] quotaCodes;
    ArrayAdapter adapterSpinClass;
    ArrayAdapter adapterSpinSrc;
    ArrayAdapter adapterSpinDest;
    ArrayAdapter adapterSpinQuota;
    ArrayAdapter adapterSpinAge;
    String src;
    String dest;
    String age;
    String quota;
    String travelClass;
    ProgressDialog progressDoalog;
    private RelativeLayout cancel_fare_txt;
    private Dialog mDialog;
    private ProgressBar fare_prog;
    private AdView mAdView;

    public FareDetailsFragment() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        ageGroups = new String[]{"CHILD[5-11]", "ADULT[12+]", "SENIOR FEMALE[58+]", "SENIOR MALE[60+]"};
        quotaCodes = new String[]{"GENERAL", "TATKAL", "PREMIUM TATKAL"};
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fare_details, container, false);

        AppCompatSpinner spinnerSrc = view.findViewById(R.id.spin_train_source);
        AppCompatSpinner spinnerDest = view.findViewById(R.id.spin_train_dest);
        AppCompatSpinner spinnerClass = view.findViewById(R.id.spin_train_class);
        AppCompatSpinner spinnerQuota = view.findViewById(R.id.spin_train_quota);
        AppCompatSpinner spinnerAge = view.findViewById(R.id.spin_age);
        final AutoCompleteTextView autoTrainCode = view.findViewById(R.id.acmtv_fair_train_code);
        editTextDate = view.findViewById(R.id.et_fare_pick_date);
        final Button btnGetFare = view.findViewById(R.id.btn_get_fare);
        cancel_fare_txt = (RelativeLayout)view.findViewById(R.id.cancel_fare_txt);
        fare_prog = view.findViewById(R.id.fare_prog);

        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        btnGetFare.setEnabled(false);
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

//        autoTrainCode.setThreshold(2);
//        autoTrainCode.setAdapter(new TrainAutoCompleteAdapter(getActivity()));

        autoTrainCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().length() == 3) {
                    ((MainActivity)getActivity()).callMe(charSequence, R.id.acmtv_fair_train_code, "train");
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
        adapterSpinAge = new ArrayAdapter(getContext(), R.layout.simple_spinner_dropdown_item, ageGroups);

        autoTrainCode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                fare_prog.setVisibility(View.VISIBLE);
                String item = adapterView.getItemAtPosition(position).toString();
                trainCode = item.substring(0, item.indexOf("-")).trim();
                btnGetFare.setEnabled(true);
                Call<TrainRouteModel> call = apiInterface.getTrainRoute(trainCode, Config.myApiKey);
                call.enqueue(new Callback<TrainRouteModel>() {
                    @Override
                    public void onResponse(Call<TrainRouteModel> call, Response<TrainRouteModel> response) {
                        if(response.isSuccessful()) {
                            if(response.body().getResponseCode() == 200) {
                                Log.e("Fare Train Code Res", "Data loaded");
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

                                fare_prog.setVisibility(View.GONE);

                            }else Toast.makeText(getActivity(),"Problem in getting data",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TrainRouteModel> call, Throwable t) {
                        Toast.makeText(getActivity(), "Request didn't go through", Toast.LENGTH_LONG).show();
                        Log.e("Fare res failed", t.toString());
                    }
                });
            }
        });

        adapterSpinClass.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinSrc.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinDest.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinAge.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapterSpinQuota.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        spinnerClass.setAdapter(adapterSpinClass);
        spinnerDest.setAdapter(adapterSpinDest);
        spinnerSrc.setAdapter(adapterSpinSrc);
        spinnerQuota.setAdapter(adapterSpinQuota);
        spinnerAge.setAdapter(adapterSpinAge);

        spinnerClass.setOnItemSelectedListener(this);
        spinnerAge.setOnItemSelectedListener(this);
        spinnerQuota.setOnItemSelectedListener(this);
        spinnerDest.setOnItemSelectedListener(this);
        spinnerSrc.setOnItemSelectedListener(this);

        btnGetFare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDoalog = new ProgressDialog(getActivity());
                progressDoalog.setCanceledOnTouchOutside(false);
                progressDoalog.setMessage("loading....");
                progressDoalog.setTitle("Fare Enquiry");
                progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDoalog.show();



                Call<FareEnquiryModel> call = apiInterface.getFareEnquiry(trainCode,
                        src, dest, age, travelClass, quota, editTextDate.getText().toString(), Config.myApiKey);
                call.enqueue(new Callback<FareEnquiryModel>() {
                    @Override
                    public void onResponse(Call<FareEnquiryModel> call, Response<FareEnquiryModel> response) {

                        if (response.isSuccessful()) {

                            Log.e("fare hit", "success");

                            if(response.body().getResponseCode() == 200){
                                progressDoalog.dismiss();
                                Log.e("Fare res", "data loaded");
                                Config.setFareEnquiryModel(response.body());
                                mDialog = new Dialog(getActivity());
                                mDialog.setContentView(R.layout.fare_dialog);
                                TextView mNum = mDialog.findViewById(R.id.fare_train_no);
                                TextView mName = mDialog.findViewById(R.id.fare_train_name);
                                TextView mFare = mDialog.findViewById(R.id.fare_train_price);
                                TextView mClass = mDialog.findViewById(R.id.fare_train_class);
                                ProgressBar mProgress = mDialog.findViewById(R.id.fare_progress);
                                ImageView imageView = mDialog.findViewById(R.id.cancel);

                                imageView.setEnabled(true);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDialog.dismiss();
                                    }
                                });
                                mNum.setText(Config.getFareEnquiryModel().getTrain().getNumber());
                                mName.setText(Config.getFareEnquiryModel().getTrain().getName());
                                mFare.setText("\u20B9"+Config.getFareEnquiryModel().getFare());
                                mClass.setText("Class : "+Config.getFareEnquiryModel().getJourneyClass().getName());
                                mProgress.setVisibility(View.INVISIBLE);

                                mDialog.show();
//                                Fragment fareFragment = new FareFragment();
//                                ((MainActivity)getActivity()).performTransaction(fareFragment);
                            }else {
                                mDialog.dismiss();
                                Toast.makeText(getActivity(),"Problem in getting data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FareEnquiryModel> call, Throwable t) {
                        Log.e("Fare res ", "failed");

                    }
                });
            }

        });

        cancel_fare_txt.setOnClickListener(new View.OnClickListener() {
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

//        btnCancelInputFare.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                autoTrainCode.setText("");
//                srcStations.clear();
//                destStations.clear();
//                classCodes.clear();
//
//                adapterSpinSrc.notifyDataSetChanged();
//                adapterSpinDest.notifyDataSetChanged();
//                adapterSpinClass.notifyDataSetChanged();
//            }
//        });



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

            case R.id.spin_age:
                Log.e("spinner", "age");
//                ((TextView)adapterView.getChildAt(i)).setTextColor(Color.WHITE);
//                ((TextView)adapterView.getChildAt(i)).setTextSize(13);
                if (i == 0) {
                    age = "9";
                } else if (i == 1) {
                    age = "18";
                } else if (i == 2) {
                    age = "58";
                } else if (i == 3) {
                    age = "61";
                }

                break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
