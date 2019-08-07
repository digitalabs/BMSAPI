package org.ibp.api.java.impl.middleware.manager;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class UserValidatorTest {

	private static final int SUPERADMIN_ID = 123;

	private UserValidator uservalidator;

	@Mock
	protected WorkbenchDataManager workbenchDataManager;

	@Mock
	private SecurityService securityService;

	private Role superAdminRole;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.uservalidator = new UserValidator();
		this.uservalidator.setWorkbenchDataManager(this.workbenchDataManager);
		this.uservalidator.setSecurityService(this.securityService);

		// TODO test validate crops
		Mockito.doReturn(null).when(this.workbenchDataManager).getProjectsByUser(ArgumentMatchers.any(WorkbenchUser.class), ArgumentMatchers.any(String.class));
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Should validate all fields empty and with length exceed.* *
	 */
	@Test
	public void testValidateFieldLength() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = new UserDetailDto();

		userDto.setLastName("zxcvbnmaaskjhdfsgeeqwfsafsafg6tk6kglkugt8oljhhlly11");
		userDto.setUsername("wertyuioiuytredsdfrtghjuklsl123");

		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(5, equalTo(bindingResult.getAllErrors().size()));
		assertThat(null, equalTo(bindingResult.getFieldError("firstName").getRejectedValue()));
		assertThat("signup.field.length.exceed", equalTo(bindingResult.getFieldError("lastName").getCode()));
		assertThat("30", equalTo(bindingResult.getFieldError("username").getArguments()[0]));
		assertThat("Username", equalTo(bindingResult.getFieldError("username").getArguments()[1]));
		assertThat(null, equalTo(bindingResult.getFieldError("email").getRejectedValue()));
		//		assertThat(null, equalTo(bindingResult.getFieldError("role").getRejectedValue()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate the Email Format.* *
	 */
	@Test
	public void testValidateEmail() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		//Valid email
		userDto.setEmail("cuenya.diego@leafnode.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

		//Valid email
		userDto.setEmail("cuenya.diego@leafnode.email-valid.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

		//Invalid email
		userDto.setEmail("cuenya.diego!@leafnode.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.invalid", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate the UserId.* *
	 */
	@Test
	public void testValidateUserId() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(777);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the userId inexistent.* *
	 */
	@Test
	public void testValidateUserIdInexistent() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the status allow.* *
	 */
	@Test
	public void testValidateStatus() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(20);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		userDto.setStatus("truee");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.status", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate if username and email exists.* *
	 */
	@Test
	public void testValidateUserAndPeronalEmailExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(2, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void testValidateUpdateUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Should validate update user with diferent Email* *
	 */
	@Test
	public void testValidateUpdateUserEmailExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		user.getPerson().setEmail("user@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));

	}

	/**
	 * Should validate update user with diferent username* *
	 */
	@Test
	public void testValidateUpdateUserUsernameExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);
		user.setName("Nahuel");

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));

	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void testValidateUpdateUserForExistingSuperAdminUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);
		user.setRoles(Arrays.asList(new UserRole(user, new Role(5, Role.SUPERADMIN))));

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);
		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat(UserValidator.CANNOT_UPDATE_SUPERADMIN, equalTo(bindingResult.getAllErrors().get(0).getCode()));

	}

	/**
	 * Should validate create user.* *
	 */
	@Test
	public void testValidateCreateUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0);

		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	@Test
	public void testValidateUserCannotAutoDeactivate() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		userDto.setStatus("false");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, not(equalTo(bindingResult.getGlobalErrorCount())));
		assertThat(UserValidator.USER_AUTO_DEACTIVATION, equalTo(bindingResult.getGlobalErrors().get(0).getCode()));
	}

	private List<Role> createTestRoles() {
		final List<Role> allRoles = new ArrayList<>();
		final Role admin = new Role(1, "ADMIN");
		final Role breeder = new Role(2, "BREEDER");
		final Role technician = new Role(3, "TECHNICIAN");
		this.superAdminRole = new Role(SUPERADMIN_ID, Role.SUPERADMIN);

		allRoles.add(admin);
		allRoles.add(breeder);
		allRoles.add(technician);
		allRoles.add(this.superAdminRole);

		return allRoles;
	}
}
