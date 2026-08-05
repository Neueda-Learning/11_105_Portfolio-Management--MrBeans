import { apiClient } from './client';


export const chatApi = {
  sendMessage: (data) => apiClient.fetch('/chat', {
    method: 'POST',
    body: JSON.stringify(data)
  })
};