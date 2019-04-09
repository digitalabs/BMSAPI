package org.ibp.api.rest.labelprinting;

import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SubObservationDatasetLabelPrintingTest {

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyDataManager studyDataManager;

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	@InjectMocks
	private SubObservationDatasetLabelPrinting subObservationDatasetLabelPrinting;


	@Before
	public void setUp() {
		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.subObservationDatasetLabelPrinting.setMessageSource(messageSource);
		this.subObservationDatasetLabelPrinting.initStaticFields();
	}

	@Test
	public void testGetAvailableLabelTypes() {
		final LabelsInfoInput labelsInfoInput = new LabelsInfoInput();
		labelsInfoInput.setStudyId(10);
		final DataSet dataset = new DataSet();
		dataset.setId(5);
		Mockito.when(this.studyDataManager.getDataSetsByType(labelsInfoInput.getStudyId(), DataSetType.SUMMARY_DATA)).thenReturn(
			Arrays.asList(dataset));
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setParentDatasetId(2);
		datasetDTO.setDatasetTypeId(10095);
		Mockito.when(middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId())).thenReturn(datasetDTO);
		final List<LabelType> labelTypes = this.subObservationDatasetLabelPrinting.getAvailableLabelTypes(labelsInfoInput);
		Mockito.verify(middlewareDatasetService).getDataset(labelsInfoInput.getDatasetId());
		Mockito.verify(this.studyDataManager).getDataSetsByType(labelsInfoInput.getStudyId(), DataSetType.SUMMARY_DATA);
		Mockito.verify(this.middlewareDatasetService).getMeasurementVariables(labelsInfoInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.middlewareDatasetService).getMeasurementVariables(dataset.getId(),
			Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
				VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));
		Mockito.verify(this.middlewareDatasetService).getMeasurementVariables(datasetDTO.getParentDatasetId(), Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));
		Mockito.verify(this.middlewareDatasetService).getMeasurementVariables(datasetDTO.getParentDatasetId(),
			Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId()));
		Mockito.verify(this.middlewareDatasetService).getMeasurementVariables(labelsInfoInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId()));
		final String studyDetailsPropValue = this.subObservationDatasetLabelPrinting.getMessage("label.printing.study.details");
		final String datasetDetailsPropValue = this.subObservationDatasetLabelPrinting.getMessage("label.printing.dataset.details");
		Assert.assertEquals(studyDetailsPropValue, labelTypes.get(0).getKey());
		Assert.assertEquals(studyDetailsPropValue, labelTypes.get(0).getTitle());
		Assert.assertEquals(datasetDetailsPropValue, labelTypes.get(1).getKey());
		Assert.assertEquals(datasetDetailsPropValue, labelTypes.get(1).getTitle());
	}

	@Test
	public void testRemovePairIdVariables() {
		final String studyDetailsPropValue = this.subObservationDatasetLabelPrinting.getMessage("label.printing.study.details");
		final LabelType labelType = new LabelType(studyDetailsPropValue, studyDetailsPropValue);
		final List<Field> fields = new ArrayList<>();
		fields.add(new Field(TermId.ENTRY_NO.getId(), TermId.ENTRY_NO.name()));
		fields.add(new Field(SubObservationDatasetLabelPrinting.PAIR_ID_VARIABLES.get(0), "PI_NAME_ID"));
		fields.add(new Field(SubObservationDatasetLabelPrinting.PAIR_ID_VARIABLES.get(1), "COOPERATOR_ID"));
		labelType.setFields(fields);
		Assert.assertEquals(3, labelType.getFields().size());
		this.subObservationDatasetLabelPrinting.removePairIdVariables(Arrays.asList(labelType));
		Assert.assertEquals(1, labelType.getFields().size());
	}

	@Test
	public void testTransform() {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias(TermId.OBS_UNIT_ID.name());
		measurementVariable.setTermId(TermId.OBS_UNIT_ID.getId());
		List<Field> fields = this.subObservationDatasetLabelPrinting.transform(Arrays.asList(measurementVariable));
		Assert.assertEquals(TermId.OBS_UNIT_ID.getId(), fields.get(0).getId().intValue());
		Assert.assertEquals(SubObservationDatasetLabelPrinting.PLOT.concat(" ").concat(measurementVariable.getAlias()), fields.get(0).getName());

		measurementVariable.setAlias(TermId.ENTRY_NO.name());
		measurementVariable.setTermId(TermId.ENTRY_NO.getId());
		fields = this.subObservationDatasetLabelPrinting.transform(Arrays.asList(measurementVariable));
		Assert.assertEquals(TermId.ENTRY_NO.getId(), fields.get(0).getId().intValue());
		Assert.assertEquals(TermId.ENTRY_NO.name(), fields.get(0).getName());
	}
}
