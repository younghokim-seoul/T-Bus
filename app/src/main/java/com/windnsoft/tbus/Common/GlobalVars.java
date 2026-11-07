package com.windnsoft.tbus.Common;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class GlobalVars {
    public final static int TrueOn = 1;
    public final static int FalseOff = 0;

    public final static String Protocol = "http";
    public final static String Domain = "3.37.185.61:8080";

    public final static int SocketChatPort = 9056;
    public final static int SocketRecievePort = 9076;
    public final static int SocketTalkPort = 9061;


    final static String PathRoot = "TBusWebapp2";
    public final static String PathUserLoginUrl = PathRoot+"/servlet/users/login";
    public final static String PathUserJoinUrl = PathRoot+"/servlet/users/join";
    public final static String PathUserJoinBusUrl = PathRoot+"/servlet/users/joinBus";
    public final static String PathUserJoinCoUrl = PathRoot+"/servlet/users/joinCo";
    public final static String PathUserInfoUrl = PathRoot+"/servlet/users/getInfo";
    public final static String PathUserListUrl = PathRoot+"/servlet/users/getUserList";
    public final static String PathCoListUrl = PathRoot+"/servlet/users/getCoList";
    public final static String PathGetLatLngListUrl = PathRoot+"/servlet/users/getLatLngList";
    public final static String PathUpdateMemberUrl = PathRoot+"/servlet/users/updateMember";
    public final static String PathUpdateProfilePictureUrl = PathRoot+"/servlet/users/updateProfilePicture";
    public final static String PathInsertUserPhotoUrl = PathRoot+"/servlet/users/insertUserPhoto";
    public final static String PathRemoveUserPhotoUrl = PathRoot+"/servlet/users/removeUserPhoto";

    public final static String PathUploadUserPicture = PathRoot+"/servlet/users/UploadPicture";
    public final static String PathUploadBoard = PathRoot+"/servlet/board/Upload";
    public final static String PathUploadBusEstimate = PathRoot+"/servlet/board/UploadBusEstimate";
    public final static String PathUploadMountain = PathRoot+"/servlet/board/UploadMountain";

    public final static String PathNoticeList = PathRoot+"/servlet/notice/getList";
    public final static String PathBoardList = PathRoot+"/servlet/board/getList";
    public final static String PathBusEstimateList = PathRoot+"/servlet/board/getBusEstimateList";
    public final static String PathMountainList = PathRoot+"/servlet/board/getMountainList";
    public final static String PathMountainBookStateChange = PathRoot+"/servlet/board/updateMountainState";
    public final static String PathBoardMyList = PathRoot+"/servlet/board/getMyList";
    public final static String PathReplyInsert = PathRoot+"/servlet/reply/insert";
    public final static String PathReplyList = PathRoot+"/servlet/reply/getList";


    public final static String PathPicturePath = "tbuspic";
    public final static String PathAdPictureFolder = "adpic";


    public static String GetPictureFullPath(String filename) {
        return Protocol + "://" + Domain + "/" + PathPicturePath + "/" + filename;
    }

    public static String GetAdFullPath(String filename) {
        return GetPictureFullPath(PathAdPictureFolder + "/" + filename);
    }
    public final static String WalieTaliePackageName = "com.windnsoft.smartwalkietalkie";
    public final static String WalieTalieOneStoreUrl = "https://onesto.re/0000738045";
}
