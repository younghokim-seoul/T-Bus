package com.windnsoft.tbus.Fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.TakeFromCameraAndGallery;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Operator.BoardOperator;
import com.windnsoft.tbus.R;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BoardWriteFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "WriteboardFragment";

    public final static String KeyResultOk = "KeyResultOk";

    DialogDismissListener onDismissListener;
    TakeFromCameraAndGallery mCameraPicture;

    int bid;
    EditText text;
    LinearLayout contentLayout;
    ArrayList<File> imagesList = new ArrayList<>();

    public BoardWriteFragment() {
    }

    public static BoardWriteFragment newInstance(int bid) {
        BoardWriteFragment fragment = new BoardWriteFragment();
        Bundle args = new Bundle();
        args.putInt("bid", bid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (!args.isEmpty())
            this.bid = args.getInt("bid");
        WsLog.w("bid:" + bid);

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        if (text != null) {
            text.requestFocus();
//            WsUtils.showKeyboard(getActivity(), text);
            text.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(text, 0);
                }
            }, 200);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_write, container, false);
        view.findViewById(R.id.wbCloseBtn).setOnClickListener(this);
        view.findViewById(R.id.wbDoneBtn).setOnClickListener(this);
        view.findViewById(R.id.wbPictureBtn).setOnClickListener(this);
        view.findViewById(R.id.wbTitleBtn).setOnClickListener(this);

        text = (EditText) view.findViewById(R.id.wbContentEt);
        text.requestFocus();
        contentLayout = (LinearLayout) view.findViewById(R.id.wbPictureLo);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wbCloseBtn:

                dismiss();
                break;
            case R.id.wbDoneBtn:
                upload();
                break;
            case R.id.wbPictureBtn:
                callTakeAlbum();
                break;
            case R.id.wbTitleBtn:
                if (text != null)
                    text.requestFocus();
                WsUtils.showKeyboard(getActivity(), text);
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
//        WsUtils.showKeyboard(getActivity());
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

    public void callTakeAlbum() {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, TakeFromCameraAndGallery.RequestCode_Gallery);
    }

    void insertImage(final Uri uri) {
        if (uri == null) {
            Toast.makeText(getActivity(), "올바르지 않은 이미지입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageView iv = new ImageView(getActivity());
        iv.setAdjustViewBounds(true);
        contentLayout.addView(iv);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        iv.setLayoutParams(params);
        Glide.with(getActivity()).load(uri).into(iv);
        File file = new File(WsUtils.getRealPathFromURI(uri, getActivity()));
        imagesList.add(file);
        WsLog.w("file:" + file.getPath());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case TakeFromCameraAndGallery.RequestCode_GalleryCrop:
            case TakeFromCameraAndGallery.RequestCode_CameraCrop:
                Uri resultUri1 = mCameraPicture.getResultUri(data);
                if (resultUri1 == null) return;
                insertImage(resultUri1);
                break;

            case TakeFromCameraAndGallery.RequestCode_Gallery:

                try {
                    Uri resultUriGallery = data.getData();
                    if (resultUriGallery == null)
                        resultUriGallery = mCameraPicture.getResultUri(data);

                    insertImage(resultUriGallery);
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
//                insertImage(imgUri);
                break;
        }
    }

    void upload() {
        if (text.getText().toString()==null || text.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), "내용을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(getActivity(), "등록되었습니다.", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onFail(String result) {
//                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(int code, String result) {
                dismiss();
            }
        };
        BoardOperator.upload(getActivity(), bid, text.getText().toString(), imagesList, listener);
    }

}
