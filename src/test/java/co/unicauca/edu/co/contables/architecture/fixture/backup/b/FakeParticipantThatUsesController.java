package co.unicauca.edu.co.contables.architecture.fixture.backup.b;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import co.unicauca.edu.co.contables.architecture.fixture.backup.controller.FakeController;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;

/** Fixture de BackupArchitectureTest: VIOLA R3 porque depende de un controlador. */
public class FakeParticipantThatUsesController implements BackupParticipant {

    private final FakeController controller = new FakeController();

    @Override
    public String moduleKey() {
        return controller.name();
    }

    @Override
    public int order() {
        return 3;
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
