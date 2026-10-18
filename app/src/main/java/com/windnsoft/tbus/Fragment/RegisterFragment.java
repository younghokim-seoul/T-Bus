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

import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.R;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class RegisterFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "JoinFragment";

    public final static String KeyResultOk = "KeyResultOk";

    DialogDismissListener onDismissListener;

    public RegisterFragment() {
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
//            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_join, container, false);
        view.findViewById(R.id.joinCancelBt).setOnClickListener(this);
        view.findViewById(R.id.joinNormalBt).setOnClickListener(this);
        view.findViewById(R.id.joinBusBt).setOnClickListener(this);
        view.findViewById(R.id.joinCoBt).setOnClickListener(this);

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
                getActivity().finish();
                break;
            case R.id.joinNormalBt:
                RegisterNormalFragment fragment = RegisterNormalFragment.newInstance();
                fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                fragment.show(getFragmentManager(), RegisterNormalFragment.TAG);
                break;
            case R.id.joinBusBt:
                RegisterBusFragment busFragment = RegisterBusFragment.newInstance();
                busFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                busFragment.show(getFragmentManager(), RegisterBusFragment.TAG);
                break;
            case R.id.joinCoBt:
                RegisterCoFragment compFragment = RegisterCoFragment.newInstance();
                compFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                compFragment.show(getFragmentManager(), RegisterCoFragment.TAG);
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


    public void setDismissListener(DialogDismissListener listener) {
        onDismissListener = listener;
    }

}
