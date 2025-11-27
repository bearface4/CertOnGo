package com.example.certongo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class SignUp extends AppCompatActivity {

    EditText editTextName, editTextAddress, editTextMobileNo, editTextEmail, editTextPassword;
    Button signUpButton, datePickerButton;
    TextView dateTextView;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextName = findViewById(R.id.name);
        editTextAddress = findViewById(R.id.address);
        editTextMobileNo = findViewById(R.id.MobNu);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        signUpButton = findViewById(R.id.signup2);
        datePickerButton = findViewById(R.id.datePickerButton);
        dateTextView = findViewById(R.id.dateTextView);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.INVISIBLE);

        // Set up clickable "terms and conditions" text
        TextView termsAndConditionsClickable = findViewById(R.id.termsandconditions);
        String fullText = "By clicking sign up, you agree to our terms and conditions.";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("terms and conditions");
        int endIndex = startIndex + "terms and conditions".length();



        // Apply the clickable span to the desired portion of the text


        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String address = editTextAddress.getText().toString().trim();
                String mobileNo = editTextMobileNo.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String dateOfBirth = dateTextView.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) ||
                        TextUtils.isEmpty(mobileNo) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(dateOfBirth)) {
                    Toast.makeText(SignUp.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidName(name)) {
                    Toast.makeText(SignUp.this, "Name should not contain special characters or numbers.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidMobileNumber(mobileNo)) {
                    Toast.makeText(SignUp.this, "Mobile number should be exactly 11 digits.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isAgeAboveThreshold(dateOfBirth)) {
                    Toast.makeText(SignUp.this, "You must be 18 years old or above to sign up.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    Toast.makeText(SignUp.this, "Password must contain at least 8 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Check if email is already registered
                checkEmailExists(email, new EmailCheckCallback() {
                    @Override
                    public void onCallback(boolean exists) {
                        if (exists) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUp.this, "Email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed with sign-up
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                addUserToFirestore(name, address, mobileNo, email, password, dateOfBirth);
                                            } else {
                                                if (task.getException().getMessage().contains("already in use")) {
                                                    Toast.makeText(SignUp.this, "Email is already registered.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(SignUp.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }

    private boolean isValidName(String name) {
        // Regex pattern to allow only letters and spaces
        String regex = "^[a-zA-Z\\s]*$";
        return name.matches(regex);
    }

    private boolean isAgeAboveThreshold(String dateOfBirth) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date dob = null;
        try {
            dob = dateFormat.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar dobCal = Calendar.getInstance();
        dobCal.setTime(dob);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age >= 18;
    }

    private boolean isValidMobileNumber(String mobileNo) {
        // Regex pattern to match exactly 11 digits
        String regex = "^\\d{11}$";
        return mobileNo.matches(regex);
    }

    private void checkEmailExists(String email, final EmailCheckCallback callback) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean exists = task.getResult().getSignInMethods().size() > 0;
                            callback.onCallback(exists);
                        } else {
                            // Error occurred while checking email existence
                            callback.onCallback(false);
                            Toast.makeText(SignUp.this, "Error checking email existence.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDatePickerDialog() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void addUserToFirestore(String name, String address, String mobileNo, String email, String password, String dateOfBirth) {
        // Calculate age from the selected date of birth
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date dob = dateFormat.parse(dateOfBirth);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            // Create a UserModel object with user details
            UserModel user = new UserModel(name, address, mobileNo, email, password, dateOfBirth, age);
            String userId = mAuth.getCurrentUser().getUid();

            // Store the user details in Firestore
            db.collection("users")
                    .document(userId)
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "Account created.", Toast.LENGTH_SHORT).show();
                                sendEmailVerification();
                                Intent intent = new Intent(SignUp.this, verifyacc.class);
                                startActivity(intent);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Navigate to SignInActivity after 5 seconds
                                        Intent signInIntent = new Intent(SignUp.this, SignIn.class);
                                        startActivity(signInIntent);
                                        finish(); // Finish VerifyAccActivity to prevent going back when pressing back button
                                    }
                                }, 5000); // 5000 milliseconds = 5 seconds

                            } else {
                                Toast.makeText(SignUp.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(SignUp.this, "Error parsing date of birth.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            ((SignUp) getActivity()).setDate(year, month, day);
        }
    }

    private void setDate(int year, int month, int day) {
        dateTextView.setVisibility(View.VISIBLE);
        dateTextView.setText(String.format("%02d/%02d/%d", month + 1, day, year));
    }

    interface EmailCheckCallback {
        void onCallback(boolean exists);
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}