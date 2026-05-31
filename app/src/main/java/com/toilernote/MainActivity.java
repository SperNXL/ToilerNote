package com.toilernote;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.toilernote.adapter.ViewPagerAdapter;
import com.toilernote.databinding.ActivityMainBinding;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_attendance) {
                binding.viewPager.setCurrentItem(0, false);
                updateHeader(getString(R.string.attendance), getCurrentMonthText());
                return true;
            } else if (itemId == R.id.nav_statistics) {
                binding.viewPager.setCurrentItem(1, false);
                updateHeader(getString(R.string.statistics), getString(R.string.data_insight));
                return true;
            } else if (itemId == R.id.nav_settings) {
                binding.viewPager.setCurrentItem(2, false);
                updateHeader(getString(R.string.settings), getString(R.string.preference_data));
                return true;
            }
            // TODO: 2026/5/31 统计功能
//            else if (itemId == R.id.nav_statistics) {
//                binding.viewPager.setCurrentItem(1, false);
//                updateHeader(getString(R.string.statistics), getString(R.string.data_insight));
//                return true;
//            }
            return false;
        });

        updateHeader(getString(R.string.attendance), getCurrentMonthText());
    }

    private void updateHeader(String title, String subtitle) {
        binding.tvPageTitle.setText(title);
        binding.tvPageSub.setText(subtitle);
    }

    private String getCurrentMonthText() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%d年%d月",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
