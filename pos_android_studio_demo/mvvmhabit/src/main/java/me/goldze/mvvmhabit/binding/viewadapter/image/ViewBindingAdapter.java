package me.goldze.mvvmhabit.binding.viewadapter.image;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class ViewBindingAdapter {
    @BindingAdapter("binding:onClickCommand")
    public static void onClickCommand(android.view.View view, final BindingCommand clickCommand) {
        view.setOnClickListener(v -> {
            if (clickCommand != null) {
                clickCommand.execute();
            }
        });
    }

    @BindingAdapter("android:src")
    public static void setImageBitmap(ImageView view, ObservableField<Bitmap> bitmap) {
        if (bitmap != null && bitmap.get() != null) {
            view.setImageBitmap(bitmap.get());
        }
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView view, int resId) {
        if (resId != 0) {
            view.setImageResource(resId);
        }
    }
}
