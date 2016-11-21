package com.hjhrq1991.library.tbs;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
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
 * @author hjhrq1991 created at 16/11/21 10 38.
 */
public class WebChromeClientListener implements OnWebChromeClientListener {

    @Override
    public void onReceivedTitle(WebView view, String title) {

    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {

    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return null;
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> valueCallback) {

    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return false;
    }

    @Override
    public boolean onCreateWindow(WebView webView, boolean isDialog, boolean isUserGesture, Message message) {
        return false;
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {

    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback geolocationPermissionsCallback) {

    }

    @Override
    public void onHideCustomView() {

    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        return false;
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        return false;
    }

    @Override
    public boolean onJsTimeout() {
        return false;
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {

    }

    @Override
    public void onReceivedIcon(WebView webView, Bitmap bitmap) {

    }

    @Override
    public void onReceivedTouchIconUrl(WebView webView, String url, boolean precomposed) {

    }

    @Override
    public void onRequestFocus(WebView webView) {

    }

    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {

    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, IX5WebChromeClient.CustomViewCallback customViewCallback) {

    }

    @Override
    public void onCloseWindow(WebView webView) {

    }

    @Override
    public View getVideoLoadingProgressView() {
        return null;
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {

    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        return false;
    }
}
