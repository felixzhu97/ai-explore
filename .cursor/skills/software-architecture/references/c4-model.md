# C4 Model

A method for software architecture documentation that focuses on different levels of abstraction.

## Overview

C4 Model uses four levels of abstraction:

1. **Context** - High-level view of the system and its users
2. **Container** - Applications and data stores
3. **Component** - Components within a container
4. **Code** - Implementation details (optional)

## The Four Levels

### Level 1: System Context

Shows how software systems relate to each other and users.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Customers                                │
│    ┌──────────────┐              ┌──────────────┐              │
│    │  Corporate   │              │   Mobile     │              │
│    │  Users       │              │   Users      │              │
│    └──────┬───────┘              └──────┬───────┘              │
│           │                             │                       │
└───────────┼─────────────────────────────┼───────────────────────┘
            │                             │
            │    HTTP/REST                │    HTTP/REST
            ▼                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│                    Internet Banking System                       │
│                                                                 │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│   │ Web App     │  │ API App     │  │   Database           │  │
│   │ (React)     │  │ (Java)       │  │   (PostgreSQL)       │  │
│   └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
            │                             │
            ▼                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     External Systems                            │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│   │ Email System│  │ Payment     │  │   Credit Bureau    │   │
│   │             │  │ Gateway     │  │   Service           │   │
│   └─────────────┘  └─────────────┘  └─────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**Questions to answer:**
- What is the system?
- Who uses it?
- What systems does it integrate with?

### Level 2: Container

Shows the high-level technology choices and how responsibilities are distributed.

```
┌─────────────────────────────────────────────────────────────────┐
│                     Internet Banking System                      │
│                                                                 │
│  ┌───────────────────┐  ┌───────────────────┐                   │
│  │    Web App         │  │    API App         │                  │
│  │                    │  │                    │                  │
│  │  Single Page App   │  │  REST API          │                  │
│  │  (React + Redux)    │  │  (Spring Boot)      │                  │
│  │                    │  │                    │                  │
│  │  Port: 8080        │  │  Port: 8081         │                  │
│  └─────────┬──────────┘  └─────────┬──────────┘                  │
│            │                        │                             │
│            │ HTTPS                   │ REST/HTTPS                  │
└────────────┼────────────────────────┼────────────────────────────┘
             │                        │
             ▼                        ▼
┌────────────────────────────────────────────────────────────────┐
│                      Data Stores                                │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │ PostgreSQL      │    │ MongoDB          │                    │
│  │ (Customer Data)  │    │ (Audit Logs)     │                    │
│  └─────────────────┘    └─────────────────┘                    │
└────────────────────────────────────────────────────────────────┘
```

**Questions to answer:**
- What is the application?
- What technology choices were made?
- How do containers communicate?

### Level 3: Component

Shows components within a container and their relationships.

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Application                          │
│                                                                 │
│  ┌──────────────────────────┐  ┌────────────────────────────┐  │
│  │      Controllers          │  │      Services              │  │
│  │                           │  │                            │  │
│  │  ┌────────────────────┐  │  │  ┌──────────────────────┐  │  │
│  │  │ AccountController  │  │  │  │  AccountService       │  │  │
│  │  └────────────────────┘  │  │  └──────────────────────┘  │  │
│  │  ┌────────────────────┐  │  │  ┌──────────────────────┐  │  │
│  │  │ TransactionCtrl   │  │  │  │  TransactionService   │  │  │
│  │  └────────────────────┘  │  │  └──────────────────────┘  │  │
│  │  ┌────────────────────┐  │  │  ┌──────────────────────┐  │  │
│  │  │ CustomerController │  │  │  │  CustomerService     │  │  │
│  │  └────────────────────┘  │  │  └──────────────────────┘  │  │
│  └──────────────────────────┘  └────────────────────────────┘  │
│                  │                        │                       │
│                  └────────┬───────────────┘                       │
│                           ▼                                       │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                        Domain Layer                         │  │
│  │                                                            │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │  │
│  │  │   Account    │  │ Transaction  │  │  Customer    │     │  │
│  │  │   Entity     │  │   Entity     │  │   Entity     │     │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘     │  │
│  │                                                            │  │
│  └────────────────────────────────────────────────────────────┘  │
│                           │                                       │
│                           ▼                                       │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │                    Repositories                             │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │  │
│  │  │ AccountRepo  │  │ Transaction  │  │ CustomerRepo │     │  │
│  │  │ (JPA)        │  │   Repo      │  │   (JPA)      │     │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘     │  │
│  └────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

**Questions to answer:**
- What components exist?
- Where does the logic reside?
- How do components collaborate?

### Level 4: Code

Detailed implementation diagrams (often skipped or auto-generated from IDE).

## PlantUML Templates

### System Context Template

```plantuml
@startuml
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Context.puml

LAYOUT_WITH_LEGEND()

title System Context diagram for Internet Banking System

Person(customer, "Customer", "A customer of the bank")
Person(admin, "Admin", "A bank administrator")
System(banking_system, "Internet Banking System", "Allows customers to view their accounts and make payments")

System_Ext(email, "Email System", "The internal email system")
System_Ext(payment, "Payment Gateway", "Processes online payments")
System_Ext(fax, "Fax System", "The internal fax system")

Rel(customer, banking_system, "Uses")
Rel(admin, banking_system, "Uses")
Rel(banking_system, email, "Sends email via")
Rel(banking_system, payment, "Processes payments via")
Rel(admin, fax, "Sends fax via")
@enduml
```

### Container Template

```plantuml
@startuml
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

LAYOUT_WITH_LEGEND()

title Container diagram for Internet Banking System

Person(customer, "Customer", "A customer of the bank")

Container(web_app, "Web Application", "Java, Spring MVC", "Serves web pages")
Container(mobile_app, "Mobile App", "Xamarin", "Cross-platform mobile app")
Container(api, "API Application", "Java, Spring Boot", "Provides API")
ContainerDb(db, "Database", "PostgreSQL", "Stores data")

System_Ext(email, "Email System", "Sends emails")

Rel(customer, web_app, "Uses")
Rel(customer, mobile_app, "Uses")
Rel(web_app, api, "Calls")
Rel(mobile_app, api, "Calls")
Rel(api, db, "Reads/Writes")
Rel(api, email, "Sends emails")
@enduml
```

## Documentation Best Practices

### Keep diagrams simple
- Use consistent notation
- Avoid showing everything
- Focus on what matters for the audience

### Match audience to level
| Audience | C4 Level |
|----------|----------|
| Business stakeholders | Context |
| Developers, architects | Container + Component |
| Team members implementing | Component + Code |

### Update diagrams with code
- Integrate C4 diagram generation into CI/CD
- Use annotations in code to maintain accuracy
- Review diagrams during code review

### PlantUML Tools
- VS Code extension: PlantUML
- Standalone: PlantUML server
- Online: PlantText PlantUML editor
