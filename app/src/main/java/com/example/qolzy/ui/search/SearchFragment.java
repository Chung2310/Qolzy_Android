package com.example.qolzy.ui.search;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.data.model.User;
import com.example.qolzy.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel mViewModel;
    private FragmentSearchBinding binding;
    private List<User> usersSearch = new ArrayList<>();
    private SearchAdapter searchAdapter;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        binding.recyclerSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(usersSearch, getContext());
        binding.recyclerSearch.setAdapter(searchAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mViewModel.searchUser(charSequence.toString());
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvSearch.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mViewModel.getUsersLiveData().observe(getViewLifecycleOwner(), response -> {
            if(response.size() > 0){
                binding.progressBar.setVisibility(View.INVISIBLE);
                searchAdapter.updateUserSearch(response);
            }
            else {
                binding.tvSearch.setText("Không tìm thấy người dùng");
                binding.tvSearch.setVisibility(View.VISIBLE);
            }
        });
    }

}