
package com.nooralquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.media.MediaPlayer;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.os.Handler;
import org.json.JSONObject;

public class MainActivity extends Activity {

    LinearLayout layout;
    List<String[]> verses = new ArrayList<>();
    MediaPlayer audioPlayer;
    int audioSurah = 1;
    int audioAyah = 1;
    int audioAyahCount = 0;
    TextView audioStatus;
    boolean audioPreparing = false;
    int selectedReciter = 0;
    String[] reciterNames = {
        "مشاري العفاسي", "محمود خليل الحصري", "ماهر المعيقلي",
        "عبد الباسط عبد الصمد", "محمد صديق المنشاوي"
    };
    String[] reciterFolders = {
        "Alafasy_128kbps", "Husary_128kbps", "MaherAlMuaiqly128kbps",
        "Abdul_Basit_Murattal_64kbps", "Minshawy_Murattal_128kbps"
    };

    final Handler clockHandler = new Handler();
    Runnable clockRunnable;
    TextView homeClock;
    TextView homeDate;
    TextView prayerStatus;
    TextView hijriDateView;
    String selectedGovernorate = "بابل";
    int prayerCalculationMethod = 0;
    String[] governorateNames = {
        "بابل","بغداد","البصرة","النجف","كربلاء","واسط","القادسية",
        "المثنى","ذي قار","ميسان","ديالى","كركوك","صلاح الدين",
        "نينوى","الأنبار","أربيل","السليمانية","دهوك","حلبجة"
    };
    double[][] governorateCoordinates = {
        {32.47,44.42},{33.315,44.366},{30.508,47.78},{32.00,44.33},
        {32.616,44.024},{32.50,45.82},{31.99,44.92},{31.31,45.28},
        {31.05,46.26},{31.84,47.14},{33.75,44.64},{35.47,44.39},
        {34.61,43.68},{36.34,43.13},{33.42,43.30},{36.19,44.01},
        {35.56,45.44},{36.86,42.99},{35.18,45.98}
    };

    String[] surahs = {
        "الفاتحة","البقرة","آل عمران","النساء","المائدة",
        "الأنعام","الأعراف","الأنفال","التوبة","يونس",
        "هود","يوسف","الرعد","إبراهيم","الحجر",
        "النحل","الإسراء","الكهف","مريم","طه",
        "الأنبياء","الحج","المؤمنون","النور","الفرقان",
        "الشعراء","النمل","القصص","العنكبوت","الروم",
        "لقمان","السجدة","الأحزاب","سبأ","فاطر",
        "يس","الصافات","ص","الزمر","غافر",
        "فصلت","الشورى","الزخرف","الدخان","الجاثية",
        "الأحقاف","محمد","الفتح","الحجرات","ق",
        "الذاريات","الطور","النجم","القمر","الرحمن",
        "الواقعة","الحديد","المجادلة","الحشر","الممتحنة",
        "الصف","الجمعة","المنافقون","التغابن","الطلاق",
        "التحريم","الملك","القلم","الحاقة","المعارج",
        "نوح","الجن","المزمل","المدثر","القيامة",
        "الإنسان","المرسلات","النبأ","النازعات","عبس",
        "التكوير","الانفطار","المطففين","الانشقاق","البروج",
        "الطارق","الأعلى","الغاشية","الفجر","البلد",
        "الشمس","الليل","الضحى","الشرح","التين",
        "العلق","القدر","البينة","الزلزلة","العاديات",
        "القارعة","التكاثر","العصر","الهمزة","الفيل",
        "قريش","الماعون","الكوثر","الكافرون","النصر",
        "المسد","الإخلاص","الفلق","الناس"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedGovernorate = getSharedPreferences("noor_prefs", MODE_PRIVATE).getString("governorate", "بابل");
        prayerCalculationMethod = getSharedPreferences("noor_prefs", MODE_PRIVATE).getInt("prayer_method", 0);
        try {
            loadQuran();
            home();
        } catch (Throwable error) {
            showStartupError(error);
        }
    }

    void showStartupError(Throwable error) {
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setGravity(Gravity.CENTER);
        screen.setPadding(24, 24, 24, 24);
        screen.setBackgroundColor(Color.rgb(12, 45, 42));

        TextView title = new TextView(this);
        title.setText("تعذر تشغيل واجهة نور القرآن");
        title.setTextColor(Color.rgb(255, 224, 157));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        screen.addView(title);

        TextView detail = new TextView(this);
        String message = error.getClass().getSimpleName() + ": " + String.valueOf(error.getMessage());
        detail.setText("سبب الخطأ:\\n" + message + "\\n\\nأرسل هذه الرسالة لي حتى أصلح المشكلة.");
        detail.setTextColor(Color.WHITE);
        detail.setTextSize(15);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(8, 20, 8, 20);
        screen.addView(detail);

        Button retry = new Button(this);
        retry.setText("إعادة المحاولة");
        screen.addView(retry);
        retry.setOnClickListener(v -> {
            try {
                loadQuran();
                home();
            } catch (Throwable nextError) {
                showStartupError(nextError);
            }
        });
        setContentView(screen);
    }

    void loadQuran() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    getAssets().open("quran-uthmani.txt"),
                    "UTF-8"
                )
            );

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);

                if (parts.length == 3) {
                    try {
                        int ayah = Integer.parseInt(parts[1].trim());
                        String firstPart = parts[0].trim();
                        int surah;

                        // The first Fatiha line in this downloaded file starts with |1|
                        if (firstPart.isEmpty() && ayah == 1 &&
                                parts[2].contains("بِسْمِ")) {
                            surah = 1;
                        } else {
                            surah = Integer.parseInt(firstPart);
                        }

                        if (surah >= 1 && surah <= 114 && ayah >= 1) {
                            verses.add(new String[]{
                                String.valueOf(surah),
                                String.valueOf(ayah),
                                parts[2].trim()
                            });
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            reader.close();

        } catch (Exception e) {
            Toast.makeText(this,
                "تعذر قراءة ملف القرآن",
                Toast.LENGTH_LONG).show();
        }
    }

    void setup() {
        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 25, 20, 25);
        layout.setBackgroundColor(Color.rgb(12, 45, 42));
        scroll.addView(layout);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(12,45,42));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        topBar.setPadding(dp(4), dp(12), dp(4), dp(5));
        Button menu = new Button(this);
        menu.setText("☰");
        menu.setTextSize(22);
        menu.setAllCaps(false);
        menu.setTextColor(Color.rgb(255,224,157));
        menu.setBackground(premiumBackground(Color.rgb(20,63,56), Color.rgb(205,170,94), 14));
        topBar.addView(menu, new LinearLayout.LayoutParams(dp(58), dp(44)));
        menu.setOnClickListener(v -> showMainMenu(menu));
        layout.addView(topBar, 0);

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(12), dp(5), dp(12), dp(8));
        footer.setBackgroundColor(Color.rgb(9,36,33));
        Button exit = new Button(this);
        exit.setText("⏻  خروج من التطبيق");
        exit.setTextSize(14);
        exit.setAllCaps(false);
        exit.setTextColor(Color.WHITE);
        exit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(126,54,48)));
        footer.addView(exit, new LinearLayout.LayoutParams(-1, dp(46)));
        exit.setOnClickListener(v -> {
            if (!getSharedPreferences("noor_prefs", MODE_PRIVATE).getBoolean("confirm_exit", true)) {
                finish();
                return;
            }
            new android.app.AlertDialog.Builder(this)
                .setTitle("الخروج من نور القرآن")
                .setMessage("هل تريد إغلاق التطبيق؟")
                .setNegativeButton("إلغاء", (d,w) -> d.dismiss())
                .setPositiveButton("خروج", (d,w) -> finish())
                .show();
        });
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    void showMainMenu(View anchor) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, anchor);
        popup.getMenu().add("⚙ الإعدادات").setOnMenuItemClickListener(item -> { showSettings(); return true; });
        popup.getMenu().add("🤲 الأدعية والزيارات والمناجاة").setOnMenuItemClickListener(item -> { showWeeklyDuasVisits(); return true; });
        popup.getMenu().add("🧳 أدعية السفر").setOnMenuItemClickListener(item -> { showTravelDuas(); return true; });
        popup.getMenu().add("🕋 الحج والعمرة").setOnMenuItemClickListener(item -> { showHajjUmrah(); return true; });
        popup.getMenu().add("🌙 الإمساكية").setOnMenuItemClickListener(item -> { showImsakiyah(); return true; });
        popup.getMenu().add("🕌 مواقيت الصلاة").setOnMenuItemClickListener(item -> { showPrayerTimes(); return true; });
        popup.show();
    }

    void showSettings() {
        setup();
        addText("⚙ إعدادات نور القرآن", 28, Color.rgb(242,211,145));
        addText("تحكّم بتفضيلات التطبيق واحفظ اختياراتك", 15, Color.WHITE);
        android.content.SharedPreferences prefs = getSharedPreferences("noor_prefs", MODE_PRIVATE);

        addAdultCard("📍 المحافظة", "المحافظة الحالية: " + selectedGovernorate, this::showPrayerTimes);
        addText("طريقة حساب مواقيت الصلاة", 17, Color.rgb(242,211,145));
        String[] methods = {"طريقة أم القرى","رابطة العالم الإسلامي","الهيئة المصرية العامة للمساحة","جامعة العلوم الإسلامية - كراتشي","تقويم شمال أمريكا","طريقة طهران"};
        Spinner methodPicker = styledSpinner(methods, Math.max(0, Math.min(prayerCalculationMethod, methods.length - 1)));
        layout.addView(methodPicker, new LinearLayout.LayoutParams(-1, -2));

        CheckBox arabicNumbers = makeSettingCheck("استخدام الأرقام العربية", "arabic_numbers", true, prefs);
        CheckBox keepLast = makeSettingCheck("حفظ آخر سورة قرأتها", "save_last_surah", true, prefs);
        CheckBox confirmExit = makeSettingCheck("طلب تأكيد قبل الخروج", "confirm_exit", true, prefs);
        CheckBox vibration = makeSettingCheck("اهتزاز خفيف عند التسبيح", "tasbeeh_vibration", true, prefs);
        CheckBox weather = makeSettingCheck("إظهار الطقس في الصفحة الرئيسية", "show_weather", true, prefs);
        CheckBox hijri = makeSettingCheck("إظهار التاريخ الهجري عند توفر الإنترنت", "show_hijri", true, prefs);
        CheckBox prayerPreview = makeSettingCheck("إظهار بطاقة مواقيت الصلاة", "show_prayer_preview", true, prefs);

        addText("المظهر", 19, Color.rgb(242,211,145));
        addAdultCard("🌙 الأخضر الزمردي والذهبي", "المظهر الحالي للتطبيق", () -> message("المظهر الحالي: الأخضر الزمردي والذهبي."));
        addText("الخصوصية والبيانات", 19, Color.rgb(242,211,145));
        addAdultCard("🛡️ البيانات", "التفضيلات تحفظ على هذا الجهاز فقط", () -> message("لا تتم مزامنة هذه التفضيلات مع خادم."));
        addAdultCard("ℹ حول التطبيق", "نور القرآن • للقراءة والتلاوة والذكر", () -> message("نسأل الله أن يجعل هذا العمل نافعاً ومباركاً."));

        addButton("💾 حفظ جميع الإعدادات", () -> {
            prayerCalculationMethod = methodPicker.getSelectedItemPosition();
            prefs.edit().putString("governorate", selectedGovernorate)
                .putInt("prayer_method", prayerCalculationMethod)
                .putBoolean("arabic_numbers", arabicNumbers.isChecked())
                .putBoolean("save_last_surah", keepLast.isChecked())
                .putBoolean("confirm_exit", confirmExit.isChecked())
                .putBoolean("tasbeeh_vibration", vibration.isChecked())
                .putBoolean("show_weather", weather.isChecked())
                .putBoolean("show_hijri", hijri.isChecked())
                .putBoolean("show_prayer_preview", prayerPreview.isChecked()).apply();
            message("تم حفظ الإعدادات على جهازك");
        });
        addButton("🌤️ فتح الطقس", this::showWeather);
        addButton("العودة للرئيسية", this::home);
    }

    CheckBox makeSettingCheck(String label, String key, boolean defaultValue,
                              android.content.SharedPreferences prefs) {
        CheckBox check = new CheckBox(this);
        check.setText(label);
        check.setTextColor(Color.WHITE);
        check.setTextSize(16);
        check.setPadding(dp(4), dp(7), dp(4), dp(7));
        check.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(238,204,125)));
        check.setChecked(prefs.getBoolean(key, defaultValue));
        layout.addView(check);
        return check;
    }

    void showTravelDuas() {
        setup();
        addText("🧳 أدعية السفر", 27, Color.rgb(242,211,145));
        addQuranText("سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ ۝ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ");
        addText("سورة الزخرف: ١٣–١٤", 14, Color.WHITE);
        addAdultCard("دعاء الركوب والسفر", "يُعرض النص القرآني أعلاه من سورة الزخرف", () -> message("يمكن قراءة الآيتين وترديدهما عند الركوب"));
        addButton("العودة للرئيسية", this::home);
    }

    void showHajjUmrah() {
        setup();
        addText("🕋 الحج والعمرة", 27, Color.rgb(242,211,145));
        addAdultCard("التلبية", "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ", () -> showDuaDetail("لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ", "التلبية"));
        addAdultCard("مناسك الحج والعمرة", "قسم إرشادي للمناسك والأذكار", () -> message("تفاصيل المناسك تحتاج مراجعة فقهية وفق مرجعك"));
        addButton("العودة للرئيسية", this::home);
    }

    void showImsakiyah() {
        setup();
        addText("🌙 الإمساكية ومواقيت الصيام", 25, Color.rgb(242,211,145));
        addText("اختر المحافظة لعرض مواقيت الفجر والمغرب من جدول الصلاة.", 16, Color.WHITE);
        addAdultCard("مواقيت الفجر والمغرب", "المحافظة الحالية: " + selectedGovernorate, this::showPrayerTimes);
        addText("توقيت الإمساك يكون قبل الفجر وفق التقويم المعتمد محلياً؛ راجع إمساكية الوقف أو الجهة الدينية في محافظتك.", 13, Color.rgb(238,204,125));
        addButton("العودة للرئيسية", this::home);
    }

    String todayArabicName() {
        String[] days = {"الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت"};
        return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    void showTodayDevotions() {
        String day = todayArabicName();
        setup();
        addText("🌿 ورد يوم " + day, 28, Color.rgb(242,211,145));
        addText("محتوى اليوم في مكان واحد", 16, Color.WHITE);
        addAdultCard("🤲 دعاء يوم " + day, "قسم دعاء اليوم", () -> showDailyTextNotice("دعاء", day));
        addAdultCard("🕌 زيارة يوم " + day, "قسم زيارة اليوم", () -> showDailyTextNotice("زيارة", day));
        addAdultCard("✨ مناجاة يوم " + day, "قسم مناجاة اليوم", () -> showDailyTextNotice("مناجاة", day));
        addText("ستظهر النصوص الكاملة بعد اعتماد مصدرها الموثوق، حتى لا ننسب نصاً غير صحيح إلى أهل البيت عليهم السلام.", 13, Color.rgb(238,204,125));
        addButton("العودة للرئيسية", this::home);
    }

    void showDailyTextNotice(String type, String day) {
        setup();
        addText(type + " يوم " + day, 25, Color.rgb(242,211,145));
        addText("هذا الموضع مخصص للنص الكامل الموثق لهذا اليوم.", 17, Color.WHITE);
        addText("لم أضع نصاً منسوباً دون مصدر معتمد تجنباً للخطأ في النصوص الدينية.", 14, Color.rgb(238,204,125));
        addButton("رجوع إلى ورد اليوم", this::showTodayDevotions);
    }

    void addText(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER);
        text.setPadding(10, 15, 10, 15);
        layout.addView(text);
    }


    String toArabicDigits(String value) {
        return value.replace('0', '٠').replace('1', '١')
            .replace('2', '٢').replace('3', '٣')
            .replace('4', '٤').replace('5', '٥')
            .replace('6', '٦').replace('7', '٧')
            .replace('8', '٨').replace('9', '٩');
    }

    void addBasmala(int surahNumber) {
        TextView text = new TextView(this);
        String basmala = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ";
        if (surahNumber == 1) {
            text.setText(basmala + "  ۝١");
        } else {
            text.setText(basmala);
        }
        text.setTextSize(27);
        text.setTextColor(Color.rgb(238, 204, 125));
        text.setGravity(Gravity.CENTER);
        text.setTextDirection(View.TEXT_DIRECTION_RTL);
        text.setLineSpacing(10, 1.12f);
        text.setPadding(14, 22, 14, 22);
        layout.addView(text, new LinearLayout.LayoutParams(-1, -2));
    }

    void addQuranText(String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(26);
        text.setTextColor(Color.rgb(248, 235, 202));
        text.setGravity(Gravity.CENTER);
        text.setTextDirection(View.TEXT_DIRECTION_RTL);
        text.setLineSpacing(16, 1.18f);
        text.setPadding(20, 26, 20, 26);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            text.setJustificationMode(android.text.Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        GradientDrawable page = new GradientDrawable();
        page.setColor(Color.rgb(20, 63, 56));
        page.setCornerRadius(18);
        page.setStroke(2, Color.rgb(190, 158, 87));
        text.setBackground(page);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 12, 0, 12);
        layout.addView(text, params);
    }

    void addButton(String value, Runnable action) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(17);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(25, 125, 100)
            )
        );
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 8, 0, 8);
        layout.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable premiumBackground(int fill, int stroke, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(fill);
        bg.setCornerRadius(dp(radius));
        bg.setStroke(dp(2), stroke);
        return bg;
    }

    void addHomeTile(android.widget.GridLayout grid, String icon, String title,
                     String subtitle, Runnable action, int tint) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(8), dp(12), dp(8), dp(12));
        tile.setBackground(premiumBackground(Color.rgb(12,52,45), Color.rgb(218,184,111), 20));
        if (android.os.Build.VERSION.SDK_INT >= 21) tile.setElevation(dp(4));

        TextView symbol = new TextView(this);
        symbol.setText(icon);
        symbol.setTextSize(29);
        symbol.setGravity(Gravity.CENTER);
        tile.addView(symbol);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.rgb(255,224,157));
        heading.setTextSize(16);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(2, dp(6), 2, 2);
        tile.addView(heading);

        TextView detail = new TextView(this);
        detail.setText(subtitle);
        detail.setTextColor(Color.rgb(241,235,216));
        detail.setTextSize(11);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(2, dp(4), 2, 0);
        tile.addView(detail);

        android.widget.GridLayout.LayoutParams p = new android.widget.GridLayout.LayoutParams();
        p.width = 0;
        p.height = dp(142);
        p.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
        p.setMargins(dp(5), dp(5), dp(5), dp(5));
        grid.addView(tile, p);
        tile.setOnClickListener(v -> action.run());
    }

    void startHomeClock() {
        if (clockRunnable != null) clockHandler.removeCallbacks(clockRunnable);
        clockRunnable = new Runnable() {
            @Override public void run() {
                String now = new SimpleDateFormat("hh:mm:ss a", new Locale("ar")).format(new Date());
                if (homeClock != null) homeClock.setText("الوقت الآن  " + now);
                if (homeDate != null) {
                    String date = new SimpleDateFormat("EEEE، dd MMMM yyyy", new Locale("ar")).format(new Date());
                    homeDate.setText("التاريخ الميلادي: " + date);
                }
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    class MosqueHeaderArt extends View {
        final android.graphics.Paint p = new android.graphics.Paint(3);
        MosqueHeaderArt() { super(MainActivity.this); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight();
            p.setStyle(android.graphics.Paint.Style.FILL);
            p.setColor(Color.rgb(8,38,34));
            c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);
            p.setColor(Color.rgb(255,222,145));
            for(int i=0;i<24;i++){
                float x=(i*73f+31f)%Math.max(1,w), y=(i*37f+19f)%Math.max(1,h*0.58f);
                c.drawCircle(x,y,dp(i%4==0?2:1),p);
            }
            // Crescent moon
            p.setColor(Color.rgb(255,220,132));
            c.drawCircle(w*0.82f,h*0.22f,dp(22),p);
            p.setColor(Color.rgb(8,38,34));
            c.drawCircle(w*0.85f,h*0.18f,dp(20),p);
            // Minarets
            p.setColor(Color.rgb(193,151,76));
            c.drawRect(w*.12f,h*.32f,w*.17f,h*.88f,p);
            c.drawRect(w*.81f,h*.29f,w*.86f,h*.88f,p);
            c.drawRect(w*.105f,h*.28f,w*.185f,h*.34f,p);
            c.drawRect(w*.795f,h*.25f,w*.875f,h*.31f,p);
            c.drawCircle(w*.145f,h*.25f,dp(5),p);
            c.drawCircle(w*.835f,h*.22f,dp(5),p);
            // Central mosque and domes
            p.setColor(Color.rgb(218,184,111));
            c.drawRect(w*.25f,h*.62f,w*.75f,h*.9f,p);
            c.drawOval(w*.39f,h*.34f,w*.61f,h*.69f,p);
            c.drawOval(w*.27f,h*.51f,w*.40f,h*.68f,p);
            c.drawOval(w*.60f,h*.51f,w*.73f,h*.68f,p);
            // Door and windows
            p.setColor(Color.rgb(10,48,42));
            c.drawRoundRect(w*.455f,h*.70f,w*.545f,h*.9f,dp(22),dp(22),p);
            c.drawRoundRect(w*.30f,h*.70f,w*.35f,h*.81f,dp(8),dp(8),p);
            c.drawRoundRect(w*.65f,h*.70f,w*.70f,h*.81f,dp(8),dp(8),p);
            p.setStyle(android.graphics.Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor(Color.rgb(255,225,153));
            c.drawLine(w*.08f,h*.91f,w*.92f,h*.91f,p);
            p.setStyle(android.graphics.Paint.Style.FILL);
        }
    }

    void home() {
        setup();
        layout.setPadding(dp(14), dp(10), dp(14), dp(22));

        // Premium welcome banner
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(18), dp(24), dp(18), dp(22));
        GradientDrawable heroBg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(31, 91, 75), Color.rgb(10, 43, 39), Color.rgb(7, 31, 30)}
        );
        heroBg.setCornerRadius(dp(26));
        heroBg.setStroke(dp(2), Color.rgb(218, 184, 111));
        hero.setBackground(heroBg);
        if (android.os.Build.VERSION.SDK_INT >= 21) hero.setElevation(dp(7));

        TextView ornament = new TextView(this);
        ornament.setText("۞  ✦  ☾  ✦  ۞");
        ornament.setTextColor(Color.rgb(231, 198, 125));
        ornament.setTextSize(19);
        ornament.setGravity(Gravity.CENTER);
        hero.addView(ornament);

        hero.addView(new MosqueHeaderArt(), new LinearLayout.LayoutParams(-1, dp(158)));

        TextView logo = new TextView(this);
        logo.setText("نُــورُ القُــرآن");
        logo.setTextColor(Color.rgb(255, 229, 170));
        logo.setTextSize(34);
        logo.setTypeface(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(8), 0, dp(5));
        hero.addView(logo);

        TextView tagline = new TextView(this);
        tagline.setText("رفيقك إلى السكينة • تلاوة وذكر وطمأنينة");
        tagline.setTextColor(Color.rgb(245, 239, 220));
        tagline.setTextSize(13);
        tagline.setGravity(Gravity.CENTER);
        hero.addView(tagline);

        TextView welcome = new TextView(this);
        welcome.setText("أهلاً بك في رحاب القرآن الكريم");
        welcome.setTextColor(Color.rgb(231, 198, 125));
        welcome.setTextSize(15);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, dp(13), 0, 0);
        hero.addView(welcome);
        layout.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout datePanel = new LinearLayout(this);
        datePanel.setOrientation(LinearLayout.VERTICAL);
        datePanel.setGravity(Gravity.CENTER);
        datePanel.setPadding(dp(12), dp(12), dp(12), dp(12));
        datePanel.setBackground(premiumBackground(Color.rgb(18, 57, 51), Color.rgb(177, 145, 78), 19));
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(-1, -2);
        dateParams.setMargins(0, dp(12), 0, dp(8));
        homeClock = new TextView(this);
        homeClock.setTextColor(Color.rgb(255, 224, 157));
        homeClock.setTextSize(23);
        homeClock.setGravity(Gravity.CENTER);
        datePanel.addView(homeClock);
        homeDate = new TextView(this);
        homeDate.setTextColor(Color.WHITE);
        homeDate.setTextSize(14);
        homeDate.setGravity(Gravity.CENTER);
        homeDate.setPadding(4, dp(6), 4, dp(4));
        datePanel.addView(homeDate);
        hijriDateView = new TextView(this);
        hijriDateView.setText("التاريخ الهجري: يظهر مع تحديث مواقيت الصلاة");
        hijriDateView.setTextColor(Color.rgb(242, 211, 145));
        hijriDateView.setTextSize(14);
        hijriDateView.setGravity(Gravity.CENTER);
        datePanel.addView(hijriDateView);
        layout.addView(datePanel, dateParams);
        startHomeClock();

        addAdultCard("🌿 ورد اليوم: " + todayArabicName(),
            "دعاء اليوم وزيارته ومناجاته في صفحة واحدة", this::showTodayDevotions);

        TextView sectionTitle = new TextView(this);
        sectionTitle.setText("✧  أبواب النور والطمأنينة  ✧");
        sectionTitle.setTextColor(Color.rgb(255, 224, 157));
        sectionTitle.setTextSize(20);
        sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionTitle.setGravity(Gravity.CENTER);
        sectionTitle.setPadding(4, dp(16), 4, dp(8));
        layout.addView(sectionTitle);

        android.widget.GridLayout grid = new android.widget.GridLayout(this);
        grid.setColumnCount(2);
        grid.setPadding(0, dp(2), 0, dp(8));
        layout.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        addHomeTile(grid, "📖", "القرآن الكريم", "قراءة السور", this::showSurahs, Color.rgb(24,76,64));
        addHomeTile(grid, "🎧", "التلاوة والاستماع", "خمسة قراء", this::audioScreen, Color.rgb(27,72,78));
        addHomeTile(grid, "🌤️", "الطقس", "حالة الجو لمحافظتك", this::showWeather, Color.rgb(48,75,83));
        addHomeTile(grid, "🕌", "مواقيت الصلاة", "محافظات العراق", this::showPrayerTimes, Color.rgb(31,70,58));
        addHomeTile(grid, "🤲", "الأدعية والزيارات", "أذكار ومناجاة", this::showWeeklyDuasVisits, Color.rgb(56,62,78));
        addHomeTile(grid, "👨‍👩‍👧", "قسم الكبار", "عبادات يومية", this::showAdultSection, Color.rgb(53,67,59));
        addHomeTile(grid, "🧒", "عالم الأطفال", "تعلم ومرح", this::showKidsSection, Color.rgb(58,69,75));
        addHomeTile(grid, "📿", "المسبحة", "تسبيح واستغفار", this::tasbeeh, Color.rgb(53,62,76));
        addHomeTile(grid, "💰", "حاسبة الزكاة", "حساب الزكاة", this::zakat, Color.rgb(62,65,52));
        addHomeTile(grid, "✨", "أسماء الله الحسنى", "تأمل وتدبر", this::showNamesOfAllah, Color.rgb(46,70,68));
        addHomeTile(grid, "🏆", "المسابقات", "أسئلة قرآنية", () -> message("المسابقات قيد التجهيز"), Color.rgb(63,60,76));
        addHomeTile(grid, "⚙️", "الإعدادات", "تخصيص التطبيق", this::showSettings, Color.rgb(49,65,58));

        LinearLayout prayerPreview = new LinearLayout(this);
        prayerPreview.setOrientation(LinearLayout.VERTICAL);
        prayerPreview.setGravity(Gravity.CENTER);
        prayerPreview.setPadding(dp(14), dp(14), dp(14), dp(14));
        prayerPreview.setBackground(premiumBackground(Color.rgb(18,57,51), Color.rgb(205,170,94), 20));
        TextView place = new TextView(this);
        place.setText("🕌  مواقيت الصلاة • " + selectedGovernorate);
        place.setTextColor(Color.rgb(255,224,157));
        place.setTextSize(18);
        place.setGravity(Gravity.CENTER);
        prayerPreview.addView(place);
        prayerStatus = new TextView(this);
        prayerStatus.setText("تابع مواقيت الصلاة اليومية حسب محافظتك");
        prayerStatus.setTextColor(Color.WHITE);
        prayerStatus.setTextSize(14);
        prayerStatus.setGravity(Gravity.CENTER);
        prayerStatus.setPadding(4, dp(8), 4, dp(4));
        prayerPreview.addView(prayerStatus);
        layout.addView(prayerPreview, new LinearLayout.LayoutParams(-1, -2));
        addButton("عرض جدول مواقيت الصلاة", this::showPrayerTimes);

        TextView closing = new TextView(this);
        closing.setText("اللهم اجعل القرآن ربيع قلوبنا ونور صدورنا");
        closing.setTextColor(Color.rgb(242,211,145));
        closing.setTextSize(15);
        closing.setGravity(Gravity.CENTER);
        closing.setPadding(8, dp(18), 8, dp(8));
        layout.addView(closing);
    }

    void showWeather() {
        setup();
        addText("🌤️ الطقس في العراق", 27, Color.rgb(242,211,145));
        addText("اختر المحافظة لعرض حالة الطقس الحالية", 15, Color.WHITE);
        Spinner cityPicker = styledSpinner(governorateNames,
            Math.max(0, java.util.Arrays.asList(governorateNames).indexOf(selectedGovernorate)));
        layout.addView(cityPicker, new LinearLayout.LayoutParams(-1, -2));
        TextView result = new TextView(this);
        result.setText("اضغط تحديث لجلب حالة الطقس");
        result.setTextColor(Color.rgb(255,224,157));
        result.setTextSize(19);
        result.setGravity(Gravity.CENTER);
        result.setPadding(dp(14), dp(24), dp(14), dp(24));
        result.setBackground(premiumBackground(Color.rgb(20,63,56), Color.rgb(205,170,94), 20));
        layout.addView(result, new LinearLayout.LayoutParams(-1, -2));
        Runnable refresh = () -> fetchWeather(cityPicker.getSelectedItemPosition(), result);
        addButton("🔄 تحديث الطقس", refresh);
        cityPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { fetchWeather(pos, result); }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        addButton("العودة للرئيسية", this::home);
    }

    void fetchWeather(int index, TextView result) {
        if (index < 0 || index >= governorateNames.length) return;
        final int chosen = index;
        result.setText("جارٍ تحميل الطقس...");
        new android.os.AsyncTask<Void,Void,String>() {
            protected String doInBackground(Void... unused) {
                HttpURLConnection connection = null;
                try {
                    double lat = governorateCoordinates[chosen][0];
                    double lon = governorateCoordinates[chosen][1];
                    URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + lat
                        + "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&timezone=Asia%2FBaghdad");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(12000);
                    connection.setReadTimeout(12000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    StringBuilder body = new StringBuilder(); String line;
                    while ((line=reader.readLine())!=null) body.append(line);
                    reader.close();
                    JSONObject current = new JSONObject(body.toString()).getJSONObject("current");
                    return current.getString("temperature_2m") + "|" + current.getString("apparent_temperature")
                        + "|" + current.getString("relative_humidity_2m") + "|" + current.getString("wind_speed_10m")
                        + "|" + current.getString("weather_code") + "|" + current.getString("time");
                } catch(Exception e) { return null; }
                finally { if(connection!=null) connection.disconnect(); }
            }
            protected void onPostExecute(String data) {
                if (data == null) { result.setText("تعذر جلب الطقس. تحقق من الإنترنت ثم حاول مجدداً."); return; }
                try {
                    String[] v = data.split("\\|");
                    int code = Integer.parseInt(v[4]);
                    String description = weatherDescription(code);
                    result.setText("📍 " + governorateNames[chosen] + "\n\n" + description
                        + "\n\n🌡️ الحرارة: " + v[0] + "°\nالإحساس: " + v[1] + "°"
                        + "\n💧 الرطوبة: " + v[2] + "%\n💨 الرياح: " + v[3] + " كم/س"
                        + "\n\nآخر تحديث: " + v[5]);
                } catch(Exception e) { result.setText("وصلت بيانات طقس غير مكتملة."); }
            }
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }

    String weatherDescription(int code) {
        if (code == 0) return "☀️ صحو";
        if (code <= 3) return "⛅ غائم جزئياً";
        if (code == 45 || code == 48) return "🌫️ ضباب";
        if (code >= 51 && code <= 67) return "🌧️ رذاذ أو أمطار";
        if (code >= 71 && code <= 77) return "❄️ ثلوج";
        if (code >= 80 && code <= 82) return "🌦️ زخات مطر";
        if (code >= 95) return "⛈️ عواصف رعدية";
        return "🌤️ حالة جوية متغيرة";
    }

    void showPrayerTimes() {
        setup();
        addText("۞ مواقيت الصلاة في العراق ۞", 25, Color.rgb(242,211,145));
        addText("اختر المحافظة لعرض مواقيت اليوم", 15, Color.WHITE);

        final TextView cityLabel = new TextView(this);
        cityLabel.setText("المحافظة الحالية: " + selectedGovernorate);
        cityLabel.setTextColor(Color.rgb(255,224,157));
        cityLabel.setTextSize(17);
        cityLabel.setGravity(Gravity.CENTER);
        layout.addView(cityLabel);

        Spinner governoratePicker = styledSpinner(governorateNames, Math.max(0, java.util.Arrays.asList(governorateNames).indexOf(selectedGovernorate)));
        layout.addView(governoratePicker, new LinearLayout.LayoutParams(-1, -2));

        TextView gregorian = new TextView(this);
        gregorian.setTextColor(Color.WHITE);
        gregorian.setTextSize(15);
        gregorian.setGravity(Gravity.CENTER);
        gregorian.setPadding(4, dp(8), 4, dp(4));
        gregorian.setText("الميلادي: " + new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date()));
        layout.addView(gregorian);

        TextView hijri = new TextView(this);
        hijri.setText("الهجري: جارٍ جلب التاريخ...");
        hijri.setTextColor(Color.rgb(242,211,145));
        hijri.setTextSize(15);
        hijri.setGravity(Gravity.CENTER);
        layout.addView(hijri);

        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        table.setPadding(0, dp(12), 0, dp(12));
        String[] prayerNames = {"الفجر","الشروق","الظهر","العصر","المغرب","العشاء"};
        TextView[] timeCells = new TextView[6];
        for (int i = 0; i < prayerNames.length; i++) {
            TableRow row = new TableRow(this);
            row.setPadding(dp(4), dp(9), dp(4), dp(9));
            row.setBackground(premiumBackground(i % 2 == 0 ? Color.rgb(20,63,56) : Color.rgb(27,76,67), Color.rgb(190,158,87), 12));
            TextView name = new TextView(this);
            name.setText(prayerNames[i]);
            name.setTextColor(Color.rgb(242,211,145));
            name.setTextSize(16);
            name.setGravity(Gravity.CENTER);
            TextView time = new TextView(this);
            time.setText("--:--");
            time.setTextColor(Color.WHITE);
            time.setTextSize(17);
            time.setGravity(Gravity.CENTER);
            timeCells[i] = time;
            row.addView(name, new TableRow.LayoutParams(0, -2, 1));
            row.addView(time, new TableRow.LayoutParams(0, -2, 1));
            TableLayout.LayoutParams rp = new TableLayout.LayoutParams(-1, -2);
            rp.setMargins(0, dp(4), 0, dp(4));
            table.addView(row, rp);
        }
        layout.addView(table, new LinearLayout.LayoutParams(-1, -2));
        TextView note = new TextView(this);
        note.setText("المواقيت محسوبة آلياً وقد تختلف دقائق عن تقويم الوقف أو المسجد المحلي.");
        note.setTextColor(Color.LTGRAY);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        layout.addView(note);
        addButton("تحديث المواقيت", () -> fetchPrayerTimes(governoratePicker.getSelectedItemPosition(), timeCells, hijri, cityLabel));
        governoratePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                selectedGovernorate = governorateNames[position];
                cityLabel.setText("المحافظة الحالية: " + selectedGovernorate);
                fetchPrayerTimes(position, timeCells, hijri, cityLabel);
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
        addButton("العودة للرئيسية", this::home);
    }

    void fetchPrayerTimes(int index, TextView[] cells, TextView hijri, TextView cityLabel) {
        if (index < 0 || index >= governorateNames.length) index = 0;
        final int chosen = index;
        selectedGovernorate = governorateNames[chosen];
        cityLabel.setText("المحافظة الحالية: " + selectedGovernorate + " - جارٍ تحميل المواقيت...");
        new android.os.AsyncTask<Void, Void, JSONObject>() {
            String error = null;
            protected JSONObject doInBackground(Void... unused) {
                HttpURLConnection connection = null;
                try {
                    String date = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
                    double lat = governorateCoordinates[chosen][0];
                    double lon = governorateCoordinates[chosen][1];
                    URL url = new URL("https://api.aladhan.com/v1/timings/" + date
                        + "?latitude=" + lat + "&longitude=" + lon + "&method=" + prayerCalculationMethod);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(12000);
                    connection.setReadTimeout(12000);
                    connection.connect();
                    if (connection.getResponseCode() != 200) throw new Exception("HTTP " + connection.getResponseCode());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                    reader.close();
                    return new JSONObject(body.toString());
                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
            protected void onPostExecute(JSONObject response) {
                if (response == null) {
                    cityLabel.setText("تعذر تحميل المواقيت. تحقق من الإنترنت ثم اضغط تحديث.");
                    return;
                }
                try {
                    JSONObject data = response.getJSONObject("data");
                    JSONObject timings = data.getJSONObject("timings");
                    String[] keys = {"Fajr","Sunrise","Dhuhr","Asr","Maghrib","Isha"};
                    for (int i = 0; i < keys.length; i++) {
                        String value = timings.getString(keys[i]).split(" ")[0];
                        cells[i].setText(toArabicDigits(value));
                    }
                    JSONObject h = data.getJSONObject("date").getJSONObject("hijri");
                    String hDate = h.getString("day") + " " + h.getJSONObject("month").getString("ar") + " " + h.getString("year") + " هـ";
                    hijri.setText("التاريخ الهجري: " + toArabicDigits(hDate));
                    cityLabel.setText("المحافظة الحالية: " + selectedGovernorate);
                    if (hijriDateView != null) hijriDateView.setText("التاريخ الهجري: " + toArabicDigits(hDate));
                    if (prayerStatus != null) prayerStatus.setText("تم تحديث جدول الصلاة للمحافظة: " + selectedGovernorate);
                } catch (Exception e) {
                    cityLabel.setText("وصلت بيانات غير مكتملة للمواقيت.");
                }
            }
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void showWeeklyDuasVisits() {
        setup();
        addText("الأدعية والزيارات", 27, Color.rgb(242,211,145));
        addAdultCard("🤲 أدعية أيام الأسبوع", "أدعية منسوبة لأيام الأسبوع", this::showWeeklyDuas);
        addAdultCard("🕌 زيارات أيام الأسبوع", "زيارة مخصصة لكل يوم", this::showWeeklyVisits);
        addAdultCard("🌹 أدعية أهل البيت عليهم السلام", "مجموعة أدعية مأثورة", () -> message("سيتم إدراج النصوص الموثقة في قسم الأدعية"));
        addAdultCard("✨ زيارات أهل البيت عليهم السلام", "الزيارات المأثورة", () -> message("قسم الزيارات الموثقة قيد استكمال النصوص"));
        addAdultCard("🌿 مناجاة أيام الأسبوع", "مناجاة لكل يوم من أيام الأسبوع", this::showWeeklyMunasajat);
        addAdultCard("📅 ورد اليوم", "دعاء وزيارة ومناجاة اليوم الحالي", this::showTodayDevotions);
        addButton("العودة للرئيسية", this::home);
    }

    void showWeeklyDuas() {
        setup();
        addText("أدعية أيام الأسبوع", 26, Color.rgb(242,211,145));
        String[] days = {"السبت","الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة"};
        for (String day : days) addAdultCard("دعاء يوم " + day, "يفتح نص الدعاء الموثق لهذا اليوم", () -> message("نص دعاء يوم " + day + " يحتاج إدراج مصدره المعتمد"));
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void showWeeklyVisits() {
        setup();
        addText("زيارات أيام الأسبوع", 26, Color.rgb(242,211,145));
        String[] days = {"السبت","الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة"};
        for (String day : days) addAdultCard("زيارة يوم " + day, "زيارة اليوم من الزيارات المأثورة", () -> message("نص زيارة يوم " + day + " يحتاج إدراج مصدره المعتمد"));
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void showWeeklyMunasajat() {
        setup();
        addText("✨ مناجاة أيام الأسبوع", 26, Color.rgb(242,211,145));
        String[] days = {"السبت","الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة"};
        for (String day : days) addAdultCard("مناجاة يوم " + day, "موضع النص الموثق لمناجاة هذا اليوم", () -> showDailyTextNotice("مناجاة", day));
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void addAdultCardHorizontal(LinearLayout row, String title, String description, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(16, 18, 16, 18);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(20, 63, 56));
        bg.setCornerRadius(20);
        bg.setStroke(2, Color.rgb(190, 158, 87));
        card.setBackground(bg);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.rgb(238, 204, 125));
        heading.setTextSize(18);
        heading.setGravity(Gravity.CENTER);
        card.addView(heading);

        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextColor(Color.rgb(248, 235, 202));
        detail.setTextSize(13);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(4, 8, 4, 4);
        card.addView(detail);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            (int)(getResources().getDisplayMetrics().density * 220), -2);
        params.setMargins(7, 4, 7, 4);
        row.addView(card, params);
        card.setOnClickListener(v -> action.run());
    }

    Button makeAudioButton(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(13);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            Color.rgb(25, 125, 100)));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    void addAudioButtonRow(String[] labels, Runnable[] actions) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER);
        for (int i = 0; i < labels.length; i++) {
            Button button = makeAudioButton(labels[i], actions[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(3, 5, 3, 5);
            column.addView(button, params);
        }
        layout.addView(column, new LinearLayout.LayoutParams(-1, -2));
    }

    void addAdultCard(String title, String description, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(18, 16, 18, 16);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(20, 63, 56));
        bg.setCornerRadius(20);
        bg.setStroke(2, Color.rgb(190, 158, 87));
        card.setBackground(bg);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.rgb(238, 204, 125));
        heading.setTextSize(20);
        heading.setGravity(Gravity.CENTER);
        card.addView(heading);

        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextColor(Color.rgb(248, 235, 202));
        detail.setTextSize(14);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(4, 7, 4, 2);
        card.addView(detail);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 8, 0, 8);
        layout.addView(card, params);
        card.setOnClickListener(v -> action.run());
    }

    void addKidsCard(String title, String description, String emoji, int cardColor, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(20, 18, 20, 18);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{cardColor, Color.rgb(255, 255, 255)}
        );
        bg.setCornerRadius(24);
        bg.setStroke(2, Color.rgb(255, 207, 92));
        card.setBackground(bg);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            card.setElevation(5);
        }

        TextView symbol = new TextView(this);
        symbol.setText(emoji);
        symbol.setTextSize(30);
        symbol.setGravity(Gravity.CENTER);
        card.addView(symbol);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.rgb(25, 73, 69));
        heading.setTextSize(21);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(4, 6, 4, 4);
        card.addView(heading);

        TextView detail = new TextView(this);
        detail.setText(description);
        detail.setTextColor(Color.rgb(66, 86, 82));
        detail.setTextSize(14);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(4, 5, 4, 2);
        card.addView(detail);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 9, 0, 9);
        layout.addView(card, params);
        card.setOnClickListener(v -> action.run());
    }

    void showKidsSection() {
        setup();
        addText("🌈 عالم نور للأطفال 🌈", 28, Color.rgb(255, 215, 120));
        addText("نتعلم القرآن والذكر بمحبة وفرح", 17, Color.WHITE);
        addKidsCard("📖 اقرأ القرآن", "افتح سور القرآن واقرأ بهدوء", "🌟",
            Color.rgb(213, 242, 225), this::showSurahs);
        addKidsCard("🎧 استمع إلى القرآن", "اختر القارئ والسورة واستمتع بالتلاوة", "🎵",
            Color.rgb(220, 235, 255), this::audioScreen);
        addKidsCard("🤲 أذكاري الجميلة", "أذكار مختارة مع عدّاد للتكرار", "🌼",
            Color.rgb(255, 238, 202), this::showAdhkar);
        addKidsCard("📿 مسبحتي الصغيرة", "سبّح واستغفر وتابع عدد التسبيحات", "🧸",
            Color.rgb(244, 226, 250), this::tasbeeh);
        addKidsCard("⭐ تحدي اليوم", "قريباً: أسئلة قرآنية ونقاط تشجيعية", "🏆",
            Color.rgb(255, 225, 218), () -> message("تحدي اليوم قيد التجهيز"));
        addText("أحسنت يا بطل! كل خطوة جميلة تقرّبك من الخير 💛", 16,
            Color.rgb(255, 215, 120));
        addButton("العودة للرئيسية", this::home);
    }

    void showAdultSection() {
        setup();
        addText("❁ قسم الكبار ❁", 29, Color.rgb(238, 204, 125));
        addText("زادُ القلب وطمأنينة الروح", 17, Color.WHITE);
        addAdultCard("📖 القرآن الكريم", "قراءة السور بترتيب المصحف", this::showSurahs);
        addAdultCard("🎧 الاستماع إلى القرآن", "اختر القارئ والسورة واستمتع بالتلاوة", this::audioScreen);
        addAdultCard("🌅 أذكار الصباح والمساء", "أذكار مختارة مع عدّاد للتكرار", this::showAdhkar);
        addAdultCard("📿 المسبحة الإلكترونية", "تسبيح واستغفار مع عدّاد", this::tasbeeh);
        addAdultCard("💰 حاسبة الزكاة", "حساب ٢٫٥٪ من المبلغ المُدخل", this::zakat);
        addAdultCard("🌿 وردي اليومي", "اختر سورة لتجعلها وردك اليوم", this::showDailyWird);
        addAdultCard("🔖 متابعة القراءة", "العودة إلى آخر سورة فتحتها", this::continueReading);
        addAdultCard("✨ أسماء الله الحسنى", "تأمل في مجموعة من أسماء الله الحسنى", this::showNamesOfAllah);
        addAdultCard("🤲 أدعية من القرآن", "أدعية قرآنية مع مواضعها", this::showQuranDuas);
        addAdultCard("🗓️ خطة عبادتي اليومية", "ضع علامة على ما أنجزته اليوم", this::showDailyPlan);
        addText("اجعل لك وقتاً ثابتاً للقرآن والذكر والعمل الصالح", 14,
            Color.rgb(238, 204, 125));
        addButton("العودة للرئيسية", this::home);
    }

    void continueReading() {
        int last = getSharedPreferences("noor_prefs", MODE_PRIVATE)
            .getInt("last_surah", 1);
        showSurah(last);
    }

    void showNamesOfAllah() {
        setup();
        addText("✧ أسماء الله الحسنى كاملة ✧", 27, Color.rgb(238, 204, 125));
        addText("الأسماء التسعة والتسعون • اقرأ وتأمل", 15, Color.WHITE);
        String[] names = {
            "الرَّحْمَٰن","الرَّحِيم","الْمَلِك","الْقُدُّوس","السَّلَام","الْمُؤْمِن","الْمُهَيْمِن","الْعَزِيز","الْجَبَّار","الْمُتَكَبِّر",
            "الْخَالِق","الْبَارِئ","الْمُصَوِّر","الْغَفَّار","الْقَهَّار","الْوَهَّاب","الرَّزَّاق","الْفَتَّاح","الْعَلِيم","الْقَابِض",
            "الْبَاسِط","الْخَافِض","الرَّافِع","الْمُعِزُّ","الْمُذِلُّ","السَّمِيع","الْبَصِير","الْحَكَم","الْعَدْل","اللَّطِيف",
            "الْخَبِير","الْحَلِيم","الْعَظِيم","الْغَفُور","الشَّكُور","الْعَلِيُّ","الْكَبِير","الْحَفِيظ","الْمُقِيت","الْحَسِيب",
            "الْجَلِيل","الْكَرِيم","الرَّقِيب","الْمُجِيب","الْوَاسِع","الْحَكِيم","الْوَدُود","الْمَجِيد","الْبَاعِث","الشَّهِيد",
            "الْحَقُّ","الْوَكِيل","الْقَوِيُّ","الْمَتِين","الْوَلِيُّ","الْحَمِيد","الْمُحْصِي","الْمُبْدِئ","الْمُعِيد","الْمُحْيِي",
            "الْمُمِيت","الْحَيُّ","الْقَيُّوم","الْوَاجِد","الْمَاجِد","الْوَاحِد","الصَّمَد","الْقَادِر","الْمُقْتَدِر","الْمُقَدِّم",
            "الْمُؤَخِّر","الْأَوَّل","الْآخِر","الظَّاهِر","الْبَاطِن","الْوَالِي","الْمُتَعَالِي","الْبَرُّ","التَّوَّاب","الْمُنْتَقِم",
            "الْعَفُوُّ","الرَّؤُوف","مَالِكُ الْمُلْك","ذُو الْجَلَالِ وَالْإِكْرَام","الْمُقْسِط","الْجَامِع","الْغَنِيُّ","الْمُغْنِي","الْمَانِع","الضَّارُّ",
            "النَّافِع","النُّور","الْهَادِي","الْبَدِيع","الْبَاقِي","الْوَارِث","الرَّشِيد","الصَّبُور"
        };
        android.widget.GridLayout namesGrid = new android.widget.GridLayout(this);
        namesGrid.setColumnCount(2);
        namesGrid.setPadding(0, dp(6), 0, dp(8));
        layout.addView(namesGrid, new LinearLayout.LayoutParams(-1, -2));
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            LinearLayout card = new LinearLayout(this);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(5), dp(13), dp(5), dp(13));
            card.setBackground(premiumBackground(Color.rgb(20,63,56), Color.rgb(205,170,94), 15));
            TextView label = new TextView(this);
            label.setText(toArabicDigits(String.valueOf(i + 1)) + "  ۞  " + name);
            label.setTextColor(Color.rgb(255,224,157));
            label.setTextSize(17);
            label.setGravity(Gravity.CENTER);
            card.addView(label);
            android.widget.GridLayout.LayoutParams p = new android.widget.GridLayout.LayoutParams();
            p.width = 0; p.height = dp(66);
            p.columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f);
            p.setMargins(dp(4), dp(4), dp(4), dp(4));
            namesGrid.addView(card, p);
            card.setOnClickListener(v -> message(name + " • تذكّر الله بهذا الاسم وادعه به"));
        }
        addText("تنبيه: توجد اختلافات في ترتيب وعدّ بعض القوائم المتداولة للأسماء الحسنى؛ هذه قائمة مشهورة للتذكير.", 12, Color.rgb(238,204,125));
        addButton("العودة إلى قسم الكبار", this::showAdultSection);
        addButton("العودة للرئيسية", this::home);
    }

    void showQuranDuas() {
        setup();
        addText("🤲 أدعية من القرآن الكريم", 26, Color.rgb(238, 204, 125));
        addAdultCard("رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "سورة البقرة • الآية ٢٠١", () -> showDuaDetail(
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "البقرة: ٢٠١"));
        addAdultCard("رَبِّ زِدْنِي عِلْمًا", "سورة طه • الآية ١١٤",
            () -> showDuaDetail("رَبِّ زِدْنِي عِلْمًا", "طه: ١١٤"));
        addAdultCard("رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً إِنَّكَ أَنتَ الْوَهَّابُ",
            "سورة آل عمران • الآية ٨", () -> showDuaDetail(
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً إِنَّكَ أَنتَ الْوَهَّابُ",
            "آل عمران: ٨"));
        addAdultCard("رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي",
            "سورة طه • الآيتان ٢٥–٢٦", () -> showDuaDetail(
            "رَبِّ اشْرَحْ لِي صَدْرِي ۝ وَيَسِّرْ لِي أَمْرِي", "طه: ٢٥–٢٦"));
        addAdultCard("رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            "سورة الأعراف • الآية ٢٣", () -> showDuaDetail(
            "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            "الأعراف: ٢٣"));
        addButton("رجوع إلى قسم الكبار", this::showAdultSection);
    }

    void showDuaDetail(String dua, String reference) {
        setup();
        addText("۞ دعاء قرآني ۞", 25, Color.rgb(238, 204, 125));
        addQuranText(dua);
        addText("الموضع: " + reference, 16, Color.WHITE);
        addButton("نسخ الدعاء", () -> {
            android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("دعاء قرآني", dua));
            message("تم نسخ الدعاء");
        });
        addButton("رجوع إلى الأدعية", this::showQuranDuas);
    }

    void showDailyPlan() {
        setup();
        addText("🗓️ خطة عبادتي اليومية", 26, Color.rgb(238, 204, 125));
        addText("ضع علامة عند إتمام العمل. تُحفظ العلامات على هذا الجهاز.", 14, Color.WHITE);
        String[] tasks = {"قراءة ورد من القرآن","أذكار الصباح","أذكار المساء",
            "الصلاة على النبي ﷺ","الاستغفار","صدقة أو عمل خير"};
        android.content.SharedPreferences prefs =
            getSharedPreferences("noor_daily_plan", MODE_PRIVATE);
        String today = new java.text.SimpleDateFormat("yyyyMMdd", Locale.US)
            .format(new java.util.Date());
        boolean sameDay = today.equals(prefs.getString("plan_date", ""));
        if (!sameDay) {
            prefs.edit().clear().putString("plan_date", today).apply();
        }
        for (int i = 0; i < tasks.length; i++) {
            final String key = "task_" + i;
            CheckBox check = new CheckBox(this);
            check.setText(tasks[i]);
            check.setTextColor(Color.rgb(248, 235, 202));
            check.setTextSize(17);
            check.setButtonTintList(android.content.res.ColorStateList.valueOf(
                Color.rgb(238, 204, 125)));
            check.setChecked(prefs.getBoolean(key, false));
            check.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(key, checked).apply());
            layout.addView(check);
        }
        addText("تذكير: هذه قائمة تنظيم شخصية وليست حكماً شرعياً.", 13,
            Color.rgb(238, 204, 125));
        addButton("رجوع إلى قسم الكبار", this::showAdultSection);
    }

    void showAdhkar() {
        setup();
        addText("🌅 أذكار الصباح والمساء", 26, Color.rgb(238, 204, 125));
        addText("اختر الذكر، ثم اضغط للتسبيح وعدّ مراتك", 15, Color.WHITE);
        addAdultCard("سبحان الله وبحمده", "كرّر الذكر ١٠٠ مرة", () ->
            showDhikrCounter("سبحان الله وبحمده", 100));
        addAdultCard("أستغفر الله وأتوب إليه", "كرّر الاستغفار ١٠٠ مرة", () ->
            showDhikrCounter("أستغفر الله وأتوب إليه", 100));
        addAdultCard("لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
            "ذكر عظيم، اجعل له نصيباً من يومك", () ->
            showDhikrCounter("لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير", 100));
        addAdultCard("سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر",
            "أذكار يسيرة على اللسان", () ->
            showDhikrCounter("سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر", 33));
        addText("هذه أذكار مختارة وليست قائمة كاملة بأذكار الصباح والمساء.", 13,
            Color.rgb(238, 204, 125));
        addButton("رجوع إلى قسم الكبار", this::showAdultSection);
    }

    void showDhikrCounter(String dhikr, int target) {
        setup();
        addText("۞ الذكر ۞", 23, Color.rgb(238, 204, 125));
        addQuranText(dhikr);
        final int[] current = {0};
        TextView counter = new TextView(this);
        counter.setText("٠ / " + toArabicDigits(String.valueOf(target)));
        counter.setTextColor(Color.WHITE);
        counter.setTextSize(32);
        counter.setGravity(Gravity.CENTER);
        counter.setPadding(10, 22, 10, 22);
        layout.addView(counter);

        TextView progress = new TextView(this);
        progress.setText("ابدأ بذكر الله");
        progress.setTextColor(Color.rgb(238, 204, 125));
        progress.setTextSize(16);
        progress.setGravity(Gravity.CENTER);
        layout.addView(progress);

        addButton("📿 اضغط للتسبيح", () -> {
            if (current[0] < target) current[0]++;
            counter.setText(toArabicDigits(String.valueOf(current[0])) + " / "
                + toArabicDigits(String.valueOf(target)));
            if (current[0] >= target) {
                progress.setText("أحسنت، أتممت العدد المحدد 🌿");
            } else {
                progress.setText("استمر، تقبّل الله ذكرك");
            }
        });
        addButton("إعادة العدّاد", () -> {
            current[0] = 0;
            counter.setText("٠ / " + toArabicDigits(String.valueOf(target)));
            progress.setText("ابدأ بذكر الله");
        });
        addButton("رجوع إلى الأذكار", this::showAdhkar);
    }

    void showDailyWird() {
        setup();
        addText("🌿 وردي اليومي", 27, Color.rgb(238, 204, 125));
        addText("اختر سورة لقراءتها اليوم", 16, Color.WHITE);
        Spinner picker = styledSpinner(surahs, 17);
        layout.addView(picker, new LinearLayout.LayoutParams(-1, -2));
        addButton("فتح السورة", () -> showSurah(picker.getSelectedItemPosition() + 1));
        addText("يمكنك العودة يومياً واختيار موضع جديد للقراءة.", 14,
            Color.rgb(248, 235, 202));
        addButton("رجوع إلى قسم الكبار", this::showAdultSection);
    }

    Spinner styledSpinner(String[] items, int selected) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView,
                                android.view.ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.rgb(248, 235, 202));
                view.setTextSize(18);
                view.setGravity(Gravity.CENTER);
                view.setPadding(14, 14, 14, 14);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        android.view.ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.rgb(248, 235, 202));
                view.setTextSize(17);
                view.setGravity(Gravity.CENTER);
                view.setPadding(18, 16, 18, 16);
                view.setBackgroundColor(position % 2 == 0
                    ? Color.rgb(20, 63, 56) : Color.rgb(27, 78, 68));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(adapter);
        spinner.setSelection(selected);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(20, 63, 56));
        bg.setCornerRadius(16);
        bg.setStroke(2, Color.rgb(190, 158, 87));
        spinner.setBackground(bg);
        spinner.setPadding(12, 4, 12, 4);
        return spinner;
    }

    void addAudioLabel(String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextColor(Color.rgb(238, 204, 125));
        label.setTextSize(17);
        label.setGravity(Gravity.RIGHT);
        label.setPadding(4, 14, 4, 6);
        layout.addView(label);
    }

    void styleAudioPanel(LinearLayout panel) {
        panel.setPadding(14, 12, 14, 12);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.rgb(20, 63, 56));
        panelBg.setCornerRadius(18);
        panelBg.setStroke(2, Color.rgb(190, 158, 87));
        panel.setBackground(panelBg);
        LinearLayout.LayoutParams panelParams =
            new LinearLayout.LayoutParams(-1, -2);
        panelParams.setMargins(0, 8, 0, 8);
        panel.setLayoutParams(panelParams);
    }

    void audioScreen() {
        setup();
        addText("❁ روضة التلاوات القرآنية ❁", 27, Color.rgb(238, 204, 125));
        addText("اختر قارئك وسورتك المفضلة", 16, Color.WHITE);

        LinearLayout selectorsColumn = new LinearLayout(this);
        selectorsColumn.setOrientation(LinearLayout.VERTICAL);
        selectorsColumn.setPadding(4, 4, 4, 4);

        LinearLayout reciterColumn = new LinearLayout(this);
        reciterColumn.setOrientation(LinearLayout.VERTICAL);
        styleAudioPanel(reciterColumn);
        TextView reciterLabel = new TextView(this);
        reciterLabel.setText("✦ اختر القارئ");
        reciterLabel.setTextColor(Color.rgb(238, 204, 125));
        reciterLabel.setGravity(Gravity.CENTER);
        reciterColumn.addView(reciterLabel);
        Spinner reciterPicker = styledSpinner(reciterNames, selectedReciter);
        reciterColumn.addView(reciterPicker, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout surahColumn = new LinearLayout(this);
        surahColumn.setOrientation(LinearLayout.VERTICAL);
        styleAudioPanel(surahColumn);
        TextView surahLabel = new TextView(this);
        surahLabel.setText("✦ اختر السورة");
        surahLabel.setTextColor(Color.rgb(238, 204, 125));
        surahLabel.setGravity(Gravity.CENTER);
        surahColumn.addView(surahLabel);
        Spinner surahPicker = styledSpinner(surahs, audioSurah - 1);
        surahColumn.addView(surahPicker, new LinearLayout.LayoutParams(-1, -2));

        selectorsColumn.addView(reciterColumn);
        selectorsColumn.addView(surahColumn);
        layout.addView(selectorsColumn, new LinearLayout.LayoutParams(-1, -2));

        audioStatus = new TextView(this);
        audioStatus.setText("۞ اختر القارئ والسورة ثم اضغط تشغيل ۞");
        audioStatus.setTextColor(Color.rgb(248, 235, 202));
        audioStatus.setTextSize(17);
        audioStatus.setGravity(Gravity.CENTER);
        audioStatus.setTextDirection(View.TEXT_DIRECTION_RTL);
        audioStatus.setPadding(16, 20, 16, 20);
        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(Color.rgb(20, 63, 56));
        statusBg.setCornerRadius(18);
        statusBg.setStroke(2, Color.rgb(190, 158, 87));
        audioStatus.setBackground(statusBg);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, 20, 0, 14);
        layout.addView(audioStatus, statusParams);

        reciterPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedReciter != position && audioPlayer != null) stopAudio();
                selectedReciter = position;
                if (audioStatus != null) audioStatus.setText("القارئ المختار: " + reciterNames[selectedReciter]);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        surahPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                audioSurah = position + 1;
                if (audioStatus != null) audioStatus.setText("السورة المختارة: " + surahs[audioSurah - 1]);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        addAudioButtonRow(new String[]{"▶ تشغيل", "⏸ إيقاف مؤقت"},
            new Runnable[]{
                () -> {
                    selectedReciter = reciterPicker.getSelectedItemPosition();
                    audioSurah = surahPicker.getSelectedItemPosition() + 1;
                    audioAyah = 1;
                    audioAyahCount = countAyahs(audioSurah);
                    if (audioAyahCount == 0) {
                        audioStatus.setText("تعذر تحديد آيات السورة من ملف القرآن");
                    } else {
                        playCurrentAyah();
                    }
                },
                this::toggleAudio
            });
        addAudioButtonRow(new String[]{"⏭ الآية التالية", "⏮ إعادة الآية"},
            new Runnable[]{
                () -> {
                    if (audioAyahCount > 0 && audioAyah < audioAyahCount) {
                        audioAyah++;
                        playCurrentAyah();
                    }
                },
                () -> { if (audioAyahCount > 0) playCurrentAyah(); }
            });
        addAudioButtonRow(new String[]{"⏹ إيقاف التلاوة", "↩ رجوع"},
            new Runnable[]{this::stopAudio, this::home});
        addText("الصوت عبر EveryAyah.com • يتطلب اتصالاً بالإنترنت", 12,
            Color.rgb(190, 200, 190));
    }

    int countAyahs(int surahNumber) {
        int total = 0;
        for (String[] verse : verses) {
            if (Integer.parseInt(verse[0]) == surahNumber) total++;
        }
        return total;
    }

    void playCurrentAyah() {
        if (audioAyah > audioAyahCount) {
            audioStatus.setText("انتهت سورة " + surahs[audioSurah - 1]);
            releaseAudio();
            return;
        }
        releaseAudio();
        audioPreparing = true;
        audioStatus.setText("جارٍ تحميل سورة " + surahs[audioSurah - 1]
            + " - الآية " + toArabicDigits(String.valueOf(audioAyah)));

        try {
            String file = String.format(Locale.US, "%03d%03d.mp3",
                audioSurah, audioAyah);
            String url = "https://everyayah.com/data/" + reciterFolders[selectedReciter] + "/" + file;
            audioPlayer = new MediaPlayer();
            audioPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(url);
            audioPlayer.setOnPreparedListener(player -> {
                audioPreparing = false;
                audioStatus.setText("يُتلى الآن ۞ " + reciterNames[selectedReciter]
                    + " ۞ سورة " + surahs[audioSurah - 1]
                    + " - الآية " + toArabicDigits(String.valueOf(audioAyah)));
                player.start();
            });
            audioPlayer.setOnCompletionListener(player -> {
                audioAyah++;
                playCurrentAyah();
            });
            audioPlayer.setOnErrorListener((player, what, extra) -> {
                audioPreparing = false;
                audioStatus.setText("تعذر تشغيل الصوت. تحقق من الإنترنت ثم حاول مجدداً.");
                releaseAudio();
                return true;
            });
            audioPlayer.prepareAsync();
        } catch (Exception e) {
            audioPreparing = false;
            audioStatus.setText("تعذر بدء التلاوة. تحقق من اتصال الإنترنت.");
            releaseAudio();
        }
    }

    void toggleAudio() {
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.pause();
            audioStatus.setText("التلاوة متوقفة مؤقتاً");
        } else if (audioPlayer != null && !audioPreparing) {
            audioPlayer.start();
            audioStatus.setText("التلاوة مستمرة");
        } else if (audioPlayer == null) {
            audioStatusMessage("اختر السورة واضغط تشغيل أولاً");
        }
    }

    void stopAudio() {
        releaseAudio();
        audioAyah = 1;
        if (audioStatus != null) audioStatus.setText("تم إيقاف التلاوة");
    }

    void releaseAudio() {
        if (audioPlayer != null) {
            try { audioPlayer.setOnPreparedListener(null); } catch (Exception ignored) {}
            try { audioPlayer.setOnCompletionListener(null); } catch (Exception ignored) {}
            try { audioPlayer.setOnErrorListener(null); } catch (Exception ignored) {}
            try { audioPlayer.stop(); } catch (Exception ignored) {}
            try { audioPlayer.release(); } catch (Exception ignored) {}
            audioPlayer = null;
        }
        audioPreparing = false;
    }

    void audioStatusMessage(String value) {
        if (audioStatus != null) audioStatus.setText(value);
        else message(value);
    }

    @Override
    protected void onDestroy() {
        releaseAudio();
        super.onDestroy();
    }

    void showSurahs() {
        setup();
        addText("سور القرآن الكريم", 27, Color.WHITE);
        addButton("رجوع", this::home);

        for (int i = 0; i < surahs.length; i++) {
            final int number = i + 1;
            addButton(number + " - سورة " + surahs[i],
                () -> showSurah(number));
        }
    }

    void showSurah(int number) {
        if (getSharedPreferences("noor_prefs", MODE_PRIVATE).getBoolean("save_last_surah", true)) {
            getSharedPreferences("noor_prefs", MODE_PRIVATE).edit()
                .putInt("last_surah", number).apply();
        }
        setup();
        addText("سورة " + surahs[number - 1], 28, Color.YELLOW);
        addButton("رجوع إلى السور", this::showSurahs);

        StringBuilder surahText = new StringBuilder();
        int found = 0;
        for (String[] verse : verses) {
            if (Integer.parseInt(verse[0]) == number) {
                String verseText = verse[2].trim();

                // In Al-Fatiha, the basmala is verse 1 and is shown separately above.
                if (number == 1 && "1".equals(verse[1])) {
                    found++;
                    continue;
                }

                if (found > 0) surahText.append("   ");
                surahText.append(verseText)
                    .append("  ۝")
                    .append(toArabicDigits(verse[1]));
                found++;
            }
        }

        if (found == 0) {
            addText("لم يتم العثور على آيات هذه السورة", 18, Color.YELLOW);
        } else {
            if (number != 9) {
                addBasmala(number);
            }
            if (surahText.length() > 0) {
                addQuranText(surahText.toString());
            }
        }
    }




    class TasbeehArtView extends View {
        final android.graphics.Paint paint = new android.graphics.Paint(3);
        TasbeehArtView(android.content.Context context) { super(context); }
        @Override protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f, cy = getHeight() / 2f;
            float radius = Math.min(getWidth() * 0.30f, getHeight() * 0.34f);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(Color.rgb(218,184,111));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            for (int n = 0; n < 33; n++) {
                double a = (Math.PI * 2 * n / 33.0) - Math.PI / 2;
                float x = cx + (float)Math.cos(a) * radius;
                float y = cy + (float)Math.sin(a) * radius;
                paint.setColor(n % 3 == 0 ? Color.rgb(255,226,157) : Color.rgb(180,139,65));
                canvas.drawCircle(x, y, dp(6), paint);
                paint.setColor(Color.argb(90,255,255,255));
                canvas.drawCircle(x - dp(2), y - dp(2), dp(2), paint);
            }
            paint.setColor(Color.rgb(218,184,111));
            canvas.drawCircle(cx, cy, dp(37), paint);
            paint.setColor(Color.rgb(13,48,43));
            canvas.drawCircle(cx, cy, dp(32), paint);
            paint.setColor(Color.rgb(255,224,157));
            paint.setTextAlign(android.graphics.Paint.Align.CENTER);
            paint.setTextSize(dp(24));
            canvas.drawText("۞", cx, cy + dp(8), paint);
            paint.setTextSize(dp(10));
            canvas.drawText("سُبْحَانَ الله", cx, cy + radius + dp(27), paint);
        }
    }

    int count = 0;

    void tasbeeh() {
        setup();
        android.content.SharedPreferences prefs = getSharedPreferences("noor_prefs", MODE_PRIVATE);
        count = prefs.getInt("tasbeeh_count", 0);
        addText("۞ المسبحة الإلكترونية ۞", 27, Color.rgb(242,211,145));
        addText("اذكر الله بقلب حاضر وطمأنينة", 15, Color.WHITE);

        LinearLayout art = new LinearLayout(this);
        art.setOrientation(LinearLayout.VERTICAL);
        art.setGravity(Gravity.CENTER);
        art.setPadding(dp(14), dp(20), dp(14), dp(20));
        art.setBackground(premiumBackground(Color.rgb(13,48,43), Color.rgb(205,170,94), 24));
        TasbeehArtView beads = new TasbeehArtView(this);
        art.addView(beads, new LinearLayout.LayoutParams(-1, dp(220)));
        TextView tasbeehCaption = new TextView(this);
        tasbeehCaption.setText("مسبحة نور القرآن");
        tasbeehCaption.setTextColor(Color.rgb(255,224,157));
        tasbeehCaption.setTextSize(18);
        tasbeehCaption.setGravity(Gravity.CENTER);
        tasbeehCaption.setPadding(0, dp(12), 0, 0);
        art.addView(tasbeehCaption);
        layout.addView(art, new LinearLayout.LayoutParams(-1, -2));

        final String[] phrases = {"سُبْحَانَ الله","الْحَمْدُ لِلَّه","لَا إِلَهَ إِلَّا الله","اللهُ أَكْبَر","أَسْتَغْفِرُ الله"};
        Spinner phrasePicker = styledSpinner(phrases, 0);
        layout.addView(phrasePicker, new LinearLayout.LayoutParams(-1, -2));

        TextView counter = new TextView(this);
        counter.setText(toArabicDigits(String.valueOf(count)));
        counter.setTextColor(Color.rgb(255,224,157));
        counter.setTextSize(46);
        counter.setTypeface(null, android.graphics.Typeface.BOLD);
        counter.setGravity(Gravity.CENTER);
        counter.setPadding(dp(18), dp(18), dp(18), dp(18));
        counter.setBackground(premiumBackground(Color.rgb(20,63,56), Color.rgb(218,184,111), 80));
        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(dp(170), dp(150));
        counterParams.gravity = Gravity.CENTER;
        counterParams.setMargins(0, dp(14), 0, dp(14));
        layout.addView(counter, counterParams);

        TextView goal = new TextView(this);
        goal.setText("استمر بالذكر، تقبّل الله منك");
        goal.setTextColor(Color.WHITE);
        goal.setTextSize(14);
        goal.setGravity(Gravity.CENTER);
        layout.addView(goal);

        addButton("📿  تسبيح", () -> {
            count++;
            counter.setText(toArabicDigits(String.valueOf(count)));
            prefs.edit().putInt("tasbeeh_count", count).apply();
            if (prefs.getBoolean("tasbeeh_vibration", true)) {
                try {
                    android.os.Vibrator vibrator = (android.os.Vibrator)getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (android.os.Build.VERSION.SDK_INT >= 26)
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(25, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                        else vibrator.vibrate(25);
                    }
                } catch (Exception ignored) {}
            }
            if (count % 33 == 0) goal.setText("أحسنت، أكملت ٣٣ تسبيحة 🌿");
        });
        addButton("إعادة العدّاد", () -> new android.app.AlertDialog.Builder(this)
            .setTitle("تصفير المسبحة").setMessage("هل تريد تصفير العدد الحالي؟")
            .setNegativeButton("إلغاء", (d,w) -> d.dismiss())
            .setPositiveButton("تصفير", (d,w) -> { count=0; counter.setText("٠"); prefs.edit().putInt("tasbeeh_count",0).apply(); goal.setText("ابدأ ذكرك من جديد"); }).show());
        addButton("رجوع", this::home);
    }

    void zakat() {
        setup();
        addText("حاسبة الزكاة", 27, Color.WHITE);
        EditText amount = new EditText(this);
        amount.setHint("أدخل المبلغ");
        amount.setInputType(8194);
        amount.setTextColor(Color.WHITE);
        layout.addView(amount);

        addButton("احسب 2.5%", () -> {
            try {
                double value = Double.parseDouble(
                    amount.getText().toString());
                addText("الناتج: " + (value * 0.025), 22, Color.YELLOW);
            } catch (Exception e) {
                message("أدخل مبلغاً صحيحاً");
            }
        });
        addButton("رجوع", this::home);
    }

    void message(String value) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }
}
