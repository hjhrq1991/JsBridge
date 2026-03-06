package com.hjhrq1991.library;

import java.util.Collections;
import java.util.List;

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * 配置文件
 */
public class BridgeConfig {

    public static final String TAG = "BridgeWebView";

    public static boolean isDebug = false;

    public static boolean showAllJSLog = false;

    public static final String toLoadJs = "WebViewJavascriptBridge.js";
    /**
     * 默认桥名
     */
    public static final String defaultBridge = "WebViewJavascriptBridge";
    /**
     * 自定义桥名
     */
    public static List<String> customBridge = Collections.singletonList(defaultBridge);

    /**
     * 需要过滤，不注入js桥的域名
     */
    public static List<String> filterDomain;

    /**
     * 消息超时未处理超时时间（有callback）
     */
    public static long SEMAPHORE_CALLBACK_TIMEOUT_MS = 5 * 1000;

    /**
     * 消息超时未处理超时时间（无callback）
     */
    public static long SEMAPHORE_NO_CALLBACK_TIMEOUT_MS = 5 * 1000;

    /**
     * 最大消息处理数
     */
    public static int MAX_IN_FLIGHT_MESSAGES = 15;
}
