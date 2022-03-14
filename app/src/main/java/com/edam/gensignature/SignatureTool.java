package com.edam.gensignature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignatureTool {
    private static final String TAG = "LeeJiEun";
    private static final String SIGNATUREMD5 = "MD5";
    private static final int HEX_CODE = 0xFF;

    public Signature[] getSignatures(Context context, String packageName) {
        if (null == packageName || packageName.length() == 0) {
            return null;
        }
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                SigningInfo signingInfo = packageInfo.signingInfo;
                return signingInfo.getApkContentsSigners();
            }else {
                PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (null != info) {
                    Log.d(TAG, info.signatures.toString());
                    return info.signatures;
                }
            }
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.toString()); // android.content.pm.PackageManager$NameNotFoundException: com.edam.gen signature
            return null;
        }
    }

    public static String getSignatureString(byte[] signaturesByteArray) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(SIGNATUREMD5);
        messageDigest.update(signaturesByteArray);
        return toHexString(messageDigest.digest());
    }

    public static String toHexString(byte[] signaturesByteArray) {
        if (null == signaturesByteArray) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(2 * signaturesByteArray.length);
        for (int i = 0; ; i++) {
            if (i >= signaturesByteArray.length) {
                return stringBuilder.toString();
            }
            String str = Integer.toString(HEX_CODE & signaturesByteArray[i], 16);
            if (str.length() == 1) {
                str = "0" + str;
            }
            stringBuilder.append(str);
        }
    }
}