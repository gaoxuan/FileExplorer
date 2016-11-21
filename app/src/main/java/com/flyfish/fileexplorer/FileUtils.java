package com.flyfish.fileexplorer;

import android.os.Environment;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gaoxuan on 2016/10/2.
 */
public class FileUtils {

    public static String readFile(String filePath) {
        String fileContent = "";
        File file = new File(filePath);
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file));
            reader = new BufferedReader(is);
            String line = null;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                fileContent += line + " ";
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileContent;
    }

    private static ArrayList<String> getDevMountList() {
        String[] toSearch = FileUtils.readFile("/system/etc/vold.fstab").split(" ");
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }

    public static String getExternalSdCardPath() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdCardFile.getAbsolutePath();
        }

        String path = null;

        File sdCardFile = null;

        ArrayList<String> devMountList = getDevMountList();

        for (String devMount : devMountList) {
            File file = new File(devMount);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                } else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }

        return null;
    }

    public static void copy(String oldPath, String newPath) {
        File file = new File(oldPath);
        if (file.isDirectory())
            copyFolder(oldPath, newPath);
        else
            copyFile(oldPath, newPath);
    }

    private static void copyFile(String oldPath, String newPath) {
        Logger.d("FileUtils copyFile oldPath:" + oldPath + ", newPath:" + newPath);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            File oldFile = new File(oldPath);
            File newFile = new File(newPath + File.separator + oldFile.getName());
            if (!newFile.exists())
                newFile.createNewFile();
            inputStream = new FileInputStream(oldFile);
            outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            Logger.d("FileUtil", "error in copyFile()");
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFolder(String oldPath, String newPath) {
        try {
            File oldFile = new File(oldPath);
            File newFile = new File(newPath + File.separator + oldFile.getName());
            if (!newFile.exists())
                newFile.mkdirs();
            for (File temp : oldFile.listFiles()) {
                if (temp.isDirectory()) {
                    copyFolder(temp.getAbsolutePath(), newPath + File.separator + temp.getName());
                } else {
                    copyFile(oldPath, newPath);
                }
            }
        } catch (Exception e) {
            Logger.d("FileUtil", "error in copyFolder()");
            e.printStackTrace();
        }
    }

    public static void move(String oldPath, String newPath) {
        Logger.d("TAG move ", "old path:" + oldPath + ", new path:" + newPath);
        new File(oldPath).renameTo(new File(newPath));
    }

    public static void delete(String path) {
        File file = new File(path);
        if (file.isDirectory())
            deleteFolder(file);
        else
            deleteFile(file);
    }

    public static void rename() {

    }

    private static boolean deleteFile(File file) {
        boolean result = false;
        if (file != null) {
            try {
                File file2 = file;
                file2.delete();
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    private static boolean deleteFolder(File folder) {
        boolean result = false;
        try {
            String childs[] = folder.list();
            if (childs == null || childs.length <= 0) {
                if (folder.delete()) {
                    result = true;
                }
            } else {
                for (int i = 0; i < childs.length; i++) {
                    String childName = childs[i];
                    String childPath = folder.getPath() + File.separator + childName;
                    File filePath = new File(childPath);
                    if (filePath.exists() && filePath.isFile()) {
                        if (filePath.delete()) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    } else if (filePath.exists() && filePath.isDirectory()) {
                        if (deleteFolder(filePath)) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    }
                }
                folder.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public static boolean createNewFolder(String path) {
        Logger.i("TAG createNewFolder path:" + path);
        File dirFile = new File(path);
        try {
            if (!dirFile.exists()) {
                boolean result = dirFile.mkdirs();
                if (result)
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createNewFile(String path, String name) {
        Logger.i("TAG createNewFolder path:" + path);
        File file = new File(path, name);
        try {
            if (!file.exists())
                file.createNewFile();
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
