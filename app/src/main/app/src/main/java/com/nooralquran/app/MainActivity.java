package com.nooralquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(24, 35, 24, 35);
        layout.setBackgroundColor(Color.rgb(12, 45, 42));

        scroll.addView(layout);
        setContentView(scroll);

        addText("نور القرآن", 34, Color.WHITE);
        addText("نورٌ يضيء حياتك", 18, Color.YELLOW);

        addButton("📖 القرآن الكريم");
        addButton("🎧 الاستماع إلى القرآن");
        addButton("👨‍👩‍👧 قسم الكبار");
        addButton("🧒 عالم الأطفال");
        addButton("📿 المسبحة الإلكترونية");
        addButton("💰 حاسبة الزكاة");
        addButton("❓ المسابقات والأسئلة");
    }

    void addText(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER);
        text.setPadding(10, 18, 10, 18);
        layout.addView(text);
    }

    void addButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(18);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(25, 125, 100)
            )
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                -1, -2
            );

        params.setMargins(0, 12, 0, 12);
        layout.addView(button, params);
    }
}
