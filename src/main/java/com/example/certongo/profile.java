package com.example.certongo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class profile extends AppCompatActivity {

    private static final String TAG = "profile";
    private TextView nametxt, emailtxt, agetxt, dobtxt, addresstxt, mobiletxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nametxt = findViewById(R.id.nametxt);
        emailtxt = findViewById(R.id.emailtxt);
        agetxt = findViewById(R.id.agetxt);
        dobtxt = findViewById(R.id.dobtxt);
        addresstxt = findViewById(R.id.addresstxt);
        mobiletxt = findViewById(R.id.mobiletxt);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.action_profile);

        fetchUserData();

        Button changePassButton = findViewById(R.id.changepass);
        changePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordResetEmail();
            }
        });

        Button signOutButton = findViewById(R.id.signoutz);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        Button deleteAccountButton = findViewById(R.id.deleteacc);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    public BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            Intent intentHistory = new Intent(profile.this, home.class);
                            startActivity(intentHistory);
                            break;
                        case R.id.action_request:
                            Intent intentRequest = new Intent(profile.this, crerep.class);
                            startActivity(intentRequest);
                            break;
                        case R.id.action_profile:
                            break;
                    }
                    return true;
                }
            };

    private void fetchUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userId);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        int age = documentSnapshot.getLong("age").intValue();
                        String dateOfBirth = documentSnapshot.getString("dateOfBirth");
                        String address = documentSnapshot.getString("address");
                        String mobileNo = documentSnapshot.getString("mobileNo");

                        nametxt.setText(name);
                        emailtxt.setText(email);
                        agetxt.setText("Age: " + String.valueOf(age));
                        dobtxt.setText("Date of Birth: " + dateOfBirth);
                        addresstxt.setText("Address: " + address);
                        mobiletxt.setText("Mobile Number: " + mobileNo);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Error getting document", e);
                }
            });
        }
    }

    private void sendPasswordResetEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                Toast.makeText(profile.this, "Reset password email has been sent, check your email.", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Error in sending password reset email.", task.getException());
                                Toast.makeText(profile.this, "Error in sending password reset email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(profile.this, "No authenticated user.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(profile.this, SignIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");
        builder.setIcon(R.drawable.warning);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAccount();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button deleteButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        deleteButton.setTextColor(getResources().getColor(R.color.red));

        Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(profile.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(profile.this, SignIn.class));
                                finish();
                            } else {
                                Log.w(TAG, "Failed to delete account", task.getException());
                                Toast.makeText(profile.this, "Failed to delete account. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
