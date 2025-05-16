package me.goldze.mvvmhabit.binding.viewadapter.navigationview;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.drawerlayout.widget.DrawerLayout;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class DrawerLayoutBindingAdapter {

    @BindingAdapter(value = {"onDrawerOpenedCommand", "onDrawerClosedCommand"}, requireAll = false)
    public static void setDrawerListener(final DrawerLayout drawerLayout,
                                         final BindingCommand<View> onDrawerOpenedCommand,
                                         final BindingCommand<View> onDrawerClosedCommand) {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {


            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (onDrawerOpenedCommand != null) {
                    onDrawerOpenedCommand.execute(drawerView);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (onDrawerClosedCommand != null) {
                    onDrawerClosedCommand.execute(drawerView);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // 抽屉状态改变时的回调
            }
        });
    }
}
