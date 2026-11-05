package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.config.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.config.i18n.MessageService;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService - Tests de servicio de mensajes i18n")
public class MessageServiceUnitTest {
      @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageSource messageSource;

    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final Locale SPANISH = new Locale("es");
    private static final String TEST_KEY = "test.message.key";
    private static final String DEFAULT_MESSAGE = "Default message";

    // ==================== Tests para getMessage(String, Object...) ====================

    @Test
    @DisplayName("getMessage(key, args) - Con key válido sin argumentos retorna mensaje en locale actual")
    void getMessage_WithValidKeyNoArgs_ReturnsMessageInCurrentLocale() {
        // Arrange
        String expectedMessage = "Test message";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, args) - Con key válido y un argumento retorna mensaje formateado")
    void getMessage_WithValidKeyAndOneArg_ReturnsFormattedMessage() {
        // Arrange
        String expectedMessage = "Hello, John!";
        Object[] args = {"John"};
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, args, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, args);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, args, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, args) - Con múltiples argumentos retorna mensaje con todos los parámetros")
    void getMessage_WithMultipleArgs_ReturnsMessageWithAllParameters() {
        // Arrange
        String expectedMessage = "Product: Widget, Price: 99.99, Quantity: 5";
        Object[] args = {"Widget", 99.99, 5};
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, args, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, args);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, args, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, args) - Con locale español retorna mensaje en español")
    void getMessage_WithSpanishLocale_ReturnsMessageInSpanish() {
        // Arrange
        String expectedMessage = "Mensaje de prueba";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(SPANISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, SPANISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, SPANISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, args) - Con key diferente retorna mensaje correspondiente")
    void getMessage_WithDifferentKey_ReturnsCorrespondingMessage() {
        // Arrange
        String differentKey = "error.message";
        String expectedMessage = "An error occurred";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(differentKey, new Object[]{}, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(differentKey);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(differentKey, new Object[]{}, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, args) - Con argumentos null retorna mensaje sin formatear")
    void getMessage_WithNullArgs_ReturnsUnformattedMessage() {
        // Arrange
        String expectedMessage = "Simple message";
        Object[] nullArgs = null;
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, nullArgs, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, nullArgs);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, nullArgs, ENGLISH);
        }
    }

    // ==================== Tests para getMessage(String, String, Object...) ====================

    @Test
    @DisplayName("getMessage(key, default, args) - Con key existente retorna mensaje del bundle")
    void getMessageWithDefault_WithExistingKey_ReturnsMessageFromBundle() {
        // Arrange
        String expectedMessage = "Message from bundle";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, DEFAULT_MESSAGE, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, DEFAULT_MESSAGE);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, DEFAULT_MESSAGE, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, default, args) - Con key no existente retorna mensaje por defecto")
    void getMessageWithDefault_WithNonExistingKey_ReturnsDefaultMessage() {
        // Arrange
        String nonExistingKey = "non.existing.key";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(nonExistingKey, new Object[]{}, DEFAULT_MESSAGE, ENGLISH))
                .thenReturn(DEFAULT_MESSAGE);
            
            // Act
            String result = messageService.getMessage(nonExistingKey, DEFAULT_MESSAGE);
            
            // Assert
            assertNotNull(result);
            assertEquals(DEFAULT_MESSAGE, result);
            verify(messageSource, times(1)).getMessage(nonExistingKey, new Object[]{}, DEFAULT_MESSAGE, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, default, args) - Con argumentos retorna mensaje formateado con default como fallback")
    void getMessageWithDefault_WithArgs_ReturnsFormattedMessageWithDefaultFallback() {
        // Arrange
        String expectedMessage = "Welcome, Alice!";
        Object[] args = {"Alice"};
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, args, DEFAULT_MESSAGE, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, DEFAULT_MESSAGE, args);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, args, DEFAULT_MESSAGE, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, default, args) - Con locale español y default retorna mensaje correcto")
    void getMessageWithDefault_WithSpanishLocale_ReturnsCorrectMessage() {
        // Arrange
        String expectedMessage = "Mensaje en español";
        String spanishDefault = "Mensaje predeterminado";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(SPANISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, spanishDefault, SPANISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, spanishDefault);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, spanishDefault, SPANISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, default, args) - Con múltiples argumentos y default retorna mensaje formateado")
    void getMessageWithDefault_WithMultipleArgsAndDefault_ReturnsFormattedMessage() {
        // Arrange
        String expectedMessage = "Order #12345 for customer Bob with total $150.50";
        Object[] args = {12345, "Bob", 150.50};
        String defaultMsg = "Order details unavailable";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, args, defaultMsg, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, defaultMsg, args);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, args, defaultMsg, ENGLISH);
        }
    }

    @Test
    @DisplayName("getMessage(key, default, args) - Con default null pero key válida retorna mensaje del bundle")
    void getMessageWithDefault_WithNullDefaultButValidKey_ReturnsMessageFromBundle() {
        // Arrange
        String expectedMessage = "Message from properties";
        String nullDefault = null;
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, nullDefault, ENGLISH))
                .thenReturn(expectedMessage);
            
            // Act
            String result = messageService.getMessage(TEST_KEY, nullDefault);
            
            // Assert
            assertNotNull(result);
            assertEquals(expectedMessage, result);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, nullDefault, ENGLISH);
        }
    }

    // ==================== Tests de integración entre ambos métodos ====================

    @Test
    @DisplayName("Ambos métodos - Usan el mismo locale del contexto actual")
    void bothMethods_UseSameLocaleFromCurrentContext() {
        // Arrange
        String message1 = "Message 1";
        String message2 = "Message 2";
        String key1 = "key1";
        String key2 = "key2";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            
            when(messageSource.getMessage(key1, new Object[]{}, ENGLISH))
                .thenReturn(message1);
            when(messageSource.getMessage(key2, new Object[]{}, DEFAULT_MESSAGE, ENGLISH))
                .thenReturn(message2);
            
            // Act
            String result1 = messageService.getMessage(key1);
            String result2 = messageService.getMessage(key2, DEFAULT_MESSAGE);
            
            // Assert
            assertEquals(message1, result1);
            assertEquals(message2, result2);
            verify(messageSource, times(1)).getMessage(key1, new Object[]{}, ENGLISH);
            verify(messageSource, times(1)).getMessage(key2, new Object[]{}, DEFAULT_MESSAGE, ENGLISH);
        }
    }

    @Test
    @DisplayName("Múltiples llamadas - Cada una obtiene el locale actual del momento de invocación")
    void multipleCalls_EachGetsCurrentLocaleAtInvocationTime() {
        // Arrange
        String englishMessage = "English message";
        String spanishMessage = "Mensaje en español";
        
        try (MockedStatic<LocaleContextHolder> mockedLocaleHolder = mockStatic(LocaleContextHolder.class)) {
            // First call with English locale
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(ENGLISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, ENGLISH))
                .thenReturn(englishMessage);
            
            String result1 = messageService.getMessage(TEST_KEY);
            
            // Second call with Spanish locale
            mockedLocaleHolder.when(LocaleContextHolder::getLocale).thenReturn(SPANISH);
            when(messageSource.getMessage(TEST_KEY, new Object[]{}, SPANISH))
                .thenReturn(spanishMessage);
            
            String result2 = messageService.getMessage(TEST_KEY);
            
            // Assert
            assertEquals(englishMessage, result1);
            assertEquals(spanishMessage, result2);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, ENGLISH);
            verify(messageSource, times(1)).getMessage(TEST_KEY, new Object[]{}, SPANISH);
        }
    }
}
