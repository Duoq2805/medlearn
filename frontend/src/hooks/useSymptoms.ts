import { useQuery, useMutation } from '@tanstack/react-query';
import { symptomApi } from '../api/symptom';

export const useSymptoms = () => {
  return useQuery({
    queryKey: ['symptoms'],
    queryFn: () => symptomApi.fetchSymptoms(),
  });
};

export const useSearchSymptoms = (query: string) => {
  return useQuery({
    queryKey: ['symptoms', 'search', query],
    queryFn: () => symptomApi.searchSymptoms(query),
    enabled: query.length > 0,
  });
};

export const useGetDiseaseRecommendations = () => {
  return useMutation({
    mutationFn: (symptomIds: number[]) =>
      symptomApi.getDiseaseRecommendations(symptomIds),
  });
};
