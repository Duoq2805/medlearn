-- Seed 11 SectionType values missing from V1_0 initial seed.
-- DraftSectionType enum has 17 values; only 6 were seeded.
-- INSERT ... ON CONFLICT DO NOTHING ensures idempotency.

INSERT INTO section_type (name, description) VALUES
    ('overview',               'General overview of the disease'),
    ('prognosis',              'Disease prognosis and outcome'),
    ('complications',          'Potential complications'),
    ('epidemiology',           'Epidemiology and incidence'),
    ('pathophysiology',        'Pathophysiology'),
    ('risk_factors',           'Risk factors'),
    ('clinical_features',      'Clinical features and presentation'),
    ('investigations',         'Investigations and laboratory tests'),
    ('management',             'Clinical management'),
    ('differential_diagnosis', 'Differential diagnosis'),
    ('reference',              'References and sources')
ON CONFLICT (name) DO NOTHING;
