package io.tnine.trainstatus.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import io.tnine.trainstatus.MainActivity;
import io.tnine.trainstatus.Models.ModelCancelledTrains.CancelledTrainsModel;
import io.tnine.trainstatus.Models.ModelPnrStatus.PnrStatusModel;
import io.tnine.trainstatus.Models.ModelRescheduledTrains.RescheduledTrainsModel;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentTransaction ft;
    private FloatingActionButton btn_get_pnr_status;
    private EditText et_pnr_number;
    ApiInterface apiInterface;
    private ProgressDialog progressDialog;
    private FrameLayout btn_ad, btn_ad_2;


    public HomeFragment() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CardView btnLiveTrain = (CardView) view.findViewById(R.id.btn_live_train);
        final CardView btnRescheduledTrain = (CardView) view.findViewById(R.id.btn_rescheduled);
        btn_ad = view.findViewById(R.id.btn_ad);
        btn_ad_2 = view.findViewById(R.id.btn_ad_2);

        CardView btnCancelledTrains = (CardView) view.findViewById(R.id.btn_cancelled);
        CardView btnPnrStatus = view.findViewById(R.id.btn_pnr);
        CardView btnTrainSchedule = view.findViewById(R.id.btn_train_schedule);
        CardView btnLiveStationStatus = view.findViewById(R.id.btn_live_station);
        CardView btnSeatAvailability = view.findViewById(R.id.btn_seat_availability);
        CardView btnFareEnquiry = view.findViewById(R.id.btn_fair_enquiry);
        btn_get_pnr_status = (FloatingActionButton)view.findViewById(R.id.btn_get_pnr_status);
        et_pnr_number = (EditText)view.findViewById(R.id.et_pnr_number);

        refreshAd(true);

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());

        //TODO: set a common OnClickListener for all buttons
        Call<CancelledTrainsModel> call = apiInterface.getCancelledTrains(formattedDate, Config.myApiKey);
        call.enqueue(new Callback<CancelledTrainsModel>() {
            @Override
            public void onResponse(Call<CancelledTrainsModel> call, Response<CancelledTrainsModel> response) {
//                Toast.makeText(getActivity(),String.valueOf(response.body().getResponseCode()),Toast.LENGTH_LONG).show();
                    if (response.isSuccessful()) {
                    if (response.body().getResponseCode() == 200) {
                        Config.setCancelledTrainsModel(response.body());
                    }
                }
            }
            @Override
            public void onFailure(Call<CancelledTrainsModel> call, Throwable t) {
                Log.e("CancelTrain res fail", t.toString());
            }
        });

        Call <RescheduledTrainsModel> callReschTrains = apiInterface.getRescheduledTrains(formattedDate, Config.myApiKey);
        callReschTrains.enqueue(new Callback<RescheduledTrainsModel>() {
            @Override
            public void onResponse(Call<RescheduledTrainsModel> callReschTrains, Response<RescheduledTrainsModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().getResponseCode() == 200) {
                        Config.setRescheduledTrainsModel(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<RescheduledTrainsModel> callReschTrains, Throwable t) {
                Log.e("Resch. Trains Res. fail", t.toString());
            }
        });

        btnCancelledTrains.setOnClickListener(new View.OnClickListener() {                          //Cancelled Trains Button
            @Override
            public void onClick(View view) {
                if (Config.getCancelledTrainsModel()!=null) {
                    if (Config.getCancelledTrainsModel().getResponseCode() == 200) {
                        Fragment cancelledTrains = new CancelledTrains();
                        ((MainActivity) getActivity()).performTransaction(cancelledTrains, "cancelledTrains");
                    }
                }else Toast.makeText(getActivity(),"Problem in fetching data!",Toast.LENGTH_SHORT).show();

            }
        });

        btnLiveTrain.setOnClickListener(new View.OnClickListener() {                                //Live Train Status button
            @Override
            public void onClick(View view) {

                Fragment liveTrain = new LiveTrainFragment();
                ((MainActivity)getActivity()).performTransaction(liveTrain,"liveTrain");


            }
        });

        btnRescheduledTrain.setOnClickListener(new View.OnClickListener() {                         //Rescheduled Trains button
            @Override
            public void onClick(View view) {
                if (Config.getRescheduledTrainsModel()!=null) {
                    if (Config.getRescheduledTrainsModel().getResponseCode() == 200) {
                        Fragment rescheduledTrain = new RescheduledTrainsFragment();
                        ((MainActivity) getActivity()).performTransaction(rescheduledTrain,"rescheduledTrain");
                    }
                }else Toast.makeText(getActivity(),"Problem in fetching data!",Toast.LENGTH_SHORT).show();
            }
        });

        btnTrainSchedule.setOnClickListener(new View.OnClickListener() {                            //Train Schedule Button
            @Override
            public void onClick(View view) {
                Fragment trainRoute = new TrainRouteDetailsFragment();
                ((MainActivity)getActivity()).performTransaction(trainRoute,"trainRoute");
            }
        });

        btnLiveStationStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment liveStationStatus = new LiveStationFragment();
                ((MainActivity)getActivity()).performTransaction(liveStationStatus, "liveStationStatus");
            }
        });

        btnPnrStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment pnrDetails = new PnrDetailsFragment();
                ((MainActivity)getActivity()).performTransaction(pnrDetails,"pnrDetails");

            }
        });

        btnSeatAvailability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment seatAvailabilityDetailsFragment = new SeatAvailabilityDetailsFragment();
                ((MainActivity)getActivity()).performTransaction(seatAvailabilityDetailsFragment,"seatAvailabilityDetailsFragment");

            }
        });

        btnFareEnquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Fragment fareDetailsFragment = new FareDetailsFragment();
                ((MainActivity)getActivity()).performTransaction(fareDetailsFragment,"fareDetailsFragment");

            }
        });

//        editPnrNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_ACTION_DONE) {
//
//
//                    return true;
//                }
//                return false;
//            }
//
//        });


        btn_get_pnr_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_pnr_number.getText().length() == 10) {
                    InputMethodManager imm = (InputMethodManager) ((MainActivity)getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("loading....");
                    progressDialog.setTitle("Pnr status");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    Call <PnrStatusModel> call = apiInterface.getPnrStatus(et_pnr_number.getText().toString(), Config.myApiKey);
                    call.enqueue(new Callback<PnrStatusModel>() {
                        @Override
                        public void onResponse(Call<PnrStatusModel> call, Response<PnrStatusModel> response) {
                            if (response.isSuccessful()) {
                                Log.e("Request hit", "is successful");
//                                Toast.makeText(getActivity(),String.valueOf(response.body().getResponseCode()),Toast.LENGTH_SHORT).show();

                                if (response.body().getResponseCode() == 200) {
                                    Log.e("PNR Response", response.body().getResponseCode() + "");
                                    Config.setPnrStatusModel(response.body());
                                    Fragment pnrStatus = new PnrStatusFragment();
                                    ((MainActivity)getActivity()).performTransaction(pnrStatus,"pnrStatus");
                                    progressDialog.dismiss();
                                }else if (response.body().getResponseCode() == 220){
                                    Toast.makeText(getActivity(),"Invalid pnr",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }else if (response.body().getResponseCode() == 404){
                                    Toast.makeText(getActivity(),"Problem in fetching data",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<PnrStatusModel> call, Throwable t) {
                            Log.e("PNR response failure", t.toString());
                        }
                    });
                }
            }
        });

        return view;
    }

    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView, NativeAppInstallAdView adView2) {
        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAppInstallAd.getVideoController();

        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                // Publishers should allow native ads to complete video playback before refreshing
                // or replacing them with another ad in the same UI location.
                super.onVideoEnd();
            }
        });

        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setPriceView(adView.findViewById(R.id.appinstall_price));
//        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        adView.setStoreView(adView.findViewById(R.id.appinstall_store));

        // Some assets are guaranteed to be in every NativeAppInstallAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
//        ((TextView) adView.getHeadlineView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
//        ((TextView) adView.getBodyView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
//        ((Button) adView.getCallToActionView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((ImageView) adView.getIconView()).setImageDrawable(
                nativeAppInstallAd.getIcon().getDrawable());

        MediaView mediaView = (MediaView) adView.findViewById(R.id.appinstall_media);
        ImageView mainImageView = (ImageView) adView.findViewById(R.id.appinstall_image);

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeAppInstallAd has a video asset.
        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);
        } else {
            adView.setImageView(mainImageView);
            mediaView.setVisibility(View.GONE);

            // At least one image is guaranteed.
            List<NativeAd.Image> images = nativeAppInstallAd.getImages();
            mainImageView.setImageDrawable(images.get(0).getDrawable());
        }

        // These assets aren't guaranteed to be in every NativeAppInstallAd, so it's important to
        // check before trying to display them.
        if (nativeAppInstallAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
//            ((TextView) adView.getPriceView()).setTypeface(TypefaceUtil.getGothamMedium());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
//            ((TextView) adView.getStoreView()).setTypeface(TypefaceUtil.getGothamMedium());
        }


        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAppInstallAd);


        adView2.setHeadlineView(adView2.findViewById(R.id.appinstall_headline));
        adView2.setBodyView(adView2.findViewById(R.id.appinstall_body));
        adView2.setCallToActionView(adView2.findViewById(R.id.appinstall_call_to_action));
        adView2.setIconView(adView2.findViewById(R.id.appinstall_app_icon));
        adView2.setPriceView(adView2.findViewById(R.id.appinstall_price));
//        adView2.setStarRatingView(adView2.findViewById(R.id.appinstall_stars));
        adView2.setStoreView(adView2.findViewById(R.id.appinstall_store));

        // Some assets are guaranteed to be in every NativeAppInstallAd.
        ((TextView) adView2.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
//        ((TextView) adView2.getHeadlineView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((TextView) adView2.getBodyView()).setText(nativeAppInstallAd.getBody());
//        ((TextView) adView2.getBodyView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((Button) adView2.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
//        ((Button) adView2.getCallToActionView()).setTypeface(TypefaceUtil.getGothamMedium());
        ((ImageView) adView2.getIconView()).setImageDrawable(
                nativeAppInstallAd.getIcon().getDrawable());

        MediaView mediaView2 = (MediaView) adView2.findViewById(R.id.appinstall_media);
        ImageView mainImageView2 = (ImageView) adView2.findViewById(R.id.appinstall_image);

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeAppInstallAd has a video asset.
        if (vc.hasVideoContent()) {
            adView2.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);
        } else {
            adView2.setImageView(mainImageView2);
            mediaView2.setVisibility(View.GONE);

            // At least one image is guaranteed.
            List<NativeAd.Image> images = nativeAppInstallAd.getImages();
            mainImageView2.setImageDrawable(images.get(0).getDrawable());
        }

        // These assets aren't guaranteed to be in every NativeAppInstallAd, so it's important to
        // check before trying to display them.
        if (nativeAppInstallAd.getPrice() == null) {
            adView2.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView2.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView2.getPriceView()).setText(nativeAppInstallAd.getPrice());
//            ((TextView) adView2.getPriceView()).setTypeface(TypefaceUtil.getGothamMedium());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView2.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView2.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView2.getStoreView()).setText(nativeAppInstallAd.getStore());
//            ((TextView) adView2.getStoreView()).setTypeface(TypefaceUtil.getGothamMedium());
        }


        // Assign native ad object to the native view.
        adView2.setNativeAd(nativeAppInstallAd);
    }

    private void refreshAd(boolean requestAppInstallAds/*, boolean requestContentAds*/) {


        AdLoader.Builder builder = new AdLoader.Builder(getContext(), "ca-app-pub-7591469242682758/7955629806");

        if (requestAppInstallAds) {
            builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                @Override
                public void onAppInstallAdLoaded(NativeAppInstallAd ad) {
                    try {
                        NativeAppInstallAdView adView = (NativeAppInstallAdView) getActivity().getLayoutInflater()
                                .inflate(R.layout.ad_layout, null);
                        NativeAppInstallAdView adView2 = (NativeAppInstallAdView) getActivity().getLayoutInflater()
                                .inflate(R.layout.ad_layout, null);
                        populateAppInstallAdView(ad, adView, adView2);
                        btn_ad.setVisibility(View.VISIBLE);
                        btn_ad_2.setVisibility(View.VISIBLE);
                        btn_ad.removeAllViews();
                        btn_ad.addView(adView);
                        btn_ad_2.removeAllViews();
                        btn_ad_2.addView(adView2);
                    } catch (Exception e) {

                    }
                }
            });
        }

       /* if (requestContentAds) {
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                @Override
                public void onContentAdLoaded(NativeContentAd ad) {
                    try {
                        FrameLayout frameLayout =
                                (FrameLayout) view.findViewById(R.id.fl_adplaceholder);
                        NativeContentAdView adView = (NativeContentAdView) getActivity().getLayoutInflater()
                                .inflate(R.layout.ad_content, null);
                        populateContentAdView(ad, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    } catch (Exception e) {

                    }
                }
            });
        }*/

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                try {
                   /* Toast.makeText(getContext(), "Failed to load native ad: "
                            + errorCode, Toast.LENGTH_SHORT).show();*/
                } catch (NullPointerException e) {
//                    Log.d(e.getMessage());
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        }).build();
        /*Bundle bundle = new MoPubAdapter.BundleBuilder().setPrivacyIconSize(15).build();
        Bundle extras1 = new FacebookAdapter.FacebookExtrasBundleBuilder()
                .setNativeAdChoicesIconExpandable(false)
                .build();
        Bundle extras2 = new Bundle();
        extras2.putString(InMobiNetworkKeys.AGE_GROUP, InMobiNetworkValues.BETWEEN_18_AND_24);
        extras2.putString(InMobiNetworkKeys.AREA_CODE, "12345");*/
        adLoader.loadAd(new AdRequest.Builder()
                /*.addNetworkExtrasBundle(MoPubAdapter.class, bundle)
                .addNetworkExtrasBundle(FacebookAdapter.class, extras1)
                .addNetworkExtrasBundle(InMobiAdapter.class, extras2)*/.build());
    }



}
