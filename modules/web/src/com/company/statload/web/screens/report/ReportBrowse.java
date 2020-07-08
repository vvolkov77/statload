package com.company.statload.web.screens.report;

import com.company.statload.entity.Department;
import com.company.statload.service.LoadFileService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogActions;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogOutcome;
import com.haulmont.cuba.gui.app.core.inputdialog.InputDialog;
import com.haulmont.cuba.gui.app.core.inputdialog.InputParameter;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Report;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import javafx.scene.text.Text;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@UiController("statload_Report.browse")
@UiDescriptor("report-browse.xml")
@LookupComponent("reportsTable")
@LoadDataBeforeShow
public class ReportBrowse extends StandardLookup<Report> {
    @Inject
    private CollectionContainer<Report> reportsDc;
    @Inject
    private FileUploadingAPI fileUploadingAPI;
    @Inject
    private Screens screens;
    @Inject
    private DataManager dataManager;
    @Inject
    private LoadFileService loadFileSvc;
    @Inject
    private Dialogs dialogs;
    @Inject
    private Notifications notifications;


    @Install(to = "reportsTable.loadvar", subject = "enabledRule")
    private boolean reportsTableLoadvarEnabledRule() {
        try {
            Report s = reportsDc.getItem();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Install(to = "reportsTable.loadreport", subject = "enabledRule")
    private boolean reportsTableLoadreportEnabledRule() {
        try {
            Report s = reportsDc.getItem();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Subscribe("reportsTableLoadvarBtn")
    public void onReportsTableLoadvarBtnClick(Button.ClickEvent event) {
        FileUploadDialog dialog = (FileUploadDialog) screens.create("fileUploadDialog", OpenMode.DIALOG);
        dialog.setCaption("Загрузка файла шаблона отчета");

        dialog.addCloseWithCommitListener(() -> {
            UUID fileId = dialog.getFileId();
            String fileName = dialog.getFileName();

            File file = fileUploadingAPI.getFile(fileId);

            FileDescriptor fileDescriptor = fileUploadingAPI.getFileDescriptor(fileId, fileName);
            try {
                // Ограничение объема файла в переменной проекта maxUploadSizeMb
                fileUploadingAPI.putFileIntoStorage(fileId, fileDescriptor);
                dataManager.commit(fileDescriptor);
            } catch (FileStorageException e) {
                throw new RuntimeException(e);
            }

            // Загружаем новые
            try {
                loadFileSvc.loadtemplate(reportsDc.getItem(), fileDescriptor);
            } catch (FileStorageException e) {
                e.printStackTrace();
            }

        });
        screens.show(dialog);
    }

    @Install(to = "inputDialogFacet", target = Target.COMPONENT, subject = "dialogResultHandler")
    private void inputDialogFacetDialogResultHandler(InputDialog.InputDialogResult inputDialogResult) {
        //dialogs.createMessageDialog().withCaption("Information").withMessage(inputDialogResult.getCloseAction().toString()+" - " + DialogAction.Type.OK).show();
        if (inputDialogResult.closedWith(DialogOutcome.OK)){
            FileUploadDialog dialog = (FileUploadDialog) screens.create("fileUploadDialog", OpenMode.DIALOG);
            dialog.setCaption("Загрузка файла отчета в БД Статистика");
            dialog.addCloseWithCommitListener(() -> {
                UUID fileId = dialog.getFileId();
                String fileName = dialog.getFileName();

                File file = fileUploadingAPI.getFile(fileId);

                FileDescriptor fileDescriptor = fileUploadingAPI.getFileDescriptor(fileId, fileName);
                try {
                    // Ограничение объема файла в переменной проекта maxUploadSizeMb
                    fileUploadingAPI.putFileIntoStorage(fileId, fileDescriptor);
                    dataManager.commit(fileDescriptor);
                } catch (FileStorageException e) {
                    throw new RuntimeException(e);
                }
                // Проверяем на обязательность заполнения полей
                 Date dt = inputDialogResult.getValue("dateParam");

                Department dep= inputDialogResult.getValue("depParam");
                    Integer dep_id = dep.getStat_bank_id();
                    String region = dep.getRegion();

                Boolean zo= inputDialogResult.getValue("zoParam");
                 if (reportsDc.getItem().getVid().getId()==3)
                     loadFileSvc.expbalstat(dt,dep_id,region, zo ? "1" : " ", reportsDc.getItem(), fileDescriptor);
                 else
                     loadFileSvc.expstat(dt,dep_id,region, zo ? "1" : " ", reportsDc.getItem(), fileDescriptor);

            });
            screens.show(dialog);
        }
    }

    @Install(to = "reportsTable.loadmaping", subject = "enabledRule")
    private boolean reportsTableLoadmapingEnabledRule() {
        try {
            Report s = reportsDc.getItem();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Subscribe("reportsTable.loadmaping")
    public void onReportsTableLoadmaping(Action.ActionPerformedEvent event) {
        FileUploadDialog dialog = (FileUploadDialog) screens.create("fileUploadDialog", OpenMode.DIALOG);
        dialog.setCaption("Загрузка файла мапинга показателей отчета");

        dialog.addCloseWithCommitListener(() -> {
            UUID fileId = dialog.getFileId();
            String fileName = dialog.getFileName();

            File file = fileUploadingAPI.getFile(fileId);

            FileDescriptor fileDescriptor = fileUploadingAPI.getFileDescriptor(fileId, fileName);
            try {
                // Ограничение объема файла в переменной проекта maxUploadSizeMb
                fileUploadingAPI.putFileIntoStorage(fileId, fileDescriptor);
                dataManager.commit(fileDescriptor);
            } catch (FileStorageException e) {
                throw new RuntimeException(e);
            }

            // Загружаем новые
            try {
                loadFileSvc.loadmaping(reportsDc.getItem(), fileDescriptor);
            } catch (FileStorageException e) {
                e.printStackTrace();
            }

        });
        screens.show(dialog);

    }

    @Install(to = "reportsTable.copystat", subject = "enabledRule")
    private boolean reportsTableCopystatEnabledRule() {
        try {
            Report s = reportsDc.getItem();
            return true;
        } catch (Exception e){
            return false;
        }
    }


    @Install(to = "inputDialogCopyStat", target = Target.COMPONENT, subject = "dialogResultHandler")
    private void inputDialogCopyStatDialogResultHandler(InputDialog.InputDialogResult inputDialogResult) {
        if (inputDialogResult.closedWith(DialogOutcome.OK)){

            Date dt1 = inputDialogResult.getValue("dateParam1");
            Date dt2 = inputDialogResult.getValue("dateParam2");
            if (dt1.equals(dt2)) throw new Error("Даты совпадают");
            int c1=loadFileSvc.checkstat(dt1,reportsDc.getItem());
            int c2=loadFileSvc.checkstat(dt2,reportsDc.getItem());
            if (c1!=0&&c2==0) {
                loadFileSvc.copystat(dt1, dt2, reportsDc.getItem());
                notifications.create()
                        .withCaption("Отчет скопирован с отражением идентификаторов показателей")
                        .show();
            } else {
                if (c1==0) throw new Error("Отчет за "+dt1 +" не найден в Статистике.Проверьте наличие отчета в Статистике и повторите операцию.");
                if (c2!=0) {
                    dialogs.createOptionDialog()
                            .withCaption("Внимание!")
                            .withMessage("В Статистике уже есть данные отчетов за дату "+dt2.toString()+". Вы действительно хотите скопировать отчет на эту дату с удалением старого?")
                            .withActions(
                                    new DialogAction(DialogAction.Type.OK).withHandler(e -> {
                                        loadFileSvc.copystat(dt1, dt2, reportsDc.getItem());
                                        notifications.create()
                                                .withCaption("Отчет скопирован с отражением идентификаторов показателей")
                                                .show();
                                    }),
                                    new DialogAction(DialogAction.Type.CANCEL).withHandler(e -> {}
                                    )
                            )

                            .show();
                }

            }
        }
    }

    @Install(to = "reportsTable.loadmapidstat", subject = "enabledRule")
    private boolean reportsTableLoadmapidstatEnabledRule() {
        try {
            Report s = reportsDc.getItem();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    @Subscribe("reportsTable.loadmapidstat")
    public void onReportsTableLoadmapidstat(Action.ActionPerformedEvent event) {
        FileUploadDialog dialog = (FileUploadDialog) screens.create("fileUploadDialog", OpenMode.DIALOG);
        dialog.setCaption("Загрузка файла мапинга идентификаторов Статистики");

        dialog.addCloseWithCommitListener(() -> {
            UUID fileId = dialog.getFileId();
            String fileName = dialog.getFileName();

            File file = fileUploadingAPI.getFile(fileId);

            FileDescriptor fileDescriptor = fileUploadingAPI.getFileDescriptor(fileId, fileName);
            try {
                // Ограничение объема файла в переменной проекта maxUploadSizeMb
                fileUploadingAPI.putFileIntoStorage(fileId, fileDescriptor);
                dataManager.commit(fileDescriptor);
            } catch (FileStorageException e) {
                throw new RuntimeException(e);
            }

            // Загружаем новые
            loadFileSvc.expstatidpokaz(reportsDc.getItem(), fileDescriptor);

        });
        screens.show(dialog);
    }


}