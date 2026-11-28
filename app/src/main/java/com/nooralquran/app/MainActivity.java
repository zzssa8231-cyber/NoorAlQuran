
package com.nooralquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    static final String ADMOB_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    static final String FIREBASE_DATABASE_URL = "https://nooralquran-3b05a-default-rtdb.asia-southeast1.firebasedatabase.app/";

    LinearLayout layout;
    List<String[]> verses = new ArrayList<>();
    MediaPlayer audioPlayer;
    MediaPlayer adhanPlayer;
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
    TextView homePrayerCountdown;
    final Handler prayerCountdownHandler = new Handler();
    Runnable prayerCountdownRunnable;
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
        MobileAds.initialize(this, initializationStatus -> {});
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
        getWindow().getDecorView().setSystemUiVisibility(0);
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

        CheckBox adhanEnabled = makeSettingCheck("تفعيل الأذان (التشغيل التلقائي قيد التجهيز)", "adhan_enabled", false, prefs);
        addAdultCard("🕌 صوت الأذان المجاني", "تسجيل Beautiful adhan — ترخيص CC0، يحتاج إنترنت عند التشغيل", this::playLegalAdhan);
        addButton("▶ تجربة صوت الأذان", this::playLegalAdhan);
        addButton("⏹ إيقاف صوت الأذان", this::stopLegalAdhan);

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
                .putBoolean("show_prayer_preview", prayerPreview.isChecked())
                .putBoolean("adhan_enabled", adhanEnabled.isChecked()).apply();
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

    void playLegalAdhan() {
        stopLegalAdhan();
        message("جارٍ تحميل تسجيل الأذان المرخّص...");
        try {
            adhanPlayer = new MediaPlayer();
            adhanPlayer.setAudioStreamType(android.media.AudioManager.STREAM_ALARM);
            adhanPlayer.setDataSource("https://upload.wikimedia.org/wikipedia/commons/transcoded/b/b0/Beautiful_adhan.ogg/Beautiful_adhan.ogg.mp3");
            adhanPlayer.setOnPreparedListener(player -> player.start());
            adhanPlayer.setOnErrorListener((player, what, extra) -> {
                message("تعذر تشغيل الأذان. تحقق من الإنترنت وحاول مجدداً.");
                stopLegalAdhan();
                return true;
            });
            adhanPlayer.prepareAsync();
        } catch (Exception e) {
            message("تعذر تشغيل تسجيل الأذان حالياً.");
            stopLegalAdhan();
        }
    }

    void stopLegalAdhan() {
        if (adhanPlayer != null) {
            try { adhanPlayer.setOnPreparedListener(null); } catch (Exception ignored) {}
            try { adhanPlayer.setOnErrorListener(null); } catch (Exception ignored) {}
            try { adhanPlayer.stop(); } catch (Exception ignored) {}
            try { adhanPlayer.release(); } catch (Exception ignored) {}
            adhanPlayer = null;
        }
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
        int dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        int[] duaPages = {72,73,74,75,76,77,78};
        int[] visitPages = {106,108,109,110,111,112,105};
        String[] munajatNames = {"مناجاة التائبين","مناجاة الشاكين","مناجاة الخائفين","مناجاة الراجين","مناجاة الراغبين","مناجاة الشاكرين","مناجاة المطيعين لله"};
        int[] munajatPages = {178,179,180,181,182,183,184};
        setup();
        addText("🌿 ورد يوم " + day, 28, Color.rgb(242,211,145));
        addText("وردك اليومي من مفاتيح الجنان", 16, Color.WHITE);
        addText("خصص بضع دقائق للقراءة والدعاء والزيارة.", 14, Color.rgb(238,204,125));
        addAdultCard("🤲 دعاء يوم " + day, "دعاء اليوم • مفاتيح الجنان", () -> openMafatihPage(duaPages[dayIndex], "دعاء يوم " + day));
        addAdultCard("🕌 زيارة يوم " + day, "زيارة اليوم • مفاتيح الجنان", () -> openMafatihPage(visitPages[dayIndex], "زيارة يوم " + day));
        final String munajatName = munajatNames[dayIndex];
        addAdultCard("✨ " + munajatName, "مناجاة مختارة لهذا اليوم", () -> openMafatihPage(munajatPages[dayIndex], munajatName));
        addAdultCard("📚 جميع أدعية الأسبوع", "تصفح أدعية الأيام السبعة", this::showWeeklyDuas);
        addAdultCard("🌹 جميع الزيارات", "تصفح زيارات أيام الأسبوع", this::showWeeklyVisits);
        addAdultCard("✨ المناجاة الخمس عشرة", "تصفح مجموعة المناجاة كاملة", this::showWeeklyMunasajat);
        addText("المناجاة المختارة هنا ترتيب يومي مقترح للتصفح، وليست نسبة لتخصيص شرعي لكل يوم.", 12, Color.rgb(238,204,125));
        addButton("العودة للرئيسية", this::home);
    }

    void showDailyTextNotice(String type, String day) {
        showTodayDevotions();
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

    void addHomeFeatureCard(String icon, String title, String subtitle, String actionText,
                            int fill, int accent, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{fill, Color.rgb(10, 39, 38)});
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(2), accent);
        card.setBackground(bg);
        if (android.os.Build.VERSION.SDK_INT >= 21) card.setElevation(dp(5));

        TextView symbol = new TextView(this);
        symbol.setText(icon);
        symbol.setTextSize(31);
        symbol.setGravity(Gravity.CENTER);
        card.addView(symbol);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.rgb(255, 226, 165));
        heading.setTextSize(20);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(5), 0, dp(4));
        card.addView(heading);

        TextView detail = new TextView(this);
        detail.setText(subtitle);
        detail.setTextColor(Color.WHITE);
        detail.setTextSize(13);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(dp(4), dp(2), dp(4), dp(9));
        card.addView(detail);

        TextView actionLabel = new TextView(this);
        actionLabel.setText("  " + actionText + "  ");
        actionLabel.setTextColor(Color.rgb(15, 43, 39));
        actionLabel.setTextSize(14);
        actionLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        actionLabel.setGravity(Gravity.CENTER);
        actionLabel.setPadding(dp(18), dp(8), dp(18), dp(8));
        actionLabel.setBackground(premiumBackground(accent, accent, 18));
        card.addView(actionLabel);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(7), 0, dp(7));
        layout.addView(card, params);
        card.setOnClickListener(v -> action.run());
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
                updatePrayerCountdown();
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

        // Three independent featured destinations on the home screen.
        addHomeFeatureCard("⭐", "نور بلس", "مكتبة تعليمية مميزة للكبار والأطفال • ٣٠٬٠٠٠ د.ع شهرياً",
            "استكشف المميزات", Color.rgb(91,65,35), Color.rgb(218,184,111), this::showNoorPlus);
        addHomeFeatureCard("🫧", "لعبة فقاعات الألوان", "لعبة مستقلة • صوّب واجمع الفقاعات المتشابهة",
            "ابدأ اللعب", Color.rgb(25,65,105), Color.rgb(95,190,245), this::showBubbleShooter);
        addHomeFeatureCard("🏆", "تحدي اليوم", "خمسة أسئلة قرآنية وثقافية • نقاط وشارات تشجيعية",
            "ابدأ التحدي", Color.rgb(89,48,70), Color.rgb(245,155,185), () -> showDailyChallengeQuestion(0, 0));

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
        addHomeTile(grid, "🏆", "المسابقات", "أسئلة وجوائز ونقاط", this::showCompetitions, Color.rgb(63,60,76));
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

        LinearLayout countdownCard = new LinearLayout(this);
        countdownCard.setOrientation(LinearLayout.VERTICAL);
        countdownCard.setGravity(Gravity.CENTER);
        countdownCard.setPadding(dp(14), dp(14), dp(14), dp(14));
        countdownCard.setBackground(premiumBackground(Color.rgb(10,48,42), Color.rgb(218,184,111), 20));
        LinearLayout.LayoutParams countdownParams = new LinearLayout.LayoutParams(-1, -2);
        countdownParams.setMargins(0, dp(10), 0, dp(8));
        TextView countdownTitle = new TextView(this);
        countdownTitle.setText("⏳ الصلاة القادمة");
        countdownTitle.setTextColor(Color.rgb(255,224,157));
        countdownTitle.setTextSize(18);
        countdownTitle.setGravity(Gravity.CENTER);
        countdownCard.addView(countdownTitle);
        homePrayerCountdown = new TextView(this);
        homePrayerCountdown.setText("جارٍ تحميل مواقيت الصلاة...");
        homePrayerCountdown.setTextColor(Color.WHITE);
        homePrayerCountdown.setTextSize(21);
        homePrayerCountdown.setGravity(Gravity.CENTER);
        homePrayerCountdown.setPadding(4, dp(9), 4, dp(3));
        countdownCard.addView(homePrayerCountdown);
        layout.addView(countdownCard, countdownParams);
        fetchHomePrayerTimes();
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

    void fetchHomePrayerTimes() {
        final int index = Math.max(0, Math.min(governorateNames.length - 1,
            java.util.Arrays.asList(governorateNames).indexOf(selectedGovernorate)));
        new android.os.AsyncTask<Void,Void,String>() {
            protected String doInBackground(Void... unused) {
                HttpURLConnection connection = null;
                try {
                    String date = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
                    double lat = governorateCoordinates[index][0], lon = governorateCoordinates[index][1];
                    URL url = new URL("https://api.aladhan.com/v1/timings/" + date
                        + "?latitude=" + lat + "&longitude=" + lon + "&method=" + prayerCalculationMethod);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(12000); connection.setReadTimeout(12000);
                    connection.connect();
                    if(connection.getResponseCode()!=200) return null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                    StringBuilder body = new StringBuilder(); String line;
                    while((line=reader.readLine())!=null) body.append(line);
                    reader.close();
                    JSONObject timings = new JSONObject(body.toString()).getJSONObject("data").getJSONObject("timings");
                    String[] keys={"Fajr","Dhuhr","Asr","Maghrib","Isha"};
                    StringBuilder values=new StringBuilder();
                    for(String key:keys){ if(values.length()>0) values.append("|"); values.append(timings.getString(key).split(" ")[0]); }
                    return values.toString();
                } catch(Exception e){ return null; }
                finally { if(connection!=null) connection.disconnect(); }
            }
            protected void onPostExecute(String values) {
                if(values==null) {
                    if(homePrayerCountdown!=null) homePrayerCountdown.setText("تعذر تحميل المواقيت. افتح مواقيت الصلاة للتحديث.");
                    return;
                }
                getSharedPreferences("noor_prefs",MODE_PRIVATE).edit()
                    .putString("home_prayer_times",values)
                    .putString("home_prayer_date",new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()))
                    .apply();
                updatePrayerCountdown();
            }
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void updatePrayerCountdown() {
        if(homePrayerCountdown==null) return;
        android.content.SharedPreferences prefs=getSharedPreferences("noor_prefs",MODE_PRIVATE);
        String savedDate=prefs.getString("home_prayer_date","");
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        if(!today.equals(savedDate)) { homePrayerCountdown.setText("جارٍ تحديث مواقيت اليوم..."); fetchHomePrayerTimes(); return; }
        String raw=prefs.getString("home_prayer_times","");
        if(raw.length()==0) { homePrayerCountdown.setText("جارٍ تحميل مواقيت الصلاة..."); return; }
        try {
            String[] times=raw.split("\\|");
            String[] names={"الفجر","الظهر","العصر","المغرب","العشاء"};
            java.text.SimpleDateFormat parser=new java.text.SimpleDateFormat("HH:mm",Locale.US);
            Calendar now=Calendar.getInstance();
            Calendar next=null; String nextName="";
            for(int i=0;i<times.length;i++){
                Date parsed=parser.parse(times[i]);
                Calendar candidate=Calendar.getInstance();
                candidate.setTime(parsed);
                candidate.set(Calendar.YEAR,now.get(Calendar.YEAR));
                candidate.set(Calendar.MONTH,now.get(Calendar.MONTH));
                candidate.set(Calendar.DAY_OF_MONTH,now.get(Calendar.DAY_OF_MONTH));
                candidate.set(Calendar.SECOND,0); candidate.set(Calendar.MILLISECOND,0);
                if(candidate.after(now)){next=candidate; nextName=names[i]; break;}
            }
            if(next==null){
                Date parsed=parser.parse(times[0]);
                next=Calendar.getInstance(); next.setTime(parsed);
                next.add(Calendar.DAY_OF_YEAR,1);
                nextName="الفجر";
            }
            long millis=Math.max(0,next.getTimeInMillis()-System.currentTimeMillis());
            long hours=millis/3600000, minutes=(millis/60000)%60, seconds=(millis/1000)%60;
            homePrayerCountdown.setText("صلاة "+nextName+" بعد\n"+String.format(Locale.US,"%02d:%02d:%02d",hours,minutes,seconds));
        } catch(Exception e){homePrayerCountdown.setText("تعذر حساب الصلاة القادمة.");}
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
        addText("🤲 الأدعية والزيارات والمناجاة", 27, Color.rgb(242,211,145));
        addText("مختارات من كتاب مفاتيح الجنان للشيخ عباس القمي، مع فتح النص من مصدره.", 14, Color.WHITE);
        addAdultCard("🤲 أدعية أيام الأسبوع", "دعاء مستقل لكل يوم", this::showWeeklyDuas);
        addAdultCard("🌹 أدعية أهل البيت عليهم السلام", "دعاء الصباح وكميل والتوسل والسمات والعشرات", this::showAhlulBaytDuas);
        addAdultCard("🕌 زيارات أيام الأسبوع", "زيارات النبي والأئمة بحسب أيام الأسبوع", this::showWeeklyVisits);
        addAdultCard("✨ زيارات أهل البيت عليهم السلام", "زيارة أمين الله ووارث والجامعة الكبيرة وغيرها", this::showAhlulBaytVisits);
        addAdultCard("🌿 المناجاة الخمس عشرة", "مناجاة الإمام زين العابدين عليه السلام", this::showWeeklyMunasajat);
        addAdultCard("📅 ورد اليوم", "دعاء وزيارة ومناجاة اليوم الحالي", this::showTodayDevotions);
        addButton("العودة للرئيسية", this::home);
    }

    void showAhlulBaytDuas() {
        setup();
        addText("🌹 أدعية أهل البيت عليهم السلام", 25, Color.rgb(242,211,145));
        addText("اختر الدعاء لفتح نصه في مفاتيح الجنان.", 14, Color.WHITE);
        String[] names = {"دعاء الصباح لأمير المؤمنين عليه السلام","دعاء كميل بن زياد","دعاء العشرات","دعاء السمات","دعاء التوسل بالأئمة عليهم السلام","دعاء الفرج"};
        int[] pages = {112,115,121,125,167,168};
        for (int i=0;i<names.length;i++) {
            final int page=pages[i]; final String name=names[i];
            addAdultCard("🤲 "+name, "مفاتيح الجنان • افتح النص الكامل", () -> openMafatihPage(page,name));
        }
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void showAhlulBaytVisits() {
        setup();
        addText("🕌 زيارات أهل البيت عليهم السلام", 25, Color.rgb(242,211,145));
        addText("زيارات مختارة من أبواب الزيارات في مفاتيح الجنان.", 14, Color.WHITE);
        String[] names = {"زيارة النبي صلى الله عليه وآله","زيارة فاطمة الزهراء عليها السلام","زيارة أمين الله","زيارة وارث للإمام الحسين عليه السلام","الزيارة الجامعة الكبيرة","زيارة صاحب الزمان عليه السلام"};
        int[] pages = {105,107,218,524,650,111};
        for (int i=0;i<names.length;i++) {
            final int page=pages[i]; final String name=names[i];
            addAdultCard("🌿 "+name, "مفاتيح الجنان • افتح نص الزيارة", () -> openMafatihPage(page,name));
        }
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void openMafatihPage(int page, String title) {
        try {
            String url = "https://books.rafed.net/view/1731/page/" + page;
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            message("تعذر فتح نص " + title + ". تحقق من اتصال الإنترنت.");
        }
    }

    void showWeeklyDuas() {
        setup();
        addText("🤲 أدعية أيام الأسبوع", 26, Color.rgb(242,211,145));
        addText("النصوص بحسب مفاتيح الجنان، نقلاً عن ملحقات الصحيفة السجادية. افتح اليوم لقراءة الدعاء كاملاً.", 14, Color.WHITE);
        String[] days = {"الأحد","الإثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت"};
        int[] pages = {72,73,74,75,76,77,78};
        for (int i = 0; i < days.length; i++) {
            final int page = pages[i];
            final String day = days[i];
            addAdultCard("دعاء يوم " + day, "مفاتيح الجنان • الصفحة " + page, () -> openMafatihPage(page, "دعاء يوم " + day));
        }
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void showWeeklyVisits() {
        setup();
        addText("🕌 زيارات أيام الأسبوع", 26, Color.rgb(242,211,145));
        addText("الزيارات اليومية كما رتبها الشيخ عباس القمي في مفاتيح الجنان.", 14, Color.WHITE);
        String[] days = {"السبت — زيارة النبي ﷺ","الأحد — زيارة أمير المؤمنين والزهراء","الإثنين — زيارة الحسن والحسين","الثلاثاء — زيارة السجاد والباقر والصادق","الأربعاء — زيارة الكاظم والرضا والجواد والهادي","الخميس — زيارة الإمام العسكري","الجمعة — زيارة صاحب الزمان"};
        int[] pages = {105,106,108,109,110,111,112};
        for (int i = 0; i < days.length; i++) {
            final int page = pages[i];
            final String day = days[i];
            addAdultCard(day, "مفاتيح الجنان • الصفحة " + page, () -> openMafatihPage(page, day));
        }
        addButton("رجوع", this::showWeeklyDuasVisits);
    }

    void showWeeklyMunasajat() {
        setup();
        addText("✨ المناجاة الخمس عشرة", 26, Color.rgb(242,211,145));
        addText("هذه مجموعة مناجاة واردة في مفاتيح الجنان، وليست مناجاة منفصلة لكل يوم من أيام الأسبوع.", 14, Color.WHITE);
        String[] names = {
            "مناجاة التائبين","مناجاة الشاكين","مناجاة الخائفين","مناجاة الراجين","مناجاة الراغبين",
            "مناجاة الشاكرين","مناجاة المطيعين لله","مناجاة المريدين","مناجاة المحبين","مناجاة المتوسلين",
            "مناجاة المفتقرين","مناجاة العارفين","مناجاة الذاكرين","مناجاة المعتصمين","مناجاة الزاهدين"
        };
        int[] pages = {178,179,180,181,182,183,184,185,186,187,188,189,190,191,192};
        for (int i = 0; i < names.length; i++) {
            final int page = pages[i];
            final String name = names[i];
            addAdultCard("🌿 " + names[i], "مفاتيح الجنان • الصفحة " + page, () -> openMafatihPage(page, name));
        }
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

    class KidsDrawingView extends View {
        final java.util.ArrayList<android.graphics.Path> paths = new java.util.ArrayList<>();
        final java.util.ArrayList<android.graphics.Paint> paints = new java.util.ArrayList<>();
        android.graphics.Path currentPath;
        android.graphics.Paint currentPaint;
        int brushColor = Color.rgb(35, 125, 95);
        float brushWidth = dp(7);

        KidsDrawingView(android.content.Context context) {
            super(context);
            setBackgroundColor(Color.WHITE);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        void setBrushColor(int color) { brushColor = color; }
        void setBrushWidth(float width) { brushWidth = dp((int) width); }
        void clearDrawing() { paths.clear(); paints.clear(); currentPath = null; invalidate(); }
        @Override protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            android.graphics.Paint border = new android.graphics.Paint(3);
            border.setColor(Color.rgb(238, 214, 160));
            border.setStyle(android.graphics.Paint.Style.STROKE);
            border.setStrokeWidth(dp(2));
            canvas.drawRoundRect(dp(2), dp(2), getWidth()-dp(2), getHeight()-dp(2), dp(14), dp(14), border);
            for (int i=0; i<paths.size(); i++) canvas.drawPath(paths.get(i), paints.get(i));
            if (currentPath != null && currentPaint != null) canvas.drawPath(currentPath, currentPaint);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            float x=event.getX(), y=event.getY();
            if (event.getAction()==android.view.MotionEvent.ACTION_DOWN) {
                currentPath = new android.graphics.Path();
                currentPath.moveTo(x,y);
                currentPaint = new android.graphics.Paint(3);
                currentPaint.setColor(brushColor);
                currentPaint.setStyle(android.graphics.Paint.Style.STROKE);
                currentPaint.setStrokeWidth(brushWidth);
                currentPaint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
                currentPaint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
                invalidate(); return true;
            }
            if (event.getAction()==android.view.MotionEvent.ACTION_MOVE && currentPath!=null) {
                currentPath.lineTo(x,y); invalidate(); return true;
            }
            if (event.getAction()==android.view.MotionEvent.ACTION_UP && currentPath!=null) {
                currentPath.lineTo(x,y);
                paths.add(currentPath);
                paints.add(currentPaint);
                currentPath=null; currentPaint=null; invalidate(); return true;
            }
            return true;
        }
    }

    void showKidsDrawing() {
        setup();
        addText("🎨 مرسمي الصغير", 28, Color.rgb(255,215,120));
        addText("ارسم ولَوِّن كما تحب يا بطل!", 16, Color.WHITE);
        KidsDrawingView board = new KidsDrawingView(this);
        LinearLayout.LayoutParams boardParams = new LinearLayout.LayoutParams(-1, dp(310));
        boardParams.setMargins(0, dp(10), 0, dp(12));
        layout.addView(board, boardParams);
        addText("اختر لونك", 17, Color.rgb(255,225,160));
        int[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.rgb(255,128,0), Color.rgb(120,70,170), Color.rgb(35,125,95)};
        LinearLayout palette = new LinearLayout(this);
        palette.setGravity(Gravity.CENTER);
        palette.setOrientation(LinearLayout.HORIZONTAL);
        for (int color : colors) {
            Button swatch = new Button(this);
            swatch.setText("●");
            swatch.setTextSize(18);
            swatch.setTextColor(color);
            swatch.setMinWidth(dp(34));
            swatch.setPadding(0,0,0,0);
            swatch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(48), 1);
            p.setMargins(dp(2),dp(3),dp(2),dp(3));
            palette.addView(swatch,p);
            swatch.setOnClickListener(v -> board.setBrushColor(color));
        }
        layout.addView(palette);
        LinearLayout tools = new LinearLayout(this);
        tools.setGravity(Gravity.CENTER);
        Button thick = new Button(this); thick.setText("قلم عريض");
        Button thin = new Button(this); thin.setText("قلم رفيع");
        Button eraser = new Button(this); eraser.setText("ممحاة");
        tools.addView(thin,new LinearLayout.LayoutParams(0,-2,1));
        tools.addView(thick,new LinearLayout.LayoutParams(0,-2,1));
        tools.addView(eraser,new LinearLayout.LayoutParams(0,-2,1));
        layout.addView(tools);
        thin.setOnClickListener(v -> board.setBrushWidth(4));
        thick.setOnClickListener(v -> board.setBrushWidth(12));
        eraser.setOnClickListener(v -> board.setBrushColor(Color.WHITE));
        addButton("🧹 مسح الرسمة والبدء من جديد", board::clearDrawing);
        addText("ارسم بإصبعك داخل اللوحة. اختر الممحاة لمسح جزء من الرسم.", 13, Color.LTGRAY);
        addButton("العودة إلى عالم الأطفال", this::showKidsSection);
    }

    void showKidsLearning() {
        setup();
        addText("📚 أتعلم وأكتشف", 28, Color.rgb(255,215,120));
        addText("اضغط على البطاقة لتسمع الحرف والكلمة", 15, Color.WHITE);
        final String[] letters = {"أ - أسد","ب - بطة","ت - تفاحة","ث - ثعلب","ج - جمل","ح - حصان",
            "خ - خروف","د - دب","ذ - ذرة","ر - رمان","ز - زهرة","س - سمكة",
            "ش - شمس","ص - صقر","ض - ضفدع","ط - طائرة","ظ - ظرف","ع - عنب",
            "غ - غزال","ف - فراشة","ق - قمر","ك - كتاب","ل - ليمون","م - موز",
            "ن - نجم","هـ - هلال","و - وردة","ي - يد"};
        final android.speech.tts.TextToSpeech[] speaker = new android.speech.tts.TextToSpeech[1];
        speaker[0] = new android.speech.tts.TextToSpeech(this, status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS && speaker[0] != null)
                speakerLanguageReady(speaker[0]);
        });
        addText("🔤 الحروف العربية", 20, Color.rgb(255,225,160));
        for (String item : letters) {
            Button b = new Button(this);
            b.setText(item + "   🔊");
            b.setTextSize(18);
            b.setAllCaps(false);
            b.setTextColor(Color.rgb(25,73,69));
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(220,245,230)));
            layout.addView(b,new LinearLayout.LayoutParams(-1,-2));
            b.setOnClickListener(v -> {
                if (speaker[0] != null) speaker[0].speak(item.replace(" - ", " مثل "), android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "kids-letter");
            });
        }
        addText("🔢 الأرقام من ١ إلى ١٠", 20, Color.rgb(255,225,160));
        addText("١  •  ٢  •  ٣  •  ٤  •  ٥  •  ٦  •  ٧  •  ٨  •  ٩  •  ١٠", 23, Color.WHITE);
        addText("🌈 الألوان: أحمر، أزرق، أصفر، أخضر، أبيض، أسود", 17, Color.WHITE);
        addText("⭐ تذكّر: تعلم حرفاً جديداً كل يوم!", 16, Color.rgb(255,215,120));
        addButton("العودة إلى عالم الأطفال", () -> {
            try { if (speaker[0] != null) { speaker[0].stop(); speaker[0].shutdown(); } } catch (Exception ignored) {}
            showKidsSection();
        });
    }

    void speakerLanguageReady(android.speech.tts.TextToSpeech speaker) {
        try { speaker.setLanguage(new Locale("ar")); } catch (Exception ignored) {}
    }

    void showKidsMemoryGame() {
        setup();
        addText("🧠 لعبة ذاكرة الأبطال", 27, Color.rgb(255, 215, 120));
        addText("اقلب البطاقات وابحث عن صورتين متشابهتين. اجمع الأزواج الستة!", 15, Color.WHITE);
        final String[] symbols = {"🦁","🌙","⭐","🕌","🌸","📖"};
        final ArrayList<String> cards = new ArrayList<>();
        for (String symbol : symbols) { cards.add(symbol); cards.add(symbol); }
        Collections.shuffle(cards);
        final boolean[] matched = new boolean[12];
        final TextView[] tiles = new TextView[12];
        final int[] firstPick = {-1};
        final int[] moves = {0};
        final int[] pairs = {0};
        final int[] mistakes = {0};
        final int maxMistakes = 3;
        final boolean[] gameOver = {false};
        final TextView status = new TextView(this);
        status.setText("الأخطاء المتبقية: ٣   |   الحركات: ٠   |   الأزواج: ٠ من ٦");
        status.setTextColor(Color.rgb(255, 224, 157));
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(12), 0, dp(12));
        layout.addView(status);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(4);
        layout.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        final boolean[] waiting = {false};
        for (int i = 0; i < 12; i++) {
            final int index = i;
            TextView tile = new TextView(this);
            tiles[i] = tile;
            tile.setText("؟");
            tile.setTextSize(30);
            tile.setTextColor(Color.WHITE);
            tile.setGravity(Gravity.CENTER);
            tile.setBackground(premiumBackground(Color.rgb(24, 76, 65), Color.rgb(218, 184, 111), 16));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dp(88);
            params.columnSpec = GridLayout.spec(i % 3, 1f);
            params.setMargins(dp(5), dp(5), dp(5), dp(5));
            grid.addView(tile, params);
            tile.setOnClickListener(v -> {
                if (gameOver[0] || waiting[0] || matched[index] || tile.getText().toString().equals(cards.get(index))) return;
                tile.setText(cards.get(index));
                tile.setBackground(premiumBackground(Color.rgb(54, 91, 69), Color.rgb(255, 224, 157), 16));
                if (firstPick[0] == -1) {
                    firstPick[0] = index;
                } else {
                    int previous = firstPick[0];
                    firstPick[0] = -1;
                    moves[0]++;
                    if (cards.get(previous).equals(cards.get(index))) {
                        matched[previous] = true;
                        matched[index] = true;
                        pairs[0]++;
                        status.setText("الأخطاء المتبقية: " + toArabicDigits(String.valueOf(maxMistakes - mistakes[0])) + "   |   الحركات: " + toArabicDigits(String.valueOf(moves[0])) + "   |   الأزواج: " + toArabicDigits(String.valueOf(pairs[0])) + " من ٦");
                        tile.setBackground(premiumBackground(Color.rgb(35, 115, 76), Color.rgb(255, 224, 157), 16));
                        tiles[previous].setBackground(premiumBackground(Color.rgb(35, 115, 76), Color.rgb(255, 224, 157), 16));
                        if (pairs[0] == 6) {
                            int best = getSharedPreferences("noor_kids_game", MODE_PRIVATE).getInt("best_moves", 0);
                            if (best == 0 || moves[0] < best) getSharedPreferences("noor_kids_game", MODE_PRIVATE).edit().putInt("best_moves", moves[0]).apply();
                            addText("🎉 أحسنت! وجدت كل الأزواج!", 21, Color.rgb(255, 215, 120));
                            addText("أفضل نتيجة: " + toArabicDigits(String.valueOf(getSharedPreferences("noor_kids_game", MODE_PRIVATE).getInt("best_moves", moves[0]))) + " حركة", 15, Color.WHITE);
                            addButton("العب مرة أخرى 🔄", this::showKidsMemoryGame);
                        }
                    } else {
                        mistakes[0]++;
                        waiting[0] = true;
                        int remaining = maxMistakes - mistakes[0];
                        status.setText("❌ محاولة خاطئة! الأخطاء المتبقية: " + toArabicDigits(String.valueOf(Math.max(0, remaining)))
                            + "   |   الحركات: " + toArabicDigits(String.valueOf(moves[0])));
                        new Handler().postDelayed(() -> {
                            tiles[previous].setText("؟");
                            tile.setText("؟");
                            tiles[previous].setBackground(premiumBackground(Color.rgb(24, 76, 65), Color.rgb(218, 184, 111), 16));
                            tile.setBackground(premiumBackground(Color.rgb(24, 76, 65), Color.rgb(218, 184, 111), 16));
                            waiting[0] = false;
                            if (mistakes[0] >= maxMistakes) {
                                gameOver[0] = true;
                                for (TextView card : tiles) card.setEnabled(false);
                                setup();
                                addText("💔 انتهت اللعبة! نفدت محاولاتك", 24, Color.rgb(255, 190, 170));
                                addText("جمعت " + toArabicDigits(String.valueOf(pairs[0])) + " من ٦ أزواج. ستبدأ من الصفر عند إعادة اللعب.", 17, Color.WHITE);
                                addButton("ابدأ من الصفر 🔄", this::showKidsMemoryGame);
                                addButton("العودة إلى عالم الأطفال", this::showKidsSection);
                            } else {
                                status.setText("الأخطاء المتبقية: " + toArabicDigits(String.valueOf(maxMistakes - mistakes[0]))
                                    + "   |   الحركات: " + toArabicDigits(String.valueOf(moves[0]))
                                    + "   |   الأزواج: " + toArabicDigits(String.valueOf(pairs[0])) + " من ٦");
                            }
                        }, 850);
                    }
                }
            });
        }
        int best = getSharedPreferences("noor_kids_game", MODE_PRIVATE).getInt("best_moves", 0);
        addText("لديك ٣ أخطاء فقط؛ الخطأ الثالث ينهي الجولة ويعيدك إلى البداية.", 14, Color.rgb(255, 190, 170));
        addText(best == 0 ? "ابدأ اللعب واكتشف ذاكرتك!" : "أفضل نتيجة سابقة: " + toArabicDigits(String.valueOf(best)) + " حركة", 14, Color.LTGRAY);
        addButton("إعادة ترتيب البطاقات", this::showKidsMemoryGame);
        addButton("العودة إلى عالم الأطفال", this::showKidsSection);
    }

    void showKidsAcademy() {
        setup();
        addText("🎓 أكاديمية نور بلس للأطفال", 28, Color.rgb(255, 215, 120));
        addText("مسارات تعليمية ممتعة للحروف العربية والتلاوة والألعاب", 16, Color.WHITE);
        addText("الاشتراك الشهري: ٣٠٬٠٠٠ دينار عراقي", 16, Color.rgb(255, 225, 160));
        addText("🌱 دروس مجانية للتجربة", 21, Color.rgb(255, 225, 160));
        addKidsCard("🔤 الحروف العربية", "تعلم الحروف مع النطق والأمثلة", "📚",
            Color.rgb(220, 245, 230), this::showKidsLearning);
        addKidsCard("🎧 الاستماع والتلاوة", "استمع إلى القرآن واختر القارئ والسورة", "📖",
            Color.rgb(220, 235, 255), this::audioScreen);
        addKidsCard("🧠 لعبة الذاكرة", "تدرب على التركيز ومطابقة الصور", "🎮",
            Color.rgb(224, 232, 255), this::showKidsMemoryGame);
        addText("⭐ مسارات نور بلس التعليمية", 21, Color.rgb(255, 225, 160));
        addKidsCard("✍️ الحروف والكتابة المتقدمة", "تمارين كتابة وتتبع الحروف والكلمات مع مستويات", "🔒",
            Color.rgb(255, 238, 202), this::showKidsPremiumInfo);
        addKidsCard("🕌 تعلّم التلاوة والتجويد", "دروس صوتية متدرجة ومراجعة مخارج الحروف وأحكام التجويد", "🔒",
            Color.rgb(244, 226, 250), this::showKidsPremiumInfo);
        addKidsCard("🏆 ألعاب وتحديات تعليمية", "مراحل وأسئلة ومكافآت تعليمية لمراجعة ما تعلمه الطفل", "🔒",
            Color.rgb(255, 231, 190), this::showKidsPremiumInfo);
        addText("المحتوى المميز سيُفتح بعد تفعيل الاشتراك والدفع الرسمي. لا توجد عملية دفع مفعّلة في هذه النسخة حالياً.", 14, Color.LTGRAY);
        addButton("العودة إلى عالم الأطفال", this::showKidsSection);
        addButton("العودة للرئيسية", this::home);
    }

    void showKidsPremiumInfo() {
        setup();
        addText("⭐ محتوى نور بلس للأطفال", 26, Color.rgb(255, 215, 120));
        addText("هذا المسار مخصص للمشتركين، ويشمل الحروف والكتابة والتلاوة والألعاب التعليمية المتدرجة.", 17, Color.WHITE);
        addText("الاشتراك الشهري: ٣٠٬٠٠٠ دينار عراقي", 17, Color.rgb(255, 225, 160));
        addText("🔒 الدفع والفتح التلقائي للدروس غير مفعّلين بعد. سيتم ذلك بعد إعداد Google Play Console وربط Google Play Billing والتحقق من الشراء.", 15, Color.rgb(255, 225, 160));
        addButton("العودة إلى أكاديمية الأطفال", this::showKidsAcademy);
    }

    void showAdultsAcademy() {
        setup();
        addText("🎓 أكاديمية نور بلس للكبار", 28, Color.rgb(242, 211, 145));
        addText("مسارات منظمة لتحسين التلاوة وبناء ورد قرآني ثابت", 16, Color.WHITE);
        addText("الاشتراك الشهري: ٣٠٬٠٠٠ دينار عراقي", 16, Color.rgb(255, 225, 160));
        addText("🌿 مسارات التعلم", 21, Color.rgb(242, 211, 145));
        addAdultCard("🗣️ تصحيح التلاوة ومخارج الحروف", "تدريبات للاستماع والنطق الصحيح ومراجعة مخارج الحروف", this::showAdultsPremiumInfo);
        addAdultCard("📘 أحكام التجويد", "أحكام النون والميم والمدود والوقف والابتداء", this::showAdultsPremiumInfo);
        addAdultCard("📖 الحفظ والمراجعة", "خطط للحفظ والتثبيت ومتابعة الإنجاز", this::showAdultsPremiumInfo);
        addAdultCard("📝 اختبارات وشهادات", "اختبارات مستوى ومراجعة وشهادات إتمام", this::showAdultsPremiumInfo);
        addText("يمكنك الاستفادة من قراءة القرآن والاستماع والأذكار المتاحة مجاناً في التطبيق.", 14, Color.LTGRAY);
        addButton("العودة إلى قسم الكبار", this::showAdultSection);
    }

    void showAdultsPremiumInfo() {
        setup();
        addText("⭐ مسار نور بلس للكبار", 26, Color.rgb(242, 211, 145));
        addText("يتضمن هذا المسار دروساً متدرجة في تصحيح التلاوة ومخارج الحروف وأحكام التجويد والحفظ والمراجعة والاختبارات.", 17, Color.WHITE);
        addText("السعر المعتمد: ٣٠٬٠٠٠ دينار عراقي شهرياً", 17, Color.rgb(255, 225, 160));
        addText("🔒 الاشتراك والدفع الفعلي لم يُفعّلا بعد. يلزم إعداد حساب Google Play Console وربط Google Play Billing والتحقق الآمن من الشراء قبل فتح المحتوى.", 15, Color.rgb(255, 225, 160));
        addButton("العودة إلى أكاديمية الكبار", this::showAdultsAcademy);
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
        addKidsCard("🎨 مرسمي الصغير", "ارسم بالألوان واختر القلم والممحاة وامسح لوحتك", "🖍️",
            Color.rgb(255, 231, 190), this::showKidsDrawing);
        addKidsCard("📚 أتعلم وأكتشف", "تعلم الحروف العربية والأرقام والألوان مع النطق الصوتي", "🔤",
            Color.rgb(220, 245, 230), this::showKidsLearning);
        addKidsCard("🧠 لعبة ذاكرة الأبطال", "اقلب البطاقات وطابق الصور واجمع الأزواج الستة", "🎮",
            Color.rgb(224, 232, 255), this::showKidsMemoryGame);
        addKidsCard("🫧 لعبة فقاعات الألوان", "صوّب الفقاعات واجمع ٣ كرات متشابهة أو أكثر", "🌈",
            Color.rgb(205, 235, 255), this::showBubbleShooter);
        addKidsCard("🎓 أكاديمية نور للأطفال", "دروس حروف وتلاوة وألعاب تعليمية مع مسارات نور بلس", "⭐",
            Color.rgb(255, 226, 185), this::showKidsAcademy);
        addKidsCard("⭐ تحدي اليوم", "خمسة أسئلة قرآنية وثقافية مع نقاط وشارات", "🏆",
            Color.rgb(255, 225, 218), () -> showDailyChallengeQuestion(0, 0));
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
        addAdultCard("🎓 أكاديمية نور بلس للكبار", "تعلم التلاوة والتجويد والحفظ والمراجعة", this::showAdultsAcademy);
        addAdultCard("👩‍🏫 معلّمو نور القرآن", "احجز درساً خاصاً أو قدّم طلب الانضمام معلّماً بمقابل مالي", this::showTeacherHub);
        addText("اجعل لك وقتاً ثابتاً للقرآن والذكر والعمل الصالح", 14,
            Color.rgb(238, 204, 125));
        addButton("العودة للرئيسية", this::home);
    }

    void showTeacherHub() {
        setup();
        addText("👩‍🏫 معلّمو نور القرآن", 28, Color.rgb(242,211,145));
        addText("دروس فردية مدفوعة عن بُعد فقط للكبار والأطفال، مع معلّمين يتم التحقق من مؤهلاتهم قبل اعتمادهم.", 16, Color.WHITE);
        addAdultCard("📚 احجز درساً", "تجويد • تصحيح قراءة • مخارج الحروف • حفظ ومراجعة", this::showLessonRequest);
        addAdultCard("📝 انضم إلينا معلّماً", "قدّم بياناتك وتخصصك وخبرتك لمراجعتها", this::showTeacherApplication);
        addText("الأسعار والمواعيد تُحدد مع المعلّم بعد قبول الطلب. لا يوجد تحصيل إلكتروني داخل التطبيق حالياً، ولا يتم تأكيد الحجز أو الدفع تلقائياً.", 13, Color.rgb(238,204,125));
        addButton("العودة إلى قسم الكبار", this::showAdultSection);
    }

    EditText teacherInput(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextColor(Color.WHITE);
        field.setHintTextColor(Color.LTGRAY);
        field.setSingleLine(true);
        field.setPadding(16, 10, 16, 10);
        layout.addView(field, new LinearLayout.LayoutParams(-1, -2));
        return field;
    }

    Spinner teacherSpinner(String[] values) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        layout.addView(spinner, new LinearLayout.LayoutParams(-1, -2));
        return spinner;
    }

    void showLessonRequest() {
        setup();
        addText("📚 طلب حجز درس مدفوع", 25, Color.rgb(242,211,145));
        addText("املأ البيانات وسنرسل طلبك للمراجعة والتواصل معك.", 14, Color.WHITE);
        EditText student = teacherInput("اسم الطالب أو ولي الأمر");
        EditText phone = teacherInput("رقم الهاتف مع رمز الدولة");
        Spinner audience = teacherSpinner(new String[]{"الفئة: اختر","للكبار","للأطفال"});
        Spinner subject = teacherSpinner(new String[]{"نوع الدرس","تعليم القراءة","تصحيح التلاوة ومخارج الحروف","أحكام التجويد","الحفظ والمراجعة"});
        addText("طريقة الدرس: عن بُعد فقط عبر الإنترنت", 15, Color.rgb(238,204,125));
        EditText time = teacherInput("الوقت المناسب للتواصل");
        EditText note = teacherInput("ملاحظات إضافية (اختياري)");
        addButton("إرسال طلب الحجز", () -> {
            String name=student.getText().toString().trim(), mobile=phone.getText().toString().trim();
            if(name.isEmpty() || mobile.isEmpty() || audience.getSelectedItemPosition()==0 || subject.getSelectedItemPosition()==0) {
                message("أكمل الاسم والهاتف والفئة ونوع الدرس وطريقته أولاً."); return;
            }
            Map<String,Object> data=new HashMap<>();
            data.put("studentOrGuardian",name); data.put("phone",mobile);
            data.put("audience",audience.getSelectedItem().toString());
            data.put("subject",subject.getSelectedItem().toString());
            data.put("lessonMode","عن بُعد عبر الإنترنت فقط");
            data.put("preferredTime",time.getText().toString().trim());
            data.put("note",note.getText().toString().trim());
            data.put("status","pending_review"); data.put("createdAt",System.currentTimeMillis());
            ensureFirebaseLogin(() -> FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("teacherLessonRequests")
                .push().setValue(data).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) { message("تم إرسال طلبك للمراجعة. سنتواصل معك بعد توفر معلّم مناسب."); showTeacherHub(); }
                    else message("تعذر إرسال الطلب. تحقق من اتصال الإنترنت وصلاحيات قاعدة البيانات.");
                }));
        });
        addButton("رجوع", this::showTeacherHub);
    }

    void showTeacherApplication() {
        setup();
        addText("📝 طلب الانضمام معلّماً", 25, Color.rgb(242,211,145));
        addText("لا يُعرض أي معلّم للطلاب قبل مراجعة بياناته ومؤهلاته.", 14, Color.WHITE);
        EditText name=teacherInput("الاسم الكامل");
        EditText phone=teacherInput("رقم الهاتف");
        EditText city=teacherInput("المحافظة / المدينة");
        Spinner specialty=teacherSpinner(new String[]{"التخصص","تعليم القراءة","التجويد ومخارج الحروف","الحفظ والتحفيظ","أكثر من تخصص"});
        EditText experience=teacherInput("سنوات الخبرة");
        EditText qualification=teacherInput("الشهادة أو الإجازة أو المؤهل");
        EditText price=teacherInput("السعر المقترح للدرس بالدينار العراقي");
        EditText details=teacherInput("نبذة عنك وطريقة التدريس");
        addButton("إرسال طلب الانضمام", () -> {
            if(name.getText().toString().trim().isEmpty() || phone.getText().toString().trim().isEmpty() ||
                city.getText().toString().trim().isEmpty() || specialty.getSelectedItemPosition()==0 ||
                qualification.getText().toString().trim().isEmpty()) {
                message("أكمل الاسم والهاتف والمدينة والتخصص والمؤهل."); return;
            }
            Map<String,Object> data=new HashMap<>();
            data.put("name",name.getText().toString().trim()); data.put("phone",phone.getText().toString().trim());
            data.put("city",city.getText().toString().trim()); data.put("specialty",specialty.getSelectedItem().toString());
            data.put("experience",experience.getText().toString().trim()); data.put("qualification",qualification.getText().toString().trim());
            data.put("proposedPriceIQD",price.getText().toString().trim()); data.put("details",details.getText().toString().trim());
            data.put("status","pending_verification"); data.put("createdAt",System.currentTimeMillis());
            ensureFirebaseLogin(() -> FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("teacherApplications")
                .push().setValue(data).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) { message("وصل طلبك. ستتم مراجعة المؤهلات قبل اعتماد الحساب."); showTeacherHub(); }
                    else message("تعذر إرسال الطلب. تحقق من اتصال الإنترنت وصلاحيات قاعدة البيانات.");
                }));
        });
        addButton("رجوع", this::showTeacherHub);
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
        stopLegalAdhan();
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


    String[] quizQuestions = {
        "ما السورة التي تُسمّى أمّ الكتاب؟",
        "كم عدد أركان الإسلام؟",
        "ما أطول سورة في القرآن الكريم؟",
        "في أي شهر يصوم المسلمون؟",
        "ما اسم الليلة التي هي خير من ألف شهر؟",
        "كم عدد أجزاء القرآن الكريم؟",
        "ما السورة التي لا تبدأ بالبسملة؟",
        "من النبي الذي ابتلعه الحوت؟",
        "ما قبلة المسلمين في الصلاة؟",
        "ما أقصر سورة في القرآن الكريم؟",
        "كم عدد الصلوات المفروضة في اليوم والليلة؟",
        "ما أول سورة في ترتيب المصحف؟",
        "ما السورة التي تعدل ثلث القرآن؟",
        "من هو خاتم الأنبياء والمرسلين؟",
        "ما اسم الكتاب الذي أُنزل على النبي محمد ﷺ؟",
        "في أي مدينة وُلد النبي محمد ﷺ؟",
        "ما اسم الغار الذي نزل فيه الوحي أول مرة؟",
        "كم عدد أركان الإيمان؟",
        "ما الشهر الذي يأتي بعد رمضان؟",
        "إلى أي مسجد أُسري بالنبي ﷺ؟",
        "من النبي الذي كلمه الله؟",
        "ما السورة التي وردت فيها آية الكرسي؟",
        "ما اسم والدة النبي عيسى عليه السلام؟",
        "كم عدد سور القرآن الكريم؟",
        "ما الصلاة التي لا ركوع فيها ولا سجود؟"
    };
    String[][] quizOptions = {
        {"الفاتحة","البقرة","الإخلاص","الناس"},
        {"ثلاثة","أربعة","خمسة","ستة"},
        {"آل عمران","البقرة","النساء","المائدة"},
        {"شعبان","رمضان","محرم","شوال"},
        {"ليلة القدر","ليلة النصف من شعبان","ليلة الإسراء","ليلة الجمعة"},
        {"٢٠ جزءاً","٢٥ جزءاً","٣٠ جزءاً","٤٠ جزءاً"},
        {"التوبة","يونس","الأنفال","هود"},
        {"موسى عليه السلام","يونس عليه السلام","إبراهيم عليه السلام","يوسف عليه السلام"},
        {"المسجد الأقصى","الكعبة المشرفة","المسجد النبوي","جبل عرفات"},
        {"الكوثر","العصر","الإخلاص","الفلق"},
        {"ثلاث صلوات","أربع صلوات","خمس صلوات","ست صلوات"},
        {"الفاتحة","البقرة","العلق","الناس"},
        {"الإخلاص","الفلق","الناس","الكوثر"},
        {"موسى عليه السلام","عيسى عليه السلام","محمد ﷺ","إبراهيم عليه السلام"},
        {"التوراة","الإنجيل","الزبور","القرآن الكريم"},
        {"المدينة المنورة","مكة المكرمة","الطائف","القدس"},
        {"غار حراء","غار ثور","جبل أحد","جبل النور"},
        {"خمسة","ستة","سبعة","ثمانية"},
        {"شوال","ذو القعدة","محرم","رجب"},
        {"المسجد النبوي","المسجد الأقصى","المسجد الحرام","مسجد قباء"},
        {"إبراهيم عليه السلام","موسى عليه السلام","يوسف عليه السلام","نوح عليه السلام"},
        {"البقرة","آل عمران","النساء","المائدة"},
        {"آسية","مريم عليها السلام","هاجر","سارة"},
        {"١١٤ سورة","١٠٠ سورة","١١٠ سور","١٢٠ سورة"},
        {"صلاة الجنازة","صلاة الفجر","صلاة المغرب","صلاة العيد"}
    };
    int[] quizCorrect = {0,2,1,1,0,2,0,1,1,0,2,0,0,2,3,1,0,1,0,1,1,0,1,0,0};
    String[] quizExplanations = {
        "سورة الفاتحة تُسمّى أمّ الكتاب وأمّ القرآن.",
        "أركان الإسلام خمسة.",
        "سورة البقرة هي أطول سور القرآن الكريم.",
        "يصوم المسلمون شهر رمضان.",
        "ليلة القدر خير من ألف شهر.",
        "القرآن الكريم ثلاثون جزءاً.",
        "سورة التوبة لا تبدأ بالبسملة.",
        "النبي يونس عليه السلام.",
        "قبلة المسلمين هي الكعبة المشرفة.",
        "سورة الكوثر هي أقصر سور القرآن.",
        "الصلوات المفروضة خمس.",
        "سورة الفاتحة هي أول سور المصحف.",
        "ورد في الحديث الصحيح أن سورة الإخلاص تعدل ثلث القرآن.",
        "محمد ﷺ خاتم الأنبياء والمرسلين.",
        "القرآن الكريم أُنزل على النبي محمد ﷺ.",
        "وُلد النبي محمد ﷺ في مكة المكرمة.",
        "بدأ نزول الوحي في غار حراء.",
        "أركان الإيمان ستة.",
        "يأتي شهر شوال بعد شهر رمضان.",
        "كان الإسراء إلى المسجد الأقصى.",
        "كلم الله موسى عليه السلام تكليماً.",
        "آية الكرسي في سورة البقرة.",
        "مريم عليها السلام هي والدة عيسى عليه السلام.",
        "عدد سور القرآن الكريم ١١٤ سورة.",
        "صلاة الجنازة ليس فيها ركوع ولا سجود."
    };
    int quizIndex = 0;
    int quizScore = 0;
    int quizMistakes = 0;
    final int quizMaxMistakes = 3;
    List<Integer> quizRoundIndices = new ArrayList<>();
    boolean quizAnswered = false;
    TextView quizFeedback;


    void showAdvertiserHub() {
        setup();
        addText("📢 أعلن معنا في نور القرآن",27,Color.rgb(242,211,145));
        addText("أرسل طلب إعلان أو رعاية للمسابقات. لا يُنشر أي إعلان قبل المراجعة والموافقة.",15,Color.WHITE);
        addAdultCard("📝 طلب إعلان جديد","بيانات نشاطك والباقـة المناسبة",this::showAdvertiserRequest);
        addAdultCard("🏆 رعاية تحدي اليوم","ادعم الأنشطة التعليمية والمسابقات",this::showAdvertiserRequest);
        addText("الباقات المقترحة: إعلان صغير ٥٠٬٠٠٠ د.ع، إعلان مميز ١٠٠٬٠٠٠ د.ع، راعي تحدي اليوم ١٥٠٬٠٠٠ د.ع شهرياً. الأسعار قابلة للتفاوض.",14,Color.rgb(238,204,125));
        addButton("العودة للمسابقات",this::showCompetitions);
    }
    void showAdvertiserRequest() {
        setup();
        addText("📝 طلب الإعلان والرعاية",25,Color.rgb(242,211,145));
        addText("املأ البيانات. سيُحفظ الطلب للمراجعة ولن يظهر إعلانك تلقائياً.",14,Color.WHITE);
        EditText business=teacherInput("اسم الشركة أو المكتبة أو المركز");
        EditText contact=teacherInput("اسم الشخص المسؤول");
        EditText phone=teacherInput("رقم الهاتف مع رمز الدولة");
        Spinner packageType=teacherSpinner(new String[]{"اختر الباقة","إعلان صغير — ٥٠٬٠٠٠ د.ع شهرياً","إعلان مميز — ١٠٠٬٠٠٠ د.ع شهرياً","راعي تحدي اليوم — ١٥٠٬٠٠٠ د.ع شهرياً"});
        EditText description=teacherInput("وصف الإعلان أو النشاط");
        EditText duration=teacherInput("المدة المطلوبة (مثلاً شهر واحد)");
        EditText link=teacherInput("رابط الموقع أو صفحة التواصل (اختياري)");
        addButton("إرسال الطلب ومشاركته عبر واتساب",() -> {
            String company=business.getText().toString().trim(), person=contact.getText().toString().trim();
            String mobile=phone.getText().toString().trim(), desc=description.getText().toString().trim();
            if(company.isEmpty()||person.isEmpty()||mobile.isEmpty()||packageType.getSelectedItemPosition()==0||desc.isEmpty()) { message("أكمل اسم النشاط والمسؤول والهاتف والباقة ووصف الإعلان."); return; }
            String selected=packageType.getSelectedItem().toString(), requestedDuration=duration.getText().toString().trim(), website=link.getText().toString().trim();
            Map<String,Object> data=new HashMap<>();
            data.put("businessName",company); data.put("contactPerson",person); data.put("phone",mobile); data.put("package",selected);
            data.put("description",desc); data.put("duration",requestedDuration); data.put("link",website); data.put("placement","competitions");
            data.put("status","pending_review"); data.put("createdAt",System.currentTimeMillis());
            ensureFirebaseLogin(() -> FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("advertiserRequests").push().setValue(data).addOnCompleteListener(task -> {
                if(!task.isSuccessful()) { message("تعذر حفظ الطلب. تحقق من الإنترنت وصلاحيات Firebase."); return; }
                String shareText="طلب إعلان في نور القرآن\nالنشاط: "+company+"\nالمسؤول: "+person+"\nالهاتف: "+mobile+"\nالباقة: "+selected+"\nالوصف: "+desc+"\nالمدة: "+requestedDuration+(website.isEmpty()?"":"\nالرابط: "+website);
                try { android.content.Intent share=new android.content.Intent(android.content.Intent.ACTION_SEND); share.setType("text/plain"); share.putExtra(android.content.Intent.EXTRA_TEXT,shareText); startActivity(android.content.Intent.createChooser(share,"إرسال الطلب عبر واتساب أو تطبيق آخر")); } catch(Exception ignored) {}
                message("تم حفظ الطلب للمراجعة. اختر واتساب من قائمة المشاركة لإرساله أيضاً."); showCompetitions();
            }));
        });
        addButton("رجوع",this::showAdvertiserHub);
    }
    void showApprovedCompetitionSponsors() {
        final LinearLayout target=layout;
        FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL).getReference("approvedSponsors").child("competitions").orderByChild("active").equalTo(true).limitToFirst(5)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snapshot) {
                    if(layout!=target||!snapshot.exists()) return;
                    addText("🤝 رعاة تحديات نور القرآن",18,Color.rgb(242,211,145));
                    for(DataSnapshot item:snapshot.getChildren()) {
                        String name=item.child("name").getValue(String.class), desc=item.child("description").getValue(String.class), url=item.child("url").getValue(String.class);
                        if(name==null||name.trim().isEmpty()) continue;
                        addAdultCard("⭐ "+name,desc==null?"راعي أنشطة نور القرآن":desc,() -> {
                            if(url!=null&&(url.startsWith("https://")||url.startsWith("http://"))) { try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(url))); } catch(Exception e) { message("تعذر فتح الرابط."); } }
                            else message("لا يوجد رابط لهذا الراعي.");
                        });
                    }
                }
                @Override public void onCancelled(DatabaseError error) {}
            });
    }

    void showCompetitions() {
        setup();
        layout.setPadding(dp(14), dp(12), dp(14), dp(22));
        android.content.SharedPreferences prefs = getSharedPreferences("noor_quiz", MODE_PRIVATE);
        int points = prefs.getInt("points", 0);
        int played = prefs.getInt("played", 0);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(18), dp(20), dp(18), dp(20));
        hero.setBackground(premiumBackground(Color.rgb(13, 55, 47), Color.rgb(218, 184, 111), 24));
        TextView icon = new TextView(this);
        icon.setText("🏆");
        icon.setTextSize(38);
        icon.setGravity(Gravity.CENTER);
        hero.addView(icon);
        TextView title = new TextView(this);
        title.setText("مسابقات نور القرآن");
        title.setTextColor(Color.rgb(255, 229, 170));
        title.setTextSize(26);
        title.setTypeface(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        hero.addView(title);
        TextView desc = new TextView(this);
        desc.setText("اختبر معلوماتك واجمع النقاط مع كل إجابة صحيحة");
        desc.setTextColor(Color.WHITE);
        desc.setTextSize(14);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, dp(8), 0, 0);
        hero.addView(desc);
        layout.addView(hero);

        LinearLayout scoreCard = new LinearLayout(this);
        scoreCard.setOrientation(LinearLayout.HORIZONTAL);
        scoreCard.setGravity(Gravity.CENTER);
        scoreCard.setPadding(dp(10), dp(14), dp(10), dp(14));
        scoreCard.setBackground(premiumBackground(Color.rgb(20, 63, 56), Color.rgb(190, 158, 87), 18));
        TextView total = new TextView(this);
        total.setText("⭐ رصيدك\n" + toArabicDigits(String.valueOf(points)) + " نقطة");
        total.setTextColor(Color.rgb(255, 224, 157));
        total.setTextSize(18);
        total.setGravity(Gravity.CENTER);
        scoreCard.addView(total, new LinearLayout.LayoutParams(0, -2, 1));
        TextView games = new TextView(this);
        games.setText("🎯 المشاركات\n" + toArabicDigits(String.valueOf(played)));
        games.setTextColor(Color.WHITE);
        games.setTextSize(16);
        games.setGravity(Gravity.CENTER);
        scoreCard.addView(games, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(-1, -2);
        scoreParams.setMargins(0, dp(12), 0, dp(10));
        layout.addView(scoreCard, scoreParams);

        addAdultCard("🌟 تحدي اليوم", "٥ أسئلة يومية • نقاط إضافية • محاولة واحدة كل يوم", this::startDailyChallenge);
        addAdultCard("📖 المسابقة القرآنية والإسلامية", "٢٥ سؤالاً متنوعاً • تظهر ١٠ أسئلة عشوائية في كل جولة • ١٠ نقاط لكل إجابة صحيحة", () -> startQuiz());
        addAdultCard("🏆 لوحة المتصدرين", "شاهد أفضل المشاركين ونقاطهم عبر الإنترنت", this::showLeaderboard);
        addAdultCard("🎁 كيف تربح؟", "اجمع النقاط بالإجابة الصحيحة، ثم تابع رصيدك داخل التطبيق.", () ->
            message("كل إجابة صحيحة تمنحك ١٠ نقاط. النقاط رمزية داخل التطبيق وليست أموالاً نقدية."));
        addText("مستوياتك: مبتدئ ٠–٤٩ • مجتهد ٥٠–١٤٩ • متميز ١٥٠–٢٩٩ • نابغة ٣٠٠ نقطة فأكثر", 13, Color.rgb(242, 211, 145));
        addText("المسابقات تعليمية وترفيهية، ولا توجد جوائز مالية أو سحب نقدي حالياً.", 12, Color.LTGRAY);
        addButton("العودة للرئيسية", this::home);
        addAdultCard("📢 أعلن معنا / اطلب رعاية","قدّم طلب إعلانك في المسابقات وتحدي اليوم",this::showAdvertiserHub);
        showApprovedCompetitionSponsors();
        addText("إعلان", 11, Color.LTGRAY);
        AdView banner = new AdView(this);
        banner.setAdSize(AdSize.BANNER);
        banner.setAdUnitId(ADMOB_TEST_BANNER_ID);
        layout.addView(banner, new LinearLayout.LayoutParams(-1, -2));
        banner.loadAd(new AdRequest.Builder().build());
    }

    int dailyChallengeIndex = 0;
    int dailyChallengeScore = 0;
    ArrayList<Integer> dailyChallengeQuestions = new ArrayList<>();
    boolean dailyChallengeAnswered = false;

    String dailyChallengeKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    void startDailyChallenge() {
        android.content.SharedPreferences prefs = getSharedPreferences("noor_daily_challenge", MODE_PRIVATE);
        String today = dailyChallengeKey();
        if (today.equals(prefs.getString("played_day", ""))) {
            setup();
            addText("🌟 تحدي اليوم", 27, Color.rgb(242,211,145));
            addText("لقد شاركت في تحدي اليوم بالفعل. عُد غداً لتحدٍ جديد!", 18, Color.WHITE);
            addText("نقاطك المكتسبة اليوم: " + toArabicDigits(String.valueOf(prefs.getInt("today_score",0))), 17, Color.rgb(255,224,157));
            addButton("العودة للمسابقات", this::showCompetitions);
            return;
        }
        dailyChallengeIndex = 0;
        dailyChallengeScore = 0;
        dailyChallengeQuestions.clear();
        int seed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        for (int i=0;i<5;i++) dailyChallengeQuestions.add((seed * 7 + i * 5) % quizQuestions.length);
        showDailyChallengeQuestion();
    }

    void showDailyChallengeQuestion() {
        final int questionId = dailyChallengeQuestions.get(dailyChallengeIndex);
        setup();
        addText("🌟 تحدي اليوم", 27, Color.rgb(242,211,145));
        addText("السؤال " + toArabicDigits(String.valueOf(dailyChallengeIndex+1)) + " من ٥", 17, Color.WHITE);
        android.widget.ProgressBar progress = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(5); progress.setProgress(dailyChallengeIndex+1);
        layout.addView(progress, new LinearLayout.LayoutParams(-1, dp(7)));
        TextView q = new TextView(this);
        q.setText(quizQuestions[questionId]); q.setTextColor(Color.rgb(255,229,170)); q.setTextSize(21);
        q.setGravity(Gravity.CENTER); q.setPadding(dp(12),dp(22),dp(12),dp(22));
        q.setBackground(premiumBackground(Color.rgb(13,55,47),Color.rgb(218,184,111),20));
        layout.addView(q,new LinearLayout.LayoutParams(-1,-2));
        TextView feedback = new TextView(this); feedback.setText("اختر إجابة واحدة. كل إجابة صحيحة = ٥ نقاط.");
        feedback.setTextColor(Color.WHITE); feedback.setTextSize(15); feedback.setGravity(Gravity.CENTER);
        feedback.setPadding(dp(8),dp(12),dp(8),dp(12)); layout.addView(feedback);
        dailyChallengeAnswered = false;
        for(int i=0;i<4;i++) {
            final int choice=i;
            Button b=new Button(this); b.setText(quizOptions[questionId][i]); b.setAllCaps(false);
            b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25,105,87)));
            layout.addView(b,new LinearLayout.LayoutParams(-1,-2));
            b.setOnClickListener(v -> {
                if(dailyChallengeAnswered) return;
                dailyChallengeAnswered=true;
                for(int j=0;j<layout.getChildCount();j++) if(layout.getChildAt(j) instanceof Button) ((Button)layout.getChildAt(j)).setEnabled(false);
                if(choice==quizCorrect[questionId]) {
                    dailyChallengeScore+=5;
                    feedback.setText("✅ إجابة صحيحة! +٥ نقاط\n"+quizExplanations[questionId]);
                    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(35,135,83)));
                } else {
                    feedback.setText("❌ ليست الإجابة الصحيحة.\nالإجابة: "+quizOptions[questionId][quizCorrect[questionId]]+"\n"+quizExplanations[questionId]);
                    b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(145,65,55)));
                }
                addButton(dailyChallengeIndex==4?"إنهاء التحدي 🏆":"السؤال التالي ←", () -> {
                    if(dailyChallengeIndex==4) finishDailyChallenge();
                    else { dailyChallengeIndex++; showDailyChallengeQuestion(); }
                });
            });
        }
        addButton("إلغاء", this::showCompetitions);
    }

    void finishDailyChallenge() {
        android.content.SharedPreferences prefs=getSharedPreferences("noor_daily_challenge",MODE_PRIVATE);
        int bonus=dailyChallengeScore==25?10:0;
        int earned=dailyChallengeScore+bonus;
        prefs.edit().putString("played_day",dailyChallengeKey()).putInt("today_score",earned).apply();
        android.content.SharedPreferences total=getSharedPreferences("noor_quiz",MODE_PRIVATE);
        int points=total.getInt("points",0)+earned;
        total.edit().putInt("points",points).putInt("played",total.getInt("played",0)+1).apply();
        submitLeaderboardScore(points);
        setup();
        addText("🎉 انتهى تحدي اليوم",27,Color.rgb(242,211,145));
        addText("حصلت على "+toArabicDigits(String.valueOf(dailyChallengeScore))+" من ٢٥ نقطة",20,Color.WHITE);
        if(bonus>0) addText("⭐ مكافأة إكمال كاملة: +١٠ نقاط",18,Color.rgb(255,224,157));
        addText("رصيد النقاط الجديد: "+toArabicDigits(String.valueOf(points)),18,Color.WHITE);
        addText("النقاط مكافآت تعليمية داخل التطبيق وليست نقوداً قابلة للسحب أو جائزة نقدية.",13,Color.LTGRAY);
        addButton("العودة للمسابقات",this::showCompetitions);
    }

    void startQuiz() {
        quizIndex = 0;
        quizScore = 0;
        quizMistakes = 0;
        quizRoundIndices.clear();
        for (int i = 0; i < quizQuestions.length; i++) quizRoundIndices.add(i);
        Collections.shuffle(quizRoundIndices);
        quizRoundIndices = new ArrayList<>(quizRoundIndices.subList(0, 10));
        showQuizQuestion();
    }

    void showQuizQuestion() {
        final int questionId = quizRoundIndices.get(quizIndex);
        setup();
        layout.setPadding(dp(14), dp(12), dp(14), dp(22));
        addText("🏆 المسابقة القرآنية", 25, Color.rgb(242, 211, 145));
        addText("السؤال " + toArabicDigits(String.valueOf(quizIndex + 1)) + " من ١٠", 16, Color.WHITE);
        addText("المحاولات الخاطئة المتبقية: " + toArabicDigits(String.valueOf(quizMaxMistakes - quizMistakes)) + " من ٣", 16, Color.rgb(255, 190, 170));

        android.widget.ProgressBar progress = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(10);
        progress.setProgress(quizIndex + 1);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(218, 184, 111)));
        layout.addView(progress, new LinearLayout.LayoutParams(-1, dp(7)));

        TextView question = new TextView(this);
        question.setText(quizQuestions[questionId]);
        question.setTextColor(Color.rgb(255, 229, 170));
        question.setTextSize(21);
        question.setTypeface(null, android.graphics.Typeface.BOLD);
        question.setGravity(Gravity.CENTER);
        question.setPadding(dp(12), dp(24), dp(12), dp(22));
        question.setBackground(premiumBackground(Color.rgb(13, 55, 47), Color.rgb(218, 184, 111), 20));
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.setMargins(0, dp(14), 0, dp(12));
        layout.addView(question, qp);

        quizFeedback = new TextView(this);
        quizFeedback.setText("اختر الإجابة الصحيحة");
        quizFeedback.setTextColor(Color.WHITE);
        quizFeedback.setTextSize(15);
        quizFeedback.setGravity(Gravity.CENTER);
        quizFeedback.setPadding(dp(8), dp(12), dp(8), dp(12));
        layout.addView(quizFeedback);

        quizAnswered = false;
        for (int i = 0; i < 4; i++) {
            final int choice = i;
            Button option = new Button(this);
            option.setText(quizOptions[questionId][i]);
            option.setTextSize(16);
            option.setTextColor(Color.WHITE);
            option.setAllCaps(false);
            option.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25, 105, 87)));
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, -2);
            op.setMargins(0, dp(5), 0, dp(5));
            layout.addView(option, op);
            option.setOnClickListener(v -> {
                if (quizAnswered) return;
                quizAnswered = true;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View child = layout.getChildAt(j);
                    if (child instanceof Button) child.setEnabled(false);
                }
                if (choice == quizCorrect[questionId]) {
                    quizScore += 10;
                    option.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(35, 135, 83)));
                    quizFeedback.setText("إجابة صحيحة! +١٠ نقاط\n" + quizExplanations[questionId]);
                } else {
                    option.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(145, 65, 55)));
                    quizMistakes++;
                    int remaining = quizMaxMistakes - quizMistakes;
                    quizFeedback.setText("❌ إجابة خاطئة! المتبقي من الأخطاء: " + toArabicDigits(String.valueOf(Math.max(0, remaining)))
                        + "\nالإجابة الصحيحة: " + quizOptions[questionId][quizCorrect[questionId]]
                        + "\n" + quizExplanations[questionId]);
                }
                if (quizMistakes >= quizMaxMistakes) {
                    addButton("عرض نتيجة الخسارة", this::failQuiz);
                } else {
                    addButton(quizIndex == quizRoundIndices.size() - 1 ? "عرض النتيجة 🏆" : "السؤال التالي ←", () -> {
                        if (quizIndex == quizRoundIndices.size() - 1) finishQuiz();
                        else { quizIndex++; showQuizQuestion(); }
                    });
                }
            });
        }
        addButton("إنهاء المسابقة", this::showCompetitions);
    }

    void failQuiz() {
        int oldPoints = getSharedPreferences("noor_quiz", MODE_PRIVATE).getInt("points", 0);
        setup();
        layout.setPadding(dp(14), dp(16), dp(14), dp(22));
        addText("💔 خسرت المسابقة!", 27, Color.rgb(255, 180, 160));
        addText("نفدت محاولاتك الخاطئة الثلاث. عادت الجولة إلى المستوى الأول.", 17, Color.WHITE);
        addText("نقاط هذه الجولة: صفر • رصيدك السابق محفوظ: " + toArabicDigits(String.valueOf(oldPoints)), 16, Color.rgb(255, 224, 157));
        addButton("ابدأ من الصفر 🔄", this::startQuiz);
        addButton("العودة للمسابقات", this::showCompetitions);
    }

    void finishQuiz() {
        android.content.SharedPreferences prefs = getSharedPreferences("noor_quiz", MODE_PRIVATE);
        int totalPoints = prefs.getInt("points", 0) + quizScore;
        int played = prefs.getInt("played", 0) + 1;
        prefs.edit().putInt("points", totalPoints).putInt("played", played).apply();
        submitLeaderboardScore(totalPoints);
        setup();
        layout.setPadding(dp(14), dp(16), dp(14), dp(22));
        addText("🎉 أحسنت! انتهت المسابقة", 25, Color.rgb(242, 211, 145));
        addText("نتيجتك: " + toArabicDigits(String.valueOf(quizScore)) + " من ١٠٠ نقطة", 22, Color.WHITE);
        addText("رصيدك الكلي: " + toArabicDigits(String.valueOf(totalPoints)) + " نقطة", 20, Color.rgb(255, 224, 157));
        String level = totalPoints >= 300 ? "نابغة" : totalPoints >= 150 ? "متميز" : totalPoints >= 50 ? "مجتهد" : "مبتدئ";
        addAdultCard("🏅 مستواك: " + level, "واصل التعلم واجمع المزيد من النقاط", () -> showCompetitions());
        addButton("🏆 عرض لوحة المتصدرين", this::showLeaderboard);
        addButton("إعادة المسابقة", this::startQuiz);
        addButton("العودة للمسابقات", this::showCompetitions);
        addButton("العودة للرئيسية", this::home);
    }

    void ensureFirebaseLogin(Runnable onReady) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            onReady.run();
            return;
        }
        auth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                onReady.run();
            } else {
                message("تعذر تسجيل الدخول إلى لوحة المتصدرين. تحقق من الإنترنت وإعدادات Firebase.");
            }
        });
    }

    void submitLeaderboardScore(int localTotalPoints) {
        ensureFirebaseLogin(() -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference playerRef = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                .getReference("leaderboard").child(uid);
            playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snapshot) {
                    long oldScore = 0;
                    if (snapshot.child("score").getValue() != null) {
                        try { oldScore = snapshot.child("score").getValue(Long.class); }
                        catch (Exception ignored) {}
                    }
                    long bestScore = Math.max(oldScore, (long) localTotalPoints);
                    Map<String, Object> record = new HashMap<>();
                    record.put("score", bestScore);
                    record.put("displayName", "مشارك-" + uid.substring(0, Math.min(5, uid.length())));
                    record.put("updatedAt", System.currentTimeMillis());
                    playerRef.setValue(record).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            message("تعذر حفظ نقاطك في لوحة المتصدرين. حاول لاحقاً.");
                        }
                    });
                }
                @Override public void onCancelled(DatabaseError error) {
                    message("تعذر قراءة نقاطك الحالية: " + error.getMessage());
                }
            });
        });
    }

    void showLeaderboard() {
        setup();
        layout.setPadding(dp(14), dp(14), dp(14), dp(22));
        addText("🏆 لوحة متصدري نور القرآن", 25, Color.rgb(242, 211, 145));
        addText("أفضل ٢٠ مشاركاً حسب النقاط المسجلة", 14, Color.WHITE);
        TextView status = new TextView(this);
        status.setText("جارٍ تحميل لوحة المتصدرين...");
        status.setTextColor(Color.WHITE);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(8), dp(18), dp(8), dp(18));
        layout.addView(status);
        final LinearLayout targetLayout = layout;

        ensureFirebaseLogin(() -> {
            Query query = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)
                .getReference("leaderboard").orderByChild("score").limitToLast(20);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot snapshot) {
                    if (layout != targetLayout) return;
                    status.setText("");
                    ArrayList<DataSnapshot> players = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) players.add(child);
                    Collections.reverse(players);
                    if (players.isEmpty()) {
                        status.setText("لا توجد نتائج بعد. أكمل مسابقة لتظهر هنا.");
                    } else {
                        int rank = 1;
                        for (DataSnapshot player : players) {
                            String name = player.child("displayName").getValue(String.class);
                            Long score = player.child("score").getValue(Long.class);
                            if (name == null || name.trim().isEmpty()) name = "مشارك";
                            if (score == null) score = 0L;
                            addAdultCard("⭐ " + rank + " ـ " + name,
                                toArabicDigits(String.valueOf(score)) + " نقطة", () -> {});
                            rank++;
                        }
                    }
                    addButton("🔄 تحديث القائمة", MainActivity.this::showLeaderboard);
                    addButton("العودة للمسابقات", MainActivity.this::showCompetitions);
                }
                @Override public void onCancelled(DatabaseError error) {
                    if (layout != targetLayout) return;
                    status.setText("تعذر تحميل القائمة: " + error.getMessage());
                    addButton("إعادة المحاولة", MainActivity.this::showLeaderboard);
                    addButton("العودة للمسابقات", MainActivity.this::showCompetitions);
                }
            });
        });
    }

    void zakat() {
        setup();
        layout.setPadding(dp(14), dp(12), dp(14), dp(22));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(18), dp(20), dp(18), dp(20));
        hero.setBackground(premiumBackground(Color.rgb(13, 55, 47), Color.rgb(218, 184, 111), 24));
        TextView symbol = new TextView(this);
        symbol.setText("۞");
        symbol.setTextColor(Color.rgb(242, 211, 145));
        symbol.setTextSize(34);
        symbol.setGravity(Gravity.CENTER);
        hero.addView(symbol);
        TextView heading = new TextView(this);
        heading.setText("حاسبة الزكاة");
        heading.setTextColor(Color.rgb(255, 229, 170));
        heading.setTextSize(27);
        heading.setTypeface(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        hero.addView(heading);
        TextView subtitle = new TextView(this);
        subtitle.setText("طهّر مالك واجعل للخير نصيباً");
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(7), 0, 0);
        hero.addView(subtitle);
        layout.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        TextView currencyNote = new TextView(this);
        currencyNote.setText("جميع المبالغ بالدينار العراقي (د.ع)");
        currencyNote.setTextColor(Color.rgb(242, 211, 145));
        currencyNote.setTextSize(13);
        currencyNote.setGravity(Gravity.CENTER);
        currencyNote.setPadding(0, dp(14), 0, dp(8));
        layout.addView(currencyNote);

        android.content.SharedPreferences prefs = getSharedPreferences("noor_zakat", MODE_PRIVATE);
        EditText cash = zakatMoneyField("النقد في البيت والمحفظة", prefs.getString("cash", ""));
        EditText bank = zakatMoneyField("الأموال في الحسابات المصرفية", prefs.getString("bank", ""));
        EditText gold = zakatMoneyField("قيمة الذهب والفضة الزكوية", prefs.getString("gold", ""));
        EditText trade = zakatMoneyField("قيمة البضائع المعدّة للبيع", prefs.getString("trade", ""));
        EditText receivables = zakatMoneyField("الديون المرجو تحصيلها", prefs.getString("receivables", ""));
        EditText debts = zakatMoneyField("الديون والالتزامات واجبة السداد الآن", prefs.getString("debts", ""));
        EditText nisab = zakatMoneyField("قيمة النصاب الحالية بالدينار العراقي", prefs.getString("nisab", ""));

        addZakatSection("١. الأموال والأصول الزكوية");
        addZakatField("النقد المتوفر", cash);
        addZakatField("الأرصدة البنكية والادخار", bank);
        addZakatField("قيمة الذهب والفضة", gold);
        addZakatField("البضائع التجارية", trade);
        addZakatField("الديون التي يُرجى تحصيلها", receivables);

        addZakatSection("٢. الالتزامات والنصاب");
        addZakatField("الديون المستحقة حالياً", debts);
        addZakatField("النصاب بالدينار العراقي", nisab);

        TextView nisabHelp = new TextView(this);
        nisabHelp.setText("النصاب يُقدّر عادةً بقيمة ٨٥ غراماً من الذهب الخالص (عيار ٢٤). أدخل قيمته الحالية من مصدر موثوق. لا تضع سعر الغرام وحده.");
        nisabHelp.setTextColor(Color.LTGRAY);
        nisabHelp.setTextSize(12);
        nisabHelp.setPadding(dp(8), dp(4), dp(8), dp(10));
        layout.addView(nisabHelp);

        LinearLayout resultCard = new LinearLayout(this);
        resultCard.setOrientation(LinearLayout.VERTICAL);
        resultCard.setGravity(Gravity.CENTER);
        resultCard.setPadding(dp(16), dp(18), dp(16), dp(18));
        resultCard.setBackground(premiumBackground(Color.rgb(10, 47, 41), Color.rgb(218, 184, 111), 22));
        TextView resultTitle = new TextView(this);
        resultTitle.setText("نتيجة حساب الزكاة");
        resultTitle.setTextColor(Color.rgb(242, 211, 145));
        resultTitle.setTextSize(19);
        resultTitle.setGravity(Gravity.CENTER);
        resultCard.addView(resultTitle);
        TextView assetsView = new TextView(this);
        assetsView.setText("إجمالي الأموال: —");
        assetsView.setTextColor(Color.WHITE);
        assetsView.setTextSize(15);
        assetsView.setGravity(Gravity.CENTER);
        assetsView.setPadding(0, dp(10), 0, dp(4));
        resultCard.addView(assetsView);
        TextView netView = new TextView(this);
        netView.setText("صافي المال الزكوي: —");
        netView.setTextColor(Color.WHITE);
        netView.setTextSize(15);
        netView.setGravity(Gravity.CENTER);
        resultCard.addView(netView);
        TextView dueView = new TextView(this);
        dueView.setText("الزكاة الواجبة: —");
        dueView.setTextColor(Color.rgb(255, 224, 157));
        dueView.setTextSize(25);
        dueView.setTypeface(null, android.graphics.Typeface.BOLD);
        dueView.setGravity(Gravity.CENTER);
        dueView.setPadding(0, dp(12), 0, dp(6));
        resultCard.addView(dueView);
        TextView statusView = new TextView(this);
        statusView.setText("أدخل المبالغ وقيمة النصاب ثم اضغط احسب");
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(13);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(4), 0, 0);
        resultCard.addView(statusView);
        LinearLayout.LayoutParams resultParams = new LinearLayout.LayoutParams(-1, -2);
        resultParams.setMargins(0, dp(14), 0, dp(10));
        layout.addView(resultCard, resultParams);

        addButton("احسب الزكاة الآن", () -> {
            try {
                double a = readZakatMoney(cash) + readZakatMoney(bank) + readZakatMoney(gold)
                    + readZakatMoney(trade) + readZakatMoney(receivables);
                double d = readZakatMoney(debts);
                double n = readZakatMoney(nisab);
                if (n <= 0) {
                    message("أدخل قيمة النصاب الحالية بالدينار العراقي أولاً");
                    return;
                }
                double net = Math.max(0, a - d);
                double due = net >= n ? net * 0.025 : 0;
                java.text.NumberFormat fmt = java.text.NumberFormat.getIntegerInstance(Locale.US);
                assetsView.setText("إجمالي الأموال: " + fmt.format(a) + " د.ع");
                netView.setText("صافي المال بعد الالتزامات: " + fmt.format(net) + " د.ع");
                dueView.setText(due > 0 ? "الزكاة الواجبة: " + fmt.format(Math.round(due)) + " د.ع"
                    : "الزكاة الواجبة: ٠ د.ع");
                if (net < n) {
                    statusView.setText("المبلغ أقل من النصاب المُدخل؛ لا تجب زكاة المال بهذه الحسبة.");
                } else {
                    statusView.setText("بلغ المال النصاب. الحسبة على أساس ٢٫٥٪، مع افتراض اكتمال الحول.");
                }
                prefs.edit().putString("cash",cash.getText().toString()).putString("bank",bank.getText().toString())
                    .putString("gold",gold.getText().toString()).putString("trade",trade.getText().toString())
                    .putString("receivables",receivables.getText().toString()).putString("debts",debts.getText().toString())
                    .putString("nisab",nisab.getText().toString()).apply();
            } catch (Exception e) {
                message("تأكد من إدخال أرقام صحيحة في جميع الحقول");
            }
        });
        addButton("مسح جميع المبالغ", () -> new android.app.AlertDialog.Builder(this)
            .setTitle("مسح الحاسبة").setMessage("هل تريد مسح المبالغ المحفوظة؟")
            .setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss())
            .setPositiveButton("مسح", (dialog, which) -> {
                prefs.edit().clear().apply();
                zakat();
            }).show());
        addText("تنبيه: هذه حاسبة إرشادية لزكاة المال وليست فتوى. تختلف أحكام بعض الأموال والذهب والديون باختلاف الحالة والمذهب، ويُرجى سؤال جهة شرعية موثوقة.", 12, Color.LTGRAY);
        addButton("العودة للرئيسية", this::home);
    }

    EditText zakatMoneyField(String hint, String saved) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setHint(hint);
        field.setText(saved);
        field.setTextColor(Color.WHITE);
        field.setHintTextColor(Color.rgb(190, 199, 187));
        field.setTextSize(16);
        field.setPadding(dp(14), dp(11), dp(14), dp(11));
        field.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field.setBackground(premiumBackground(Color.rgb(22, 61, 54), Color.rgb(151, 128, 76), 13));
        return field;
    }

    void addZakatSection(String title) {
        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(Color.rgb(242, 211, 145));
        label.setTextSize(18);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setPadding(dp(4), dp(16), dp(4), dp(8));
        layout.addView(label);
    }

    void addZakatField(String label, EditText field) {
        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setPadding(dp(4), dp(5), dp(4), dp(5));
        layout.addView(title);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(7));
        layout.addView(field, params);
    }

    double readZakatMoney(EditText field) {
        String value = field.getText().toString().trim()
            .replace(",", "").replace("٬", "").replace("٫", ".");
        value = value.replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3')
            .replace('٤','4').replace('٥','5').replace('٦','6').replace('٧','7')
            .replace('٨','8').replace('٩','9');
        if (value.length() == 0) return 0;
        return Double.parseDouble(value);
    }

    void message(String value) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }

    void showNoorPlus() {
        setup();
        addText("⭐ نور بلس", 30, Color.rgb(255, 224, 157));
        addText("مساحة تعليمية مميزة للكبار والأطفال", 17, Color.WHITE);
        addHomeFeatureCard("📖", "للكبار", "تصحيح التلاوة ومخارج الحروف • أحكام التجويد • الحفظ والمراجعة • اختبارات المستوى",
            "استكشف أكاديمية الكبار", Color.rgb(53,65,53), Color.rgb(218,184,111), this::showAdultsAcademy);
        addHomeFeatureCard("🌈", "للأطفال", "تعلم الحروف والكتابة • القراءة والتلاوة • أنشطة وألعاب تعليمية متدرجة",
            "استكشف أكاديمية الأطفال", Color.rgb(49,65,88), Color.rgb(145,190,245), this::showKidsAcademy);
        addText("السعر المختار: ٣٠٬٠٠٠ دينار عراقي شهرياً", 19, Color.rgb(255, 224, 157));
        addText("مميزات نور بلس المقترحة: مسارات تعليمية منظمة، تمارين إضافية، متابعة التقدم، اختبارات مراجعة، ومحتوى خاص للأطفال والكبار.", 15, Color.WHITE);
        addText("تنبيه: الاشتراك والدفع التلقائي وفتح المحتوى بعد الشراء غير مفعّلة في هذه النسخة. يلزم ربط Google Play Billing والتحقق من الشراء قبل تحصيل أي مبلغ أو وعد المستخدم بفتح المزايا.", 13, Color.rgb(255, 190, 170));
        addButton("العودة إلى الواجهة الرئيسية", this::home);
    }

    void showDailyChallengeQuestion(int questionIndex, int score) {
        final String[] questions = {
            "كم عدد سور القرآن الكريم؟",
            "ما السورة التي تُسمّى قلب القرآن؟",
            "في أي شهر نزل القرآن الكريم؟",
            "ما أطول سورة في القرآن الكريم؟",
            "كم عدد أركان الإسلام؟",
            "ما السورة التي لا تبدأ بالبسملة؟",
            "ما اسم الليلة التي هي خير من ألف شهر؟",
            "كم عدد أجزاء القرآن الكريم؟",
            "ما أول سورة في المصحف؟",
            "ما السورة التي تعدل ثلث القرآن؟",
            "إلى أي جهة يتجه المسلم في الصلاة؟",
            "ما اسم أمّ الكتاب؟"
        };
        final String[][] options = {
            {"١١٤","١١٠","١٢٠"},
            {"يس","الرحمن","الملك"},
            {"رمضان","شعبان","محرم"},
            {"البقرة","آل عمران","النساء"},
            {"خمسة","أربعة","ستة"},
            {"التوبة","الأنفال","يونس"},
            {"ليلة القدر","ليلة الإسراء","ليلة الجمعة"},
            {"٣٠","٢٠","٤٠"},
            {"الفاتحة","البقرة","الإخلاص"},
            {"الإخلاص","الفلق","الكوثر"},
            {"الكعبة المشرفة","المسجد الأقصى","المسجد النبوي"},
            {"الفاتحة","البقرة","الناس"}
        };
        final int[] correct = {0,0,0,0,0,0,0,0,0,0,0,0};
        final String[] explanations = {
            "عدد سور القرآن الكريم ١١٤ سورة.",
            "تُعرف سورة يس بهذا الاسم المشهور.",
            "نزل القرآن الكريم في شهر رمضان.",
            "سورة البقرة هي أطول سور القرآن الكريم.",
            "أركان الإسلام خمسة.",
            "سورة التوبة لا تبدأ بالبسملة.",
            "ليلة القدر خير من ألف شهر.",
            "القرآن الكريم ثلاثون جزءاً.",
            "سورة الفاتحة هي أول سورة في ترتيب المصحف.",
            "ورد أن سورة الإخلاص تعدل ثلث القرآن.",
            "يتجه المسلمون إلى الكعبة المشرفة.",
            "الفاتحة تُسمّى أمّ الكتاب."
        };
        final int total = 5;
        if (questionIndex >= total) {
            android.content.SharedPreferences prefs = getSharedPreferences("noor_daily_challenge", MODE_PRIVATE);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            int previousBest = prefs.getString("best_date", "").equals(today) ? prefs.getInt("best_score", 0) : 0;
            int best = Math.max(previousBest, score);
            prefs.edit().putString("best_date", today).putInt("best_score", best).apply();
            setup();
            addText("🏆 اكتمل تحدي اليوم!", 29, Color.rgb(255, 224, 157));
            addText("نتيجتك: " + toArabicDigits(String.valueOf(score)) + " من ٥٠ نقطة", 22, Color.WHITE);
            addText(score >= 40 ? "🌟 ممتاز! معرفة رائعة" : score >= 20 ? "👏 أحسنت، واصل التعلم" : "🌱 بداية جميلة، جرّب غداً من جديد", 19, Color.rgb(255, 215, 120));
            addText("أفضل نتيجة مسجلة لهذا اليوم: " + toArabicDigits(String.valueOf(best)) + " نقطة", 15, Color.WHITE);
            addText("النقاط والشارات تشجيعية داخل التطبيق فقط، وليست أموالاً أو جوائز نقدية.", 13, Color.rgb(255, 190, 170));
            addButton("العودة للرئيسية", this::home);
            addButton("إعادة التحدي للتدريب دون احتساب نتيجة جديدة", () -> showDailyChallengeQuestion(0, 0));
            return;
        }
        int dayOffset = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % questions.length;
        int q = (dayOffset + questionIndex) % questions.length;
        setup();
        addText("🏆 تحدي اليوم", 29, Color.rgb(255, 224, 157));
        addText("السؤال " + toArabicDigits(String.valueOf(questionIndex + 1)) + " من " + toArabicDigits(String.valueOf(total)), 17, Color.WHITE);
        TextView progress = new TextView(this);
        progress.setText("⭐ نقاطك الحالية: " + toArabicDigits(String.valueOf(score)));
        progress.setTextColor(Color.rgb(255, 224, 157));
        progress.setTextSize(16);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(dp(8), dp(8), dp(8), dp(12));
        layout.addView(progress);
        addText(questions[q], 22, Color.WHITE);
        final boolean[] answered = {false};
        final TextView feedback = new TextView(this);
        feedback.setText("");
        feedback.setTextColor(Color.WHITE);
        feedback.setTextSize(15);
        feedback.setGravity(Gravity.CENTER);
        feedback.setPadding(dp(8), dp(12), dp(8), dp(12));
        for (int i = 0; i < options[q].length; i++) {
            final int choice = i;
            Button answer = new Button(this);
            answer.setText(options[q][i]);
            answer.setAllCaps(false);
            answer.setTextSize(17);
            answer.setTextColor(Color.WHITE);
            answer.setBackground(premiumBackground(Color.rgb(20, 75, 64), Color.rgb(218, 184, 111), 16));
            LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(-1, -2);
            answerParams.setMargins(0, dp(5), 0, dp(5));
            layout.addView(answer, answerParams);
            answer.setOnClickListener(v -> {
                if (answered[0]) return;
                answered[0] = true;
                boolean right = choice == correct[q];
                int newScore = score + (right ? 10 : 0);
                answer.setBackground(premiumBackground(right ? Color.rgb(31, 112, 70) : Color.rgb(130, 53, 53),
                    right ? Color.rgb(145, 230, 170) : Color.rgb(255, 160, 150), 16));
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View child = layout.getChildAt(j);
                    if (child instanceof Button) child.setEnabled(false);
                }
                feedback.setText((right ? "✓ إجابة صحيحة! +١٠ نقاط\n" : "✕ ليست الإجابة الصحيحة\n")
                    + explanations[q]);
                feedback.setTextColor(right ? Color.rgb(160, 245, 180) : Color.rgb(255, 205, 180));
                layout.addView(feedback);
                addButton(questionIndex == total - 1 ? "عرض النتيجة" : "السؤال التالي ←",
                    () -> showDailyChallengeQuestion(questionIndex + 1, newScore));
            });
        }
        addText("كل إجابة صحيحة = ١٠ نقاط • مجموع التحدي ٥٠ نقطة", 13, Color.rgb(255, 224, 157));
        addButton("إلغاء والعودة للرئيسية", this::home);
    }

    void showBubbleShooter() {
        setup();
        addText("🫧 فقاعات الألوان", 28, Color.rgb(255, 215, 120));
        addText("وجّه الكرة نحو اللون نفسه واجمع ثلاث فقاعات أو أكثر لتفجيرها", 14, Color.WHITE);
        BubbleShooterView game = new BubbleShooterView();
        layout.addView(game, new LinearLayout.LayoutParams(-1, dp(610)));
        addButton("إعادة اللعبة", this::showBubbleShooter);
        addButton("العودة إلى عالم الأطفال", this::showKidsSection);
    }

    class BubbleShooterView extends View {
        final int[] palette={Color.rgb(0,76,96),Color.rgb(142,10,43),Color.rgb(76,18,132),Color.rgb(12,45,132),Color.rgb(5,88,48),Color.rgb(138,72,5)};
        final int rows=10,cols=10;
        final int[][] board=new int[rows][cols];
        final android.graphics.Paint p=new android.graphics.Paint(3);
        final java.util.Random random=new java.util.Random();
        final android.os.Handler gameHandler=new android.os.Handler();
        final java.util.ArrayList<Particle> particles=new java.util.ArrayList<>();
        final java.util.ArrayList<SpecialFx> specialFx=new java.util.ArrayList<>();
        final java.util.ArrayList<float[]> shotPath=new java.util.ArrayList<>();
        int current,next,score=0,shots=44;
        int aimHitRow=-1,aimHitCol=-1;
        boolean finished=false,flying=false,swappingTouch=false;
        float radius,cellX,cellY,top,shooterY,aimX,aimY,flyX,flyY,flyProgress,screenPulse=0;
        int flyingColor;
        long lastFrame;
        final class Particle {
            float x,y,vx,vy,size,life; int color;
            Particle(float x,float y,int color,float power) {
                this.x=x;this.y=y;this.color=color;
                double a=random.nextDouble()*Math.PI*2;
                float speed=dp(1.5f+random.nextFloat()*3.8f)*power;
                vx=(float)Math.cos(a)*speed;vy=(float)Math.sin(a)*speed;
                size=dp(1.6f+random.nextFloat()*3.2f);life=1f;
            }
        }
        final class SpecialFx {
            float x,y,life=1f,radius=dp(8); int type;
            SpecialFx(float x,float y,int type){this.x=x;this.y=y;this.type=type;}
        }
        final Runnable frameTask=new Runnable(){@Override public void run(){
            long now=android.os.SystemClock.uptimeMillis();
            float dt=lastFrame==0?1f:Math.min(2f,(now-lastFrame)/16.67f);lastFrame=now;
            boolean active=false;
            if(flying){
                if(((int)(flyProgress*100))%2==0)particles.add(new Particle(flyX,flyY,palette[flyingColor],.30f));
                flyProgress=Math.min(1f,flyProgress+dt*.26f);
                float distance=pathLength(shotPath)*flyProgress;
                float[] point=pointAtDistance(shotPath,distance);
                flyX=point[0];flyY=point[1];active=true;
                if(flyProgress>=1f){flying=false;finishShot();}
            }
            for(int i=particles.size()-1;i>=0;i--){
                Particle a=particles.get(i);a.x+=a.vx*dt;a.y+=a.vy*dt;a.vy+=dp(.055f)*dt;
                a.vx*=Math.pow(.985,dt);a.life-=.035f*dt;
                if(a.life<=0)particles.remove(i);else active=true;
            }
            for(int i=specialFx.size()-1;i>=0;i--){
                SpecialFx fx=specialFx.get(i);
                if(fx.type==1)fx.y-=dp(8.5f)*dt;else fx.radius+=dp(5.5f)*dt;
                fx.life-=.035f*dt;
                if(fx.life<=0)specialFx.remove(i);else active=true;
            }
            if(screenPulse>0){screenPulse=Math.max(0,screenPulse-.065f*dt);active=true;}
            invalidate();if(active)gameHandler.postDelayed(this,16);else lastFrame=0;
        }};
        BubbleShooterView(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_HARDWARE,null);
            for(int r=0;r<rows;r++)for(int c=0;c<cols;c++)board[r][c]=-1;
            for(int r=0;r<6;r++)for(int c=0;c<cols;c++){if(r>4&&random.nextFloat()<.10f)continue;board[r][c]=random.nextInt(palette.length);}
            current=random.nextInt(palette.length);next=random.nextInt(palette.length);setFocusable(true);
        }
        float dp(float v){return v*getResources().getDisplayMetrics().density;}
        float startX(){return getWidth()/2f-((cols-1)*cellX+radius)/2f;}
        float bubbleX(int r,int c){return startX()+c*cellX+(r%2==1?radius:0);}
        float bubbleY(int r){return top+r*cellY;}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
            radius=Math.min(w/(cols*2.42f),dp(14));cellX=radius*2.04f;cellY=radius*1.80f;
            top=dp(76);shooterY=h-dp(116);aimX=w/2f;aimY=top+dp(100);
        }
        @Override protected void onDraw(android.graphics.Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight();
            p.setStyle(android.graphics.Paint.Style.FILL);p.setShader(null);p.setColor(Color.BLACK);
            p.setShader(new android.graphics.LinearGradient(0,0,w,h,new int[]{0xFF071A3D,0xFF123C78,0xFF245D9B,0xFF102B59},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p);p.setShader(null);
            // soft ambient glows and tiny stars
            p.setColor(0x183ACDFF);c.drawCircle(w*.18f,h*.34f,w*.42f,p);
            p.setColor(0x182D6BFF);c.drawCircle(w*.88f,h*.57f,w*.36f,p);
            for(int i=0;i<34;i++){float sx=(i*83+29)%Math.max(1,(int)w),sy=(i*137+61)%(Math.max(1,(int)(h*.72f)));p.setColor(i%3==0?0x66FFFFFF:0x229DEBFF);c.drawCircle(sx,sy,dp(i%4==0?1.7f:1f),p);}
            // header
            p.setShader(new android.graphics.LinearGradient(0,dp(8),0,dp(66),new int[]{0xEE193D70,0xDD10284F},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(dp(14),dp(10),w-dp(14),dp(66),dp(19),dp(19),p);p.setShader(null);
            p.setColor(0x557FDFFF);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(1));c.drawRoundRect(dp(14),dp(10),w-dp(14),dp(66),dp(19),dp(19),p);p.setStyle(android.graphics.Paint.Style.FILL);
            p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextSize(dp(10));p.setColor(0xFF9DDCFF);c.drawText("النقاط",dp(29),dp(29),p);
            p.setTextSize(dp(23));p.setColor(Color.WHITE);c.drawText(String.valueOf(score),dp(28),dp(53),p);
            p.setTextSize(dp(13));p.setColor(0xFFFFD978);c.drawText("●",w-dp(112),dp(34),p);p.setColor(Color.WHITE);p.setTextSize(dp(14));c.drawText("الرميات  "+shots,w-dp(96),dp(38),p);
            // framed play field
            p.setShader(new android.graphics.LinearGradient(0,dp(77),0,h-dp(112),new int[]{0x443B8ED0,0x22203E76},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(dp(9),dp(76),w-dp(9),h-dp(105),dp(24),dp(24),p);p.setShader(null);
            p.setColor(0x337FCBFF);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(1));c.drawRoundRect(dp(9),dp(76),w-dp(9),h-dp(105),dp(24),dp(24),p);p.setStyle(android.graphics.Paint.Style.FILL);
            for(int r=0;r<rows;r++)for(int col=0;col<cols;col++)if(board[r][col]>=0)drawBubble(c,bubbleX(r,col),bubbleY(r),radius,palette[board[r][col]]);
            if(!flying&&!finished){
                updateAimPath();
                // A luminous dotted trajectory, with a soft glow around every guide point.
                for(int i=2;i<shotPath.size();i+=4){
                    float[] dot=shotPath.get(i);
                    float fade=1f-(float)i/Math.max(1,shotPath.size());
                    p.setColor(((int)(105*fade+35)<<24)|0x00B9F5FF);
                    c.drawCircle(dot[0],dot[1],dp(5.2f),p);
                    p.setColor(((int)(210*fade+35)<<24)|0x00FFFFFF);
                    c.drawCircle(dot[0],dot[1],dp(1.7f),p);
                }
                if(!shotPath.isEmpty()){
                    if(aimHitRow>=0&&aimHitCol>=0){
                        float tx=bubbleX(aimHitRow,aimHitCol),ty=bubbleY(aimHitRow);
                        p.setColor(0x554DFFF0);c.drawCircle(tx,ty,radius*1.48f,p);
                        p.setColor(0xFFB9FFF4);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(2));c.drawCircle(tx,ty,radius*1.12f,p);p.setStyle(android.graphics.Paint.Style.FILL);
                    }
                    float[] end=shotPath.get(shotPath.size()-1);
                    p.setColor(0x443CEBFF);c.drawCircle(end[0],end[1],radius*1.28f,p);
                    p.setColor(0xFFFFE18A);p.setStyle(android.graphics.Paint.Style.STROKE);
                    p.setStrokeWidth(dp(2.2f));c.drawCircle(end[0],end[1],radius*.72f,p);
                    p.setStrokeWidth(dp(1.1f));c.drawCircle(end[0],end[1],radius*.43f,p);
                    p.setStyle(android.graphics.Paint.Style.FILL);
                    p.setColor(0xFFFFF4C4);c.drawCircle(end[0],end[1],dp(2.3f),p);
                }
            }
            if(flying){
                p.setColor(0x5539E8FF);c.drawCircle(flyX,flyY,radius*1.55f,p);
                p.setColor(0x99FFFFFF);c.drawCircle(flyX,flyY,radius*1.22f,p);
                drawBubble(c,flyX,flyY,radius*1.1f,palette[flyingColor]);
            }
            // Ornate jewel launcher: layered gold trim, engraved rails and easy-to-see firing socket.
            p.setColor(0x5547DFFF);c.drawOval(w/2f-dp(68),shooterY+dp(24),w/2f+dp(68),shooterY+dp(62),p);
            p.setShader(new android.graphics.LinearGradient(w/2f-dp(62),shooterY+dp(16),w/2f+dp(62),shooterY+dp(56),new int[]{0xFFFFE7A0,0xFF9B5B18,0xFF321B42},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(w/2f-dp(62),shooterY+dp(17),w/2f+dp(62),shooterY+dp(52),dp(17),dp(17),p);p.setShader(null);
            p.setColor(0xFFFFD878);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(2.2f));
            c.drawRoundRect(w/2f-dp(55),shooterY+dp(21),w/2f+dp(55),shooterY+dp(48),dp(13),dp(13),p);
            p.setColor(0xFF542B59);p.setStrokeWidth(dp(1));c.drawRoundRect(w/2f-dp(47),shooterY+dp(25),w/2f+dp(47),shooterY+dp(44),dp(9),dp(9),p);
            p.setStyle(android.graphics.Paint.Style.FILL);
            for(int side=-1;side<=1;side+=2){
                float gx=w/2f+side*dp(47);
                p.setColor(0xFFFFD978);c.drawCircle(gx,shooterY+dp(34),dp(5),p);
                p.setColor(0xFF56F1F4);c.drawCircle(gx,shooterY+dp(34),dp(2.7f),p);
                p.setColor(0xFFFFF0B0);c.drawCircle(gx-dp(1),shooterY+dp(32),dp(1),p);
            }
            p.setShader(new android.graphics.LinearGradient(w/2f-dp(29),shooterY,w/2f+dp(29),shooterY+dp(39),new int[]{0xFFFFE9A8,0xFF9C5C9C,0xFF321D57},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(w/2f-dp(29),shooterY+dp(5),w/2f+dp(29),shooterY+dp(39),dp(14),dp(14),p);p.setShader(null);
            p.setColor(0xFFFFD878);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(2));
            c.drawRoundRect(w/2f-dp(24),shooterY+dp(10),w/2f+dp(24),shooterY+dp(34),dp(10),dp(10),p);p.setStyle(android.graphics.Paint.Style.FILL);
            p.setShader(new android.graphics.LinearGradient(w/2f-dp(16),shooterY-dp(31),w/2f+dp(16),shooterY+dp(24),new int[]{0xFFFFF2C4,0xFFB477B9,0xFF35205D},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(w/2f-dp(17),shooterY-dp(27),w/2f+dp(17),shooterY+dp(23),dp(15),dp(15),p);p.setShader(null);
            p.setColor(0xFFFFE5A0);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(2.8f));c.drawCircle(w/2f,shooterY-dp(21),dp(15),p);
            p.setColor(0xFF69F5F2);p.setStrokeWidth(dp(1.3f));c.drawCircle(w/2f,shooterY-dp(21),dp(19),p);p.setStyle(android.graphics.Paint.Style.FILL);
            if(!flying)drawBubble(c,w/2f,shooterY-dp(21),radius*.77f,palette[current]);
            // Decorated swap pedestal: gold frame and bright jewel; tapping here only swaps bubbles.
            p.setShader(new android.graphics.LinearGradient(w-dp(100),shooterY-dp(10),w-dp(20),shooterY+dp(66),new int[]{0xFFF7D98B,0xFF89502E,0xFF35204F},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(w-dp(101),shooterY-dp(10),w-dp(20),shooterY+dp(64),dp(19),dp(19),p);p.setShader(null);
            p.setColor(0xFFFFE6A2);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(1.8f));
            c.drawRoundRect(w-dp(96),shooterY-dp(5),w-dp(25),shooterY+dp(59),dp(15),dp(15),p);
            p.setColor(0xFF261B48);p.setStyle(android.graphics.Paint.Style.FILL);
            c.drawRoundRect(w-dp(90),shooterY+dp(1),w-dp(31),shooterY+dp(43),dp(12),dp(12),p);
            p.setColor(0xFFFFD978);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(1.2f));
            c.drawCircle(w-dp(60),shooterY+dp(20),dp(22),p);p.setStyle(android.graphics.Paint.Style.FILL);
            drawBubble(c,w-dp(60),shooterY+dp(19),radius*.56f,palette[next]);
            p.setColor(0xFFFFE7A8);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(1));
            c.drawCircle(w-dp(60),shooterY+dp(19),radius*.69f,p);p.setStyle(android.graphics.Paint.Style.FILL);
            p.setColor(0xFFFFD878);c.drawCircle(w-dp(60),shooterY-dp(1),dp(2.2f),p);
            p.setTextSize(dp(10));p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setColor(0xFFFFE9B5);p.setTextAlign(android.graphics.Paint.Align.CENTER);c.drawText("تبديل",w-dp(60),shooterY+dp(55),p);p.setTextAlign(android.graphics.Paint.Align.LEFT);
            // particles and impact glow
            for(Particle a:particles){int alpha=Math.max(0,Math.min(255,(int)(255*a.life)));p.setColor((alpha<<24)|(a.color&0x00FFFFFF));c.drawCircle(a.x,a.y,a.size*a.life,p);}
            // Animated bomb shockwaves and rockets after every successful match.
            for(SpecialFx fx:specialFx){
                int alpha=Math.max(0,Math.min(255,(int)(220*fx.life)));
                if(fx.type==0){
                    p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(dp(3)*fx.life);
                    p.setColor((alpha<<24)|0x00FFB52E);c.drawCircle(fx.x,fx.y,fx.radius,p);
                    p.setStrokeWidth(dp(1.8f));p.setColor((alpha<<24)|0x00FFF1A6);c.drawCircle(fx.x,fx.y,fx.radius*.72f,p);
                    p.setStrokeWidth(dp(1.1f));p.setColor((alpha/2<<24)|0x00FF6A24);c.drawCircle(fx.x,fx.y,fx.radius*1.28f,p);
                    p.setStyle(android.graphics.Paint.Style.FILL);p.setColor((alpha<<24)|0x00FF6A24);c.drawCircle(fx.x,fx.y,dp(7)*fx.life,p);
                    p.setColor((alpha<<24)|0x00FFE08A);c.drawCircle(fx.x,fx.y,dp(3)*fx.life,p);
                    p.setColor((alpha<<24)|0x00FFFFFF);c.drawCircle(fx.x-dp(2),fx.y-dp(2),dp(1.7f)*fx.life,p);
                }else{
                    p.setColor((alpha<<24)|0x00FF4B35);c.drawCircle(fx.x,fx.y+dp(12),dp(5)*fx.life,p);
                    p.setColor((alpha<<24)|0x00FFD45A);c.drawCircle(fx.x,fx.y+dp(7),dp(3.5f)*fx.life,p);
                    p.setShader(new android.graphics.LinearGradient(fx.x-dp(5),fx.y-dp(11),fx.x+dp(5),fx.y+dp(11),new int[]{0xFFFFFFFF,0xFFFFD45A,0xFFE64B35},null,android.graphics.Shader.TileMode.CLAMP));
                    c.drawRoundRect(fx.x-dp(4),fx.y-dp(10),fx.x+dp(4),fx.y+dp(10),dp(4),dp(4),p);p.setShader(null);
                    p.setColor((alpha<<24)|0x00FFFFFF);
                    android.graphics.Path nose=new android.graphics.Path();nose.moveTo(fx.x,fx.y-dp(16));nose.lineTo(fx.x-dp(5),fx.y-dp(7));nose.lineTo(fx.x+dp(5),fx.y-dp(7));nose.close();c.drawPath(nose,p);
                    p.setColor((alpha<<24)|0x00FF6A35);
                    android.graphics.Path fin=new android.graphics.Path();fin.moveTo(fx.x-dp(4),fx.y+dp(5));fin.lineTo(fx.x-dp(9),fx.y+dp(12));fin.lineTo(fx.x-dp(3),fx.y+dp(10));fin.close();c.drawPath(fin,p);
                    android.graphics.Path fin2=new android.graphics.Path();fin2.moveTo(fx.x+dp(4),fx.y+dp(5));fin2.lineTo(fx.x+dp(9),fx.y+dp(12));fin2.lineTo(fx.x+dp(3),fx.y+dp(10));fin2.close();c.drawPath(fin2,p);
                }
            }
            if(screenPulse>0){p.setColor(((int)(screenPulse*55)<<24)|0x00A8F2FF);c.drawRect(0,0,w,h,p);}
            p.setShader(new android.graphics.LinearGradient(0,h-dp(62),0,h-dp(9),new int[]{0xEE173C70,0xEE0B2048},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(dp(14),h-dp(62),w-dp(14),h-dp(9),dp(17),dp(17),p);p.setShader(null);
            p.setColor(0xFFBDEBFF);p.setTextSize(dp(12));p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextAlign(android.graphics.Paint.Align.CENTER);c.drawText("اسحب لتحديد الاتجاه ثم اترك لإطلاق الكرة",w/2f,h-dp(30),p);p.setTextAlign(android.graphics.Paint.Align.LEFT);
            if(finished){p.setColor(0xDD06152F);c.drawRoundRect(dp(24),h*.38f,w-dp(24),h*.60f,dp(22),dp(22),p);p.setTextAlign(android.graphics.Paint.Align.CENTER);p.setColor(0xFFFFE18A);p.setTextSize(dp(25));c.drawText("انتهت الجولة",w/2f,h*.46f,p);p.setColor(Color.WHITE);p.setTextSize(dp(18));c.drawText("نتيجتك: "+score,w/2f,h*.52f,p);p.setTextAlign(android.graphics.Paint.Align.LEFT);}
        }
        void drawBubble(android.graphics.Canvas c,float x,float y,float r,int color){
            p.setStyle(android.graphics.Paint.Style.FILL);p.setShader(null);
            p.setColor(0x6603132B);c.drawCircle(x+r*.13f,y+r*.24f,r*1.10f,p);
            // Layered neon rim, glossy body and tiny reflected highlights.
            p.setColor(0x223BEAFF);c.drawCircle(x,y,r*1.28f,p);
            p.setColor((0x45<<24)|(color&0x00FFFFFF));c.drawCircle(x,y,r*1.15f,p);
            p.setShader(new android.graphics.RadialGradient(x-r*.34f,y-r*.39f,r*1.58f,new int[]{0xFFB8D5E8,lighten(color),color,darken(color)},new float[]{0f,.16f,.58f,1f},android.graphics.Shader.TileMode.CLAMP));
            c.drawCircle(x,y,r,p);p.setShader(null);
            p.setShader(new android.graphics.LinearGradient(x-r*.35f,y-r*.05f,x+r*.28f,y+r*.88f,new int[]{0x00FFFFFF,0x22FFFFFF,0x66000000},null,android.graphics.Shader.TileMode.CLAMP));
            c.drawOval(x-r*.78f,y-r*.05f,x+r*.78f,y+r*.88f,p);p.setShader(null);
            p.setColor(0x86B9D8E8);c.drawOval(x-r*.59f,y-r*.72f,x-r*.08f,y-r*.34f,p);
            p.setColor(0x44D7E8F4);c.drawOval(x-r*.39f,y-r*.52f,x-r*.20f,y-r*.43f,p);
            p.setColor(0x99FFFFFF);c.drawCircle(x+r*.52f,y-r*.42f,r*.075f,p);
            p.setColor(0x66FFFFFF);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(Math.max(dp(1),r*.075f));c.drawCircle(x,y,r*.95f,p);p.setStyle(android.graphics.Paint.Style.FILL);
        }
        int lighten(int color){return Color.rgb(Math.min(255,Color.red(color)+30),Math.min(255,Color.green(color)+30),Math.min(255,Color.blue(color)+30));}
        int darken(int color){return Color.rgb((int)(Color.red(color)*.55f),(int)(Color.green(color)*.55f),(int)(Color.blue(color)*.62f));}
        int[] neighbors(int r,int col,int k){
            switch(k){
                case 0:return new int[]{r,col-1};case 1:return new int[]{r,col+1};
                case 2:return new int[]{r-1,col+(r%2==1?0:-1)};
                case 3:return new int[]{r-1,col+(r%2==1?1:0)};
                case 4:return new int[]{r+1,col+(r%2==1?0:-1)};
                default:return new int[]{r+1,col+(r%2==1?1:0)};
            }
        }
        boolean valid(int r,int col){return r>=0&&r<rows&&col>=0&&col<cols;}
        void updateAimPath(){
            shotPath.clear();aimHitRow=-1;aimHitCol=-1;float sx=getWidth()/2f,sy=shooterY;
            float dx=aimX-sx,dy=aimY-sy;
            if(dy> -dp(24))dy=-dp(24);
            float len=(float)Math.hypot(dx,dy);if(len<1)len=1;dx/=len;dy/=len;
            float x=sx,y=sy,step=Math.max(dp(5),radius*.52f);shotPath.add(new float[]{x,y});
            for(int n=0;n<560;n++){
                x+=dx*step;y+=dy*step;
                if(x<radius){x=radius+(radius-x);dx=Math.abs(dx);}
                if(x>getWidth()-radius){x=(getWidth()-radius)-(x-(getWidth()-radius));dx=-Math.abs(dx);}
                if(y<=top+radius*.15f){y=top+radius*.15f;shotPath.add(new float[]{x,y});break;}
                boolean hit=false;
                float hitDistance=(radius*1.92f)*(radius*1.92f);
                for(int r=0;r<rows&&!hit;r++)for(int col=0;col<cols;col++)if(board[r][col]>=0){
                    float bx=bubbleX(r,col),by=bubbleY(r);
                    float d=(x-bx)*(x-bx)+(y-by)*(y-by);
                    if(d<hitDistance){hit=true;hitDistance=d;aimHitRow=r;aimHitCol=col;}
                }
                if(hit){shotPath.add(new float[]{x,y});break;}
                if(n%3==0)shotPath.add(new float[]{x,y});
            }
            if(shotPath.size()<2)shotPath.add(new float[]{sx,top});
        }
        float pathLength(java.util.ArrayList<float[]> path){float total=0;for(int i=1;i<path.size();i++)total+=dist(path.get(i-1),path.get(i));return total;}
        float dist(float[] a,float[] b){return (float)Math.hypot(b[0]-a[0],b[1]-a[1]);}
        float[] pointAtDistance(java.util.ArrayList<float[]> path,float distance){
            for(int i=1;i<path.size();i++){float[] a=path.get(i-1),b=path.get(i);float d=dist(a,b);if(distance<=d||i==path.size()-1){float t=d==0?1:Math.min(1,distance/d);return new float[]{a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t};}distance-=d;}
            return path.get(path.size()-1);
        }
        void shoot(){
            if(finished||flying)return;updateAimPath();if(shotPath.size()<2)return;
            performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
            flying=true;flyingColor=current;flyProgress=0;float[] first=shotPath.get(0);flyX=first[0];flyY=first[1];
            lastFrame=0;gameHandler.removeCallbacks(frameTask);gameHandler.post(frameTask);
        }
        void finishShot(){
            float[] impact=shotPath.get(shotPath.size()-1);int bestR=-1,bestC=-1;float best=Float.MAX_VALUE;
            // Attach only beside the exact bubble struck by the aiming ray.
            if(aimHitRow>=0&&aimHitCol>=0&&board[aimHitRow][aimHitCol]>=0){
                int bestGroup=0;
                // Evaluate every free cell beside the struck bubble. Prefer the cell
                // that creates the largest connected group of the fired color.
                for(int k=0;k<6;k++){
                    int[] nb=neighbors(aimHitRow,aimHitCol,k);int rr=nb[0],cc=nb[1];
                    if(!valid(rr,cc)||board[rr][cc]>=0)continue;
                    board[rr][cc]=flyingColor;
                    boolean[][] seenTry=new boolean[rows][cols];
                    java.util.ArrayDeque<int[]> tryQueue=new java.util.ArrayDeque<>();
                    tryQueue.add(new int[]{rr,cc});seenTry[rr][cc]=true;int groupSize=0;
                    while(!tryQueue.isEmpty()){
                        int[] a=tryQueue.remove();groupSize++;
                        for(int t=0;t<6;t++){
                            int[] near=neighbors(a[0],a[1],t);int ar=near[0],ac=near[1];
                            if(valid(ar,ac)&&!seenTry[ar][ac]&&board[ar][ac]==flyingColor){
                                seenTry[ar][ac]=true;tryQueue.add(new int[]{ar,ac});
                            }
                        }
                    }
                    board[rr][cc]=-1;
                    float px=bubbleX(rr,cc),py=bubbleY(rr);
                    float d=(px-impact[0])*(px-impact[0])+(py-impact[1])*(py-impact[1]);
                    if(groupSize>bestGroup||(groupSize==bestGroup&&d<best)){
                        bestGroup=groupSize;best=d;bestR=rr;bestC=cc;
                    }
                }
            }else{
                // If the shot reaches the ceiling without touching a bubble, use only the top row.
                for(int col=0;col<cols;col++)if(board[0][col]<0){
                    float px=bubbleX(0,col),py=bubbleY(0);
                    float d=(px-impact[0])*(px-impact[0])+(py-impact[1])*(py-impact[1]);
                    if(d<best){best=d;bestR=0;bestC=col;}
                }
            }
            if(bestR<0){finished=true;invalidate();return;}
            performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
            board[bestR][bestC]=flyingColor;
            java.util.ArrayList<int[]> group=new java.util.ArrayList<>();boolean[][] seen=new boolean[rows][cols];
            java.util.ArrayDeque<int[]> q=new java.util.ArrayDeque<>();q.add(new int[]{bestR,bestC});seen[bestR][bestC]=true;
            while(!q.isEmpty()){
                int[] a=q.remove();group.add(a);
                for(int k=0;k<6;k++){int[] nb=neighbors(a[0],a[1],k);int rr=nb[0],cc=nb[1];
                    if(valid(rr,cc)&&!seen[rr][cc]&&board[rr][cc]==flyingColor){seen[rr][cc]=true;q.add(nb);}
                }
            }
            if(group.size()>=3){
                float burstX=0,burstY=0;
                for(int[] a:group){burstX+=bubbleX(a[0],a[1]);burstY+=bubbleY(a[0]);}
                burstX/=group.size();burstY/=group.size();
                specialFx.add(new SpecialFx(burstX,burstY,0));
                specialFx.add(new SpecialFx(burstX,shooterY-dp(28),1));
                specialFx.add(new SpecialFx(burstX+dp(10),burstY-dp(7),0));
                for(int k=0;k<28;k++)particles.add(new Particle(burstX,burstY,k%4==0?0xFFFFD34F:(k%4==1?0xFFFF542E:(k%4==2?0xFF39E8FF:0xFFFFFFFF)),1.8f));
                for(int[] a:group){float x=bubbleX(a[0],a[1]),y=bubbleY(a[0]);
                    for(int k=0;k<10;k++)particles.add(new Particle(x,y,palette[flyingColor],1.20f));
                    board[a[0]][a[1]]=-1;
                }
                score+=group.size()*10;screenPulse=1f;
                // Drop bubbles no longer connected to the ceiling.
                boolean[][] connected=new boolean[rows][cols];java.util.ArrayDeque<int[]> topQueue=new java.util.ArrayDeque<>();
                for(int col=0;col<cols;col++)if(board[0][col]>=0){connected[0][col]=true;topQueue.add(new int[]{0,col});}
                while(!topQueue.isEmpty()){int[] a=topQueue.remove();for(int k=0;k<6;k++){int[] nb=neighbors(a[0],a[1],k);int rr=nb[0],cc=nb[1];if(valid(rr,cc)&&!connected[rr][cc]&&board[rr][cc]>=0){connected[rr][cc]=true;topQueue.add(nb);}}}
                for(int r=0;r<rows;r++)for(int col=0;col<cols;col++)if(board[r][col]>=0&&!connected[r][col]){
                    float x=bubbleX(r,col),y=bubbleY(r);for(int k=0;k<9;k++)particles.add(new Particle(x,y,palette[board[r][col]],1.65f));board[r][col]=-1;score+=20;
                }
            }else{
                float x=bubbleX(bestR,bestC),y=bubbleY(bestR);for(int k=0;k<14;k++)particles.add(new Particle(x,y,palette[flyingColor],.85f));screenPulse=.22f;
            }
            shots--;current=next;next=random.nextInt(palette.length);
            if(shots<=0)finished=true;
            boolean any=false;for(int r=0;r<rows;r++)for(int col=0;col<cols;col++)if(board[r][col]>=0)any=true;
            if(!any)finished=true;
            invalidate();
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            int action=e.getAction();
            if(action==android.view.MotionEvent.ACTION_DOWN){
                // The reserve-bubble panel is a dedicated swap control, never a firing zone.
                boolean reserveTap=e.getX()>=getWidth()-dp(108)
                    &&e.getY()>=shooterY-dp(16)&&e.getY()<=shooterY+dp(70);
                if(reserveTap&&!finished&&!flying){
                    swappingTouch=true;
                    int oldCurrent=current;current=next;next=oldCurrent;
                    performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                    invalidate();
                    return true;
                }
                swappingTouch=false;
                aimX=Math.max(dp(4),Math.min(getWidth()-dp(4),e.getX()));
                aimY=Math.min(shooterY-dp(24),e.getY());
                if(!flying&&!finished){updateAimPath();invalidate();}
                return true;
            }
            if(action==android.view.MotionEvent.ACTION_MOVE){
                if(swappingTouch)return true;
                aimX=Math.max(dp(4),Math.min(getWidth()-dp(4),e.getX()));
                aimY=Math.min(shooterY-dp(24),e.getY());
                if(!flying&&!finished){updateAimPath();invalidate();}
                return true;
            }
            if(action==android.view.MotionEvent.ACTION_UP){
                if(swappingTouch){swappingTouch=false;return true;}
                if(!finished&&!flying){
                    aimX=Math.max(dp(4),Math.min(getWidth()-dp(4),e.getX()));
                    aimY=Math.min(shooterY-dp(24),e.getY());
                    updateAimPath();shoot();
                }
                return true;
            }
            if(action==android.view.MotionEvent.ACTION_CANCEL){
                swappingTouch=false;
                return true;
            }
            return true;
        }
        @Override protected void onDetachedFromWindow(){gameHandler.removeCallbacks(frameTask);super.onDetachedFromWindow();}
    }

}
