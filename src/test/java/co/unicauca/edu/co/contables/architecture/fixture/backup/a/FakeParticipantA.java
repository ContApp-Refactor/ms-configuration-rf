package co.unicauca.edu.co.contables.architecture.fixture.backup.a;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;

/** Fixture de BackupArchitectureTest: participante que cumple las reglas. */
public class FakeParticipantA implements BackupParticipant {

    @Override
    public String moduleKey() {
        return "fake.a";
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public int schemaVersion() {
        return 1;
    }

    @Override
    public Set<String> requires() {
        return Set.of();
    }

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        // fixture
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        // fixture
    }

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of();
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        return List.of();
    }
}
