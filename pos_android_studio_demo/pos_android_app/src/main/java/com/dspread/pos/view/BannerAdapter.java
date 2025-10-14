package com.dspread.pos.view;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.pos.utils.BannerItem;
import com.dspread.pos_android_app.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder>{
    private final List<BannerItem> bannerList;

    // 构造方法：接收轮播数据
    public BannerAdapter(List<BannerItem> bannerList) {
        this.bannerList = bannerList;
    }

    // 创建ViewHolder（加载轮播项布局）
    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    // 绑定数据到ViewHolder
    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = bannerList.get(position);
        holder.ivBanner.setImageResource(item.getImageResId()); // 设置图片
        holder.tvBanner.setText(Html.fromHtml(item.getText(), Html.FROM_HTML_MODE_COMPACT));
//        holder.tvBanner.setText(item.getText());                 // 设置文本
    }

    // 返回数据总数
    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    // ViewHolder：持有轮播项的控件引用
    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvBanner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner);
            tvBanner = itemView.findViewById(R.id.tv_banner);
        }
    }
}
