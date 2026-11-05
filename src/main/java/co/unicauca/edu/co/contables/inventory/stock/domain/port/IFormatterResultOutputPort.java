package co.unicauca.edu.co.contables.inventory.stock.domain.port;

public interface IFormatterResultOutputPort {
    public void returnBusinessRuleErrorResponse(int status, String message);
    public void returnEntityAlreadyExistsErrorResponse(int status, String message);
    public void returnEntityDoesNotExistErrorResponse(int status, String message);
    public void returnErrorGenericResponse(int status, String message);
}
