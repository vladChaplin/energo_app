package com.example.application_energo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application_energo.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Подключаем меню
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_slider) {
            // Переход на SliderActivity
            startActivity(new Intent(this, SliderActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            // Переход на HelpActivity
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if (id == R.id.action_exit) {
            // Закрытие приложения
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
