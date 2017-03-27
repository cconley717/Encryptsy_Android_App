package com.encryptsy.SignInSignUp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.encryptsy.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Chris on 6/21/2016.
 */
public class HTTP_Communications
{
    private static OkHttpClient client;

    public interface OnServerRespond {
        public void onServerRespond(JSONObject response, boolean success, String errorReason);
    }

    public static void initialize()
    {
        client = new OkHttpClient.Builder().build();
    }

    public static void signUp(Context context, String username, String password, String decoyPassword, byte[] encryptedVaultKey, byte[] encryptedVaultIV, OnServerRespond listener)
    {
        RequestBody requestBody = buildSignUpRequestBody(username, password, decoyPassword, encryptedVaultKey, encryptedVaultIV);
        String url = context.getResources().getString(R.string.encryptsy_server_address) + "/signUp";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    public static void signIn(Context context, String username, String password, OnServerRespond listener)
    {
        RequestBody requestBody = buildSignInRequestBody(username, password);
        String url = context.getResources().getString(R.string.encryptsy_server_address) + "/signIn";

        Request request = buildPostRequest(requestBody, url);

        sendRequestToServer(request, listener);
    }

    private static Request buildPostRequest(RequestBody requestBody, String url)
    {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        return request;
    }

    private static RequestBody buildSignUpRequestBody(String username, String password, String decoyPassword, byte[] encryptedVaultKey, byte[] encryptedVaultIV)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("decoyPassword", decoyPassword)
                .add("encryptedVaultKey", Base64.encodeToString(encryptedVaultKey, Base64.NO_WRAP))
                .add("encryptedVaultIV", Base64.encodeToString(encryptedVaultIV, Base64.NO_WRAP))
                .build();

        return requestBody;
    }

    private static RequestBody buildSignInRequestBody(String username, String password)
    {
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        return requestBody;
    }



    private static void sendRequestToServer(final Request request, final OnServerRespond listener)
    {
        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("testing", "HTTP_CLIENT: " + e.getMessage());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onServerRespond(null, false, "An error occurred. Please try again.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                final String temp = response.body().string();
                Log.d("testing", "BODY: " + temp);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (!response.isSuccessful()) {
                            Log.d("testing", "HTTP_CLIENT: " + response.message());
                            listener.onServerRespond(null, false, "An error occurred. Please try again.");
                        }
                        else
                        {
                            try
                            {
                                JSONObject jsonObject = new JSONObject(temp);

                                boolean isError = jsonObject.getBoolean("isError");

                                if(isError)
                                {
                                    String errorReason = jsonObject.getString("errorReason");
                                    listener.onServerRespond(jsonObject, false, errorReason);
                                }
                                else
                                    listener.onServerRespond(jsonObject, true, "");
                            }
                            catch (JSONException e)
                            {
                                listener.onServerRespond(null, false, "An error occurred. Please try again.");
                                e.printStackTrace();
                            }
                        }

                        response.body().close();
                    }
                });
            }
        });
    }
}
