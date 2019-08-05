package com.gx.fileexplorer;

import android.graphics.drawable.Drawable;

/**
 * Created by gaoxuan on 2016/10/1.
 */
public class FileItemBean {
    private String fileName;
    private String filePath;
    private Drawable icon;
    private long lastModified;
    private boolean isDirectory;
    private boolean isSelected;

    public FileItemBean(String fileName) {
        this.fileName = fileName;
    }

    public FileItemBean(String fileName, Drawable icon) {
        this.fileName = fileName;
        this.icon = icon;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
