package com.hjhrq1991.library

class DefaultHandler : BridgeHandler {
    private val TAG = "DefaultHandler"

    override fun handler(data: String?, function: CallBackFunction?) {
        function?.onCallBack("DefaultHandler response data")
    }
}
