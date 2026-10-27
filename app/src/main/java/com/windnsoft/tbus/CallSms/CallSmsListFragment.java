package com.windnsoft.tbus.CallSms;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CallSmsListFragment extends DialogFragment {
    public static final String TAG = "CallSmsListFragment";

    private List<PhonebookReader.Member> memberList;

    private RecyclerView listView;
    private CallSmsListAdapter adapter;
    private EditText searchEt;

    public static CallSmsListFragment newInstance() {
        CallSmsListFragment fragment = new CallSmsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMemberLists();

        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.callsms_list_fragment, container, false);
        listView = (RecyclerView) view.findViewById(R.id.itemlist_list);

        searchEt = (EditText) view.findViewById(R.id.itemlist_search);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchList();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        view.findViewById(R.id.clCloseBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setListAdapter(memberList);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getMemberLists() {
        try {
            PhonebookReader phonebookReader = new PhonebookReader(getActivity());
            phonebookReader.needMobilePhone();
            phonebookReader.flatNumber();
            memberList = phonebookReader.getMemberList();
            Collections.sort(memberList);
        } catch (SecurityException e) {
            memberList = new ArrayList<>();
        }
    }

    private void setListAdapter(List<PhonebookReader.Member> list) {

        try {
            adapter = new CallSmsListAdapter(getActivity(), list);
            adapter.setListener(new OnClickMemberListener() {
                @Override
                public void onClick(PhonebookReader.Member member) {

//                    CallSmsConfirmDialog dialog = new CallSmsConfirmDialog();
//                    dialog.setMember(member);
//                    dialog.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
//                    dialog.show(getFragmentManager(), CallSmsConfirmDialog.TAG);

                    UserEntity user = SharedPreferencesCache.getCurrentUser();
                    String link = "http://play.google.com/store/apps/details?id="+getActivity().getPackageName();
                    String message = "[" + getString(R.string.app_name) + "]\n" + user.name + " 님의 초대입니다 \n" +link;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + member.mobilePhone));

                    intent.putExtra("sms_body", message);
                    startActivity(intent);

                    dismiss();
                }
            });
        } catch (IllegalArgumentException e) {
            WsLog.w("Channel list is null");
        }
        listView.setAdapter(adapter);
//        listView.invalidate();
    }

    private void searchList() {
        if (memberList == null) return;
        List<PhonebookReader.Member> list = new ArrayList();
        String str = searchEt.getText().toString();
        String regex = ".*(" + str + ").*";
        for (PhonebookReader.Member member : memberList) {
            if (String.valueOf(member.name).matches(regex) || String.valueOf(member.mobilePhone).matches(regex)) {
                list.add(member);
            }
        }
        adapter.setList(list);
    }

    private void showIme() {
        EditText editText = (EditText) getView().findViewById(R.id.itemlist_search);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(editText.getWindowToken(), InputMethodManager.SHOW_FORCED);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

}
