package co.unicauca.edu.co.contables.architecture.fixture.backup.coordinator;

import co.unicauca.edu.co.contables.architecture.fixture.backup.a.FakeParticipantA;

/** Fixture de BackupArchitectureTest: VIOLA R2 porque conoce un participante concreto. */
public class FakeCoordinator {

    public String run() {
        return new FakeParticipantA().moduleKey();
    }
}
