package com.hjhrq1991.jsbridge.example;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.hjhrq1991.library.BridgeHandler;
import com.hjhrq1991.library.BridgeWebView;
import com.hjhrq1991.library.CallBackFunction;
import com.hjhrq1991.library.DefaultHandler;
import com.hjhrq1991.library.SimpleBridgeWebViewClientListener;

/**
 * @author hjhrq1991 created at 4/28/16 14:33.
 * @Description: js桥demo实例
 */
public class MainActivity extends Activity implements OnClickListener {

    private final String TAG = "MainActivity";

    private String url;

    BridgeWebView webView;

    Button backBtn;
    Button btn1;
    Button btn2;
    Button btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //=======================使用时请替换成自己url==========================
//        url = "file:///android_asset/demo2.html";
        url = "file:///android_asset/testJavascriptBridge.html";
        initView();
    }

    private void initView() {
        webView = (BridgeWebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        //设定支持h5viewport
        webView.getSettings().setUseWideViewPort(true);
        // 自适应屏幕.
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        mWwebViewebView.getSettings().setUserAgentString(Constant.useragent);

        backBtn = (Button) findViewById(R.id.back);
        backBtn.setOnClickListener(this);

        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);

        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(this);

        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(this);

        //=======================js桥使用改方法替换原有setWebViewClient()方法==========================
        webView.setBridgeWebViewClientListener(new SimpleBridgeWebViewClientListener() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "超链接：" + url);
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i(TAG, "超链接：" + url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap bitmap) {
                if (btn1 != null) {
                    btn1.setVisibility(View.GONE);
                }

                if (btn2 != null) {
                    btn2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

            @Override
            public boolean onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                String message;
                switch (error.getPrimaryError()) {
                    case android.net.http.SslError.SSL_UNTRUSTED:
                        message = "证书颁发机构不受信任";
                        break;
                    case android.net.http.SslError.SSL_EXPIRED:
                        message = "证书过期";
                        break;
                    case android.net.http.SslError.SSL_IDMISMATCH:
                        message = "网站名称与证书不一致";
                        break;
                    case android.net.http.SslError.SSL_NOTYETVALID:
                        message = "证书无效";
                        break;
                    case android.net.http.SslError.SSL_DATE_INVALID:
                        message = "证书日期无效";
                        break;
                    case android.net.http.SslError.SSL_INVALID:
                    default:
                        message = "证书错误";
                        break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示").setMessage(message + "，是否继续").setCancelable(true)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.proceed();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
        //=======================此方法必须调用==========================
        webView.setDefaultHandler(new DefaultHandler());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.loadUrl(url);

        //description：如需使用自定义桥名，调用以下方法即可，
        // 传空或不调用setBridge方法即使用默认桥名。
        // 默认桥名：WebViewJavascriptBridge
        //=======================使用自定义桥名时调用以下代码即可==========================
//        webView.setBridge("桥名");
        webView.setBridge("WebBridge", "TestJavascriptBridge");

        //=======================以下web调用app示例方法==========================
        webView.registerHandler("jsClick1", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "console  jsClick1  回传结果：" + data);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        webView.registerHandler("jsClick2", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "console  jsClick2  回传结果：" + data);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        webView.registerHandler("jsCall1", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "console   jsCall1  回传结果：" + data);
                if (btn1 != null)
                    btn1.setVisibility(View.VISIBLE);
            }
        });

        webView.registerHandler("jsCall2", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "回传结果：" + data);
                if (btn2 != null)
                    btn2.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (webView.canGoBack())
                    webView.goBack();
                else
                    finish();
                break;
            case R.id.btn1:
                //=======================这里是app调用web==========================
                webView.callHandler("appClick1", "pic", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "console   appClick1   回传结果：" + data);
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn2:
                //=======================这里是app调用web==========================
                webView.callHandler("appClick2", "success", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "console   appClick2   回传结果：" + data);
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn3:
                //=======================这里是app调用web==========================
                webView.callHandler("appClick3", "success", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "回传结果：" + data);
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            finish();
    }
}
