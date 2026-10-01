package co.unicauca.edu.co.contables.accounting.treasury.domain.port;

public interface IFormatterResultOutputPort {
    void returnResponseError(int status, String message);
}
