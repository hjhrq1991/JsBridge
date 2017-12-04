package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * @author hjhrq1991 created at 2017/12/4 17 42 .
 *         description:
 */

public class SimpleBridgeWebViewClientListener implements BridgeWebViewClientListener {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onLoadResource(WebView view, String url) {

    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return null;
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {

    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {

    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {

    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return false;
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {

    }

    @Override
    public void onUnhandledInputEvent(WebView view, InputEvent event) {

    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {

    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {

    }
}
