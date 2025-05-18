package com.hjhrq1991.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 * @author haoqing
 *
 */
public class Message {

    private String callbackId; //callbackId
    private String responseId; //responseId
    private String responseData; //responseData
    private String data; //data of message
    private String handlerName; //name of handler
    private boolean highPriority; // new field for priority

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";
    private final static String HIGH_PRIORITY_STR = "highPriority";

    public String getResponseId() {
        return responseId;
    }
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    public String getResponseData() {
        return responseData;
    }
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    public String getCallbackId() {
        return callbackId;
    }
    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getHandlerName() {
        return handlerName;
    }
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public void setHighPriority(boolean highPriority) {
        this.highPriority = highPriority;
    }

    public String toJson() {
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            jsonObject.put(RESPONSE_DATA_STR, getResponseData());
            jsonObject.put(RESPONSE_ID_STR, getResponseId());
            jsonObject.put(HIGH_PRIORITY_STR, isHighPriority());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toObject(String jsonStr) {
        Message m =  new Message();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            m.setHandlerName(jsonObject.optString(HANDLER_NAME_STR));
            m.setCallbackId(jsonObject.optString(CALLBACK_ID_STR));
            m.setResponseData(jsonObject.optString(RESPONSE_DATA_STR));
            m.setResponseId(jsonObject.optString(RESPONSE_ID_STR));
            m.setData(jsonObject.optString(DATA_STR));
            m.setHighPriority(jsonObject.optBoolean(HIGH_PRIORITY_STR));
            return m;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return m;
    }

    public static List<Message> toArrayList(String jsonStr){
        List<Message> list = new ArrayList<>();

        // Return empty list for null or empty input
        if (jsonStr == null || jsonStr.trim().isEmpty() || "null".equalsIgnoreCase(jsonStr)) {
            return list;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i = 0; i < jsonArray.length(); i++){
                Message m = new Message();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                m.setHandlerName(jsonObject.optString(HANDLER_NAME_STR));
                m.setCallbackId(jsonObject.optString(CALLBACK_ID_STR));
                m.setResponseData(jsonObject.optString(RESPONSE_DATA_STR));
                m.setResponseId(jsonObject.optString(RESPONSE_ID_STR));
                m.setData(jsonObject.optString(DATA_STR));
                m.setHighPriority(jsonObject.optBoolean(HIGH_PRIORITY_STR));
                list.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
