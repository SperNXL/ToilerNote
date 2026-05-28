# 🐮 牛马日记

> 解决月底计算工时与加班时长的麻烦，提供可视化考勤管理与月度工时统计。

## 功能简介

「牛马日记」是一款 Android 原生应用，帮助打工人记录每日考勤、自动计算工时与加班时长，并以可视化图表呈现工作强度趋势。

### 核心功能

| 模块 | 功能说明 |
|------|---------|
| **📅 考勤页面** | 月历展示每日状态（上班/休息/请假），支持月份切换，底部实时汇总当月统计数据 |
| **📝 日期编辑** | 点击日期弹出 BottomSheet，可设置上下班时间、休息时长、全天加班、请假时段；支持快捷按钮（+5分/+1h等）和「复制昨日」 |
| **📊 统计页面** | 柱状图展示每日工时与加班分布，折线图展示累计趋势；工作强度预警（连续上班天数、加班占比）；与上月周期对比 |
| **⚙️ 设置页面** | 默认时间、日历颜色（支持 HEX 自定义）、时薪与加班倍数、数据管理、深色模式 |
| **🤖 智能初始化** | 首次启动自动根据「默认工作日」将非工作日标记为休息，省去手动录入 |

### 工时计算规则

- **普通工作日**：实际工时以计划工时为上限，超出部分计入加班（扣除晚上休息）
- **全天加班**：当日全部时间计入加班（仅扣除中间休息）
- **请假日**：从计划工时中扣除请假时段
- **迟到判定**：实际上班时间晚于计划时间即标记迟到，不影响工时计算

---

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| 语言 | Java 11 |
| UI | XML + Material Design Components + ViewBinding |
| 架构 | MVVM + Repository |
| 导航 | BottomNavigationView + ViewPager2 + Fragment |
| 数据库 | Room (SQLite) |
| 图表 | 自定义 View（BarChartView / LineChartView） |
| 最低版本 | Android 8.0 (API 26) |

---

## 项目结构

```
app/src/main/java/com/toilernote/
├── MainActivity.java              # 主 Activity，底部导航与页面联动
├── ToilerNoteApp.java             # Application，初始化默认偏好与智能休息日
├── adapter/
│   ├── CalendarAdapter.java       # 日历日期格子适配器
│   └── ViewPagerAdapter.java      # ViewPager2 Fragment 适配器
├── dao/
│   ├── DailyRecordDao.java        # 考勤记录 DAO
│   └── UserPreferenceDao.java     # 用户偏好 DAO
├── database/
│   └── AppDatabase.java           # Room 数据库单例
├── entity/
│   ├── DailyRecord.java           # 每日考勤记录实体
│   └── UserPreference.java        # 用户偏好实体
├── model/
│   └── MonthStatistics.java       # 月度统计包装类
├── repository/
│   └── RecordRepository.java      # 数据仓库封装
├── ui/
│   ├── AttendanceFragment.java    # 考勤页面（日历 + 统计卡片）
│   ├── RecordEditBottomSheet.java # 日期编辑弹窗
│   ├── SettingsFragment.java      # 设置页面
│   ├── StatisticsFragment.java    # 统计页面（图表 + 分析）
│   └── view/
│       ├── BarChartView.java      # 自定义柱状图
│       └── LineChartView.java     # 自定义折线图
├── utils/
│   ├── TimeUtils.java             # 时间/日期工具
│   └── WorkHoursCalculator.java   # 工时/加班/迟到计算
└── viewmodel/
    ├── CalendarViewModel.java     # 考勤页 ViewModel
    ├── SettingsViewModel.java     # 设置页 ViewModel
    └── StatisticsViewModel.java   # 统计页 ViewModel
```

---

## 构建与运行

```bash
# 克隆项目后执行
./gradlew assembleDebug

# 或直接在 Android Studio 中打开项目，点击 Run
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

---

## 设计参考

- 产品文档：`design/牛马日记第三版.md`
- 交互原型：`design/牛马日记原型.html`

---

## 开发计划

- [ ] 数据导出（Excel / CSV / JSON）
- [ ] 法定节假日自动标记
- [ ] 云备份与恢复
- [ ] 每日支持上传打卡图片

---

## License

MIT
