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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.BoardIDs;
import com.windnsoft.tbus.Entities.BoardItemEntity;
import com.windnsoft.tbus.Entities.NoticeEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.Http.HttpRequestAsync;
import com.windnsoft.tbus.Http.HttpRequestParams;
import com.windnsoft.tbus.Http.ResponseCodes;
import com.windnsoft.tbus.Http.ResponseMessageEntity;
import com.windnsoft.tbus.R;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.windnsoft.tbus.Common.GlobalVars.GetPictureFullPath;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class BoardListView extends RecyclerView {
    public static final String TAG = "ListBoard";

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private int bid;
    private List<BoardItemEntity> list;
    private ListAdapter adapter;
    private List<NoticeEntity> noticeEntityList;

    public void setBoardId(int bid) {
        this.bid = bid;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }


    public void loadList() {
        if (bid == BoardIDs.Free) {
            loadNotice();
        }
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
//                WsLog.v(result);
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(result));
                reader.setLenient(true);
                ResponseMessageEntity response = gson.fromJson(reader, ResponseMessageEntity.class);
                if (response.code != ResponseCodes.CodeOk) {
                    onFail(response.msg);
                    return;
                }

                Type listType = new TypeToken<List<BoardItemEntity>>() {
                }.getType();
                List<BoardItemEntity> entityList = (List<BoardItemEntity>) gson.fromJson(response.msg, listType);

                for (BoardItemEntity entity : entityList) {
                    if (entity.photos != null && !entity.photos.equals("")) {
                        if (entity.photo == null) entity.photo = new ArrayList<>();
                        listType = new TypeToken<List<String>>() {
                        }.getType();
                        List<String> photo = (List<String>) gson.fromJson(entity.photos, listType);
                        entity.photo = photo;
                    }
                    if (entity.time != null)
                        entity.time = WsUtils.formatBoardDate(entity.time);
                }
                sortListIfNeeded(entityList);
                setList(entityList);
//                WsLog.i(entityList.get(0).photo.get(0));
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

        WsLog.e("" + bid);
        HttpRequestParams params = HttpRequestParams.getInstance();
        params.setMethods(HttpRequestParams.Methods.Post);
        switch (bid) {
            case BoardIDs.My:
                params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathBoardMyList);
                params.addParam("uid", "" + SharedPreferencesCache.getCurrentUser().id);

                break;
            default:
                params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathBoardList);
                params.addParam("bid", "" + this.bid);
                break;
        }
        HttpRequestAsync.excute(params, listener);
    }

    void loadNotice() {
        HttpRequestAsync.OnCompleteListener listener = new HttpRequestAsync.OnCompleteListener() {
            @Override
            public void onSuccess(String result) {
//                WsLog.v(result);
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(result));
                reader.setLenient(true);
                ResponseMessageEntity response = gson.fromJson(reader, ResponseMessageEntity.class);
                if (response.code != ResponseCodes.CodeOk) {
                    onFail(response.msg);
                    return;
                }

                Type listType = new TypeToken<List<NoticeEntity>>() {
                }.getType();
                noticeEntityList = (List<NoticeEntity>) gson.fromJson(response.msg, listType);
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
        params.setMethods(HttpRequestParams.Methods.Post);
        params.buildUrl(GlobalVars.Protocol, GlobalVars.Domain, GlobalVars.PathNoticeList);
        params.addParam("bid", "" + this.bid);
        HttpRequestAsync.excute(params, listener);
    }

    public void setList(List<BoardItemEntity> list) {
        this.list = list;
        if (adapter == null) {
            setListAdapter(list);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(llm);
        }

        adapter.setList(list);
    }

    public void setListAdapter(List<BoardItemEntity> list) {
        adapter = new ListAdapter(list);
        setAdapter(adapter);
        invalidate();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView profile;
        public TextView name;
        public TextView time;
        public TextView replyNum;
        public TextView contents;
        public String imagePath;

        public ListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            profile = (ImageView) itemView.findViewById(R.id.cuProfileImg);
            name = (TextView) itemView.findViewById(R.id.lbNameTxt);
            time = (TextView) itemView.findViewById(R.id.lbTimeTxt);
            replyNum = (TextView) itemView.findViewById(R.id.lbReplyTxt);
            contents = (TextView) itemView.findViewById(R.id.lbContentTxt);
        }
    }

    class HeaderHolder extends RecyclerView.ViewHolder {
        public View view;
        public LinearLayout layout;

        public HeaderHolder(View itemView) {
            super(itemView);
            view = itemView;
            layout = (LinearLayout) itemView.findViewById(R.id.ntRootLo);
        }
    }

    public class ListAdapter extends RecyclerView.Adapter {

        private List<BoardItemEntity> items;

        ListAdapter(List<BoardItemEntity> list) {
            this.items = list;
        }

        public void setList(List<BoardItemEntity> list) {
            items = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new HeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_header_layout, parent, false));
            } else if (viewType == TYPE_ITEM) {
                return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list_item, parent, false));
            }
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (viewHolder instanceof ListViewHolder) {
                final BoardItemEntity item = getItem(position);
                final ListViewHolder holder = (ListViewHolder) viewHolder;

                holder.profile.setImageBitmap(null);
                if (item.userProfile != null && !item.userProfile.equals("")) {
                    String imgPath = GetPictureFullPath(item.userProfile);
                    Glide.with(context).load(imgPath).centerCrop().into(holder.profile);
                } else {
                    holder.profile.setImageResource(R.drawable.person);
                }

                holder.time.setText(item.time);
                holder.name.setText(item.userName);
                holder.contents.setText(item.contents);
                holder.view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openItemView(bid, item);
                    }
                });
            } else if (viewHolder instanceof HeaderHolder) {
                HeaderHolder holder = (HeaderHolder) viewHolder;

                if (noticeEntityList != null && !noticeEntityList.isEmpty()) {
                    holder.layout.removeAllViews();
                    for (final NoticeEntity entity : noticeEntityList) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.notice_header_item, null);
                        ((TextView) view.findViewById(R.id.ntTitleTxt)).setText(entity.title);
                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openNoticeView(bid, entity);
                            }
                        });

                        holder.layout.addView(view);
                    }
                } else {
                    holder.layout.setVisibility(GONE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        private BoardItemEntity getItem(int position) {
            if (!items.isEmpty())
                return items.get(position - 1);
            else return null;
        }
    }

    public void openItemView(int boardID, BoardItemEntity entity) {
        if (entity.bid > 0) boardID = entity.bid;
        BoardDetailFragment fragment = BoardDetailFragment.newInstance(boardID, entity);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.setDismissListener(new DialogDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
//                boardListView.loadList();
            }
        });
        fragment.show(((Activity) context).getFragmentManager(), BoardDetailFragment.TAG);

    }

    public void openNoticeView(int boardID, NoticeEntity entity) {
        if (entity.bid > 0) boardID = entity.bid;
        NoticeDetailFragment fragment = NoticeDetailFragment.newInstance(boardID, entity);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        fragment.show(((Activity) context).getFragmentManager(), NoticeDetailFragment.TAG);

    }

    public BoardListView(Context context) {
        super(context);
        this.context = context;
    }

    public BoardListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public BoardListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

    List<BoardItemEntity> sortListIfNeeded(List<BoardItemEntity> list) {
        if (bid == BoardIDs.My) {
            Comparator<BoardItemEntity> comparator = new Comparator<BoardItemEntity>() {
                private final Collator collator = Collator.getInstance();

                @Override
                public int compare(BoardItemEntity entity, BoardItemEntity t1) {
                    return collator.compare(t1.time, entity.time);
                }
            };

            Collections.sort(list, comparator);
        }
        return list;
    }


}
