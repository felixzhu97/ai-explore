package com.ai.agents.domain;

import java.time.Instant;
import java.util.*;

/**
 * Incident value object for AIOps operations.
 * Represents a system incident with severity and status.
 */
public final class Incident {
    private final IncidentId id;
    private final String title;
    private final String description;
    private final Severity severity;
    private final IncidentStatus status;
    private final List<String> affectedSystems;
    private final Map<String, String> labels;
    private final List<TimelineEvent> timeline;
    private final Instant createdAt;
    private final Instant lastUpdatedAt;

    private Incident(
            IncidentId id,
            String title,
            String description,
            Severity severity,
            IncidentStatus status,
            List<String> affectedSystems,
            Map<String, String> labels,
            List<TimelineEvent> timeline,
            Instant createdAt,
            Instant lastUpdatedAt
    ) {
        this.id = Objects.requireNonNull(id, "IncidentId cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description != null ? description : "";
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.affectedSystems = affectedSystems != null ? List.copyOf(affectedSystems) : List.of();
        this.labels = labels != null ? Map.copyOf(labels) : Map.of();
        this.timeline = timeline != null ? new ArrayList<>(timeline) : new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.lastUpdatedAt = Objects.requireNonNull(lastUpdatedAt, "LastUpdatedAt cannot be null");
    }

    public static Incident create(String title, String description, Severity severity, List<String> affectedSystems) {
        Instant now = Instant.now();
        return new Incident(
                IncidentId.generate(),
                title,
                description,
                severity,
                IncidentStatus.OPEN,
                affectedSystems,
                Map.of(),
                List.of(TimelineEvent.created(now)),
                now,
                now
        );
    }

    public Incident updateStatus(IncidentStatus newStatus, String message) {
        Instant now = Instant.now();
        List<TimelineEvent> newTimeline = new ArrayList<>(timeline);
        newTimeline.add(TimelineEvent.statusChange(newStatus, message, now));
        return new Incident(
                id, title, description, severity, newStatus,
                affectedSystems, labels, newTimeline, createdAt, now
        );
    }

    public Incident addTimelineEvent(String eventType, String message) {
        List<TimelineEvent> newTimeline = new ArrayList<>(timeline);
        newTimeline.add(new TimelineEvent(eventType, message, Instant.now()));
        return new Incident(
                id, title, description, severity, status,
                affectedSystems, labels, newTimeline, createdAt, Instant.now()
        );
    }

    public Incident addLabel(String key, String value) {
        Map<String, String> newLabels = new HashMap<>(labels);
        newLabels.put(key, value);
        return new Incident(
                id, title, description, severity, status,
                affectedSystems, newLabels, timeline, createdAt, Instant.now()
        );
    }

    public Incident escalate(Severity newSeverity) {
        return new Incident(
                id, title, description, newSeverity, status,
                affectedSystems, labels, timeline, createdAt, Instant.now()
        );
    }

    public Incident resolve(String resolution) {
        return addTimelineEvent("resolved", resolution)
                .updateStatus(IncidentStatus.RESOLVED, resolution);
    }

    public boolean isOpen() { return status == IncidentStatus.OPEN || status == IncidentStatus.INVESTIGATING; }
    public boolean isResolved() { return status == IncidentStatus.RESOLVED; }
    public boolean isCritical() { return severity == Severity.CRITICAL; }

    public IncidentId id() { return id; }
    public String idValue() { return id.value(); }
    public String title() { return title; }
    public String description() { return description; }
    public Severity severity() { return severity; }
    public IncidentStatus status() { return status; }
    public List<String> affectedSystems() { return affectedSystems; }
    public Map<String, String> labels() { return labels; }
    public List<TimelineEvent> timeline() { return List.copyOf(timeline); }
    public Instant createdAt() { return createdAt; }
    public Instant lastUpdatedAt() { return lastUpdatedAt; }

    public enum Severity {
        CRITICAL, WARNING, INFO
    }

    public enum IncidentStatus {
        OPEN, INVESTIGATING, MITIGATED, RESOLVED, CLOSED
    }

    public record TimelineEvent(String eventType, String message, Instant timestamp) {
        public static TimelineEvent created(Instant timestamp) {
            return new TimelineEvent("created", "Incident created", timestamp);
        }
        public static TimelineEvent statusChange(IncidentStatus status, String message, Instant timestamp) {
            return new TimelineEvent("status_change", status.name() + ": " + message, timestamp);
        }
    }

    private static final class IncidentId {
        private final String value;

        private IncidentId(String value) { this.value = value; }

        static IncidentId generate() {
            return new IncidentId("inc_" + UUID.randomUUID().toString().substring(0, 8));
        }

        String value() { return value; }
    }
}
