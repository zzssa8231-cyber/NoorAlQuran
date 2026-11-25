
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
        loadQuran();
        home();
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
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(20, 25, 20, 25);
        layout.setBackgroundColor(Color.rgb(12, 45, 42));
        scroll.addView(layout);
        setContentView(scroll);
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

    void home() {
        setup();
        addText("نور القرآن", 34, Color.WHITE);
        addText("نورٌ يضيء حياتك", 18, Color.YELLOW);

        addButton("📖 القرآن الكريم", this::showSurahs);
        addButton("🎧 الاستماع إلى القرآن", this::audioScreen);
        addButton("👨‍👩‍👧 قسم الكبار", this::showAdultSection);
        addButton("🧒 عالم الأطفال", () -> message("أهلاً بكم في عالم الأطفال"));
        addButton("📿 المسبحة الإلكترونية", this::tasbeeh);
        addButton("💰 حاسبة الزكاة", this::zakat);
        addButton("❓ المسابقات والأسئلة", () -> message("المسابقات قيد التجهيز"));
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
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (int i = 0; i < labels.length; i++) {
            Button button = makeAudioButton(labels[i], actions[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1f);
            params.setMargins(3, 5, 3, 5);
            row.addView(button, params);
        }
        layout.addView(row, new LinearLayout.LayoutParams(-1, -2));
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

    void showAdultSection() {
        setup();
        addText("❁ قسم الكبار ❁", 29, Color.rgb(238, 204, 125));
        addText("زادُ القلب وطمأنينة الروح", 17, Color.WHITE);
        HorizontalScrollView horizontalMenu = new HorizontalScrollView(this);
        horizontalMenu.setHorizontalScrollBarEnabled(false);
        horizontalMenu.setFillViewport(false);
        LinearLayout cardRow = new LinearLayout(this);
        cardRow.setOrientation(LinearLayout.HORIZONTAL);
        cardRow.setPadding(8, 8, 8, 8);
        horizontalMenu.addView(cardRow, new HorizontalScrollView.LayoutParams(-2, -2));
        layout.addView(horizontalMenu, new LinearLayout.LayoutParams(-1, -2));
        addAdultCardHorizontal(cardRow, "📖 القرآن الكريم", "قراءة السور بترتيب المصحف", this::showSurahs);
        addAdultCardHorizontal(cardRow, "🎧 الاستماع إلى القرآن", "اختر القارئ والسورة واستمتع بالتلاوة", this::audioScreen);
        addAdultCardHorizontal(cardRow, "🌅 أذكار الصباح والمساء", "أذكار مختارة مع عدّاد للتكرار", this::showAdhkar);
        addAdultCardHorizontal(cardRow, "📿 المسبحة الإلكترونية", "تسبيح واستغفار مع عدّاد", this::tasbeeh);
        addAdultCardHorizontal(cardRow, "💰 حاسبة الزكاة", "حساب ٢٫٥٪ من المبلغ المُدخل", this::zakat);
        addAdultCardHorizontal(cardRow, "🌿 وردي اليومي", "اختر سورة لتجعلها وردك اليوم", this::showDailyWird);
        addAdultCardHorizontal(cardRow, "🔖 متابعة القراءة", "العودة إلى آخر سورة فتحتها", this::continueReading);
        addAdultCardHorizontal(cardRow, "✨ أسماء الله الحسنى", "تأمل في مجموعة من أسماء الله الحسنى", this::showNamesOfAllah);
        addAdultCardHorizontal(cardRow, "🤲 أدعية من القرآن", "أدعية قرآنية مع مواضعها", this::showQuranDuas);
        addAdultCardHorizontal(cardRow, "🗓️ خطة عبادتي اليومية", "ضع علامة على ما أنجزته اليوم", this::showDailyPlan);
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
        addText("✧ أسماء الله الحسنى ✧", 27, Color.rgb(238, 204, 125));
        addText("مجموعة للتأمل والتذكر", 15, Color.WHITE);
        String[] names = {
            "الرَّحْمَٰن","الرَّحِيم","الْمَلِك","الْقُدُّوس",
            "السَّلَام","الْمُؤْمِن","الْمُهَيْمِن","الْعَزِيز",
            "الْجَبَّار","الْمُتَكَبِّر","الْخَالِق","الْبَارِئ",
            "الْمُصَوِّر","الْغَفَّار","الْقَهَّار","الْوَهَّاب",
            "الرَّزَّاق","الْفَتَّاح","الْعَلِيم","السَّمِيع",
            "الْبَصِير","الْحَكَم","الْعَدْل","اللَّطِيف"
        };
        for (int i = 0; i < names.length; i++) {
            addAdultCard("۞ " + toArabicDigits(String.valueOf(i + 1))
                + "  " + names[i], "تأمل في معنى الاسم وادعُ الله به", () ->
                message("نسأل الله أن يرزقنا حسن معرفته ودعائه"));
        }
        addText("هذه مجموعة من الأسماء، وليست عرضاً كاملاً للأسماء التسعة والتسعين.", 13,
            Color.rgb(238, 204, 125));
        addButton("رجوع إلى قسم الكبار", this::showAdultSection);
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

    void audioScreen() {
        setup();
        addText("❁ روضة التلاوات القرآنية ❁", 27, Color.rgb(238, 204, 125));
        addText("اختر قارئك وسورتك المفضلة", 16, Color.WHITE);

        LinearLayout selectorsRow = new LinearLayout(this);
        selectorsRow.setOrientation(LinearLayout.HORIZONTAL);
        selectorsRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout reciterColumn = new LinearLayout(this);
        reciterColumn.setOrientation(LinearLayout.VERTICAL);
        reciterColumn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView reciterLabel = new TextView(this);
        reciterLabel.setText("✦ اختر القارئ");
        reciterLabel.setTextColor(Color.rgb(238, 204, 125));
        reciterLabel.setGravity(Gravity.CENTER);
        reciterColumn.addView(reciterLabel);
        Spinner reciterPicker = styledSpinner(reciterNames, selectedReciter);
        reciterColumn.addView(reciterPicker, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout surahColumn = new LinearLayout(this);
        surahColumn.setOrientation(LinearLayout.VERTICAL);
        surahColumn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView surahLabel = new TextView(this);
        surahLabel.setText("✦ اختر السورة");
        surahLabel.setTextColor(Color.rgb(238, 204, 125));
        surahLabel.setGravity(Gravity.CENTER);
        surahColumn.addView(surahLabel);
        Spinner surahPicker = styledSpinner(surahs, audioSurah - 1);
        surahColumn.addView(surahPicker, new LinearLayout.LayoutParams(-1, -2));

        selectorsRow.addView(reciterColumn);
        selectorsRow.addView(surahColumn);
        layout.addView(selectorsRow, new LinearLayout.LayoutParams(-1, -2));

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
        getSharedPreferences("noor_prefs", MODE_PRIVATE).edit()
            .putInt("last_surah", number).apply();
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




    int count = 0;

    void tasbeeh() {
        setup();
        addText("المسبحة الإلكترونية", 27, Color.WHITE);
        TextView counter = new TextView(this);
        counter.setTextSize(35);
        counter.setTextColor(Color.YELLOW);
        counter.setGravity(Gravity.CENTER);
        counter.setText("0");
        layout.addView(counter);

        addButton("سبّح", () -> {
            count++;
            counter.setText(String.valueOf(count));
        });
        addButton("تصفير", () -> {
            count = 0;
            counter.setText("0");
        });
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
