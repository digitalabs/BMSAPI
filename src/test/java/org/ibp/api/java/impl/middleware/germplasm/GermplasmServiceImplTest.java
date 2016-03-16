
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.List;

import com.google.common.collect.Lists;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GermplasmServiceImplTest {

	private GermplasmServiceImpl germplasmServiceImpl;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private PedigreeService pedigreeService;

	@Mock
	private LocationDataManager locationDataManger;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.germplasmServiceImpl = new GermplasmServiceImpl();
		this.germplasmServiceImpl.setGermplasmDataManager(this.germplasmDataManager);
		this.germplasmServiceImpl.setPedigreeService(this.pedigreeService);
		this.germplasmServiceImpl.setLocationDataManger(this.locationDataManger);
	}

	@Test
	public void testSearchGermplasm() throws MiddlewareQueryException {

		Germplasm gp = new Germplasm();
		gp.setGid(3);
		gp.setGpid1(1);
		gp.setGpid2(2);
		gp.setMethodId(1);
		gp.setLocationId(1);

		List<Germplasm> middlewareSearchResults = Lists.newArrayList(gp);
		middlewareSearchResults.add(null);

		Mockito.when(this.germplasmDataManager.searchForGermplasm("CML", Operation.LIKE, false, false)).thenReturn(middlewareSearchResults);
		String gpPedigree = "CML1/CML2";
		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.anyInt(), Matchers.any(CrossExpansionProperties.class))).thenReturn(
				gpPedigree);

		Name gpName = this.createName(gp.getGid());
		List<Name> gpNames = Lists.newArrayList(gpName);
		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.anyInt(), Matchers.anyInt(), Matchers.any(GermplasmNameType.class)))
				.thenReturn(gpNames);

		UserDefinedField udf = this.createUserDefinedField();
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId())).thenReturn(udf);

		Method gpMethod = this.createMethod();
		Mockito.when(this.germplasmDataManager.getMethodByID(Matchers.anyInt())).thenReturn(gpMethod);

		Location gpLocation = this.createLocation();
		Mockito.when(this.locationDataManger.getLocationByID(Matchers.anyInt())).thenReturn(gpLocation);

		List<GermplasmSummary> germplasmSummaries = this.germplasmServiceImpl.searchGermplasm("CML");
		Assert.assertTrue(!germplasmSummaries.isEmpty());

		Assert.assertEquals(gp.getGid().toString(), germplasmSummaries.get(0).getGermplasmId());
		Assert.assertEquals(gp.getGpid1().toString(), germplasmSummaries.get(0).getParent1Id());
		Assert.assertEquals(gp.getGpid2().toString(), germplasmSummaries.get(0).getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummaries.get(0).getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummaries.get(0).getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummaries.get(0).getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummaries.get(0).getNames().get(0).getName());
	}

	@Test(expected = ApiRuntimeException.class)
	public void testSearchGermplasmThrowsException() throws MiddlewareQueryException {
		Mockito.when(this.germplasmDataManager.searchForGermplasm("CML", Operation.LIKE, false, false)).thenThrow(MiddlewareQueryException.class);

		List<GermplasmSummary> germplasmSummaries = this.germplasmServiceImpl.searchGermplasm("CML");
	}

	@Test
	public void testGetGermplasm(){

		Germplasm germplasm = new Germplasm();
		germplasm.setGid(3);
		germplasm.setGpid1(1);
		germplasm.setGpid2(2);
		germplasm.setMethodId(1);
		germplasm.setLocationId(1);

		Mockito.when(this.germplasmDataManager.getGermplasmByGID(germplasm.getGid())).thenReturn(germplasm);

		String gpPedigree = "CML1/CML2";
		Mockito.when(this.pedigreeService.getCrossExpansion(Matchers.anyInt(), Matchers.any(CrossExpansionProperties.class))).thenReturn(
				gpPedigree);

		Name gpName = this.createName(germplasm.getGid());

		List<Name> gpNames = Lists.newArrayList(gpName);
		Mockito.when(this.germplasmDataManager.getNamesByGID(Matchers.anyInt(), Matchers.anyInt(), Matchers.any(GermplasmNameType.class)))
				.thenReturn(gpNames);

		UserDefinedField udf = this.createUserDefinedField();
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId())).thenReturn(udf);

		Method gpMethod = this.createMethod();
		Mockito.when(this.germplasmDataManager.getMethodByID(Matchers.anyInt())).thenReturn(gpMethod);

		Location gpLocation = this.createLocation();
		Mockito.when(this.locationDataManger.getLocationByID(Matchers.anyInt())).thenReturn(gpLocation);

		GermplasmSummary germplasmSummary = this.germplasmServiceImpl.getGermplasm(String.valueOf(germplasm.getGid()));

		Assert.assertEquals(germplasm.getGid().toString(), germplasmSummary.getGermplasmId());
		Assert.assertEquals(germplasm.getGpid1().toString(), germplasmSummary.getParent1Id());
		Assert.assertEquals(germplasm.getGpid2().toString(), germplasmSummary.getParent2Id());
		Assert.assertEquals(gpPedigree, germplasmSummary.getPedigreeString());
		Assert.assertEquals(gpMethod.getMname(), germplasmSummary.getBreedingMethod());
		Assert.assertEquals(gpLocation.getLname(), germplasmSummary.getLocation());
		Assert.assertEquals(gpName.getNval(), germplasmSummary.getNames().get(0).getName());
	}

	@Test(expected = ApiRuntimeException.class)
	public void testGetGermplasmThrowsException() throws MiddlewareQueryException {
		Integer germplasmId = 1;
		Mockito.when(this.germplasmDataManager.getGermplasmByGID(germplasmId)).thenThrow(MiddlewareQueryException.class);

		GermplasmSummary germplasm = this.germplasmServiceImpl.getGermplasm(String.valueOf(germplasmId));
	}

	private UserDefinedField createUserDefinedField(){
		UserDefinedField udf = new UserDefinedField();
		udf.setFcode("FCode");
		udf.setFname("FName");
		return udf;
	}

	private Method createMethod(){
		Method gpMethod = new Method();
		gpMethod.setMname("Backcross");
		return gpMethod;
	}

	private Location createLocation(){
		Location gpLocation = new Location();
		gpLocation.setLname("Mexico");
		return gpLocation;
	}

	private Name createName(Integer germplasmId){
		Name gpName = new Name();
		gpName.setGermplasmId(germplasmId);
		gpName.setNval("CML1");
		gpName.setTypeId(1);
		return gpName;
	}
}
