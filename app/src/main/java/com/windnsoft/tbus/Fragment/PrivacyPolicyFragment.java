package com.windnsoft.tbus.Fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.R;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class PrivacyPolicyFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "PersonalInformationAgreementFragment";

    DialogInterface.OnDismissListener listener;

    TextView tvPrivacyPolicyAgreement;
    Button btnAgree, btnCancel;
    boolean readOnly;


    public PrivacyPolicyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_privacy_policy, container, false);

        UiInit(view);
        readOnly = getArguments() != null && getArguments().getBoolean("one button");
        btnCancel.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        btnAgree.setText(readOnly ? R.string.btn_ok : R.string.btn_agree);
        UiListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);

            Rect availableBounds = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(availableBounds);
            float density = getResources().getDisplayMetrics().density;
            int width = Math.min(Math.round(availableBounds.width() * 0.9f), Math.round(600 * density));
            int height = Math.min(Math.round(availableBounds.height() * 0.85f), Math.round(720 * density));
            window.setLayout(width, height);
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
        try (InputStreamReader reader = new InputStreamReader(
                context.getAssets().open("personal_information_agreement.txt"), StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                content.append(buffer, 0, count);
            }
            String text = content.toString();
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            String title = getString(R.string.ppTitle);
            if (text.startsWith(title)) {
                text = text.substring(title.length()).trim();
            }
            tvPrivacyPolicyAgreement.setText(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnCancel.getId()) {
            if (!readOnly) {
                SharedPreferencesCache.setData(SharedPreferencesCache.KeyUserAgreement, "");
            }
            dismiss();
        } else if (v.getId() == btnAgree.getId()) {
            if (!readOnly) {
                SharedPreferencesCache.setData(SharedPreferencesCache.KeyUserAgreement, "yes");
            }
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
