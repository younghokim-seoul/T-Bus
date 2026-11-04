package com.windnsoft.tbus.CallSms;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.General.ObjectRecursivelyInfo;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.R;


public class CallSmsConfirmDialog extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "CallSmsConfirmDialog";

    TextView smsContentsTxt;
    Button btnAgree, btnCancel;
    PhonebookReader.Member member;
    String message;

    public CallSmsConfirmDialog() {
    }

    public void setMember(PhonebookReader.Member member) {
        this.member = member;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.callsms_confirm_dialog, container, false);

        smsContentsTxt = (TextView) v.findViewById(R.id.callsmsContentsTxt);
        btnAgree = (Button) v.findViewById(R.id.okBtn);
        btnCancel = (Button) v.findViewById(R.id.cancelBtn);
        btnAgree.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        UserEntity user = SharedPreferencesCache.getCurrentUser();
        try {
            WsLog.i(ObjectRecursivelyInfo.toStringRecursive(user));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String nick = user.name;
        message = "[" + getString(R.string.app_name) + "] \n" + user.name + " (" + nick + ") 님의 초대입니다 \n" ;

        smsContentsTxt.setText(message);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnCancel.getId()) {
            dismiss();
            return;
        } else if (v.getId() == btnAgree.getId()) {
            if (member == null || member.mobilePhone == null) {
                dismiss();
                return;
            }
            if (WsUtils.composeSms(getActivity(), member.mobilePhone, message)) {
                dismiss();
            }
        }
    }


}
