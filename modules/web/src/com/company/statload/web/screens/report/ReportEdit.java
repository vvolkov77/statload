package com.company.statload.web.screens.report;

import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Report;

@UiController("statload_Report.edit")
@UiDescriptor("report-edit.xml")
@EditedEntityContainer("reportDc")
@LoadDataBeforeShow
public class ReportEdit extends StandardEditor<Report> {
}