package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseException extends RuntimeException {

  private Integer status;
  private String message;

  public BaseException(Integer status, String message) {
    super(message);
    this.status = status;
    this.message = message;
  }
}
