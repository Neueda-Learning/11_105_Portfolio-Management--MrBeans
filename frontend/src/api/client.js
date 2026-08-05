const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);this.status = status;
    this.name = 'ApiError';
  }
}

export const apiClient = {
  async fetch(endpoint, options) {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers
      }
    });

    if (!response.ok) {
      let errorMessage = 'An error occurred';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
      } catch {
        errorMessage = response.statusText;
      }
      throw new ApiError(response.status, errorMessage);
    }

    if (response.status === 204) {
      return {};
    }

    return response.json();
  }
};