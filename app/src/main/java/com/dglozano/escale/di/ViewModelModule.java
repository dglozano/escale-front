package com.dglozano.escale.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dglozano.escale.di.annotation.ViewModelKey;
import com.dglozano.escale.ui.common.pw_change.ChangePasswordActivityViewModel;
import com.dglozano.escale.ui.common.pw_recovery.RecoverPasswordActivityViewModel;
import com.dglozano.escale.ui.drawer.profile.PatientProfileActivityViewModel;
import com.dglozano.escale.ui.login.LoginActivityViewModel;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.ui.main.diet.DietViewModel;
import com.dglozano.escale.ui.main.diet.all.AllDietsViewModel;
import com.dglozano.escale.ui.main.diet.current.CurrentDietViewModel;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivityViewModel;
import com.dglozano.escale.ui.main.home.AddMeasurementViewModel;
import com.dglozano.escale.ui.main.home.HomeViewModel;
import com.dglozano.escale.ui.main.messages.MessagesViewModel;
import com.dglozano.escale.ui.main.stats.StatsViewModel;
import com.dglozano.escale.ui.main.stats.chart.StatsChartViewModel;
import com.dglozano.escale.ui.main.stats.list.StatsListViewModel;

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
    @ViewModelKey(AddMeasurementViewModel.class)
    abstract ViewModel bindAddMeasurementViewModel(AddMeasurementViewModel addMeasurementViewModel);

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
    @ViewModelKey(StatsChartViewModel.class)
    abstract ViewModel bindStatsChartViewModel(StatsChartViewModel statsChartViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel.class)
    abstract ViewModel bindStatsViewModel(StatsViewModel statsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsListViewModel.class)
    abstract ViewModel bindStatsListViewModel(StatsListViewModel statsListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CurrentDietViewModel.class)
    abstract ViewModel bindCurrentDietViewModel(CurrentDietViewModel currentDietViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DietViewModel.class)
    abstract ViewModel bindDietViewModel(DietViewModel dietViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginActivityViewModel.class)
    abstract ViewModel bindLoginActivityViewModel(LoginActivityViewModel loginActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PatientProfileActivityViewModel.class)
    abstract ViewModel bindProfileActivityViewModel(PatientProfileActivityViewModel profileActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordActivityViewModel.class)
    abstract ViewModel bindChangePasswordActivityViewModel(
            ChangePasswordActivityViewModel changePasswordActivityViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RecoverPasswordActivityViewModel.class)
    abstract ViewModel bindRecoverPasswordActivityViewModel(
            RecoverPasswordActivityViewModel recoverPasswordActivityViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);
}
