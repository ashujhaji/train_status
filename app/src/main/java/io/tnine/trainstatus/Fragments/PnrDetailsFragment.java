package io.tnine.trainstatus.Fragments;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Queue;

import io.tnine.trainstatus.Adapters.StationAutoCompleteAdapter;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelCancelledTrains.CancelledTrainsModel;
import io.tnine.trainstatus.Models.ModelPnrStatus.PnrStatusModel;
import io.tnine.trainstatus.Models.ModelRescheduledTrains.RescheduledTrainsModel;
import io.tnine.trainstatus.Models.Station;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.DelayAutoCompleteTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PnrDetailsFragment extends Fragment {

    ApiInterface apiInterface;
    private Boolean trainsEnabledState = false;
    private Boolean ifFromStationEntered = false;
    private Boolean ifToStationEntered = false;
    ProgressDialog progressDialog;

    private RelativeLayout cancel_btw_src, cancel_btw_des;
    private AdView mAdView;

    String substrFrom;
    String substrTo;

    public PnrDetailsFragment() {

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pnr_details, container, false);
        final EditText editTextDate = (EditText) view.findViewById(R.id.et_pick_dat);
        final AutoCompleteTextView autoTo = (AutoCompleteTextView) view.findViewById(R.id.acmtv_to);
        final AutoCompleteTextView autoFrom = (AutoCompleteTextView) view.findViewById(R.id.acmtv_from);
        final Button btnGetTrainsBetweenStations = (Button) view.findViewById(R.id.btn_get_trains_bw_stations);
        cancel_btw_src = (RelativeLayout)view.findViewById(R.id.cancel_btw_src);
        cancel_btw_des = (RelativeLayout)view.findViewById(R.id.cancel_btw_des);

        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        btnGetTrainsBetweenStations.setEnabled(trainsEnabledState);

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());
        editTextDate.setText(formattedDate);

        final DatePickerDialog.OnDateSetListener mdate = new DatePickerDialog.OnDateSetListener() {

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

        autoFrom.setThreshold(2);
        autoFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!Config.isNumeric(charSequence.toString())) {
                    if (charSequence.toString().length() == 3) {
                        ((MainActivity) getActivity()).callMe(charSequence, R.id.acmtv_from, "station");

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        autoTo.setThreshold(2);
        autoTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (!Config.isNumeric(charSequence.toString())) {
                    if (charSequence.toString().length() == 3) {
                        ((MainActivity) getActivity()).callMe(charSequence, R.id.acmtv_to, "station");

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        autoFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {                     //From Station AutoCompleteTextView
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = adapterView.getItemAtPosition(position).toString();
                substrFrom = item.substring(0, item.indexOf("-")).trim();
                ifFromStationEntered = true;
                if(ifFromStationEntered == true && ifToStationEntered == true){
                    trainsEnabledState = true;
                    Log.e("button state", trainsEnabledState.toString());

                }
            }
        });

        autoTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {                       //To Station AutoCompleteTextView
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = adapterView.getItemAtPosition(position).toString();
                substrTo = item.substring(0, item.indexOf("-")).trim();
                ifToStationEntered = true;
                if(ifFromStationEntered == true && ifToStationEntered == true){
                    trainsEnabledState = true;
                    btnGetTrainsBetweenStations.setEnabled(trainsEnabledState);
                    Log.e("button state", trainsEnabledState.toString());

                }
            }
        });


        btnGetTrainsBetweenStations.setOnClickListener(new View.OnClickListener() {                 //Trains Between Stations Button
            @Override
            public void onClick(View view) {
                if (autoFrom.getText().toString().equals(autoTo.getText().toString())){
                    Toast.makeText(getActivity(),"Source and destination must be different",Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("loading....");
                progressDialog.setTitle("Checking trains");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                progressDialog.show();

                String fromAutoCompleteTextViewText = autoFrom.getText().toString();
                String toAutoCompleteTextViewText = autoTo.getText().toString();
                String dateChosen = editTextDate.getText().toString();
                Log.d("Test",dateChosen);


                Bundle bundle = new Bundle();
                bundle.putString("src_stn_code", substrFrom);
                bundle.putString("dest_stn_code", substrTo);
                bundle.putString("trains_bw_date", dateChosen);

                Fragment trainsbwStations = new TrainsBetweenStationsFragment();
                trainsbwStations.setArguments(bundle);

                ((MainActivity)getActivity()).performTransaction(trainsbwStations,"trainsbwStations");
                progressDialog.dismiss();

            }
        });

        cancel_btw_src.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFrom.setText("");
                btnGetTrainsBetweenStations.setEnabled(false);
            }
        });

        cancel_btw_des.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTo.setText("");
                btnGetTrainsBetweenStations.setEnabled(false);
            }
        });



        return view;
    }

}
