// Query invalidation - use right after successful login
import { useQueryClient } from '@tanstack/react-query';

// In a component:
const queryClient = useQueryClient();

// After login success:
queryClient.invalidateQueries({ queryKey: ['auth', 'me'] });
// This forces /me to refetch with new token

// After mutation (e.g., case study diagnosis):
queryClient.invalidateQueries({ queryKey: ['cases'] });

// Query configuration in main.tsx or App.tsx:
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,      // 5 min before refetch
      gcTime: 30 * 60 * 1000,         // 30 min garbage collection
      retry: 1,                        // retry once on failure
      refetchOnWindowFocus: false,     // avoid unnecessary refetches
      refetchOnReconnect: false,
    },
  },
});
