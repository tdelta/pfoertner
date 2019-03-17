package de.tu_darmstadt.epool.pfoertner.common.qrcode;

import com.google.gson.Gson;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;

/**
 * Wrapper for join office data retrieved when scanning a qr code
 */
public class QRCodeData {
    public final int officeId;
    public final String joinCode;

    public QRCodeData(final Office office) {
        this.officeId = office.getId();
        this.joinCode = office.getJoinCode();
    }

    /**
     * Convert the object into a JSON String
     * @return JSON String
     */
    public String serialize() {
        return new Gson()
                .toJson(this);
    }

    /**
     * Convert a JSON String into an object
     * @param data JSON String
     * @return QRCodeData instance
     */
    public static QRCodeData deserialize(final String data) {
        return new Gson()
                .fromJson(data, QRCodeData.class);
    }
}
