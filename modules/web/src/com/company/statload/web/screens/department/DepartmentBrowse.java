package com.company.statload.web.screens.department;

import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Department;

@UiController("statload_Department.browse")
@UiDescriptor("department-browse.xml")
@LookupComponent("departmentsTable")
@LoadDataBeforeShow
public class DepartmentBrowse extends StandardLookup<Department> {
}