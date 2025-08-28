package com.hjhrq1991.library;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BridgeUtil {
    // 批量处理协议
    public static final String YY_BATCH_DATA = "yy://batch/"; // 直接获取数据，可能导致数据量过大的情况无法获取，
    public static final String YY_BATCH_IDS = "yy://batch_ids/"; // 获取id，再通过id获取数据，可支持较大数据量
    public static final String JS_FETCH_MESSAGE_BY_ID = "javascript:WebViewJavascriptBridge._fetchMessageById('%s');"; // 通过id获取数据方法
    public static final String YY_BATCH_RETURN = "yy://batch_return/";

    // 修改原有协议常量
    public static final String YY_RETURN_DATA = "yy://return/"; //格式为   yy://return/{function}/returncontent
    public static final String YY_OVERRIDE_SCHEMA = "yy://bridge/";

    // 新增子协议前缀
    public static final String YY_RETURN_DATA_PREFIX = "yy://return/_fetchQueue/";
    public static final String YY_OVERRIDE_SCHEMA_PREFIX = "yy://bridge/_fetchQueue/";

    final static String YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/";
    final static String EMPTY_STR = "";
    final static String UNDERLINE_STR = "_";
    final static String SPLIT_MARK = "/";

    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    final static String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
    final static String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";
    public final static String JAVASCRIPT_STR = "javascript:";

    public static String parseFunctionName(String jsUrl, String customJs) {
        return jsUrl.replace("javascript:" + customJs + ".", "").replaceAll("\\(.*\\);", "");
    }

    public static String getDataFromReturnUrl(String url) {
        if (url.startsWith(YY_FETCH_QUEUE)) {
            return url.replace(YY_FETCH_QUEUE, EMPTY_STR);
        }

        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);

        if (functionAndData.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < functionAndData.length; i++) {
                sb.append(functionAndData[i]);
            }
            return sb.toString();
        }
        return null;
    }

    public static String getFunctionFromReturnUrl(String url) {
        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 1) {
            return functionAndData[0];
        }
        return null;
    }


    /**
     * js 文件将注入为第一个script引用
     *
     * @param view webview
     * @param url url
     */
    public static void webViewLoadJs(WebView view, String url) {
        String js = "var newscript = document.createElement(\"script\");";
        js += "newscript.src=\"" + url + "\";";
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
        view.loadUrl("javascript:" + js);
    }

    public static void webViewLoadLocalJs(WebView view, String path) {
        String jsContent = assetFile2Str(view.getContext(), path);
        view.loadUrl("javascript:" + jsContent);
    }

    public static void webViewLoadLocalJs(WebView view, String path, String defaultJs, String customJs) {
        String jsContent = assetFile2Str(view.getContext(), path);
        if (!TextUtils.isEmpty(jsContent)) {
            jsContent = jsContent.replaceAll(defaultJs, customJs);

            // 设置是否显示全部日志
            if (BridgeConfig.showAllJSLog) {
                jsContent = jsContent.replaceAll("var showAllLog = false;", "var showAllLog = true;");
            }
        }
        view.loadUrl("javascript:" + jsContent);
    }

    public static String assetFile2Str(Context c, String urlStr) {
        InputStream in = null;
        try {
            in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
