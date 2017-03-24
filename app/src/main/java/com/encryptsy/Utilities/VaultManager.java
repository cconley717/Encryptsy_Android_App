package com.encryptsy.Utilities;

/**
 * Created by Chris on 7/21/2015.
 */

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.encryptsy.SignInSignUp.HTTP_Communications;
import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.HashMap;


/**
 * Created by Chris on 6/18/2015.
 */
public class VaultManager {

    private static String vaultName = null;
    private static Vault vault;
    private static HashMap<String, VaultItem> vaultItems;
    private static byte[] vaultKey;
    private static byte[] vaultIV;


    private static OnVaultManagerRespond onVaultManagerRespond;
    public interface OnVaultManagerRespond {
        public void onVaultManagerRespond(JSONObject returnToken, boolean success, String reason);
    }

    public static void createVault(final Context context, final String username, String password, String decoyPassword, final OnVaultManagerRespond listener)
            throws UnsupportedEncodingException {
        onVaultManagerRespond = listener;

        SecureRandom random = new SecureRandom();

        final byte[] vaultKey = generate256BitKey(random);
        final byte[] vaultIV = generate128BitIV(random);

        final byte[] vaultEncryptionKey = generate256BitKey(random);
        final byte[] vaultEncryptionIV = generate128BitIV(random);

        AESUtil aesUtil = new AESUtil(vaultEncryptionKey, vaultEncryptionIV);

        byte[] encryptedVaultKey = aesUtil.encrypt(vaultKey);
        byte[] encryptedVaultIV = aesUtil.encrypt(vaultIV);

        HTTP_Communications.signUp(context, username, password, decoyPassword, encryptedVaultKey, encryptedVaultIV, new HTTP_Communications.OnServerRespond()
        {
            @Override
            public void onServerRespond(JSONObject returnToken, boolean success, String reason) {
                if (success) {
                    try {
                        Vault vault = new Vault(vaultKey, vaultIV, vaultEncryptionKey, vaultEncryptionIV);

                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir().toString() + File.separator + username + "_Vault")));
                        out.writeObject(vault);
                        out.close();

                        vaultName = username;
                        listener.onVaultManagerRespond(returnToken, true, reason);
                    } catch (IOException e) {
                        Log.d("testing", e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    listener.onVaultManagerRespond(returnToken, false, reason);
                }
            }
        });
    }

    public static void loadVault(final Context context, final String username, String password, final OnVaultManagerRespond listener)
    {
        onVaultManagerRespond = listener;

        HTTP_Communications.signIn(context, username, password, new HTTP_Communications.OnServerRespond() {
            @Override
            public void onServerRespond(JSONObject returnToken, boolean success, String reason) {
                if (success) {
                    try {
                        Log.d("testing", "LOADING VAULT: " + username + "_Vault");

                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(context.getFilesDir().toString() + File.separator + username + "_Vault")));
                        vault = (Vault) input.readObject();
                        input.close();

                        byte[] vaultEncryptionKey = vault.getVaultEncryptionKey();
                        byte[] vaultEncryptionIV = vault.getVaultEncryptionIV();

                        AESUtil aesUtil = new AESUtil(vaultEncryptionKey, vaultEncryptionIV);

                        vaultKey = aesUtil.decrypt(Base64.decode(returnToken.getString("encryptedVaultKey"), Base64.NO_WRAP));
                        vaultIV = aesUtil.decrypt(Base64.decode(returnToken.getString("encryptedVaultIV"), Base64.NO_WRAP));

                        aesUtil = new AESUtil(vaultKey, vaultIV);

                        byte[] unencryptedVaultItems = aesUtil.decrypt(vault.getVaultItems());

                        vaultItems = (HashMap<String, VaultItem>) SerializationUtils.deserialize(unencryptedVaultItems);
                        vaultName = username;

                        Log.d("testing", "size: " + vaultItems.size());

                        listener.onVaultManagerRespond(returnToken, true, reason);
                    } catch (IOException e) {
                        Log.d("testing", e.getMessage());
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        Log.d("testing", e.getMessage());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    listener.onVaultManagerRespond(returnToken, false, reason);
                }
            }
        });
    }

    public static void saveVault(Context context, final String username)
    {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir().toString() + File.separator + username + "_Vault")));
            vault.encryptVault(vaultItems, vaultKey, vaultIV);
            out.writeObject(vault);
            out.close();
        } catch (IOException e) {
            Log.d("testing", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void unloadVault(Context context, final String username, final OnVaultManagerRespond listener)
    {
        onVaultManagerRespond = listener;
    }

    private static byte[] generate256BitKey(SecureRandom random)
    {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return bytes;
    }

    private static byte[] generate128BitIV(SecureRandom random)
    {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);

        return bytes;
    }

    public static HashMap<String, VaultItem> getVaultItems() {
        return vaultItems;
    }

    public static boolean vaultExists(Context context, String username)
    {
        return (new File(context.getFilesDir().toString() + File.separator + username + "_Vault").exists());
    }
}
