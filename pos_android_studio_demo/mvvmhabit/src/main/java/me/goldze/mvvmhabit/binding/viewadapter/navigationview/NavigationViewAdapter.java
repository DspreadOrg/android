package me.goldze.mvvmhabit.binding.viewadapter.navigationview;

import android.view.MenuItem;
import androidx.databinding.BindingAdapter;
import com.google.android.material.navigation.NavigationView;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class NavigationViewAdapter {
    @BindingAdapter(value = {"onNavigationItemSelectedCommand"}, requireAll = false)
    public static void setOnNavigationItemSelectedCommand(NavigationView navigationView, final BindingCommand<Integer> command) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (command != null) {
                    command.execute(item.getItemId());
                }
                return true;
            }
        });
    }
}