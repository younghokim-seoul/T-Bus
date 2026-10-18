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
import android.widget.Button;
import android.widget.TextView;

import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.R;

import java.io.IOException;
import java.io.InputStream;


public class PrivacyPolicyFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "PersonalInformationAgreementFragment";

    DialogInterface.OnDismissListener listener;

    TextView tvPrivacyPolicyAgreement;
    Button btnAgree, btnCancel;


    public PrivacyPolicyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_privacy_policy, container, false);

        if (getArguments() != null) {
            boolean oneButton = getArguments().getBoolean("one button");
            if (oneButton) {
                view.findViewById(R.id.agreeCancelBt).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.agreeAgreeBt)).setText(getString(R.string.btn_ok));
            }
        }

        UiInit(view);
        UiListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InitData(getActivity());
    }

    void UiInit(View v) {
        tvPrivacyPolicyAgreement = (TextView) v.findViewById(R.id.tvPersonalInformationAgreement);
        btnAgree = (Button) v.findViewById(R.id.agreeAgreeBt);
        btnCancel = (Button) v.findViewById(R.id.agreeCancelBt);
    }

    void UiListener() {
        btnAgree.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    void InitData(Context context) {
        try {
            InputStream is = context.getAssets().open("personal_information_agreement.txt");

            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String text = new String(buffer);

            tvPrivacyPolicyAgreement.setText(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnCancel.getId()) {
            SharedPreferencesCache.setData(SharedPreferencesCache.KeyUserAgreement, "");
            dismiss();
        } else if (v.getId() == btnAgree.getId()) {
            SharedPreferencesCache.setData(SharedPreferencesCache.KeyUserAgreement, "yes");
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (listener != null) {
            listener.onDismiss(getDialog());
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.listener = listener;
    }


}
