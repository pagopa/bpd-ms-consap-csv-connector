package it.gov.pagopa.bpd.consap_csv_connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main class for microservice startup
 */
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class, SessionAutoConfiguration.class})
@ComponentScan(basePackages = {"eu.sia.meda", "it.gov.pagopa.bpd"})
public class RtdCsvConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RtdCsvConnectorApplication.class, args);
	}

}
