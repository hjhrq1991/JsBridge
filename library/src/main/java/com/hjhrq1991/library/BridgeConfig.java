package com.hjhrq1991.library;

import java.util.Collections;
import java.util.List;

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * 配置文件
 */
public class BridgeConfig {

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
}
