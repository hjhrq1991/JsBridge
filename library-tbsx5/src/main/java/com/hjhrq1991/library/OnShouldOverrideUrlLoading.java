package com.hjhrq1991.library;

import android.graphics.Bitmap;

import com.tencent.smtt.sdk.WebView;

/**
 * @author hjhrq1991 created at 5/10/16 15:12.
 *         超链接回调
 */
public interface OnShouldOverrideUrlLoading {

    /**
     * @param view webview
     * @param url  url
     * @return boolean
     * 非js桥的超链接回调回去自行处理
     * @author hjhrq1991 created at 5/10/16 15:12.
     */
    boolean onShouldOverrideUrlLoading(WebView view, String url);

    void onPageFinished(WebView view, String url);

    void onPageStarted(WebView view, String url, Bitmap bitmap);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

}
