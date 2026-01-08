package com.hjhrq1991.library

import java.util.Collections

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * 配置文件
 */
object BridgeConfig {
    const val TAG = "BridgeWebView"

    var isDebug = false

    var showAllJSLog = false

    const val toLoadJs = "WebViewJavascriptBridge.js"
    /**
     * 默认桥名
     */
    const val defaultBridge = "WebViewJavascriptBridge"
    /**
     * 自定义桥名
     */
    var customBridge: List<String> = Collections.singletonList(defaultBridge)

    /**
     * 需要过滤，不注入js桥的域名
     */
    var filterDomain: List<String>? = null

    /**
     * 消息超时未处理超时时间（有callback）
     */
    var SEMAPHORE_CALLBACK_TIMEOUT_MS: Long = 5 * 1000

    /**
     * 消息超时未处理超时时间（无callback）
     */
    var SEMAPHORE_NO_CALLBACK_TIMEOUT_MS: Long = 5 * 1000

    /**
     * 最大消息处理数
     */
    var MAX_IN_FLIGHT_MESSAGES = 15
}
