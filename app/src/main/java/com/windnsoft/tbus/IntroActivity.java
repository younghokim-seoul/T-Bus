package com.windnsoft.tbus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.windnsoft.tbus.Common.BaseApplication;
import com.windnsoft.tbus.Common.ImageLoaderInit;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Fragment.RegisterFragment;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.Operator.UsersOperator;
import com.windnsoft.tbus.reciever.NetworkStateReceiver;

import static com.windnsoft.tbus.Http.ResponseCodes.CodeForbidden;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeOk;
import static com.windnsoft.tbus.Http.ResponseCodes.CodeSelectNone;

public class IntroActivity extends Activity {
    String tel;
    final int rEXTERNAL_STORAGE = 1;
    final int rREAD_PHONE_STATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);

        BaseApplication.setUiActivity(this);
        tel = SharedPreferencesCache.getRealPhoneNumber();

//        showJoinFragment();

        if (!NetworkStateReceiver.getInstance().isNetworkAvailable()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
//            alert.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    destroy();
//                }
//            });
            alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            alert.setMessage("인터넷에 연결할 수 없습니다.");
            alert.show();
            return;
        } else if (tel == null || "".equals(tel)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
            alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            alert.setMessage("전화번호를 가져올 수 없습니다.");
            alert.show();
            return;

        } else {
            login();
            ImageLoaderInit.initIfNeeded(this);
        }

    }

    void login() {
        UserEntity user = new UserEntity();
        user.tel = tel;

        UsersOperator.login(user, new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {

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
//                        CheckBox autoLoginCb = (CheckBox) getView().findViewById(R.id.loginAutoLoginCb);
//                        if(autoLoginCb.isChecked()){
//                            SharedPreferencesCache.setData(SharedPreferencesCache.KeyAutoLogin,SharedPreferencesCache.ValueTrue);
//                        } else {
//                            SharedPreferencesCache.setData(SharedPreferencesCache.KeyAutoLogin,SharedPreferencesCache.ValueFalse);
//                        }

                        UserEntity resultUser = gson.fromJson(res.msg, UserEntity.class);
                        if (resultUser == null || resultUser.id == 0) {
                            onError(ResponseCodes.CodeInsertionExist, result);
                            return;
                        }

                        SharedPreferencesCache.setCurrentUser(resultUser);

                        nextActivity();
                        break;

                    case CodeSelectNone:
                        showJoinFragment();
                        break;

                    case CodeForbidden:
                        alert = new AlertDialog.Builder(IntroActivity.this);
                        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        alert.setCancelable(false);
                        alert.setMessage("승인 대기중입니다.");
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
                finish();
            }

            @Override
            public void onError(int code, String result) {
                Toast.makeText(BaseApplication.getApplication().getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }


    private void showJoinFragment() {
        RegisterFragment fragment = RegisterFragment.newInstance();
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.setDismissListener(new DialogDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (getValueForBool(RegisterFragment.KeyResultOk, false)) {
                    nextActivity();
                } else {
                    Toast.makeText(IntroActivity.this, "종료합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        fragment.show(getFragmentManager(), RegisterFragment.TAG);

    }


    void nextActivity() {

        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }


}
