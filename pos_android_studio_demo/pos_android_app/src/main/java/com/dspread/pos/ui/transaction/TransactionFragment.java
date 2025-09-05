package com.dspread.pos.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.ui.transaction.details.TransactionDetailActivity;
import com.dspread.pos.ui.transaction.filter.TransactionFilterActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentTransactionBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;


public class TransactionFragment extends BaseFragment<FragmentTransactionBinding, TransactionViewModel> implements TitleProviderListener {


    @Override
    public String getTitle() {
        return "Transaction";
    }

    @Override
    public int initContentView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        return R.layout.fragment_transaction;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    private PaymentsAdapter adapter;
    private List<Transaction> paymentList;

    private ArrayList<Transaction> cacheArrayList = new ArrayList();
    private boolean showCategorized = false;

    private ActivityResultLauncher<Intent> launcher;
    private static final int FILTER_RECEIVE = 101;


    @Override
    public void initData() {
        super.initData();
        // Setup recycler view
        binding.paymentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewModel.init();
        binding.transactionPb.setVisibility(View.VISIBLE);
        viewModel.transactionList.observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                binding.transactionPb.setVisibility(View.INVISIBLE);
                cacheArrayList.clear();
                cacheArrayList.addAll(transactions);
                handleList(transactions);
            }
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == FILTER_RECEIVE && result.getData() != null) {
                        String filter = result.getData().getStringExtra("filter");
                        // 这里拿到 filter 字符串
                        viewModel.requestTransactionRequest(filter);
                    }
                }
        );

        // Set click listener for View All text
        binding.viewAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategorized = !showCategorized;
                if (showCategorized) {
                    binding.llTransactionHeader.setVisibility(View.GONE);
                } else {
                    binding.llTransactionHeader.setVisibility(View.VISIBLE);
                }
                adapter.setShowCategorized(showCategorized, paymentList);
                binding.viewAllText.setText(showCategorized ? "Show Less" : "View All");
            }
        });

        binding.filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), TransactionFilterActivity.class);
                launcher.launch(intent);
            }
        });

        binding.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
    }


    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            ArrayList<Transaction> transactionsList = new ArrayList<>();
            if (cacheArrayList != null && cacheArrayList.size() > 0) {
                for (int i = 0; i < cacheArrayList.size(); i++) {
                    Transaction transaction = cacheArrayList.get(i);
                    if ((transaction.getId() + "").contains(query)) {
                        transactionsList.add(transaction);
                    }
                }
            }
            handleList(transactionsList);
            transactionsList.clear();
            hideKeyboard();
        } else {
            TRACE.d("搜所:null null ");
            handleList(cacheArrayList);
            hideKeyboard();
        }
    }
    // 修复的隐藏键盘方法
    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && binding.searchEditText != null) {
                imm.hideSoftInputFromWindow(binding.searchEditText.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleList(List<Transaction> transactions) {
        this.paymentList = transactions;
        // Setup adapter\
        if (transactions != null && transactions.size() > 0) {
            double amount = 0;
            for (Transaction transaction : transactions) {
                amount += transaction.getAmount();
            }
            binding.paymentsAmount.setText("$" + amount);

            binding.paymentsCount.setText(transactions.size() + " Payments Today");
        }


        if (transactions != null && transactions.size() < 1) {
            binding.transactionIvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.transactionIvEmpty.setVisibility(View.GONE);
        }

        adapter = new PaymentsAdapter(transactions, new PaymentsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(Transaction transaction) {
                //Toast.makeText(getContext(), "item click:" + transaction.getAmount(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                intent.putExtra("transaction", (Serializable) transaction);
                getActivity().startActivity(intent);
            }
        });
        binding.paymentsRecycler.setAdapter(adapter);
    }
}
