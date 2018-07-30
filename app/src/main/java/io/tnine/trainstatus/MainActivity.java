package io.tnine.trainstatus;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.Console;
import java.util.List;

import io.tnine.trainstatus.Fragments.CancelledTrains;
import io.tnine.trainstatus.Fragments.FareDetailsFragment;
import io.tnine.trainstatus.Fragments.HomeFragment;
import io.tnine.trainstatus.Fragments.LiveTrainFragment;
import io.tnine.trainstatus.Fragments.PnrDetailsFragment;
import io.tnine.trainstatus.Fragments.SeatAvailabilityDetailsFragment;
import io.tnine.trainstatus.Fragments.TrainRouteDetailsFragment;
import io.tnine.trainstatus.Interfaces.ApiInterface;
import io.tnine.trainstatus.Utils.ApiClient;
import io.tnine.trainstatus.Utils.Config;
import io.tnine.trainstatus.Utils.Constants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final boolean DEFAULT = false;
    private ImageView bg_img;
    FrameLayout frame;
    private int count = 0;
    private Dialog dialog;
    private AdView mAdView;
    ApiInterface apiInterface;
    private Fragment homeFragment = new HomeFragment();
    public void performTransaction(Fragment fragment, String backStackVal){
        FragmentTransaction ft;
        bg_img.setVisibility(View.GONE);
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, fragment, backStackVal).addToBackStack(backStackVal).commit();
        count++;


    }
    public void  callMe(CharSequence constraint, int resId, String callContext){
        RemoteData remoteData = new RemoteData(this);
        if (callContext.equals("train")){
            remoteData.getSuggestionsData(constraint, resId);
        }

        else if(callContext.equals("station")){
            remoteData.getStationSuggestionsData(constraint, resId);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RemoteData remoteData = new RemoteData(this);
        bg_img = findViewById(R.id.bg_img);
        MobileAds.initialize(this, Config.ADMOB_MAIN_ID);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("304bc2c8-928f-4596-8d4d-47731c27cffc")  // An example device ID
                .build();

        sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("status",DEFAULT)) {
            Config.dialogPopup(MainActivity.this);
            editor.putBoolean("status", true);
            editor.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 0;
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();

                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        Config.myApiKey = mFirebaseRemoteConfig.getString("abcd");
                        Log.d("keys",Config.myApiKey);
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, homeFragment, "homeFrag").addToBackStack("home").commit();
                    }
                });


        Logger.addLogAdapter(new AndroidLogAdapter());

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_ad_back);
        TextView cancel = dialog.findViewById(R.id.cancel_back);
        TextView exit = dialog.findViewById(R.id.exit_app);
        mAdView = dialog.findViewById(R.id.adView);
        AdView adView = new AdView(MainActivity.this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-7591469242682758/1689870281");

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (count==0) {
                dialog.show();
            }else{
                super.onBackPressed();
                count--;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check all kind of railways query\nOne app for all services.\nDownload now\n" + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_live_train) {
            Fragment liveTrain = new LiveTrainFragment();
            (MainActivity.this).performTransaction(liveTrain, "liveTrain");

        }else if(id == R.id.nav_pnr_status){
            Fragment pnr = new HomeFragment();
            (MainActivity.this).performTransaction(pnr, "pnr");
        }else if(id == R.id.nav_train_route){
            Fragment route = new TrainRouteDetailsFragment();
            (MainActivity.this).performTransaction(route, "route");
        }else if(id == R.id.nav_fare_enquiry){
            Fragment fare = new FareDetailsFragment();
            (MainActivity.this).performTransaction(fare,"fare");
        }else if(id == R.id.nav_seat_avail){
            Fragment seat = new SeatAvailabilityDetailsFragment();
            (MainActivity.this).performTransaction(seat, "seat");
        }else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_contact) {


        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check all kind of railways query\nOne app for all services.\nDownload now\n" + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_rate) {
            RateMyAppL();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void RateMyAppL() {
        Uri uri = Uri.parse("market://details?id=" +getApplicationContext().getPackageName());
        final RatingDialog ratingDialog = new RatingDialog.Builder(MainActivity.this)
                .icon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.train_icon))
                .threshold(4)
                .title("Are you satisfied with this app? Please rate us. ")
                .titleTextColor(R.color.black)
                .positiveButtonText("Not Now")
                .positiveButtonTextColor(R.color.color_light_text)
                .ratingBarColor(R.color.yellow)
                .playstoreUrl(uri.toString())
                .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                    @Override
                    public void onThresholdCleared(final RatingDialog ratingDialog, float rating, boolean thresholdCleared) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                ratingDialog.dismiss();

                                /////////////

                                // Build an AlertDialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                LayoutInflater inflater = getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.alertbox_custom_layout, null);

                                // Set the custom layout as alert dialog view
                                builder.setView(dialogView);

                                // Get the custom alert dialog view widgets reference
                                TextView btn_positive = (TextView) dialogView.findViewById(R.id.dialog_positive_btn);
                                TextView btn_negative = (TextView) dialogView.findViewById(R.id.dialog_neutral_btn);


                                // Create the alert dialog
                                final AlertDialog dialog = builder.create();

                                // Set positive/yes button click listener
                                btn_positive.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Dismiss the alert dialog

                                        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        try {
                                            startActivity(myAppLinkToMarket);
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(getApplicationContext(), " unable to find market app", Toast.LENGTH_LONG).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });

                                // Set negative/no button click listener
                                btn_negative.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Dismiss the alert dialog
                                        dialog.dismiss();

                                    }
                                });
                                // Display the custom alert dialog on interface
                                dialog.show();




                                //////////////
                            }
                        }, 500);


                    }
                }).onThresholdFailed(new RatingDialog.Builder.RatingThresholdFailedListener() {
                    @Override
                    public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                        ratingDialog.dismiss();
                        //                       CustomToast.getInstance().setCustomToast("Thanks for your feedback. We will work on improvements.");
                        Toast.makeText(getApplicationContext(), "Thanks for your feedback. We will work on improvements.", Toast.LENGTH_SHORT).show();

                    }
                }).build();

        ratingDialog.show();

    }

}