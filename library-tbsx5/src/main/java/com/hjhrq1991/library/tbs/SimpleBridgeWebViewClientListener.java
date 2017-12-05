package com.hjhrq1991.library.tbs;

import android.graphics.Bitmap;
import android.os.*;
import android.os.Message;
import android.view.KeyEvent;

import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;

/**
 * @author hjhrq1991 created at 2017/12/4 17 30 .
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
    public void onLoadResource(WebView webView, String s) {

    }

    @Override
    public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {

    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
        return null;
    }

    @Override
    public void doUpdateVisitedHistory(WebView webView, String s, boolean b) {

    }

    @Override
    public boolean onFormResubmission(WebView webView, Message message, Message message1) {
        return false;
    }

    @Override
    public boolean onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1) {
        return false;
    }

    @Override
    public boolean onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        return false;
    }

    @Override
    public boolean onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
        return false;
    }

    @Override
    public void onScaleChanged(WebView webView, float v, float v1) {

    }

    @Override
    public void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent) {

    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public void onTooManyRedirects(WebView webView, Message message, Message message1) {

    }

    @Override
    public void onReceivedLoginRequest(WebView webView, String s, String s1, String s2) {

    }

    @Override
    public void onDetectedBlankScreen(String s, int i) {

    }
}
