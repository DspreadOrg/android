package com.dspread.pos.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.ui.transaction.details.TransactionDetailActivity;
import com.dspread.pos.ui.transaction.filter.TransactionFilterActivity;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentTransactionBinding;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.goldze.mvvmhabit.utils.SPUtils;

public class TransactionFragment extends BaseFragment<FragmentTransactionBinding, TransactionViewModel> implements TitleProviderListener {
    private String filter = "all";
    private boolean isSmallScreenDevice = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

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

    private final ArrayList<Transaction> cacheArrayList = new ArrayList<>();
    private boolean showCategorized = false;

    private ActivityResultLauncher<Intent> launcher;
    private static final int FILTER_RECEIVE = 101;

    @Override
    public void initData() {
        super.initData();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    viewModel.init();
                }
            }
        };

        // Setup recycler view
        binding.paymentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewModel.init();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        if (widthPx <= 320 && heightPx <= 240) {
            isSmallScreenDevice = true;
            viewModel.isTransactionHeader.set(false);
            viewModel.isTransactionViewAll.set(false);
        }

        viewModel.transactionList.observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                TransactionSorter.sortTransactions(transactions);
                cacheArrayList.clear();
                cacheArrayList.addAll(transactions);
                if (showCategorized) {
                    if (adapter != null) {
                        adapter.refreshAdapter(true, transactions);
                    }
                } else {
                    handleList(transactions);
                }
            }
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == FILTER_RECEIVE && result.getData() != null) {
                        filter = result.getData().getStringExtra("filter");
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
                    viewModel.isTransactionHeader.set(false);
                } else {
                    viewModel.isTransactionHeader.set(true);
                }
                if (adapter != null) {
                    adapter.setShowCategorized(showCategorized, cacheArrayList);
                }
                binding.viewAllText.setText(showCategorized ? "Show Less" : "View All");
            }
        });

        binding.filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
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
        setupSwipeRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        TRACE.d("TransactionFragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Clear all pending refresh tasks
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Handler and Runnable
        handler.removeCallbacksAndMessages(null);
        refreshRunnable = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        TRACE.d("TransactionFragment hidden:" + hidden);
        // Trigger refresh when fragment becomes visible
        if (!hidden) {
            handler.removeCallbacks(refreshRunnable);
            handler.postDelayed(refreshRunnable, 300);
        }
    }


    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light
        );

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (binding.searchEditText.getText().toString().trim().isEmpty()) {
                    String filterType = SPUtils.getInstance().getString("filterType", "all");
                    viewModel.refreshWithFilter(filterType);
                } else {
                    performSearch();
                }
                binding.swipeRefreshLayout.setRefreshing(false);
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
            hideKeyboard();
        } else {
            handleList(cacheArrayList);
            hideKeyboard();
        }
    }

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

        showTransationListUI(transactions);

        adapter = new PaymentsAdapter(transactions, new PaymentsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(Transaction transaction) {
                Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                intent.putExtra("transaction", (Serializable) transaction);
                getActivity().startActivity(intent);
            }
        });
        binding.paymentsRecycler.setAdapter(adapter);
    }

    private void showTransationListUI(List<Transaction> transactions) {
        if (transactions != null && transactions.size() > 0) {
            double amount = 0;
            for (Transaction transaction : transactions) {
                amount += transaction.getAmount();
            }
            String mAmount = DeviceUtils.convertAmountToCents(new BigDecimal(amount).toPlainString());
            binding.paymentsAmount.setText("$" + mAmount);
            if (binding.paymentsAmount.length() > 16) {
                binding.paymentsAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            } else {
                binding.paymentsAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44);
            }

            String filterType = SPUtils.getInstance().getString("filterType", "all");
            switch (filterType) {
                case "1":
                    binding.paymentsCount.setText(transactions.size() + " Payments Today");
                    break;
                case "3":
                    binding.paymentsCount.setText(transactions.size() + " Payments 3Days");
                    break;
                case "all":
                default:
                    binding.paymentsCount.setText(transactions.size() + " Payments All");
                    break;
            }
            //int todayCount = TransactionDateFilter.getTodayTransactions(transactions).size();
            //binding.paymentsCount.setText(todayCount + " Payments Today");
        }

        if (transactions == null || transactions.size() < 1) {
            viewModel.isEmpty.set(true);
            viewModel.isTransactionHeader.set(false);
            viewModel.isTransactionViewAll.set(false);
        } else {
            viewModel.isEmpty.set(false);
            if (!isSmallScreenDevice) {
                viewModel.isTransactionHeader.set(true);
                viewModel.isTransactionViewAll.set(true);
            }
        }
    }
}