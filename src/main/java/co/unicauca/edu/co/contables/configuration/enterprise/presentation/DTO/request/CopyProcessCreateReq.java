package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import java.util.Map;
import java.util.UUID;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Cuerpo de {@code POST /api/enterprises/copy/processes}.
 *
 * {@code empresaDestino} llega como string plano para BACKUP (no se usa) o
 * como objeto {@code { nombre: string }} para DUPLICATE, aunque el modelo de
 * respuesta lo tipe siempre como string; por eso se recibe como
 * {@code Object} y se resuelve con {@link #resolveEmpresaDestinoNombre()}.
 */
@Getter
@Setter
@NoArgsConstructor
public class CopyProcessCreateReq {
    private CopyProcessTipoEnum tipo;
    private UUID empresaOrigen;
    private Object empresaDestino;
    private boolean generateBackup;

    /**
     * Devuelve el nombre de destino sin importar si {@code empresaDestino}
     * llegó como string plano o como objeto {@code {nombre}}.
     */
    @SuppressWarnings("unchecked")
    public String resolveEmpresaDestinoNombre() {
        if (empresaDestino == null) {
            return null;
        }
        if (empresaDestino instanceof String s) {
            return s;
        }
        if (empresaDestino instanceof Map<?, ?> map) {
            Object nombre = ((Map<String, Object>) map).get("nombre");
            return nombre == null ? null : nombre.toString();
        }
        return empresaDestino.toString();
    }
}
