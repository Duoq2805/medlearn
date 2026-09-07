import { useQuery, useMutation } from '@tanstack/react-query';
import { diseaseApi } from '../api/disease';
import type { CreateDiseaseRequest } from '../types/disease';

export const useDiseases = (page: number = 0, pageSize: number = 20) => {
  return useQuery({
    queryKey: ['diseases', page, pageSize],
    queryFn: () => diseaseApi.fetchDiseases(undefined, undefined, undefined, page, pageSize),
  });
};

export const useDisease = (id: string | number) => {
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
    mutationFn: (disease: CreateDiseaseRequest) =>
      diseaseApi.createDisease(disease),
  });
};
