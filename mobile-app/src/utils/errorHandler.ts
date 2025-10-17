import { ApiErrorTypes, ApiErrorType } from '../services/api';

export interface AppError {
  type: 'API' | 'VALIDATION' | 'NETWORK' | 'UNKNOWN';
  message: string;
  originalError?: Error;
}

export class ErrorHandler {
  static handleApiError(error: Error): AppError {
    const message = error.message;

    // Handle specific API error types
    if (message === ApiErrorTypes.NETWORK_ERROR) {
      return {
        type: 'NETWORK',
        message: 'Network connection failed. Please check your internet connection.',
        originalError: error,
      };
    }

    if (message === ApiErrorTypes.REQUEST_TIMEOUT) {
      return {
        type: 'NETWORK',
        message: 'Request timed out. Please try again.',
        originalError: error,
      };
    }

    if (message === ApiErrorTypes.SESSION_EXPIRED) {
      return {
        type: 'API',
        message: 'Your session has expired. Please log in again.',
        originalError: error,
      };
    }

    if (message === ApiErrorTypes.SERVER_ERROR) {
      return {
        type: 'API',
        message: 'Server error. Please try again later.',
        originalError: error,
      };
    }

    if (message === ApiErrorTypes.FORBIDDEN) {
      return {
        type: 'API',
        message: 'You do not have permission to perform this action.',
        originalError: error,
      };
    }

    // Handle HTTP error codes
    if (message.startsWith('HTTP_401:')) {
      return {
        type: 'API',
        message: 'Authentication failed. Please check your credentials.',
        originalError: error,
      };
    }

    if (message.startsWith('HTTP_403:')) {
      return {
        type: 'API',
        message: 'Access denied. You do not have permission for this action.',
        originalError: error,
      };
    }

    if (message.startsWith('HTTP_404:')) {
      return {
        type: 'API',
        message: 'The requested resource was not found.',
        originalError: error,
      };
    }

    if (message.startsWith('HTTP_422:')) {
      return {
        type: 'VALIDATION',
        message: 'Invalid data provided. Please check your input.',
        originalError: error,
      };
    }

    if (message.startsWith('HTTP_429:')) {
      return {
        type: 'API',
        message: 'Too many requests. Please wait a moment and try again.',
        originalError: error,
      };
    }

    if (message.startsWith('HTTP_5')) {
      return {
        type: 'API',
        message: 'Server error. Please try again later.',
        originalError: error,
      };
    }

    // Default error handling
    return {
      type: 'UNKNOWN',
      message: 'An unexpected error occurred. Please try again.',
      originalError: error,
    };
  }

  static getErrorMessage(error: unknown): string {
    if (error instanceof Error) {
      const appError = this.handleApiError(error);
      return appError.message;
    }

    if (typeof error === 'string') {
      return error;
    }

    return 'An unexpected error occurred. Please try again.';
  }

  static logError(error: unknown, context?: string) {
    const prefix = context ? `[${context}]` : '[Error]';
    
    if (error instanceof Error) {
      console.error(`${prefix} ${error.message}`, error.stack);
    } else {
      console.error(`${prefix}`, error);
    }
  }
}

// Utility function for consistent error handling in components
export const handleError = (error: unknown, context?: string): string => {
  ErrorHandler.logError(error, context);
  return ErrorHandler.getErrorMessage(error);
};