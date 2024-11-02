package com.example.application_energo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.ArrayAdapter;
import com.example.application_energo.R;
import com.example.application_energo.activities.player.CustomMediaController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaPlayer audioPlayer;
    private ListView mediaListView;
    private Button addMediaButton;
    private Button closeMediaButton;
    private ArrayAdapter<String> mediaListAdapter;
    private List<String> mediaList;
    private ViewGroup videoContainer;
    private TextView welcomeTextView;
    private ConstraintLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация элементов интерфейса
        mediaListView = findViewById(R.id.mediaListView);
        addMediaButton = findViewById(R.id.addMediaButton);
        closeMediaButton = findViewById(R.id.closeMediaButton);
        videoView = findViewById(R.id.videoView);
        videoContainer = findViewById(R.id.videoContainer);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        mainLayout = findViewById(R.id.mainLayout);

        mediaList = new ArrayList<>();
        mediaListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mediaList);
        mediaListView.setAdapter(mediaListAdapter);

        loadMediaList();

        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("MainActivity", "Add media button clicked");
                pickMediaFromStorage();
            }
        });

        closeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                if (audioPlayer != null && audioPlayer.isPlaying()) {
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                }
                exitFullScreen();

                videoContainer.setVisibility(View.GONE);
                mediaListView.setVisibility(View.VISIBLE);
                addMediaButton.setVisibility(View.VISIBLE);
                welcomeTextView.setVisibility(View.VISIBLE);
                closeMediaButton.setVisibility(View.GONE);
            }
        });

        mediaListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMedia = mediaList.get(position);
            Uri mediaUri = getExternalMediaUri(selectedMedia);
            if (mediaUri != null) {
                String extension = getFileExtension(selectedMedia);
                String mimeType = null;
                if (extension != null) {
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    mimeType = mime.getMimeTypeFromExtension(extension.toLowerCase());
                }
                if (mimeType != null) {
                    if (mimeType.startsWith("video")) {
                        // Воспроизведение видео
                        videoContainer.setVisibility(View.VISIBLE);
                        mediaListView.setVisibility(View.GONE);
                        addMediaButton.setVisibility(View.GONE);
                        welcomeTextView.setVisibility(View.GONE);
                        closeMediaButton.setVisibility(View.VISIBLE);

                        enterFullScreen();

                        videoView.setVideoURI(mediaUri);
                        videoView.requestFocus();
                        CustomMediaController mediaController = new CustomMediaController(MainActivity.this);
                        mediaController.setAnchorView(videoView);
                        mediaController.setPadding(0, 0, 0, 100); // Отступы для контролов
                        videoView.setMediaController(mediaController);
                        videoView.start();
                    } else if (mimeType.startsWith("audio")) {
                        // Воспроизведение аудио
                        if (audioPlayer != null) {
                            audioPlayer.release();
                            audioPlayer = null;
                        }
                        audioPlayer = new MediaPlayer();
                        try {
                            audioPlayer.setDataSource(MainActivity.this, mediaUri);
                            audioPlayer.prepare();
                            audioPlayer.start();
                            closeMediaButton.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error playing audio: " + e.getMessage());
                        }
                    } else {
                        Log.e("MainActivity", "Unsupported media type");
                    }
                } else {
                    Log.e("MainActivity", "Could not determine MIME type");
                }
            } else {
                Log.e("MainActivity", "Media file not found");
            }
        });

        // Дополнительно, добавляем медиафайлы из ресурсов
        addRawMediaToMediaList();
    }

    @Override
    public void onBackPressed() {
        if (videoContainer.getVisibility() == View.VISIBLE) {
            // Если видео воспроизводится, останавливаем его и возвращаемся к списку
            closeMediaButton.performClick();
        } else {
            super.onBackPressed();
        }
    }

    private void enterFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(selectedMediaUri, takeFlags);
                saveMedia(selectedMediaUri);
            }
        } else {
            Log.d("MainActivity", "Файл не выбран или результат не OK");
        }
    }

    private void loadMediaList() {
        File mediaDir = getExternalFilesDir("media");
        if (mediaDir != null && mediaDir.exists()) {
            File[] files = mediaDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        mediaList.add(file.getName());
                    }
                }
                mediaListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void addRawMediaToMediaList() {
        // Если у вас есть медиафайлы в ресурсах raw, вы можете их добавить
        // Пример для видео:
        String rawVideoName = "first_video.mp4";
        copyRawMediaToStorage(R.raw.first_video, rawVideoName);

        // Пример для аудио:
        String rawAudioName = "first_audio.mp3";
        copyRawMediaToStorage(R.raw.first_audio, rawAudioName);
    }

    private void copyRawMediaToStorage(int rawResourceId, String fileName) {
        File mediaDir = getExternalFilesDir("media");
        if (mediaDir != null) {
            File destination = new File(mediaDir, fileName);
            if (!destination.exists()) {
                try {
                    InputStream in = getResources().openRawResource(rawResourceId);
                    OutputStream out = new FileOutputStream(destination);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.close();
                    mediaList.add(fileName);
                    mediaListAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("MainActivity", "Error saving raw media: " + e.getMessage());
                }
            }
        }
    }

    private void pickMediaFromStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"video/*", "audio/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, 2);
    }

    private void saveMedia(Uri selectedMediaUri) {
        try {
            // Получаем отображаемое имя файла
            String fileName = null;
            Cursor cursor = getContentResolver().query(selectedMediaUri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            if (fileName == null) {
                // Используем стандартное имя файла
                fileName = "selected_media_" + System.currentTimeMillis();
            }

            File mediaDir = getExternalFilesDir("media");
            File destination = new File(mediaDir, fileName);

            InputStream in = getContentResolver().openInputStream(selectedMediaUri);
            OutputStream out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();

            mediaList.add(fileName);
            mediaListAdapter.notifyDataSetChanged();
            Log.d("MainActivity", "Медиа успешно сохранено: " + fileName);
        } catch (Exception e) {
            Log.e("MainActivity", "Ошибка при сохранении медиа: " + e.getMessage());
        }
    }

    private Uri getExternalMediaUri(String fileName) {
        File mediaFile = new File(getExternalFilesDir("media"), fileName);
        if (mediaFile.exists()) {
            return Uri.fromFile(mediaFile);
        }
        return null;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(lastDot + 1);
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayers();
    }

    private void releaseMediaPlayers() {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        if (videoView != null) {
            videoView.suspend();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Инфлейтим меню
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