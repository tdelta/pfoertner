package de.tu_darmstadt.epool.pfoertnerpanel;

import androidx.room.Room;
import android.content.Context;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;

/**
 * Class for maintaining global application state that is required in the 
 * whole panel
 */
public class PanelApplication extends PfoertnerApplication {

    public static PanelApplication get(final Context context) {
        return (PanelApplication) context.getApplicationContext();
    }

}
