package org.activiti.examples.flowable6.designbydoing;

/**
 * This class describes and event occured during the ad hoc task execution
 */
public class Event {
    enum Type {
        CREATED,
        UPDATED
    }
    private final Type type;
    private final String taskId;
    private final String assigneeId;
    private final String taskName;

    public Event(Type type, String taskId, String assigneeId, String taskName) {
        this.type = type;
        this.taskId = taskId;
        this.assigneeId = assigneeId;
        this.taskName = taskName;
    }

    public Type getType() {
        return type;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", assigneeId='" + assigneeId + '\'' +
                ", taskName='" + taskName + '\'' +
                '}';
    }
}
