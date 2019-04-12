package com.dglozano.escale.di;

import com.dglozano.escale.ui.common.pw_change.ChangePasswordActivity;
import com.dglozano.escale.ui.common.pw_change.ChangePasswordActivityModule;
import com.dglozano.escale.ui.common.pw_recovery.RecoverPasswordActivity;
import com.dglozano.escale.ui.common.pw_recovery.RecoverPasswordActivityModule;
import com.dglozano.escale.ui.doctor.main.AddPatientActivity;
import com.dglozano.escale.ui.doctor.main.AddPatientModule;
import com.dglozano.escale.ui.doctor.main.DoctorMainActivity;
import com.dglozano.escale.ui.doctor.main.DoctorMainActivityModule;
import com.dglozano.escale.ui.drawer.profile.PatientProfileActivity;
import com.dglozano.escale.ui.drawer.profile.PatientProfileActivityModule;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.login.LoginActivityModule;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityModule;
import com.dglozano.escale.ui.main.diet.DietFragmentProvider;
import com.dglozano.escale.ui.main.diet.all.AllDietsProvider;
import com.dglozano.escale.ui.main.diet.current.CurrentDietProvider;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivity;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfModule;
import com.dglozano.escale.ui.main.home.AddMeasurementActivity;
import com.dglozano.escale.ui.main.home.AddMeasurementModule;
import com.dglozano.escale.ui.main.home.HomeFragmentProvider;
import com.dglozano.escale.ui.main.messages.MessagesProvider;
import com.dglozano.escale.ui.main.stats.StatsFragmentProvider;
import com.dglozano.escale.ui.main.stats.chart.StatsChartProvider;
import com.dglozano.escale.ui.main.stats.list.StatsListProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {
            MainActivityModule.class,
            HomeFragmentProvider.class,
            StatsFragmentProvider.class,
            StatsChartProvider.class,
            StatsListProvider.class,
            DietFragmentProvider.class,
            AllDietsProvider.class,
            CurrentDietProvider.class,
            MessagesProvider.class
    })
    public abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector(modules = {
            LoginActivityModule.class
    })
    public abstract LoginActivity bindLoginActivity();

    @ContributesAndroidInjector(modules = {
            ChangePasswordActivityModule.class
    })
    public abstract ChangePasswordActivity bindChangePasswordActivity();

    @ContributesAndroidInjector(modules = {
            RecoverPasswordActivityModule.class
    })
    public abstract RecoverPasswordActivity bindRecoverPasswordActivity();

    @ContributesAndroidInjector(modules = {
            ShowDietPdfModule.class
    })
    public abstract ShowDietPdfActivity bindShowDietPdfActivity();

    @ContributesAndroidInjector(modules = {
            AddMeasurementModule.class
    })
    public abstract AddMeasurementActivity bindShowAddMeasurementActivity();

    @ContributesAndroidInjector(modules = {
            PatientProfileActivityModule.class
    })
    public abstract PatientProfileActivity bindPatientProfileActivity();

    @ContributesAndroidInjector(modules = {
            DoctorMainActivityModule.class
    })
    public abstract DoctorMainActivity bindDoctorMainActivity();

    @ContributesAndroidInjector(modules = {
            AddPatientModule.class
    })
    public abstract AddPatientActivity bindAddPatientActivity();
}