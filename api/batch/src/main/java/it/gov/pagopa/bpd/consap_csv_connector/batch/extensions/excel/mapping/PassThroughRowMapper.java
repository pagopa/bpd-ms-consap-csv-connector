package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.mapping;

import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.RowMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi.PoiItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset.RowSet;

public class PassThroughRowMapper implements RowMapper<String[]> {

    @Override
    public String[] mapRow(final RowSet rs) throws Exception {
        return rs.getCurrentRow();
    }

}
