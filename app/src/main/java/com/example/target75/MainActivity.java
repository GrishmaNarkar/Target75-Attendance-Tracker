package com.example.target75;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CircularProgressIndicator materialProgress;
    private TextView tvDonutPercentage;
    private TextView tvStatusTitle;
    private RecyclerView rvSubjects;
    private MaterialButton btnAddSubject;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    private SubjectAdapter subjectAdapter;
    private List<Subject> subjectList;
    private Random random = new Random();

    private final String[] dangerSentences = {
            "NEED TO ATTEND LECTURES!",
            "Bhai proxy lagwana shuru kar de 💀",
            "Ghar pe letter aane wala hai lagta hai!",
            "College tumhara intezar kar raha hai, jao!",
            "Attendance duba di bhai tumne toh.",
            "Padhai likhai mein dhyan lagao, IAS-YAS bano...",
            "Teacher toh list lekar baithi hai tumhari!",
            "Defaulter list mein top karne ka irada hai?",
            "Chalo chalo, last bench tumhein bula rahi hai.",
            "Kitna bunk karoge? Thoda toh reham khao!",
            "Lagta hai detention ka shauk hai tumhein.",
            "Exam dene milega na? Soch lo ek baar!"
    };

    private final String[] safeSentences = {
            "CAN BUNK EASILY!",
            "Mauj lo! Attendance ekdum kadak hai 😎",
            "Aaj ka din bunk ke naam, ghumne jao!",
            "Sona hai toh so jao, scene ekdum clear hai.",
            "Topper wali feel aa rahi hai na?",
            "Attendance full tight! System phaad diya.",
            "Teacher bhi dekh ke hairan hai tumhein!",
            "Bunk maaro par thoda dhyan se haan.",
            "Relax, target achieved! Ab chill karo.",
            "75% se upar chal rahe ho, raja ho tum!",
            "Aaj toh canteen mein party banti hai.",
            "Gharwalo ko dikhane layak percentage hai!",
            "Attendance on fleek! Bunking mode: ON"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Permission Check for Android 13+
        checkNotificationPermission();

        // 🔥 Start automatic 45 mins notification alarm
        start45MinNotificationAlarm();

        // Views Initialization
        materialProgress = findViewById(R.id.donut_progress_material);
        tvDonutPercentage = findViewById(R.id.tvDonutPercentage);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        rvSubjects = findViewById(R.id.rvSubjects);
        btnAddSubject = findViewById(R.id.btnAddSubject);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menuIcon);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setItemIconTintList(null);

        Menu menu = navigationView.getMenu();
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);
        SpannableString s = new SpannableString(logoutItem.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#FF3B3B")), 0, s.length(), 0);
        logoutItem.setTitle(s);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Already on Home
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(MainActivity.this, "Profile screen coming soon!", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_notifications) {
                    // 🔥 Manual trigger for testing funny notifications
                    sendTestNotification();
                } else if (id == R.id.nav_theme) {
                    Toast.makeText(MainActivity.this, "Dark/Light Mode", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        loadData();

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        subjectAdapter = new SubjectAdapter(subjectList, new SubjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showUpdateSubjectDialog(position);
            }
        });
        rvSubjects.setAdapter(subjectAdapter);

        calculateAndAnimateAttendance();

        btnAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSubjectDialog();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    // 🔥 AUTOMATIC 45 MINS BACKGROUND ALARM SCHEDULER
    private void start45MinNotificationAlarm() {
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent == null) {
            PendingIntent scheduleIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            long interval = 45 * 60 * 1000; // 45 Minutes
            long triggerTime = System.currentTimeMillis() + interval;

            if (alarmManager != null) {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, scheduleIntent);
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // 🔥 Manual test function for Sidebar click (Now calls the Funny Receiver)
    private void sendTestNotification() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        sendBroadcast(intent);
    }

    private void showAddSubjectDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_add_subject);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etSubjectName = dialog.findViewById(R.id.etSubjectName);
        EditText etPresentCount = dialog.findViewById(R.id.etPresentCount);
        EditText etAbsentCount = dialog.findViewById(R.id.etAbsentCount);
        MaterialButton btnDialogDone = dialog.findViewById(R.id.btnDialogDone);
        MaterialButton btnDialogDelete = dialog.findViewById(R.id.btnDialogDelete);

        btnDialogDelete.setVisibility(View.GONE);
        btnDialogDone.setBackgroundColor(Color.parseColor("#00B0FF"));
        btnDialogDone.setTextColor(Color.WHITE);

        btnDialogDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etSubjectName.getText().toString().trim();
                String presentStr = etPresentCount.getText().toString().trim();
                String absentStr = etAbsentCount.getText().toString().trim();

                if (name.isEmpty() || presentStr.isEmpty() || absentStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Bhai poora fill kar!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int present = Integer.parseInt(presentStr);
                int absent = Integer.parseInt(absentStr);

                subjectList.add(new Subject(name, present, absent));
                subjectAdapter.notifyDataSetChanged();
                calculateAndAnimateAttendance();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showUpdateSubjectDialog(final int position) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_add_subject);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        EditText etSubjectName = dialog.findViewById(R.id.etSubjectName);
        EditText etPresentCount = dialog.findViewById(R.id.etPresentCount);
        EditText etAbsentCount = dialog.findViewById(R.id.etAbsentCount);
        MaterialButton btnDialogDone = dialog.findViewById(R.id.btnDialogDone);
        MaterialButton btnDialogDelete = dialog.findViewById(R.id.btnDialogDelete);

        btnDialogDelete.setVisibility(View.VISIBLE);
        btnDialogDone.setBackgroundColor(Color.parseColor("#00B0FF"));
        btnDialogDone.setTextColor(Color.WHITE);

        btnDialogDelete.setBackgroundColor(Color.TRANSPARENT);
        btnDialogDelete.setTextColor(Color.parseColor("#FF3B3B"));
        btnDialogDelete.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        btnDialogDelete.setStrokeWidth(0);

        Subject selectedSubject = subjectList.get(position);
        dialogTitle.setText(selectedSubject.getName());
        etSubjectName.setText(selectedSubject.getName());
        etSubjectName.setEnabled(false);
        etSubjectName.setAlpha(0.5f);

        etPresentCount.setText(String.valueOf(selectedSubject.getPresent()));
        etAbsentCount.setText(String.valueOf(selectedSubject.getAbsent()));

        btnDialogDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presentStr = etPresentCount.getText().toString().trim();
                String absentStr = etAbsentCount.getText().toString().trim();

                if (presentStr.isEmpty() || absentStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Bhai khali mat chhod!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int newPresent = Integer.parseInt(presentStr);
                int newAbsent = Integer.parseInt(absentStr);

                selectedSubject.setPresent(newPresent);
                selectedSubject.setAbsent(newAbsent);
                subjectAdapter.notifyItemChanged(position);
                calculateAndAnimateAttendance();
                dialog.dismiss();
            }
        });

        btnDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjectList.remove(position);
                subjectAdapter.notifyItemRemoved(position);
                subjectAdapter.notifyItemRangeChanged(position, subjectList.size());
                calculateAndAnimateAttendance();
                Toast.makeText(MainActivity.this, "Subject Deleted!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void calculateAndAnimateAttendance() {
        saveData();

        if (subjectList != null && subjectList.size() > 1) {
            java.util.Collections.sort(subjectList, new java.util.Comparator<Subject>() {
                @Override
                public int compare(Subject s1, Subject s2) {
                    return Integer.compare(s1.getPercentage(), s2.getPercentage());
                }
            });
            subjectAdapter.notifyDataSetChanged();
        }

        int totalPresent = 0;
        int totalLectures = 0;

        for (Subject subject : subjectList) {
            totalPresent += subject.getPresent();
            totalLectures += subject.getTotal();
        }

        int targetPercentage = 0;
        if (totalLectures > 0) {
            targetPercentage = (totalPresent * 100) / totalLectures;
        }

        // 🔥 NAYA CODE: Notification Receiver ke liye percentage save kar rahe hain
        android.content.SharedPreferences prefs = getSharedPreferences("Target75Prefs", MODE_PRIVATE);
        prefs.edit().putInt("overall_percentage", targetPercentage).apply();

        if (targetPercentage < 75) {
            int randomIndex = random.nextInt(dangerSentences.length);
            tvStatusTitle.setText(dangerSentences[randomIndex]);
            materialProgress.setIndicatorColor(Color.parseColor("#FF3B3B"));
        } else {
            int randomIndex = random.nextInt(safeSentences.length);
            tvStatusTitle.setText(safeSentences[randomIndex]);
            materialProgress.setIndicatorColor(Color.parseColor("#1E88E5"));
        }

        ValueAnimator animator = ValueAnimator.ofInt(0, targetPercentage);
        animator.setDuration(1200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                materialProgress.setProgress(animatedValue);
                tvDonutPercentage.setText(animatedValue + "%");
            }
        });
        animator.start();
    }

    private void saveData() {
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("Target75Prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String json = gson.toJson(subjectList);
        editor.putString("subject_list", json);
        editor.apply();
    }

    private void loadData() {
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("Target75Prefs", MODE_PRIVATE);
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String json = sharedPreferences.getString("subject_list", null);
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<ArrayList<Subject>>() {}.getType();
        subjectList = gson.fromJson(json, type);

        if (subjectList == null) {
            subjectList = new ArrayList<>();
        }
    }
}