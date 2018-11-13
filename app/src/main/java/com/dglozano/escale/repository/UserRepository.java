package com.dglozano.escale.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.dglozano.escale.db.dao.UserDao;
import com.dglozano.escale.db.entity.User;
import com.dglozano.escale.di.annotation.ApplicationScope;

import java.util.List;

import javax.inject.Inject;

@ApplicationScope
public class UserRepository {

    private UserDao mUserDao;

    @Inject
    public UserRepository(UserDao userDao) {
        mUserDao = userDao;
    }

    public LiveData<List<User>> getAllUsers() {
        return mUserDao.getAllUsers();
    }

    public LiveData<User> getUserById(int userId) {
        return mUserDao.getUserById(userId);
    }

    public void insert(User user) {
        new UserRepository.insertAsyncTask(mUserDao).execute(user);
    }

    public void deleteAll() {
        new UserRepository.deleteAsyncTask(mUserDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<User, Void, Void> {

        private UserDao mAsyncTaskDao;

        insertAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... params) {
            mAsyncTaskDao.insertUser(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private UserDao mAsyncTaskDao;

        deleteAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}
