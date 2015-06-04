
package org.ibp.api.java.impl.middleware.common.validator;

import java.util.Objects;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProgramValidator extends BaseValidator implements Validator {

	private static final String PROGRAM_DOES_NOT_EXIST = "program.does.not.exist";

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramValidator.class);

	@Autowired
	public WorkbenchDataManager workbenchDataManager;

	@Override
	public boolean supports(Class<?> aClass) {
		return String.class.isAssignableFrom(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		// check for program id should not be null
		this.shouldNotNullOrEmpty("Program", "programId", target, errors);
		if (errors.hasErrors()) {
			return;
		}

		// check if program id is non numeric
		String id = (String) target;

		this.checkIfProgramExist("programId", id, errors);
	}

	protected void checkIfProgramExist(String fieldName, String programUuid, Errors errors) {
		try {
			Project project = this.workbenchDataManager.getProjectByUuid(programUuid);
			if (Objects.equals(project, null)) {
				this.addCustomError(errors, fieldName, ProgramValidator.PROGRAM_DOES_NOT_EXIST, null);
			}
		} catch (MiddlewareException e) {
			ProgramValidator.LOGGER.error("Error occur while fetching program data", e);
		}
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}
}
