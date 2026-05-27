# AI Memory & Documentation Workflow

The repository uses markdown documentation as persistent reusable AI memory.

Documentation exists to:
- reduce repeated repository analysis
- preserve architectural reasoning
- compress implementation knowledge
- improve future AI sessions
- reduce unnecessary token usage
- maintain long-term consistency

The AI should treat documentation as reusable engineering intelligence, not traditional documentation.

---

# Documentation Structure

```txt
/docs
├── architecture/
├── workflows/
├── summaries/
└── rules/

/diagrams
/docs-html
```

---

# Folder Responsibilities

## `/docs/architecture`

Contains compressed reusable architecture knowledge.

Examples:
- backend architecture
- authentication architecture
- database architecture
- API architecture
- security structure
- package responsibilities

Purpose:
- reduce repeated architecture inference
- help future sessions understand the system quickly
- preserve important engineering decisions

---

## `/docs/workflows`

Contains reusable business workflow knowledge.

Examples:
- reviewer moderation workflow
- disease versioning flow
- AI learning workflow
- approval lifecycle
- upload flow

Purpose:
- preserve business logic reasoning
- reduce repeated workflow analysis
- maintain workflow consistency

---

## `/docs/summaries`

Contains compressed reusable session memory.

This is the PRIMARY AI memory layer.

After important implementations:
- create or update summaries
- compress implementation reasoning
- preserve reusable decisions
- preserve reusable patterns
- preserve future considerations

DO NOT store trivial implementation details.

Focus on:
- architecture decisions
- reusable patterns
- business logic reasoning
- important service interactions
- future maintainability

Summaries should help future sessions avoid scanning large parts of the repository.

---

## `/docs/rules`

Contains reusable coding conventions and standards.

Examples:
- DTO rules
- naming conventions
- API response rules
- validation rules
- transaction rules

Avoid duplicating rules already defined inside CLAUDE.md.

---

## `/diagrams`

Contains Mermaid and PlantUML diagrams.

Generate or update diagrams ONLY when:
- workflows changed significantly
- architecture changed significantly
- authentication flow changed
- entity relationships changed

Avoid generating unnecessary diagrams for trivial CRUD features.

---

## `/docs-html`

Contains optional human-friendly visual summaries.

Generate HTML documentation ONLY for:
- large workflows
- authentication overview
- architecture overview
- AI system overview

Avoid excessive HTML generation.

Markdown remains the primary AI-readable memory format.

---

# Automatic Documentation Behavior

Before implementation:
1. Read relevant summaries
2. Read relevant architecture docs
3. Read related workflow docs
4. Scan only relevant modules afterward

Avoid scanning the entire repository unless absolutely necessary.

---

# Required Documentation Updates

After IMPORTANT implementations:
- update summaries
- update workflows if business logic changed
- update architecture docs if system structure changed
- update diagrams if architecture/workflow changed significantly

Documentation updates should happen automatically without requiring explicit user prompts.

---

# Important Documentation Rules

## ALWAYS
- keep documentation concise
- keep documentation reusable
- compress reasoning efficiently
- preserve future maintainability
- reuse existing docs before generating new docs
- prefer incremental updates
- prefer semantic organization
- prioritize token efficiency

---

## NEVER
- generate verbose academic documentation
- duplicate architectural explanations
- store trivial implementation details
- rewrite entire documentation unnecessarily
- generate excessive diagrams
- create unnecessary documentation files

---

# AI Context Optimization

The AI MUST optimize context usage.

Prefer:
- feature-level reasoning
- module-level reasoning
- changed-file analysis
- reusable summaries
- focused repository scanning

Avoid:
- full repository analysis
- repeated architecture inference
- unnecessary context loading

---

# Session Compression Workflow

After large implementation sessions:
1. compress implementation knowledge
2. update reusable summaries
3. preserve architectural reasoning
4. preserve reusable workflows
5. preserve future considerations

The repository itself should gradually evolve into a reusable engineering memory system.

---

# Persistent Engineering Memory Philosophy

The goal is NOT:
- storing AI outputs

The goal IS:
- preserving reusable engineering intelligence

The repository should become progressively smarter and more reusable across future AI sessions.

---

# Continuation & Recovery Rules

When an implementation becomes large, complex, or incomplete:

- generate a reusable continuation summary
- preserve implementation progress
- preserve important reasoning
- preserve unfinished tasks
- preserve architecture decisions
- preserve next implementation steps

If context becomes too large or generation becomes unstable:
- generate a continuation prompt
- summarize current implementation state
- summarize pending tasks
- summarize important constraints

Continuation prompts should help future sessions continue work without re-analyzing the entire repository.