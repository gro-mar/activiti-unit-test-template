package org.activiti.examples.flowable6.designbydoing;

/**
 * This class represents a relationship between two {@link Node}s.
 */
public class Link {
    private Node sourceNode;
    private Node targetNode;

    private int count;

    public Link(Node sourceNode, Node targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.count = 1;
    }

    public void incrementCount() {
        this.count++;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (sourceNode != null ? !sourceNode.equals(link.sourceNode) : link.sourceNode != null) return false;
        return targetNode != null ? targetNode.equals(link.targetNode) : link.targetNode == null;

    }

    @Override
    public int hashCode() {
        int result = sourceNode != null ? sourceNode.hashCode() : 0;
        result = 31 * result + (targetNode != null ? targetNode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Link{" +
                "targetNode=" + targetNode +
                ", count=" + count +
                '}';
    }
}
