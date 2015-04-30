
package org.ibp.api.java.impl.middleware.common.validator;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.java.impl.middleware.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CropNameValidationInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
	private RequestInformationProvider requestInformationProvider;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		final Map<String, String> uriTemplateVars = requestInformationProvider.getUrlTemplateAttributes();

		if (uriTemplateVars.containsKey(Constants.CROPNAME_URI_PARAM)) {
			String cropName = uriTemplateVars.get(Constants.CROPNAME_URI_PARAM);
			ErrorResponse errorResponse = null;
			try {
				CropType cropType = workbenchDataManager.getCropTypeByName(cropName);
				if (cropType == null) {
					errorResponse = new ErrorResponse("error", "Invalid crop name path parameter: " + cropName);
				}
			} catch (MiddlewareException e) {
				errorResponse = new ErrorResponse("error", "Error while validating crop name path parameter: " + e.getMessage());
			}

			if (errorResponse != null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("application/json");
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(response.getOutputStream(), errorResponse);
				return false; // abort handling
			}			
		}
		return true; // continue handling
	}

	class ErrorResponse {

		private String status;
		private String message;

		public ErrorResponse(String status, String message) {
			this.status = status;
			this.message = message;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	
	protected void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	
	protected void setRequestInformationProvider(RequestInformationProvider requestInformationProvider) {
		this.requestInformationProvider = requestInformationProvider;
	}
}
