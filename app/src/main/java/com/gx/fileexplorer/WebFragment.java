package com.gx.fileexplorer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by gaoxuan on 2016/11/5.
 */
public class WebFragment extends Fragment {
    private TextView gotoTV;
    private EditText addressET;
    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        gotoTV = (TextView) view.findViewById(R.id.tv_web_goto);
        addressET = (EditText) view.findViewById(R.id.et_web_address);
        webView = (WebView) view.findViewById(R.id.webview);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setListeners();
    }

    private void setListeners() {
        addressET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(addressET.getText().toString())) {
                    gotoTV.setTextColor(getResources().getColor(R.color.search_tv));
                    gotoTV.setEnabled(true);
                } else {
                    gotoTV.setTextColor(getResources().getColor(R.color.search_tv_hint));
                    gotoTV.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addressET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String url = addressET.getText().toString().trim();
                    if (!TextUtils.isEmpty(url)) {
                        browser(url);
                    }
                    return true;
                }
                return false;
            }
        });

        gotoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(addressET.getText().toString()))
                    browser(addressET.getText().toString());
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private void browser(String string) {
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(addressET.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        if (URLUtil.isNetworkUrl(string))
            webView.loadUrl(string);
        else {
            try {
                String key = URLEncoder.encode(string, "gb2312");
                webView.loadUrl("https://www.baidu.com.cn/s?wd=" + key);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
