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
 * @author hjhrq1991 created at 16/11/21 10 23.
 */
interface OnWebChromeClientListener {
    fun onReceivedTitle(view: WebView?, title: String?)

    fun onProgressChanged(view: WebView?, newProgress: Int)

    fun onReceivedIcon(view: WebView?, icon: Bitmap?)

    fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean)

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)

    fun onShowCustomView(view: View?, requestedOrientation: Int, callback: WebChromeClient.CustomViewCallback?)

    fun onHideCustomView()

    fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean

    fun onRequestFocus(view: WebView?)

    fun onCloseWindow(window: WebView?)

    fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean

    fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean

    fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean

    fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean

    fun onExceededDatabaseQuota(url: String?, databaseIdentifier: String?, quota: Long, estimatedDatabaseSize: Long, totalQuota: Long, quotaUpdater: WebStorage.QuotaUpdater?)

    fun onReachedMaxAppCacheSize(requiredStorage: Long, quota: Long, quotaUpdater: WebStorage.QuotaUpdater?)

    fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?)

    fun onGeolocationPermissionsHidePrompt()

    fun onPermissionRequest(request: PermissionRequest?)

    fun onPermissionRequestCanceled(request: PermissionRequest?)

    fun onJsTimeout(): Boolean

    fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?)

    fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean

    fun getDefaultVideoPoster(): Bitmap?

    fun getVideoLoadingProgressView(): View?

    fun getVisitedHistory(callback: ValueCallback<Array<String>>?)

    fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: WebChromeClient.FileChooserParams?): Boolean
}
