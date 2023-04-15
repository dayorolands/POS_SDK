package com.dspread.qpos.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by dsppc11 on 2019/3/21.
 */

public class FileUtils {
    public static byte[] readAssetsLine(String fileName, Context context) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            android.content.ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            byte[] data = new byte[512];
            int current = 0;
            while ((current = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return buffer.toByteArray();
    }
}
