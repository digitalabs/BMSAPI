package org.ibp.api.java.impl.middleware.design.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.ibp.api.domain.design.DesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseService;
import org.ibp.api.java.design.runner.ProcessRunner;
import org.ibp.api.rest.design.BVDesignProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@ConditionalOnProperty(
	value = "design.runner.license.service",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.BVDesignLicenseService")
public class BVDesignLicenseService implements DesignLicenseService {

	public static final String LICENSE_DATE_FORMAT = "dd-MMM-yyyy";
	public static final String LICENSE_SUCCESS_CODE = "0";
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignLicenseService.class);

	public static final String BVDESIGN_STATUS_OUTPUT_FILENAME = "son";

	@Resource
	private BVDesignProperties bvDesignProperties;

	@Resource
	private MessageSource messageSource;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ProcessRunner bvDesignLicenseProcessRunner = new BVDesignLicenseProcessRunner();

	@Override
	public boolean isExpired() {

		try {
			final DesignLicenseInfo designLicenseInfo = this.retrieveLicenseInfo();
			final Format formatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);
			final Date expiryDate = (Date) formatter.parseObject(designLicenseInfo.getStatus().getLicense().getExpiry());
			final Date currentDate = DateUtil.getCurrentDateWithZeroTime();
			if (currentDate.compareTo(expiryDate) > 0) {
				return true;
			}
		} catch (final ParseException e) {
			BVDesignLicenseService.LOG.error(e.getMessage(), e);
		}
		return false;
	}

	private DesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {

		final String bvDesignLocation = this.bvDesignProperties.getBvDesignPath();

		this.generateBVDesignLicenseJsonFile(bvDesignLocation);

		final String jsonPathFile = new File(bvDesignLocation).getParent() + File.separator + BVDESIGN_STATUS_OUTPUT_FILENAME;

		return this.readLicenseInfoFromJsonFile(new File(jsonPathFile));
	}

	private DesignLicenseInfo readLicenseInfoFromJsonFile(final File file) throws BVLicenseParseException {

		DesignLicenseInfo designLicenseInfo;

		try {

			designLicenseInfo = objectMapper.readValue(file, DesignLicenseInfo.class);

		} catch (final IOException e) {

			final String errorMessage =
				this.messageSource.getMessage("bv.design.error.cannot.read.license.file", null, LocaleContextHolder.getLocale());
			BVDesignLicenseService.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

		if (!LICENSE_SUCCESS_CODE.equals(designLicenseInfo.getStatus().getReturnCode())) {
			final String errorMessage = this.messageSource.getMessage("bv.design.error.generic", null, LocaleContextHolder.getLocale());
			throw new BVLicenseParseException(errorMessage + designLicenseInfo.getStatus().getAppStatus());
		}

		return designLicenseInfo;

	}

	private void generateBVDesignLicenseJsonFile(final String bvDesignLocation) throws BVLicenseParseException {

		try {

			final String bvDesignDirectory = new File(bvDesignLocation).getParent();
			bvDesignLicenseProcessRunner.setDirectory(bvDesignDirectory);
			bvDesignLicenseProcessRunner.run(bvDesignLocation, "-status", "-json");

		} catch (final Exception e) {
			final String errorMessage =
				this.messageSource.getMessage("bv.design.error.failed.license.generation", null, LocaleContextHolder.getLocale());
			BVDesignLicenseService.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

	}

	public void setBvDesignLicenseProcessRunner(final BVDesignLicenseProcessRunner bvDesignLicenseProcessRunner) {
		this.bvDesignLicenseProcessRunner = bvDesignLicenseProcessRunner;
	}

	@Override
	public Integer getExpiryDays() {
		final DesignLicenseInfo designLicenseInfo = this.retrieveLicenseInfo();
		return Integer.parseInt(designLicenseInfo.getStatus().getLicense().getExpiryDays());
	}

	class BVDesignLicenseProcessRunner implements ProcessRunner {

		private String bvDesignDirectory = "";

		@Override
		public Integer run(final String... command) throws IOException {

			final Integer statusCode = -1;

			Process p = null;
			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(new File(bvDesignDirectory));
			p = processBuilder.start();
			try {
				return p.waitFor();
			} catch (final InterruptedException e) {
				BVDesignLicenseService.LOG.error(e.getMessage(), e);
			}

			return statusCode;
		}

		@Override
		public void setDirectory(final String directory) {
			this.bvDesignDirectory = directory;
		}

	}

}
