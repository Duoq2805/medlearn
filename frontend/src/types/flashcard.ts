export type FlashcardDifficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type FlashcardStatus = 'ACTIVE' | 'ARCHIVED' | 'MASTERED';
export type SourceType =
  | 'DISEASE'
  | 'DOCUMENT'
  | 'CHUNK'
  | 'DRAFT'
  | 'SUMMARY'
  | 'CASE_STUDY'
  | 'CHAT'
  | 'STUDY_REPORT';

export interface FlashcardGenerateRequest {
  diseaseId?: number;
  documentId?: number;
  title: string;
  count?: number;
  difficulty?: FlashcardDifficulty;
  tag?: string;
  model?: string;
  temperature?: number;
}

export interface FlashcardUpdateRequest {
  question?: string;
  answer?: string;
  explanation?: string;
  tag?: string;
  difficulty?: FlashcardDifficulty;
  status?: FlashcardStatus;
}

export interface FlashcardBatchUpdateRequest {
  ids: number[];
  status?: FlashcardStatus;
}

export interface FlashcardReviewRequest {
  quality: number;
  responseTimeMs?: number;
}

export interface FlashcardResponse {
  id: number;
  deckId: number;
  deckTitle: string;
  question: string;
  answer: string;
  explanation?: string;
  source?: string;
  tag?: string;
  difficulty: FlashcardDifficulty;
  status: FlashcardStatus;
  createdBy?: number;
  totalReviews: number;
  correctCount: number;
  incorrectCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface FlashcardDeckResponse {
  id: number;
  title: string;
  description?: string;
  sourceType?: SourceType;
  sourceId?: number;
  status?: string;
  cardCount: number;
  createdBy?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface FlashcardGenerateResponse {
  generationId: string;
  totalGenerated: number;
  flashcards: FlashcardResponse[];
  usage?: {
    promptTokens?: number;
    completionTokens?: number;
    totalTokens?: number;
    latencyMs?: number;
  };
}

export interface FlashcardProgressResponse {
  flashcard: FlashcardResponse;
  easinessFactor: number;
  interval: number;
  repetitions: number;
  totalReviews: number;
  correctCount: number;
  incorrectCount: number;
  nextReviewAt?: string;
  lastReviewedAt?: string;
  lastQuality?: number;
  mastered: boolean;
}

export interface FlashcardReviewResponse {
  flashcardId: number;
  quality: number;
  easinessFactor: number;
  interval: number;
  repetitions: number;
  nextReviewAt?: string;
  totalReviews: number;
  mastered: boolean;
}

export interface FlashcardStatsResponse {
  totalCards: number;
  masteredCards: number;
  cardsDueToday: number;
  cardsDueThisWeek: number;
  averageEasinessFactor: number;
  overallAccuracy: number;
  totalReviews: number;
  totalDecks: number;
}

export interface FlashcardExportResponse {
  format: string;
  deckTitle: string;
  count: number;
  flashcards: FlashcardResponse[];
  csvData?: string;
}
