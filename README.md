# 🐮 牛马日记

> 解决月底计算工时与加班时长的麻烦，提供可视化考勤管理与月度工时统计。

## 功能简介

「牛马日记」是一款 Android 原生应用，帮助记录每日考勤、自动计算工时与加班时长，并以可视化图表呈现工作强度趋势。

### 核心功能

| 模块 | 功能说明 |
|------|---------|
| **📅 考勤页面** | 月历展示每日状态（上班/休息/请假），支持月份切换，底部实时汇总当月统计数据 |
| **📝 日期编辑** | 点击日期弹出 BottomSheet，可设置上下班时间、休息时长、全天加班、请假时段；支持快捷按钮（+5分/+1h等）和「复制昨日」 |
| **📊 统计页面** | 柱状图展示每日工时与加班分布，折线图展示累计趋势；工作强度预警（连续上班天数、加班占比）；与上月周期对比**（暂未开发）** |
| **⚙️ 设置页面** | 默认时间、日历颜色（支持 HEX 自定义）、时薪与加班倍数**（暂未开发）**、数据管理 |
| **🤖 智能初始化** | 首次启动自动根据「默认工作日」将非工作日标记为休息，省去手动录入 |
| **💾 数据备份** | 支持导出/导入 JSON 备份文件，完整迁移考勤记录与设置 |

### 工时计算规则

- **普通工作日**：实际工时以计划工时为上限，超出部分计入加班（扣除晚上休息）
- **全天加班**：当日全部在岗时间计入加班，扣除中间休息与晚上休息
- **请假日**：请假时段从计划工时中扣除；计划下班后的时间仍可计入加班（扣除晚上休息）
- **迟到判定**：实际上班时间晚于计划时间即标记迟到，不影响工时计算

---

## 技术栈

| 项目 | 说明 |
|------|------|
| 语言 | Java 11 |
| UI | XML + Material Design Components + ViewBinding |
| 导航 | BottomNavigationView + ViewPager2 + Fragment |
| 数据库 | Room (SQLite) |
| 图表 | 自定义 View（BarChartView / LineChartView） |
| 最低版本 | Android 8.0 (API 26) |

---

## 构建与运行

```bash
# 克隆项目后执行
./gradlew assembleDebug

# 或直接在 Android Studio 中打开项目，点击 Run
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

---

## Release 编译

```bash
./gradlew assembleRelease
```

## git 推送

**./.git/config**

```
[core]
	repositoryformatversion = 0
	filemode = false
	bare = false
	logallrefupdates = true
	symlinks = false
	ignorecase = true
[remote "origin"]
	// github仓库
	url = https://github.com/SperNXL/ToilerNote.git
	// gitee 仓库
	url = https://gitee.com/spernxl/ToilerNote.git
	fetch = +refs/heads/*:refs/remotes/origin/*
[branch "main"]
	remote = origin
	merge = refs/heads/main

```

## 开发计划

- [ ] 时薪与加班倍数
- [ ] 统计页面
- [ ] 法定节假日自动标记
- [ ] 云备份与恢复
- [ ] 每日支持上传打卡图片