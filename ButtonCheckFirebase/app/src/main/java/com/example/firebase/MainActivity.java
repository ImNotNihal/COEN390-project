package com.example.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private TextView statusText;
    private Handler handler;
    private Runnable runnable;
    private static final String CHANNEL_ID = "status_notification_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        statusText = findViewById(R.id.status_text);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> resetStatus());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                checkFirebaseStatus();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void resetStatus() {
        mDatabase.child("status").setValue(0);
    }

    private void checkFirebaseStatus() {
        mDatabase.child("status").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    int status = snapshot.getValue(Integer.class);
                    if (status == 1) {
                        statusText.setText("Status: Alert");
                        showNotification();
                    } else {
                        statusText.setText("Status: Good");
                    }
                }
            } else {
                handleFirebaseError(task.getException());
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Status Notification Channel";
            String description = "Channel for status notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("ALERT!")
                .setContentText("Wristband Owner Pressed Button")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void handleFirebaseError(Exception error) {
        if (error != null) {
            Log.e("Firebase Error", error.getMessage());
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}


