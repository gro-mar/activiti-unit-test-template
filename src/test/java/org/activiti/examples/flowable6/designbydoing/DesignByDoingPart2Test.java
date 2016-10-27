package org.activiti.examples.flowable6.designbydoing;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class DesignByDoingPart2Test {

    private static final int TRASH_HOLD = 5;
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();
    private TestActivitiEntityEventListener listener;
    private int counter = 0;

    @Before
    public void setUp() throws Exception {
        listener = new TestActivitiEntityEventListener(Task.class);
        ((ProcessEngineConfigurationImpl) this.activitiRule.getProcessEngine().getProcessEngineConfiguration()).getEventDispatcher().addEventListener(listener);
    }

    @After
    public void tearDown() throws Exception {
        if (listener != null) {
            ((ProcessEngineConfigurationImpl) this.activitiRule.getProcessEngine().getProcessEngineConfiguration()).getEventDispatcher().removeEventListener(listener);
        }
    }

    /**
     * The test describes the case when how participants can share tasks.
     */
    @Test
    public void step1FormalizeTasks() throws IOException {
        // Fred received an e-mail with letter of resignation. It means he has to create the first task
        // with the name 'Make a record' assigned to him
        Task newTask = getTaskService().newTask();
        newTask.setName("Make a record");
        newTask.setAssignee("fred");
        getTaskService().saveTask(newTask);
        assertLogTaskNameAndAssignee(Event.Type.CREATED, newTask.getId(), "fred", "Make a record");

        // Fred can add a comment for John that the record was made and John can continue in the task
        reassignTaskWithComment(newTask, "john", "Create inventory list", "The record was made. John, could you create an inventory list please?");
        assertLogTaskNameAndAssignee(Event.Type.UPDATED, newTask.getId(), "john", "Create inventory list");

        // John adds his part.
        // add inventory list attachment to the task.
        try (InputStream inventoryListContent = new ByteArrayInputStream(new String(
                "one notebook with 50 pages, 3 pencils, one chair, one desk, one lamp"
        ).getBytes(StandardCharsets.UTF_8))) {
            getTaskService().createAttachment(
                    "inventoryList",
                    newTask.getId(),
                    null,
                    "InventoryList_for_resignation",
                    "This is a list of inventory for resignation",
                    inventoryListContent);
        }
        // add a comment that inventory list is ready and Bill can collect inventory from employee.
        reassignTaskWithComment(newTask, "bill", "Collect inventory", "The record was made. John, could you create an inventory list please?");
        assertLogTaskNameAndAssignee(Event.Type.UPDATED, newTask.getId(), "bill", "Collect inventory");

        // Bill collects the inventory and can complete task.
        getTaskService().complete(newTask.getId());
        assertLogTaskNameAndAssignee(Event.Type.UPDATED, newTask.getId(), "bill", "Collect inventory");
    }

    @Test
    public void step2GenerateDataAndExtractProcessModel() throws IOException {
        // Arrange - run processes several times to obtain some data in the listener
        generateData();

        // Act
        // identify start nodes and initialize whole path from them
        Collection<Node> startNodes = findAllPaths(this.listener.getEventsReceived());
        Collection<Process> processModels = extractAllProcesses(startNodes);

        // Assert
        String processXml = assertGetProcessXml(processModels);
        assertThat(processXml, containsString(
                "<process id=\"processId\" isExecutable=\"true\">\n" +
                        "    <startEvent id=\"start\"></startEvent>\n" +
                        "    <sequenceFlow id=\"toUserTask0\" sourceRef=\"start\" targetRef=\"userTask0\"></sequenceFlow>\n" +
                        "    <userTask id=\"userTask0\" name=\"Make a record\" activiti:assignee=\"fred\"></userTask>\n" +
                        "    <sequenceFlow id=\"toUserTask1\" sourceRef=\"userTask0\" targetRef=\"userTask1\"></sequenceFlow>\n" +
                        "    <userTask id=\"userTask1\" name=\"Create inventory list\" activiti:assignee=\"john\"></userTask>\n" +
                        "    <sequenceFlow id=\"toUserTask2\" sourceRef=\"userTask1\" targetRef=\"userTask2\"></sequenceFlow>\n" +
                        "    <userTask id=\"userTask2\" name=\"Collect inventory\" activiti:assignee=\"bill\"></userTask>\n" +
                        "    <sequenceFlow id=\"toEnd\" sourceRef=\"userTask2\" targetRef=\"end\"></sequenceFlow>\n" +
                        "    <endEvent id=\"end\"></endEvent>\n" +
                        "  </process>\n"
                )
        );
    }

    private String assertGetProcessXml(Collection<Process> processModels) throws IOException {
        assertThat(processModels.size(), is(1));
        Process processModel = (Process) processModels.toArray()[0];
        Deployment deployment = deployModelWithProcess(processModel);
        try (InputStream in = this.activitiRule.getRepositoryService().getResourceAsStream(deployment.getId(), "designByDoing-model.bpmn")) {
            return IOUtils.toString(in);
        }
    }

    private Deployment deployModelWithProcess(Process process) {
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return activitiRule.getRepositoryService().createDeployment()
                .addBpmnModel("designByDoing-model.bpmn", model).name("DesignByDoing process deployment")
                .deploy();
    }

    private Collection<Process> extractAllProcesses(Collection<Node> startNodes) {
        Collection<Process> processModels = new ArrayList<>();
        for (Node startNode : startNodes) {
            if (startNode.getCount() > TRASH_HOLD) {
                processModels.add(createProcessFor(startNode));
            }
        }
        return processModels;
    }

    private Process createProcessFor(Node startNode) {
        Process designByDoingProcess = new Process();
        designByDoingProcess.setId("processId");
        designByDoingProcess.addFlowElement(createStartEvent());

        createNextProcessFlowElement("start", startNode, designByDoingProcess);

        designByDoingProcess.addFlowElement(createSequenceFlow("toEnd", "userTask" + this.counter, "end"));
        designByDoingProcess.addFlowElement(createEndEvent());
        return designByDoingProcess;
    }

    private void createNextProcessFlowElement(String startFlowElementId, Node nextNode, Process designByDoingProcess) {
        String nextUserTaskId = "userTask" + this.counter;
        designByDoingProcess.addFlowElement(createSequenceFlow("toUserTask" + this.counter, startFlowElementId, nextUserTaskId));
        designByDoingProcess.addFlowElement(createUserTask("userTask" + this.counter, nextNode.getName(), nextNode.getAssigneeId()));
        for (Link link : nextNode.getLinks()) {
            if (link.getCount() > TRASH_HOLD) {
                this.counter++;
                createNextProcessFlowElement(nextUserTaskId, link.getTargetNode(), designByDoingProcess);
            }
        }
    }

    private UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    private SequenceFlow createSequenceFlow(String id, String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    private StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    private EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }

    private Collection<Node> findAllPaths(List<Event> eventsReceived) {
        Map<String, Node> startNodes = new HashMap<>();
        for (int i = 0; i < eventsReceived.size(); i++) {
            if (eventsReceived.get(i).getType().equals(Event.Type.CREATED)) {
                Event leftEvent = eventsReceived.get(i);
                if (startNodes.keySet().contains(leftEvent.getAssigneeId())) {
                    startNodes.get(leftEvent.getAssigneeId()).incrementCount();
                } else {
                    Node newNode = new Node(leftEvent.getAssigneeId(), leftEvent.getTaskName());
                    startNodes.put(leftEvent.getAssigneeId(), newNode);
                }
                Node leftNode = startNodes.get(leftEvent.getAssigneeId());

                findNextNode(eventsReceived, i, leftEvent, leftNode);
            }
        }
        return startNodes.values();
    }

    private void findNextNode(List<Event> eventsReceived, int i, Event leftEvent, Node leftNode) {
        for (int j = i + 1; j < eventsReceived.size(); j++) {
            if (eventsReceived.get(j).getType().equals(Event.Type.UPDATED)) {
                Event rightEvent = eventsReceived.get(j);
                if (rightEvent.getTaskId().equals(leftEvent.getTaskId())) {
                    Node rightNode = new Node(rightEvent.getAssigneeId(), rightEvent.getTaskName());
                    Link linkToRight = leftNode.addLinkTo(rightNode);
                    findNextNode(eventsReceived, j, rightEvent, linkToRight.getTargetNode());
                    return;
                }
            }
        }
    }

    /**
     * We try to simulate behaviour of the "real" company many different processes and project tasks are running
     * concurrently.
     * In our case we have only 2 processes which are executed. Resignation process occurs more frequently.
     *
     * @throws IOException
     */
    private void generateData() throws IOException {
        executeResignationProcess();
        executeResignationProcess();
        executeResignationProcess();
        executeAnotherProcess();
        executeAnotherProcess();
        executeResignationProcess();
        executeResignationProcess();
        executeAnotherProcess();
        executeAnotherProcess();
        executeResignationProcess();
        executeResignationProcess();
        executeResignationProcess();
    }

    private void executeResignationProcess() throws IOException {
        Task newTask = getTaskService().newTask();
        newTask.setName("Make a record");
        newTask.setAssignee("fred");
        getTaskService().saveTask(newTask);

        reassignTaskWithComment(newTask, "john", "Create inventory list", "The record was made. John, could you create an inventory list please?");

        try (InputStream inventoryListContent = new ByteArrayInputStream(new String(
                "one notebook with 50 pages, 3 pencils, one chair, one desk, one lamp"
        ).getBytes(StandardCharsets.UTF_8))) {
            getTaskService().createAttachment(
                    "inventoryList",
                    newTask.getId(),
                    null,
                    "InventoryList_for_resignation",
                    "This is a list of inventory for resignation",
                    inventoryListContent);
        }

        reassignTaskWithComment(newTask, "bill", "Collect inventory", "The record was made. John, could you create an inventory list please?");

        // Bill collects the inventory and can complete task.
        getTaskService().complete(newTask.getId());
    }

    private void executeAnotherProcess() throws IOException {
        Task newTask = getTaskService().newTask();
        newTask.setName("The first task");
        newTask.setAssignee("jane");
        getTaskService().saveTask(newTask);

        reassignTaskWithComment(newTask, "ann", "The second task", "The first task was done. Ann, could you complete process please?");

        getTaskService().complete(newTask.getId());
    }

    private void reassignTaskWithComment(Task newTask, String newAssignee, String newTaskName, String comment) {
        getTaskService().addComment(newTask.getId(), null, comment);
        newTask.setName(newTaskName);
        newTask.setAssignee(newAssignee);
        getTaskService().saveTask(newTask);
    }

    private void assertLogTaskNameAndAssignee(Event.Type eventType, String taskId, String assigneeId, String taskName) {
        List<Event> eventsReceived = this.listener.getEventsReceived();
        for (Event event : eventsReceived) {
            if (event.getType().equals(eventType)) {
                assertThat(event.getTaskId(), is(taskId));
                assertThat(event.getTaskName(), is(taskName));
                assertThat(event.getAssigneeId(), is(assigneeId));
            }
        }
        this.listener.clearEvents();
    }

    private TaskService getTaskService() {
        return this.activitiRule.getTaskService();
    }

}
