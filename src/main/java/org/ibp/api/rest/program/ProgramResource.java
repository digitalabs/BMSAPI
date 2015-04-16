package org.ibp.api.rest.program;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.api.domain.program.ProjectBasicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/program")
public class ProgramResource {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<ProjectBasicInfo> listPrograms() throws MiddlewareException {
		List<Project> projectList = this.workbenchDataManager.getProjects();
		List<ProjectBasicInfo> projectBasicInfoList = new ArrayList<>();

		if (!projectList.isEmpty()) {
			for (Project project : projectList) {
				projectBasicInfoList.add(new ProjectBasicInfo(project));
			}
		}
		return projectBasicInfoList;
	}
}
