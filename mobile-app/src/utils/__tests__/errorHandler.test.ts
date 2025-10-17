import { ErrorHandler, handleError, AppError } from '../errorHandler';
import { ApiErrorTypes } from '../../services/api';

// Mock the API error types
jest.mock('../../services/api', () => ({
  ApiErrorTypes: {
    NETWORK_ERROR: 'NETWORK_ERROR',
    REQUEST_TIMEOUT: 'REQUEST_TIMEOUT',
    SESSION_EXPIRED: 'SESSION_EXPIRED',
    SERVER_ERROR: 'SERVER_ERROR',
    FORBIDDEN: 'FORBIDDEN',
  },
}));

describe('ErrorHandler', () => {
  describe('handleApiError', () => {
    it('should handle network errors', () => {
      const networkError = new Error(ApiErrorTypes.NETWORK_ERROR);

      const result = ErrorHandler.handleApiError(networkError);

      expect(result).toEqual({
        type: 'NETWORK',
        message: 'Network connection failed. Please check your internet connection.',
        originalError: networkError,
      });
    });

    it('should handle request timeout errors', () => {
      const timeoutError = new Error(ApiErrorTypes.REQUEST_TIMEOUT);

      const result = ErrorHandler.handleApiError(timeoutError);

      expect(result).toEqual({
        type: 'NETWORK',
        message: 'Request timed out. Please try again.',
        originalError: timeoutError,
      });
    });

    it('should handle session expired errors', () => {
      const sessionError = new Error(ApiErrorTypes.SESSION_EXPIRED);

      const result = ErrorHandler.handleApiError(sessionError);

      expect(result).toEqual({
        type: 'API',
        message: 'Your session has expired. Please log in again.',
        originalError: sessionError,
      });
    });

    it('should handle server errors', () => {
      const serverError = new Error(ApiErrorTypes.SERVER_ERROR);

      const result = ErrorHandler.handleApiError(serverError);

      expect(result).toEqual({
        type: 'API',
        message: 'Server error. Please try again later.',
        originalError: serverError,
      });
    });

    it('should handle forbidden errors', () => {
      const forbiddenError = new Error(ApiErrorTypes.FORBIDDEN);

      const result = ErrorHandler.handleApiError(forbiddenError);

      expect(result).toEqual({
        type: 'API',
        message: 'You do not have permission to perform this action.',
        originalError: forbiddenError,
      });
    });

    it('should handle HTTP 401 errors', () => {
      const unauthorizedError = new Error('HTTP_401: Unauthorized');

      const result = ErrorHandler.handleApiError(unauthorizedError);

      expect(result).toEqual({
        type: 'API',
        message: 'Authentication failed. Please check your credentials.',
        originalError: unauthorizedError,
      });
    });

    it('should handle HTTP 403 errors', () => {
      const forbiddenError = new Error('HTTP_403: Forbidden');

      const result = ErrorHandler.handleApiError(forbiddenError);

      expect(result).toEqual({
        type: 'API',
        message: 'Access denied. You do not have permission for this action.',
        originalError: forbiddenError,
      });
    });

    it('should handle HTTP 404 errors', () => {
      const notFoundError = new Error('HTTP_404: Not Found');

      const result = ErrorHandler.handleApiError(notFoundError);

      expect(result).toEqual({
        type: 'API',
        message: 'The requested resource was not found.',
        originalError: notFoundError,
      });
    });

    it('should handle HTTP 422 validation errors', () => {
      const validationError = new Error('HTTP_422: Unprocessable Entity');

      const result = ErrorHandler.handleApiError(validationError);

      expect(result).toEqual({
        type: 'VALIDATION',
        message: 'Invalid data provided. Please check your input.',
        originalError: validationError,
      });
    });

    it('should handle HTTP 429 rate limit errors', () => {
      const rateLimitError = new Error('HTTP_429: Too Many Requests');

      const result = ErrorHandler.handleApiError(rateLimitError);

      expect(result).toEqual({
        type: 'API',
        message: 'Too many requests. Please wait a moment and try again.',
        originalError: rateLimitError,
      });
    });

    it('should handle HTTP 5xx server errors', () => {
      const serverError = new Error('HTTP_500: Internal Server Error');

      const result = ErrorHandler.handleApiError(serverError);

      expect(result).toEqual({
        type: 'API',
        message: 'Server error. Please try again later.',
        originalError: serverError,
      });
    });

    it('should handle unknown errors', () => {
      const unknownError = new Error('Something went wrong');

      const result = ErrorHandler.handleApiError(unknownError);

      expect(result).toEqual({
        type: 'UNKNOWN',
        message: 'An unexpected error occurred. Please try again.',
        originalError: unknownError,
      });
    });
  });

  describe('getErrorMessage', () => {
    it('should get message from Error object', () => {
      const error = new Error(ApiErrorTypes.NETWORK_ERROR);

      const result = ErrorHandler.getErrorMessage(error);

      expect(result).toBe('Network connection failed. Please check your internet connection.');
    });

    it('should handle string errors', () => {
      const error = 'String error message';

      const result = ErrorHandler.getErrorMessage(error);

      expect(result).toBe('String error message');
    });

    it('should handle unknown error types', () => {
      const error = { someProperty: 'value' };

      const result = ErrorHandler.getErrorMessage(error);

      expect(result).toBe('An unexpected error occurred. Please try again.');
    });
  });

  describe('logError', () => {
    it('should log Error objects with stack trace', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = new Error('Test error');
      error.stack = 'Error stack trace';

      ErrorHandler.logError(error, 'TestContext');

      expect(consoleSpy).toHaveBeenCalledWith('[TestContext] Test error', 'Error stack trace');
      consoleSpy.mockRestore();
    });

    it('should log non-Error objects', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = 'String error';

      ErrorHandler.logError(error, 'TestContext');

      expect(consoleSpy).toHaveBeenCalledWith('[TestContext]', 'String error');
      consoleSpy.mockRestore();
    });

    it('should log without context', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = new Error('Test error');

      ErrorHandler.logError(error);

      expect(consoleSpy).toHaveBeenCalledWith('[Error] Test error', expect.any(String));
      consoleSpy.mockRestore();
    });
  });
});

describe('handleError utility function', () => {
  it('should handle errors and return message', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
    const error = new Error(ApiErrorTypes.NETWORK_ERROR);

    const result = handleError(error, 'TestContext');

    expect(result).toBe('Network connection failed. Please check your internet connection.');
    expect(consoleSpy).toHaveBeenCalled();
    consoleSpy.mockRestore();
  });

  it('should handle string errors', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
    const error = 'String error';

    const result = handleError(error, 'TestContext');

    expect(result).toBe('String error');
    expect(consoleSpy).toHaveBeenCalled();
    consoleSpy.mockRestore();
  });
});