package com.flyfish.fileexplorer;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FileGridFragment.OnStateChangeListener {

    private DrawerLayout mDrawerLayout;
    private TextView mCategoryTV;
    private FileGridFragment fileGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initFragment();
        initPath2NameMap();
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

    private void initFragment() {
        fileGridFragment =
                (FileGridFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fileGridFragment == null) {
            // Create the fragment
            fileGridFragment = FileGridFragment.newInstance();
            Utils.addFragmentToActivity(
                    getSupportFragmentManager(), fileGridFragment, R.id.contentFrame);
        }
    }

    private void initViews() {
        mCategoryTV = (TextView) findViewById(R.id.tv_main_category);

        initToolBar();
        initDrawer();
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    private void initToolBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_format_list_bulleted_white_24dp)); //设置三个点为别的图标
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_pdf:
                        if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PDF, MainActivity.this))
                            openAPP(AppConstants.APP_PACKAGE_PDF, AppConstants.APP_CLASS_PDF);
                        else
                            alert("北极星Office", AppConstants.APP_PACKAGE_PDF);
                        break;
                    case R.id.action_png:
                        if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG, MainActivity.this))
                            openAPP(AppConstants.APP_PACKAGE_PNG, AppConstants.APP_CLASS_PNG);
                        else
                            alert("涂画", AppConstants.APP_PACKAGE_PNG);
                        break;
                    case R.id.action_png_write:
                        if (Utils.checkHasMatchApp(AppConstants.APP_PACKAGE_PNG_WRITE, MainActivity.this))
                            openAPP(AppConstants.APP_PACKAGE_PNG_WRITE, AppConstants.APP_CLASS_PNG_WRITE);
                        else
                            alert("MetaMoji", AppConstants.APP_PACKAGE_PNG_WRITE);
                        break;
                }
                return true;
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.main_local_navigation_menu_item:
                                fileGridFragment.setCurrentPath(AppConstants.PATH_MAIN, menuItem.getTitle().toString());
                                break;
                            case R.id.root_local_navigation_menu_item:
                                fileGridFragment.setCurrentPath(AppConstants.PATH_ROOT, menuItem.getTitle().toString());
                                break;
                            case R.id.download_local_navigation_menu_item:
                                fileGridFragment.setCurrentPath(AppConstants.PATH_DOWNLOAD, menuItem.getTitle().toString());
                                break;
                            case R.id.storage_local_navigation_menu_item:
                                fileGridFragment.setCurrentPath(AppConstants.PATH_STORAGE, menuItem.getTitle().toString());
                                break;
                            case R.id.sdcard_local_navigation_menu_item:
                                fileGridFragment.setCurrentPath(AppConstants.PATH_SDCARD, menuItem.getTitle().toString());
                                break;

//                            case R.id.picture_repository_navigation_menu_item:
//                                fileGridFragment.setCurrentPath(AppConstants.PATH_PICTURE);
//                                break;
//                            case R.id.music_repository_navigation_menu_item:
//                                fileGridFragment.setCurrentPath(AppConstants.PATH_MUSIC);
//                                break;
//                            case R.id.video_repository_navigation_menu_item:
//                                fileGridFragment.setCurrentPath(AppConstants.PATH_VIDEO);
//                                break;
//                            case R.id.document_repository_navigation_menu_item:
//                                fileGridFragment.setCurrentPath(AppConstants.PATH_DOCUMENT);
//                                break;
//                            case R.id.application_repository_navigation_menu_item:
//                                break;
//                            case R.id.compress_repository_navigation_menu_item:
//                                break;
                            case R.id.bluetooth_network_navigation_menu_item:

                                break;
                            case R.id.network_network_navigation_menu_item:

                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
//                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        mCategoryTV.setText(menuItem.getTitle());
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (fileGridFragment.isSelecting())
            fileGridFragment.cancelSelectState();
        else if (fileGridFragment.hasOtherDir())
            fileGridFragment.back2LastPath();
        else if (fileGridFragment.isOperateState())
            fileGridFragment.cancelSelectState();
        else super.onBackPressed();
    }

    @Override
    public void onSelectChanged(boolean isSelection) {
    }

    @Override
    public void onDirChanged(String path) {
        mCategoryTV.setText(path);
    }
}
