/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.classiqo.nativeandroid_32bitz;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.media.MediaRouter;

import com.classiqo.nativeandroid_32bitz.model.MusicProvider;
import com.classiqo.nativeandroid_32bitz.playback.*;
import com.classiqo.nativeandroid_32bitz.ui.NowPlayingActivity;
import com.classiqo.nativeandroid_32bitz.utils.LogHelper;
import com.google.android.gms.cast.framework.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.classiqo.nativeandroid_32bitz.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.classiqo.nativeandroid_32bitz.utils.MediaIDHelper.MEDIA_ID_ROOT;

public class MusicService extends MediaBrowserServiceCompat implements
        PlaybackManager.PlaybackServiceCallback {
    private static final String TAG = LogHelper.makeLogTag(MusicService.class);

    public static final String EXTRA_CONNECTED_CAST = "com.classiqo.nativeandroid_32bitz.CAST_NAME";
    public static final String ACTION_CMD = "com.classiqo.nativeandroid_32bitz.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    private static final int STOP_DELAY = 30000;

    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private Bundle mSessionExtras;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private MediaRouter mMediaRouter;
    private PackageValidator mPackageValidator;
    private SessionManager mCastSessionManager;
    private SessionManagerListener<CastSession> mCastSessionManagerListener;

    private boolean mlsConnectedToCar;
    private BroadcastReceiver mCarConnectionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.d(TAG, "onCreate");

        mMusicProvider = new MusicProvider();
        mMusicProvider.retrieveMediaAsync(null);
        mPackageValidator = new PackageValidator(this);

        QueueManager queueManager = new QueueManager(mMusicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        mPlaybackManager.updatePlaybackState(
                                getString(R.string.error_no_metadata));
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        mPlaybackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                        mSession.setQueue(newQueue);
                        mSession.setQueueTitle(title);
                    }
                });

        LocalPlayback playback = new LocalPlayback(this, mMusicProvider);
        mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager, playback);

        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, NowPlayingActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mSessionExtras = new Bundle();
//        CarHelper.setSlotReservationFlags(mSessionExtras, true, true, true);
//        WearHelper.setSlotReservationFlags(mSessionExtras, true, true);
//        SearHelper.setUseBackgroundFromTheme(mSessionExtras, true);
        mSession.setExtras(mSessionExtras);

        mPlaybackManager.updatePlaybackState(null);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }

//        if (!TvHelper.isTvUiMode(this)) {
//            mCastSessionManager = CastContext.getSharedInstance(this).getSessionManager();
//            mCastSessionManagerListener = new CastSessionManagerListener();
//            mCastSessionManager.addSessionManagerListener(mCastSessionManagerListener, CastSession.class);
//        }

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());

//        registerCarConnectionReceiver();
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);

            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                MediaButtonReceiver.handleIntent(mSession, startIntent);
            }
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.d(TAG, "onDestroy");
//        unregisterCarConnectionReceiver();
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();

        if (mCastSessionManager != null) {
            mCastSessionManager.removeSessionManagerListener(mCastSessionManagerListener, CastSession.class);
        }

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName = " + clientPackageName,
                "; clientUid = " + clientUid + " ; rootHints = ", rootHints);

        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            LogHelper.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                    + "Returning empty browser root so all apps can use MediaController."
                    + clientPackageName);
            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

//        if (CarHelper.isValidCarPackage(clientPackageName)) {
//
//        }
//
//        if (WearHelper.isValidWearCompanionPackage(clientPackageName)) {
//
//        }

        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId = ", parentMediaId);

        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
        } else {
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                }
            });
        }
    }

    @Override
    public void onPlaybackStart() {
        mSession.setActive(true);

        mDelayedStopHandler.removeCallbacksAndMessages(null);

        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onPlaybackStop() {
        mSession.setActive(false);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }

//    private void unregisterCarConnectionReceiver() {
//        IntentFilter filter = new IntentFilter(CarHelper.ACTION_MEDIA_STATUE);
//        mCarConnectionReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String connectionEvent = intent.getStringExtra(CarHelper.MEDIA_CONNECTION_STATUS);
//                mlsConnectedToCar = CarHelper.MEDIA_CONNECTED.equals(connectionEvent);
//
//                LogHelper.i(TAG, "Connection event to Android Auto: ", connectionEvent,
//                        " isConnectedToCar = ", mlsConnectedToCar);
//            }
//        };
//        registerReceiver(mCarConnectionReceiver, filter);
//    }
//
//    private void unregisterCarConnectionReceiver() {
//        unregisterReceiver(mCarConnectionReceiver);
//    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();

            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.");

                    return;
                }
                LogHelper.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

//    private class CastSessionManagerListener implements SessionManagerListener<CastSession> {
//        @Override
//        public void onSessionEnded(CastSession session, int error) {
//            LogHelper.d(TAG, "onSessionEnded");
//            mSessionExtras.remove(EXTRA_CONNECTED_CAST);
//            mSession.setExtras(mSessionExtras);
//            Playback playback = new LocalPlayback(MusicService.this, mMusicProvider);
//            mMediaRouter.setMediaSessionCompat(null);
//            mPlaybackManager.switchToPlayback(playback, false);
//        }
//
//        @Override
//        public void onSessionResumed(CastSession session, boolean wasSuspended) {
//
//        }
//
//        @Override
//        public void onSessionStarted(CastSession castSession, String s) {
//            mSessionExtras.putString(EXTRA_CONNECTED_CAST,
//                    castSession.getCastDevice().getFriendlyName());
//            mSession.setExtras(mSessionExtras);
//            Playback playback = new CastPlayback(mMusicProvider, MusicService.this);
//            mMediaRouter.setMediaSessionCompat(mSession);
//            mPlaybackManager.switchToPlayback(playback, true);
//        }
//
//        @Override
//        public void onSessionStarting(CastSession session) {
//
//        }
//
//        @Override
//        public void onSessionStartFailed(CastSession session, int error) {
//
//        }
//
//        @Override
//        public void onSessionEnding(CastSession session) {
//            mPlaybackManager.getPlayback().updateLastKnownStreamPosition();
//        }
//
//        @Override
//        public void onSessionResuming(CastSession session, String s) {
//
//        }
//
//        @Override
//        public void onSessionResumeFailed(CastSession session, int error) {
//
//        }
//
//        @Override
//        public void onSessionSuspended(CastSession session, int reason) {
//
//        }
//    }

}
