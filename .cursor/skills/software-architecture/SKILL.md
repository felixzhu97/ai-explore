---
name: software-architecture
description: Software Architecture Design Methodology Guide. Covers Hexagonal Architecture, DDD, Clean Architecture, Event-Driven, Microservices patterns, C4 Model, and ADRs — with detailed references for each topic.
version: "1.0"
lastUpdated: "2026-06-20"
---

# Software Architecture

## When to Use

Use this skill when designing system architecture, making technology choices, reviewing designs, or structuring a project. It covers Hexagonal Architecture, Domain-Driven Design, Event-Driven patterns, Microservices patterns, C4 documentation, Architecture Decision Records, and anti-patterns to avoid.

> **Note**: Hexagonal Architecture and Clean Architecture are synonymous. This project uses "Hexagonal Architecture" terminology.

## Quick Reference

| Topic | File |
|-------|------|
| **Architecture Principles** | |
| SOLID Principles | `./references/solid-principles.md` |
| Hexagonal Architecture Deep Dive | `./references/hexagonal-architecture-deep-dive.md` |
| DDD Strategic Design | `./references/ddd-strategic-design.md` |
| **Architecture Styles** | |
| Hexagonal Architecture | `./references/hexagonal-architecture.md` |
| Event-Driven Architecture | `./references/event-driven-architecture.md` |
| Microservices Patterns | `./references/microservices-patterns.md` |
| **Architecture Governance** | |
| Architecture Decision Records | `./references/architecture-decision-records.md` |
| Architecture Review Checklist | `./references/architecture-review-checklist.md` |
| Architecture Anti-Patterns | `./references/antipatterns.md` |
| **Architecture Patterns** | |
| Architecture Patterns (index) | `./references/patterns.md` |
| Entity Pattern | `./references/entity-pattern.md` |
| Value Object Pattern | `./references/value-object-pattern.md` |
| Aggregate Pattern | `./references/aggregate-pattern.md` |
| Repository Pattern | `./references/repository-pattern.md` |
| Domain Service Pattern | `./references/domain-service-pattern.md` |
| Domain Event Pattern | `./references/domain-event-pattern.md` |
| Event Sourcing Pattern | `./references/event-sourcing-pattern.md` |
| CQRS Pattern | `./references/cqrs-pattern.md` |
| Saga Pattern | `./references/saga-pattern.md` |
| Rich vs Anemic Model | `./references/rich-vs-anemic-model.md` |
| **C4 Model Documentation** | |
| C4 Four Levels | `./references/c4-four-levels.md` |
| C4 PlantUML Templates | `./references/c4-plantuml-templates.md` |
| C4 Documentation Best Practices | `./references/c4-documentation-best-practices.md` |
| **Resources** | |
| Books | `./references/books.md` |
| Online Resources | `./references/online-resources.md` |

## How to Use This Skill

Browse the Quick Reference table above to find the topic you need. Each reference file contains detailed explanations, diagrams, Bad/Good code examples, real implementation references, and links to related topics. Start with the principles (SOLID, Hexagonal Architecture) for foundational understanding, then drill into specific patterns or styles as needed.

## Related Skills

- [Software Development](../software-development/SKILL.md) - day-to-day craft: Clean Code (naming, functions, comments, errors, formatting, tests) and Hexagonal Architecture (Domain / Application / adapter/out / adapter/in layers) with Bad/Good examples and references to the real `apps/server/` Java module.
- [Hexagonal Architecture](../hexagonal-architecture/SKILL.md) - Refactoring guide for Ports-and-Adapters pattern
