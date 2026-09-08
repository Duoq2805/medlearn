export type QuizStatus = 'ACTIVE' | 'ARCHIVED';
export type SourceType = 'DISEASE' | 'DOCUMENT';

export interface QuizGenerateRequest {
  diseaseId?: number;
  documentId?: number;
  title: string;
  count?: number;
  model?: string;
  temperature?: number;
}

export interface QuizSubmitRequest {
  answers: QuizAnswer[];
}

export interface QuizAnswer {
  questionId: number;
  selectedAnswer: 'A' | 'B' | 'C' | 'D';
}

export interface QuestionResponse {
  id: number;
  quizId: number;
  content: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  explanation?: string;
  displayOrder?: number;
  createdAt?: string;
}

export interface QuizResponse {
  id: number;
  title: string;
  sourceType?: SourceType;
  sourceId?: number;
  questionCount: number;
  status?: QuizStatus;
  createdBy?: number;
  questions: QuestionResponse[];
  createdAt?: string;
  updatedAt?: string;
}

export interface QuizGenerateResponse {
  quizId: number;
  title: string;
  totalGenerated: number;
  quiz: QuizResponse;
  usage?: {
    promptTokens?: number;
    completionTokens?: number;
    totalTokens?: number;
  };
}

export interface QuizSubmitResponse {
  attemptId: number;
  quizId: number;
  correctCount: number;
  totalQuestions: number;
  percentage: number;
  results: QuestionResult[];
}

export interface QuestionResult {
  questionId: number;
  selectedAnswer: string;
  correctAnswer: string;
  correct: boolean;
  explanation?: string;
}

export interface QuizAttemptResponse {
  id: number;
  quizId: number;
  quizTitle: string;
  correctCount: number;
  totalQuestions: number;
  percentage: number;
  status: string;
  createdAt?: string;
}
