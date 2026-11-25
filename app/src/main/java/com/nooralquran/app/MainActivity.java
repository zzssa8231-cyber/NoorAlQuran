
package com.nooralquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    LinearLayout layout;
    List<String[]> verses = new ArrayList<>();

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
        addButton("🎧 الاستماع إلى القرآن", () -> message("قسم التلاوة الصوتية قيد التجهيز"));
        addButton("👨‍👩‍👧 قسم الكبار", () -> message("أهلاً بكم في قسم الكبار"));
        addButton("🧒 عالم الأطفال", () -> message("أهلاً بكم في عالم الأطفال"));
        addButton("📿 المسبحة الإلكترونية", this::tasbeeh);
        addButton("💰 حاسبة الزكاة", this::zakat);
        addButton("❓ المسابقات والأسئلة", () -> message("المسابقات قيد التجهيز"));
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
        setup();
        addText("سورة " + surahs[number - 1], 28, Color.YELLOW);
        addButton("رجوع إلى السور", this::showSurahs);

        int found = 0;
        for (String[] verse : verses) {
            if (Integer.parseInt(verse[0]) == number) {
                addText(formatVerse(verse[2], verse[1]),
                    23, Color.WHITE);
                found++;
            }
        }

        if (found == 0) {
            addText("لم يتم العثور على آيات هذه السورة", 18, Color.YELLOW);
        }
    }


    String formatVerse(String verseText, String verseNumber) {
        String basmala = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ";
        String text = verseText.trim();

        if (text.startsWith(basmala)) {
            String rest = text.substring(basmala.length()).trim();
            if (rest.isEmpty()) {
                return basmala + "   (" + verseNumber + ")";
            }
            return basmala + "\\n۞ ﴿" + rest + "﴾ ۞   (" + verseNumber + ")";
        }

        return "۞ ﴿" + text + "﴾ ۞   (" + verseNumber + ")";
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
