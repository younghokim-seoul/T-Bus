package com.windnsoft.tbus.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.windnsoft.tbus.Common.BaseApplication;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Entities.UserTypes;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.Operator.UsersOperator;
import com.windnsoft.tbus.R;

import static com.windnsoft.tbus.Http.ResponseCodes.CodeForbidden;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeInsertionExist;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeOk;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class RegisterNormalFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "JoinNormFragment";

    DialogDismissListener onDismissListener;

    public RegisterNormalFragment() {
    }

    public static RegisterNormalFragment newInstance() {
        RegisterNormalFragment fragment = new RegisterNormalFragment();
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
        View view = inflater.inflate(R.layout.dialog_join_norm, container, false);
        view.findViewById(R.id.joinCancelBt).setOnClickListener(this);
        view.findViewById(R.id.joinRegisterBt).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementCb).setOnClickListener(this);
        view.findViewById(R.id.joinAgreementBt).setOnClickListener(this);

        String phone_number = SharedPreferencesCache.getRealPhoneNumber();
        if (phone_number == null || phone_number.equals(""))
            Toast.makeText(getActivity(), "전화번호를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        if (phone_number != null && !phone_number.equals(""))
            ((EditText) view.findViewById(R.id.joinPhonenumberEt)).setText(phone_number);

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
                join();
                break;
            case R.id.joinAgreementCb:
            case R.id.joinAgreementBt:
                personalAgreements();
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

    void join() {
        final EditText phoneEt = (EditText) getView().findViewById(R.id.joinPhonenumberEt);
        String phonenumber = phoneEt.getText().toString();
        if (phonenumber.length() < 8) {
            Toast.makeText(getActivity(), "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            phoneEt.requestFocus();
            return;
        }

        final EditText nickEt = (EditText) getView().findViewById(R.id.joinNicknameEt);
        String nickname = nickEt.getText().toString();
        if (nickname.length() < 2) {
            Toast.makeText(getActivity(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            nickEt.requestFocus();
            return;
        }

        final CheckBox agreement = (CheckBox) getView().findViewById(R.id.joinAgreementCb);
        if (!agreement.isChecked()) {
            Toast.makeText(getActivity(), "개인정보 보호방침에 동의해야 합니다..", Toast.LENGTH_SHORT).show();
            return;
        }

        final UserEntity user = new UserEntity();
        user.type = UserTypes.Normal;
        user.tel = phonenumber;
        user.name = nickname;
        user.permit = true;

        UsersOperator.register(user, new HttpRequestAsync.OnCompleteListener() {
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
                Toast.makeText(BaseApplication.getApplication().getApplicationContext(), getString(R.string.network_failure), Toast.LENGTH_LONG).show();
                WsLog.e(result);
            }

            @Override
            public void onError(int code, String result) {
                Toast.makeText(BaseApplication.getApplication().getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
                WsLog.e(""+code+":"+result);
            }
        });
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
