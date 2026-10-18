package com.windnsoft.tbus.CallSms;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.windnsoft.tbus.R;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class CallSmsListViewHolder extends RecyclerView.ViewHolder {
    public View view;
    public TextView name;
    public TextView phone;

    public CallSmsListViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        name = (TextView) itemView.findViewById(R.id.smsNameTxt);
        phone = (TextView) itemView.findViewById(R.id.smsPhoneTxt);
    }
}
