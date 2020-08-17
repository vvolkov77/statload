package com.company.statload.web.screens.report;

import com.company.statload.service.LoadFileService;
import com.google.common.collect.Sets;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.*;
import com.company.statload.entity.Report;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@UiController("statload_loadstat.browse")
@UiDescriptor("report-loadstat-dialog.xml")
@LookupComponent("reportsTable")
@LoadDataBeforeShow
public class LoadStat extends StandardLookup<Report> {
    @Inject
    private FileMultiUploadField multiUploadField;
    @Inject
    private FileUploadingAPI fileUploadingAPI;
    @Inject
    private Notifications notifications;
    @Inject
    private DataManager dataManager;
    @Inject
    private LoadFileService loadFileSvc;
    @Inject
    private CollectionContainer<Report> reportsDc;
    @Inject
    private DateField dateParam;
    @Inject
    private CheckBox zoParam;
    @Inject
    private Dialogs dialogs;

    @Subscribe
    public void onInit(InitEvent event) {
        multiUploadField.setPermittedExtensions(Sets.newHashSet(".xlsx"));
        multiUploadField.addQueueUploadCompleteListener(queueUploadCompleteEvent -> {

                        for (Map.Entry<UUID, String> entry : multiUploadField.getUploadsMap().entrySet()) {
                            UUID fileId = entry.getKey();
                            String fileName = entry.getValue();
                            FileDescriptor fd = fileUploadingAPI.getFileDescriptor(fileId, fileName);
                            try {
                                fileUploadingAPI.putFileIntoStorage(fileId, fd);
                            } catch (FileStorageException e) {
                                multiUploadField.clearUploads();
                                throw new java.lang.Error("Ошибка сохранения во временное серверное хранилище файлов", e);
                            }
                            dataManager.commit(fd);
                            if (dateParam.isEmpty()) {
                                notifications.create(Notifications.NotificationType.ERROR)
                                        .withCaption("Ошибка!!! \n Отсутствует дата отчета.")
                                        .show();
                                multiUploadField.clearUploads();
                               return;
                            }
                            String depcode = fd.getName().substring(0,3);
                            int depStatId = loadFileSvc.getDepartmentStatId(depcode);
                            String regStatId = loadFileSvc.getDepartmentRegion(depcode);
                            try {
                                if (reportsDc.getItem().getVid().getId()==3)
                                   loadFileSvc.expbalstat((Date) dateParam.getValue(), depStatId, regStatId,
                                        zoParam.isChecked() ? "1" : " ", reportsDc.getItem(), fd);
                                else
                                  loadFileSvc.expstat((Date) dateParam.getValue(), depStatId, regStatId,
                                        zoParam.isChecked() ? "1" : " ", reportsDc.getItem(), fd);
                            } catch(Exception e){
                                multiUploadField.clearUploads();
                                throw new java.lang.Error("Ошибка загрузки файла "+fileName+".",e);
                            }

                        }

                  notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption("Загружены файлы: " + multiUploadField.getUploadsMap().values())
                        .show();

               multiUploadField.clearUploads();

        });

       /* multiUploadField.addFileUploadErrorListener(queueFileUploadErrorEvent -> {
            notifications.create()
                    .withCaption("Ошибка загрузки файла во временное хранилище файлов!")
                    .show();
        });*/
    }

    @Subscribe(id = "reportsDc", target = Target.DATA_CONTAINER)
    public void onReportsDcItemChange(InstanceContainer.ItemChangeEvent<Report> event) {
        multiUploadField.setEnabled(true);
    }


}