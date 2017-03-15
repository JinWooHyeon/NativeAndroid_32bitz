package com.classiqo.nativeandroid_32bitz.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.classiqo.nativeandroid_32bitz.utils.LogHelper;

/**
 * Created by JsFish-DT on 2017-03-14.
 */
public class NowPlayingActivity extends Activity{
    private static final String TAG = LogHelper.makeLogTag(NowPlayingActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "onCreate");
        Intent newIntent;
        UiModeManager uiModeManager = (UiModeManager)getSystemService(UI_MODE_SERVICE);

        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            LogHelper.d(TAG, "Running on a TV device");
//            newIntent = new Intent(this, TvPlaybackActivity.class);
            newIntent = new Intent(this, MusicPlayerActivity.class);
        } else {
            LogHelper.d(TAG, "Running on a non_TV_Device");
            newIntent = new Intent(this, MusicPlayerActivity.class);
        }
        startActivity(newIntent);
        finish();
    }
}
