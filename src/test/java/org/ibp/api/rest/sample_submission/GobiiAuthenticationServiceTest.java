package org.ibp.api.rest.sample_submission;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by clarysabel on 9/12/18.
 */
@Ignore
public class GobiiAuthenticationServiceTest {

	private GOBiiAuthenticationResource gobiiAuthenticationService;

	@Before
	public void before() {
		gobiiAuthenticationService = new GOBiiAuthenticationResource();
	}

	@Test
	public void authenticateTest() {
		gobiiAuthenticationService.authenticate("USER_READER", "reader");
	}

}
