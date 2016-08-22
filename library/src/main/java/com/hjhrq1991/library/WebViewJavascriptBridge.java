package com.hjhrq1991.library;


public interface WebViewJavascriptBridge {

    void send(String data);

    void send(String data, CallBackFunction responseCallback);

}
