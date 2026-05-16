package edu.cit.camoro.peertayo.notification.entity;

/**
 * Categorises every notification so preferences can be checked before delivery.
 */
public enum NotificationType {
    /** Sent to evaluators when a facilitator creates an evaluation and assigns them. */
    EVALUATION_ASSIGNED,

    /** Sent to evaluators 48 h and 24 h before an evaluation deadline. */
    DEADLINE_REMINDER,

    /** Sent to evaluatees when their results become available. */
    RESULTS_PUBLISHED,

    /** Sent to the facilitator when they successfully create or update a form. */
    FORM_CREATED,

    /** Sent to the facilitator when an evaluator submits a response. */
    SUBMISSION_RECEIVED,

    /** Platform-wide announcements. */
    SYSTEM,

    /** Sent to facilitator when deadline expires with 0 submissions (BR-004). */
    ZERO_SUBMISSIONS,

    /** Sent to evaluators when a deadline is extended (BR-004). */
    DEADLINE_EXTENDED,

    /** Sent to new users upon successful registration. */
    WELCOME
}
