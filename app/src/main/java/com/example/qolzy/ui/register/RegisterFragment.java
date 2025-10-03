package com.example.qolzy.ui.register;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import com.example.qolzy.R;
import com.example.qolzy.data.model.RegisterRequest;
import com.example.qolzy.databinding.FragmentRegisterBinding;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private RegisterViewModel registerViewModel;
    private String email, password;
    private String msg = null;

    public RegisterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);

        registerViewModel.getStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == 201) {
               Bundle bundle = new Bundle();
               bundle.putString("email", email);
               bundle.putString("password", password);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_register_to_login, bundle);
            }

        });

        registerViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                Log.d("msg", msg + "");
            }
        });


        setupEvents();
    }

    private void setupEvents() {
        binding.btnRegister.setOnClickListener(v -> {
            email = binding.edtEmail.getText().toString().trim();
            String firstName = binding.edtFirstName.getText().toString().trim();
            String userName = binding.edtUsername.getText().toString().trim();
            String lastName = binding.edtLastName.getText().toString().trim();
            password = binding.edtPassword.getText().toString();
            String confirmPassword = binding.edtConfirmPassword.getText().toString();


            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(userName)) {
                Toast.makeText(requireContext(), "Không được để trống thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Mật khẩu không trùng khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(isValidUsername(userName)){
                Toast.makeText(requireContext(),"Tên độc nhất không đúng đinh dạng!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Email không đúng định dạng!", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest();
            request.setEmail(email);
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setUserName(userName);
            request.setPassWord(password);

            registerViewModel.createUser(request);
        });

        binding.tvGoToLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_register_to_login);
        });
    }

    public boolean isValidPhoneVN(String phone) {
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone, "VN");
            return phoneUtil.isValidNumber(numberProto);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidUsername(String username) {
        // regex: chỉ a-z, A-Z, 0-9, . và _
        String regex = "^(?=.{3,30}$)(?![._])(?!.*[._]{2})[a-zA-Z0-9._]+(?<![._])$";
        return username.matches(regex);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
