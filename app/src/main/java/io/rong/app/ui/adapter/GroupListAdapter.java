package io.rong.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import io.rong.app.App;
import io.rong.app.R;
import io.rong.app.server.pinyin.Group;

/**
 * Created by Bob on 2015/1/31.
 */
public class GroupListAdapter extends android.widget.BaseAdapter implements SectionIndexer{

    private Context context;

    private List<Group> list;

    public GroupListAdapter(Context context) {
        this.context = context;
    }

    public GroupListAdapter(Context context, List<Group> list) {
        this.context = context;
        this.list = list;
    }

    /**
     * 传入新的数据 刷新UI的方法
     */
    public void updateListView(List<Group> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final Group mContent = list.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.group_item, null);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.groupname);
            viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.catalog);
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.groupuri);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //根据position获取分类的首字母的Char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getLetters());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }
        viewHolder.tvTitle.setText(this.list.get(position).getName());
        ImageLoader.getInstance().displayImage(list.get(position).getPortraitUri(),viewHolder.mImageView, App.getOptions());
        return convertView;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == sectionIndex) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return list.get(position).getLetters().charAt(0);
    }


    final static class ViewHolder {
        /**
         * 首字母
         */
        TextView tvLetter;
        /**
         * 昵称
         */
        TextView tvTitle;
        /**
         * 头像
         */
        ImageView mImageView;
        /**
         * userid
         */
//        TextView tvUserId;
    }
}
