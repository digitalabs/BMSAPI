package org.ibp.api.java.role;

import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.ibp.api.domain.role.RoleDto;
import org.ibp.api.rest.role.RoleGeneratorInput;

import java.util.List;
import java.util.Map;

public interface RoleService {

	List<RoleDto> getRoles(RoleSearchDto roleSearchDto);

	Integer createRole(RoleGeneratorInput dto);

	RoleDto getRole(Integer id);

	void updateRole(RoleGeneratorInput roleGeneratorInput);
}
