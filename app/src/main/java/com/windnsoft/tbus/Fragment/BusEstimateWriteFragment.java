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
import com.windnsoft.tbus.Entities.BusEstimateEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Operator.BoardOperator;
import com.windnsoft.tbus.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BusEstimateWriteFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "WriteBusFragment";

    DialogDismissListener onDismissListener;
    Calendar calStart = new GregorianCalendar();
    Calendar calArrival = new GregorianCalendar();

    public BusEstimateWriteFragment() {
    }

    public static BusEstimateWriteFragment newInstance() {
        BusEstimateWriteFragment fragment = new BusEstimateWriteFragment();
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
        View view = inflater.inflate(R.layout.bus_estimate_write, container, false);
        view.findViewById(R.id.wbCloseBtn).setOnClickListener(this);
        view.findViewById(R.id.wbDoneBtn).setOnClickListener(this);

        view.findViewById(R.id.wbusStartDayBtn).setOnClickListener(this);
        view.findViewById(R.id.wbusStartTimeBt).setOnClickListener(this);
        view.findViewById(R.id.wbusArrivaldayBt).setOnClickListener(this);
        view.findViewById(R.id.wbusArrivaltimeBt).setOnClickListener(this);

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

            case R.id.wbusStartDayBtn:
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calStart.set(i, i1, i2);
                        ((TextView) getView().findViewById(R.id.wbusStartDayBtn)).setText(calStart.get(Calendar.YEAR) + "-" + (calStart.get(Calendar.MONTH) + 1) + "-" + calStart.get(Calendar.DAY_OF_MONTH));
                    }
                }, calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.wbusStartTimeBt:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        calStart.set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH), i, i1);
                        ((TextView) getView().findViewById(R.id.wbusStartTimeBt)).setText(calStart.get(Calendar.HOUR) + ":" + calStart.get(Calendar.MINUTE));

                    }
                }, calStart.get(Calendar.HOUR), calStart.get(Calendar.MINUTE), false).show();
                break;
            case R.id.wbusArrivaldayBt:
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        calArrival.set(i, i1, i2);
                        ((TextView) getView().findViewById(R.id.wbusArrivaldayBt)).setText(calArrival.get(Calendar.YEAR) + "-" + (calArrival.get(Calendar.MONTH) + 1) + "-" + calArrival.get(Calendar.DAY_OF_MONTH));

                    }
                }, calArrival.get(Calendar.YEAR), calArrival.get(Calendar.MONTH), calArrival.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.wbusArrivaltimeBt:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        calArrival.set(calArrival.get(Calendar.YEAR), calArrival.get(Calendar.MONTH), calArrival.get(Calendar.DAY_OF_MONTH), i, i1);
                        ((TextView) getView().findViewById(R.id.wbusArrivaltimeBt)).setText(calArrival.get(Calendar.HOUR) + ":" + calArrival.get(Calendar.MINUTE));
                    }
                }, calArrival.get(Calendar.HOUR), calArrival.get(Calendar.MINUTE), false).show();
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

        BusEstimateEntity estimateEntity = new BusEstimateEntity();
        estimateEntity.uid = SharedPreferencesCache.getCurrentUser().id;
        estimateEntity.title = ((EditText) getView().findViewById(R.id.wbusTitleEt)).getText().toString().trim();
        estimateEntity.start_time = "" + calStart.get(Calendar.YEAR) + "-" + calStart.get(Calendar.MONTH) + "-" + calStart.get(Calendar.DAY_OF_MONTH) + " " + calStart.get(Calendar.HOUR) + ":" + calStart.get(Calendar.MINUTE) + ":00";
//        estimateEntity.start_time = calStart.getTimeInMillis() + "";
        estimateEntity.arrival_time = "" + calArrival.get(Calendar.YEAR) + "-" + calArrival.get(Calendar.MONTH) + "-" + calArrival.get(Calendar.DAY_OF_MONTH) + " " + calArrival.get(Calendar.HOUR) + ":" + calArrival.get(Calendar.MINUTE) + ":00";
//        estimateEntity.arrival_time = calArrival.getTimeInMillis() + "";
        estimateEntity.start_locale = ((EditText) getView().findViewById(R.id.wbusStartlocaleEt)).getText().toString().trim();
        estimateEntity.arrival_locale = ((EditText) getView().findViewById(R.id.wbusArrvallocaleEt)).getText().toString().trim();
        estimateEntity.term = ((EditText) getView().findViewById(R.id.wbusTermEt)).getText().toString().trim();
        estimateEntity.capacity = ((EditText) getView().findViewById(R.id.wbusCapacityEt)).getText().toString().trim();
        estimateEntity.round = ((EditText) getView().findViewById(R.id.wbusRoundEt)).getText().toString().trim();
        estimateEntity.vehicle = ((EditText) getView().findViewById(R.id.wbusVehicleEt)).getText().toString().trim();
        estimateEntity.cars = ((EditText) getView().findViewById(R.id.wbusCarsEt)).getText().toString().trim();

        if (((TextView) getView().findViewById(R.id.wbusStartDayBtn)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "출발일을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (((TextView) getView().findViewById(R.id.wbusStartTimeBt)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "시간을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (((TextView) getView().findViewById(R.id.wbusArrivaldayBt)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "도착일을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (((TextView) getView().findViewById(R.id.wbusArrivaltimeBt)).getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "시간을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.title == null || estimateEntity.title.isEmpty()) {
            Toast.makeText(getActivity(), "제목을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.start_locale == null || estimateEntity.start_locale.isEmpty()) {
            Toast.makeText(getActivity(), "출발지역을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.arrival_locale == null || estimateEntity.arrival_locale.isEmpty()) {
            Toast.makeText(getActivity(), "목적지를 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.term == null || estimateEntity.term.isEmpty()) {
            Toast.makeText(getActivity(), "기간을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.capacity == null || estimateEntity.capacity.isEmpty()) {
            Toast.makeText(getActivity(), "인원을 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (estimateEntity.round == null || estimateEntity.round.isEmpty()) {
            Toast.makeText(getActivity(), "왕복/편도 를 적어주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        BoardOperator.uploadBusEstimate(estimateEntity, listener);
    }

}
