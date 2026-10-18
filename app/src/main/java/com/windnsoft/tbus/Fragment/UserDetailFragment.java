package com.windnsoft.tbus.Fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Common.ImageLoaderInit;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.TakeFromCameraAndGallery;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.Operator.UsersOperator;
import com.windnsoft.tbus.R;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;

import static com.windnsoft.tbus.Http.HttpRequestAsync.OnCompleteListener;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class UserDetailFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "UserViewFragment";

    public final static String KeyResultOk = "KeyResultOk";

    int uid;
    UserEntity userEntity;

    DialogDismissListener onDismissListener;
    TakeFromCameraAndGallery mCameraPicture;

    public UserDetailFragment() {
    }

    public static UserDetailFragment newInstance(int uid) {
        UserDetailFragment fragment = new UserDetailFragment();
        fragment.uid = uid;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        loadUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_info_view, container, false);
        view.findViewById(R.id.userPhoneBtn).setOnClickListener(this);
        view.findViewById(R.id.topCloseBtn).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userPhoneBtn:
                if (userEntity != null)
                    WsUtils.phoneCall(getActivity(), userEntity.tel);
                break;
            case R.id.topCloseBtn:
                dismiss();
                break;
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onPause() {
        super.onPause();
        WsUtils.hideKeyboard(getActivity(), getView());

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
        super.onDismiss(dialog);
    }

    public void setDismissListener(DialogDismissListener listener) {
        onDismissListener = listener;
    }

    void loadUserInfo() {
        OnCompleteListener listener = new OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
//                WsLog.v(result);
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(result));
                reader.setLenient(true);
                ResponseMessageEntity response = gson.fromJson(reader, ResponseMessageEntity.class);

                if (response.code != ResponseCodes.CodeOk) {
                    onFail(response.msg);
                    return;
                }

                try {
                    reader = new JsonReader(new StringReader(response.msg));
                    reader.setLenient(true);
                    UserEntity user = gson.fromJson(reader, UserEntity.class);

                    if (user.photo != null && !user.photo.isEmpty()) {
                        Type listType = new TypeToken<List<String>>() {
                        }.getType();
                        reader = new JsonReader(new StringReader(user.photo));
                        reader.setLenient(true);
                        user.photoList = (List<String>) gson.fromJson(reader, listType);
                    }
                    invalidateData(user);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "data error.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }
            }

            @Override
            public void onFail(String result) {
                Toast.makeText(getActivity(), "internet error.", Toast.LENGTH_SHORT).show();
                WsLog.e(result);
            }

            @Override
            public void onError(int code, String result) {
                WsLog.e(result);
                Toast.makeText(getActivity(), "인터넷에 문제가 있습니다.", Toast.LENGTH_SHORT).show();

            }
        };

        UsersOperator.getUserInfo(uid, listener);
    }

    void invalidateData(UserEntity userEntity) {
        if (userEntity == null) return;
        this.userEntity = userEntity;
        ((TextView) getView().findViewById(R.id.userPhoneTxt)).setText(userEntity.tel);
        ((TextView) getView().findViewById(R.id.userAddrTxt)).setText(userEntity.ride);
        ((TextView) getView().findViewById(R.id.userCompTxt)).setText(userEntity.company_name);
        ((TextView) getView().findViewById(R.id.userNameTxt)).setText(userEntity.name);
        ImageLoaderInit.initIfNeeded(getActivity());
        if (userEntity.profile != null && !userEntity.profile.isEmpty()) {
            ImageLoader.getInstance().displayImage(GlobalVars.GetPictureFullPath(userEntity.profile), (ImageView) getView().findViewById(R.id.userProfileImg));

        }
        if (userEntity.photoList != null && !userEntity.photoList.isEmpty()) {
            LinearLayout photoLO = (LinearLayout) getView().findViewById(R.id.userPhotoLo);
            for (String img : userEntity.photoList) {
                ImageView iv = new ImageView(getActivity());
                photoLO.addView(iv);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                iv.setLayoutParams(params);
                DisplayImageOptions ip = new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();
                ImageLoader.getInstance().displayImage(GlobalVars.GetPictureFullPath(img), iv, ip);

            }
        }
    }

}
