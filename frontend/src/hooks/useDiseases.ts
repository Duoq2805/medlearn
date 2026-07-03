import { useQuery, useMutation } from '@tanstack/react-query';
import { diseaseApi } from '../api/disease';
import { Disease } from '../types';

export const useDiseases = (page: number = 1, pageSize: number = 10) => {
  return useQuery({
    queryKey: ['diseases', page, pageSize],
    queryFn: () => diseaseApi.fetchDiseases(page, pageSize),
  });
};

export const useDisease = (id: string) => {
  return useQuery({
    queryKey: ['disease', id],
    queryFn: () => diseaseApi.fetchDisease(id),
    enabled: !!id,
  });
};

export const useSearchDiseases = (query: string) => {
  return useQuery({
    queryKey: ['diseases', 'search', query],
    queryFn: () => diseaseApi.searchDiseases(query),
    enabled: query.length > 0,
  });
};

export const useDiseaseCategories = () => {
  return useQuery({
    queryKey: ['disease-categories'],
    queryFn: () => diseaseApi.getCategories(),
  });
};

export const useCreateDisease = () => {
  return useMutation({
    mutationFn: (disease: Omit<Disease, 'id' | 'createdAt' | 'updatedAt'>) =>
      diseaseApi.createDisease(disease),
  });
};
