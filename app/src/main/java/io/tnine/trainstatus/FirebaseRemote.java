package io.tnine.trainstatus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import io.tnine.trainstatus.Utils.Config;

public class FirebaseRemote {

    public static void initializeConfig(){
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        long cacheExpiration = 0;
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(, "Fetch Succeeded", Toast.LENGTH_SHORT).show();

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();

                        } else {
//                            Toast.makeText(MainActivity.this, "Fetch Failed",
//                                    Toast.LENGTH_SHORT).show();
                        }
                        Config.myApiKey = mFirebaseRemoteConfig.getString("abcd");
//                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, homeFragment).commit();
                    }
                });
    }
}
