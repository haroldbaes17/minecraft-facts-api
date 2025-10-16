package io.github.haroldbaes17.minecraftfacts.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ========= Helpers ========= */

    private ProblemDetail baseProblem(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        // Extras útiles y consistentes en todas las respuestas
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        if (req != null) {
            pd.setProperty("path", req.getRequestURI());
            if (req.getQueryString() != null) {
                pd.setProperty("query", req.getQueryString());
            }
        }
        return pd;
    }

    private ResponseEntity<Object> wrap(ProblemDetail pd) {
        return ResponseEntity.status(pd.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }

    /* ========= Overrides de ResponseEntityExceptionHandler (errores “web” típicos) ========= */

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                // si hay varios errores en el mismo campo, conserva el primero
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a, LinkedHashMap::new));

        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Hay errores de validación en el payload.", req);
        pd.setProperty("errors", fieldErrors);
        pd.setProperty("errorCode", "VALIDATION_ERROR");

        // Log a nivel debug para no ensuciar en prod
        log.debug("Validation failed: {}", fieldErrors, ex);
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Malformed JSON",
                "El cuerpo de la solicitud no es JSON válido o falta.", req);
        pd.setProperty("errorCode", "MALFORMED_JSON");
        log.debug("Malformed JSON", ex);
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Missing parameter",
                "Falta el parámetro requerido: " + ex.getParameterName(), req);
        pd.setProperty("errorCode", "MISSING_PARAM");
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Missing path variable",
                "Falta la variable de ruta: " + ex.getVariableName(), req);
        pd.setProperty("errorCode", "MISSING_PATH_VARIABLE");
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed",
                "Método HTTP no permitido para este endpoint.", req);
        pd.setProperty("allowed", ex.getSupportedHttpMethods());
        pd.setProperty("errorCode", "METHOD_NOT_ALLOWED");
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.NOT_FOUND, "Not Found",
                "No existe un endpoint para la ruta solicitada.", req);
        pd.setProperty("errorCode", "NO_HANDLER");
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {

        HttpServletRequest req = (HttpServletRequest) request.resolveReference(org.springframework.web.context.request.WebRequest.REFERENCE_REQUEST);
        ProblemDetail pd = baseProblem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type",
                "Tipo de contenido no soportado.", req);
        pd.setProperty("supported", ex.getSupportedMediaTypes());
        pd.setProperty("errorCode", "UNSUPPORTED_MEDIA_TYPE");
        return wrap(pd);
    }

    /* ========= ExceptionHandlers específicos ========= */

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = baseProblem(HttpStatus.NOT_FOUND, "Resource not found",
                ex.getMessage() != null ? ex.getMessage() : "El recurso solicitado no existe.", req);
        pd.setProperty("errorCode", "ENTITY_NOT_FOUND");
        return wrap(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class) // para @Validated en parámetros (path/query)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a,b)->a, LinkedHashMap::new));

        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Constraint violation",
                "Hay violaciones de restricciones en los parámetros.", req);
        pd.setProperty("errors", errors);
        pd.setProperty("errorCode", "CONSTRAINT_VIOLATION");
        return wrap(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        ProblemDetail pd = baseProblem(HttpStatus.BAD_REQUEST, "Type mismatch",
                "Tipo de dato inválido para el parámetro '" + ex.getName() + "'.", req);
        pd.setProperty("requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null);
        pd.setProperty("value", ex.getValue());
        pd.setProperty("errorCode", "TYPE_MISMATCH");
        return wrap(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = baseProblem(HttpStatus.CONFLICT, "Data integrity violation",
                "La operación viola restricciones de integridad (unicidad/foreign key/etc.).", req);
        pd.setProperty("errorCode", "DATA_INTEGRITY");
        log.debug("Data integrity violation", ex);
        return wrap(pd);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        ProblemDetail pd = baseProblem(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Autenticación requerida o credenciales inválidas.", req);
        pd.setProperty("errorCode", "UNAUTHORIZED");
        return wrap(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = baseProblem(HttpStatus.FORBIDDEN, "Forbidden",
                "No tienes permisos para acceder a este recurso.", req);
        pd.setProperty("errorCode", "FORBIDDEN");
        return wrap(pd);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        var req = (jakarta.servlet.http.HttpServletRequest)
                request.resolveReference(WebRequest.REFERENCE_REQUEST);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No se encontró el endpoint solicitado.");
        pd.setTitle("Not Found");
        pd.setProperty("path", req != null ? req.getRequestURI() : null);
        pd.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        pd.setProperty("errorCode", "NO_RESOURCE_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    /* ========= Fallback ========= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest req) {
        // Log completo en servidor, mensaje genérico al cliente
        log.error("Unhandled exception", ex);
        ProblemDetail pd = baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ha ocurrido un error inesperado. Si el problema persiste, contacta al soporte.", req);
        pd.setProperty("errorCode", "UNEXPECTED_ERROR");
        return wrap(pd);
    }
}
