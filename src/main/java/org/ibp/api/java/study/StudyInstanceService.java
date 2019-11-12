package org.ibp.api.java.study;

import org.ibp.api.domain.study.StudyInstance;

import java.util.List;
import java.util.Optional;

public interface StudyInstanceService {

	StudyInstance createStudyInstance(final String cropName, final int studyId);

	List<StudyInstance> getStudyInstances(int studyId);

}
