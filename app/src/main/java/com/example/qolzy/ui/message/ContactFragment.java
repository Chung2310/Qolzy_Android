package com.example.qolzy.ui.message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.Contact;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentContactBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactFragment extends Fragment {

    private ContactViewModel mViewModel;
    private ContactAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();
    private FragmentContactBinding binding;
    private UserRepository userRepository;
    private Long userId;
    private int page = 0, size = 10, pageS =0, sizeS = 10;
    private boolean isSearch = false;

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactAdapter(requireContext(),contacts);
        binding.rvContacts.setAdapter(adapter);

        userRepository = new UserRepository(requireContext());
        userId = (long) userRepository.getUserId();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                .setTitle("Qolzy Mess");

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        if (userId != null){
            mViewModel.getContacts(userId, page, size);
        }

        ProgressBar progressBar = binding.progressBar;

        mViewModel.getContactsLiveData().observe(getViewLifecycleOwner(), contactsResponse -> {
            if(contactsResponse.size() != 0 || !isSearch){
                adapter.updateContacts(contactsResponse);
                binding.txtNoData.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
            else if (contactsResponse.size() != 0 || isSearch){
                adapter.updateContactsSearch(contactsResponse);
                binding.txtNoData.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            } else {
                binding.txtNoData.setText("Chưa có liên hệ nào!");
            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //khi người dùng nhấn endter
                mViewModel.getContactsByUserNam(query,pageS,sizeS);
                isSearch = true;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        adapter.setOnContactActionListener(new ContactAdapter.OnContactActionListener() {
            @Override
            public void onClicked(Long contactId) {
                DetailMessageFragment detailMessageFragment = new DetailMessageFragment();
                Bundle args = new Bundle();
                args.putLong("contactId", contactId);
                detailMessageFragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailMessageFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ẩn BottomNavigation khi vào Fragment này
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hiện lại khi rời khỏi Fragment
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(true);
    }

}