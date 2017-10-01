package com.example.heavyautometer.igeoscanpreapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;


import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.usb.UVCCameraHandler;

import java.io.File;


/**
 * Created by iGSJinHyung on 2016-08-25.
 */
public class CaptureSoilTask extends AsyncTask<Integer, Void, File> {

    private Context context;
    private UVCCameraHandler mUVCHandler;
    private ImageView dataview_1_img_spot;


    public CaptureSoilTask(Context context, UVCCameraHandler mUVCHandler, ImageView dataview_1_img_spot) {
        this.context = context;
        this.mUVCHandler = mUVCHandler;
        this.dataview_1_img_spot = dataview_1_img_spot;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected File doInBackground(Integer... params) {
        int select_mode = params[0].intValue();
        String fileName = "";

        if(select_mode == 1){
            //파일 덮어쓰기
            fileName = "a.png";
        } else{
            fileName = "b.png";
        }

        final File outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, fileName);
        mUVCHandler.captureStill(outputFile.toString());

        try {
            MediaScannerConnection.scanFile(context, new String[]{ outputFile.toString() }, null, null);
        } catch (final Exception e) {
            Log.e("File-Capture", "MediaScannerConnection#scanFile: ", e);
        }

        return outputFile;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }


    @Override
    protected void onPostExecute(final File outputFile) {
        super.onPostExecute(outputFile);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //타이밍 맞추려면 1초를 딜레이시켜야함.
                    if (outputFile != null) {
                        Bitmap capture = BitmapFactory.decodeFile(outputFile.toString());
                        Bitmap newImage = null;

                        synchronized (this) {
                             newImage = Bitmap.createScaledBitmap(capture, 160, 189, false);
                        }

                        if(newImage != null) dataview_1_img_spot.setImageBitmap(newImage);
                    }

            }
        }, 1000);

    }
}
