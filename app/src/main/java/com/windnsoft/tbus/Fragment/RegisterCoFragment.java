package com.windnsoft.tbus.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.List;

import static com.windnsoft.tbus.General.TakeFromCameraAndGallery.RequestCode_Gallery;
import static com.windnsoft.tbus.General.TakeFromCameraAndGallery.RequestCode_GalleryOnly;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeForbidden;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeInsertionExist;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeOk;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class RegisterCoFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "RegisterCopFragment";

    DialogDismissListener onDismissListener;
    TakeFromCameraAndGallery mCameraPicture;
    String profilePhoto;
    ArrayList<File> photoList = new ArrayList<>();

    public RegisterCoFragment() {
    }

    public static RegisterCoFragment newInstance() {
        RegisterCoFragment fragment = new RegisterCoFragment();
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
        View view = inflater.inflate(R.layout.dialog_join_comp, container, false);
        view.findViewById(R.id.joinCancelBt).setOnClickListener(this);
        view.findViewById(R.id.joinRegisterBt).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementCb).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementBt).setOnClickListener(this);
        view.findViewById(R.id.joinProfileBt).setOnClickListener(this);
        view.findViewById(R.id.joinPhotoBt).setOnClickListener(this);
        view.findViewById(R.id.joinTelBtn).setOnClickListener(this);
        view.findViewById(R.id.jcMapSearchBtn).setOnClickListener(this);

        Spinner sectorSpinner = (Spinner) view.findViewById(R.id.jcSectorsSp);
        sectorSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.sector, R.layout.spinner));

        Spinner localeSpinner = (Spinner) view.findViewById(R.id.jcLocale1Sp);
        localeSpinner.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.locale, R.layout.spinner));
        localeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                Spinner spinner = (Spinner) getView().findViewById(R.id.jcLocale2Sp);
                int r = R.array.localeSeoul;
                switch (position) {
                    case 0:
                        r = R.array.localeSeoul;
                        break;
                    case 1:
                        r = R.array.localeBusan;
                        break;
                    case 2:
                        r = R.array.localeDaegu;
                        break;
                    case 3:
                        r = R.array.localeIncheon;
                        break;
                    case 4:
                        r = R.array.localeGwangju;
                        break;
                    case 5:
                        r = R.array.localeDaejeon;
                        break;
                    case 6:
                        r = R.array.localeUlsan;
                        break;
                    case 7:
                        r = R.array.localeGyeonggi;
                        break;
                    case 8:
                        r = R.array.localeGangwon;
                        break;
                    case 9:
                        r = R.array.localeChungcheongbuk;
                        break;
                    case 10:
                        r = R.array.localeChungcheongnam;
                        break;
                    case 11:
                        r = R.array.localeJeollabuk;
                        break;
                    case 12:
                        r = R.array.localeJeollanam;
                        break;
                    case 13:
                        r = R.array.localeGyeongsangbuk;
                        break;
                    case 14:
                        r = R.array.localeGyeongsangnam;
                        break;
                    case 15:
                        r = R.array.localeJeju;
                        break;
                }
                if (spinner != null)
                    spinner.setAdapter(ArrayAdapter.createFromResource(getActivity(), r, R.layout.spinner));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                WsUtils.phoneCall(getActivity(), ((TextView) getView().findViewById(R.id.joinTelBtn)).getText().toString());
                break;
            case R.id.joinPhotoBt:
                insertPhotoFromGallery();
                break;
            case R.id.jcMapSearchBtn:
                searchMapLocation();
                break;
        }
    }

    private void searchMapLocation() {
        try {
            Geocoder geocoder = new Geocoder(getActivity());
            final List<Address> addresses = geocoder.getFromLocationName(
                    ((Spinner) getView().findViewById(R.id.jcLocale1Sp)).getSelectedItem().toString() + " " +
                            ((Spinner) getView().findViewById(R.id.jcLocale2Sp)).getSelectedItem().toString() + " " +
                            ((EditText) getView().findViewById(R.id.jcAddrEd)).getText().toString()
                    ,
                    1);

            if (addresses != null && addresses.size() > 0) {
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.jcMapView);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
                        googleMap.addMarker(markerOptions);

                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                    }
                });
            } else {
                Toast.makeText(getActivity(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "구글맵에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.jcMapView);
        if(fragment != null)
            getFragmentManager().beginTransaction().remove(fragment).commit();
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);
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
        TakeFromCameraAndGallery.runWithMediaPermission(getActivity(), new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RequestCode_GalleryOnly);
            }
        });
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
                } catch (Exception aNFE) {
                    String fallbackImg = mCameraPicture.getTempFile();
                    if (fallbackImg != null && new File(fallbackImg).exists()) {
                        updateProfileImage(fallbackImg);
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case TakeFromCameraAndGallery.RequestCode_Camera:
                Uri resultUriCamera = mCameraPicture.getResultUri(data);
                if (resultUriCamera == null) {
                    return;
                }
                try {
                    mCameraPicture.startIntentCrop(resultUriCamera, null);
                } catch (Exception aNFE) {
                    String fallbackImg = mCameraPicture.getTempFile();
                    if (fallbackImg != null && new File(fallbackImg).exists()) {
                        updateProfileImage(fallbackImg);
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
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
                } catch (Exception aNFE) {
                    String fallbackImg = mCameraPicture.getTempFile();
                    if (fallbackImg != null && new File(fallbackImg).exists()) {
                        updateProfileImage(fallbackImg);
                    } else {
                        Toast.makeText(getActivity().getBaseContext(), "이미지가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
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

        if (!((CheckBox) getView().findViewById(R.id.joinAgreementCb)).isChecked()) {
            Toast.makeText(getActivity(), "개인정보 취급방침에 동의해야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText jcCompanyNameEd = (EditText) getView().findViewById(R.id.jcCompanyNameEd);
        final EditText jcProductEd = (EditText) getView().findViewById(R.id.jcProductEd);
        final Spinner jcSectorsSp = (Spinner) getView().findViewById(R.id.jcSectorsSp);
        final Spinner jcLocale1Sp = (Spinner) getView().findViewById(R.id.jcLocale1Sp);
        final Spinner jcLocale2Sp = (Spinner) getView().findViewById(R.id.jcLocale2Sp);
        final EditText jcAddrEd = (EditText) getView().findViewById(R.id.jcAddrEd);
        final EditText jcManagerNameEd = (EditText) getView().findViewById(R.id.jcManagerNameEd);
        final EditText jcManagerTelEd = (EditText) getView().findViewById(R.id.jcManagerTelEd);
        final EditText jcContactEd = (EditText) getView().findViewById(R.id.jcContactEd);
        final EditText jcUsageEd = (EditText) getView().findViewById(R.id.jcUsageEd);
        final EditText jcMenuEd = (EditText) getView().findViewById(R.id.jcMenuEd);
        final EditText jcCapacityEd = (EditText) getView().findViewById(R.id.jcCapacityEd);
        final EditText jcTimeEd = (EditText) getView().findViewById(R.id.jcTimeEd);
        final EditText jcClosedEd = (EditText) getView().findViewById(R.id.jcClosedEd);
        final EditText jcParkingEd = (EditText) getView().findViewById(R.id.jcParkingEd);
        final EditText jcDetailEd = (EditText) getView().findViewById(R.id.jcDetailEd);

        if (toastIfTextEmpty(jcCompanyNameEd)) return;
        if (toastIfTextEmpty(jcProductEd)) return;
        if (toastIfTextEmpty(jcAddrEd)) return;
        if (toastIfTextEmpty(jcManagerNameEd)) return;
        if (toastIfTextEmpty(jcManagerTelEd)) return;
        if (toastIfTextEmpty(jcContactEd)) return;
        if (toastIfTextEmpty(jcMenuEd)) return;
        if (toastIfTextEmpty(jcTimeEd)) return;
        if (toastIfTextEmpty(jcClosedEd)) return;
        if (toastIfTextEmpty(jcUsageEd)) return;
        if (toastIfTextEmpty(jcCapacityEd)) return;
        if (toastIfTextEmpty(jcParkingEd)) return;
        if (toastIfTextEmpty(jcDetailEd)) return;

        String selectedSubLocale = jcLocale2Sp.getSelectedItem() != null ? jcLocale2Sp.getSelectedItem().toString().trim() : "";
        if (selectedSubLocale.isEmpty() || selectedSubLocale.contains("전체")) {
            Toast.makeText(getActivity(), "세부 지역을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        final UserEntity user = new UserEntity();
        user.type = UserTypes.Co;
        user.tel = SharedPreferencesCache.getRealPhoneNumber();
        user.company_name = jcCompanyNameEd.getText().toString().trim();
        user.product = jcProductEd.getText().toString().trim();
        user.sectors = jcSectorsSp.getSelectedItem().toString().trim();
        user.locale = jcLocale1Sp.getSelectedItem().toString().trim() + " " + jcLocale2Sp.getSelectedItem().toString().trim();
        user.addr = jcAddrEd.getText().toString().trim();
        user.manager_name = jcManagerNameEd.getText().toString().trim();
        user.manager_tel = jcManagerTelEd.getText().toString().trim();
        user.contact = jcContactEd.getText().toString().trim();
        user.usage = jcUsageEd.getText().toString().trim();
        user.menu = jcMenuEd.getText().toString().trim();
        user.capacity = Integer.parseInt(jcCapacityEd.getText().toString().trim());
        user.time = jcTimeEd.getText().toString().trim();
        user.closed = jcClosedEd.getText().toString().trim();
        user.parking = jcParkingEd.getText().toString().trim();
        user.booking = ((CheckBox) getView().findViewById(R.id.jcBookingCk)).isChecked();
        user.detail = jcDetailEd.getText().toString().trim();
        user.name = user.manager_name;

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

        UsersOperator.registerCo(user, profileFile, photoList, listener, getActivity());
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
