package com.gx.fileexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gx.fileexplorer.bean.FileItemBean;
import com.gx.fileexplorer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxuan on 2016/10/1.
 */
public class FileGridAdapter extends BaseAdapter {
    private List<FileItemBean> fileList;
    private Context mContext;
    private boolean selecting;

    public FileGridAdapter(Context context) {
        mContext = context;
    }

    public void setListAndNotifyDataChanged(List<FileItemBean> list) {
        if (list == null) return;
        if (fileList == null) fileList = new ArrayList<>();
        fileList.clear();
        fileList.addAll(list);

        notifyDataSetChanged();
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    @Override
    public int getCount() {
        return fileList == null ? 0 : fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_grid_file, null);
            viewHolder = new ViewHolder();
            viewHolder.iconIV = (ImageView) view.findViewById(R.id.iv_grid_file);
            viewHolder.selectedIV = (ImageView) view.findViewById(R.id.iv_grid_file_selected);
            viewHolder.unSelectedIV = (ImageView) view.findViewById(R.id.iv_grid_file_unselected);
            viewHolder.nameTV = (TextView) view.findViewById(R.id.tv_gird_file);
            view.setTag(viewHolder);
        } else viewHolder = (ViewHolder) view.getTag();

        FileItemBean itemBean = fileList.get(position);
        viewHolder.iconIV.setBackground(itemBean.getIcon());
        viewHolder.nameTV.setText(itemBean.getFileName());
        if (isSelecting()) {
            viewHolder.unSelectedIV.setVisibility(View.VISIBLE);
        } else viewHolder.unSelectedIV.setVisibility(View.GONE);
        if (isSelecting() && itemBean.isSelected()) {
            viewHolder.selectedIV.setVisibility(View.VISIBLE);
        } else viewHolder.selectedIV.setVisibility(View.GONE);
        return view;
    }

    static class ViewHolder {
        ImageView iconIV, selectedIV, unSelectedIV;
        TextView nameTV;
    }
}
