package com.flyfish.fileexplorer;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FileGridFragment.OnStateChangeListener {
    private static final String[] FRAGMENT_TAG = new String[]{"WEB", "MAIN", "ROOT", "DOWNLOAD", "STORAGE", "SDCARD"};
    private static final int REQUEST_CODE_ASK_EXTERNAL_STORAGE = 1;

    private DrawerLayout mDrawerLayout;
    private TextView mCategoryTV;
    private FragmentManager fragmentManager;
    private ArrayList<Fragment> fragmentList;
    private WebFragment webFragment;
    private ArrayList<String> indexList;
    private int currentIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initFragments(savedInstanceState);
        initPath2NameMap();

        requestNecessaryPermission();
    }

    private void requestNecessaryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readPreference();
    }

    private void readPreference() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sortTime = settings.getString(AppConstants.PREF_LIST_SORT_TIME, "0");
        String sortName = settings.getString(AppConstants.PREF_LIST_SORT_NAME, "1");
        String sortSize = settings.getString(AppConstants.PREF_LIST_SORT_SIZE, "1");
        Comparator<FileItemBean> comparator = getComparatorFromPref(sortTime, sortName, sortSize);

        String column = settings.getString(AppConstants.PREF_LIST_ICON_SIZE, "1");
        int columnNum = 4;
        switch (column) {
            case "1":
                columnNum = 4;
                break;
            case "0":
                columnNum = 5;
                break;
            case "-1":
                columnNum = 6;
                break;
        }

        boolean showHideFile = settings.getBoolean(AppConstants.PREF_SWITCHER_ICON_SIZE, true);

        for (int i = 1; i < fragmentList.size(); i++) {
            ((FileGridFragment) fragmentList.get(i)).setComparator(comparator);
            ((FileGridFragment) fragmentList.get(i)).setColumnNum(columnNum);
            ((FileGridFragment) fragmentList.get(i)).showHideFiles(showHideFile);
        }
    }

    private Comparator<FileItemBean> getComparatorFromPref(final String sortTime, final String sortName, String sortSize) {
        return new Comparator<FileItemBean>() {
            @Override
            public int compare(FileItemBean f1, FileItemBean f2) {
                if (f1 == null || f2 == null) {
                    if (f1 == null)
                        return -1;
                    else
                        return 1;
                } else {
                    if (f1.isDirectory() && f2.isDirectory()) {
                        long compare;
                        if (sortTime.equals("1")) {
                            compare = f1.getLastModified() - f2.getLastModified();
                        } else {
                            compare = f2.getLastModified() - f1.getLastModified();
                        }
                        if (compare == 0) {
                            if (sortName.equals("1"))
                                return f1.getFileName().compareToIgnoreCase(f2.getFileName());
                            else
                                return f2.getFileName().compareToIgnoreCase(f1.getFileName());
                        } else
                            return compare < 0 ? 1 : -1;
                    } else {
                        if (f1.isDirectory() && !f2.isDirectory()) {
                            return -1;
                        } else if (f2.isDirectory() && !f1.isDirectory()) {
                            return 1;
                        } else {
                            long compare;
                            if (sortTime.equals("1")) {
                                compare = f1.getLastModified() - f2.getLastModified();
                            } else {
                                compare = f2.getLastModified() - f1.getLastModified();
                            }
                            if (compare == 0) {
                                if (sortName.equals("1"))
                                    return f1.getFileName().compareToIgnoreCase(f2.getFileName());
                                else
                                    return f2.getFileName().compareToIgnoreCase(f1.getFileName());
                            } else
                                return compare < 0 ? 1 : -1;
                        }
                    }
                }
            }
        };
    }

    private void initPath2NameMap() {
        HashMap hashMap = new HashMap();
        hashMap.put(getResources().getString(R.string.nav_local_main), AppConstants.PATH_MAIN);
        hashMap.put(getResources().getString(R.string.nav_local_root), AppConstants.PATH_ROOT);
        hashMap.put(getResources().getString(R.string.nav_local_storage), AppConstants.PATH_STORAGE);
        hashMap.put(getResources().getString(R.string.nav_local_sdcard), AppConstants.PATH_SDCARD);
        hashMap.put(getResources().getString(R.string.nav_local_download), AppConstants.PATH_DOWNLOAD);
        Utils.initPath2NameMap(hashMap);
    }

    private void initFragments(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentList = new ArrayList<>();
            indexList = new ArrayList<>();
            webFragment = new WebFragment();
            FileGridFragment mainFragment = FileGridFragment.newInstance(null, AppConstants.PATH_MAIN);
            FileGridFragment rootFragment = FileGridFragment.newInstance(null, AppConstants.PATH_ROOT);
            FileGridFragment downloadFragment = FileGridFragment.newInstance(null, AppConstants.PATH_DOWNLOAD);
            FileGridFragment storageFragment = FileGridFragment.newInstance(null, AppConstants.PATH_STORAGE);
            FileGridFragment sdcardFragment = FileGridFragment.newInstance(null, AppConstants.PATH_SDCARD);

            fragmentList.add(webFragment);
            fragmentList.add(mainFragment);
            fragmentList.add(rootFragment);
            fragmentList.add(downloadFragment);
            fragmentList.add(storageFragment);
            fragmentList.add(sdcardFragment);
            fragmentManager.beginTransaction().add(R.id.contentFrame, webFragment, FRAGMENT_TAG[0])
                    .add(R.id.contentFrame, mainFragment, FRAGMENT_TAG[1]).add(R.id.contentFrame, rootFragment, FRAGMENT_TAG[2])
                    .add(R.id.contentFrame, downloadFragment, FRAGMENT_TAG[3]).add(R.id.contentFrame, storageFragment, FRAGMENT_TAG[4])
                    .add(R.id.contentFrame, sdcardFragment, FRAGMENT_TAG[5]).commitAllowingStateLoss();
            showCurrentFragmentByIndex(1);
            indexList.add(String.valueOf(1));
        } else {
            //TODO findFragmentByTag

        }
        //        fileGridFragment =
//                (FileGridFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
//        if (fileGridFragment == null) {
//            // Create the fragment
//            fileGridFragment = FileGridFragment.newInstance(null);
//            Utils.addFragmentToActivity(
//                    getSupportFragmentManager(), fileGridFragment, R.id.contentFrame);
//        }
    }

    /**
     * 根据下标值显示Fragment
     *
     * @param indexID [0, FRAGMENT_TAG.length)
     */
    private void showCurrentFragmentByIndex(int indexID) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentList.size(); i++) {
            if (i == indexID)
                transaction.show(fragmentList.get(i));
            else
                transaction.hide(fragmentList.get(i));
        }
        transaction.commitAllowingStateLoss();
    }

    private void initViews() {
        mCategoryTV = (TextView) findViewById(R.id.tv_main_category);

        initToolBar();
        initDrawer();
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mDrawerLayout.closeDrawers();
        NavigationFragment fragment = (NavigationFragment) getSupportFragmentManager().findFragmentById(R.id.nav_view);
        fragment.setOnNavigationItemClickListener(new NavigationFragment.OnNavigationItemClickListener() {
            @Override
            public void onNavItemClick(int id) {
                String title = null;
                switch (id) {
                    case R.id.ll_nav_setting:
                        title = getResources().getString(R.string.nav_setting);
                        Intent intent = new Intent(MainActivity.this, SimpleSettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.ll_nav_web:
                        title = getResources().getString(R.string.nav_web);
                        if (currentIndex == 0) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 0;
                        break;
                    case R.id.ll_nav_main:
                        title = getResources().getString(R.string.nav_local_main);
                        if (currentIndex == 1) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 1;
                        break;
                    case R.id.ll_nav_root:
                        title = getResources().getString(R.string.nav_local_root);
                        if (currentIndex == 2) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 2;
                        break;
                    case R.id.ll_nav_download:
                        title = getResources().getString(R.string.nav_local_download);
                        if (currentIndex == 3) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 3;
                        break;
                    case R.id.ll_nav_storage:
                        title = getResources().getString(R.string.nav_local_storage);
                        if (currentIndex == 4) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 4;
                        break;
                    case R.id.ll_nav_sdcard:
                        title = getResources().getString(R.string.nav_local_sdcard);
                        if (currentIndex == 5) {
                            mDrawerLayout.closeDrawers();
                        }
                        currentIndex = 5;
                        break;
                    case R.id.ll_nav_network:

                        break;
                    case R.id.ll_nav_bluetooth:

                        break;
                }
                indexList.add(String.valueOf(currentIndex));
                showCurrentFragmentByIndex(currentIndex);
                mDrawerLayout.closeDrawers();
                mCategoryTV.setText(title);
            }
        });
    }

    private void initToolBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_format_list_bulleted_white_24dp)); //设置三个点为别的图标
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void openAPP(String packageName, String className) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        ComponentName componentName = new ComponentName(packageName, className);
        intent.setComponent(componentName);
        startActivity(intent);
    }

    private void alert(String message, final String packagename) {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("提示");
        alert.setMessage("尚未安装" + message + "，是否下载");
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_ASK_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("该应用使用需要“写入文件权限”，请到 “应用信息 -> 权限” 中授予！");
                    builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        SubMenu subMenu = menu.addSubMenu("");
        subMenu.add(getResources().getString(R.string.action_pdf)).setIcon(R.mipmap.ic_action_pdf).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PDF, MainActivity.this))
                    openAPP(AppConstants.APP_PACKAGE_PDF, AppConstants.APP_CLASS_PDF);
                else
                    alert("北极星Office", AppConstants.APP_PACKAGE_PDF);
                return true;
            }
        });
        subMenu.add(getResources().getString(R.string.action_png)).setIcon(R.mipmap.ic_action_png).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG, MainActivity.this))
                    openAPP(AppConstants.APP_PACKAGE_PNG, AppConstants.APP_CLASS_PNG);
                else
                    alert("涂画", AppConstants.APP_PACKAGE_PNG);
                return true;
            }
        });
        subMenu.add(getResources().getString(R.string.action_png_write)).setIcon(R.mipmap.ic_action_png_write).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG_WRITE, MainActivity.this))
                    openAPP(AppConstants.APP_PACKAGE_PNG_WRITE, AppConstants.APP_CLASS_PNG_WRITE);
                else
                    alert("MetaMoji", AppConstants.APP_PACKAGE_PNG_WRITE);
                return true;
            }
        });
        MenuItem item = subMenu.getItem();
        item.setIcon(R.drawable.ic_menu);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Logger.i("MainActivity onBackPressed:" + "currentIndex:" + indexList.get(indexList.size() - 1) + ", indexSize:" + indexList.size());

        if (mDrawerLayout.isDrawerOpen(Gravity.START))
            mDrawerLayout.closeDrawers();
        else if (currentIndex == 0) {
            indexList.remove(indexList.size() - 1);
            currentIndex = Integer.parseInt(indexList.get(indexList.size() - 1));
            showCurrentFragmentByIndex(currentIndex);
        } else {
            FileGridFragment fileGridFragment = (FileGridFragment) fragmentList.get(Integer.parseInt(indexList.get(indexList.size() - 1)));
            if (fileGridFragment.isSelecting())
                fileGridFragment.cancelSelectState();
            else if (fileGridFragment.hasOtherDir())
                fileGridFragment.back2LastPath();
            else if (fileGridFragment.isOperateState())
                fileGridFragment.cancelSelectState();
            else if (indexList.size() > 1) {
                indexList.remove(indexList.size() - 1);
                currentIndex = Integer.parseInt(indexList.get(indexList.size() - 1));
                showCurrentFragmentByIndex(currentIndex);
            } else
                super.onBackPressed();
        }
    }

    @Override
    public void onSelectChanged(boolean isSelection) {
    }

    @Override
    public void onDirChanged(String path) {
        mCategoryTV.setText(path);
    }

}
