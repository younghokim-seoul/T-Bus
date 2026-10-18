package com.windnsoft.tbus.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.MountainEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Operator.BoardOperator;
import com.windnsoft.tbus.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class MountainWriteFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "MountainWriteFragment";

    DialogDismissListener onDismissListener;
    Calendar calStart = new GregorianCalendar();

    public MountainWriteFragment() {
    }

    public static MountainWriteFragment newInstance(int boardId) {
        MountainWriteFragment fragment = new MountainWriteFragment();
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mountain_write, container, false);
        view.findViewById(R.id.wbCloseBtn).setOnClickListener(this);
        view.findViewById(R.id.wbDoneBtn).setOnClickListener(this);

        view.findViewById(R.id.mwStartDayBtn).setOnClickListener(this);
        view.findViewById(R.id.mwStartTimeBt).setOnClickListener(this);

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

            case R.id.mwStartDayBtn:
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calStart.set(i, i1, i2);
                        ((TextView) getView().findViewById(R.id.mwStartDayBtn)).setText(calStart.get(Calendar.YEAR) + "-" + (calStart.get(Calendar.MONTH) + 1) + "-" + calStart.get(Calendar.DAY_OF_MONTH));
                    }
                }, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.mwStartTimeBt:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        calStart.set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH), i, i1);
                        ((TextView) getView().findViewById(R.id.mwStartTimeBt)).setText(calStart.get(Calendar.HOUR) + ":" + calStart.get(Calendar.MINUTE));

                    }
                }, calStart.get(Calendar.HOUR), calStart.get(Calendar.MINUTE), false).show();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
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


    void upload() {


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

        MountainEntity entity = new MountainEntity();
        entity.uid = SharedPreferencesCache.getCurrentUser().id;
        entity.title = ((EditText) getView().findViewById(R.id.mwTitleEt)).getText().toString().trim();
        entity.start_t = "" + calStart.get(Calendar.YEAR) + "-" + calStart.get(Calendar.MONTH) + "-" + calStart.get(Calendar.DAY_OF_MONTH) + " " + calStart.get(Calendar.HOUR) + ":" + calStart.get(Calendar.MINUTE) + ":00";
        entity.locale = ((EditText) getView().findViewById(R.id.mwStartlocaleEt)).getText().toString().trim();
        entity.tax = ((EditText) getView().findViewById(R.id.mwTaxEt)).getText().toString().trim();
        entity.term = ((EditText) getView().findViewById(R.id.mwTermEt)).getText().toString().trim();

        if (((TextView) getView().findViewById(R.id.mwStartDayBtn)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "출발일을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (((TextView) getView().findViewById(R.id.mwStartTimeBt)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "시간을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (entity.title == null || entity.title.isEmpty()) {
            Toast.makeText(getActivity(), "제목을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (entity.locale == null || entity.locale.isEmpty()) {
            Toast.makeText(getActivity(), "출발지역을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (entity.tax == null || entity.tax.isEmpty()) {
            Toast.makeText(getActivity(), "비용을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (entity.term == null || entity.term.isEmpty()) {
            Toast.makeText(getActivity(), "기간을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        BoardOperator.uploadMountain(entity, listener);
    }

}
