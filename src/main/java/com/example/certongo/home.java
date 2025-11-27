    package com.example.certongo;

    import static android.content.ContentValues.TAG;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.ListView;
    import android.widget.Spinner;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.DocumentChange;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.Comparator;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

    public class home extends AppCompatActivity {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ListView listView;
        List<Document> documentList = new ArrayList<>();
        List<Document> originalDocumentList = new ArrayList<>();
        ArrayAdapter<Document> adapter;
        Spinner spinner;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            bottomNav.setSelectedItemId(R.id.action_home);
            Button addRep = findViewById(R.id.addrep);
            listView = findViewById(R.id.myListView);
            spinner = findViewById(R.id.spinner2);
            String[] options = {"All", "Brgy Certificate", "Brgy.ID", "Cedula", "Business Permit", "Brgy Indigency", "Clearance"};
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
            spinner.setAdapter(spinnerAdapter);

            addRep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(home.this, crerep.class);
                    startActivity(intent);
                }
            });

            adapter = new ArrayAdapter<Document>(home.this, 0, documentList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                    }

                    TextView nameTextView = convertView.findViewById(R.id.doc_name);
                    TextView typeTextView = convertView.findViewById(R.id.doc_type);
                    TextView purposeTextView = convertView.findViewById(R.id.doc_purpose);
                    TextView statusTextView = convertView.findViewById(R.id.doc_status);

                    Document document = getItem(position);

                    if (document != null) {
                        nameTextView.setText("Name: " + document.getName());
                        typeTextView.setText("Type of Document: " + document.getType_of_document());
                        purposeTextView.setText("Purpose of Request: " + document.getPurpose_of_request());
                        statusTextView.setText("Status: " + document.getStatus());
                    }

                    return convertView;
                }
            };

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the clicked document
                    Document clickedDocument = documentList.get(position);

                    String documentId = clickedDocument.getDocumentId();

                    // Create an intent to start the Infos activity
                    Intent intent = new Intent(home.this, Infos.class);

                    // Pass data to Infos activity using intent extras
                    intent.putExtra("name", clickedDocument.getName());
                    intent.putExtra("type_of_document", clickedDocument.getType_of_document());
                    intent.putExtra("purpose_of_request", clickedDocument.getPurpose_of_request());
                    intent.putExtra("date", clickedDocument.getDate());
                    intent.putExtra("status", clickedDocument.getStatus());
                    intent.putExtra("reqfile", clickedDocument.getReqfile());
                    intent.putExtra("feedback", clickedDocument.getFeedback());
                    intent.putExtra("documentId", documentId);
                    // Pass the document ID to Infos activity


                    // Start the Infos activity
                    startActivity(intent);
                }
            });

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                String userID = currentUser.getUid();

                db.collection("users").document(userID).collection("docus")
                        .orderBy("submit_date", Query.Direction.DESCENDING)
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (e != null) {
                                Log.d(TAG, "Error: " + e.getMessage());
                                return;
                            }

                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                    Document document = dc.getDocument().toObject(Document.class);
                                    String id = dc.getDocument().getId();
                                    document.setDocumentId(id); // Set the document ID

                                    switch (dc.getType()) {
                                        case ADDED:
                                            originalDocumentList.add(document);
                                            break;

                                        case MODIFIED:
                                            originalDocumentList.removeIf(d -> d.getDocumentId().equals(id)); // Remove the old document
                                            originalDocumentList.add(document); // Add the updated document
                                            break;

                                        case REMOVED:
                                            originalDocumentList.removeIf(d -> d.getDocumentId().equals(id)); // Remove the document
                                            break;
                                    }
                                }

                                applyFilter();
                                adapter.notifyDataSetChanged();
                            }
                        });

            }


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    applyFilter();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing
                }
            });
        }

        private void applyFilter() {
            String selectedType = spinner.getSelectedItem().toString();
            if (selectedType.equals("All")) {
                // Show all documents arranged by latest submit date
                documentList.clear();
                documentList.addAll(originalDocumentList);
                Collections.sort(documentList, (d1, d2) -> compareDates(d1.getSubmitDate(), d2.getSubmitDate()));
            } else {
                // Show documents of the selected type arranged by latest submit date
                List<Document> filteredList = new ArrayList<>();
                for (Document document : originalDocumentList) {
                    if (document.getType_of_document().equals(selectedType)) {
                        filteredList.add(document);
                    }
                }
                Collections.sort(filteredList, (d1, d2) -> compareDates(d1.getSubmitDate(), d2.getSubmitDate()));
                documentList.clear();
                documentList.addAll(filteredList);
            }
            adapter.notifyDataSetChanged();
        }

        private int compareDates(Timestamp t1, Timestamp t2) {
            if (t1 == null || t2 == null) {
                return 0;
            }
            // Reverse the order for descending sort
            return -t1.compareTo(t2);
        }


        private int compareDates(String date1, String date2) {
            if (date1 == null && date2 == null) {
                return 0;
            } else if (date1 == null) {
                return 1;
            } else if (date2 == null) {
                return -1;
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date d1 = format.parse(date1);
                Date d2 = format.parse(date2);
                return d2.compareTo(d1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public BottomNavigationView.OnNavigationItemSelectedListener navListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                break;
                            case R.id.action_request:
                                Intent intentReq = new Intent(home.this, crerep.class);
                                startActivity(intentReq);
                                break;
                            case R.id.action_profile:
                                Intent intentProfile = new Intent(home.this, profile.class);
                                startActivity(intentProfile);
                                break;
                        }
                        return true;
                    }
                };
    }
