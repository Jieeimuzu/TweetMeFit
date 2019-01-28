package com.example.james.tweetmefit;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;

public class encryptions {
    private static String cryptoPass = "tw33tm3fit";

    public String encrypt(String data) {
        if (data == "null") {
            return null;
        }

        String encrypedValue = null;

        Cipher cipher = null;
        DESKeySpec keySpec = null;
        try {
            keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));

            SecretKeyFactory keyFactory = null;
            keyFactory = SecretKeyFactory.getInstance("DES");

            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] clearText = new byte[0];
            clearText = data.getBytes("UTF8");

            // Cipher is not thread safe
            cipher = Cipher.getInstance("DES");

            cipher.init(Cipher.ENCRYPT_MODE, key);


            encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Encrypted: " + data + " -> " + encrypedValue);
        return encrypedValue;
    }

    public static String decrypt(String data) {
        if (data == "null") {
            return null;
        }
        String decrypedValue = null;
        try {
            DESKeySpec keySpec = new DESKeySpec(cryptoPass.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encrypedPwdBytes = Base64.decode(data, Base64.DEFAULT);
            // cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

            decrypedValue = new String(decrypedValueBytes);
            Log.d(TAG, "Decrypted: " + data + " -> " + decrypedValue);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return decrypedValue;
    }

}

