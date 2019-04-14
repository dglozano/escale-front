package com.dglozano.escale.ui.main.messages;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class MessagesFragment extends Fragment {

    @BindView(R.id.messages_list)
    MessagesList mMessagesList;
    @BindView(R.id.messages_input)
    MessageInput mMessageInput;
    @BindView(R.id.messages_progress_bar)
    ProgressBar mProgressBarSend;

    MessagesListAdapter<MessageImpl> mMessagesListAdapter;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private MainActivityViewModel mMainActivityViewModel;
    private MessagesViewModel mMessagesViewModel;

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
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupMessagesList();
        setupSendListener();
        setupSendingStatusObserver();
        setupErrorEventObserver();
    }

    private void setupErrorEventObserver() {
        mMessagesViewModel.getErrorEvent().observe(this, errorEvent -> {
            if (errorEvent != null && errorEvent.peekContent() != null && !errorEvent.hasBeenHandled())
                Toast.makeText(getActivity(), errorEvent.handleContent(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSendingStatusObserver() {
        mMessagesViewModel.observeSendingStatus().observe(this, isSending -> {
            if (isSending != null) {
                mProgressBarSend.setVisibility(isSending ? View.VISIBLE : View.GONE);
            } else {
                mProgressBarSend.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.messages_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.menu_messages_copy);
        item.setVisible(mMessagesViewModel.isCopyMenuVisible());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_messages_copy) {
            mMessagesListAdapter.copySelectedMessagesText(getActivity(), message -> {
                String createdAt = new SimpleDateFormat("d/M/yy H:mm", Locale.getDefault())
                        .format(message.getCreatedAt());

                return String.format(Locale.getDefault(), "[%s] %s: %s ",
                        createdAt, message.getUser().getName(), message.getText());
            }, false);
            Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
        } else if (id == android.R.id.home) {
            Timber.d("back pressed");
            getActivity().onBackPressed();
        }
        return true;
    }

    private void setupSendListener() {
        mMessageInput.setInputListener(input -> {
            //validate and send message
            if (!mMainActivityViewModel.isDoctorView()) {
                mMessagesViewModel.sendMessageAsPatient(input.toString());
            } else {
                mMessagesViewModel.sendMessageAsDoctor(input.toString());
            }
            return true;
        });
    }

    private void setupMessagesList() {
        String senderId;

        if (!mMainActivityViewModel.isDoctorView()) {
            senderId = mMainActivityViewModel.getLoggedPatientId().toString();
        } else {
            senderId = mMainActivityViewModel.getLoggedDoctorId().toString();
        }
        mMessagesListAdapter = new MessagesListAdapter<>(senderId, null);
        mMessagesListAdapter.enableSelectionMode(count -> {
            mMessagesViewModel.setCopyMenuVisible(count > 0);
            getActivity().invalidateOptionsMenu();
        });
        mMessagesList.setAdapter(mMessagesListAdapter);
        mMessagesViewModel.getMessagesOfPatientChat().observe(this, messages -> {
            if (messages != null) {
                mMessagesListAdapter.clear();
                mMessagesListAdapter.addToEnd(messages, false);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mMessagesListAdapter.unselectAllItems();
        mMessagesViewModel.setCopyMenuVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
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
        mMessagesViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MessagesViewModel.class);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }
}
