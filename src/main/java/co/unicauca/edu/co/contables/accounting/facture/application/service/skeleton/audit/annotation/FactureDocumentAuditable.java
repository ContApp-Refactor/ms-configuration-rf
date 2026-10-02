package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactureDocumentAuditable {
    DocumentOperationType operationType();

    String moduleName() default "FACTURAS";
}