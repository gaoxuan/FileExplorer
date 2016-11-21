package com.flyfish.fileexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by gaoxuan on 2016/11/21.
 */
public class NavigationFragment extends Fragment implements OnClickListener {
    private LinearLayout settingLL;
    private LinearLayout webLL;
    private LinearLayout mainLocalLL;
    private LinearLayout rootLocalLL;
    private LinearLayout downloadLocalLL;
    private LinearLayout storageLocalLL;
    private LinearLayout sdcardLocalLL;
    private LinearLayout networklLL;
    private LinearLayout bluetoothlLL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_list, container, false);
        settingLL = (LinearLayout) view.findViewById(R.id.ll_nav_setting);
        webLL = (LinearLayout) view.findViewById(R.id.ll_nav_web);
        mainLocalLL = (LinearLayout) view.findViewById(R.id.ll_nav_main);
        rootLocalLL = (LinearLayout) view.findViewById(R.id.ll_nav_root);
        downloadLocalLL = (LinearLayout) view.findViewById(R.id.ll_nav_download);
        storageLocalLL = (LinearLayout) view.findViewById(R.id.ll_nav_storage);
        sdcardLocalLL = (LinearLayout) view.findViewById(R.id.ll_nav_sdcard);
        networklLL = (LinearLayout) view.findViewById(R.id.ll_nav_network);
        bluetoothlLL = (LinearLayout) view.findViewById(R.id.ll_nav_bluetooth);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingLL.setOnClickListener(this);
        webLL.setOnClickListener(this);
        mainLocalLL.setOnClickListener(this);
        rootLocalLL.setOnClickListener(this);
        downloadLocalLL.setOnClickListener(this);
        storageLocalLL.setOnClickListener(this);
        sdcardLocalLL.setOnClickListener(this);
        networklLL.setOnClickListener(this);
        bluetoothlLL.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        listener.onNavItemClick(v.getId());
    }

    private OnNavigationItemClickListener listener;

    public interface OnNavigationItemClickListener {
        void onNavItemClick(int id);
    }

    public void setOnNavigationItemClickListener(OnNavigationItemClickListener listener) {
        this.listener = listener;
    }
}


