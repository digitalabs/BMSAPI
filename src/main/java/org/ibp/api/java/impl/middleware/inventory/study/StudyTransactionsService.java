package org.ibp.api.java.impl.middleware.inventory.study;

import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface StudyTransactionsService {

	long countStudyTransactions(Integer studyId, StudyTransactionsRequest studyTransactionsRequest);

	List<StudyTransactionsDto> searchStudyTransactions(Integer studyId, StudyTransactionsRequest studyTransactionsRequest, PageRequest pageRequest);

	void cancelPendingTransactions(Integer studyId, SearchCompositeDto<Integer, Integer> searchCompositeDto);

}
