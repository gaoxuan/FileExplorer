package com.gx.fileexplorer;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuan on 2016/10/24.
 */
public class PathHorizontalScrollView extends HorizontalScrollView implements View.OnClickListener {
    private static final int ITEM_WIDTH = 60;
    private LinearLayout mContainer;
    private Context mContext;
    private int itemWidth;
    private int childViewCount;
    private Map<View, Integer> viewPositionMap = new HashMap<>();
    private Handler handler;

    public PathHorizontalScrollView(Context context) {
        this(context, null);
    }

    public PathHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        handler = new Handler();
        initAttributes(attrs);
        initContainers(context);
    }

    private void initAttributes(AttributeSet attrs) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.PathHorizontalScrollView);
        int width = array.getInt(R.styleable.PathHorizontalScrollView_itemWidth, ITEM_WIDTH);
        itemWidth = Utils.dp2px(width, mContext);
        array.recycle();
    }

    private void initContainers(Context context) {
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        mContainer.setGravity(Gravity.CENTER_VERTICAL);
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mContainer);
        ImageView imageView = new ImageView(mContext);
        int padding = Utils.dp2px(10, mContext);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setImageResource(R.drawable.history_rootdirectory);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setOnClickListener(this);
        mContainer.addView(imageView);
        viewPositionMap.put(imageView, childViewCount++);
    }

    public void initStartPath() {
        childViewCount = 1;
        int count = mContainer.getChildCount();
        if (count > 1)
            for (int index = 1; index < count; index++) {
                mContainer.removeViewAt(1);
            }
    }

    public void addView(String message) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.ic_keyboard_arrow_right_grey_500_24dp);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Utils.dp2px(20, mContext), Utils.dp2px(20, mContext));
        imageView.setLayoutParams(layoutParams);
        mContainer.addView(imageView);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setText(message);
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(12);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(this);
        mContainer.addView(textView);
        viewPositionMap.put(textView, childViewCount++);
        calculateScrollBar();
    }

    /**
     * Maybe Fragment will call it
     *
     * @param position
     */
    public void removeView(int position) {
        if (position == childViewCount - 1)
            return;
        /*
            current view state(container has 11 child):
            / > path1 > path2 > path3 > path4 > path5
            if position = 2
                container.remove(> path3 > path4(5,6,7,8,9,10))
            after current view state(container has 5 child):
            / > path1 > path2
         */
        childViewCount = position + 1;
        int start = position * 2 + 1;
        int delta = 0;
        int childCount = mContainer.getChildCount();
        for (int index = start; index < childCount; index++) {
            mContainer.removeViewAt(index - delta);
            delta++;
        }
        calculateScrollBar();
    }

    private void calculateScrollBar() {
        final int width = getMeasuredWidth();
        final int childWidth = itemWidth * childViewCount;
        final int divideWidth = Utils.dp2px(20, mContext) * (childViewCount - 1);

        if (childWidth + divideWidth > width) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    scrollBy(childWidth + divideWidth - width, 0);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onItemClick(viewPositionMap.get(v));
        removeView(viewPositionMap.get(v));
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
