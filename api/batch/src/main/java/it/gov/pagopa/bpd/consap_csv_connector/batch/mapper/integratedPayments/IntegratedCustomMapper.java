package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.RowMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset.RowSet;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;

import java.math.BigDecimal;

public class IntegratedCustomMapper implements RowMapper<InboundIntegratedPayments> {

    @Override
    public InboundIntegratedPayments mapRow(RowSet rs) throws Exception {

        InboundIntegratedPayments inboundIntegratedPayments = new InboundIntegratedPayments();
        String[] row = rs.getCurrentRow();
        inboundIntegratedPayments.setFiscalCode(row[0]);
        inboundIntegratedPayments.setAwardPeriodId(new Long(row[1]));
        inboundIntegratedPayments.setTicketId(new Long(row[2]));
        inboundIntegratedPayments.setRelatedPaymentId(Long.parseLong(!row[3].toString().equals("null") ? row[3] : "0"));
        inboundIntegratedPayments.setAmount(new BigDecimal((row[4]).replace(",", ".")));
        inboundIntegratedPayments.setCashbackAmount(new BigDecimal((row[5]).replace(",", ".")));
        inboundIntegratedPayments.setJackpotAmount(new BigDecimal((row[6]).replace(",", ".")));
        inboundIntegratedPayments.setFilename("CONSAP_BANKT_FLUSSI_INTEGRATIVI.xslx");
        return inboundIntegratedPayments;
    }


}
