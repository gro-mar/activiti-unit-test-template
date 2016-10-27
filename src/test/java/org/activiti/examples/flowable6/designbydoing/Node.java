package org.activiti.examples.flowable6.designbydoing;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents Node information. Node is a candidate for the user task when we are trying to
 * identify tasks in the process flow.
 */
public class Node {
    private final String assigneeId;
    private final String name;
    private int count;
    private List<Link> links;

    public Node(String assigneeId, String name) {
        this.assigneeId = assigneeId;
        this.name = name;
        this.links = new ArrayList<>();
        this.count = 1;
    }

    public Link addLinkTo(Node targetNode) {
        for (Link link : links) {
            if (link.getSourceNode().equals(this) && link.getTargetNode().equals(targetNode)) {
                link.incrementCount();
                link.getTargetNode().incrementCount();
                return link;
            }
        }
        Link link = new Link(this, targetNode);
        this.links.add(link);
        return link;
    }

    public void incrementCount() {
        this.count++;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public List<Link> getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (assigneeId != null ? !assigneeId.equals(node.assigneeId) : node.assigneeId != null) return false;
        return name != null ? name.equals(node.name) : node.name == null;

    }

    @Override
    public int hashCode() {
        int result = assigneeId != null ? assigneeId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "assigneeId='" + assigneeId + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                ", links=" + links +
                '}';
    }
}
