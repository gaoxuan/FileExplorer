package com.flyfish.fileexplorer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gaoxuan on 2016/10/1.
 */
public class FileGridFragment extends Fragment implements View.OnClickListener {
    private static final String OPERATE_COPY = "operate_copy";
    private static final String OPERATE_CUT = "operate_cut";
    private static final String OPERATE_CANCEL = "operate_cancel";
    private LinearLayout selectLL;
    private LinearLayout operateLL;
    private RelativeLayout tipRL;
    private GridView fileGV;
    private TextView copyTV;
    private TextView cutTV;
    private TextView deleteTV;
    private TextView renameTV;
    private TextView pasteTV;
    private TextView newTV;
    private TextView cancelTV;
    private FileGridAdapter adapter;
    private String currentPath;
    private List<FileItemBean> currentFileList;
    private List<String> operateFileList;
    private List<String> positionList;
    private List<String> pathList;
    private String operate = OPERATE_CANCEL;

    private UIHandler uiHandler;
    private AlertDialog copyDialog;
    private AlertDialog folderDialog;
    private AlertDialog deleteDialog;
    private Client mClient;

    static class UIHandler extends Handler {
        WeakReference<FileGridFragment> fragmentWeakReference;

        UIHandler(FileGridFragment fragment) {
            fragmentWeakReference = new WeakReference<FileGridFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            FileGridFragment fragment = fragmentWeakReference.get();
            if (msg.what == AppConstants.MSG_COPY_OK) {
                Toast.makeText(fragment.getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
                fragment.updateFileList();
                if (fragment.copyDialog != null)
                    fragment.copyDialog.dismiss();
            } else if (msg.what == AppConstants.MSG_CUT_OK) {
                Toast.makeText(fragment.getActivity(), "移动成功", Toast.LENGTH_SHORT).show();
                fragment.updateFileList();
                if (fragment.copyDialog != null)
                    fragment.copyDialog.dismiss();
            }
        }
    }

    private static Comparator<FileItemBean> comparator = new Comparator<FileItemBean>() {
        @Override
        public int compare(FileItemBean f1, FileItemBean f2) {
            if (f1 == null || f2 == null) {
                if (f1 == null)
                    return -1;
                else
                    return 1;
            } else {
                if (f1.isDirectory() == true && f2.isDirectory() == true) {
                    return f1.getFileName().compareToIgnoreCase(f2.getFileName());
                } else {
                    if ((f1.isDirectory() && !f2.isDirectory()) == true) {
                        return -1;
                    } else if ((f2.isDirectory() && !f1.isDirectory()) == true) {
                        return 1;
                    } else {
                        return f1.getFileName().compareToIgnoreCase(f2.getFileName());
                    }
                }
            }
        }
    };

    public static FileGridFragment newInstance() {
        return new FileGridFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new UIHandler(this);
        mClient = Client.newInstance();
        adapter = new FileGridAdapter(getActivity());
        currentPath = AppConstants.PATH_MAIN;
        currentFileList = Utils.readFileListFromPath(getActivity(), currentPath);
        if (currentFileList != null)
            Collections.sort(currentFileList, comparator);
        positionList = new ArrayList<>();
        pathList = new ArrayList<>();
        pathList.add(currentPath);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_grid, null, false);
        selectLL = (LinearLayout) view.findViewById(R.id.ll_file_grid_bottom_select);
        operateLL = (LinearLayout) view.findViewById(R.id.ll_file_grid_bottom_operate);
        tipRL = (RelativeLayout) view.findViewById(R.id.rl_file_grid_none);
        copyTV = (TextView) view.findViewById(R.id.tv_file_copy);
        cutTV = (TextView) view.findViewById(R.id.tv_file_cut);
        deleteTV = (TextView) view.findViewById(R.id.tv_file_delete);
        renameTV = (TextView) view.findViewById(R.id.tv_file_rename);
        pasteTV = (TextView) view.findViewById(R.id.tv_file_operate_paste);
        newTV = (TextView) view.findViewById(R.id.tv_file_operate_new);
        cancelTV = (TextView) view.findViewById(R.id.tv_file_operate_cancel);
        fileGV = (GridView) view.findViewById(R.id.grid_file);
        fileGV.setAdapter(adapter);
        setListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setListAndNotifyDataChanged(currentFileList);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onStateChangeListener = (MainActivity) activity;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_file_copy:
                fileCopy();
                break;
            case R.id.tv_file_cut:
                fileMove();
                break;
            case R.id.tv_file_delete:
                fileDelete();
                break;
            case R.id.tv_file_rename:
                Toast.makeText(getActivity(), "修改名字", Toast.LENGTH_SHORT).show();
                cancelSelect();
                break;
            case R.id.tv_file_operate_cancel:
                cancelSelect();
                break;
            case R.id.tv_file_operate_paste:
                filePaste();
                break;
            case R.id.tv_file_operate_new:
                fileNewFolder();
                break;
        }
    }

    private void fileNewFolder() {
        if (folderDialog == null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_newfolder, null);
            final EditText nameET = (EditText) view.findViewById(R.id.et_dialog_name);
            Selection.setSelection(nameET.getText(), nameET.getText().toString().length());
            folderDialog = new AlertDialog.Builder(getActivity()).create();
            folderDialog.setTitle("新建文件夹");
            folderDialog.setView(view);
            folderDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean result = FileUtils.createNewFolder(currentPath + File.separator + nameET.getText().toString());
                    if (result)
                        updateFileList();
                    else
                        Toast.makeText(getActivity(), "抱歉，操作失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
        folderDialog.show();
    }

    private void filePaste() {
        Task task = null;
        String title = "粘贴";
        switch (operate) {
            case OPERATE_COPY:
                task = new CopyTask(operateFileList, currentPath, uiHandler);
                title = "文件复制";
                break;
            case OPERATE_CUT:
                task = new CutTask(operateFileList, currentPath, uiHandler);
                title = "文件移动";
                break;
        }
        final String taskName = task.getTaskName();
        mClient.submit(task);
        if (copyDialog == null) {
            copyDialog = new AlertDialog.Builder(getActivity()).create();
            copyDialog.setTitle(title);
            copyDialog.setMessage("处理中，请稍后...");
            copyDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mClient.cancel(taskName);
                    copyDialog.dismiss();
                }
            });
        }
        copyDialog.show();
        cancelSelect();
    }

    private void fileCopy() {
        if (checkSelectedFilesExist()) {
            cancelSelect();
            operateLL.setVisibility(View.VISIBLE);
            operate = OPERATE_COPY;
        }
    }

    private void fileMove() {
        if (checkSelectedFilesExist()) {
            cancelSelect();
            operateLL.setVisibility(View.VISIBLE);
            operate = OPERATE_CUT;
        }
    }

    private void fileDelete() {
        if (deleteDialog == null) {
            deleteDialog = new AlertDialog.Builder(getActivity()).create();
            deleteDialog.setTitle("文件删除");
            deleteDialog.setMessage("确定要删除选中的文件吗");
            deleteDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteDialog.dismiss();
                }
            });
            deleteDialog.setButton(DialogInterface.BUTTON_POSITIVE, "删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (positionList.size() > 0) {
                        Iterator<String> it = positionList.iterator();
                        FileItemBean fileItemBean;
                        while (it.hasNext()) {
                            fileItemBean = currentFileList.get(Integer.parseInt(it.next()));
                            Log.i("FileGridFragment", "fileDelete:" + currentPath + File.separator + fileItemBean.getFileName());
                            FileUtils.delete(currentPath + File.separator + fileItemBean.getFileName());
                        }
                        positionList.clear();
                        updateFileList();
                        cancelSelect();
                    }
                    deleteDialog.dismiss();
                }
            });
        }
        deleteDialog.show();
    }

    private boolean checkSelectedFilesExist() {
        if (positionList.size() == 0)
            return false;
        if (operateFileList == null)
            operateFileList = new ArrayList<>();
        else
            operateFileList.clear();
        Iterator<String> it = positionList.iterator();
        String temp;
        while (it.hasNext()) {
            temp = currentFileList.get(Integer.parseInt(it.next())).getFileName();
            Logger.d("checkSelectedFilesExist temp:" + temp + ", name:" + currentPath + File.separator + temp);
            operateFileList.add(currentPath + File.separator + temp);
        }
        positionList.clear();
        return true;
    }

    private void setTVBgColor(boolean prepared) {
        if (prepared) {
            Drawable copy = getResources().getDrawable(R.drawable.ic_filter_1_white_24dp);
            copy.setBounds(0, 0, copy.getMinimumWidth(), copy.getMinimumHeight());
            copyTV.setCompoundDrawables(null, copy, null, null);
            Drawable cut = getResources().getDrawable(R.drawable.ic_content_cut_white_24dp);
            cut.setBounds(0, 0, cut.getMinimumWidth(), cut.getMinimumHeight());
            cutTV.setCompoundDrawables(null, cut, null, null);
            Drawable delete = getResources().getDrawable(R.drawable.ic_delete_white_24dp);
            delete.setBounds(0, 0, delete.getMinimumWidth(), delete.getMinimumHeight());
            deleteTV.setCompoundDrawables(null, delete, null, null);
            Drawable rename = getResources().getDrawable(R.drawable.ic_mode_edit_white_24dp);
            rename.setBounds(0, 0, rename.getMinimumWidth(), rename.getMinimumHeight());
            renameTV.setCompoundDrawables(null, rename, null, null);
        } else {
            Drawable copy = getResources().getDrawable(R.drawable.ic_filter_1_grey_500_24dp);
            copy.setBounds(0, 0, copy.getMinimumWidth(), copy.getMinimumHeight());
            copyTV.setCompoundDrawables(null, copy, null, null);
            Drawable cut = getResources().getDrawable(R.drawable.ic_content_cut_grey_500_24dp);
            cut.setBounds(0, 0, cut.getMinimumWidth(), cut.getMinimumHeight());
            cutTV.setCompoundDrawables(null, cut, null, null);
            Drawable delete = getResources().getDrawable(R.drawable.ic_delete_grey_500_24dp);
            delete.setBounds(0, 0, delete.getMinimumWidth(), delete.getMinimumHeight());
            deleteTV.setCompoundDrawables(null, delete, null, null);
            Drawable rename = getResources().getDrawable(R.drawable.ic_mode_edit_grey_500_24dp);
            rename.setBounds(0, 0, rename.getMinimumWidth(), rename.getMinimumHeight());
            renameTV.setCompoundDrawables(null, rename, null, null);
        }
    }

    private void setOperateTVBgColor(boolean prepared) {
        if (prepared) {
            Drawable paste = getResources().getDrawable(R.drawable.ic_content_copy_white_24dp);
            paste.setBounds(0, 0, paste.getMinimumWidth(), paste.getMinimumHeight());
            pasteTV.setCompoundDrawables(null, paste, null, null);
            pasteTV.setClickable(true);
            Drawable add = getResources().getDrawable(R.drawable.ic_add_white_24dp);
            add.setBounds(0, 0, add.getMinimumWidth(), add.getMinimumHeight());
            newTV.setCompoundDrawables(null, add, null, null);
            newTV.setClickable(true);
        } else {
            Drawable paste = getResources().getDrawable(R.drawable.ic_content_copy_grey_500_24dp);
            paste.setBounds(0, 0, paste.getMinimumWidth(), paste.getMinimumHeight());
            pasteTV.setCompoundDrawables(null, paste, null, null);
            pasteTV.setClickable(false);
            Drawable add = getResources().getDrawable(R.drawable.ic_add_grey_500_24dp);
            add.setBounds(0, 0, add.getMinimumWidth(), add.getMinimumHeight());
            newTV.setCompoundDrawables(null, add, null, null);
            newTV.setClickable(false);
        }
    }


    private void setListeners() {
        fileGV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectLL.setVisibility(View.VISIBLE);
                currentFileList.get(i).setSelected(true);
                positionList.add(String.valueOf(i));
                Logger.d("onItemLongClick i:" + i);
                adapter.setSelecting(true);
                adapter.notifyDataSetChanged();
                if (positionList.size() == 1)
                    setTVBgColor(true);
                return true;
            }
        });
        fileGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapter.isSelecting()) {
                    if (currentFileList.get(i).isSelected()) {
                        currentFileList.get(i).setSelected(false);
                        positionList.remove(String.valueOf(i));
                        if (positionList.size() == 0)
                            setTVBgColor(false);
                    } else {
                        currentFileList.get(i).setSelected(true);
                        positionList.add(String.valueOf(i));
                        Logger.d("onItemClick i:" + i);
                        if (positionList.size() == 1)
                            setTVBgColor(true);
                    }
                    adapter.setListAndNotifyDataChanged(currentFileList);
                } else {
                    String path = currentPath + File.separator + currentFileList.get(i).getFileName();
                    File file = new File(path);
                    if (file.isDirectory()) {
                        pathList.add(path);
                        currentPath = path;
                        onStateChangeListener.onDirChanged(file.getName());
                        updateFileList();
                    } else {
                        openFile(file);
                    }
                }
            }
        });
        copyTV.setOnClickListener(this);
        cutTV.setOnClickListener(this);
        deleteTV.setOnClickListener(this);
        renameTV.setOnClickListener(this);
        pasteTV.setOnClickListener(this);
        newTV.setOnClickListener(this);
        cancelTV.setOnClickListener(this);
    }

    private void updateFileList() {
        if (Utils.readFileListFromPath(getActivity(), currentPath) == null ||
                Utils.readFileListFromPath(getActivity(), currentPath).size() == 0) {
            tipRL.setVisibility(View.VISIBLE);
            fileGV.setVisibility(View.GONE);
        } else {
            tipRL.setVisibility(View.GONE);
            fileGV.setVisibility(View.VISIBLE);
            currentFileList = Utils.readFileListFromPath(getActivity(), currentPath);
            Collections.sort(currentFileList, comparator);
            adapter.setListAndNotifyDataChanged(currentFileList);
        }
    }

    public boolean isSelecting() {
        return adapter.isSelecting();
    }

    public boolean isOperateState() {
        return !operate.equals(OPERATE_CANCEL);
    }

    public void cancelSelectState() {
        cancelSelect();
    }

    private void cancelSelect() {
        operate = OPERATE_CANCEL;
        selectLL.setVisibility(View.GONE);
        operateLL.setVisibility(View.GONE);
        adapter.setSelecting(false);
        adapter.notifyDataSetChanged();
        Iterator<FileItemBean> it = currentFileList.iterator();
        while (it.hasNext()) {
            it.next().setSelected(false);
        }
    }

    private void openFile(final File file) {
        final String fileType = Utils.getFileTypeFromName(file, getActivity());
        if (fileType.toLowerCase().contains("pdf")) {
            if (!Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PDF, getActivity())) {
                alert("北极星Office", AppConstants.APP_PACKAGE_PDF, new Executable<Boolean>() {
                    @Override
                    public void execute(Boolean confirm) {
                        if (!confirm)
                            openFileWithApp(file, fileType);
                    }
                });
                return;
            }
        } else if (fileType.toLowerCase().contains("image")) {
            if (file.getName().toLowerCase().contains(".atdoc")) {
                if (!Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG_WRITE, getActivity())) {
                    alert("MetaMoji", AppConstants.APP_PACKAGE_PNG_WRITE, new Executable<Boolean>() {
                        @Override
                        public void execute(Boolean confirm) {
                            if (!confirm)
                                openFileWithApp(file, fileType);
                        }
                    });
                    return;
                }
            } else if (!Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG, getActivity())) {
                alert("涂画", AppConstants.APP_PACKAGE_PNG, new Executable<Boolean>() {
                    @Override
                    public void execute(Boolean confirm) {
                        if (!confirm)
                            openFileWithApp(file, fileType);
                    }
                });
                return;
            }
        }
        openFileWithApp(file, fileType);
    }

    private void alert(String message, final String packagename, final Executable<Boolean> executor) {
        final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
        alert.setTitle("提示");
        alert.setMessage("是否要用" + message + "打开该文件");
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();
                executor.execute(false);
            }
        });
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("market://details?id=" + packagename);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                }
            }
        });
        alert.show();
    }

    private void openFileWithApp(File file, String fileType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), fileType);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(Intent.createChooser(intent, "选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentPath(String path, String name) {
        currentPath = path;
        updateFileList();
        pathList.add(name);
    }

    public void back2LastPath() {
        pathList.remove(pathList.size() - 1);

        currentPath = pathList.get(pathList.size() - 1);
        currentPath = Utils.getPathFromName(currentPath);
        if (pathList.size() == 1)
            onStateChangeListener.onDirChanged(getString(R.string.nav_local_main));
        else {
            String dir = pathList.get(pathList.size() - 1);
            onStateChangeListener.onDirChanged(dir.substring(dir.lastIndexOf("/") + 1));
        }
        updateFileList();
    }


    public boolean hasOtherDir() {
        return pathList.size() > 1;
    }

    private OnStateChangeListener onStateChangeListener;

    public interface OnStateChangeListener {
        void onSelectChanged(boolean isSelection);

        void onDirChanged(String path);
    }
}