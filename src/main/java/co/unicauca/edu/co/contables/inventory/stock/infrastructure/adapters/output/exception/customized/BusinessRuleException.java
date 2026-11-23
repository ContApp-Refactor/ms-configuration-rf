package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleException extends BaseException {

  public BusinessRuleException(Integer status, String message) {
    super(status, message);
  }
}
