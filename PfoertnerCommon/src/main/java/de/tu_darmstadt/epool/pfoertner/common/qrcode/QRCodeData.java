package de.tu_darmstadt.epool.pfoertner.common.qrcode;

import com.google.gson.Gson;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;

public class QRCodeData {
    public final int officeId;
    public final String joinCode;

    public QRCodeData(final Office office) {
        this.officeId = office.getId();
        this.joinCode = office.getJoinCode();
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
