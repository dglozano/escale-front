package com.dglozano.escale.di;

import android.app.Application;
import android.app.NotificationManager;
import androidx.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import com.dglozano.escale.db.EscaleDatabase;
import com.dglozano.escale.db.dao.BodyMeasurementDao;
import com.dglozano.escale.db.dao.ChatDao;
import com.dglozano.escale.db.dao.ChatMessageDao;
import com.dglozano.escale.db.dao.DietDao;
import com.dglozano.escale.db.dao.DoctorDao;
import com.dglozano.escale.db.dao.PatientDao;
import com.dglozano.escale.db.dao.UserChatJoinDao;
import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.di.annotation.ApplicationContext;
import com.dglozano.escale.di.annotation.ApplicationScope;
import com.dglozano.escale.di.annotation.BaseUrl;
import com.dglozano.escale.di.annotation.BluetoothInfo;
import com.dglozano.escale.di.annotation.CacheDirectory;
import com.dglozano.escale.di.annotation.DatabaseInfo;
import com.dglozano.escale.di.annotation.RootFileDirectory;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.web.ApiServiceHolder;
import com.dglozano.escale.web.CustomOkHttpAuthenticator;
import com.dglozano.escale.web.DateDeserializer;
import com.dglozano.escale.web.DateSerializer;
import com.dglozano.escale.web.EscaleRestApi;
import com.dglozano.escale.web.HeaderTokenInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.polidea.rxandroidble2.RxBleClient;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module(includes = {
        ViewModelModule.class,
})
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
    PatientDao providePatientDao(EscaleDatabase db) {
        return db.patientDao();
    }

    @Provides
    @ApplicationScope
    BodyMeasurementDao provideBodyMeasurementDao(EscaleDatabase db) {
        return db.bodyMeasurementDao();
    }

    @Provides
    @ApplicationScope
    UserDao provideUserDao(EscaleDatabase db) {
        return db.userDao();
    }

    @Provides
    @ApplicationScope
    ChatDao provideChatDao(EscaleDatabase db) {
        return db.chatDao();
    }

    @Provides
    @ApplicationScope
    ChatMessageDao provideChatMessageDao(EscaleDatabase db) {
        return db.chatMessageDao();
    }

    @Provides
    @ApplicationScope
    DoctorDao provideDoctorDao(EscaleDatabase db) {
        return db.doctorDao();
    }

    @Provides
    @ApplicationScope
    UserChatJoinDao provideUserChatJoinDao(EscaleDatabase db) {
        return db.userChatJoinDao();
    }

    @Provides
    @ApplicationScope
    DietDao provideDietDao(EscaleDatabase db) {
        return db.dietDao();
    }

    @Provides
    @ApplicationScope
    NotificationManager provideNotificationManager(@ApplicationContext Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @DatabaseInfo
    String provideDatabaseName() {
        // TODO: Move to App Constants
        return "escale.db";
    }

    @Provides
    @ApplicationScope
    @RootFileDirectory
    File getRootDirectory(@ApplicationContext Context context) {
        return context.getFilesDir();
    }

    @Provides
    @ApplicationScope
    @CacheDirectory
    File getCacheDirectory(@ApplicationContext Context context) {
        return context.getCacheDir();
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
        return Constants.BASE_LOCALHOST_URL;
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
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
        return gsonBuilder.create();
    }

    @Provides
    @ApplicationScope
    OkHttpClient provideOkhttpClient(HttpLoggingInterceptor httpLoggingInterceptor,
                                     CustomOkHttpAuthenticator customAuthenticator,
                                     HeaderTokenInterceptor headerTokenInterceptor) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(15, TimeUnit.SECONDS);
        client.readTimeout(15, TimeUnit.SECONDS);
        client.writeTimeout(15, TimeUnit.SECONDS);
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
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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

    @Provides
    @ApplicationScope
    public Picasso picasso(@ApplicationContext Context context, OkHttp3Downloader okHttp3Downloader){
        return new Picasso.Builder(context)
                .loggingEnabled(true)
                .indicatorsEnabled(true)
                .downloader(okHttp3Downloader)
                .build();
    }

    @Provides
    @ApplicationScope
    public OkHttp3Downloader okHttp3Downloader(OkHttpClient okHttpClient){
        return new OkHttp3Downloader(okHttpClient);
    }

}