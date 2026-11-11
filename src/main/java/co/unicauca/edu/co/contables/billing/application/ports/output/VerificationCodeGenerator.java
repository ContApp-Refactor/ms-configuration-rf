package co.unicauca.edu.co.contables.billing.application.ports.output;

/**
 * Genera el codigo de verificacion (QR) que se congela al emitir una factura. El valor es opaco
 * para quien lo consume; hoy es aleatorio y puede sustituirse (p. ej. por el CUFE) sin cambiar el contrato.
 */
public interface VerificationCodeGenerator {

    String next();
}
