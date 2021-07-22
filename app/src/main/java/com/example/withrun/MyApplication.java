package com.example.withrun;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class MyApplication extends Application {

    final String TAG = "MyApplication";


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);


        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        });
    }


    private void registerReceiver(BroadcastReceiver broadcastReceiver) {
    }

    ActivityLifecycleCallbacks mActivityLifecycleCallbacks =
            new ActivityLifecycleCallbacks() {

                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityCreated("+activity.getLocalClassName()+")");

                    if ( MainActivity.Companion.getLoginNickname() == "" ) {

                        SharedPreferences userData = getSharedPreferences("User",MODE_PRIVATE);

                        MainActivity.Companion.setLoginId(userData.getInt("loginId",0));
                        MainActivity.Companion.setLoginEmail(userData.getString("loginEmail",""));
                        MainActivity.Companion.setLoginMarketing_noti(userData.getString("loginMarketing_noti",""));
                        MainActivity.Companion.setLoginNickname(userData.getString("loginNickname",""));
                        MainActivity.Companion.setLoginGender(userData.getString("loginGender",""));
                        MainActivity.Companion.setLoginBirth(userData.getString("loginBirth",""));
                        MainActivity.Companion.setLoginHeight(userData.getString("loginHeight",""));
                        MainActivity.Companion.setLoginWeight(userData.getString("loginWeight",""));
                        MainActivity.Companion.setLoginProfileImgPath(userData.getString("loginProfileImgPath",""));

                    }
                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityStarted("+activity.getLocalClassName()+")");
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityResumed("+activity.getLocalClassName()+")");

                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityPaused("+activity.getLocalClassName()+")");
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityStopped("+activity.getLocalClassName()+")");
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivitySaveInstanceState("+activity.getLocalClassName()+")");

                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                    Log.d(TAG, "mActivityLifecycleCallbacks: onActivityDestroyed("+activity.getLocalClassName()+")");
                }
            };

}

