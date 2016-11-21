package com.hjhrq1991.library.tbs;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;

/**
 * @author hjhrq1991 created at 16/11/21 10 23.
 *
 */
public interface OnWebChromeClientListener {

    void onReceivedTitle(WebView view, String title);

    void onProgressChanged(WebView view, int newProgress);

    void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater);

    Bitmap getDefaultVideoPoster();

    void getVisitedHistory(ValueCallback<String[]> valueCallback);

    boolean onConsoleMessage(ConsoleMessage consoleMessage);

    boolean onCreateWindow(WebView webView, boolean isDialog, boolean isUserGesture, android.os.Message message);

    void onGeolocationPermissionsHidePrompt();

    void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback geolocationPermissionsCallback);

    void onHideCustomView();

    boolean onJsAlert(WebView view, String url, String message, JsResult result);

    boolean onJsConfirm(WebView view, String url, String message, JsResult result);

    boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);

    boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result);

    boolean onJsTimeout();

    void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater);

    void onReceivedIcon(WebView webView, Bitmap bitmap);

    void onReceivedTouchIconUrl(WebView webView, String url, boolean precomposed);

    void onRequestFocus(WebView webView);

    void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback);

    void onShowCustomView(View view, int requestedOrientation, IX5WebChromeClient.CustomViewCallback customViewCallback);

    void onCloseWindow(WebView webView);

    View getVideoLoadingProgressView();

    void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1);

    boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams);
}
