package com.example.certongo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class crerep extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crerep);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }


        Button btnChooseDate = findViewById(R.id.btnChooseDate);
        TextView dateTextView = findViewById(R.id.dateTextView);
        Button submit = findViewById(R.id.submit);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.action_request);

        btnChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(crerep.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar selectedDate = Calendar.getInstance();
                                selectedDate.set(year, monthOfYear, dayOfMonth);

                                if (selectedDate.before(c)) {
                                    Toast.makeText(crerep.this, "You cannot choose a date in the past.", Toast.LENGTH_SHORT).show();
                                } else {
                                    String formattedDate = String.format("%02d/%02d/%02d", monthOfYear + 1, dayOfMonth, year % 100);
                                    dateTextView.setText(formattedDate);
                                    dateTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((TextInputEditText) findViewById(R.id.etName)).getText().toString();
                String documentType = ((Spinner) findViewById(R.id.spinnerDocumentType)).getSelectedItem().toString();
                String purpose = ((TextInputEditText) findViewById(R.id.etPurpose)).getText().toString();
                String date = dateTextView.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(crerep.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (purpose.isEmpty()) {
                    Toast.makeText(crerep.this, "Purpose of request cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (date.isEmpty()) {
                    Toast.makeText(crerep.this, "Please choose a date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(crerep.this)
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to submit this request?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, Object> report = new HashMap<>();
                                report.put("name", name);
                                report.put("type_of_document", documentType);
                                report.put("purpose_of_request", purpose);
                                report.put("date", date);
                                report.put("status", "Pending"); // Add status field

                                // Get the current date and time as a string
                                String submitDateString = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a z", Locale.getDefault()).format(new Date());
                                report.put("submit_date", submitDateString);

                                db.collection("users")
                                        .document(userId)
                                        .collection("docus")
                                        .add(report)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.update("submit_date", FieldValue.serverTimestamp());

                                                Toast.makeText(crerep.this, "Document request sent successfully", Toast.LENGTH_SHORT).show();

                                                Intent intentRecrep = new Intent(crerep.this, recrep.class);
                                                startActivity(intentRecrep);

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intentHome = new Intent(crerep.this, home.class);
                                                        startActivity(intentHome);
                                                        finish();
                                                    }
                                                }, 5000);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(crerep.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    public BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            Intent intentHome = new Intent(crerep.this, home.class);
                            startActivity(intentHome);
                            break;
                        case R.id.action_request:
                            break;
                        case R.id.action_profile:
                            Intent intentProfile = new Intent(crerep.this, profile.class);
                            startActivity(intentProfile);
                            break;
                    }
                    return true;
                }
            };
}
