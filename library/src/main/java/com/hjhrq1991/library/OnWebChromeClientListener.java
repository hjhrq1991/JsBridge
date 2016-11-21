package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

/**
 * @author hjhrq1991 created at 16/11/21 10 23.
 * @Package com.hjhrq1991.library.tbs
 * @Description:
 */
public interface OnWebChromeClientListener {

    void onReceivedTitle(WebView view, String title);

    void onProgressChanged(WebView view, int newProgress);

    void onReceivedIcon(WebView view, Bitmap icon);

    void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed);

    void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback);

    void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback);

    void onHideCustomView();

    boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg);

    void onRequestFocus(WebView view);

    void onCloseWindow(WebView window);

    boolean onJsAlert(WebView view, String url, String message, JsResult result);

    boolean onJsConfirm(WebView view, String url, String message, JsResult result);

    boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);

    boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result);

    void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater);

    void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater);

    void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback);

    void onGeolocationPermissionsHidePrompt();

    void onPermissionRequest(PermissionRequest request);

    void onPermissionRequestCanceled(PermissionRequest request);

    boolean onJsTimeout();

    void onConsoleMessage(String message, int lineNumber, String sourceID);

    boolean onConsoleMessage(ConsoleMessage consoleMessage);

    Bitmap getDefaultVideoPoster();

    View getVideoLoadingProgressView();

    void getVisitedHistory(ValueCallback<String[]> callback);

    boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);
}
