package com.windnsoft.tbus.Operator;

import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class ReplayOperator {

    public static void insert(int bid, int uid, long idx, String text, final HttpRequestAsync.OnCompleteListener listener) {

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathReplyInsert);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("bid", bid);
        params.addParam("uid", uid);
        params.addParam("idx", idx);
        params.addParam("text", text);

        HttpRequestAsync.excute(params, listener);
    }

    public static void getList(int bid, long id, HttpRequestAsync.OnCompleteListener listener) {
        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathReplyList);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("bid", "" + bid);
        params.addParam("id", "" + id);
        HttpRequestAsync.excute(params, listener);
    }

}
