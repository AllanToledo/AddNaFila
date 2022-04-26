package com.allantoledo.addnafila.requests;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONCompleteRequest extends JsonObjectRequest {

    private JSONObject body;
    private JSONObject headers;
    private byte[] bodyByteArray;

    public JSONCompleteRequest(int method, String url, @Nullable JSONObject body, @Nullable JSONObject headers, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
        this.body = body;
        this.headers = headers;
    }
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Iterator<String> nameItr = headers.keys();
        Map<String, String> outMap = new HashMap<String, String>();
        while(nameItr.hasNext()) {
            String name = nameItr.next();
            try {
                outMap.put(name, headers.get(name).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return outMap;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        Iterator<String> nameItr = body.keys();
        Map<String, String> outMap = new HashMap<String, String>();
        while(nameItr.hasNext()) {
            String name = nameItr.next();
            try {
                outMap.put(name, body.get(name).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return outMap;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded";
    }

    @Override
    public byte[] getBody() {
        if(bodyByteArray != null)
            return bodyByteArray;
        Iterator<String> nameItr = body.keys();
        String bodyString = "";
        while(nameItr.hasNext()) {
            String name = nameItr.next();
            try {
                if(bodyString.length()>0){
                    bodyString += String.format("&%s=%s", name, URLEncoder.encode(body.get(name).toString()));
                } else {
                    bodyString += String.format("%s=%s", name, URLEncoder.encode(body.get(name).toString()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        bodyByteArray = bodyString.getBytes(StandardCharsets.UTF_8);
        return bodyByteArray;
    }
}
