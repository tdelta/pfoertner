package de.tu_darmstadt.epool.pfoertner.common;

import com.google.gson.Gson;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;

public class QRCodeData {
    public final int officeId;
    public final String joinCode;

    public QRCodeData(final Office office) {
        this.officeId = office.id;
        this.joinCode = office.userJoinCode;
    }

    public String serialize() {
        return new Gson()
                .toJson(this);
    }

    public static QRCodeData deserialize(final String data) {
        return new Gson()
                .fromJson(data, QRCodeData.class);
    }
}
