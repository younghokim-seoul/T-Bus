package com.windnsoft.tbus.Http;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.windnsoft.tbus.General.CreateProgressDialog;
import com.windnsoft.tbus.General.WsLog;

import java.io.File;
import java.io.IOException;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class HttpRequestAsync {
    public final static String TAG = "HttpRequestAsync";

    public interface OnCompleteListener {
        void onSuccess(String result);

        void onFail(String result);

        void onError(int code, String result);
    }

    public static void post(@NonNull final String url, @Nullable final OnCompleteListener listener) {
        post(HttpRequestParams.getInstance().setUrl(Uri.parse(url)), listener);
    }

    public static void post(@NonNull final HttpRequestParams reqParam, @Nullable final OnCompleteListener listener) {
        new AsyncTask<HttpRequestParams, Long, String>() {
            int code;

            @Override
            protected String doInBackground(HttpRequestParams... params) {
                String response;

                WsLog.d(reqParam.getUrl().toString());
//                WsLog.d(reqParam.getParams().toString());
                try {
                    HttpRequest request = HttpRequest.post(reqParam.getUrl().toString());
                    if (reqParam != null && reqParam.getParams() != null) {
                        request.form(reqParam.getParamsAsMap());
                    }
                    response = request.body();
                    code = request.code();
                } catch (HttpRequest.HttpRequestException exception) {
                    code = ResponseCodes.CodeHttpError;
                    return null;
                }

                return response;
            }

            protected void onPostExecute(String response) {
                WsLog.d("code:" + code + " /// response:" + response);

                if (listener != null) {
                    switch (code) {
                        case ResponseCodes.CodeHttpOk:
                            listener.onSuccess(response);
                            break;
                        case ResponseCodes.CodeHttpError:
                            listener.onFail(response);
                            break;
                        default:
                            listener.onError(code, response);
                            break;
                    }
                }
            }
        }.execute(reqParam);
    }

    public static void excute(@NonNull HttpRequestParams param, @Nullable OnCompleteListener listener) {
        if (param.getMethods() == HttpRequestParams.Methods.Post) {
            post(param, listener);
        }
    }

    public static void multipart(@NonNull final HttpRequestParams reqParam, @Nullable final OnCompleteListener listener, final Context context){
        new AsyncTask<HttpRequestParams, Long, String>() {
            int code;
            Dialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = CreateProgressDialog.show(context, null, null);
            }

            @Override
            protected String doInBackground(HttpRequestParams... params) {
                String response = "///no response///";

                WsLog.d(reqParam.getUrl().toString());
                try {
                    HttpMultipartRequest multipart = new HttpMultipartRequest(reqParam.getUrl().toString(), "utf-8");
                    for (String key: reqParam.getParts().keySet()) {
                        Object obj = reqParam.getParts().get(key);
                        if(obj instanceof File) {
                            multipart.addFilePart(key, (File) obj);
                        }else if(obj instanceof String) {
                            multipart.addFormField(key, (String) obj);
                            WsLog.v(key+":"+(String)obj);
                        }
                    }

                    response = multipart.finish();
                    code = multipart.getCode();

                } catch (IOException e) {
                    e.printStackTrace();
                }
/*

                HttpRequest request = HttpRequest.post(reqParam.getUrl().toString());
                try {
                    for (String key: reqParam.getParts().keySet()) {
                        Object obj = reqParam.getParts().get(key);
                        if(obj instanceof File)
                            request.part(key, (File) obj);
                        else if(obj instanceof String) {
//                            request.form(key, obj);
                            request.part(key, (String) obj);
                        }
                    }

                    response = request.body();
                    code = request.code();
                    request.closeOutputQuietly();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    code = ResponseCodes.CodeHttpError;
                    return "request multipartOld error";
                } finally {
                }
*/

                return response;
            }

            protected void onPostExecute(String response) {
                dialog.dismiss();
                WsLog.d("code:" + code + " /// response:" + response);

                if (listener != null) {
                    switch (code) {
                        case ResponseCodes.CodeHttpOk:
                            listener.onSuccess(response);
                            break;
                        case ResponseCodes.CodeHttpError:
                            listener.onFail(response);
                            break;
                        default:
                            listener.onError(code, response);
                            break;
                    }
                }
            }
        }.execute(reqParam);
    }

    public static void multipartOld(@NonNull final HttpRequestParams reqParam, @Nullable final OnCompleteListener listener, final Context context) {
        new AsyncTask<HttpRequestParams, Long, String>() {
            int code;
            Dialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = CreateProgressDialog.show(context, null, null);
            }

            @Override
            protected String doInBackground(HttpRequestParams... params) {
                String response;

                WsLog.d(reqParam.getUrl().toString());
                HttpRequest request = HttpRequest.post(reqParam.getUrl().toString());
                try {
                    for (String key: reqParam.getParts().keySet()) {
                        Object obj = reqParam.getParts().get(key);
                        if(obj instanceof File)
                            request.part(key, (File) obj);
                        else if(obj instanceof String) {
//                            request.form(key, obj);
                            request.part(key, (String) obj);
                        }
                    }

                    response = request.body();
                    code = request.code();
                    request.closeOutputQuietly();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    code = ResponseCodes.CodeHttpError;
                    return "request multipart error";
                } finally {
                }

                return response;
            }

            protected void onPostExecute(String response) {
                dialog.dismiss();
                WsLog.d("code:" + code + " /// response:" + response);

                if (listener != null) {
                    switch (code) {
                        case ResponseCodes.CodeHttpOk:
                            listener.onSuccess(response);
                            break;
                        case ResponseCodes.CodeHttpError:
                            listener.onFail(response);
                            break;
                        default:
                            listener.onError(code, response);
                            break;
                    }
                }
            }
        }.execute(reqParam);
    }


}
