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
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.data.repository.UserSearchRepository;
import com.example.qolzy.databinding.FragmentSearchBinding;
import com.example.qolzy.ui.account.AccountFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel mViewModel;
    private FragmentSearchBinding binding;
    private List<User> usersSearch = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private Long userId;
    private UserRepository userRepository;
    private UserSearchRepository userSearchRepository;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        userRepository = new UserRepository(getContext());
        userSearchRepository = new UserSearchRepository(getContext());

        userId = (long) userRepository.getUserId();

        binding.recyclerSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(usersSearch, getContext());
        binding.recyclerSearch.setAdapter(searchAdapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        searchAdapter.updateUserSearch(userSearchRepository.getHistory());

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mViewModel.searchUser(userId ,charSequence.toString());
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

        searchAdapter.setOnSearchActionListener(new SearchAdapter.OnSearchUserActionListener() {
            @Override
            public void onClicked(User user, Boolean followByCurrentUser) {
                userSearchRepository.addUser(user);
                openAccountFragment(user, followByCurrentUser);
            }
        });
    }

    public void openAccountFragment(User user, Boolean followByCurrentUser){
        AccountFragment fragment = new AccountFragment();

        // truyền userId qua Bundle
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        args.putBoolean("followByCurrentUser", followByCurrentUser);
        fragment.setArguments(args);

        // mở fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // fragment_container là id FrameLayout chứa fragment
                .addToBackStack(null)
                .commit();

    }
}