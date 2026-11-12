package co.unicauca.edu.co.contables.architecture.fixture.backup.controller;

/**
 * Fixture de BackupArchitectureTest: "controlador" por paquete. Sin
 * {@code @RestController} a propósito, para que el component scan de los
 * {@code @SpringBootTest} (que también ve las clases de test) no lo registre.
 */
public class FakeController {

    public String name() {
        return "fake.controller";
    }
}
