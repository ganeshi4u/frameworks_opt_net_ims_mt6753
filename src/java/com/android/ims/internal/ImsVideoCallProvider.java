/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
 * limitations under the License
 */

package com.android.ims.internal;

import com.android.internal.os.SomeArgs;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.telecom.Connection;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import android.util.Log;

public abstract class ImsVideoCallProvider {
    private static final int MSG_SET_CALLBACK = 1;
    private static final int MSG_SET_CAMERA = 2;
    private static final int MSG_SET_PREVIEW_SURFACE = 3;
    private static final int MSG_SET_DISPLAY_SURFACE = 4;
    private static final int MSG_SET_DEVICE_ORIENTATION = 5;
    private static final int MSG_SET_ZOOM = 6;
    private static final int MSG_SEND_SESSION_MODIFY_REQUEST = 7;
    private static final int MSG_SEND_SESSION_MODIFY_RESPONSE = 8;
    private static final int MSG_REQUEST_CAMERA_CAPABILITIES = 9;
    private static final int MSG_REQUEST_CALL_DATA_USAGE = 10;
    private static final int MSG_SET_PAUSE_IMAGE = 11;
    /* M: ViLTE part start */
    private static final int MSG_MTK_BASE = 100;
    private static final int MSG_SET_UI_MODE = MSG_MTK_BASE;
    /* M: ViLTE part end */

    private final ImsVideoCallProviderBinder mBinder;

    private IImsVideoCallCallback mCallback;

    private static final String TAG = "ImsVideoCallProvider";

    /**
     * Default handler used to consolidate binder method calls onto a single thread.
     */
    private final Handler mProviderHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "[handleMessage]message: " + msg.what);
            switch (msg.what) {
                case MSG_SET_CALLBACK:
                    mCallback = (IImsVideoCallCallback) msg.obj;
                    break;
                case MSG_SET_CAMERA:
                    onSetCamera((String) msg.obj);
                    break;
                case MSG_SET_PREVIEW_SURFACE:
                    onSetPreviewSurface((Surface) msg.obj);
                    break;
                case MSG_SET_DISPLAY_SURFACE:
                    onSetDisplaySurface((Surface) msg.obj);
                    break;
                case MSG_SET_DEVICE_ORIENTATION:
                    onSetDeviceOrientation(msg.arg1);
                    break;
                case MSG_SET_ZOOM:
                    onSetZoom((Float) msg.obj);
                    break;
                case MSG_SEND_SESSION_MODIFY_REQUEST: {
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        VideoProfile fromProfile = (VideoProfile) args.arg1;
                        VideoProfile toProfile = (VideoProfile) args.arg2;

                        onSendSessionModifyRequest(fromProfile, toProfile);
                    } finally {
                        args.recycle();
                    }
                    break;
                }
                case MSG_SEND_SESSION_MODIFY_RESPONSE:
                    onSendSessionModifyResponse((VideoProfile) msg.obj);
                    break;
                case MSG_REQUEST_CAMERA_CAPABILITIES:
                    onRequestCameraCapabilities();
                    break;
                case MSG_REQUEST_CALL_DATA_USAGE:
                    onRequestCallDataUsage();
                    break;
                case MSG_SET_PAUSE_IMAGE:
                    onSetPauseImage((Uri) msg.obj);
                    break;
                /* M: ViLTE part start */
                case MSG_SET_UI_MODE:
                    onSetUIMode((int) msg.obj);
                    break;
                /* M: ViLTE part end */
                default:
                    break;
            }
        }
    };

    /**
     * IImsVideoCallProvider stub implementation.
     */
    private final class ImsVideoCallProviderBinder extends IImsVideoCallProvider.Stub {
        public void setCallback(IImsVideoCallCallback callback) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setCallback");
            mProviderHandler.obtainMessage(MSG_SET_CALLBACK, callback).sendToTarget();
        }

        public void setCamera(String cameraId) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setCamera");
            mProviderHandler.obtainMessage(MSG_SET_CAMERA, cameraId).sendToTarget();
        }

        public void setPreviewSurface(Surface surface) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setPreviewSurface");
            mProviderHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, surface).sendToTarget();
        }

        public void setDisplaySurface(Surface surface) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setDisplaySurface");
            mProviderHandler.obtainMessage(MSG_SET_DISPLAY_SURFACE, surface).sendToTarget();
        }

        public void setDeviceOrientation(int rotation) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setDeviceOrientation");
            mProviderHandler.obtainMessage(MSG_SET_DEVICE_ORIENTATION, rotation, 0).sendToTarget();
        }

        public void setZoom(float value) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setZoom");
            mProviderHandler.obtainMessage(MSG_SET_ZOOM, value).sendToTarget();
        }

        public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fromProfile;
            args.arg2 = toProfile;
            mProviderHandler.obtainMessage(MSG_SEND_SESSION_MODIFY_REQUEST, args).sendToTarget();
        }

        public void sendSessionModifyResponse(VideoProfile responseProfile) {
            mProviderHandler.obtainMessage(
                    MSG_SEND_SESSION_MODIFY_RESPONSE, responseProfile).sendToTarget();
        }

        public void requestCameraCapabilities() {
            mProviderHandler.obtainMessage(MSG_REQUEST_CAMERA_CAPABILITIES).sendToTarget();
        }

        public void requestCallDataUsage() {
            mProviderHandler.obtainMessage(MSG_REQUEST_CALL_DATA_USAGE).sendToTarget();
        }

        public void setPauseImage(Uri uri) {
            mProviderHandler.obtainMessage(MSG_SET_PAUSE_IMAGE, uri).sendToTarget();
        }

        /* M: ViLTE part start */
        public void setUIMode(int mode) {
            Log.d(TAG, "[ImsVideoCallProviderBinder] setUIMode, mode:" + mode);
            mProviderHandler.obtainMessage(MSG_SET_UI_MODE, mode).sendToTarget();
        }
        /* M: ViLTE part end */
    }

    public ImsVideoCallProvider() {
        mBinder = new ImsVideoCallProviderBinder();
    }

    /**
     * Returns binder object which can be used across IPC methods.
     */
    public final IImsVideoCallProvider getInterface() {
        return mBinder;
    }

    /** @see Connection.VideoProvider#onSetCamera */
    public abstract void onSetCamera(String cameraId);

    /** @see Connection.VideoProvider#onSetPreviewSurface */
    public abstract void onSetPreviewSurface(Surface surface);

    /** @see Connection.VideoProvider#onSetDisplaySurface */
    public abstract void onSetDisplaySurface(Surface surface);

    /** @see Connection.VideoProvider#onSetDeviceOrientation */
    public abstract void onSetDeviceOrientation(int rotation);

    /** @see Connection.VideoProvider#onSetZoom */
    public abstract void onSetZoom(float value);

    /** @see Connection.VideoProvider#onSendSessionModifyRequest */
    public abstract void onSendSessionModifyRequest(VideoProfile fromProfile,
            VideoProfile toProfile);

    /** @see Connection.VideoProvider#onSendSessionModifyResponse */
    public abstract void onSendSessionModifyResponse(VideoProfile responseProfile);

    /** @see Connection.VideoProvider#onRequestCameraCapabilities */
    public abstract void onRequestCameraCapabilities();

    /** @see Connection.VideoProvider#onRequestCallDataUsage */
    public abstract void onRequestCallDataUsage();

    /** @see Connection.VideoProvider#onSetPauseImage */
    public abstract void onSetPauseImage(Uri uri);

    /* M: ViLTE part start */
    /** @see Connection.VideoProvider#onSetUIMode */
    public abstract void onSetUIMode(int mode);
    /* M: ViLTE part end */

    /** @see Connection.VideoProvider#receiveSessionModifyRequest */
    public void receiveSessionModifyRequest(VideoProfile VideoProfile) {
        if (mCallback != null) {
            try {
                mCallback.receiveSessionModifyRequest(VideoProfile);
            } catch (RemoteException ignored) {
            }
        }
    }

    /** @see Connection.VideoProvider#receiveSessionModifyResponse */
    public void receiveSessionModifyResponse(
            int status, VideoProfile requestedProfile, VideoProfile responseProfile) {
        if (mCallback != null) {
            try {
                mCallback.receiveSessionModifyResponse(status, requestedProfile, responseProfile);
            } catch (RemoteException ignored) {
            }
        }
    }

    /** @see Connection.VideoProvider#handleCallSessionEvent */
    public void handleCallSessionEvent(int event) {
        if (mCallback != null) {
            try {
                mCallback.handleCallSessionEvent(event);
            } catch (RemoteException ignored) {
            }
        }
    }

    /** @see Connection.VideoProvider#changePeerDimensions */
    public void changePeerDimensions(int width, int height) {
        if (mCallback != null) {
            try {
                mCallback.changePeerDimensions(width, height);
            } catch (RemoteException ignored) {
            }
        }
    }

    /* M: ViLTE part start */
    /** @see Connection.VideoProvider#changePeerDimensionsWithAngle */
    /* Different from AOSP, additional parameter "rotation" is added. */
    public void changePeerDimensionsWithAngle(int width, int height, int rotation) {
        if (mCallback != null) {
            try {
                mCallback.changePeerDimensionsWithAngle(width, height, rotation);
            } catch (RemoteException ignored) {
            }
        }
    }
    /* M: ViLTE part end */

    /** @see Connection.VideoProvider#changeCallDataUsage */
    public void changeCallDataUsage(long dataUsage) {
        if (mCallback != null) {
            try {
                mCallback.changeCallDataUsage(dataUsage);
            } catch (RemoteException ignored) {
            }
        }
    }

    /** @see Connection.VideoProvider#changeCameraCapabilities */
    public void changeCameraCapabilities(CameraCapabilities CameraCapabilities) {
        if (mCallback != null) {
            try {
                mCallback.changeCameraCapabilities(CameraCapabilities);
            } catch (RemoteException ignored) {
            }
        }
    }

    /** @see Connection.VideoProvider#changeVideoQuality */
    public void changeVideoQuality(int videoQuality) {
        if (mCallback != null) {
            try {
                mCallback.changeVideoQuality(videoQuality);
            } catch (RemoteException ignored) {
            }
        }
    }
}
