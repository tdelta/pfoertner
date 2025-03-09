package de.tu_darmstadt.epool.pfoertnerpanel.viewmodels;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.TimeslotRepository;

public class ScheduleAppointmentViewModel extends AndroidViewModel {

    public class DayItem {
        public final LocalDate date;
        public final boolean hasTimeslots;
        public final boolean isSelected;

        public DayItem(LocalDate date, boolean hasTimeslots, boolean isSelected){
            this.date = date;
            this.hasTimeslots = hasTimeslots;
            this.isSelected = isSelected;
        }
    }
    public final TimeslotRepository repo;
    public final MutableLiveData<LocalDate> selectedDay = new MutableLiveData<>();

    // Number of days in the future to display
    private final int nDays = 28;

    public ScheduleAppointmentViewModel(@NonNull Application app) {
        super(app);

        repo = PfoertnerApplication.get(app.getApplicationContext()).getRepo().getTimeslotRepo();
        selectedDay.setValue(null); // Needs to have a value so that getNextDays returns data
    }

    public LiveData<List<Timeslot>> getTimeslotsOfMember(int memberId) {
        return repo.getTimeslotsOfMember(LocalDateTime.now().plusDays(nDays), memberId);
    }

    public LiveData<List<Timeslot>> getTimeslotsOnSelectedDay(int memberId) {
        return Transformations.switchMap(getTimeslotsOfMember(memberId), timeslots ->
                Transformations.map(selectedDay, day ->
                        timeslots.stream()
                                .filter(timeslot -> timeslot.getStart().toLocalDate().equals(day))
                                .collect(Collectors.toList())
                )
        );
    }

    private List<DayItem> calculateDayItems(@Nullable List<Timeslot> timeslots, LocalDate startDate, @Nullable LocalDate selectedDate) {
        if (timeslots == null) {
            return List.of();
        }

        Set<LocalDate> timeslotDates = timeslots.stream()
                .map(Timeslot::getStart)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());

        return IntStream.range(0, nDays).mapToObj(i -> {
            LocalDate date = startDate.plusDays(i);
            boolean hasTimeslots = timeslotDates.contains(date);
            boolean isSelected = date.equals(selectedDate);
            return new DayItem(date, hasTimeslots, isSelected);
        }).collect(Collectors.toList());
    }

    public LiveData<List<DayItem>> getNextDays(int memberId, LocalDate startDate) {
        return Transformations.switchMap(getTimeslotsOfMember(memberId), timeslots ->
                Transformations.map(selectedDay, day ->
                    calculateDayItems(timeslots, startDate, day)
                )
        );
    }
}
