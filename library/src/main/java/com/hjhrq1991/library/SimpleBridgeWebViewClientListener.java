package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
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
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
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
    public boolean onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        return false;
    }

    @Override
    public boolean onFormResubmission(WebView view, Message dontResend, Message resend) {
        return false;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {

    }

    @Override
    public boolean onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        return false;
    }

    @Override
    public boolean onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        return false;
    }

    @Override
    public boolean onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        return false;
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onUnhandledKeyEvent(WebView view, KeyEvent event) {
        return false;
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {

    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {

    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {

    }

    @Override
    public boolean onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        return false;
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {

    }
}
