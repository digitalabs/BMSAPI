package org.ibp.api.java.impl.middleware.inventory.manager;

import factory.ExtendedLotDtoDummyFactory;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotAdjustmentRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionSourceType;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.collection.IsMapContaining;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LotServiceImplTest {

    private static final Integer USER_ID = ThreadLocalRandom.current().nextInt();
    private static final String PROGRAM_UUID = UUID.randomUUID().toString();

    @InjectMocks
    private LotServiceImpl lotService;

    @Mock
    private LotMergeValidator lotMergeValidator;

    @Mock
    private LotSplitValidator lotSplitValidator;

    @Mock
    private ExtendedLotListValidator extendedLotListValidator;

    @Mock
    private LotInputValidator lotInputValidator;

    @Mock
    private SecurityService securityService;

    @Mock
    private org.generationcp.middleware.service.api.inventory.LotService middlewareLotService;

    @Mock
    private TransactionService middlewareTransactionService;

    @Mock
    private ContextUtil contextUtil;

    @Mock
    private StockService stockService;

    @Captor
    private ArgumentCaptor<Set> setCollectionArgumentCaptor;

    @Captor
    private ArgumentCaptor<LotGeneratorInputDto> lotGeneratorInputDtoArgumentCaptor;

    @Captor
    private ArgumentCaptor<LotDepositRequestDto> lotDepositRequestDtoArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldMergeLots() {
        final LotsSearchDto lotsSearchDto = Mockito.mock(LotsSearchDto.class);

        final String keepLotUUID = "keepLotUUID";
        final List<ExtendedLotDto> extendedLotDtos = Arrays.asList(
            ExtendedLotDtoDummyFactory.create(1, keepLotUUID),
            ExtendedLotDtoDummyFactory.create(2, "UUID")
        );

        Mockito.doNothing().when(this.lotMergeValidator).validate(keepLotUUID, extendedLotDtos);
        Mockito.when(this.lotService.searchLots(lotsSearchDto, null)).thenReturn(extendedLotDtos);
        Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

        this.lotService.mergeLots(keepLotUUID, lotsSearchDto);

        Mockito.verify(this.middlewareLotService).mergeLots(USER_ID, 1, lotsSearchDto);
    }

    @Test
    public void shouldSplitLot() {
        final double lotActualBalance = 10D;

        final LotSplitRequestDto lotSplitRequestDto = new LotSplitRequestDto();
        final LotSplitRequestDto.InitialLotDepositDto initialDeposit = new LotSplitRequestDto.InitialLotDepositDto();
        initialDeposit.setAmount(5D);
        initialDeposit.setNotes("Initial deposit notes");
        lotSplitRequestDto.setInitialDeposit(initialDeposit);

        final LotSplitRequestDto.NewLotSplitDto newLotSplitDto = new LotSplitRequestDto.NewLotSplitDto();
        newLotSplitDto.setLocationId(new Random().nextInt());
        newLotSplitDto.setNotes("New lot notes");
        newLotSplitDto.setGenerateStock(true);
        newLotSplitDto.setStockPrefix(UUID.randomUUID().toString());
        lotSplitRequestDto.setNewLot(newLotSplitDto);

        final ExtendedLotDto splitExtendedLotDto = ExtendedLotDtoDummyFactory.create(lotActualBalance);
        Mockito.doNothing().when(this.extendedLotListValidator).validateAllProvidedLotUUIDsExist(ArgumentMatchers.anyList(),
            ArgumentMatchers.anySet());
        Mockito.doNothing().when(this.lotSplitValidator).validateSplitLot(PROGRAM_UUID, splitExtendedLotDto, newLotSplitDto, initialDeposit);

        Mockito.doNothing().when(this.lotInputValidator).validate(ArgumentMatchers.eq(PROGRAM_UUID),
            ArgumentMatchers.any(LotGeneratorInputDto.class));

        Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(new WorkbenchUser(USER_ID));

        final Project project = new Project();
        project.setCropType(new CropType(CropType.CropEnum.BEAN.name()));
        Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);

        Mockito.when(this.stockService.calculateNextStockIDPrefix(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(UUID.randomUUID().toString());

        final String newLotUUID = UUID.randomUUID().toString();
        Mockito.when(this.middlewareLotService.saveLot(ArgumentMatchers.eq(project.getCropType()), ArgumentMatchers.eq(USER_ID), ArgumentMatchers.any(LotGeneratorInputDto.class))).
            thenReturn(newLotUUID);

        Mockito.doNothing().when(this.middlewareTransactionService).saveAdjustmentTransactions(ArgumentMatchers.eq(USER_ID),
            ArgumentMatchers.any(Set.class), ArgumentMatchers.eq(5D), ArgumentMatchers.isNull());

        final ExtendedLotDto newSplitExtendedLotDto = ExtendedLotDtoDummyFactory.create(lotActualBalance);
        Mockito.when(this.lotService.searchLots(ArgumentMatchers.any(LotsSearchDto.class), ArgumentMatchers.isNull()))
            .thenReturn(Arrays.asList(splitExtendedLotDto))
            .thenReturn(Arrays.asList(newSplitExtendedLotDto));

        Mockito.doNothing().when(this.middlewareTransactionService).depositLots(ArgumentMatchers.eq(USER_ID),
            ArgumentMatchers.anySet(), ArgumentMatchers.any(LotDepositRequestDto.class), ArgumentMatchers.eq(TransactionStatus.CONFIRMED),
            ArgumentMatchers.eq(TransactionSourceType.SPLIT_LOT), ArgumentMatchers.eq(newSplitExtendedLotDto.getLotId()));

        this.lotService.splitLot(PROGRAM_UUID, lotSplitRequestDto);

        Mockito.verify(this.lotSplitValidator).validateSplitLot(PROGRAM_UUID, splitExtendedLotDto, newLotSplitDto, initialDeposit);
        Mockito.verify(this.middlewareLotService).saveLot(ArgumentMatchers.eq(project.getCropType()),
            ArgumentMatchers.eq(USER_ID),
            this.lotGeneratorInputDtoArgumentCaptor.capture());
        LotGeneratorInputDto actualLotGeneratorInputDto = this.lotGeneratorInputDtoArgumentCaptor.getValue();
        assertNotNull(actualLotGeneratorInputDto);
        assertThat(actualLotGeneratorInputDto.getGid(), is(splitExtendedLotDto.getGid()));
        assertThat(actualLotGeneratorInputDto.getUnitId(), is(splitExtendedLotDto.getUnitId()));
        assertThat(actualLotGeneratorInputDto.getLocationId(), is(newLotSplitDto.getLocationId()));
        assertThat(actualLotGeneratorInputDto.getNotes(), is(newLotSplitDto.getNotes()));
        assertThat(actualLotGeneratorInputDto.getGenerateStock(), is(newLotSplitDto.getGenerateStock()));
        assertThat(actualLotGeneratorInputDto.getStockPrefix(), is(newLotSplitDto.getStockPrefix()));

        Mockito.verify(this.middlewareTransactionService).saveAdjustmentTransactions(ArgumentMatchers.eq(USER_ID),
            this.setCollectionArgumentCaptor.capture(), ArgumentMatchers.eq(5D), ArgumentMatchers.isNull());
        final Set<Integer> saveAdjustmentTransactionLotIds = this.setCollectionArgumentCaptor.getValue();
        assertNotNull(saveAdjustmentTransactionLotIds);
        assertThat(saveAdjustmentTransactionLotIds, hasSize(1));
        assertThat(saveAdjustmentTransactionLotIds, contains(splitExtendedLotDto.getLotId()));

        Mockito.verify(this.middlewareTransactionService).depositLots(ArgumentMatchers.eq(USER_ID),
            this.setCollectionArgumentCaptor.capture(),
            this.lotDepositRequestDtoArgumentCaptor.capture(),
            ArgumentMatchers.eq(TransactionStatus.CONFIRMED),
            ArgumentMatchers.eq(TransactionSourceType.SPLIT_LOT),
            ArgumentMatchers.eq(splitExtendedLotDto.getLotId()));
        LotDepositRequestDto actualLotDepositRequestDto = this.lotDepositRequestDtoArgumentCaptor.getValue();
        assertNotNull(actualLotDepositRequestDto);
        assertThat(actualLotDepositRequestDto.getDepositsPerUnit(), IsMapContaining.hasKey(splitExtendedLotDto.getUnitName()));
        assertThat(actualLotDepositRequestDto.getDepositsPerUnit(), IsMapContaining.hasValue(initialDeposit.getAmount()));
        assertThat(actualLotDepositRequestDto.getNotes(), is(initialDeposit.getNotes()));

        final Set<Integer> depositLotLotIds = this.setCollectionArgumentCaptor.getValue();
        assertNotNull(depositLotLotIds);
        assertThat(depositLotLotIds, hasSize(1));
        assertThat(depositLotLotIds, contains(newSplitExtendedLotDto.getLotId()));
    }

}
