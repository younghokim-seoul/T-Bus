package com.windnsoft.tbus.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.windnsoft.tbus.Common.BaseApplication;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Entities.UserTypes;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.TakeFromCameraAndGallery;
import com.windnsoft.tbus.General.WsBitmapTools;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.Operator.UsersOperator;
import com.windnsoft.tbus.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.windnsoft.tbus.General.TakeFromCameraAndGallery.RequestCode_Gallery;
import static com.windnsoft.tbus.General.TakeFromCameraAndGallery.RequestCode_GalleryOnly;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeForbidden;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeInsertionExist;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeOk;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class RegisterBusFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "JoinNormFragment";

    DialogDismissListener onDismissListener;
    TakeFromCameraAndGallery mCameraPicture;
    String profilePhoto;
    ArrayList<File> photoList = new ArrayList<>();

    public RegisterBusFragment() {
    }

    public static RegisterBusFragment newInstance() {
        RegisterBusFragment fragment = new RegisterBusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCameraPicture = new TakeFromCameraAndGallery(this);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
//            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_join_bus, container, false);
        view.findViewById(R.id.joinCancelBt).setOnClickListener(this);
        view.findViewById(R.id.joinRegisterBt).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementCb).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementBt).setOnClickListener(this);
        view.findViewById(R.id.joinProfileBt).setOnClickListener(this);
        view.findViewById(R.id.joinPhotoBt).setOnClickListener(this);
        view.findViewById(R.id.joinTelBtn).setOnClickListener(this);

        String phone_number = SharedPreferencesCache.getRealPhoneNumber();
        if (phone_number == null || phone_number.equals("")) {
            Toast.makeText(getActivity(), "전화번호를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        if (phone_number != null && !phone_number.equals(""))
            ((EditText) view.findViewById(R.id.jbTelEd)).setText(phone_number);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.joinCancelBt:
                dismiss();
                break;
            case R.id.joinRegisterBt:
                register();
                break;
            case R.id.joinAgreementCb:
            case R.id.joinAgreementBt:
                personalAgreements();
                break;
            case R.id.joinProfileBt:
                getImageFromGallery();
                break;
            case R.id.joinTelBtn:
                WsUtils.phoneCall(getActivity(), ((TextView)getView().findViewById(R.id.joinTelBtn)).getText().toString());
                break;
            case R.id.joinPhotoBt:
                insertPhotoFromGallery();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
        super.onDismiss(dialog);
    }

    void getImageFromGallery() {

        mCameraPicture.setSaveFilePath(mCameraPicture.createTempFile4SaveCrop().toString());
        mCameraPicture.startIntentGallery();
//        mCameraPicture.startIntentGalleryWithCrop();
    }

    void insertPhotoFromGallery() {
        if (photoList.size() >= 4) {
            Toast.makeText(getActivity(), "4장까지 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RequestCode_GalleryOnly);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WsLog.w(requestCode + "???" + resultCode);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case TakeFromCameraAndGallery.RequestCode_GalleryCrop:
            case TakeFromCameraAndGallery.RequestCode_CameraCrop:
                Uri resultUri1 = mCameraPicture.getResultUri(data);
                if (resultUri1 == null) return;
                updateProfileImage(resultUri1.toString());
                break;

            case RequestCode_Gallery:

                try {
                    Uri resultUriGallery = data.getData();
                    if (resultUriGallery == null)
                        resultUriGallery = mCameraPicture.getResultUri(data);
                    else {
                    }

                    mCameraPicture.startIntentCrop(resultUriGallery, new HashMap<String, Integer>() {
                        {
                            put("aspectX", 1);
                            put("aspectY", 1);
                            put("outputX", 100);
                            put("outputY", 100);
                        }
                    });
                } catch (ActivityNotFoundException aNFE) {
                    Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case TakeFromCameraAndGallery.RequestCode_Camera:
                Uri resultUriCamera = mCameraPicture.getResultUri(data);
                if (resultUriCamera == null) {
                    return;
                }
                try {
                    mCameraPicture.startIntentCrop(resultUriCamera, null);
                } catch (ActivityNotFoundException aNFE) {
                    Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case TakeFromCameraAndGallery.RequestCode_Crop:
                String imgUri = mCameraPicture.getTempFile();
                updateProfileImage(imgUri);
                break;

            case RequestCode_GalleryOnly:
                try {
                    Uri resultUriGallery = data.getData();
                    if (resultUriGallery == null)
                        resultUriGallery = mCameraPicture.getResultUri(data);
                    insertImage(resultUriGallery);
                } catch (ActivityNotFoundException aNFE) {
                    Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    void updateProfileImage(String img) {
        this.profilePhoto = img;
        WsBitmapTools.saveBitmap(WsBitmapTools.decodeSampledBitmapFromFile(img, 150, 150), img);
        ImageView iv = (ImageView) getView().findViewById(R.id.joinProfileImg);
        iv.setBackground(BitmapDrawable.createFromPath(img));
    }

    void insertImage(final Uri uri) {
        if (uri == null) {
            Toast.makeText(getActivity(), "올바르지 않은 이미지입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout contentLayout = (LinearLayout) getView().findViewById(R.id.joinPhotoLo);
        ImageView iv = new ImageView(getActivity());
        iv.setAdjustViewBounds(true);
        contentLayout.addView(iv);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        iv.setLayoutParams(params);
        Glide.with(getActivity()).load(uri).into(iv);
        File file = new File(WsUtils.getRealPathFromURI(uri, getActivity()));
        photoList.add(file);
    }

    boolean toastIfTextEmpty(EditText et) {
        boolean ret = false;
        if (et.getText().length() == 0) {
            et.requestFocus();
            Toast.makeText(getActivity(), "입력해주세요", Toast.LENGTH_SHORT).show();
            ret = true;
        }
        return ret;
    }

    void register() {
        if (photoList == null || photoList.isEmpty()) {
            Toast.makeText(getActivity(), "최소 하나이상의 사진을 올려야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText jbGarageEd = (EditText) getView().findViewById(R.id.jbGarageEd);
        final EditText jbRideEd = (EditText) getView().findViewById(R.id.jbRideEd);
        final EditText jbCompanyNameEd = (EditText) getView().findViewById(R.id.jbCompanyNameEd);
        final EditText jbPassangersEd = (EditText) getView().findViewById(R.id.jbPassangersEd);
        final EditText jbNameEd = (EditText) getView().findViewById(R.id.jbNameEd);
        final EditText jbTelEd = (EditText) getView().findViewById(R.id.jbTelEd);
        final EditText jbPossibleDateEd = (EditText) getView().findViewById(R.id.jbPossibleDateEd);
        final EditText jbCarNumberEd = (EditText) getView().findViewById(R.id.jbCarNumberEd);

        if (toastIfTextEmpty(jbGarageEd)) return;
        if (toastIfTextEmpty(jbRideEd)) return;
        if (toastIfTextEmpty(jbCompanyNameEd)) return;
        if (toastIfTextEmpty(jbPassangersEd)) return;
        if (toastIfTextEmpty(jbNameEd)) return;
        if (toastIfTextEmpty(jbTelEd)) return;
        if (toastIfTextEmpty(jbPossibleDateEd)) return;
        if (toastIfTextEmpty(jbCarNumberEd)) return;

        final UserEntity user = new UserEntity();
        user.type = UserTypes.Bus;
        user.garage = jbGarageEd.getText().toString().trim();
        user.ride = jbRideEd.getText().toString().trim();
        user.company_name = jbCompanyNameEd.getText().toString().trim();
        user.passengers = Integer.parseInt(jbPassangersEd.getText().toString().trim());
        user.name = jbNameEd.getText().toString().trim();
        user.tel = SharedPreferencesCache.getRealPhoneNumber();
        user.possible_date = jbPossibleDateEd.getText().toString().trim();
        user.car_number = jbCarNumberEd.getText().toString().trim();

        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                WsLog.e(result);

                if (result == null || result.equals("")) {
                    onError(ResponseCodes.CodeUnknownException, result);
                    return;
                }

                Gson gson = new Gson();
                ResponseMessageEntity res = gson.fromJson(result, ResponseMessageEntity.class);
                if (res == null) {
                    onError(ResponseCodes.CodeUnknownException, result);
                    return;
                }
                AlertDialog.Builder alert;
                switch (res.code) {
                    case CodeOk:
                        UserEntity resultUser = gson.fromJson(res.msg, UserEntity.class);
//                        resultUser.id = Integer.parseInt(res.msg);
                        if (resultUser.id == 0) {
                            onError(CodeInsertionExist, result);
                            return;
                        }
                        SharedPreferencesCache.setCurrentUser(resultUser);

                        alert = new AlertDialog.Builder(getActivity());
                        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dismiss();
                                getActivity().finish();
                            }
                        });
                        alert.setMessage("등록되었습니다.\n앱을 다시 시작해주세요.");
                        alert.show();
                        break;

                    case CodeInsertionExist:
                        alert = new AlertDialog.Builder(getActivity());
                        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dismiss();
//                                phoneEt.requestFocus();
                            }
                        });
                        alert.setMessage(getString(R.string.phonenumberExist));
                        alert.show();
                        break;

                    case CodeForbidden:
                        alert = new AlertDialog.Builder(getActivity());
                        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                dismiss();
                                BaseApplication.destroy();
                            }
                        });
                        alert.setCancelable(false);
                        alert.setMessage("앱을 실행할 수 없습니다.");
                        alert.show();
                        break;

                    default:
                        onError(ResponseCodes.CodeUnknownException, result);
                        return;
                }
            }

            @Override
            public void onFail(String result) {
                Toast.makeText(BaseApplication.getApplication().getApplicationContext(), "잘못된 요청 같습니다.", Toast.LENGTH_LONG).show();
                WsLog.e(result);
            }

            @Override
            public void onError(int code, String result) {
                Toast.makeText(BaseApplication.getApplication().getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
                WsLog.e("" + code + ":" + result);
            }
        };
        File profileFile = null;
        if (profilePhoto != null && !profilePhoto.isEmpty()) profileFile = new File(profilePhoto);

        UsersOperator.registerBus(user, profileFile, photoList, listener, getActivity());
    }

    void personalAgreements() {

        PrivacyPolicyFragment dialog = new PrivacyPolicyFragment();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                String agreement = SharedPreferencesCache.getStringData(SharedPreferencesCache.KeyUserAgreement);
                CheckBox agreementCb = (CheckBox) getView().findViewById(R.id.joinAgreementCb);
                if (agreement != null && agreement.equals("yes")) {
                    agreementCb.setChecked(true);
                    Toast.makeText(getActivity(), "개인정보 보호방침에 동의하셨습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    agreementCb.setChecked(false);
                }
            }
        });
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        dialog.show(getFragmentManager(), PrivacyPolicyFragment.TAG);

    }


}
