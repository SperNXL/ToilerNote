package com.toilernote;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.toilernote.adapter.ViewPagerAdapter;
import com.toilernote.databinding.ActivityMainBinding;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ImageView catPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置状态栏和导航栏颜色跟随应用主题
        int surfaceColor = getColor(R.color.surface);
        getWindow().setStatusBarColor(surfaceColor);
        getWindow().setNavigationBarColor(surfaceColor);

        // 设置状态栏图标明暗（浅色模式=深色图标，深色模式=浅色图标）
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), binding.getRoot());
        boolean isLightMode = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                != android.content.res.Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(isLightMode);
        controller.setAppearanceLightNavigationBars(isLightMode);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // main 只处理左右内边距，上下由 header 和 bottomNav 各自处理
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            // 状态栏区域由 headerContainer 的顶部 padding 填充
            binding.headerContainer.setPadding(
                    binding.headerContainer.getPaddingLeft(),
                    systemBars.top,
                    binding.headerContainer.getPaddingRight(),
                    binding.headerContainer.getPaddingBottom());
            // 导航栏区域由 bottomNav 的底部 padding 填充
            binding.bottomNav.setPadding(
                    binding.bottomNav.getPaddingLeft(),
                    binding.bottomNav.getPaddingTop(),
                    binding.bottomNav.getPaddingRight(),
                    systemBars.bottom);
            return insets;
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        ImageView catPanel = findViewById(R.id.cat_panel);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_attendance) {
                binding.viewPager.setCurrentItem(0, false);
                updateHeader(getString(R.string.attendance), getCurrentMonthText());
                catPanel.setImageResource(R.drawable.cat_panel_2_2);
                return true;
            } else if (itemId == R.id.nav_settings) {
                binding.viewPager.setCurrentItem(2, false);
                updateHeader(getString(R.string.settings), getString(R.string.preference_data));
                catPanel.setImageResource(R.drawable.cat_panel_3_2);
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
