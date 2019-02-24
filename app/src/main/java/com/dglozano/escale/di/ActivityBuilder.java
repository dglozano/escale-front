package com.dglozano.escale.di;

import com.dglozano.escale.ui.common.ChangePasswordActivity;
import com.dglozano.escale.ui.common.ChangePasswordActivityModule;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.login.LoginActivityModule;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityModule;
import com.dglozano.escale.ui.main.diet.DietFragmentProvider;
import com.dglozano.escale.ui.main.diet.current.CurrentDietProvider;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfActivity;
import com.dglozano.escale.ui.main.diet.show.ShowDietPdfModule;
import com.dglozano.escale.ui.main.diet.old.AllDietsProvider;
import com.dglozano.escale.ui.main.home.HomeFragmentProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = {
            MainActivityModule.class,
            HomeFragmentProvider.class,
            DietFragmentProvider.class,
            AllDietsProvider.class,
            CurrentDietProvider.class
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
            ShowDietPdfModule.class
    })
    public abstract ShowDietPdfActivity bindShowDietPdfActivity();
}