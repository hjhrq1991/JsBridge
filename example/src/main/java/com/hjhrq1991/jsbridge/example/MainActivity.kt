package com.hjhrq1991.jsbridge.example

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import com.hjhrq1991.jsbridge.example.databinding.ActivityMainBinding
import com.hjhrq1991.library.BridgeHandler
import com.hjhrq1991.library.BridgeWebView
import com.hjhrq1991.library.CallBackFunction
import com.hjhrq1991.library.DefaultHandler
import com.hjhrq1991.library.SimpleBridgeWebViewClientListener

/**
 * @author hjhrq1991 created at 4/28/16 14:33.
 * @Description: js桥demo实例
 */
class MainActivity : Activity(), View.OnClickListener {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private var url: String? = null

    private val webView: BridgeWebView
        get() = binding.webView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //=======================使用时请替换成自己url==========================
        //        url = "file:///android_asset/demo2.html";
        url = "file:///android_asset/testJavascriptBridge.html"
        initView()
    }

    private fun initView() {
        // 设置为预加载模式
        webView.isPreloadMode = true

        webView.settings.javaScriptEnabled = true
        webView.settings.saveFormData = false
        webView.settings.savePassword = false
        webView.settings.setSupportZoom(true)
        webView.settings.domStorageEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.loadWithOverviewMode = true
        //设定支持h5viewport
        webView.settings.useWideViewPort = true
        // 自适应屏幕.
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        //        mWwebViewebView.settings.userAgentString = Constant.useragent

        binding.back.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btnInit.setOnClickListener(this)

        //=======================js桥使用改方法替换原有setWebViewClient()方法==========================
        webView.setBridgeWebViewClientListener(object : SimpleBridgeWebViewClientListener() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, bitmap: Bitmap?) {
                binding.btn1.visibility = View.GONE
                binding.btn2.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?): Boolean {
                val message = when (error?.primaryError) {
                    SslError.SSL_UNTRUSTED -> "证书颁发机构不受信任"
                    SslError.SSL_EXPIRED -> "证书过期"
                    SslError.SSL_IDMISMATCH -> "网站名称与证书不一致"
                    SslError.SSL_NOTYETVALID -> "证书无效"
                    SslError.SSL_DATE_INVALID -> "证书日期无效"
                    SslError.SSL_INVALID -> "证书错误"
                    else -> "证书错误"
                }
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("提示")
                    .setMessage("$message，是否继续")
                    .setCancelable(true)
                    .setPositiveButton("确认") { _: DialogInterface, _: Int ->
                        handler?.proceed()
                    }
                    .setNegativeButton("取消") { _: DialogInterface, _: Int ->
                        handler?.cancel()
                    }
                val alert = builder.create()
                alert.show()

                return true
            }
        })
        //=======================此方法必须调用==========================
        webView.setDefaultHandler(DefaultHandler())
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }
        }

        url?.let { webView.loadUrl(it) }

        //description：如需使用自定义桥名，调用以下方法即可，
        // 传空或不调用setBridge方法即使用默认桥名。
        // 默认桥名：WebViewJavascriptBridge
        //=======================使用自定义桥名时调用以下代码即可==========================
        //        webView.setBridge("桥名");
        webView.setBridge("WebBridge", "TestJavascriptBridge", "AppBridge")

        //=======================以下web调用app示例方法==========================
        webView.registerHandler("jsClick1", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.i(TAG, "console  jsClick1  回传结果：$data")
                Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
            }
        })

        webView.registerHandler("jsClick2", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.i(TAG, "console  jsClick2  回传结果：$data")
                Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
            }
        })

        webView.registerHandler("jsCall1", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.i(TAG, "console   jsCall1  回传结果：$data")
                binding.btn1.visibility = View.VISIBLE
            }
        })

        webView.registerHandler("jsCall2", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.i(TAG, "回传结果：$data")
                binding.btn2.visibility = View.VISIBLE
            }
        })

        //AppBridge桥的方法
        webView.registerHandler("jsBridge2Click", object : BridgeHandler {
            override fun handler(data: String?, function: CallBackFunction?) {
                Log.i(TAG, "console  该桥未初始化，不执行发送消息到 桥2也收到消息了  回传结果：$data")
                Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
            R.id.btn1 -> {
                //=======================这里是app调用web==========================
                webView.callHandler("appClick1", "pic", object : CallBackFunction {
                    override fun onCallBack(data: String?) {
                        Log.i(TAG, "console   appClick1   回传结果：$data")
                        Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
                    }
                })
                webView.callHandler("AppBridgeClick1", "pic", object : CallBackFunction {
                    override fun onCallBack(data: String?) {
                        Log.i(TAG, "console   appClick1   回传结果：$data")
                        Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.btn2 -> {
                //=======================这里是app调用web==========================
                webView.callHandler("appClick2", "success", object : CallBackFunction {
                    override fun onCallBack(data: String?) {
                        Log.i(TAG, "console   appClick2   回传结果：$data")
                        Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.btn3 -> {
                //=======================这里是app调用web==========================
                webView.callHandler("appClick3", "success", object : CallBackFunction {
                    override fun onCallBack(data: String?) {
                        Log.i(TAG, "回传结果：$data")
                        Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.btn_init -> {
                webView.isPreloadMode = false
                webView.initJSBridge()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }
}
