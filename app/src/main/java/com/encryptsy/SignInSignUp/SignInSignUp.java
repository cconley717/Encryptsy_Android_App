package com.encryptsy.SignInSignUp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import com.encryptsy.EncryptDecrypt.EncryptDecrypt;
import com.encryptsy.R;
import com.encryptsy.Utilities.DialogDispatcher;
import com.encryptsy.Utilities.VaultManager;
import com.gc.materialdesign.views.ButtonRectangle;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.security.Security;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SignInSignUp extends AppCompatActivity {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private SignInSignUp instance;

    private Context context;

    private TabHost tabHost;
    private ButtonRectangle signInButton, clearSignInButton, signUpButton, clearSignUpButton;
    private MaterialEditText usernameSignInInput, passwordSignInInput,
            usernameSignUpInput, passwordSignUp1Input, passwordSignUp2Input, decoyPasswordSignUp1Input, decoyPasswordSignUp2Input;

    final private int ENCRYPT_DECRYPT_ACTIVITY = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_sign_up);

        this.instance = this;
        this.context = this;

        registerResources();
        registerListeners();
        initializeTabHost();

        /*
        String data = "hello world";

        MessageDigest mda = null;
        try {
            mda = MessageDigest.getInstance("SHA-512", "BC");
            byte [] digesta = mda.digest(data.getBytes());
            Log.d("testing", new String(Hex.encode(digesta)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_in_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_howProtected) {
            showHowAmIProtectedDialog();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            Log.d("testing", "result returned");
            switch (requestCode) {
                case ENCRYPT_DECRYPT_ACTIVITY:

                    break;
            }
        }
    }



    public void registerResources() {
        tabHost = (TabHost) findViewById(R.id.tabHost);

        signInButton = (ButtonRectangle) findViewById(R.id.signInButton);
        clearSignInButton = (ButtonRectangle) findViewById(R.id.clearSignInButton);
        signUpButton = (ButtonRectangle) findViewById(R.id.signUpButton);
        clearSignUpButton = (ButtonRectangle) findViewById(R.id.clearSignUpButton);

        usernameSignInInput = (MaterialEditText) findViewById(R.id.usernameSignInInput);
        passwordSignInInput = (MaterialEditText) findViewById(R.id.passwordSignInInput);
        usernameSignUpInput = (MaterialEditText) findViewById(R.id.usernameSignUpInput);
        passwordSignUp1Input = (MaterialEditText) findViewById(R.id.passwordSignUp1Input);
        passwordSignUp2Input = (MaterialEditText) findViewById(R.id.passwordSignUp2Input);
        decoyPasswordSignUp1Input = (MaterialEditText) findViewById(R.id.decoyPasswordSignUp1Input);
        decoyPasswordSignUp2Input = (MaterialEditText) findViewById(R.id.decoyPasswordSignUp2Input);
    }

    public void registerListeners() {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if ("Login".equals(tabId)) {

                }
                if ("SignUp".equals(tabId)) {

                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInSignUpPermissionsDispatcher.signInWithCheck(instance);
                //signIn();
            }
        });

        clearSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameSignInInput.setText("");
                passwordSignInInput.setText("");
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInSignUpPermissionsDispatcher.signUpWithCheck(instance);
                //signUp();
            }
        });

        clearSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSignUpInputs();
            }
        });
    }

    public void initializeTabHost() {
        tabHost.setup();

        TabHost.TabSpec signInTabSpec = tabHost.newTabSpec("SignIn");
        signInTabSpec.setContent(R.id.signInTab);
        signInTabSpec.setIndicator("Sign In");

        TabHost.TabSpec signUpTabSpec = tabHost.newTabSpec("SignUp");
        signUpTabSpec.setContent(R.id.signUpTab);
        signUpTabSpec.setIndicator("Sign Up");

        tabHost.addTab(signInTabSpec);
        tabHost.addTab(signUpTabSpec);
    }

    private boolean signUpInputsAreGood(String username, String password1, String password2, String decoyPassword1, String decoyPassword2) {
        boolean isInputGood = true;
        String title = "Sign Up Error";
        String message = "";

        if (username.equals("")) {
            message += "-username cannot be blank\n";
            isInputGood = false;
        }
        if (password1.equals("") || password2.equals("")) {
            message += "-password cannot be blank\n";
            isInputGood = false;
        }
        if (decoyPassword1.equals("") || decoyPassword2.equals("")) {
            message += "-nuke password cannot be blank\n";
            isInputGood = false;
        }

        if (username.length() > 50) {

            isInputGood = false;
        }
        if (password1.length() > 50) {

            isInputGood = false;
        }
        if (password2.length() > 50) {

            isInputGood = false;
        }
        if (decoyPassword1.length() > 50) {

            isInputGood = false;
        }
        if (decoyPassword2.length() > 50) {

            isInputGood = false;
        }


        if (!password1.equals(password2)) {
            message += "-passwords do not match\n";
            isInputGood = false;
        }
        if (!decoyPassword1.equals(decoyPassword2)) {
            message += "-nuke passwords do not match\n";
            isInputGood = false;
        }
        if (password1.equals(password2) && password1.equals(decoyPassword1) && password1.equals(decoyPassword2)) {
            message += "-regular password and nuke password cannot be the same\n";
            isInputGood = false;
        }

        if (!isInputGood)
            DialogDispatcher.showInformationDialog(this, title, message);

        return isInputGood;
    }

    public boolean signInInputsAreGood(String username, String password) {
        boolean isInputGood = true;
        String title = "Sign In Error";
        String message = "";

        if (username.equals("")) {
            message += "-username cannot be blank\n";
            isInputGood = false;
        }
        if (password.equals("")) {
            message += "-password cannot be blank\n";
            isInputGood = false;
        }

        if (username.length() > 50) {

            isInputGood = false;
        }
        if (password.length() > 50) {

            isInputGood = false;
        }

        if (!isInputGood)
            DialogDispatcher.showInformationDialog(this, title, message);

        return isInputGood;
    }

    private void showHowAmIProtectedDialog() {
        String title = getString(R.string.how_am_i_protected_dialog_title);
        String message = getResources().getString(R.string.encryptsy_protection);

        DialogDispatcher.showInformationDialog(this, title, message);
    }

    private void showAboutDialog() {
        String title = getString(R.string.about_encryptsy_dialog_title);
        Spanned message = Html.fromHtml(getString(R.string.encryptsy_about));

        DialogDispatcher.showInformationDialog(this, title, message);
    }

    private void clearSignUpInputs() {
        usernameSignUpInput.setText("");
        passwordSignUp1Input.setText("");
        passwordSignUp2Input.setText("");
        decoyPasswordSignUp1Input.setText("");
        decoyPasswordSignUp2Input.setText("");
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void signIn()
    {
        final String username = usernameSignInInput.getText().toString().toLowerCase();
        String password = passwordSignInInput.getText().toString();

        if (signInInputsAreGood(username, password)) {
            DialogDispatcher.showPassiveProgressDialog(context, "Signing Into Encryptsy", "...please wait..");

            VaultManager.loadVault(context, username, password, new VaultManager.OnVaultManagerRespond() {
                @Override
                public void onVaultManagerRespond(JSONObject returnToken, boolean success, String reason) {
                    if (success) {
                        usernameSignInInput.setText("");
                        passwordSignInInput.setText("");

                        Log.d("testing", "sign in success!");
                        Intent encryptDecryptActivity = new Intent(SignInSignUp.this, EncryptDecrypt.class);
                        encryptDecryptActivity.putExtra("username", username);
                        startActivityForResult(encryptDecryptActivity, ENCRYPT_DECRYPT_ACTIVITY);

                        DialogDispatcher.dismissCurrentDialog();
                    } else {
                        passwordSignInInput.setText("");
                        DialogDispatcher.adjustToInformationDialog("Oops", reason, "Ok");
                    }
                }
            });
        }
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void signUp()
    {
        final String username = usernameSignUpInput.getText().toString().toLowerCase();
        final String password1 = passwordSignUp1Input.getText().toString();
        final String password2 = passwordSignUp2Input.getText().toString();
        final String decoyPassword1 = decoyPasswordSignUp1Input.getText().toString();
        final String decoyPassword2 = decoyPasswordSignUp2Input.getText().toString();

        if (signUpInputsAreGood(username, password1, password2, decoyPassword1, decoyPassword2)) {
            DialogDispatcher.showPassiveProgressDialog(context, "Creating account", "...please wait...");

            try {
                VaultManager.createVault(context, username, password1, decoyPassword1, new VaultManager.OnVaultManagerRespond() {
                    @Override
                    public void onVaultManagerRespond(JSONObject returnToken, boolean success, String reason) {
                        String status;
                        String message;

                        if (success) {
                            Log.d("testing", "sign up success!");

                            try {
                                String username = returnToken.getString("username");
                                String password = returnToken.getString("password");
                                String decoyPassword = returnToken.getString("decoyPassword");

                                status = "Account Created!";
                                message = "Username: " + username + "\nPassword: " + password +
                                        "\nDecoy Password: " + decoyPassword +

                                        "\n\nYour account has been created!" +
                                        "\n\nIf you would like to know how Encryptsy protects you, " +
                                        "select that option in the dropdown menu above.";

                                usernameSignUpInput.setText("");
                                passwordSignUp1Input.setText("");
                                passwordSignUp2Input.setText("");
                                decoyPasswordSignUp1Input.setText("");
                                decoyPasswordSignUp2Input.setText("");

                                tabHost.setCurrentTab(0);

                            } catch (JSONException e) {

                                status = "Oops";
                                message = "An error occurred. Please try again";

                                e.printStackTrace();
                            }
                        } else {
                            status = "oops";
                            message = reason;
                        }

                        DialogDispatcher.adjustToInformationDialog(status, message, "Ok");
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationaleForStorage(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("Encryptsy requires access to your device's storage in order to encrypt and decrypt files. \n\nTouch 'ok' to proceed.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showDeniedForStorage() {
        Toast.makeText(this, "unable to continue", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showNeverAskForStorage() {
        //Toast.makeText(this, "never ask", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SignInSignUpPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}