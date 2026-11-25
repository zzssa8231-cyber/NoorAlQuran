
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
        addButton("👨‍👩‍👧 قسم الكبار", () -> message("أهلاً بكم في قسم الكبار"));
        addButton("🧒 عالم الأطفال", () -> message("أهلاً بكم في عالم الأطفال"));
        addButton("📿 المسبحة الإلكترونية", this::tasbeeh);
        addButton("💰 حاسبة الزكاة", this::zakat);
        addButton("❓ المسابقات والأسئلة", () -> message("المسابقات قيد التجهيز"));
    }

    void audioScreen() {
        setup();
        addText("الاستماع إلى القرآن الكريم", 27, Color.WHITE);
        addText("تلاوة الشيخ مشاري العفاسي", 18, Color.rgb(238, 204, 125));

        Spinner picker = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, surahs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setSelection(audioSurah - 1);
        layout.addView(picker, new LinearLayout.LayoutParams(-1, -2));

        audioStatus = new TextView(this);
        audioStatus.setText("اختر السورة ثم اضغط تشغيل");
        audioStatus.setTextColor(Color.WHITE);
        audioStatus.setTextSize(17);
        audioStatus.setGravity(Gravity.CENTER);
        audioStatus.setPadding(10, 22, 10, 22);
        layout.addView(audioStatus);

        addButton("▶ تشغيل السورة", () -> {
            audioSurah = picker.getSelectedItemPosition() + 1;
            audioAyah = 1;
            audioAyahCount = countAyahs(audioSurah);
            if (audioAyahCount == 0) {
                audioStatus.setText("تعذر تحديد آيات السورة من ملف القرآن");
            } else {
                playCurrentAyah();
            }
        });
        addButton("⏸ إيقاف مؤقت / متابعة", this::toggleAudio);
        addButton("⏹ إيقاف", this::stopAudio);
        addButton("رجوع", this::home);
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
            String url = "https://everyayah.com/data/Alafasy_128kbps/" + file;
            audioPlayer = new MediaPlayer();
            audioPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(url);
            audioPlayer.setOnPreparedListener(player -> {
                audioPreparing = false;
                audioStatus.setText("يُتلى الآن: سورة " + surahs[audioSurah - 1]
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
