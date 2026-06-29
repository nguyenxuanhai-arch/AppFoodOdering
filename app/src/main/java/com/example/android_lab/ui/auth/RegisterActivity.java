package com.example.android_lab.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_lab.R;
import com.example.android_lab.ui.user.MainActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        ImageView btnRegister = findViewById(R.id.btnRegister);
        TextView btnLoginNow = findViewById(R.id.btnLogin);
        Button btnGoogle = findViewById(R.id.btnRegisterGG);

        btnRegister.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            registerUser(name, email, password);
        });

        btnLoginNow.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        setupGoogleSignIn();
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void registerUser(String name, String email, String password) {
        if (name.isEmpty() || name.length() < 3) {
            editName.setError("Tên không hợp lệ");
            editName.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            editEmail.setError("Email không hợp lệ");
            editEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            editPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        assert firebaseUser != null;
                        String userId = firebaseUser.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", email);
                        userMap.put("name", name);
                        userMap.put("role", "user"); // default role

                        db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                    SharedPreferences prefs = getSharedPreferences("intro_prefs", MODE_PRIVATE);
                                    prefs.edit().putBoolean("isFirstLaunch", false).apply();

                                    startActivity(new Intent(this, MainActivity.class));
                                    finishAffinity(); // Đóng tất cả activity trước đó
                                });
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        String userId = user.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", user.getEmail());
                        userMap.put("name", user.getDisplayName());
                        userMap.put("role", "user"); // default role

                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();

                                SharedPreferences prefs = getSharedPreferences("intro_prefs", MODE_PRIVATE);
                                prefs.edit().putBoolean("isFirstLaunch", false).apply();

                                startActivity(new Intent(this, MainActivity.class));
                                finishAffinity();
                            });
                    } else {
                        Toast.makeText(this, "Đăng nhập Google thất bại: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}