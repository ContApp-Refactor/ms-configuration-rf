package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief D3: al restaurar una copia como empresa NUEVA, el tenant destino ya
 * tiene una empresa con ese NIT. (Duplicar sí permite el NIT repetido.)
 *
 * El front puede reintentar mandando {@code nitDestino} y/o {@code nombreDestino}.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EnterpriseNitAlreadyExistsException extends RuntimeException {

    public static final String CODE = "ENTERPRISE_NIT_ALREADY_EXISTS";

    private final String nit;

    public EnterpriseNitAlreadyExistsException(String nit) {
        super("Ya tienes una empresa con NIT " + nit + ". Indica otro NIT o nombre para restaurar la copia.");
        this.nit = nit;
    }

    public String getNit() {
        return nit;
    }
}
