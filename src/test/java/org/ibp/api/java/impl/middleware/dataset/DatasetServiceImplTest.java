package org.ibp.api.java.impl.middleware.dataset;

import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.validation.ObjectError;

public class DatasetServiceImplTest {

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyValidator studyValidator;

	@InjectMocks
	private DatasetServiceImpl studyDatasetService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCountPhenotypes() {
		final int studyId = 101;
		final int datasetId = 123;
		final List<Integer> traitIds = Arrays.asList(1, 2, 3);
		this.studyDatasetService.countPhenotypes(studyId, datasetId, traitIds);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypes(datasetId, traitIds);
	}

	@Test
	public void testCountPhenotypesByInstance() {
		final int studyId = 101;
		final int datasetId = 123;
		final int instanceId = 234;
		this.studyDatasetService.countPhenotypesByInstance(studyId, datasetId, instanceId);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypesByInstance(datasetId, instanceId);
	}

}
