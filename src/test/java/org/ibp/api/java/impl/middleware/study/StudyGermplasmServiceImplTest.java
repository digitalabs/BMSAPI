package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class StudyGermplasmServiceImplTest {

    @Mock
    private StudyValidator studyValidator;

    @Mock
    private PedigreeService pedigreeService;

    @Mock
    private CrossExpansionProperties crossExpansionProperties;

    @Mock
    private StudyGermplasmValidator studyGermplasmValidator;

    @Mock
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

    @Mock
    private DatasetService datasetService;

    @InjectMocks
    private final StudyGermplasmServiceImpl studyGermplasmService = new StudyGermplasmServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReplaceStudyGermplasm() {
        final Random random = new Random();
        final Integer studyId = random.nextInt();
        final Integer entryId = random.nextInt();
        final Integer newGid = random.nextInt();
        final String crossExpansion = RandomStringUtils.randomAlphabetic(20);
        Mockito.doReturn(crossExpansion).when(this.pedigreeService).getCrossExpansion(newGid, this.crossExpansionProperties);
        final StudyGermplasmDto dto = new StudyGermplasmDto();
        dto.setGermplasmId(newGid);
        this.studyGermplasmService.replaceStudyGermplasm(studyId, entryId, dto);
        Mockito.verify(this.studyValidator).validate(studyId, true);
        Mockito.verify(this.studyGermplasmValidator).validate(studyId, entryId, newGid);
        Mockito.verify(this.middlewareStudyGermplasmService).replaceStudyGermplasm(studyId, entryId, newGid, crossExpansion);
    }

    @Test
    public void testGetEntryDescriptorColumns() {
        final Random random = new Random();
        final int studyId = random.nextInt();
        final int datasetId = random.nextInt();
        final DatasetDTO datasetDTO = new DatasetDTO();
        datasetDTO.setDatasetId(datasetId);
        datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLOT_DATA.getId());
        final List<DatasetDTO> datasetDTOS = Collections.singletonList(datasetDTO);
        Mockito.when(this.datasetService.getDatasets(studyId, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
            .thenReturn(datasetDTOS);

        final MeasurementVariable entryCodeVariable = new MeasurementVariable(TermId.ENTRY_CODE.getId());
        final MeasurementVariable observationUnitIdVariable = new MeasurementVariable(TermId.OBS_UNIT_ID.getId());
        final MeasurementVariable entryNoVariable = new MeasurementVariable(TermId.ENTRY_NO.getId());
        final MeasurementVariable designationVariable = new MeasurementVariable(TermId.DESIG.getId());
        final MeasurementVariable stockIdVariable = new MeasurementVariable(TermId.STOCKID.getId());
        final MeasurementVariable crossVariable = new MeasurementVariable(TermId.CROSS.getId());
        final MeasurementVariable gidVariable = new MeasurementVariable(TermId.GID.getId());

        final List<MeasurementVariable> measurementVariables = Lists
            .newArrayList(entryCodeVariable, observationUnitIdVariable, entryNoVariable, designationVariable, stockIdVariable,
                crossVariable, gidVariable);

        Mockito.when(this.datasetService.getObservationSetVariables(datasetId, Lists
            .newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId()))).thenReturn(measurementVariables);

        final List<MeasurementVariable> results = studyGermplasmService.getEntryDescriptorColumns(studyId);

        MatcherAssert.assertThat(results, IsCollectionWithSize.hasSize(8));
        MatcherAssert.assertThat(entryCodeVariable, IsIn.in(results));
        MatcherAssert.assertThat(entryNoVariable, IsIn.in(results));
        MatcherAssert.assertThat(designationVariable, IsIn.in(results));
        MatcherAssert.assertThat(crossVariable, IsIn.in(results));
        MatcherAssert.assertThat(gidVariable, IsIn.in(results));
        MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_UNIT.getId()), IsIn.in(results));
        MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_AVAILABLE_BALANCE.getId()), IsIn.in(results));
        MatcherAssert.assertThat(new MeasurementVariable(TermId.GID_ACTIVE_LOTS_COUNT.getId()), IsIn.in(results));
    }

}
