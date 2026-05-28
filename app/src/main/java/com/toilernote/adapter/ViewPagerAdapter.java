package com.toilernote.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.toilernote.ui.AttendanceFragment;
import com.toilernote.ui.SettingsFragment;
import com.toilernote.ui.StatisticsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AttendanceFragment();
            case 1:
                return new StatisticsFragment();
            case 2:
                return new SettingsFragment();
            default:
                return new AttendanceFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
