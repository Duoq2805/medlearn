-- =========================================
-- V3_3: Seed flashcard-gen prompt template
-- FlashcardGeneratorImpl requires code='flashcard-gen' with status='ACTIVE'
-- Variables used: {{disease}}, {{count}}, {{difficulty}}, {{context}}
-- =========================================
INSERT INTO prompt_template (code, version, name, description, model, temperature, max_tokens,
                              system_prompt, user_prompt_template, required_variables,
                              status, created_at, updated_at)
SELECT 'flashcard-gen', '1.0', 'Flashcard Generator',
       'Generates Q&A flashcards from medical disease or document content',
       'gpt-4o-mini', 0.3, 4000,
       'You are a medical education expert creating flashcards for medical students.
Generate flashcards in Vietnamese. Use clear, precise medical language.
Each flashcard must have a question and answer that tests understanding, not just recall.
Return ONLY a JSON object with a ''flashcards'' array.
Each element must have: ''question'', ''answer'', ''explanation'', ''source''.
Do NOT include markdown code fences, markdown formatting, or any text outside the JSON.
Example: {"flashcards":[{"question":"...","answer":"...","explanation":"...","source":"..."}]}',
       'Generate {{count}} flashcards about "{{disease}}" at {{difficulty}} difficulty level.

Use the following source material:
{{context}}

Language: Vietnamese
Return ONLY valid JSON as specified in the system prompt.',
       'disease,count,difficulty,context',
       'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM prompt_template WHERE code = 'flashcard-gen');
