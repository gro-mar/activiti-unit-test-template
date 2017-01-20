package org.activiti;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MyUnitTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();

	@Test
	@Deployment(resources = {"org/activiti/test/my-process.bpmn20.xml"})
	public void test() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assignee", "newAssignee");
		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process",
				variables);
		assertNotNull(processInstance);

		Task task = activitiRule.getTaskService().createTaskQuery().singleResult();
		assertEquals("doc for newAssignee", task.getDescription());
	}

}
