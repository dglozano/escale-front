package com.dglozano.escale.ui.main.messages;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MessagesProvider {

    @ContributesAndroidInjector(modules = MessagesModule.class)
    public abstract MessagesFragment provideMessagesFragmentFactory();
}
