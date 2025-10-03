package com.example.qolzy.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.RefreshTokenRequest;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentSplashBinding;
import com.example.qolzy.util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;
    private SharedPreferences securePrefs;
    private SplashViewModel viewModel;

    private UserRepository userRepository;
    private String nextRole = null;
    private boolean goLogin = false;

    public SplashFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(SplashViewModel.class);

        try {
            MasterKey masterKey = new MasterKey.Builder(requireContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    requireContext(),
                    "TokenAuth",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            Log.e("Splash", "EncryptedSharedPreferences lỗi", e);
            requireContext().getSharedPreferences("TokenAuth", Context.MODE_PRIVATE).edit().clear().apply();
        }

        // 👉 Luôn hiển thị animation
        setupAnimation();

        // 👉 Xử lý token song song
        handleTokenCheck();

        return binding.getRoot();
    }

    private void setupAnimation() {
        binding.animationView.setAnimation(R.raw.loadanimation);
        binding.animationView.playAnimation();
        binding.animationView.loop(true);

        binding.splashtext.setText("QOLZY");
        binding.splashtext.setAlpha(0f);
        binding.splashtext.animate()
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(500)
                .start();
    }

    private void handleTokenCheck() {
        userRepository = new UserRepository(getContext());
        String accessToken = userRepository.getAccessToken();
        String refreshToken = userRepository.getRefreshToken();

        Log.d("Splash", "AccessToken: "+ accessToken);
        Log.d("Splash", "RefreshToken: "+ refreshToken);

        boolean isAccessTokenValid = accessToken != null && isTokenValid(accessToken);
        boolean isRefreshTokenValid = refreshToken != null && isTokenValid(refreshToken);

        if (isAccessTokenValid) {
            nextRole = getRoleFromToken(accessToken);
            Log.d("Splash", " Access token còn hạn");
        } else if (isRefreshTokenValid) {
            String roleFromRefresh = getRoleFromToken(refreshToken);
            Log.d("Splash", "Access token hết hạn, dùng refresh token để lấy mới");
            refreshAccessToken(refreshToken, accessToken, roleFromRefresh);
        } else {
            Log.d("Splash", " Cả accessToken và refreshToken hết hạn");
            goLogin = true;
        }

        // 👉 Delay 3s mới chuyển màn
        new Handler().postDelayed(() -> {
            if (goLogin) {
                goToLogin();
            } else {
                goNext(nextRole);
            }
        }, 3000);
    }

    private boolean isTokenValid(String token) {
        try {
            byte[] secretKeyBytes = Utils.SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKey key = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return true;
        } catch (Exception e) {
            return false;
        }
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

    private void refreshAccessToken(String refreshToken, String accessToken, String roleFromRefresh) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        request.setAccessToken(accessToken);

        viewModel.refreshToken(request);

        viewModel.getRefreshTokenRequestMutableLiveData().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                String newAccessToken = response.getAccessToken();
                nextRole = getRoleFromToken(newAccessToken);
            } else {
                goLogin = true;
            }
        });
    }

    private void goNext(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            // TODO: Điều hướng đến AdminFragment/Activity
            // NavHostFragment.findNavController(this).navigate(R.id.action_splashFragment_to_adminFragment);
            startMainActivity(); // tạm thời vẫn vào main
        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(requireContext(), MainActivity.class));
        requireActivity().finish();
    }

    private void goToLogin() {
        securePrefs.edit()
                .remove("token")
                .remove("refreshToken")
                .apply();

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_splash_to_login);
    }

    private void showError(String message) {
        binding.animationView.setAnimation(R.raw.erroranimation);
        binding.animationView.playAnimation();
        binding.animationView.loop(false);

        binding.splashtext.setText(message);
        binding.splashtext.animate()
                .alpha(1f)
                .setDuration(1500)
                .start();

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        if (binding.animationView != null) {
            binding.animationView.cancelAnimation();
        }
        super.onDestroyView();
        binding = null;
    }
}
