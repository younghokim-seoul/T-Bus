package com.windnsoft.tbus.CallSms;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.windnsoft.tbus.R;

import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class CallSmsListAdapter extends RecyclerView.Adapter {

    private Activity context;
    private List<PhonebookReader.Member> items;
    private OnClickMemberListener listener;

    CallSmsListAdapter(Activity context, List<PhonebookReader.Member> list) {
        this.context = context;
        this.items = list;
    }

    public void setList(List<PhonebookReader.Member> list) {
        items = list;
        notifyDataSetChanged();
    }

    public void setListener(OnClickMemberListener listener){
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.callsms_list_item, parent, false);
        return new CallSmsListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final PhonebookReader.Member member = items.get(position);
        CallSmsListViewHolder holder = (CallSmsListViewHolder) viewHolder;
        holder.name.setText(member.name);
        holder.phone.setText(member.mobilePhone);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
