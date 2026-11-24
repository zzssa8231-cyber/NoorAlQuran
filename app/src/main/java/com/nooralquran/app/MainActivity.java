
package com.nooralquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout layout;
    int green = Color.rgb(12, 65, 54);
    int gold = Color.rgb(230, 190, 95);
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home();
    }

    void setup(String title) {
        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(22, 30, 22, 30);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(green);

        scroll.addView(layout);
        setContentView(scroll);

        text(title, 28, gold);
    }

    void text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 18, 10, 18);
        layout.addView(t);
    }

    void button(String name, Runnable action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextSize(17);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(25, 125, 100)
            )
        );
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 9, 0, 9);
        layout.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    void home() {
        setup("نور القرآن");
        text("نورٌ يضيء حياتك", 18, Color.WHITE);

        button("📖 القرآن الكريم", () -> quran());
        button("🎧 الاستماع إلى القرآن", () -> audio());
        button("👨‍👩‍👧 قسم الكبار", () -> adults());
        button("🧒 عالم الأطفال", () -> kids());
        button("📿 المسبحة الإلكترونية", () -> tasbeeh());
        button("💰 حاسبة الزكاة", () -> zakat());
        button("❓ المسابقات والأسئلة", () -> quiz());
    }

    void back() {
        button("⬅ العودة للرئيسية", () -> home());
    }

    void quran() {
        setup("القرآن الكريم");
        String[] surahs = {
            "الفاتحة", "البقرة", "آل عمران",
            "النساء", "المائدة", "الأنعام",
            "الأعراف", "الأنفال", "التوبة",
            "يونس", "هود", "يوسف",
            "الرعد", "إبراهيم", "الحجر"
        };

        for (String s : surahs) {
            button("سورة " + s, () -> {
                setup("سورة " + s);
                text("سيتم إضافة نص السورة كاملاً في تحديث المحتوى.", 19, Color.WHITE);
                back();
            });
        }
        back();
    }

    void audio() {
        setup("الاستماع إلى القرآن");
        text("قسم التلاوات الصوتية", 20, Color.WHITE);
        text("سيتم إضافة ملفات التلاوة الصوتية في تحديث لاحق.", 17, gold);
        back();
    }

    void adults() {
        setup("قسم الكبار");
        button("القرآن الكريم", () -> quran());
        button("المسبحة الإلكترونية", () -> tasbeeh());
        button("حاسبة الزكاة", () -> zakat());
        button("المسابقات", () -> quiz());
        back();
    }

    void kids() {
        setup("عالم الأطفال");
        text("مرحباً يا بطل!", 25, gold);
        text("تعلم القرآن والأخلاق الإسلامية", 19, Color.WHITE);
        button("اختبر معلوماتك", () -> quiz());
        button("قراءة القرآن", () -> quran());
        back();
    }

    void tasbeeh() {
        setup("المسبحة الإلكترونية");
        TextView number = new TextView(this);
        number.setText("0");
        number.setTextSize(45);
        number.setTextColor(gold);
        number.setGravity(Gravity.CENTER);
        layout.addView(number);

        button("سبحان الله", () -> {
            count++;
            number.setText(String.valueOf(count));
        });

        button("تصفير العداد", () -> {
            count = 0;
            number.setText("0");
        });
        back();
    }

    void zakat() {
        setup("حاسبة الزكاة");
        text("أدخل المبلغ لحساب 2.5%", 18, Color.WHITE);

        EditText input = new EditText(this);
        input.setHint("المبلغ");
        input.setInputType(8194);
        input.setTextColor(Color.WHITE);
        layout.addView(input);

        button("احسب الزكاة", () -> {
            try {
                double amount = Double.parseDouble(
                    input.getText().toString()
                );
                text("الزكاة: " +
                    String.format(Locale.US, "%.2f", amount * 0.025),
                    22, gold);
            } catch (Exception e) {
                Toast.makeText(this,
                    "أدخل مبلغاً صحيحاً", Toast.LENGTH_SHORT).show();
            }
        });
        back();
    }

    void quiz() {
        setup("مسابقة نور القرآن");
        text("كم عدد سور القرآن الكريم؟", 21, Color.WHITE);
        button("114 سورة", () -> result(true));
        button("100 سورة", () -> result(false));
        button("120 سورة", () -> result(false));
        back();
    }

    void result(boolean correct) {
        setup("نتيجة المسابقة");
        text(correct ? "أحسنت! إجابة صحيحة 🌟" :
            "حاول مرة أخرى ❤️", 23, gold);
        back();
    }
}
