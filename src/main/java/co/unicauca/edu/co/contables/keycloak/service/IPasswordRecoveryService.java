package co.unicauca.edu.co.contables.keycloak.service;

public interface IPasswordRecoveryService {
  void startRecovery(String email);
  void resetPassword(String token, String newPassword);
}
