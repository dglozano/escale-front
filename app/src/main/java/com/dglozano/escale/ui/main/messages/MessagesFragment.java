package com.dglozano.escale.ui.main.messages;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.ChatMessage;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class MessagesFragment extends Fragment {

    @BindView(R.id.messages_list)
    MessagesList mMessagesList;

    MessagesListAdapter<MessageImpl> mMessagesListAdapter;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private MessagesViewModel mMessagesViewModel;

    private boolean started = false;
    private boolean visible = false;

    public MessagesFragment() {
        // Required empty public constructor
    }

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerList();
    }

    private void setupRecyclerList() {
        String senderId = mMainActivityViewModel.getLoggedPatient().getValue().getId().toString();
        mMessagesListAdapter = new MessagesListAdapter<>(senderId, null);
        mMessagesList.setAdapter(mMessagesListAdapter);
        mMessagesViewModel.getMessagesOfPatientChat().observe(this, messages -> {
            if (messages != null) {
                mMessagesListAdapter.clear();
                mMessagesListAdapter.addToEnd(messages, false);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        this.visible = isVisibleToUser;

        if(this.started) {
            mMainActivityViewModel.setNumberOfUnreadMessages(0);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.started = false;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        this.started = true;
        mMainActivityViewModel = ViewModelProviders.of((MainActivity) context).get(MainActivityViewModel.class);
        mMainActivityViewModel.setNumberOfUnreadMessages(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mMessagesViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MessagesViewModel.class);
    }
}
