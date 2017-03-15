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

import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * Created by JsFish-DT on 2017-03-08.
 */
public class VoiceSearchParams {
    public final String query;
    public boolean isAny;
    public boolean isUnstructured;
    public boolean isGenreFocus;
    public boolean isArtistFocus;
    public boolean isAlbumFocus;
    public boolean isSongFocus;
    public String genre;
    public String artist;
    public String album;
    public String song;

    public VoiceSearchParams(String query, Bundle extras) {
        this.query = query;

        if (TextUtils.isEmpty(query)) {
            isAny = true;
        } else {
            if (extras == null) {
                isUnstructured = true;
            } else {
                String genreKey;

                if (Build.VERSION.SDK_INT >= 21) {
                    genreKey = MediaStore.EXTRA_MEDIA_GENRE;
                } else {
                    genreKey = "android.intent.extra.genre";
                }

                String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);

                if (TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
                    isGenreFocus = true;
                    genre = extras.getString(genreKey);

                    if (TextUtils.isEmpty(genre)) {
                        genre = query;
                    }
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                    isArtistFocus = true;
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                    isAlbumFocus = true;
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)) {
                    isSongFocus = true;
                    song = extras.getString(MediaStore.EXTRA_MEDIA_TITLE);
                    album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                    genre = extras.getString(genreKey);
                    artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                } else {
                    isUnstructured = true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "query = " + query
                + " isAny = " + isAny
                + " isUnstructured = " + isUnstructured
                + " isGenreFocus = " + isGenreFocus
                + " isArtistFocue = " + isArtistFocus
                + " isAlbumFocus = " + isAlbumFocus
                + " isSongFocus = " + isSongFocus
                + " genre = " + genre
                + " artist = " + artist
                + " album = " + album
                + " song = " + song;
    }

}
