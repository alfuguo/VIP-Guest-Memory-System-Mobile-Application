import axios from 'axios';
import { apiClient, ApiErrorTypes } from '../api';
import { tokenStorage } from '../../utils/tokenStorage';

// Mock dependencies
jest.mock('axios');
jest.mock('../../utils/tokenStorage');

const mockedAxios = axios as jest.Mocked<typeof axios>;
const mockedTokenStorage = tokenStorage as jest.Mocked<typeof tokenStorage>;

describe('ApiClient', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock axios.create to return a mock instance
    const mockAxiosInstance = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      request: jest.fn(),
      interceptors: {
        request: {
          use: jest.fn(),
        },
        response: {
          use: jest.fn(),
        },
      },
    };
    
    mockedAxios.create.mockReturnValue(mockAxiosInstance as any);
  });

  it('should create axios instance with correct configuration', () => {
    expect(mockedAxios.create).toHaveBeenCalledWith({
      baseURL: expect.stringContaining('/api'),
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  });

  it('should setup request and response interceptors', () => {
    const mockAxiosInstance = mockedAxios.create();
    
    expect(mockAxiosInstance.interceptors.request.use).toHaveBeenCalled();
    expect(mockAxiosInstance.interceptors.response.use).toHaveBeenCalled();
  });

  it('should export correct error types', () => {
    expect(ApiErrorTypes.UNAUTHORIZED).toBe('UNAUTHORIZED');
    expect(ApiErrorTypes.FORBIDDEN).toBe('FORBIDDEN');
    expect(ApiErrorTypes.SERVER_ERROR).toBe('SERVER_ERROR');
    expect(ApiErrorTypes.NETWORK_ERROR).toBe('NETWORK_ERROR');
    expect(ApiErrorTypes.REQUEST_TIMEOUT).toBe('REQUEST_TIMEOUT');
    expect(ApiErrorTypes.SESSION_EXPIRED).toBe('SESSION_EXPIRED');
    expect(ApiErrorTypes.TOO_MANY_REQUESTS).toBe('TOO_MANY_REQUESTS');
  });
});