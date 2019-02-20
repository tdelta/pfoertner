package de.tu_darmstadt.epool.pfoertneradmin.di;

import android.arch.lifecycle.ViewModel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.Binds;
import dagger.MapKey;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodel.MemberProfileViewModel;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapKey
@interface ViewModelKey {
    Class<? extends ViewModel> value();
}

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MemberProfileViewModel.class)
    abstract ViewModel bindUserViewModel(MemberProfileViewModel userViewModel);
}
