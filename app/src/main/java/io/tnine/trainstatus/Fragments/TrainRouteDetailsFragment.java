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

import io.tnine.trainstatus.Adapters.TrainAutoCompleteAdapter;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelLiveTrainStatus.LiveTrain;
import io.tnine.trainstatus.Models.ModelTrainRoute.TrainRouteModel;
import io.tnine.trainstatus.R;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.DelayAutoCompleteTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TrainRouteDetailsFragment extends Fragment {
    ApiInterface apiInterface;
    private LiveTrain train;
    private String trainCode;
    private TrainRouteModel model;
    private AutoCompleteTextView auto_txt_route;
    private ProgressDialog progressDialog;
    private RelativeLayout cancel_route_txt;
    private AdView mAdView;

    public TrainRouteDetailsFragment() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_train_route_details, container, false);

        final Button btnGetTrainRoute = view.findViewById(R.id.btn_get_train_route);
        Button btnCancelInputRoute = view.findViewById(R.id.btn_cancel_input_route);
        cancel_route_txt = (RelativeLayout)view.findViewById(R.id.cancel_route_txt);
        auto_txt_route = (AutoCompleteTextView)view.findViewById(R.id.auto_txt_route);
        mAdView = view.findViewById(R.id.adView);
        AdView adView = new AdView(getActivity());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        btnGetTrainRoute.setEnabled(false);
        auto_txt_route.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() == 3) {
                    ((MainActivity)getActivity()).callMe(charSequence, R.id.auto_txt_route, "train");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        auto_txt_route.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("clickitem", parent.getItemAtPosition(position) + "");
                String item = parent.getItemAtPosition(position).toString();
                trainCode = item.substring(0, item.indexOf("-")).trim();
                btnGetTrainRoute.setEnabled(true);
            }
        });


        btnGetTrainRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                if (auto_txt_route.getText().toString().isEmpty()||auto_txt_route==null){
                    auto_txt_route.setError("Field is empty");
                    auto_txt_route.requestFocus();
                }else {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("loading....");
                    progressDialog.setTitle("Train Route");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    Call<TrainRouteModel> call = apiInterface.getTrainRoute(trainCode, Config.myApiKey);
                    call.enqueue(new Callback<TrainRouteModel>() {
                        @Override
                        public void onResponse(Call<TrainRouteModel> call, Response<TrainRouteModel> response) {
                            if(response.isSuccessful()) {
                                Log.e("Request hit", "is successful");

                                if(response.body().getResponseCode() == 200) {
                                    Log.e("Train Route Response", "200");
                                    model = response.body();
                                    Config.setTrainRouteModel(model);
                                    Fragment trainRoute = new TrainRouteFragment();
                                    ((MainActivity)getActivity()).performTransaction(trainRoute,"trainRoute");
                                    progressDialog.dismiss();
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Data not available",Toast.LENGTH_SHORT);
                                }
                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_SHORT);
                            }
                        }
                        @Override
                        public void onFailure(Call<TrainRouteModel> call, Throwable t) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_SHORT);
                            Log.e("Train route res failure", t.toString());
                        }
                    });
                }

            }
        });

        cancel_route_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto_txt_route.setText("");
            }
        });




        return view;
    }

}