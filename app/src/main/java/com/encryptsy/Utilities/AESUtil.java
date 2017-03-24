package com.encryptsy.Utilities;


import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.GCMBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;
import org.spongycastle.crypto.params.KeyParameter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;


/**
 * Implementation of AES
 * Bouncy Castle API installed as a library
 * GCM mode for encryption and decryption
 * @author Chris Conley
 */

public class AESUtil {

    public interface OnProgress {
        public void onProgress(long bytesWritten, int progress);
        public void fileFinished();
    }

    private GCMBlockCipher encryptCipher = null;
    private GCMBlockCipher decryptCipher = null;

    private byte[] buf = new byte[1024];
    private byte[] obuf = new byte[1024];

    private byte[] key = null;
    private byte[] IV = null;

    private static int blockSize = 16;

    public AESUtil()
    {
        encryptCipher = new GCMBlockCipher(new AESEngine());
        decryptCipher = new GCMBlockCipher(new AESEngine());
    }

    public AESUtil(byte[] keyBytes, byte[] iv)
    {
        encryptCipher = new GCMBlockCipher(new AESEngine());
        decryptCipher = new GCMBlockCipher(new AESEngine());

        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);

        IV = new byte[blockSize];
        System.arraycopy(iv, 0, IV, 0, iv.length);

        encryptCipher.init(true, new AEADParameters(new KeyParameter(key), 128, IV, new byte[] {}));
        decryptCipher.init(false, new AEADParameters(new KeyParameter(key), 128, IV, new byte[]{}));
    }

    public void setKey(byte[] keyBytes)
    {
        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
    }

    public void setIV(byte[] iv)
    {
        IV = new byte[blockSize];
        System.arraycopy(iv, 0, IV, 0, iv.length);
    }

    public void InitCiphers(byte[] keyBytes, byte[] iv)
    {
        ResetCiphers();

        key = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);

        IV = new byte[blockSize];
        System.arraycopy(iv, 0, IV, 0, iv.length);

        encryptCipher.init(true, new AEADParameters(new KeyParameter(key), 128, IV, new byte[] {}));
        decryptCipher.init(false, new AEADParameters(new KeyParameter(key), 128, IV, new byte[]{}));
    }

    public void ResetCiphers()
    {
        if (encryptCipher != null)
            encryptCipher.reset();

        if (decryptCipher != null)
            decryptCipher.reset();
    }

    public void GCMEncrypt(InputStream in, OutputStream out) throws
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException,
            DataLengthException,
            IllegalStateException,
            InvalidCipherTextException,
            IOException
    {
        int noBytesRead = 0;
        int noBytesProcessed = 0;
        while ((noBytesRead = in.read(buf)) >= 0)
        {
            noBytesProcessed = encryptCipher.processBytes(buf, 0, noBytesRead, obuf, 0);
            out.write(obuf, 0, noBytesProcessed);
        }

        noBytesProcessed = encryptCipher.doFinal(obuf, 0);
        out.write(obuf, 0, noBytesProcessed);

        out.flush();

        in.close();
        out.close();
    }

    public void GCMEncrypt(InputStream in, OutputStream out, long size, OnProgress listener) throws
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException,
            DataLengthException,
            IllegalStateException,
            InvalidCipherTextException,
            IOException
    {
        int noBytesRead = 0;
        int noBytesProcessed = 0;
        long bytesWritten = 0;
        while ((noBytesRead = in.read(buf)) >= 0)
        {
            noBytesProcessed = encryptCipher.processBytes(buf, 0, noBytesRead, obuf, 0);
            out.write(obuf, 0, noBytesProcessed);

            bytesWritten += noBytesProcessed;
            listener.onProgress(noBytesProcessed, (int) (((float) bytesWritten / (float) size) * 100.0f));
        }

        noBytesProcessed = encryptCipher.doFinal(obuf, 0);

        out.write(obuf, 0, noBytesProcessed);
        out.flush();

        in.close();
        out.close();

        listener.fileFinished();
    }

    public void GCMDecrypt(InputStream in, OutputStream out) throws
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException,
            DataLengthException,
            IllegalStateException,
            InvalidCipherTextException,
            IOException
    {
        int noBytesRead = 0;
        int noBytesProcessed = 0;

        while ((noBytesRead = in.read(buf)) >= 0)
        {
            noBytesProcessed = decryptCipher.processBytes(buf, 0, noBytesRead, obuf, 0);
            out.write(obuf, 0, noBytesProcessed);
        }

        noBytesProcessed = decryptCipher.doFinal(obuf, 0);
        out.write(obuf, 0, noBytesProcessed);

        out.flush();

        in.close();
        out.close();
    }

    public void GCMDecrypt(InputStream in, OutputStream out, long size, OnProgress listener) throws
            ShortBufferException,
            IllegalBlockSizeException,
            BadPaddingException,
            DataLengthException,
            IllegalStateException,
            InvalidCipherTextException,
            IOException
    {
        int noBytesRead = 0;
        int noBytesProcessed = 0;
        long bytesWritten = 0;
        while ((noBytesRead = in.read(buf)) >= 0)
        {
            noBytesProcessed = decryptCipher.processBytes(buf, 0, noBytesRead, obuf, 0);
            out.write(obuf, 0, noBytesProcessed);

            bytesWritten += noBytesProcessed;
            listener.onProgress(noBytesProcessed, (int) (((float) bytesWritten / (float) size) * 100.0f));
        }

        noBytesProcessed = decryptCipher.doFinal(obuf, 0);
        out.write(obuf, 0, noBytesProcessed);

        out.flush();

        in.close();
        out.close();
    }

    public byte[] encrypt(byte[] plainText)
    {
        final byte[] encryptedText = new byte[encryptCipher.getOutputSize(plainText.length)];
        final int resp = encryptCipher.processBytes(plainText, 0, plainText.length, encryptedText, 0);

        try
        {
            encryptCipher.doFinal(encryptedText, resp);

            return encryptedText;
        }
        catch (final InvalidCipherTextException e)
        {
            throw new RuntimeException("Unexpected behaviour in crypto libraries", e);
        }
    }

    public byte[] decrypt(byte[] encryptedText)
    {
        final byte[] plainText = new byte[decryptCipher.getOutputSize(encryptedText.length)];
        final int pp = decryptCipher.processBytes(encryptedText, 0, encryptedText.length, plainText, 0);

        try
        {
            decryptCipher.doFinal(plainText, pp);

            return plainText;
        }
        catch (final InvalidCipherTextException e)
        {
            throw new RuntimeException("Unexpected behaviour in crypto libraries", e);
        }
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getIV() {
        return IV;
    }

    public GCMBlockCipher getEncryptCipher() {
        return encryptCipher;
    }

    public GCMBlockCipher getDecryptCipher() {
        return decryptCipher;
    }
}