# Disease Versioning Workflow

Disease content supports version tracking.

Core entities:
- Disease
- DiseaseVersion
- ReviewerAction

Versioning flow:
- disease edited
- new DiseaseVersion created
- previous approved version remains public
- reviewer evaluates new version
- approved version replaces current live version

Versioning goals:
- preserve history
- support moderation
- prevent accidental data loss
- maintain educational consistency

Important rules:
- avoid direct overwrite of approved content
- preserve reviewer history
- maintain auditability