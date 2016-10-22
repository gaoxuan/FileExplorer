package com.flyfish.fileexplorer;

import android.os.Environment;

/**
 * Created by gaoxuan on 2016/10/1.
 */
public class AppConstants {
    public static final String PREF_NAME = "pref";
    public static final String KEY_PREF_WORKSPACE = "workspace";

    public static final String FILE_TYPE_MUSIC = "music";
    public static final String FILE_TYPE_VIDEO = "video";
    public static final String FILE_TYPE_PICTURE = "picture";
    public static final String FILE_TYPE_DOCUMENT = "document";
    public static final String FILE_TYPE_APPLICATION = "application";

    public static final String PATH_ROOT = "/";
    public static final String PATH_MAIN = Environment.getExternalStorageDirectory().getPath();
    public static final String PATH_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static final String PATH_SDCARD = FileUtils.getExternalSdCardPath();
    public static final String PATH_WORKSPACE = "/";
    public static final String PATH_DOWNLOAD = "/storage/emulated/0/" + Environment.DIRECTORY_DOWNLOADS;
    public static final String PATH_PICTURE = "/storage/emulated/0/" + Environment.DIRECTORY_DCIM;
    public static final String PATH_MUSIC = "/storage/emulated/0/" + Environment.DIRECTORY_MUSIC;
    public static final String PATH_VIDEO = "/storage/emulated/0/" + Environment.DIRECTORY_MOVIES;
    public static final String PATH_DOCUMENT = "/storage/emulated/0/" + Environment.DIRECTORY_DOCUMENTS;
    public static final String PATH_COMPRESS = "/";
    public static final String PATH_APPLICATION = "/";
    public static final String PATH_BLUETOOTH = "/";

    public static final int MSG_COPY_OK = 1;
    public static final int MSG_CUT_OK = 2;

    public static final int COUNT_THREAT_SINGLE = 1;
    public static final int COUNT_THREAT_NORMAL = 3;
    public static final int COUNT_THREAT_MAX = 5;

    public static final String NAME_COPY = "copy";
    public static final String NAME_CUT = "cut";

    public static final String APP_PACKAGE_PDF = "com.infraware.office.link.china";
    public static final String APP_PACKAGE_PNG = "com.sonymobile.sketch";
    public static final String APP_PACKAGE_PNG_WRITE = "com.metamoji.noteanytime";
    public static final String APP_CLASS_PDF = "com.infraware.service.ActLauncher";
    public static final String APP_CLASS_PNG = "com.sonymobile.sketch.dashboard.DashboardActivity";
    public static final String APP_CLASS_PNG_WRITE = "com.metamoji.noteanytime.StartupActivity";
}
