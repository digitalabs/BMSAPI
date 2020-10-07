package factory;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.pojos.ims.LotStatus;

import java.util.Random;
import java.util.UUID;

public class ExtendedLotDtoDummyFactory {

	public static final Integer DEFAULT_LOT_ID = new Random().nextInt();
	public static final String DEFAULT_UUID = UUID.randomUUID().toString();
	public static final Integer DEFAULT_GID = new Random().nextInt();
	public static final LotStatus DEFAULT_STATUS = LotStatus.ACTIVE;
	public static final Integer DEFAULT_UNIT_ID = new Random().nextInt();
	public static final double DEFAULT_ACTUAL_BALANCE = 10D;

	public static ExtendedLotDto create() {
		return create(DEFAULT_ACTUAL_BALANCE);
	}

	public static ExtendedLotDto create(final double actualBalance) {
		return create(DEFAULT_LOT_ID, DEFAULT_UUID, actualBalance);
	}

	public static ExtendedLotDto create(final Integer lotId, final String UUID) {
		return create(lotId, UUID, DEFAULT_GID, DEFAULT_STATUS, "unitName", 1, 5D);
	}

	public static ExtendedLotDto create(final Integer lotId, final String UUID, final double actualBalance) {
		return create(lotId, UUID, DEFAULT_GID, DEFAULT_STATUS, "unitName", 1, actualBalance);
	}

	public static ExtendedLotDto create(final Integer gid, final LotStatus lotStatus, final String unitName) {
		return create(DEFAULT_LOT_ID, DEFAULT_UUID, gid, lotStatus, unitName, DEFAULT_UNIT_ID, 5D);
	}

	public static ExtendedLotDto create(final Integer gid, final String UUID, final LotStatus lotStatus, final String unitName) {
		return create(DEFAULT_LOT_ID, UUID, gid, lotStatus, unitName, DEFAULT_UNIT_ID, 5D);
	}

	public static ExtendedLotDto create(final Integer lotId, final String UUID, final Integer gid, final LotStatus status, final String unitName,
		final Integer unitId, final double actualBalance) {
		final ExtendedLotDto lotDto = new ExtendedLotDto();
		lotDto.setLotId(lotId);
		lotDto.setLotUUID(UUID);
		lotDto.setGid(gid);
		lotDto.setStatus(status.name());
		lotDto.setUnitName(unitName);
		lotDto.setUnitId(unitId);
		lotDto.setActualBalance(actualBalance);
		return lotDto;
	}


}
