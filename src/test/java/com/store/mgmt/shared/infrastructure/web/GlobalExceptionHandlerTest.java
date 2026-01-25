package com.store.mgmt.shared.infrastructure.web;

import com.store.mgmt.shared.domain.exception.EntityNotFoundException;
import com.store.mgmt.shared.domain.exception.ValidationException;
import com.store.mgmt.modules.organization.domain.exception.TemplateAlreadyAppliedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleMethodArgumentTypeMismatch")
    class HandleMethodArgumentTypeMismatch {

        @Test
        @DisplayName("Should return 400 with helpful message for invalid UUID")
        void returnsHelpfulMessageForInvalidUuid() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("organizationId");
            when(ex.getValue()).thenReturn("undefined");
            when(ex.getRequiredType()).thenReturn((Class) UUID.class);

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message())
                    .contains("Invalid UUID format")
                    .contains("organizationId")
                    .contains("undefined");
        }

        @Test
        @DisplayName("Should return 400 with type info for non-UUID type mismatch")
        void returnsTypeInfoForNonUuidMismatch() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("page");
            when(ex.getValue()).thenReturn("abc");
            when(ex.getRequiredType()).thenReturn((Class) Integer.class);

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message())
                    .contains("Invalid value 'abc'")
                    .contains("page")
                    .contains("Integer");
        }
    }

    @Nested
    @DisplayName("handleEntityNotFound")
    class HandleEntityNotFound {

        @Test
        @DisplayName("Should return 404 with entity message")
        void returns404WithMessage() {
            // Given
            EntityNotFoundException ex = new EntityNotFoundException("Organization", "123");

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleEntityNotFound(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().message()).isEqualTo("Organization '123' not found");
        }
    }

    @Nested
    @DisplayName("handleTemplateAlreadyApplied")
    class HandleTemplateAlreadyApplied {

        @Test
        @DisplayName("Should return 409 Conflict")
        void returns409Conflict() {
            // Given
            TemplateAlreadyAppliedException ex = new TemplateAlreadyAppliedException(
                    "A template has already been applied to this organization"
            );

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleTemplateAlreadyApplied(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().message()).contains("already been applied");
        }
    }

    @Nested
    @DisplayName("handleValidationExceptions")
    class HandleValidationExceptions {

        @Test
        @DisplayName("Should return 400 with validation message")
        void returns400WithMessage() {
            // Given
            ValidationException ex = new ValidationException("Name cannot be empty");

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidationExceptions(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Name cannot be empty");
        }
    }

    @Nested
    @DisplayName("handleIllegalArgument")
    class HandleIllegalArgument {

        @Test
        @DisplayName("Should return 400 for illegal argument")
        void returns400ForIllegalArgument() {
            // Given
            IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Invalid parameter value");
        }
    }

    @Nested
    @DisplayName("handleAllExceptions")
    class HandleAllExceptions {

        @Test
        @DisplayName("Should return 500 with generic message for unhandled exceptions")
        void returns500WithGenericMessage() {
            // Given
            Exception ex = new RuntimeException("Something went wrong internally");

            // When
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleAllExceptions(ex);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred. Please try again later.");
        }
    }
}
