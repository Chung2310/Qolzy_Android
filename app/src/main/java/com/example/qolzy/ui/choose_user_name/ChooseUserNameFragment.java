package com.example.qolzy.ui.choose_user_name;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentChooseUserNameBinding;

public class ChooseUserNameFragment extends Fragment {
    private FragmentChooseUserNameBinding binding;
    private ChooseUserNameViewModel mViewModel;
    private UserRepository userRepository;

    public static ChooseUserNameFragment newInstance() {
        return new ChooseUserNameFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChooseUserNameBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChooseUserNameViewModel.class);

        Long userId =  getArguments().getLong("userId");
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = binding.edtUsername.getText().toString().trim();

                if (userName.isEmpty()) {
                    Toast.makeText(getContext(), "Tên người dùng độc nhất không được để trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isValidUsername(userName)) {
                    binding.tvStatus.setText("✓ Hợp lệ");
                    binding.tvStatus.setTextColor(Color.GREEN); // đổi sang màu dễ thấy
                    mViewModel.saveUserName(userName, (long) userId);
                } else {
                    binding.tvStatus.setText("✗ Không hợp lệ");
                    binding.tvStatus.setTextColor(Color.RED);
                    Toast.makeText(getContext(),"Tên không hợp lệ!",Toast.LENGTH_SHORT).show();
                }
            }
        });


        mViewModel.getStatusLiveData().observe(getViewLifecycleOwner(), status ->{
            if(status == 200){
                Log.d("ChooseUserNameViewModel", "ở đây");
//                NavController navController = NavHostFragment.findNavController(this);
//                navController.navigate(R.id.action_choose_username_to_home);
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }
    private boolean isValidUsername(String username) {
        // regex: chỉ a-z, A-Z, 0-9, . và _
        String regex = "^(?=.{3,30}$)(?![._])(?!.*[._]{2})[a-zA-Z0-9._]+(?<![._])$";
        return username.matches(regex);
    }
}