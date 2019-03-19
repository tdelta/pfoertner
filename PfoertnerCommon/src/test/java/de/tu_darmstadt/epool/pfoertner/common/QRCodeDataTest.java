package de.tu_darmstadt.epool.pfoertner.common;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.runner.RunWith;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class QRCodeDataTest {
    @Before
    public void setup() {
        BasicConfigurator.configure();
    }

    @Property
    public void serializationIdentity(final int id, final String rawJoinCode) {
        final OfficeEntity office = new OfficeEntity(id,rawJoinCode,"","","","");
        final QRCodeData data = new QRCodeData(office);

        final String serialized = data.serialize();
        final QRCodeData deserialized = QRCodeData.deserialize(serialized);

        assertEquals(data.officeId, deserialized.officeId);
        assertEquals(data.joinCode, deserialized.joinCode);
    }
}

