package com.windnsoft.tbus.Fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Entities.BusEstimateEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.R;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BusEstimateListView extends RecyclerView {
    public static final String TAG = "BusEstimateListView";

    private Context context;
    private int bid;
    private List<BusEstimateEntity> list;
    private ListAdapter adapter;

    public void setBoardId(int bid) {
        this.bid = bid;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void loadList() {
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(result));
                reader.setLenient(true);
                ResponseMessageEntity response = gson.fromJson(reader, ResponseMessageEntity.class);

                if (response.code != ResponseCodes.CodeOk) onFail(response.msg);

                Type listType = new TypeToken<List<BusEstimateEntity>>() {
                }.getType();
                List<BusEstimateEntity> entityList = (List<BusEstimateEntity>) gson.fromJson(response.msg, listType);

                setList(entityList);
            }

            @Override
            public void onFail(String result) {
                WsLog.e(result);
            }

            @Override
            public void onError(int code, String result) {
                WsLog.e(result);
            }
        };

        HttpRequestParams params = HttpRequestParams.getInstance();
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathBusEstimateList);
        params.setMethods(HttpRequestParams.Methods.Post);
        HttpRequestAsync.excute(params, listener);
    }

    public void setList(List<BusEstimateEntity> list) {
        this.list = list;
        if (adapter == null) {
            setListAdapter(list);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(llm);
        }

        adapter.setList(list);
    }

    public void setListAdapter(List<BusEstimateEntity> list) {
        adapter = new ListAdapter(list);
        setAdapter(adapter);
        invalidate();
    }

    public class ListViewHolder extends ViewHolder {
        public View view;
        public ImageView profile;
        public TextView name;
        public TextView time;
        public TextView title;
        public TextView replyNum;
        public TextView contents;

        public ListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            profile = (ImageView) itemView.findViewById(R.id.cuProfileImg);
            name = (TextView) itemView.findViewById(R.id.lbNameTxt);
            time = (TextView) itemView.findViewById(R.id.lbTimeTxt);
            title = (TextView) itemView.findViewById(R.id.lbTitleTxt);
            replyNum = (TextView) itemView.findViewById(R.id.lbReplyTxt);
            contents = (TextView) itemView.findViewById(R.id.lbContentTxt);
        }
    }

    public class ListAdapter extends Adapter {

        private List<BusEstimateEntity> items;

        ListAdapter(List<BusEstimateEntity> list) {
            this.items = list;
        }

        public void setList(List<BusEstimateEntity> list) {
            items = list;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list_item, parent, false);
            return new ListViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            final BusEstimateEntity item = items.get(position);
            ListViewHolder holder = (ListViewHolder) viewHolder;
//            holder.profile
            holder.title.setText(item.title);
            holder.name.setText(item.userName);
            String content = "";
            if (item.start_time != null && item.start_time.length() > 16 && item.arrival_time != null && item.arrival_time.length() > 16) {
                content += item.start_time.substring(0, 16) + " ~ " + item.arrival_time.substring(0, 16);
            }
            holder.contents.setText("\n" + content + item.start_locale + " ~ " + item.arrival_locale);
            holder.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    openItemView(bid, item);
                }
            });
            if (item.userProfile != null && !item.userProfile.isEmpty()) {
                Glide.with(context).load(GlobalVars.GetPictureFullPath(item.userProfile)).into(holder.profile);
            } else
                holder.profile.setImageResource(R.drawable.person);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void openItemView(int boardID, BusEstimateEntity entity) {
        BusEstimateDetailFragment fragment = BusEstimateDetailFragment.newInstance(boardID, entity);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.setDismissListener(new DialogDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                loadList();
            }
        });
        fragment.show(((Activity) context).getFragmentManager(), BusEstimateDetailFragment.TAG);

    }


    public BusEstimateListView(Context context) {
        super(context);
        this.context = context;
    }

    public BusEstimateListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public BusEstimateListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

}
