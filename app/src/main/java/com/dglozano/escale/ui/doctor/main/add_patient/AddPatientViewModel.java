package com.dglozano.escale.ui.doctor.main.add_patient;

import com.dglozano.escale.R;
import com.dglozano.escale.repository.DoctorRepository;
import com.dglozano.escale.util.ui.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dglozano.escale.util.ValidationHelper.isValidBirthday;
import static com.dglozano.escale.util.ValidationHelper.isValidEmail;
import static com.dglozano.escale.util.ValidationHelper.isValidGenre;
import static com.dglozano.escale.util.ValidationHelper.isValidHeight;
import static com.dglozano.escale.util.ValidationHelper.isValidName;
import static com.dglozano.escale.util.ValidationHelper.isValidPhysicalActivity;

public class AddPatientViewModel extends ViewModel {

    private final DoctorRepository mDoctorRepository;
    private final CompositeDisposable disposables;
    private final MutableLiveData<Event<Integer>> mErrorEvent;
    private final MutableLiveData<Event<Boolean>> mSuccessEvent;
    private final MutableLiveData<Boolean> mLoading;

    @Inject
    public AddPatientViewModel(DoctorRepository doctorRepository) {
        mDoctorRepository = doctorRepository;
        mLoading = new MutableLiveData<>();
        mErrorEvent = new MutableLiveData<>();
        mSuccessEvent = new MutableLiveData<>();
        disposables = new CompositeDisposable();
        mLoading.postValue(false);
    }

    private boolean isInputValid(CharSequence firstName,
                                 CharSequence lastName,
                                 CharSequence email,
                                 CharSequence birthday,
                                 CharSequence heightInCm,
                                 int genre,
                                 int phActivity) {
        return isValidName(firstName)
                && isValidName(lastName)
                && isValidEmail(email)
                && isValidBirthday(birthday)
                && isValidHeight(heightInCm)
                && isValidGenre(genre)
                && isValidPhysicalActivity(phActivity);
    }

    public LiveData<Boolean> getLoading() {
        return mLoading;
    }

    public LiveData<Event<Integer>> getErrorEvent() {
        return mErrorEvent;
    }

    public LiveData<Event<Boolean>> getSuccessEvent() {
        return mSuccessEvent;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }

    public void hitAddPatient(CharSequence firstName,
                              CharSequence lastName,
                              CharSequence email,
                              CharSequence birthdayString,
                              CharSequence heightInCmString,
                              int genre,
                              int phActivity) {
        if (isInputValid(firstName, lastName, email, birthdayString, heightInCmString, genre, phActivity)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthday = sdf.parse(birthdayString.toString());
                int heightInCm = Integer.parseInt(heightInCmString.toString());
                disposables.add(mDoctorRepository
                        .addPatient(
                                firstName.toString(),
                                lastName.toString(),
                                email.toString(),
                                birthday,
                                heightInCm,
                                genre,
                                phActivity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe((d) -> mLoading.postValue(true))
                        .subscribe(
                                () -> {
                                    Timber.d("Patient %s, %s added", lastName.toString().toUpperCase(), firstName.toString());
                                    mSuccessEvent.postValue(new Event<>(true));
                                    mLoading.postValue(false);
                                },
                                error -> {
                                    Timber.e(error);
                                    mLoading.postValue(false);
                                    mErrorEvent.postValue(new Event<>(R.string.add_patient_error_snackbar));
                                }
                        )
                );
            } catch (ParseException e) {
                mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
            }
        } else {
            mErrorEvent.postValue(new Event<>(R.string.input_validation_error_snackbar));
        }

    }
}
