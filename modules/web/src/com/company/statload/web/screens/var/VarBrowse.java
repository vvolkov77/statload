package com.company.statload.web.screens.var;

import com.company.statload.service.DBStatSyncService;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Var;

import javax.inject.Inject;
import javax.swing.*;

@UiController("statload_Var.browse")
@UiDescriptor("var-browse.xml")
@LookupComponent("varsTable")
@LoadDataBeforeShow
public class VarBrowse extends StandardLookup<Var> {
    @Inject
    private DBStatSyncService SyncService;
    @Inject
    private Dialogs dialogs;
    @Inject
    private Notifications notifications;

    @Install(to = "varsTable.Sync", subject = "enabledRule")
    private boolean varsTableSyncEnabledRule() {
        return true;
    }

    @Subscribe("varsTableSyncBtn")
    public void onVarsTableSyncBtnClick(Button.ClickEvent event) {
        String status = SyncService.Sync();
        dialogs.createMessageDialog().withCaption("Результат сервиса обновления справочников").withMessage(status).show();

    }

    @Subscribe("varsTable.FullSync")
    public void onVarsTableFullSync(com.haulmont.cuba.gui.components.Action.ActionPerformedEvent event) {
        // Спросить уверен ли чел
        String status="";
        status = SyncService.FullSyncStatForm();
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(status)
                .show();
        status = SyncService.FullSyncStatPokaz();
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(status)
                .show();
        status = SyncService.FullSyncStatSprav();
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(status)
                .show();
        status = SyncService.FullSyncStatBanks();
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(status)
                .show();
        //String status = SyncService.FullSync();

    }
}