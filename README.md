本项目共有两个部分：
1.基于系统的JsBridge；
2.基于Tbs X5内核的JsBridge；

# 一、JsBridge
基于https://github.com/lzyzsd/JsBridge 优化改进而来的Android JsBridge

### 优化
1.支持自定义桥名；<br/>2.修复web页未渲染即进行跳转导致Js桥初始化失败的问题；

### 使用

添加maven依赖

```maven
<dependency>
  <groupId>com.hjhrq1991.library</groupId>
  <artifactId>jsbridge</artifactId>
  <version>1.0.6</version>
  <type>pom</type>
</dependency>
```

添加gradle依赖

```gradle
compile 'com.hjhrq1991.library:jsbridge:1.0.6'
```

在你的布局上添加BridgeWebView

```xml
<com.hjhrq1991.library.BridgeWebView
 android:id="@+id/webView"
 android:layout_width="match_parent"
 android:layout_height="match_parent" />
```

### 使用默认桥名或自定义桥名

```java
//description：如需使用自定义桥名，调用以下方法即可，
// 传空或不调用setCustom方法即使用默认桥名。
// 默认桥名：WebViewJavascriptBridge
webView.setCustom("TestJavascriptBridge");
```

### Android上使用方法

注册一个handler方法供Js调用
```java
webView.registerHandler("initSignNetPay", new BridgeHandler() {
   @Override
   public void handler(String data, CallBackFunction function) {
      Log.i(TAG, "回传结果：" + data);
      Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
   }
});
```

Java里调用Js里的handler方法

```java
webView.callHandler("click1", "success", new CallBackFunction() {
   @Override
   public void onCallBack(String data) {
      Log.i(TAG, "回传结果：" + data);
      Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
   }
});
```

使用默认handler方法来进行交互

```java
webView.setDefaultHandler(new DefaultHandler());
```

```javascript
window.WebViewJavascriptBridge.send(
   data, function(responseData) {
      document.getElementById("show").innerHTML = "repsonseData from java, data = " + responseData
   }
);
```

### JavaScript上使用方法

构建桥连接

```javascript
var default_data = {
   error: "1"
};

var connectMerchantJSBridge = function (callback) {
   try {
      if (window.WebViewJavascriptBridge) {
      callback(WebViewJavascriptBridge);
   } else {
      document.addEventListener("WebViewJavascriptBridgeReady", function () {
         callback(WebViewJavascriptBridge);
         }, false);
      }
   } catch (ex) { }
};

var cmbMerchantBridge = {
   initSignNet: function (payData,name) {
      if (!payData) {
         payData = default_data;
      }
      connectMerchantJSBridge(function (bridge) {
         if (typeof bridge === "undefined") {
            return;
         }
      bridge.callHandler(name, JSON.stringify(payData));
      });
   },
};
````

调用Android上的handler方法

```javascript
function click1(){
   var objData = new datas();
   var payData = objData.click1;
   try {
      cmbMerchantBridge.initSignNet(payData, "initSignNetPay");
   } catch (ex) { }
}
```

JavaScript里注册一个handler方法供Android调用

```javascript
/*app native调用本页面方法*/
connectMerchantJSBridge(function(bridge) {
   bridge.init(function(message, responseCallback) {

});

bridge.registerHandler("click1", function(data, responseCallback) {
   responseCallback("receive click1");
   //可以在下面执行操作
   });
})

window.cmbMerchantBridge = cmbMerchantBridge;
```

### 二、TbsBridgeWebView

TbsBridgeWebView基于Tbs(腾讯浏览服务)X5内核，结合JsBridge的自定义WebView。

### TbsBridgeWebView使用

考虑App用户群的极少数没装有微信、手Q的情况，因此采用TbsX5 for share。下文基于Tbs for share来实现。

之前写过一篇<a href="http://hjhrq1991.info/2016/08/22/JsBridge">Android-使用JsBridge来优化js与本地的交互</a>的文章，这次的TbsBridgeWebView同样集成了这套JsBridge，同时使用TbsX5解决web适配问题。

添加maven依赖

```maven
<dependency>
<groupId>com.hjhrq1991.library.tbs</groupId>
<artifactId>tbsjsbridge</artifactId>
<version>1.0.1</version>
<type>pom</type>
</dependency>
```

添加gradle依赖

```gradle
compile 'com.hjhrq1991.library.tbs:tbsjsbridge:1.0.1'
```

添加权限
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_SETTINGS" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

在你的布局上添加TbsBridgeWebView

```xml
<com.hjhrq1991.library.tbs.TbsBridgeWebView
android:id="@+id/webView"
android:layout_width="match_parent"
android:layout_height="match_parent" />
```
JsBridge的使用请参考上面的使用方法。

### 重要Tips

Tbs替换android.webkit相同的类
```
#系统内核                                       #SDK内核
android.webkit.ConsoleMessage                 com.tencent.smtt.export.external.interfaces.ConsoleMessage
android.webkit.CacheManager                   com.tencent.smtt.sdk.CacheManager(deprecated)
android.webkit.CookieManager                  com.tencent.smtt.sdk.CookieManager
android.webkit.CookieSyncManager              com.tencent.smtt.sdk.CookieSyncManager
android.webkit.CustomViewCallback             com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback
android.webkit.DownloadListener               com.tencent.smtt.sdk.DownloadListener
android.webkit.GeolocationPermissions         com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback
android.webkit.HttpAuthHandler                com.tencent.smtt.export.external.interfaces.HttpAuthHandler
android.webkit.JsPromptResult                 com.tencent.smtt.export.external.interfaces.JsPromptResult
android.webkit.JsResult                       com.tencent.smtt.export.external.interfaces.JsResult
android.webkit.SslErrorHandler                com.tencent.smtt.export.external.interfaces.SslErrorHandler
android.webkit.ValueCallback                  com.tencent.smtt.sdk.ValueCallback
android.webkit.WebBackForwardList             com.tencent.smtt.sdk.WebBackForwardList
android.webkit.WebChromeClient                com.tencent.smtt.sdk.WebChromeClient
android.webkit.WebHistoryItem                 com.tencent.smtt.sdk.WebHistoryItem
android.webkit.WebIconDatabase                com.tencent.smtt.sdk.WebIconDatabase
android.webkit.WebResourceResponse            com.tencent.smtt.export.external.interfaces.WebResourceResponse
android.webkit.WebSettings                    com.tencent.smtt.sdk.WebSettings
android.webkit.WebSettings.LayoutAlgorithm    com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm
android.webkit.WebStorage                     com.tencent.smtt.sdk.WebStorage
android.webkit.WebView                        com.tencent.smtt.sdk.WebView
android.webkit.WebViewClient                  com.tencent.smtt.sdk.WebViewClient
```

### 关于Cookie
com.tencent.smtt.sdk.CookieManager和com.tencent.smtt.sdk.CookieSyncManager的相关接口的调用，在接入SDK后，需要放到创建X5的WebView之后（也就是X5内核加载完成）进行；否则，cookie的相关操作只能影响系统内核。

## License

This project is licensed under the terms of the MIT license.

