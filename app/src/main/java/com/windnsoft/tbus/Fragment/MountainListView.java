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
import com.windnsoft.tbus.Entities.MountainEntity;
import com.windnsoft.tbus.Entities.MountainListType;
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

public class MountainListView extends RecyclerView {
    public static final String TAG = "MountainListView";

    private Context context;
    private int bid;
    private List<MountainEntity> list;
    private ListAdapter adapter;
    private int listType = MountainListType.All;

    public void setBoardId(int bid) {
        this.bid = bid;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void loadList(final int listType) {
        this.listType = listType;
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(result));
                reader.setLenient(true);
                ResponseMessageEntity response = gson.fromJson(reader, ResponseMessageEntity.class);

                if (response.code != ResponseCodes.CodeOk) {
                    onFail(response.msg);
                    return;
                }

                Type tokenType = new TypeToken<List<MountainEntity>>() {
                }.getType();
                List<MountainEntity> entityList = (List<MountainEntity>) gson.fromJson(response.msg, tokenType);

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
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathMountainList);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("listType", listType);
        HttpRequestAsync.excute(params, listener);
    }

    public void setList(List<MountainEntity> list) {
        this.list = list;
        if (adapter == null) {
            setListAdapter(list);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(llm);
        }

        adapter.setList(list);
    }

    public void setListAdapter(List<MountainEntity> list) {
        adapter = new ListAdapter(list);
        setAdapter(adapter);
        invalidate();
    }

    public class ListViewHolder extends ViewHolder {
        public View view;
        public ImageView profile;
        public TextView name;
        public TextView book;
        public TextView title;
        public TextView replyNum;
        public TextView contents;

        public ListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            profile = (ImageView) itemView.findViewById(R.id.cuProfileImg);
            name = (TextView) itemView.findViewById(R.id.lbNameTxt);
            book = (TextView) itemView.findViewById(R.id.lbNameExtTxt);
            title = (TextView) itemView.findViewById(R.id.lbTitleTxt);
            replyNum = (TextView) itemView.findViewById(R.id.lbReplyTxt);
            contents = (TextView) itemView.findViewById(R.id.lbContentTxt);
        }
    }

    public class ListAdapter extends Adapter {

        private List<MountainEntity> items;

        ListAdapter(List<MountainEntity> list) {
            this.items = list;
        }

        public void setList(List<MountainEntity> list) {
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
            final MountainEntity item = items.get(position);
            ListViewHolder holder = (ListViewHolder) viewHolder;
//            holder.profile
            holder.title.setText(item.title);
            holder.book.setText(item.state?"예약가능":"예약완료");
            holder.book.setBackground(item.state?getResources().getDrawable(R.drawable.prg_border_blue):getResources().getDrawable(R.drawable.prg_border_orange));
            holder.name.setText(item.userName);
            holder.contents.setText(item.start_t.substring(0,16)+"\n"+item.locale);
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

    public void openItemView(int boardID, MountainEntity entity) {
        MountainDetailFragment fragment = MountainDetailFragment.newInstance(boardID, entity);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.setDismissListener(new DialogDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                loadList(listType);
            }
        });
        fragment.show(((Activity) context).getFragmentManager(), BusEstimateDetailFragment.TAG);

    }


    public MountainListView(Context context) {
        super(context);
        this.context = context;
    }

    public MountainListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MountainListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

}
