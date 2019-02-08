package com.dglozano.escale.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.di.annotation.ApplicationContext;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BaseUrl;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.di.annotation.DatabaseInfo;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.web.ApiServiceHolder;
import com.dglozano.escale.web.CustomOkHttpAuthenticator;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.HeaderTokenInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.polidea.rxandroidble2.RxBleClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = ViewModelModule.class)
public class AppModule {

    @Provides
    @ApplicationContext
    @ApplicationScope
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @ApplicationScope
    EscaleDatabase provideAppDatabase(@DatabaseInfo String dbName, @ApplicationContext Context context) {
        return Room.databaseBuilder(context, EscaleDatabase.class, dbName)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @ApplicationScope
    PatientDao provideUserDao(EscaleDatabase db) {
        return db.userDao();
    }

    @Provides
    @ApplicationScope
    BodyMeasurementDao provideBodyMeasurementDao(EscaleDatabase db) {
        return db.bodyMeasurementDao();
    }

    @Provides
    @DatabaseInfo
    String provideDatabaseName() {
        // TODO: Move to App Constants
        return "escale.db";
    }

    @Provides
    @ApplicationScope
    RxBleClient providesRxBleClient(@ApplicationContext Context context) {
        return RxBleClient.create(context);
    }

    @Provides
    @BluetoothInfo
    String provideScaleName() {
        return Constants.BF600;
    }

    @Provides
    @BaseUrl
    String provideBaseUrl() {
        return Constants.BASE_HEROKU_URL;
    }

    @Provides
    SimpleDateFormat provideSimpleDateFormat() {
        return new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT);
    }

    @Provides
    DecimalFormat provideDecimalFormat() {
        return new DecimalFormat(Constants.DECIMAL_FORMAT);
    }

    @Provides
    @ApplicationScope
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create();
    }

    @Provides
    @ApplicationScope
    OkHttpClient provideOkhttpClient(HttpLoggingInterceptor httpLoggingInterceptor,
                                     CustomOkHttpAuthenticator customAuthenticator,
                                     HeaderTokenInterceptor headerTokenInterceptor) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(httpLoggingInterceptor);
        client.addNetworkInterceptor(headerTokenInterceptor);
        client.authenticator(customAuthenticator);
        return client.build();
    }

    @Provides
    @ApplicationScope
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    @Provides
    @ApplicationScope
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient, @BaseUrl String baseUrl) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @ApplicationScope
    EscaleRestApi provideEscaleApi(Retrofit retrofit, ApiServiceHolder apiServiceHolder) {
        EscaleRestApi escaleRestApi = retrofit.create(EscaleRestApi.class);
        apiServiceHolder.setApiService(escaleRestApi);
        return escaleRestApi;
    }

    @Provides
    @ApplicationScope
    SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences("escalePref", Context.MODE_PRIVATE);
    }

    @Provides
    @ApplicationScope
    ApiServiceHolder provideApiServiceHolder() {
        return new ApiServiceHolder();
    }
}