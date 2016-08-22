# JsBridge
基于https://github.com/lzyzsd/JsBridge 优化改进而来的Android JsBridge

### 优化
1.支持自定义桥名；<br/>2.修复web页未渲染即进行跳转导致Js桥初始化失败的问题；

### 使用

添加maven依赖

```maven

<dependency>
  <groupId>com.hjhrq1991.library</groupId>
  <artifactId>jsbridge</artifactId>
  <version>1.0.5</version>
  <type>pom</type>
</dependency>

```

添加gradle依赖

```gradle

compile 'com.hjhrq1991.library:jsbridge:1.0.5'

```

在你的布局上添加BridgeWebView

```xml

<com.github.lzyzsd.jsbridge.BridgeWebView
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

## License

This project is licensed under the terms of the MIT license.

