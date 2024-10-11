package com.example.application_energo.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_energo.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView textView = findViewById(R.id.textView);

        // Текст с описанием
        String text = "Приложение, разработанное студентом Чаплиным В.Ю. (группа ИСу-22-2), " +
                "предназначено для удобного и эффективного использования функционала с интуитивной навигацией";

        // SpannableString для изменения цвета текста
        SpannableString spannableString = new SpannableString(text);

        // Изменение цвета инициалов "Чаплиным В.Ю." на красный
        int startIndex = text.indexOf("Чаплиным В.Ю.");
        int endIndex = startIndex + "Чаплиным В.Ю.".length();
        if (startIndex >= 0) {
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Установка текста в TextView
        textView.setText(spannableString);
    }
}
