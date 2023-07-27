package edu.upc.essi.dtim.odin;

import edu.upc.essi.dtim.odin.bootstrapping.SourceController;
import edu.upc.essi.dtim.odin.bootstrapping.SourceService;
import edu.upc.essi.dtim.odin.project.ProjectController;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OdinApplicationTests {
	private static final Logger logger = LoggerFactory.getLogger(OdinApplicationTests.class);

	@Autowired
	private ProjectController projectController;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SourceController sourceController;
	@Autowired
	private SourceService sourceService;

	@Test
	void contextLoads() {
		OdinApplication.main(new String[]{});
		logger.info("Application started. Ready to receive API requests.");

		assertNotNull(OdinApplication.class);
		assertNotNull(projectController);
		assertNotNull(projectService);
		assertNotNull(sourceController);
		assertNotNull(sourceService);
	}

}
