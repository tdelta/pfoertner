package de.tu_darmstadt.epool.pfoertnerpanel;

import android.arch.persistence.room.Room;
import android.content.Context;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;
import de.tu_darmstadt.epool.pfoertnerpanel.repositories.PanelRepository;
import de.tu_darmstadt.epool.pfoertnerpanel.webapi.CalendarApi;

public class PanelApplication extends PfoertnerApplication {
    private PanelDatabase db;
    private PanelRepository repo;
    private CalendarApi calendarApi;

    @Override
    public void onInit() {
        db = Room.databaseBuilder(this, PanelDatabase.class, "PanelDatabase").build();
        repo = new PanelRepository(db,this);
        calendarApi = new CalendarApi();
    }

    public static PanelApplication get(final Context context) {
        return (PanelApplication) context.getApplicationContext();
    }

    public PanelRepository getPanelRepo() {
        checkInitStatus();

        return repo;
    }

    public CalendarApi getCalendarApi() {
        checkInitStatus();

        return calendarApi;
    }
}
