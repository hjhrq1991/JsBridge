package com.hjhrq1991.library

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView

/**
 * @author hjhrq1991 created at 16/11/21 10 38.
 */
open class WebChromeClientListener : OnWebChromeClientListener {
    override fun onReceivedTitle(view: WebView?, title: String?) {
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
    }

    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
    }

    override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
    }

    override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: WebChromeClient.CustomViewCallback?) {
    }

    override fun onHideCustomView() {
    }

    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
        return false
    }

    override fun onRequestFocus(view: WebView?) {
    }

    override fun onCloseWindow(window: WebView?) {
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        return false
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        return false
    }

    override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
        return false
    }

    override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        return false
    }

    override fun onExceededDatabaseQuota(url: String?, databaseIdentifier: String?, quota: Long, estimatedDatabaseSize: Long, totalQuota: Long, quotaUpdater: WebStorage.QuotaUpdater?) {
    }

    override fun onReachedMaxAppCacheSize(requiredStorage: Long, quota: Long, quotaUpdater: WebStorage.QuotaUpdater?) {
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
    }

    override fun onGeolocationPermissionsHidePrompt() {
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
    }

    override fun onJsTimeout(): Boolean {
        return false
    }

    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return false
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        return null
    }

    override fun getVideoLoadingProgressView(): View? {
        return null
    }

    override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
    }

    override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: WebChromeClient.FileChooserParams?): Boolean {
        return false
    }
}
