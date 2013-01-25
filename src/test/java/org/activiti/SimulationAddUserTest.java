package org.activiti;

import java.io.File;
import java.io.IOException;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SimulationAddUserTest {
	
	protected static final String tempDir = System.getProperty("tempDir", "target");
	protected static final String LIVE_DB = tempDir +"/liveDB";
	protected static final String SIM_DB = tempDir +"/simDB";
	
	@Before
	public void before() throws Exception {
        System.setProperty("liveDB", LIVE_DB);
        System.setProperty("_SIM_DB_PATH", tempDir+"/simDB");
        
        FileUtils.copyFile( new File(LIVE_DB+".h2.db"), new File(SIM_DB+".h2.db"));
	}

	@After
	public void after() {
		// delete database file
		File f = new File( SIM_DB+".h2.db");
		f.delete();
    }

	@Test
	public void testOK() throws IOException {

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("sim-engine.xml");
        ProcessEngine processEngine = (ProcessEngine) appContext.getBean("simProcessEngine");
        
        IdentityService identityService = (IdentityService) appContext.getBean("simIdentityService");
        identityService.newUser("user5");
        identityService.createMembership("user5", "Group2");
        
        
        processEngine.close();        
        appContext.close();        
	}

}
