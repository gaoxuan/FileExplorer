package com.gx.fileexplorer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;

import com.gx.fileexplorer.AppConstants;
import com.gx.fileexplorer.bean.FileItemBean;
import com.gx.fileexplorer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gaoxuan on 2016/10/1.
 */
public class Utils {
    private static HashMap<String, String> path2NameMap = new HashMap<>();

    public static String getWorkspaceDir(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        String result = sharedPreferences.getString(AppConstants.KEY_PREF_WORKSPACE, "");
        return result;
    }

    public static void setWorkspaceDir(Context context, String path) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConstants.KEY_PREF_WORKSPACE, path);
        editor.apply();
    }

    public static void initPath2NameMap(HashMap hashMap) {
        path2NameMap = hashMap;
    }

    public static String getPathFromName(String name) {
        if (path2NameMap.containsKey(name))
            return path2NameMap.get(name);
        return name;
    }

    public static List<FileItemBean> readFileListFromPath(Context context, String path, boolean showHideFile) {
        List<FileItemBean> resultList = new ArrayList<>();
        File file = new File(path);
        File[] source = file.listFiles();
        if (source == null) return null;
        for (File temp : source) {
            FileItemBean fileItemBean = new FileItemBean(temp.getName());
            fileItemBean.setLastModified(temp.lastModified());
            fileItemBean.setFilePath(temp.getAbsolutePath());
            if (fileItemBean.getFileName().startsWith(".") && !showHideFile)
                continue;
            if (temp.isDirectory()) {
                fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_folder));
                fileItemBean.setDirectory(true);
            } else {
                fileItemBean.setDirectory(false);
                if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_music)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_music));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_video)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_media));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_picture)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_picture));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_document)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_document));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_compress)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_compress));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_application)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_app));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_word)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_word));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_excel)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_excel));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_ppt)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_ppt));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_flash)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_flash));
                else if (checkEndWidthCurrentType(fileItemBean.getFileName(), context.getResources().getStringArray(R.array.type_html)))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_html));
                else if (fileItemBean.getFileName().toLowerCase().endsWith(".txt"))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_text));
                else if (fileItemBean.getFileName().toLowerCase().endsWith(".pdf"))
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_pdf));
                else
                    fileItemBean.setIcon(context.getResources().getDrawable(R.drawable.format_unkown));
            }
            resultList.add(fileItemBean);
        }
        return resultList;
    }

    public static String getFileTypeFromName(File file, Context context) {
        String fileType = "file/*";
        if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_pdf)))
            fileType = "application/pdf";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_picture)))
            fileType = "image/*";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_ppt)))
            fileType = "application/vnd.ms-powerpoint";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_video)))
            fileType = "video/*";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_music)))
            fileType = "audio/*";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_application)))
            fileType = "application/vnd.android.package-archive";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_document)))
            fileType = "text/plain";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_excel)))
            fileType = "application/vnd.ms-excel";
        else if (checkEndWidthCurrentType(file.getName(), context.getResources().getStringArray(R.array.type_word)))
            fileType = "application/msword";

        return fileType;
    }

    public static boolean checkEndWidthCurrentType(String filename, String[] regex) {
        for (String type : regex) {
            if (filename.endsWith(type))
                return true;
        }
        return false;
    }

    public static void addFragmentToActivity(FragmentManager fragmentManager, Fragment fragment, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commitAllowingStateLoss();
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static boolean checkHasMatchApp(String packageName, Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        if (packages != null) {
            for (int i = 0; i < packages.size(); i++) {
                String pn = packages.get(i).packageName;
                if (pn.equals(packageName))
                    return true;
            }
        }
        return false;
    }

    public static int dp2px(int dpSize, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, context.getResources().getDisplayMetrics());
    }

    public static void addIndex(ArrayList<String> indexList, int currentIndex) {
        if (indexList == null) return;
        if (indexList.contains(String.valueOf(currentIndex))) {
            for (int index = 0; index < indexList.size(); index++) {
                if (indexList.get(index).equals(String.valueOf(currentIndex))) {
                    indexList.remove(index);
                    break;
                }
            }
        }
        indexList.add(String.valueOf(currentIndex));
    }

    public static void removeIndex(ArrayList<String> indexList, int currentIndex) {
        if (!indexList.contains(String.valueOf(currentIndex))) return;
        if (indexList.contains(String.valueOf(currentIndex))) {
            for (int index = 0; index < indexList.size(); index++) {
                if (indexList.get(index).equals(String.valueOf(currentIndex))) {
                    indexList.remove(index);
                    return;
                }
            }
        }
    }
}
