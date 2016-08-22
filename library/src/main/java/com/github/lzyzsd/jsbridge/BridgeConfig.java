package com.github.lzyzsd.jsbridge;

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * @Description:
 */
public class BridgeConfig {

    public static final String toLoadJs = "WebViewJavascriptBridge.js";
    /**
     * 默认桥名
     */
    public static final String defaultJs = "WebViewJavascriptBridge";
    /**
     * 自定义桥名
     */
    public static String customJs = BridgeConfig.defaultJs;
}
