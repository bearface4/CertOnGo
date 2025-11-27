package com.example.certongo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Infos extends AppCompatActivity {

    private static final String TAG = "Infos";
    private String oldStatus = "";
    private String oldFeedback = "";
    private String oldReqfile = "";
    private SharedPreferences prefs;

    // Method to check if the notification has been seen
    private boolean isNotificationSeen(String notificationId) {
        return prefs.getBoolean(notificationId, false);
    }

    // Method to mark the notification as seen
    private void markNotificationAsSeen(String notificationId) {
        prefs.edit().putBoolean(notificationId, true).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infos);
        prefs = getSharedPreferences("com.example.certongo", MODE_PRIVATE);

        // Check if the WRITE_EXTERNAL_STORAGE permission is already available.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CertongoChannel";
            String description = "Channel for Certongo notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Certongo", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Button backbut = findViewById(R.id.backbut);

        backbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Infos.this, home.class);
                startActivity(intent);
            }
        });

        // Set retrieved data to TextViews or other UI elements in the layout
        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView typeTextView = findViewById(R.id.typeTextView);
        TextView purposeTextView = findViewById(R.id.purposeTextView);
        TextView statTextView = findViewById(R.id.statusTextView);
        TextView feedTextView = findViewById(R.id.FeedbackTextView);
        TextView DateTextView = findViewById(R.id.RequestClaimView);
        TextView FileTextView = findViewById(R.id.FileClaimView);

        // Create a ClickableSpan
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                String fullText = FileTextView.getText().toString(); // get the full text
                String reqfileLink = fullText.replace("Requested File: ", ""); // remove the "Requested File: " part

                if (reqfileLink == null || reqfileLink.isEmpty()) {
                    Log.e(TAG, "File link is null or empty");
                    return;
                }

                Uri downloadUri;
                try {
                    downloadUri = Uri.parse(reqfileLink);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing URL: " + reqfileLink, e);
                    return;
                }

                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Download");
                request.setDescription("The file is being downloaded...");

                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                // Set the local destination for the downloaded file to a path within the application's external files directory
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis());

                // Get download service and enqueue file
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if(manager == null) {
                    Log.e(TAG, "Download manager is not available");
                    return;
                }

                try {
                    manager.enqueue(request);
                } catch (Exception e) {
                    Log.e(TAG, "Error enqueuing download manager request", e);
                }
            }
        };

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid(); // get the current user's ID
        String documentId = getIntent().getStringExtra("documentId"); // get the document ID passed from the previous activity
        DocumentReference docRef = db.collection("users").document(userID).collection("docus").document(documentId);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Log the error and return
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Update the status, feedback, and requested file TextViews
                    String name = snapshot.getString("name");
                    String typeOfDocument = snapshot.getString("type_of_document");
                    String purposeOfRequest = snapshot.getString("purpose_of_request");
                    String date = snapshot.getString("date");
                    String status = snapshot.getString("status");
                    String feedback = snapshot.getString("feedback");
                    String reqfile = snapshot.getString("reqfile");

                    nameTextView.setText("Name: " + name);
                    typeTextView.setText("Type of Document: " + typeOfDocument);
                    purposeTextView.setText("Purpose of Request: " + purposeOfRequest);
                    statTextView.setText("Status: " + status);
                    feedTextView.setText("Feedback: " + feedback);
                    DateTextView.setText("Requested claiming date: " + date);

                    SpannableString ss = new SpannableString("Requested File: " + reqfile);
                    ss.setSpan(clickableSpan, 16, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    FileTextView.setText(ss);
                    FileTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    // Generate unique notificationId
                    int notificationId = (int) System.currentTimeMillis();

                    // Check if status has changed
                    if (!oldStatus.equals(status) && !status.equals("Pending")) {
                        if (!isNotificationSeen("statusNotification" + notificationId)) {
                            // Create a notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(Infos.this, "Certongo")
                                    .setSmallIcon(R.drawable.notif)
                                    .setContentTitle("CertonGo")
                                    .setContentText("The status for " + typeOfDocument + " has been updated to: " + status)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Infos.this);
                            notificationManager.notify(notificationId, builder.build());
                            oldStatus = status;

                            // Mark the notification as seen
                            markNotificationAsSeen("statusNotification" + notificationId);
                        }
                    }


                    if (!oldFeedback.equals(feedback) && feedback != null) {
                        if (!isNotificationSeen("feedbackNotification" + notificationId)) {
                            // Create a notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(Infos.this, "Certongo")
                                    .setSmallIcon(R.drawable.notif)
                                    .setContentTitle("CertonGo Update")
                                    .setContentText("Feedback for " + typeOfDocument + " is available: " + feedback)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            // Show the notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Infos.this);
                            notificationManager.notify(notificationId, builder.build());
                            oldFeedback = feedback;

                            // Mark the notification as seen
                            markNotificationAsSeen("feedbackNotification" + notificationId);
                        }
                    }


                    if (!oldReqfile.equals(reqfile) && reqfile != null) {
                        if (!isNotificationSeen("reqfileNotification" + notificationId)) {
                            // Create a notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(Infos.this, "Certongo")
                                    .setSmallIcon(R.drawable.notif)
                                    .setContentTitle("CertonGo")
                                    .setContentText("The requested file for " + typeOfDocument + " is now available.")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            // Show the notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Infos.this);
                            notificationManager.notify(notificationId, builder.build());
                            oldReqfile = reqfile;

                            // Mark the notification as seen
                            markNotificationAsSeen("reqfileNotification" + notificationId);
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}
