package com.dglozano.escale.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.dglozano.escale.di.annotation.ViewModelKey;
import com.dglozano.escale.ui.common.ChangePasswordActivityViewModel;
import com.dglozano.escale.ui.login.LoginActivityViewModel;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.ui.main.diet.current.CurrentDietViewModel;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivityViewModel;
import com.dglozano.escale.ui.main.diet.old.AllDietsViewModel;
import com.dglozano.escale.ui.main.home.HomeViewModel;
import com.dglozano.escale.ui.main.messages.MessagesViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel.class)
    abstract ViewModel bindMainActivityViewModel(MainActivityViewModel mainActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ShowDietPdfActivityViewModel.class)
    abstract ViewModel bindShowDietPdfAcctivityViewModel(ShowDietPdfActivityViewModel showDietPdfActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel.class)
    abstract ViewModel bindHomeFragmentViewModel(HomeViewModel homeViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AllDietsViewModel.class)
    abstract ViewModel bindAllDietsViewModel(AllDietsViewModel allDietsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MessagesViewModel.class)
    abstract ViewModel bindMessagesViewModel(MessagesViewModel messagesViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CurrentDietViewModel.class)
    abstract ViewModel bindCurrentDietViewModel(CurrentDietViewModel currentDietViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginActivityViewModel.class)
    abstract ViewModel bindLoginActivityViewModel(LoginActivityViewModel loginActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordActivityViewModel.class)
    abstract ViewModel bindChangePasswordActivityViewModel(
            ChangePasswordActivityViewModel changePasswordActivityViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}
