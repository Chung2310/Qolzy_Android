package com.example.qolzy.ui.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.LoginRequestFirebase;
import com.example.qolzy.databinding.FragmentLoginBinding;
import com.example.qolzy.data.model.LoginRequest;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private UserRepository userRepository;

    private String email, password;

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public LoginFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        FirebaseApp.initializeApp(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // lấy từ google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext() ,gso);

        if(getArguments() != null){
            email = getArguments().getString("email");
            password = getArguments().getString("password");
            binding.edtEmail.setText(email);
            binding.edtPassword.setText(password);
        }

        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(LoginViewModel.class);

        userRepository = new UserRepository(requireContext());

        // Lắng nghe message
        viewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Lắng nghe user login
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                return;
            }
            else if (user.getUserName() == null){

                Bundle bundle = new Bundle();
                bundle.putLong("userId", user.getId());
                binding.progressBar.setVisibility(View.GONE);
                userRepository.saveUser(user);

                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.action_login_to_choose_username, bundle);

            } else {
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                Log.d("LoginFragment", "User ID: " + user.getId());
                userRepository.saveUser(user);
                String role = getRoleFromToken(user.getToken());

                if (Objects.equals(role, "ROLE_ADMIN")) {
                    // startActivity(new Intent(requireContext(), AdminActivity.class));
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(requireContext(), MainActivity.class));
                }

                requireActivity().finish(); // chỉ finish khi chuyển sang Activity khác
            }

        });

        // Sự kiện nút Login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String pass = binding.edtPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Email và mật khẩu không được để trống!", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.login(email, pass);
            }
        });

        // Chuyển sang màn hình đăng ký
//        binding.tvRegister.setOnClickListener(v -> {
//            NavController navController = NavHostFragment.findNavController(this);
//            navController.navigate(R.id.action_login_to_register);
//        });

        // Nhận dữ liệu login từ RegisterFragment (nếu có)
        if (getArguments() != null) {
            LoginRequest loginRequest = (LoginRequest) getArguments().getSerializable("login");
            if (loginRequest != null) {
                binding.edtEmail.setText(loginRequest.getEmail());
                binding.edtPassword.setText(loginRequest.getPassword());
            }
        }

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                signIn();
            }
        });
    }

    private String getRoleFromToken(String token) {
        try {
            byte[] secretKeyBytes = Utils.SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKey key = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object roleClaim = claims.get("role");
            if (roleClaim == null) {
                roleClaim = claims.get("roles");
            }
            return roleClaim != null ? roleClaim.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    Toast.makeText(getContext(), "Google SignIn thất bại!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Đăng nhập Firebase bằng Google credential
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(requireActivity(), firebaseTask -> {
                            if (firebaseTask.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // ✅ Lấy Firebase ID Token để gửi lên server
                                    user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();

                                            Log.d("FirebaseAuth", "Firebase ID Token: " + firebaseIdToken);

                                            // Gửi token này lên server Spring Boot
                                            viewModel.loginOrRegisterWithFirebase(
                                                    new LoginRequestFirebase(firebaseIdToken)
                                            );

                                        } else {
                                            Toast.makeText(getContext(), "Không lấy được Firebase ID Token", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Log.w(TAG, "signInWithCredential:failure", firebaseTask.getException());
                                Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getContext(), "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getContext(), "Welcome " + (user != null ? user.getEmail() : ""), Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(requireContext(), MainActivity.class));
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
