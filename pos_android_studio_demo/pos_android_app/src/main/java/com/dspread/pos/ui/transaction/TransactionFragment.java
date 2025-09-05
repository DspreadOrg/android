package com.dspread.pos.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.ui.transaction.details.TransactionDetailActivity;
import com.dspread.pos.ui.transaction.filter.TransactionFilterActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentTransactionBinding;

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
    private boolean showCategorized = false;

    private ActivityResultLauncher<Intent> launcher;
    private static final int FILTER_RECEIVE = 101;


    @Override
    public void initData() {
        super.initData();
        // Setup recycler view
        binding.paymentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        viewModel.init();
        viewModel.transactionList.observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(List<Transaction> transactions) {
                handleList(transactions);
            }
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == FILTER_RECEIVE && result.getData() != null) {
                        String filter = result.getData().getStringExtra("filter");
                        // 这里拿到 filter 字符串
                        TRACE.d("filter:"+filter);
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
    }

    private void handleList(List<Transaction> transactions) {
        this.paymentList = transactions;
        // Setup adapter
        adapter = new PaymentsAdapter(transactions, new PaymentsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(Transaction transaction) {
                Toast.makeText(getContext(), "item click:" + transaction.getAmount(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                getActivity().startActivity(intent);
            }
        });
        binding.paymentsRecycler.setAdapter(adapter);

    }
}
