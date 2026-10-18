package com.windnsoft.tbus.Operator;

import android.content.Context;

import com.google.gson.Gson;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.BusEstimateEntity;
import com.windnsoft.tbus.Entities.MountainEntity;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;

import java.io.File;
import java.util.List;

import static com.windnsoft.tbus.Http.HttpRequestAsync.excute;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BoardOperator {

    public static void upload(Context context, int bid, String contents, List<File> files, final HttpRequestAsync.OnCompleteListener listener) {
        UserEntity user = SharedPreferencesCache.getCurrentUser();
        if (user.id <= 0) return;

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUploadBoard);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addPart("uid", "" + user.id);
        params.addPart("bid", "" + bid);
        params.addPart("contents", contents);
//        params.addPart("isFile", String.valueOf(files != null && files.size() > 0));
        if (files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                params.addPart("file"+i, files.get(i));
            }
        }

        HttpRequestAsync.multipart(params, listener, context);
    }

    public static void uploadBusEstimate(BusEstimateEntity estimateEntity, final HttpRequestAsync.OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUploadBusEstimate);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("estimate", new Gson().toJson(estimateEntity));

        excute(params, listener);
    }


    public static void uploadMountain(MountainEntity entity, HttpRequestAsync.OnCompleteListener listener) {
        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathUploadMountain);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("mountain", new Gson().toJson(entity));

        excute(params, listener);
    }
}
