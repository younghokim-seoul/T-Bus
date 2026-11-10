package com.windnsoft.tbus.Fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.BoardIDs;
import com.windnsoft.tbus.Entities.BoardItemEntity;
import com.windnsoft.tbus.Entities.ReplyListItemEntity;
import com.windnsoft.tbus.Entities.UserTypes;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.Operator.ReplayOperator;
import com.windnsoft.tbus.R;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BoardDetailFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "BoardViewFragment";

    public final static String KeyResultOk = "KeyResultOk";

    BoardItemEntity entity;
    BoardIDs.BoardEntity board;
    int bid;

    DialogDismissListener onDismissListener;

    public BoardDetailFragment() {
    }

    public static BoardDetailFragment newInstance(int bid, BoardItemEntity item) {
        BoardDetailFragment fragment = new BoardDetailFragment();
        Bundle args = new Bundle();
        args.putInt("bid", bid);
        fragment.setArguments(args);
        fragment.setEntity(item);
        return fragment;
    }

    public void setEntity(BoardItemEntity item) {
        entity = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (!args.isEmpty()) {
            this.bid = args.getInt("bid");
            board = BoardIDs.BoardMap.get(bid);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        loadReplyList(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_detail, container, false);
        view.findViewById(R.id.bvBackBtn).setOnClickListener(this);
        view.findViewById(R.id.bvReplySendBtn).setOnClickListener(this);

        ((TextView) view.findViewById(R.id.bvTitle)).setText(board.text);
        ((TextView) view.findViewById(R.id.bvWriterNameTxt)).setText(entity.userName);
        ((TextView) view.findViewById(R.id.bvWriterDateTxt)).setText(entity.time);
        WsUtils.setPhoneClickableText(getActivity(), (TextView) view.findViewById(R.id.bvContentsTxt), entity.contents);

        ImageView ivWriter = (ImageView) view.findViewById(R.id.bvWriterImg);
        if (entity.userType == UserTypes.Bus) {
            ivWriter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserDetailFragment fragment = UserDetailFragment.newInstance(entity.uid);
                    fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    fragment.show(getFragmentManager(), UserDetailFragment.TAG);
                }
            });
        }

        if (entity.userProfile != null && !entity.userProfile.isEmpty()) {
            Glide.with(getActivity()).load(GlobalVars.GetPictureFullPath(entity.userProfile)).into(ivWriter);
        }

        if (entity.photo != null && entity.photo.size() > 0) {
            LinearLayout photoLO = (LinearLayout) view.findViewById(R.id.bvPhotosLo);
            for (String img : entity.photo) {
                ImageView iv = new ImageView(getActivity());
                iv.setAdjustViewBounds(true);
                photoLO.addView(iv);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                iv.setLayoutParams(params);
                Glide.with(getActivity()).load(GlobalVars.GetPictureFullPath(img)).into(iv);
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bvBackBtn:
                dismiss();
                break;
            case R.id.bvReplySendBtn:
                sendReply();
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


    void sendReply() {
        final EditText editText = (EditText) getView().findViewById(R.id.bvReplyEt);
        String text = editText.getText().toString();
        if (text == null || text.equals("")) return;

        editText.setText("");
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                editText.clearFocus();
                Toast.makeText(getActivity(), "댓글을 등록했습니다.", Toast.LENGTH_SHORT).show();
                WsUtils.hideKeyboard(getActivity(), editText);
                loadReplyList(true);
            }

            @Override
            public void onFail(String result) {
            }

            @Override
            public void onError(int code, String result) {
            }
        };

        ReplayOperator.insert(bid, SharedPreferencesCache.getCurrentUser().id, entity.id, text, listener);

    }

    void loadReplyList(final boolean scrolldown) {
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
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

                Type listType = new TypeToken<List<ReplyListItemEntity>>() {
                }.getType();
                reader = new JsonReader(new StringReader(response.msg));
                reader.setLenient(true);
                List<ReplyListItemEntity> entityList = (List<ReplyListItemEntity>) gson.fromJson(reader, listType);

//                for (ReplyListItemEntity entity : entityList) {
//                    if (entity.photos != null && !entity.photos.equals("")) {
//                        if (entity.photo == null) entity.photo = new ArrayList<>();
//                        listType = new TypeToken<List<String>>() {
//                        }.getType();
//                        List<String> photo = (List<String>) gson.fromJson(entity.photos, listType);
//                        entity.photo = photo;
//                    }
//                    entity.time = entity.time.substring(5, 16);
//                }
                inflateReply(entityList);
                if (scrolldown) {
                    getView().findViewById(R.id.bvReplyLo).post(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) getView().findViewById(R.id.bvReplyScroll)).fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }

//                WsLog.i(entityList.get(0).photo.get(0));
            }

            @Override
            public void onFail(String result) {
                WsLog.e(result);
            }

            @Override
            public void onError(int code, String result) {
                WsLog.e(result);
            }
        };

        ReplayOperator.getList(board.id, entity.id, listener);

    }

    void inflateReply(List<ReplyListItemEntity> itemList) {
        LinearLayout replayLayout = (LinearLayout) getView().findViewById(R.id.bvReplyLo);
        replayLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (final ReplyListItemEntity entity : itemList) {
            View view = inflater.inflate(R.layout.reply_item, null);
            ((TextView) view.findViewById(R.id.reWriterNameTxt)).setText(entity.user_name);
            WsUtils.setPhoneClickableText(getActivity(), (TextView) view.findViewById(R.id.reBodyTxt), entity.contents);
            ((TextView) view.findViewById(R.id.reTimeTxt)).setText(entity.date.toString().substring(0, 16));

            //TODO
            ImageView profile = (ImageView) view.findViewById(R.id.reProfileImg);
            if (entity.type == UserTypes.Bus) {
                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UserDetailFragment fragment = UserDetailFragment.newInstance(entity.uid);
                        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        fragment.show(getFragmentManager(), UserDetailFragment.TAG);
                    }
                });
            }

            if (entity.user_profile != null && !entity.user_profile.isEmpty()) {
                Glide.with(getActivity()).load(GlobalVars.GetPictureFullPath(entity.user_profile)).into(profile);
            }
            replayLayout.addView(view);

        }
    }


}
