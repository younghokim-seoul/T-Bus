package com.windnsoft.tbus.Fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
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
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.R;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class CompanyListView extends RecyclerView {
    public static final String TAG = "CoUserListView";

    private Context context;
    private String sectors;
    private List<UserEntity> list;
    private ListAdapter adapter;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void loadList(String sectors, String locale, final String localeString) {
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

                Type listType = new TypeToken<List<UserEntity>>() {
                }.getType();
                List<UserEntity> entityList = (List<UserEntity>) gson.fromJson(response.msg, listType);

                for (UserEntity entity : entityList) {
                    entity.locale = localeString;
                    if (entity.photo != null && !entity.photo.equals("")) {
                        if (entity.photoList == null) entity.photoList = new ArrayList<>();
                        listType = new TypeToken<List<String>>() {
                        }.getType();
                        List<String> photoList = (List<String>) gson.fromJson(entity.photo, listType);
                        entity.photoList = photoList;
                    }
                }
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
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathCoListUrl);
        params.setMethods(HttpRequestParams.Methods.Post);
        params.addParam("sectors", "" + sectors);
        params.addParam("locale", "" + locale);
        HttpRequestAsync.excute(params, listener);
    }

    public void setList(List<UserEntity> list) {
        this.list = list;
        if (adapter == null) {
            setListAdapter(list);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(llm);
        }

        adapter.setList(list);
    }

    public void setListAdapter(List<UserEntity> list) {
        adapter = new ListAdapter(list);
        setAdapter(adapter);
        invalidate();
    }

    public class ListViewHolder extends ViewHolder {
        public View view;
        public ImageView profile;
        public TextView name;
        public TextView addr;
        public TextView replyNum;
        public TextView contents;

        public ListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            profile = (ImageView) itemView.findViewById(R.id.cuProfileImg);
            name = (TextView) itemView.findViewById(R.id.cuNameTxt);
            addr = (TextView) itemView.findViewById(R.id.cuAddrTxt);
//            replyNum = (TextView) itemView.findViewById(R.id.lbReplyTxt);
            contents = (TextView) itemView.findViewById(R.id.cuBodyTxt);
        }
    }

    public class ListAdapter extends Adapter {

        private List<UserEntity> items;

        ListAdapter(List<UserEntity> list) {
            this.items = list;
        }

        public void setList(List<UserEntity> list) {
            items = list;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_list_item, parent, false);
            return new ListViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            final UserEntity item = items.get(position);
            ListViewHolder holder = (ListViewHolder) viewHolder;
//            holder.profile
            holder.addr.setText(item.addr);
            holder.name.setText(item.company_name);
            holder.contents.setText(item.product);
            holder.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    openItemView(item);
                }
            });
            if (item.profile != null && !item.profile.isEmpty()) {
                Glide.with(context).load(GlobalVars.GetPictureFullPath(item.profile)).into(holder.profile);
            } else {
                holder.profile.setImageResource(R.drawable.person);
            }


        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void openItemView(UserEntity entity) {
        CompanyDetailFragment fragment = CompanyDetailFragment.newInstance(entity);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.show(((Activity) context).getFragmentManager(), CompanyDetailFragment.TAG);
    }


    public CompanyListView(Context context) {
        super(context);
        this.context = context;
    }

    public CompanyListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CompanyListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

}
