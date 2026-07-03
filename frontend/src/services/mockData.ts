// ============================================
// MOCK DATA FOR MISSING APIS
// TODO: Replace with real API calls when backend endpoints are ready
// ============================================

// Flag to easily switch between mock and real data
export const USE_MOCK_DATA = true;

export interface StreakData {
  currentStreak: number;
  longestStreak: number;
  lastActiveDate: string;
  totalStudyDays: number;
}

export interface GoalsData {
  dailyGoal: number;
  weeklyGoal: number;
  monthlyGoal: number;
  currentProgress: {
    daily: number;
    weekly: number;
    monthly: number;
  };
}

export interface ProgressData {
  totalCasesCompleted: number;
  totalDiseasesLearned: number;
  totalSymptomsLearned: number;
  completionRate: number;
  recentActivities: Array<{
    id: string;
    type: 'case' | 'disease' | 'symptom';
    name: string;
    completedAt: string;
  }>;
}

export interface RecommendedCase {
  id: string;
  title: string;
  description: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  estimatedTime: number;
  tags: string[];
  imageUrl?: string;
}

export const mockStreakData: StreakData = {
  currentStreak: 5,
  longestStreak: 12,
  lastActiveDate: new Date().toISOString(),
  totalStudyDays: 45,
};

export const mockGoalsData: GoalsData = {
  dailyGoal: 3,
  weeklyGoal: 15,
  monthlyGoal: 60,
  currentProgress: {
    daily: 2,
    weekly: 10,
    monthly: 35,
  },
};

export const mockProgressData: ProgressData = {
  totalCasesCompleted: 12,
  totalDiseasesLearned: 8,
  totalSymptomsLearned: 24,
  completionRate: 0.75,
  recentActivities: [
    {
      id: '1',
      type: 'case',
      name: 'Case Study: Respiratory Infection',
      completedAt: new Date().toISOString(),
    },
    {
      id: '2',
      type: 'disease',
      name: 'Pneumonia',
      completedAt: new Date(Date.now() - 86400000).toISOString(),
    },
  ],
};

export const mockRecommendedCases: RecommendedCase[] = [
  {
    id: '1',
    title: 'Acute Respiratory Infection',
    description: 'A 45-year-old patient presents with cough, fever, and shortness of breath.',
    difficulty: 'INTERMEDIATE',
    estimatedTime: 15,
    tags: ['Respiratory', 'Infection'],
  },
  {
    id: '2',
    title: 'Diabetes Management Case',
    description: 'Type 2 diabetes patient with uncontrolled blood sugar levels.',
    difficulty: 'ADVANCED',
    estimatedTime: 20,
    tags: ['Endocrine', 'Chronic'],
  },
  {
    id: '3',
    title: 'Hypertension Basics',
    description: 'Understand the fundamentals of hypertension diagnosis and management.',
    difficulty: 'BEGINNER',
    estimatedTime: 10,
    tags: ['Cardiovascular', 'Fundamentals'],
  },
];
