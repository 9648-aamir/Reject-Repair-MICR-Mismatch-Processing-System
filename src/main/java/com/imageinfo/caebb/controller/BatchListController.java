package com.imageinfo.caebb.controller;

import java.text.DecimalFormat;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.imageinfo.caebb.dao.BatchDao;
import com.imageinfo.caebb.model.Batch;

@SuppressWarnings("serial")
public class BatchListController extends SelectorComposer<Window> {
    private transient BatchDao db = new BatchDao();

    @Wire
    private Rows batchRows;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        refresh();
    }

    @Listen("onClick=#backBtn")
    public void back() {
        Executions.sendRedirect("/process_selection.zul");
    }

    private void refresh() {
        try {
            Session sess = Sessions.getCurrent();
            String processType = (String) sess.getAttribute("processType");
            String clearingType = (String) sess.getAttribute("clearingType");

            List<Batch> batches = db.fetchBatches(processType, clearingType);
            
            DecimalFormat df = new DecimalFormat("##,##,##,##,##0.00");
            batchRows.getChildren().clear();
            for (Batch b : batches) {
                Row r = new Row();
                r.appendChild(new Label(b.getBatchNumber()));
                r.appendChild(new Label(String.valueOf(b.getChequeCount())));
                r.appendChild(new Label(String.valueOf(df.format(b.getAmount()))));
                r.appendChild(new Label(String.valueOf(b.getPendingCount())));
                r.appendChild(new Label(String.valueOf(b.getProcessedCount())));
                r.appendChild(new Label(b.getStatus()));
                r.appendChild(new Label(b.getUserId()));

               Batch scanStats = db.getBatchScanCounts();
               b.setScanCount(scanStats.getScanCount());
               b.setBatchCount(scanStats.getBatchCount());
               b.setDifference(scanStats.getDifference());
               
                r.addEventListener("onClick", evt -> {
                	Sessions.getCurrent().setAttribute("selectedBatch", b);
                    Executions.sendRedirect("cheque_entry.zul");
                });

               
                batchRows.appendChild(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
}
