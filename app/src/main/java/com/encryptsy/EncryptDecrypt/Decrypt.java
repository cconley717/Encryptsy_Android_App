package com.encryptsy.EncryptDecrypt;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import com.encryptsy.Utilities.AESUtil;
import com.encryptsy.Utilities.FileItem;
import com.encryptsy.Utilities.VaultItem;
import com.encryptsy.Utilities.VaultManager;
import org.spongycastle.crypto.InvalidCipherTextException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

/**
 * Created by Chris on 7/22/2015.
 */
public class Decrypt {

    public interface OnProgress {
        public void onProgress(int progress, int overallProgress, int itemCount, int currentItem, boolean done);
    }

    private static boolean debug = false;
    private static AESUtil aesUtil;
    private static long jobSize, jobSizeCompleted;
    private static int itemCount, currentItem;


    public static void decryptFileItems(final Context context, final String username, final ArrayList<FileItem> fileItems, final OnProgress listener)
    {
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                jobSize = 0;
                jobSizeCompleted = 0;
                itemCount = 0;
                currentItem = 0;
            }

            @Override
            protected Void doInBackground(Void... params) {
                aesUtil = new AESUtil();

                File currentFile;
                for(int i = 0; i < fileItems.size(); i++)
                {
                    currentFile = new File(fileItems.get(i).getPath());
                    if(currentFile.isDirectory())
                        jobSize = directorySize(currentFile);
                    else {
                        jobSize += new File(fileItems.get(i).getPath()).length();
                        itemCount++;
                    }
                }

                if(debug)
                    Log.d("testing", "Job Size: " + jobSize);

                for(int i = 0; i < fileItems.size(); i++)
                    decryptFileItem(fileItems.get(i).getPath());

                //VaultManager.saveVault(context, username);

                return null;
            }

            private void decryptFileItem(final String source) {
                try {
                    if(debug)
                        Log.d("testing", "decrypting an item");

                    File inputFile = new File(source);
                    if(inputFile.isDirectory())
                    {
                        if(debug)
                            Log.d("testing", "directory detected, traversing..");

                        String[] subNote = inputFile.list();
                        for (String filename : subNote) {
                            decryptFileItem(inputFile.getAbsolutePath() + "/" + filename);
                        }

                        return;
                    }

                    currentItem++;

                    if(!isFileItemInVault(source)) {
                        if(debug)
                            Log.d("testing", "item is not in vault");
                        return;
                    }
                    if(debug)
                        Log.d("testing", "calculating hash");
                    FileInputStream fileInputStream = new FileInputStream(inputFile);
                    String hash = calculateHash(fileInputStream);
                    byte[] decryptionKey = VaultManager.getVaultItems().get(hash).getEncryptionKey();
                    byte[] decryptionIV = VaultManager.getVaultItems().get(hash).getEncryptionIV();

                    if(debug)
                        Log.d("testing", "creating input and output file streams");
                    fileInputStream = new FileInputStream(inputFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(context.getFilesDir().getAbsolutePath() + "/" + "DEC");

                    if(debug)
                        Log.d("testing", "decrypting");
                    aesUtil.InitCiphers(decryptionKey, decryptionIV);
                    aesUtil.GCMDecrypt(fileInputStream, fileOutputStream, inputFile.length(), new AESUtil.OnProgress() {
                        @Override
                        public void onProgress(long bytesWritten, int progress) {
                            jobSizeCompleted += bytesWritten;

                            //Log.d("testing", "aesUtil overall: " + (int)(((float)jobSizeCompleted / (float)jobSize) * 100.0f));
                            //Log.d("testing", "jobSizeCompleted: " + jobSizeCompleted);
                            //Log.d("testing", "jobSize: " + jobSize);

                            publishProgress(progress, (int) (((float) jobSizeCompleted / (float) jobSize) * 100.0f));

                            //Log.d("testing", "bytesWritten: " + bytesWritten);
                            //Log.d("testing", "progress: " + String.valueOf(progress));
                        }

                        @Override
                        public void fileFinished() {

                        }
                    });
                    if(debug)
                        Log.d("testing", "decrypted");
                    fileInputStream.close();
                    fileOutputStream.close();

                    if(debug) {
                        Log.d("testing", "removing item from vault");
                        Log.d("testing", "pre after decryption: " + VaultManager.getVaultItems().size());
                    }

                    //VaultManager.getVaultItems().remove(hash);

                    if(debug)
                        Log.d("testing", "pre after decryption: " + VaultManager.getVaultItems().size());

                    deleteSource(context, source);

                    copyFile(context.getFilesDir().getAbsolutePath() + "/" + "DEC", source);

                    if(debug)
                        Log.d("testing", "<file copied>");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (ShortBufferException e) {
                    e.printStackTrace();
                } catch (InvalidCipherTextException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            protected void onProgressUpdate(Integer... progress)
            {
                super.onProgressUpdate(progress);

                listener.onProgress(progress[0], progress[1], itemCount, currentItem, false);
            }

            @Override
            protected void onPostExecute(Void v) {
                listener.onProgress(100, 100, itemCount, currentItem, true);
            }
        }.execute(null, null, null);
    }

    public static long directorySize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
                itemCount++;
            }
            else if(file.isDirectory())
                length += directorySize(file);
        }
        return length;
    }

    private static boolean isFileItemInVault(String source) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(new File(source));
        String hash = calculateHash(fileInputStream);

        if(VaultManager.getVaultItems().containsKey(hash))
            return true;
        else
            return false;
    }

    private static String calculateHash(InputStream inputStream)
    {
        int read;
        byte[] buffer = new byte[8192];

        try {
            //MessageDigest digest = MessageDigest.getInstance("SHA-1", "BC");
            //MessageDigest digest = MessageDigest.getInstance("SHA-512", "BC");
            MessageDigest digest = MessageDigest.getInstance("MD5", "SC");
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();

            return Base64.encodeToString(hash, Base64.NO_WRAP);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copyFile(String source, String destination) throws IOException {

        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        if (!sourceFile.exists()) {
            return;
        }

        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        FileChannel destinationChannel = new FileOutputStream(destinationFile).getChannel();

        if (destinationChannel != null && sourceChannel != null) {
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        if (sourceChannel != null) {
            sourceChannel.close();
        }
        if (destinationChannel != null) {
            destinationChannel.close();
        }
    }

    private static void deleteSource(Context context, String source)
    {
        Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ", new String[]{source}, null);
        Uri imageUri = Uri.parse("content://media/external/images/media");

        Cursor videoCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID}, MediaStore.Video.Media.DATA + "=? ", new String[]{source}, null);
        Uri videoUri = Uri.parse("content://media/external/video/media");

        Cursor audioCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DATA + "=? ", new String[]{source}, null);
        Uri audioUri = Uri.parse("content://media/external/audio/media");

        Cursor fileCursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                null, MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE, null, null);
        Uri fileUri = MediaStore.Files.getContentUri("external");


        ArrayList<Uri> uriArrayList = new ArrayList<>();

        if (imageCursor != null && imageCursor.moveToFirst()) {
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            uriArrayList.add(Uri.withAppendedPath(imageUri, "" + id));
        }
        if (videoCursor != null && videoCursor.moveToFirst()) {
            int id = videoCursor.getInt(videoCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            uriArrayList.add(Uri.withAppendedPath(videoUri, "" + id));
        }
        if (audioCursor != null && audioCursor.moveToFirst()) {
            int id = audioCursor.getInt(audioCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            uriArrayList.add(Uri.withAppendedPath(audioUri, "" + id));
        }
        if (fileCursor != null && fileCursor.moveToFirst()) {
            int id = fileCursor.getInt(fileCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            uriArrayList.add(Uri.withAppendedPath(fileUri, "" + id));
        }

        for(int i = 0; i < uriArrayList.size(); i++) {
            context.getContentResolver().delete(uriArrayList.get(i), null, null);
        }
    }
}
