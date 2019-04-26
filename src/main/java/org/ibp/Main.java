
package org.ibp;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@EnableSwagger
@Configuration
public class Main extends WebMvcConfigurerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	@Autowired
	private SpringSwaggerConfig springSwaggerConfig;

	@Value("${swagger.enable}")
	private boolean enableSwagger;

	public static void main(final String[] args) {
		SpringApplication.run(Main.class, args);
		Main.LOGGER.info("Startup Complete!");
	}

	@Bean
	public TemplateResolver templateResolver() {
		final ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/html/");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML5");
		templateResolver.setCacheable(false);

		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine templateEngine() {
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(this.templateResolver());
		templateEngine.addDialect(this.securityDialect());
		return templateEngine;
	}

	@Bean
	public SpringSecurityDialect securityDialect() {
		return new SpringSecurityDialect();
	}

	@Bean
	public CropNameValidationInterceptor getCropNameValidationInterceptor() {
		return new CropNameValidationInterceptor();
	}

	@Override
	public void configurePathMatch(final PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {
		registry.addInterceptor(this.getCropNameValidationInterceptor()).addPathPatterns("/**");
	}

	@Bean
	public SwaggerSpringMvcPlugin customImplementation() {
		return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).enable(this.enableSwagger).apiInfo(this.apiInfo());
	}

	@Bean
	public ResourceBundleMessageSource getResourceBundleMessageSource() {
		final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasenames("CommonMessages", "mw_messages_en", "messages_en");
		return resourceBundleMessageSource;
	}

	private ApiInfo apiInfo() {
		return new ApiInfo("Welcome!", "Try out the Breeding Management System API methods listed below!", "http://bit.ly/KQX1nL",
			"naymesh@leafnode.io", "GNU General Public License", "http://bit.ly/8Ztv8M");
	}

	protected void setEnableSwagger(final boolean enableSwagger) {
		this.enableSwagger = enableSwagger;
	}
}
