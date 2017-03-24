package com.encryptsy.Utilities;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Chris on 7/21/2015.
 */
public class Vault implements Serializable {
    private byte[] vaultEncryptionKey;
    private byte[] vaultEncryptionIV;
    private byte[] vaultItems;

    public Vault(byte[] vaultKey, byte[] vaultIV, byte[] vaultEncryptionKey, byte[] vaultEncryptionIV)
    {
        this.vaultEncryptionKey = vaultEncryptionKey;
        this.vaultEncryptionIV = vaultEncryptionIV;

        createEncryptedVault(vaultKey, vaultIV);
    }

    private void createEncryptedVault(byte[] vaultKey, byte[] vaultIV)
    {
        HashMap<String, VaultItem> unencryptedVaultItems = new HashMap<>();

        byte[] data = SerializationUtils.serialize(unencryptedVaultItems);
        AESUtil aesUtil = new AESUtil(vaultKey, vaultIV);

        this.vaultItems = aesUtil.encrypt(data);
    }

    public void encryptVault(HashMap<String, VaultItem> vaultItems, byte[] vaultKey, byte[] vaultIV)
    {
        byte[] data = SerializationUtils.serialize(vaultItems);
        AESUtil aesUtil = new AESUtil(vaultKey, vaultIV);

        this.vaultItems = aesUtil.encrypt(data);
    }

    public byte[] getVaultEncryptionKey() {
        return vaultEncryptionKey;
    }

    public byte[] getVaultEncryptionIV() {
        return vaultEncryptionIV;
    }

    public byte[] getVaultItems() {
        return vaultItems;
    }
}
