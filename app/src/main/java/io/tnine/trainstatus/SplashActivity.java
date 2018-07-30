package io.tnine.trainstatus;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import io.tnine.trainstatus.Utils.Config;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (isWorkingInternetPersent()){
            Thread thread = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                    }
                }
            };
            thread.start();
        }else {
            Snackbar.make(findViewById(android.R.id.content), "No internet access!", Snackbar.LENGTH_LONG)
                    .setAction("TURN ON", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .show();
        }

    }

    public boolean isWorkingInternetPersent() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }
}
