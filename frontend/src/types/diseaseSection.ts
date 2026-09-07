export interface SectionTypeResponse { id: number; name: string; code?: string; description?: string | null; }
export interface SectionTemplateResponse { id: number; name: string; content?: string | null; }
export interface DiseaseSectionResponse { id: number; versionId?: number; sectionTypeId?: number; sectionType?: string; title: string; content: string; orderIndex: number; }
export interface CreateDiseaseSectionRequest { sectionTypeId: number; title: string; content: string; orderIndex?: number; }
export interface UpdateDiseaseSectionRequest { sectionTypeId?: number; title?: string; content?: string; orderIndex?: number; }
export interface SectionOrderRequest { sectionId: number; orderIndex: number; }