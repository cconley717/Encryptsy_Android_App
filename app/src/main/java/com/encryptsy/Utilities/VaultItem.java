package com.encryptsy.Utilities;

import java.io.Serializable;

/**
 * Created by Chris on 7/21/2015.
 */
public class VaultItem implements Serializable {

    private String hash;
    private byte[] encryptionKey;
    private byte[] encryptionIV;
    private boolean directory;

    public VaultItem(String hash, byte[] encryptionKey, byte[] encryptionIV, boolean directory)
    {
        this.hash = hash;
        this.encryptionKey = encryptionKey;
        this.encryptionIV = encryptionIV;

        this.directory = directory;
    }

    public byte[] getEncryptionKey() {
        return encryptionKey;
    }

    public byte[] getEncryptionIV() {
        return encryptionIV;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getHash() {
        return hash;
    }
}
