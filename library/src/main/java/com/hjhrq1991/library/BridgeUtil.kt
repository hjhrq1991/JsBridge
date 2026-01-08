package com.hjhrq1991.library

import android.content.Context
import android.text.TextUtils
import android.webkit.WebView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object BridgeUtil {
    // 批量处理协议
    const val YY_BATCH_DATA = "yy://batch/" // 直接获取数据，可能导致数据量过大的情况无法获取，
    const val YY_BATCH_IDS = "yy://batch_ids/" // 获取id，再通过id获取数据，可支持较大数据量
    const val JS_FETCH_MESSAGE_BY_ID = "javascript:WebViewJavascriptBridge._fetchMessageById('%s');" // 通过id获取数据方法
    const val YY_BATCH_RETURN = "yy://batch_return/"

    // 修改原有协议常量
    const val YY_RETURN_DATA = "yy://return/" //格式为   yy://return/{function}/returncontent
    const val YY_OVERRIDE_SCHEMA = "yy://bridge/"

    // 新增子协议前缀
    const val YY_RETURN_DATA_PREFIX = "yy://return/_fetchQueue/"
    const val YY_OVERRIDE_SCHEMA_PREFIX = "yy://bridge/_fetchQueue/"

    const val YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/"
    const val EMPTY_STR = ""
    const val UNDERLINE_STR = "_"
    const val SPLIT_MARK = "/"

    const val CALLBACK_ID_FORMAT = "JAVA_CB_%s"
    const val JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');"
    const val JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();"
    const val JAVASCRIPT_STR = "javascript:"

    fun parseFunctionName(jsUrl: String?, customJs: String?): String {
        return jsUrl?.replace("javascript:$customJs.", "")?.replace("\\(.*\\);".toRegex(), "") ?: ""
    }

    fun getDataFromReturnUrl(url: String?): String? {
        if (url?.startsWith(YY_FETCH_QUEUE) == true) {
            return url.replace(YY_FETCH_QUEUE, EMPTY_STR)
        }

        val temp = url?.replace(YY_RETURN_DATA, EMPTY_STR) ?: return null
        val functionAndData = temp.split(SPLIT_MARK)

        if (functionAndData.size >= 2) {
            val sb = StringBuilder()
            for (i in 1 until functionAndData.size) {
                sb.append(functionAndData[i])
            }
            return sb.toString()
        }
        return null
    }

    fun getFunctionFromReturnUrl(url: String?): String? {
        val temp = url?.replace(YY_RETURN_DATA, EMPTY_STR) ?: return null
        val functionAndData = temp.split(SPLIT_MARK)
        if (functionAndData.isNotEmpty()) {
            return functionAndData[0]
        }
        return null
    }

    /**
     * js 文件将注入为第一个script引用
     *
     * @param view webview
     * @param url url
     */
    fun webViewLoadJs(view: WebView?, url: String?) {
        var js = "var newscript = document.createElement(\"script\");"
        js += "newscript.src=\"$url\";"
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);"
        view?.loadUrl("javascript:$js")
    }

    fun webViewLoadLocalJs(view: WebView?, path: String?) {
        val jsContent = assetFile2Str(view?.context, path)
        view?.loadUrl("javascript:$jsContent")
    }

    fun webViewLoadLocalJs(view: WebView?, path: String?, defaultJs: String?, customJs: String?) {
        var jsContent = assetFile2Str(view?.context, path)
        if (!TextUtils.isEmpty(jsContent)) {
            jsContent = jsContent?.replace(defaultJs ?: "", customJs ?: "") ?: ""

            // 设置是否显示全部日志
            if (BridgeConfig.showAllJSLog) {
                jsContent = jsContent?.replace("var showAllLog = false;", "var showAllLog = true;") ?: ""
            }
        }
        view?.loadUrl("javascript:$jsContent")
    }

    fun assetFile2Str(c: Context?, urlStr: String?): String? {
        var `in`: InputStream? = null
        return try {
            `in` = c?.assets?.open(urlStr ?: "")
            val bufferedReader = BufferedReader(InputStreamReader(`in`))
            var line: String?
            val sb = StringBuilder()
            do {
                line = bufferedReader.readLine()
                if (line != null && !line.matches("^\\s*\\/\\/.*".toRegex())) {
                    sb.append(line)
                }
            } while (line != null)

            bufferedReader.close()
            `in`?.close()

            sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                }
            }
        }
    }
}
