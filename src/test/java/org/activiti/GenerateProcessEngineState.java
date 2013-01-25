package org.activiti;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * generate basic process engine state on which simulation is performed 
 *
 */
public class GenerateProcessEngineState {
	
	protected static String TEMP_DIR;
	
	public static void main(String[] args) throws InterruptedException {
		TEMP_DIR = args[0];
		if (TEMP_DIR == null)
			TEMP_DIR = "target";
		
		generateBasicEngine();
		
	}

	private static void generateBasicEngine()
			throws InterruptedException {
		String PROCESS_KEY = "process1";
		RepositoryService repositoryService;
		RuntimeService runtimeService;
		TaskService taskService;
		IdentityService identityService;
		ProcessEngine processEngine;
		String liveDB = TEMP_DIR + "/liveDB";
		
		// delete previous DB instalation and set property to point to the current file
		File prevDB = new File(liveDB+".h2.db");
				
		System.setProperty("liveDB", liveDB);
		if ( prevDB.exists() )
			prevDB.delete();
		prevDB = null;

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("live-engine.xml");
		
		repositoryService = appContext.getBean( RepositoryService.class );
		runtimeService = appContext.getBean( RuntimeService.class );
		taskService = appContext.getBean( TaskService.class );
		identityService = appContext.getBean( IdentityService.class );
		processEngine = appContext.getBean( ProcessEngine.class);
		
		// deploy processes
		repositoryService.createDeployment()
	       .addClasspathResource("org/activiti/test/MyProcess.bpmn")
	       .deploy();

		// init identity service
		identityService.saveGroup( identityService.newGroup("Group1") );
		identityService.saveGroup( identityService.newGroup("Group2") );
		identityService.saveUser( identityService.newUser("user1") );
		identityService.saveUser( identityService.newUser("user2") );
		
		identityService.createMembership("user1", "Group1");
		identityService.createMembership("user2", "Group2");
		
		// start processes
		
		for (int i = 0; i < 10; i++) {			
			runtimeService.startProcessInstanceByKey( PROCESS_KEY, "BUSINESS-KEY-" + i, null);
		}
		
		processEngine.close();
		
		appContext.close();
		
	}

}
