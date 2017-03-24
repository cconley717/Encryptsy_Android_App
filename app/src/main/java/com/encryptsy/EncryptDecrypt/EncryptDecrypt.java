package com.encryptsy.EncryptDecrypt;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.encryptsy.R;
import com.encryptsy.Utilities.DialogDispatcher;
import com.encryptsy.Utilities.FileItem;
import com.encryptsy.Utilities.FileUtils;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarIndeterminateDeterminate;
import com.nononsenseapps.filepicker.ImagePickerActivity;
import java.util.ArrayList;


public class EncryptDecrypt extends AppCompatActivity {

    private Context context;
    final private int ENCRYPT_ACTION = 10;
    final private int DECRYPT_ACTION = 20;
    private String username;

    private ButtonRectangle encryptButton, decryptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_decrypt);

        context = this;

        username = getIntent().getStringExtra("username");

        registerResources();
        registerListeners();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_encrypt_decrypt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        if (id == R.id.action_howProtected) {
            showHowAmIProtectedDialog();
            return true;
        }
        else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("testing", "back button pressed");

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("testing", "result returned");

        if (resultCode == RESULT_OK) {

            ArrayList<FileItem> fileItems = new ArrayList<>();

            if (data.getBooleanExtra(ImagePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    final ClipData clip = data.getClipData();

                    if (clip != null) {

                        for (int i = 0; i < clip.getItemCount(); i++) {
                            final Uri uri = clip.getItemAt(i).getUri();
                            final String path = FileUtils.getPath(context, uri);

                            fileItems.add(new FileItem(path, uri));
                        }
                    }
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra(ImagePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);

                            Log.d("testing", "2Path: " + path);
                            Log.d("testing", "2Uri: " + uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                String path = FileUtils.getPath(this, uri);

                Log.d("testing", "3Path: " + path);
                Log.d("testing", "3Uri: " + uri);
            }

            String title = "";
            if(requestCode == ENCRYPT_ACTION)
                title = "Encrypting";
            else if(requestCode == DECRYPT_ACTION)
                title = "Decrypting";

            final MaterialDialog encryptionProgressDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .title(title)
                    .customView(R.layout.encrypt_decrypt_progress_indicator, false)
                    .show();

            final ProgressBarIndeterminateDeterminate itemProgress = (ProgressBarIndeterminateDeterminate) encryptionProgressDialog.findViewById(R.id.itemProgress);
            final ProgressBarIndeterminateDeterminate overallProgress = (ProgressBarIndeterminateDeterminate) encryptionProgressDialog.findViewById(R.id.overallProgress);
            final TextView overallPercentage = (TextView) encryptionProgressDialog.findViewById(R.id.overallPercentage);
            final TextView itemPercentage = (TextView) encryptionProgressDialog.findViewById(R.id.itemPercentage);
            final TextView itemIndex = (TextView) encryptionProgressDialog.findViewById(R.id.itemIndex);

            switch (requestCode) {

                case ENCRYPT_ACTION:
                    Log.d("testing", "----------about to encrypt-----------");

                    Encrypt.encryptFileItems(context, username, fileItems, new Encrypt.OnProgress() {
                        @Override
                        public void onProgress(int progress, int overall, int itemCount, int currentIndex, boolean done) {
                            if(done)
                            {
                                encryptionProgressDialog.dismiss();
                                Toast.makeText(context, "Encryption Complete", Toast.LENGTH_LONG).show();
                                Log.d("testing", "----------encrypt finished-----------");
                            }
                            else
                            {
                                itemProgress.setProgress(progress);
                                overallProgress.setProgress(overall);

                                itemPercentage.setText(progress + "%");
                                itemIndex.setText(currentIndex + "/" + itemCount);
                                overallPercentage.setText(overall + "%");
                            }
                        }
                    });

                    break;

                case DECRYPT_ACTION:
                    Log.d("testing", "----------about to decrypt-----------");

                    Decrypt.decryptFileItems(context, username, fileItems, new Decrypt.OnProgress() {
                        @Override
                        public void onProgress(int progress, int overall, int itemCount, int currentIndex, boolean done) {
                            if (done) {
                                encryptionProgressDialog.dismiss();
                                Toast.makeText(context, "Decryption Complete", Toast.LENGTH_LONG).show();
                                Log.d("testing", "----------decrypt finished-----------");
                            } else {
                                itemProgress.setProgress(progress);
                                overallProgress.setProgress(overall);

                                itemPercentage.setText(progress + "%");
                                itemIndex.setText(currentIndex + "/" + itemCount);
                                overallPercentage.setText(overall + "%");
                            }
                        }
                    });

                    break;
            }
        }
    }

    private void registerResources()
    {
        encryptButton = (ButtonRectangle) findViewById(R.id.encryptButton);
        decryptButton = (ButtonRectangle) findViewById(R.id.decryptButton);
    }

    private void registerListeners()
    {
        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDirectoryChooser(ENCRYPT_ACTION);
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileDirectoryChooser(DECRYPT_ACTION);
            }
        });
    }

    private void showFileDirectoryChooser(int action)
    {
        Intent intent = new Intent(this, ImagePickerActivity.class);

        intent.putExtra(ImagePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(ImagePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        intent.putExtra(ImagePickerActivity.EXTRA_MODE, ImagePickerActivity.MODE_FILE_AND_DIR);

        intent.putExtra(ImagePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(intent, action);
    }

    private void showHowAmIProtectedDialog()
    {
        String title = getString(R.string.how_am_i_protected_dialog_title);
        String message = getResources().getString(R.string.encryptsy_protection);

        DialogDispatcher.showInformationDialog(this, title, message);
    }

    private void showAboutDialog()
    {
        String title = getString(R.string.about_encryptsy_dialog_title);
        Spanned message = Html.fromHtml(getString(R.string.encryptsy_about));

        DialogDispatcher.showInformationDialog(this, title, message);
    }
}
