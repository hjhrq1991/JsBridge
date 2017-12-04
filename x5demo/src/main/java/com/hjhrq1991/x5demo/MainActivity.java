package com.hjhrq1991.x5demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.hjhrq1991.library.tbs.BridgeHandler;
import com.hjhrq1991.library.tbs.CallBackFunction;
import com.hjhrq1991.library.tbs.DefaultHandler;
import com.hjhrq1991.library.tbs.SimpleBridgeWebViewClientListener;
import com.hjhrq1991.library.tbs.TbsBridgeWebView;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

/**
 * @author hjhrq1991 created at 4/28/16 14:33.
 * @Description: jsBridge for tbs_x5 core.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    private String url;

    TbsBridgeWebView webView;

    Button backBtn;
    Button btn1;
    Button btn2;
    Button btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);//（这个对宿主没什么影响，建议声明）
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //=======================使用时请替换成自己url==========================
//        url = "file:///android_asset/demo2.html";
        url = "file:///android_asset/testJavascriptBridge.html";
        initView();
    }

    private void initView() {
        webView = (TbsBridgeWebView) findViewById(R.id.webView);

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
                // TODO Auto-generated method stub
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.loadUrl(url);

        //description：如需使用自定义桥名，调用以下方法即可，
        // 传空或不调用setCustom方法即使用默认桥名。
        // 默认桥名：WebViewJavascriptBridge
        //=======================使用自定义桥名时调用以下代码即可==========================
//        webView.setCustom("桥名");
        webView.setCustom("TestJavascriptBridge");

        //=======================以下4个web调用native示例方法==========================
        webView.registerHandler("initSignNetPay", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "回传结果：" + data);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        webView.registerHandler("initSignNetShare", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "回传结果：" + data);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        webView.registerHandler("jsHandler1", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "回传结果：" + data);
                if (btn1 != null)
                    btn1.setVisibility(View.VISIBLE);
            }
        });

        webView.registerHandler("jsHandler2", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "回传结果：" + data);
                if (btn2 != null)
                    btn2.setVisibility(View.VISIBLE);
            }
        });


        //=======================招行一网通js桥回调==========================
        webView.registerHandler("initCmbSignNetPay", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                //在这里解析回调数据并执行处理
                Log.i(TAG, "回传结果：" + data);
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
                //=======================这里是native调用web==========================
                webView.callHandler("click1", "pic", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "回传结果：" + data);
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn2:
                //=======================这里是native调用web==========================
                webView.callHandler("click2", "success", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        Log.i(TAG, "回传结果：" + data);
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.btn3:
                //=======================这里是native调用web==========================
                webView.callHandler("click3", "success", new CallBackFunction() {
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
