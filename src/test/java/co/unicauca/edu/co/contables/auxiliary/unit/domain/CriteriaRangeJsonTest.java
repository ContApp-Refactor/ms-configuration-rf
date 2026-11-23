package co.unicauca.edu.co.contables.auxiliary.unit.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CriteriaRangeJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializesFromAndToKeys() throws Exception {
        String json = "{\"from\":4,\"to\":6}";
        CriteriaRange range = mapper.readValue(json, CriteriaRange.class);
        assertEquals(4L, range.getFromRange());
        assertEquals(6L, range.getToRange());
    }

    @Test
    void deserializesLegacyFromRangeAndToRangeKeys() throws Exception {
        String json = "{\"fromRange\":4,\"toRange\":6}";
        CriteriaRange range = mapper.readValue(json, CriteriaRange.class);
        assertEquals(4L, range.getFromRange());
        assertEquals(6L, range.getToRange());
    }

    @Test
    void serializesUsingFromAndToKeys() throws Exception {
        CriteriaRange range = new CriteriaRange(4L, 6L);
        String json = mapper.writeValueAsString(range);
        assertTrue(json.contains("\"from\":4"));
        assertTrue(json.contains("\"to\":6"));
    }
}
