package com.dglozano.escale.ui.drawer.profile;

import android.graphics.Bitmap;
import android.view.animation.AlphaAnimation;

import com.dglozano.escale.R;
import com.yalantis.ucrop.UCrop;

import dagger.Module;
import dagger.Provides;

@Module
public class PatientProfileActivityModule {

    @Provides
    UCrop.Options uCropOptions(PatientProfileActivity profileActivity) {
        UCrop.Options options = new UCrop.Options();
        int colorPrimaryLight = profileActivity.getColor(R.color.colorPrimaryLight);
        int almostWhite = profileActivity.getColor(R.color.almostWhite);
        int colorPrimaryDarkTransparent = profileActivity.getColor(R.color.colorPrimaryDarkTransparent);
        int white = profileActivity.getColor(R.color.white);
        String title = profileActivity.getString(R.string.ucrop_activity_title);

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setToolbarColor(colorPrimaryLight);
        options.setToolbarTitle(title);
        options.setToolbarWidgetColor(white);
        options.setLogoColor(white);
        options.setActiveWidgetColor(colorPrimaryLight);
        options.setRootViewBackgroundColor(almostWhite);
        options.setCircleDimmedLayer(true);
        options.setStatusBarColor(colorPrimaryDarkTransparent);

        return options;
    }

    @Provides
    AlphaAnimation fadeAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(350);
        return alphaAnimation;
    }
}
