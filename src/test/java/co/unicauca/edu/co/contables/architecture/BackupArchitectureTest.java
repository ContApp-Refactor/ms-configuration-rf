package co.unicauca.edu.co.contables.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import co.unicauca.edu.co.contables.architecture.fixture.backup.a.FakeParticipantA;
import co.unicauca.edu.co.contables.architecture.fixture.backup.b.FakeParticipantB;
import co.unicauca.edu.co.contables.architecture.fixture.backup.b.FakeParticipantThatUsesController;
import co.unicauca.edu.co.contables.architecture.fixture.backup.coordinator.FakeCoordinator;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;

/**
 * Reglas de arquitectura de la copia de seguridad completa:
 * <ol>
 * <li>R1: un participante no depende de otro participante.</li>
 * <li>R2: fuera del paquete de un participante nadie depende de él (el coordinador
 * solo conoce {@code List<BackupParticipant>}).</li>
 * <li>R3: los participantes no dependen de controladores.</li>
 * <li>R4: {@code commons.backup} no depende de ningún módulo de negocio.</li>
 * </ol>
 * Las reglas de participantes permiten conjuntos vacíos porque los participantes
 * se agregan en tareas posteriores. {@link #lasReglasDetectanViolaciones()}
 * prueba las condiciones propias contra clases de fixture (que, por estar en
 * test, no entran en el import de producción).
 */
class BackupArchitectureTest {

    private static final String ROOT = "co.unicauca.edu.co.contables";

    /** Implementación (no interfaz) de {@link BackupParticipant}. */
    static final DescribedPredicate<JavaClass> PARTICIPANT_IMPL =
            new DescribedPredicate<>("implementan BackupParticipant") {
                @Override
                public boolean test(JavaClass c) {
                    return isParticipantImpl(c);
                }
            };

    static final ArchRule R1_participants_do_not_depend_on_each_other = classes()
            .that(PARTICIPANT_IMPL)
            .should(new ArchCondition<JavaClass>("no depender de otro BackupParticipant") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    for (Dependency d : item.getDirectDependenciesFromSelf()) {
                        JavaClass target = d.getTargetClass();
                        if (isParticipantImpl(target) && !sameTopLevel(item, target)) {
                            events.add(SimpleConditionEvent.violated(item, d.getDescription()));
                        }
                    }
                }
            })
            .allowEmptyShould(true);

    static final ArchRule R2_only_own_package_depends_on_a_participant = classes()
            .should(new ArchCondition<JavaClass>(
                    "depender de un BackupParticipant concreto solo desde su mismo paquete") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    for (Dependency d : item.getDirectDependenciesFromSelf()) {
                        JavaClass target = d.getTargetClass();
                        if (isParticipantImpl(target) && !target.getPackageName().equals(item.getPackageName())) {
                            events.add(SimpleConditionEvent.violated(item, d.getDescription()));
                        }
                    }
                }
            })
            .allowEmptyShould(true);

    static final ArchRule R3_participants_do_not_depend_on_controllers = noClasses()
            .that(PARTICIPANT_IMPL)
            .should().dependOnClassesThat().areAnnotatedWith(RestController.class)
            .orShould().dependOnClassesThat().areAnnotatedWith(Controller.class)
            .orShould().dependOnClassesThat().resideInAnyPackage("..controller..", "..controllers..")
            .allowEmptyShould(true);

    static final ArchRule R4_commons_backup_does_not_depend_on_business_modules = noClasses()
            .that().resideInAPackage(ROOT + ".commons.backup..")
            .should().dependOnClassesThat(
                    resideInAPackage(ROOT + "..")
                            .and(not(resideInAPackage(ROOT + ".commons.."))));

    /**
     * Clases de producción (sin tests). Se importan una vez por JVM. Las reglas se
     * evalúan desde {@code @Test} de Jupiter y no con {@code @ArchTest}: con
     * {@code -Dtest=...} surefire no ejecuta los campos del motor de ArchUnit
     * (reporta 0 tests) y la regla pasaría sin evaluarse.
     */
    private static JavaClasses production;

    private static synchronized JavaClasses production() {
        if (production == null) {
            production = new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages(ROOT);
        }
        return production;
    }

    @Test
    void r1_losParticipantesNoDependenEntreSi() {
        R1_participants_do_not_depend_on_each_other.check(production());
    }

    @Test
    void r2_soloSuPaqueteDependeDeUnParticipanteConcreto() {
        R2_only_own_package_depends_on_a_participant.check(production());
    }

    @Test
    void r3_losParticipantesNoDependenDeControladores() {
        R3_participants_do_not_depend_on_controllers.check(production());
    }

    @Test
    void r4_commonsBackupNoDependeDeModulosDeNegocio() {
        JavaClasses classes = production();
        assertThat(classes.contain(BackupParticipant.class)).isTrue();
        R4_commons_backup_does_not_depend_on_business_modules.check(classes);
    }

    /** Las condiciones propias no son vacuas: con fixtures violadores, fallan. */
    @Test
    void lasReglasDetectanViolaciones() {
        JavaClasses fixtures = new ClassFileImporter()
                .importPackages(ROOT + ".architecture.fixture.backup");
        assertThat(fixtures.contain(FakeParticipantA.class)).isTrue();

        assertThat(R1_participants_do_not_depend_on_each_other.evaluate(fixtures).getFailureReport().getDetails())
                .anyMatch(s -> s.contains(FakeParticipantB.class.getName()) && s.contains(FakeParticipantA.class.getName()));
        assertThat(R2_only_own_package_depends_on_a_participant.evaluate(fixtures).getFailureReport().getDetails())
                .anyMatch(s -> s.contains(FakeCoordinator.class.getName()));
        assertThat(R3_participants_do_not_depend_on_controllers.evaluate(fixtures).getFailureReport().getDetails())
                .anyMatch(s -> s.contains(FakeParticipantThatUsesController.class.getName()));
        assertThat(R1_participants_do_not_depend_on_each_other.evaluate(fixtures).getFailureReport().getDetails())
                .noneMatch(s -> s.startsWith("Method <" + FakeParticipantA.class.getName()));
    }

    private static boolean isParticipantImpl(JavaClass c) {
        return !c.isInterface()
                && !c.isEquivalentTo(BackupParticipant.class)
                && c.isAssignableTo(BackupParticipant.class);
    }

    private static boolean sameTopLevel(JavaClass a, JavaClass b) {
        return topLevelName(a).equals(topLevelName(b));
    }

    private static String topLevelName(JavaClass c) {
        String name = c.getName();
        int idx = name.indexOf('$');
        return idx < 0 ? name : name.substring(0, idx);
    }
}
