package co.unicauca.edu.co.contables.inventory.kardex.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MessageProcessingError;

class MessageProcessingErrorTest {

    @Test
    void requireValid_setsTimestampAndValidates() {
        MessageProcessingError mpe = new MessageProcessingError();
        mpe.setEventType("PRODUCT_CREATED");
        mpe.setEntityType("Product");
        mpe.setErrorDescription("Failed to persist message");

        mpe.requireValid();

        assertNotNull(mpe.getErrorTimestamp());
        assertEquals("PRODUCT_CREATED", mpe.getEventType());
        assertEquals("Product", mpe.getEntityType());
        assertFalse(mpe.getErrorDescription().isEmpty());
    }

    @Test
    void summary_buildsConciseString() {
        MessageProcessingError mpe = new MessageProcessingError();
        mpe.setEventType("EV");
        mpe.setEntityType("Entity");
        mpe.setErrorDescription("Some detailed error message that should be truncated if too long");
        mpe.requireValid();

        String s = mpe.summary(50);
        assertNotNull(s);
        assertTrue(s.contains("event=EV"));
        assertTrue(s.contains("entity=Entity"));
        assertTrue(s.contains("detail="));
    }
}
