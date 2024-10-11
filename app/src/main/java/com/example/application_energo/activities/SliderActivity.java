package com.example.application_energo.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.application_energo.R;
import com.example.application_energo.activities.adapter.ImageSliderAdapter;

import java.util.Arrays;
import java.util.List;

public class SliderActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TextView descriptionTextView;
    private List<String> descriptions = Arrays.asList(
            "Величественные горы обрамляют город Алматы, создавая впечатляющий пейзаж",
            "Панорама Алматы: современный город на фоне снежных вершин.",
            "Гостиница \"Алматы\" — историческая часть города с видом на горы и центральные улицы."
    );

    private List<Integer> images = Arrays.asList(
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        viewPager = findViewById(R.id.viewPager);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        ImageSliderAdapter adapter = new ImageSliderAdapter(this, images);
        viewPager.setAdapter(adapter);

        // Устанавливаем начальное описание
        descriptionTextView.setText(descriptions.get(0));

        // Слушатель для изменения описания при пролистывании
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                descriptionTextView.setText(descriptions.get(position));
            }
        });
    }
}
