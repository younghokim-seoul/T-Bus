package com.windnsoft.tbus.Operator;

import android.content.Context;

import com.google.gson.Gson;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;

import java.io.File;
import java.util.List;

import static com.windnsoft.tbus.Http.HttpRequestAsync.OnCompleteListener;
import static com.windnsoft.tbus.Http.HttpRequestAsync.excute;
import static com.windnsoft.tbus.Http.HttpRequestAsync.multipartOld;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class UsersOperator {

    public static void register(UserEntity user, final OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserJoinUrl);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("type", user.type);
        if (user.tel != null)
            params.addParam("tel", user.tel);
        if (user.name != null)
            params.addParam("name", user.name);
        if (user.garage != null)
            params.addParam("garage", user.garage);
        if (user.ride != null)
            params.addParam("ride", user.ride);
        if (user.passengers != null)
            params.addParam("passengers", user.passengers);
        if (user.capacity != null)
            params.addParam("capacity", user.capacity);
        if (user.car_number != null)
            params.addParam("car_number", user.car_number);
        if (user.product != null)
            params.addParam("product", user.product);
        if (user.sectors != null)
            params.addParam("sectors", user.sectors);
        if (user.locale != null)
            params.addParam("locale", user.locale);
        if (user.addr != null)
            params.addParam("addr", user.addr);
        if (user.manager_name != null)
            params.addParam("manager_name", user.manager_name);
        if (user.manager_tel != null)
            params.addParam("manager_tel", user.manager_tel);
        if (user.contact != null)
            params.addParam("contact", user.contact);
        if (user.usage != null)
            params.addParam("usage", user.usage);
        if (user.menu != null)
            params.addParam("menu", user.menu);
        if (user.capacity_copy1 != null)
            params.addParam("capacity_copy1", user.capacity_copy1);
        if (user.time != null)
            params.addParam("time", user.time);
        if (user.closed != null)
            params.addParam("closed", user.closed);
        if (user.parking != null)
            params.addParam("parking", user.parking);
        if (user.booking != null)
            params.addParam("booking", user.booking);
        if (user.detail != null)
            params.addParam("detail", user.detail);

        excute(params, listener);
    }

    public static void login(UserEntity user, OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserLoginUrl);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("tel", user.tel);

        excute(params, listener);
    }

    public static void registerBus(UserEntity user, File profileFile, List<File> photoList, final OnCompleteListener listener, Context context) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserJoinBusUrl);
        params.setMethods(HttpRequestParams.Methods.Post);

        params.addPart("type", String.valueOf(user.type));
        if (user.tel != null) params.addPart("tel", user.tel);
        if (user.name != null) params.addPart("name", user.name);
        if (user.garage != null) params.addPart("garage", user.garage);
        if (user.ride != null) params.addPart("ride", user.ride);
        if (user.company_name != null) params.addPart("company_name", user.company_name);
        if (user.passengers != null) params.addPart("passengers", String.valueOf(user.passengers));
        if (user.possible_date != null) params.addPart("possible_date", user.possible_date);
        if (user.car_number != null) params.addPart("car_number", user.car_number);

        if (user.capacity != null) params.addPart("capacity", String.valueOf(user.capacity));
        if (user.product != null) params.addPart("product", user.product);
        if (user.sectors != null) params.addPart("sectors", String.valueOf(user.sectors));
        if (user.locale != null) params.addPart("locale", String.valueOf(user.locale));
        if (user.addr != null) params.addPart("addr", user.addr);
        if (user.manager_name != null) params.addPart("manager_name", user.manager_name);
        if (user.manager_tel != null) params.addPart("manager_tel", user.manager_tel);
        if (user.contact != null) params.addPart("contact", user.contact);
        if (user.usage != null) params.addPart("usage", user.usage);
        if (user.menu != null) params.addPart("menu", user.menu);
        if (user.time != null) params.addPart("time", user.time);
        if (user.closed != null) params.addPart("closed", user.closed);
        if (user.parking != null) params.addPart("parking", user.parking);
        if (user.booking != null) params.addPart("booking", String.valueOf(user.booking));
        if (user.detail != null) params.addPart("detail", user.detail);

        if (profileFile != null && profileFile.exists()) params.addPart("profile", profileFile);

        if (photoList != null && !photoList.isEmpty()) {
            for (int i = 0; i < photoList.size(); i++) {
                params.addPart("file" + i, photoList.get(i));
            }
        }
        HttpRequestAsync.multipart(params, listener, context);
    }

    public static void registerCo(UserEntity user, File profileFile, List<File> photoList, final OnCompleteListener listener, Context context) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserJoinCoUrl);
        params.setMethods(HttpRequestParams.Methods.Post);

        params.addPart("user", new Gson().toJson(user));

        if (profileFile != null && profileFile.exists()) params.addPart("profile", profileFile);

        if (photoList != null && !photoList.isEmpty()) {
            for (int i = 0; i < photoList.size(); i++) {
                params.addPart("file" + i, photoList.get(i));
            }
        }
        HttpRequestAsync.multipart(params, listener, context);
    }

    public static void getUserInfo(long uid, OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserInfoUrl);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("uid", uid);

        excute(params, listener);
    }

    public static void getCoUserList(int sector, String locale, OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUserInfoUrl);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("sector", sector);
        params.addParam("locale", locale);

        excute(params, listener);
    }


/*
    public static void updateFmsToken(String token) {
        UserEntity user = SharedPreferencesCache.getCurrentUser();
        if (user.id <= 0) return;

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUpdateFmsToken);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("id", user.id);
        params.addParam("fms_token", token);
        params.addParam("user_phone_number", user.user_phone_number);
        params.addParam("user_device", user.user_device);

        HttpRequestAsync.excute(params, new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                if (result == null || result.equals("")) {
                    onError(ResponseCodes.CodeUnknownException, result);
                    return;
                }

                Gson gson = new Gson();
                ResponseMessageEntity res = gson.fromJson(result, ResponseMessageEntity.class);
                if (res == null || res.code != ResponseCodes.CodeOk) {
                    onError(ResponseCodes.CodeUnknownException, result);
                    return;
                }
            }

            @Override
            public void onFail(String result) {
            }

            @Override
            public void onError(int code, String result) {
            }
        });
    }*/
/*
    public static void updateNickname(String nickname, final HttpRequestAsync.OnCompleteListener listener) {
        UserEntity user = SharedPreferencesCache.getCurrentUser();
        if (user.id <= 0) return;

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUpdateNickname);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("uid", user.id);
        params.addParam("nickname", nickname);
        params.addParam("ifkey", user.ifkey);

        HttpRequestAsync.excute(params, listener);
    }*/

    public static void uploadPicture(Context context, File file, final OnCompleteListener listener) {
        UserEntity user = SharedPreferencesCache.getCurrentUser();
        if (user.id <= 0) return;

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUploadUserPicture);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addPart("uid", "" + user.id);
        params.addPart("file", file);

        multipartOld(params, listener, context);
    }

}
