//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if (window.WebViewJavascriptBridge) { return; }

    //注入文件不能直接使用script标签引入js文件，因此使用内联js文件源码的方式进行引用
    var LZString=function(){function o(o,r){if(!t[o]){t[o]={};for(var n=0;n<o.length;n++)t[o][o.charAt(n)]=n}return t[o][r]}var r=String.fromCharCode,n="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",e="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$",t={},i={compressToBase64:function(o){if(null==o)return"";var r=i._compress(o,6,function(o){return n.charAt(o)});switch(r.length%4){default:case 0:return r;case 1:return r+"===";case 2:return r+"==";case 3:return r+"="}},decompressFromBase64:function(r){return null==r?"":""==r?null:i._decompress(r.length,32,function(e){return o(n,r.charAt(e))})},compressToUTF16:function(o){return null==o?"":i._compress(o,15,function(o){return r(o+32)})+" "},decompressFromUTF16:function(o){return null==o?"":""==o?null:i._decompress(o.length,16384,function(r){return o.charCodeAt(r)-32})},compressToUint8Array:function(o){for(var r=i.compress(o),n=new Uint8Array(2*r.length),e=0,t=r.length;t>e;e++){var s=r.charCodeAt(e);n[2*e]=s>>>8,n[2*e+1]=s%256}return n},decompressFromUint8Array:function(o){if(null===o||void 0===o)return i.decompress(o);for(var n=new Array(o.length/2),e=0,t=n.length;t>e;e++)n[e]=256*o[2*e]+o[2*e+1];var s=[];return n.forEach(function(o){s.push(r(o))}),i.decompress(s.join(""))},compressToEncodedURIComponent:function(o){return null==o?"":i._compress(o,6,function(o){return e.charAt(o)})},decompressFromEncodedURIComponent:function(r){return null==r?"":""==r?null:(r=r.replace(/ /g,"+"),i._decompress(r.length,32,function(n){return o(e,r.charAt(n))}))},compress:function(o){return i._compress(o,16,function(o){return r(o)})},_compress:function(o,r,n){if(null==o)return"";var e,t,i,s={},p={},u="",c="",a="",l=2,f=3,h=2,d=[],m=0,v=0;for(i=0;i<o.length;i+=1)if(u=o.charAt(i),Object.prototype.hasOwnProperty.call(s,u)||(s[u]=f++,p[u]=!0),c=a+u,Object.prototype.hasOwnProperty.call(s,c))a=c;else{if(Object.prototype.hasOwnProperty.call(p,a)){if(a.charCodeAt(0)<256){for(e=0;h>e;e++)m<<=1,v==r-1?(v=0,d.push(n(m)),m=0):v++;for(t=a.charCodeAt(0),e=0;8>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}else{for(t=1,e=0;h>e;e++)m=m<<1|t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t=0;for(t=a.charCodeAt(0),e=0;16>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}l--,0==l&&(l=Math.pow(2,h),h++),delete p[a]}else for(t=s[a],e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;l--,0==l&&(l=Math.pow(2,h),h++),s[c]=f++,a=String(u)}if(""!==a){if(Object.prototype.hasOwnProperty.call(p,a)){if(a.charCodeAt(0)<256){for(e=0;h>e;e++)m<<=1,v==r-1?(v=0,d.push(n(m)),m=0):v++;for(t=a.charCodeAt(0),e=0;8>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}else{for(t=1,e=0;h>e;e++)m=m<<1|t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t=0;for(t=a.charCodeAt(0),e=0;16>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}l--,0==l&&(l=Math.pow(2,h),h++),delete p[a]}else for(t=s[a],e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;l--,0==l&&(l=Math.pow(2,h),h++)}for(t=2,e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;for(;;){if(m<<=1,v==r-1){d.push(n(m));break}v++}return d.join("")},decompress:function(o){return null==o?"":""==o?null:i._decompress(o.length,32768,function(r){return o.charCodeAt(r)})},_decompress:function(o,n,e){var t,i,s,p,u,c,a,l,f=[],h=4,d=4,m=3,v="",w=[],A={val:e(0),position:n,index:1};for(i=0;3>i;i+=1)f[i]=i;for(p=0,c=Math.pow(2,2),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;switch(t=p){case 0:for(p=0,c=Math.pow(2,8),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;l=r(p);break;case 1:for(p=0,c=Math.pow(2,16),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;l=r(p);break;case 2:return""}for(f[3]=l,s=l,w.push(l);;){if(A.index>o)return"";for(p=0,c=Math.pow(2,m),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;switch(l=p){case 0:for(p=0,c=Math.pow(2,8),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;f[d++]=r(p),l=d-1,h--;break;case 1:for(p=0,c=Math.pow(2,16),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;f[d++]=r(p),l=d-1,h--;break;case 2:return w.join("")}if(0==h&&(h=Math.pow(2,m),m++),f[l])v=f[l];else{if(l!==d)return null;v=s+s.charAt(0)}w.push(v),f[d++]=s+v.charAt(0),h--,s=v,0==h&&(h=Math.pow(2,m),m++)}}};return i}();"function"==typeof define&&define.amd?define(function(){return LZString}):"undefined"!=typeof module&&null!=module&&(module.exports=LZString);

    function zipString(str) {
        return LZString.compressToBase64(str);
        //体积比 Base64 小约 30%
        //        return LZString.compressToUTF16(str);
    }

    function unzipString(str) {
        return LZString.decompressFromBase64(str);
        //体积比 Base64 小约 30%
        //        return LZString.decompressFromUTF16(str);
    }

    // 创建Web Worker用于处理压缩/解压缩
    const compressionWorker = new Worker(URL.createObjectURL(new Blob([`
//        self.importScripts('LZString.js');
        var LZString=function(){function o(o,r){if(!t[o]){t[o]={};for(var n=0;n<o.length;n++)t[o][o.charAt(n)]=n}return t[o][r]}var r=String.fromCharCode,n="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",e="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$",t={},i={compressToBase64:function(o){if(null==o)return"";var r=i._compress(o,6,function(o){return n.charAt(o)});switch(r.length%4){default:case 0:return r;case 1:return r+"===";case 2:return r+"==";case 3:return r+"="}},decompressFromBase64:function(r){return null==r?"":""==r?null:i._decompress(r.length,32,function(e){return o(n,r.charAt(e))})},compressToUTF16:function(o){return null==o?"":i._compress(o,15,function(o){return r(o+32)})+" "},decompressFromUTF16:function(o){return null==o?"":""==o?null:i._decompress(o.length,16384,function(r){return o.charCodeAt(r)-32})},compressToUint8Array:function(o){for(var r=i.compress(o),n=new Uint8Array(2*r.length),e=0,t=r.length;t>e;e++){var s=r.charCodeAt(e);n[2*e]=s>>>8,n[2*e+1]=s%256}return n},decompressFromUint8Array:function(o){if(null===o||void 0===o)return i.decompress(o);for(var n=new Array(o.length/2),e=0,t=n.length;t>e;e++)n[e]=256*o[2*e]+o[2*e+1];var s=[];return n.forEach(function(o){s.push(r(o))}),i.decompress(s.join(""))},compressToEncodedURIComponent:function(o){return null==o?"":i._compress(o,6,function(o){return e.charAt(o)})},decompressFromEncodedURIComponent:function(r){return null==r?"":""==r?null:(r=r.replace(/ /g,"+"),i._decompress(r.length,32,function(n){return o(e,r.charAt(n))}))},compress:function(o){return i._compress(o,16,function(o){return r(o)})},_compress:function(o,r,n){if(null==o)return"";var e,t,i,s={},p={},u="",c="",a="",l=2,f=3,h=2,d=[],m=0,v=0;for(i=0;i<o.length;i+=1)if(u=o.charAt(i),Object.prototype.hasOwnProperty.call(s,u)||(s[u]=f++,p[u]=!0),c=a+u,Object.prototype.hasOwnProperty.call(s,c))a=c;else{if(Object.prototype.hasOwnProperty.call(p,a)){if(a.charCodeAt(0)<256){for(e=0;h>e;e++)m<<=1,v==r-1?(v=0,d.push(n(m)),m=0):v++;for(t=a.charCodeAt(0),e=0;8>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}else{for(t=1,e=0;h>e;e++)m=m<<1|t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t=0;for(t=a.charCodeAt(0),e=0;16>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}l--,0==l&&(l=Math.pow(2,h),h++),delete p[a]}else for(t=s[a],e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;l--,0==l&&(l=Math.pow(2,h),h++),s[c]=f++,a=String(u)}if(""!==a){if(Object.prototype.hasOwnProperty.call(p,a)){if(a.charCodeAt(0)<256){for(e=0;h>e;e++)m<<=1,v==r-1?(v=0,d.push(n(m)),m=0):v++;for(t=a.charCodeAt(0),e=0;8>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}else{for(t=1,e=0;h>e;e++)m=m<<1|t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t=0;for(t=a.charCodeAt(0),e=0;16>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1}l--,0==l&&(l=Math.pow(2,h),h++),delete p[a]}else for(t=s[a],e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;l--,0==l&&(l=Math.pow(2,h),h++)}for(t=2,e=0;h>e;e++)m=m<<1|1&t,v==r-1?(v=0,d.push(n(m)),m=0):v++,t>>=1;for(;;){if(m<<=1,v==r-1){d.push(n(m));break}v++}return d.join("")},decompress:function(o){return null==o?"":""==o?null:i._decompress(o.length,32768,function(r){return o.charCodeAt(r)})},_decompress:function(o,n,e){var t,i,s,p,u,c,a,l,f=[],h=4,d=4,m=3,v="",w=[],A={val:e(0),position:n,index:1};for(i=0;3>i;i+=1)f[i]=i;for(p=0,c=Math.pow(2,2),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;switch(t=p){case 0:for(p=0,c=Math.pow(2,8),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;l=r(p);break;case 1:for(p=0,c=Math.pow(2,16),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;l=r(p);break;case 2:return""}for(f[3]=l,s=l,w.push(l);;){if(A.index>o)return"";for(p=0,c=Math.pow(2,m),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;switch(l=p){case 0:for(p=0,c=Math.pow(2,8),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;f[d++]=r(p),l=d-1,h--;break;case 1:for(p=0,c=Math.pow(2,16),a=1;a!=c;)u=A.val&A.position,A.position>>=1,0==A.position&&(A.position=n,A.val=e(A.index++)),p|=(u>0?1:0)*a,a<<=1;f[d++]=r(p),l=d-1,h--;break;case 2:return w.join("")}if(0==h&&(h=Math.pow(2,m),m++),f[l])v=f[l];else{if(l!==d)return null;v=s+s.charAt(0)}w.push(v),f[d++]=s+v.charAt(0),h--,s=v,0==h&&(h=Math.pow(2,m),m++)}}};return i}();"function"==typeof define&&define.amd?define(function(){return LZString}):"undefined"!=typeof module&&null!=module&&(module.exports=LZString);

        self.onmessage = function(e) {
            const { id, type, data } = e.data;
            let result;

            try {
                if (type === 'compress') {
                    result = LZString.compressToBase64(data);
                } else if (type === 'decompress') {
                    result = LZString.decompressFromBase64(data);
                }
                self.postMessage({ id, success: true, result });
            } catch (error) {
                self.postMessage({ id, success: false, error: error.message });
            }
        };
    `], { type: 'text/javascript' })));

    // 使用Promise包装压缩/解压缩操作
    // 使用Map存储pending的操作
    const pendingOperations = new Map();
    let operationId = 0;

    compressionWorker.onmessage = function(e) {
        const { id, success, result, error } = e.data;
        const operation = pendingOperations.get(id);
        if (operation) {
            pendingOperations.delete(id);
            if (success) {
                operation.resolve(result);
            } else {
                operation.reject(new Error(error));
            }
        }
    };

    function asyncCompress(str) {
        return new Promise((resolve, reject) => {
            const id = ++operationId;
            pendingOperations.set(id, { resolve, reject });

            compressionWorker.postMessage({
                id,
                type: 'compress',
                data: str
            });

            // 添加超时处理
            setTimeout(() => {
                if (pendingOperations.has(id)) {
                    pendingOperations.delete(id);
                    reject(new Error('Compression timeout'));
                }
            }, 30000);
        });
    }

    function asyncDecompress(str) {
        return new Promise((resolve, reject) => {
            const id = ++operationId;
            pendingOperations.set(id, { resolve, reject });

            compressionWorker.postMessage({
                id,
                type: 'decompress',
                data: str
            });

            // 添加超时处理
            setTimeout(() => {
                if (pendingOperations.has(id)) {
                    pendingOperations.delete(id);
                    reject(new Error('Decompression timeout'));
                }
            }, 30000);
        });
    }

    // 常量定义
    var CUSTOM_PROTOCOL_SCHEME = 'yy';
    var QUEUE_HAS_MESSAGE = 'bridge/';
    var MAX_QUEUE_LENGTH = 500;
    // 数据交互最大长度，超过则进行压缩处理，避免导致无响应
    var maxLength = 10 * 10000;

    // 核心变量
    var messagingIframe;
    var sendMessageQueue = [];
    var receiveMessageQueue = [];
    var messageHandlers = {};
    var responseCallbacks = {};
    var uniqueId = 1;
    // 桥是否初始化
    var isBridgeInit = false;

    // ==================== 流量控制核心 ====================
    // 每次批量发送3条消息
    var BATCH_SIZE = 5;
    // 最小发送间隔(ms)
    var MIN_SEND_INTERVAL = 25;
    var lastSendTime = 0;
    var isSending = false;

    // 在 WebViewJavascriptBridge 初始化之前添加监控函数
    //    function monitorQueueSize() {
    //        var data = {
    //            '发送队列长度': sendMessageQueue.length,
    //            '接收队列长度': receiveMessageQueue ? receiveMessageQueue.length : 0,
    //            '消息处理器数量': Object.keys(messageHandlers).length,
    //            '回调函数数量': Object.keys(responseCallbacks).length
    //        };
    //
    //        console.log('队列监控报告：', JSON.stringify(data));
    //
    //        // 检查队列大小是否异常
    //        if (sendMessageQueue.length > 200) {
    //            console.warn('监控警告：发送队列积压严重，当前长度：', sendMessageQueue.length);
    //            sendMessageQueue = sendMessageQueue.slice(-200);
    //        }
    //        if (receiveMessageQueue && receiveMessageQueue.length > 200) {
    //            console.warn('监控警告：接收队列积压严重，当前长度：', receiveMessageQueue.length);
    //            receiveMessageQueue = receiveMessageQueue.slice(-200);
    //        }
    //        if (Object.keys(responseCallbacks).length > 500) {
    //            console.warn('监控警告：回调函数可能存在泄漏，当前数量：', Object.keys(responseCallbacks).length);
    //
    //            var now = Date.now();
    //            var timeout = 30 * 60 * 1000;
    //            Object.keys(responseCallbacks).forEach(function(callbackId) {
    //                try {
    //                    var timestamp = parseInt(callbackId.split('_')[2]);
    //                    // 30分钟前的超时回调
    //                    if (now - timestamp > timeout) {
    //                        console.warn('监控警告：清理超时回调：', callbackId);
    //                        delete responseCallbacks[callbackId];
    //                    }
    //                } catch (error) {
    //                    console.error('监控警告：清理超时回调异常：', error);
    //                }
    //            });
    //        }
    //    }

    // ==================== 核心优化部分 ====================
    function enhancedDoSend(message, responseCallback) {
        // 1. 注册回调（带超时）
        if (responseCallback) {
            var callbackId = 'cb_' + (uniqueId++) + '_' + Date.now();
            responseCallbacks[callbackId] = responseCallback;
            message.callbackId = callbackId;

            // 8秒超时
            setTimeout(function() {
                if (responseCallbacks[callbackId]) {
                    responseCallbacks[callbackId]({ error: "timeout" });
                    delete responseCallbacks[callbackId];
                }
            }, 8000);
        }

        // 2. 队列控制
        if (sendMessageQueue.length >= MAX_QUEUE_LENGTH) {
            var removed = sendMessageQueue.shift();
            if (removed.callbackId) {
                delete responseCallbacks[removed.callbackId];
            }
        }

        sendMessageQueue.push(message);
        scheduleBatchSend();
    }

    function scheduleBatchSend() {
        if (isSending || sendMessageQueue.length === 0) return;

        var now = Date.now();
        var delay = Math.max(0, MIN_SEND_INTERVAL - (now - lastSendTime));

        setTimeout(function() {
            isSending = true;

            // 批量取出消息
            var batch = [];
            while (batch.length < BATCH_SIZE && sendMessageQueue.length > 0) {
                batch.push(sendMessageQueue.shift());
            }

            // 发送批量消息
            var batchData = encodeURIComponent(JSON.stringify(batch));
            messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://batch/' + batchData;
            lastSendTime = Date.now();

            // 继续处理剩余消息
            isSending = false;
            if (sendMessageQueue.length > 0) {
                scheduleBatchSend();
            }
        }, delay);
    }

    function _createQueueReadyIframe(doc) {
        messagingIframe = doc.createElement('iframe');
        messagingIframe.style.display = 'none';
        doc.documentElement.appendChild(messagingIframe);
    }

    //set default messageHandler
    function init(messageHandler) {
        isBridgeInit = true;
        if (WebViewJavascriptBridge._messageHandler) {
            throw new Error('WebViewJavascriptBridge.init called twice');
        }
        WebViewJavascriptBridge._messageHandler = messageHandler;
        var receivedMessages = receiveMessageQueue;
        receiveMessageQueue = null;
        for (var i = 0; i < receivedMessages.length; i++) {
            _dispatchMessageFromNative(receivedMessages[i]);
        }
    }

    function send(data, responseCallback) {
        _doSend({
            data: data
        }, responseCallback);
    }

    //注册
    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
        console.log("App JSBridge", "web 注册了handler：" + handlerName);
    }

    //反注册
    function unregisterHandler(handlerName) {
        delete messageHandlers[handlerName];
    }

    //前端调用
    function callHandler(handlerName, data, responseCallback) {
        var dataStr = JSON.stringify(data);
        try {
            // 如果 data 是字符串，解析它；否则直接使用
            const request = typeof data === 'string' ? JSON.parse(data) : data;
            const nativeConfig = JSON.stringify(request.nativeConfig);
            console.log("App JSBridge", "callHandler：" + handlerName + "  callName：" + nativeConfig);
        } catch (error) {
            console.error("App JSBridge", "压缩数据时发生错误：", error);
            console.error("App JSBridge", "callHandler：" + handlerName);
        }

        var length = dataStr.length;
        // 默认不压缩
        var _data = dataStr;

        if (length > maxLength) {
            console.log("App JSBridge", "数据长度大于等于 " + maxLength / 10000 + " 万，调用asyncCallHandler");
            asyncCallHandler(handlerName, data, responseCallback);
        } else {
            console.log("App JSBridge", "数据长度小于 " + maxLength / 10000 + " 万，无需压缩，直接执行");
            synchronizationCallHandler(handlerName, data, responseCallback);
        }
    }

    // 同步执行
    function synchronizationCallHandler(handlerName, data, responseCallback) {
        // ✅ _data 现在是最终要发送的数据
        _doSend({
            handlerName: handlerName,
            data: data
        }, responseCallback);
    }

    // 异步调用
    async function asyncCallHandler(handlerName, data, responseCallback) {
        var dataStr = JSON.stringify(data);
        var length = dataStr.length;
        // 默认不压缩
        var _data = dataStr;

        if (length > maxLength) {
            try {
                // ✅ 处理异常
                var compress = await asyncCompress(dataStr);
                console.log("App JSBridge", "原始数据长度：" + JSON.stringify(data).length + " 压缩后数据长度：" + compress.length);
                _data = "lzstring:" + compress
            } catch (error) {
                console.error("App JSBridge", "压缩数据时发生错误：", error);
            }
        } else {
            console.log("App JSBridge", "数据长度小于 " + maxLength / 10000 + " 万，无需压缩");
        }

        // ✅ _data 现在是最终要发送的数据
        _doSend({
            handlerName: handlerName,
            data: _data
        }, responseCallback);
    }

    // ==================== 原有函数改造 ====================
    function _doSend(message, responseCallback) {
        enhancedDoSend(message, responseCallback);
    }

    function _fetchQueue() {
        if (!isBridgeInit) return;

        // 批量取出所有消息
        var batch = [];
        while (sendMessageQueue.length > 0) {
            batch.push(sendMessageQueue.shift());
        }

        if (batch.length > 0) {
            var batchData = encodeURIComponent(JSON.stringify(batch));
            messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://batch_return/_fetchQueue/' + batchData;
        }
    }

    //sendMessage add message, 触发native处理 sendMessage
//    function _doSend(message, responseCallback) {
//        // 1. 检查队列是否堆积
//        if (sendMessageQueue.length >= MAX_QUEUE_LENGTH) {
//            console.warn("JS Bridge 队列积压，丢弃最旧的消息");
//            // 移除最旧的消息
//            sendMessageQueue.shift();
//        }
//
//        // 2. 如果有回调，检查是否已有太多未完成回调
//        if (responseCallback) {
//            const callbackId = 'cb_' + (uniqueId++) + '_' + Date.now();
//            responseCallbacks[callbackId] = responseCallback;
//            message.callbackId = callbackId;
//
//            // 添加超时清理
//            setTimeout(() => {
//                if (responseCallbacks[callbackId]) {
//                    // 超时后删除
//                    responseCallback({ error: "JS Bridge 回调超时" });
//                    delete responseCallbacks[callbackId];
//                }
//            }, 20 * 1000);
//        }
//
//        sendMessageQueue.push(message);
//        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
//    }

    // 提供给native调用,该函数作用:获取sendMessageQueue返回给native,由于android不能直接获取返回的内容,所以使用url shouldOverrideUrlLoading 的方式返回内容
    function _fetchQueue() {
        if (isBridgeInit == false) { return; }
        var messageQueueString = JSON.stringify(sendMessageQueue);

        sendMessageQueue = [];
        //android can't read directly the return data, so we can reload iframe src to communicate with java
        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://return/_fetchQueue/' + encodeURIComponent(messageQueueString);
    }

    // ==================== Native消息处理优化 ====================
    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            try {
                var message = JSON.parse(messageJSON);

                // 批量响应处理
                if (message.responseBatch) {
                    message.responseBatch.forEach(function(item) {
                        if (item.responseId && responseCallbacks[item.responseId]) {
                            responseCallbacks[item.responseId](item.responseData);
                            delete responseCallbacks[item.responseId];
                        }
                    });
                    return;
                }

                // 单条消息处理（保持原逻辑）
                if (message.responseId) {
                    var callback = responseCallbacks[message.responseId];
                    if (callback) {
                        callback(message.responseData);
                        delete responseCallbacks[message.responseId];
                    }
                    return;
                }

                // ... 其余原有逻辑保持不变
                var _data = message.data;
                if (_data.startsWith("lzstring:")) {
                    asyncDispatchMessageFromNative(messageJSON);
                } else {
                    synchronizationDispatchMessageFromNative(messageJSON);
                }
            } catch (e) {
                console.error("消息解析错误:", e);
            }
        }, 0);
    }

//    //提供给native使用,
//    function _dispatchMessageFromNative(messageJSON) {
//        try {
//            var message = JSON.parse(messageJSON);
//            var _data = message.data;
//            if (_data.startsWith("lzstring:")) {
//                asyncDispatchMessageFromNative(messageJSON);
//            } else {
//                synchronizationDispatchMessageFromNative(messageJSON);
//            }
//        } catch (exception) {
//
//        }
//    }

    function synchronizationDispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            var responseCallback;
            //java call finished, now need to call js callback function
            if (message.responseId) {
                responseCallback = responseCallbacks[message.responseId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
            } else {
                //直接发送
                if (message.callbackId) {
                    var callbackResponseId = message.callbackId;
                    responseCallback = function(responseData) {
                        _doSend({
                            responseId: callbackResponseId,
                            responseData: responseData
                        });
                    };
                }

                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    handler = messageHandlers[message.handlerName];
                }
                //查找指定handler
                try {
                    handler(message.data, responseCallback);
                } catch (exception) {
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
                }
            }
        });
    }

    async function asyncDispatchMessageFromNative(messageJSON) {
        setTimeout(async function() {
            var message = JSON.parse(messageJSON);
            var responseCallback;
            //java call finished, now need to call js callback function
            if (message.responseId) {
                responseCallback = responseCallbacks[message.responseId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
            } else {
                //直接发送
                if (message.callbackId) {
                    var callbackResponseId = message.callbackId;
                    responseCallback = function(responseData) {
                        _doSend({
                            responseId: callbackResponseId,
                            responseData: responseData
                        });
                    };
                }

                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    handler = messageHandlers[message.handlerName];
                }
                //查找指定handler
                try {
                    var _data = message.data;
                    if (_data.startsWith("lzstring:")) {
                        //存在压缩处理，进行解压缩操作
                        var decompress = _data.substring("lzstring:".length);
                        _data = await asyncDecompress(decompress);
                        console.log("App JSBridge", "send：" + " 数据量过大，需做解压处理 解压前长度：" + decompress.length + "  解压后长度：" + _data.length);
                    } else {

                    }

                    // ✅ _data 现在是最终要发送的数据
                    handler(_data, responseCallback);
                } catch (exception) {
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
                }
            }
        });
    }

    //App调用JS并发送数据
    function _handleMessageFromNative(messageJSON) {
        if (isBridgeInit == false) { return; }
        //        console.log(messageJSON);
        if (receiveMessageQueue) {
            if (receiveMessageQueue && receiveMessageQueue.length >= MAX_QUEUE_LENGTH) {
                receiveMessageQueue.shift();
            }
            receiveMessageQueue.push(messageJSON);
        } else {
            _dispatchMessageFromNative(messageJSON);
        }
    }

    var WebViewJavascriptBridge = window.WebViewJavascriptBridge = {
        init: init,
        send: function(data, responseCallback) {
            _doSend({ data: data }, responseCallback);
        },
        registerHandler: registerHandler,
        callHandler: callHandler,
        _fetchQueue: _fetchQueue,
        _handleMessageFromNative: _handleMessageFromNative
    };

    // 添加清理函数
    function cleanup() {
        try {
            // 清理 Worker
            if (compressionWorker) {
                compressionWorker.terminate();
                compressionWorker = null;
            }

            // 清理 pending 操作
            pendingOperations.forEach((operation, id) => {
                operation.reject(new Error('Bridge cleanup'));
            });
            pendingOperations.clear();

            // 清理队列
            sendMessageQueue = [];
            receiveMessageQueue = [];

            // 清理回调
            Object.keys(responseCallbacks).forEach(key => {
                const callback = responseCallbacks[key];
                if (typeof callback === 'function') {
                    try {
                        callback({ error: 'Bridge cleanup' });
                    } catch (e) {
                        console.error('Cleanup callback error:', e);
                    }
                }
            });
            responseCallbacks = {};

            // 清理 handlers
            messageHandlers = {};

            // 清理 iframe
            if (messagingIframe && messagingIframe.parentNode) {
                messagingIframe.parentNode.removeChild(messagingIframe);
                messagingIframe = null;
            }

            // 重置状态
            isBridgeInit = false;
            uniqueId = 1;
            operationId = 0;

            console.log('App JSBridge cleanup completed');
        } catch (error) {
            console.error('App JSBridge cleanup error:', error);
        }
    }

    var doc = document;
    _createQueueReadyIframe(doc);
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    doc.dispatchEvent(readyEvent);
    // 启动队列监控，每10秒执行一次监控
    //    setInterval(monitorQueueSize, 10 * 1000);

    // 在页面卸载时清理
    window.addEventListener('unload', cleanup);
})();