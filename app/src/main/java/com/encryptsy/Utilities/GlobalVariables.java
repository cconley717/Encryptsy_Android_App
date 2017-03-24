package com.encryptsy.Utilities;

import android.app.Application;
import android.util.Log;

import com.encryptsy.SignInSignUp.HTTP_Communications;

/**
 * Created by Chris on 6/21/2016.
 */
public class GlobalVariables extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Log.d("testing", "initializing global variables");

        HTTP_Communications.initialize();
    }
}
