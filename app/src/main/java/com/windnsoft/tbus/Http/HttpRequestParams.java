package com.windnsoft.tbus.Http;

import android.net.Uri;
import android.util.Log;

import com.windnsoft.tbus.General.WsNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpRequestParams {
    private static final String TAG = "WSRequestParam";

    private String protocol;
    private String domain;
    private Uri url;
    private String path;
    private List<WsNameValuePair> params;
    private List<WsNameValuePair> headers;
    private Map<String, Object> parts;
    private Methods methods = Methods.Post;

    public enum Methods {
        Put, Get, Post;
    }

    public static HttpRequestParams getInstance() {
        HttpRequestParams instance = new HttpRequestParams();
        return instance;
    }

    public HttpRequestParams() {
        url = Uri.EMPTY;
    }


    public Uri getUrl() {
        return url;
    }

    public HttpRequestParams setUrl(Uri url) {
        this.url = url;
        protocol = url.getScheme();
        domain = url.getAuthority();
        path = url.getPath();
        return this;
    }

    private void buildUrl() {
        buildUrl(protocol, domain, path);
    }

    public HttpRequestParams buildUrl(String protocol, String domain, String path) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol);
        builder.authority(domain);
        builder.appendEncodedPath(path);
        url = builder.build();

        return this;
    }

    public static Uri buildUri(String protocol, String domain, String path) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol);
        builder.authority(domain);
        builder.appendEncodedPath(path);
        return builder.build();
    }

    public String getPath() {
        return path;
    }

    public HttpRequestParams setPath(String path) {
        this.path = path;
        if (protocol != null && path != null) {
            buildUrl();
        }
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public HttpRequestParams setDomain(String domain) {
        this.domain = domain;
        if (protocol != null && path != null) {
            buildUrl();
        }
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public HttpRequestParams setProtocol(String protocol) {
        this.protocol = protocol;
        if (domain != null && path != null) {
            buildUrl();
        }
        return this;
    }

    public HttpRequestParams addHeader(String key, Object value) {
        if (headers == null)
            headers = new ArrayList<WsNameValuePair>();

        headers.add(new WsNameValuePair(key, "" + value));
        return this;
    }

    public List<WsNameValuePair> getHeaders() {
        return headers;
    }

    public Map<String, String> getHeadersAsMap() {
        if (headers == null) return null;

        HashMap map = new HashMap<String, String>();
        for (WsNameValuePair pair : headers) {
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    public HttpRequestParams setHeaders(List<WsNameValuePair> headers) {
        this.headers = headers;
        return this;
    }

    public List<WsNameValuePair> getParams() {
        return params;
    }

    public Map<String, String> getParamsAsMap() {
        if (params == null) return null;

        HashMap map = new HashMap<String, String>();
        for (WsNameValuePair pair : params) {
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    public HttpRequestParams addParam(String key, Object value) {
        if (params == null)
            params = new ArrayList<WsNameValuePair>();

        params.add(new WsNameValuePair(key, "" + value));
        return this;
    }

    public HttpRequestParams setParams(List<WsNameValuePair> params) {
        this.params = params;
        return this;
    }

    public HttpRequestParams addPart(String key, String value) {
        if (this.parts == null)
            parts = new HashMap<>();
        parts.put(key, value);
        return this;
    }

    public HttpRequestParams addPart(String key, File file) {
        if (this.parts == null)
            parts = new HashMap<>();
        parts.put(key, file);
        return this;
    }

    public Map<String, Object> getParts(){
        return parts;
    }

    public Methods getMethods() {
        return methods;
    }

    public HttpRequestParams setMethods(Methods methods) {
        this.methods = methods;
        return this;
    }

    public void testPrint() {
        Log.d(TAG, "params");
        if (params != null) {
            Log.d(TAG, "url=" + url.toString());
            for (WsNameValuePair nvp : params) {
                Log.d(TAG, "\t key= " + nvp.getName() + ", value= " + nvp.getValue());
            }
        }
    }

}
