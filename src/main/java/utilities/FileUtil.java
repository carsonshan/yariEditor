/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */
package utilities;

import com.thoughtworks.xstream.XStream;
import components.dialog.AlertDialogType;
import components.dialog.NonActionableAlertDialog;
import javafx.beans.property.*;
import javafx.print.*;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yari.core.table.DecisionTable;
import table.DecisionTableService;
import validation.ValidationService;
import validation.ValidationType;
import view.RootLayoutFactory;
import view.TablePrintView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Contains several file-management utilities such as save, open, and new files.
 */
public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final ObjectProperty<File> CURRENT_FILE = new SimpleObjectProperty<>(null);
    private static final BooleanProperty DIRTY = new SimpleBooleanProperty(false);
    private static final ValidationService VALID_SVC = ValidationService.getService();
    private static final DecisionTableService DECISION_TBL_SVC = DecisionTableService.getService();
    private static XStream xStream;

    /**
     * Private constructor to hide the implicit public constructor.
     */
    private FileUtil() {

    }

    /**
     * Show a file chooser and attempt to open the selected file.
     *
     * @param stage the stage to base the file chooser on.
     * @param busy
     * @return
     */
    public static boolean openDecisionTableFile(Stage stage, BooleanProperty busy) {
        FileChooser fileChooser = new FileChooser();

        // Set the extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show save file dialog
        File file = fileChooser.showOpenDialog(stage); //must be on fx thread
        if (file == null) {
            LOGGER.debug("A file was not chosen. Aborting open.");
            return false;
        }
        openDecisionTableFile(file, stage, busy);
        return true;
    }

    public static void openDecisionTableFile(File file, Stage stage, BooleanProperty busy) {
        FXUtil.runAsync(() -> {
            if (!SettingsUtil.getSettings().getValidationType().equals(ValidationType.DISABLED)) {
                ValidationService.getService().setEnabled(false);
            }

            //start simplified logic
            //load the file
            DECISION_TBL_SVC.clearData();
            DecisionTable decisionTable = loadObjectFromFile(DecisionTable.class, file.getPath());
            if (decisionTable == null) {
                LOGGER.warn("DecisionTable failed to load.");
                if (RootLayoutFactory.isDisplayed()) {
                    ToastUtil.sendPersistentToast("Failed to load the file. Is it a decision table file?");
                } else {
                    FXUtil.runOnFXThread(() -> {
                        Window owner = null;
                        if (stage != null){
                            owner = stage.getOwner();
                        }
                        NonActionableAlertDialog alert = new NonActionableAlertDialog(AlertDialogType.ERROR, owner);
                        alert.setTitle("Failed To Load");
                        alert.setBody("Failed to load decision table data from the file. Ensure the file is formatted " +
                                "as a decision table.");
                        alert.show();
                    });
                }
                if (busy != null) {
                    busy.setValue(false);
                }
                if (!SettingsUtil.getSettings().getValidationType().equals(ValidationType.DISABLED)) {
                    ValidationService.getService().setEnabled(true);
                }
                newFile();
                return;
            }

            //load data into our own lists
            DECISION_TBL_SVC.setDecisionTable(decisionTable);
            DECISION_TBL_SVC.updateFXListsFromTable();

            //ensure data is valid
            VALID_SVC.runValidationImmediately();
            if (!VALID_SVC.isValid()) {
                LOGGER.debug("The table that was loaded is not valid.");
                DECISION_TBL_SVC.clearData();
                if (busy != null) {
                    busy.setValue(false);
                }
                if (!SettingsUtil.getSettings().getValidationType().equals(ValidationType.DISABLED)) {
                    ValidationService.getService().setEnabled(true);
                }
                if (RootLayoutFactory.isDisplayed()) {
                    ToastUtil.sendPersistentToast("Could not load table data from file. The table from the file is was invalid.");
                } else {
                    FXUtil.runOnFXThread(() -> {
                        NonActionableAlertDialog alert = new NonActionableAlertDialog(AlertDialogType.ERROR, stage.getOwner());
                        alert.setTitle("Failed To Load");
                        alert.setBody("Failed to load decision table data from the file. The table in the file is invalid.");
                        alert.show();
                    });
                }
                newFile();
                return;
            }

            setDirty(false);
            CURRENT_FILE.setValue(file);
            SettingsUtil.addRecommendedFile(file);
            if (!SettingsUtil.getSettings().getValidationType().equals(ValidationType.DISABLED)) {
                ValidationService.getService().setEnabled(true);
            }

            if (!RootLayoutFactory.isDisplayed()) {
                FXUtil.runOnFXThread(() -> RootLayoutFactory.show(stage));
            }
        });
    }

    /**
     * Create a new file.
     */
    public static void newFile() {
        CURRENT_FILE.setValue(null);
        DECISION_TBL_SVC.clearData();
        DecisionTable decisionTable = new DecisionTable();
        decisionTable.setDescription("MyTable Description");
        decisionTable.setName("MyTable");
        DECISION_TBL_SVC.setDecisionTable(decisionTable);
        setDirty(false);
    }

    /**
     * Load an object of type T from file located at the supplied path value.
     * May return null if the load failed.
     *
     * @param type the class type the object is expected to be.
     * @param path the path of the file to attempt load from.
     * @param <T> the type of file to return.
     * @return object of type T loaded from file.
     */
    public static <T extends Object> T loadObjectFromFile(Class<T> type, String path) {

        T loadedObj = null;

        try (FileReader reader = new FileReader(path)) {
            loadedObj = (T) getXStream().fromXML(reader);
        } catch (Exception ex) {
            LOGGER.error("Failed to load file from path: " + path + ", of type " + type.getSimpleName() + ".", ex);
        }

        return loadedObj;

    }

    /**
     * Save the supplied object value to a file located at the supplied path.
     *
     * @param object the object to save.
     * @param path the path to save the object to.
     * @return true if save succeeded, false if failed.
     */
    public static boolean saveObjectToFile(Object object, String path) {
        return saveObjectToFile(object, new File(path));
    }

    /**
     * Save the supplied object value to a file located at the supplied path.
     *
     * @param object the object to save.
     * @param file the file to save the object to.
     * @return true if save succeeded, false if failed.
     */
    public static boolean saveObjectToFile(Object object, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            getXStream().toXML(object, out);
        } catch (Exception ex) {
            LOGGER.error("Failed to save file to path: {}.", file.getPath());
            return false;
        }
        return true;
    }

    /**
     * Save the file.
     *
     * @param file the file to save.
     */
    public static void saveDecisionTableToFile(File file) {
        FXUtil.runAsync(() -> {
            DECISION_TBL_SVC.updateTable();
            VALID_SVC.runValidationImmediately();

            if (!VALID_SVC.validProperty().get()) {
                LOGGER.debug("The DecisionTable does not pass validation. Aborting save.");
                ToastUtil.sendToast("Cannot save! The DecisionTable does not pass validation.");
                return;
            }

            boolean success = saveObjectToFile(DECISION_TBL_SVC.getDecisionTable(), file);
            if (success) {
                CURRENT_FILE.setValue(file);
                SettingsUtil.addRecommendedFile(file);
                setDirty(false);
                ToastUtil.sendToast("File saved.");
            } else {
                LOGGER.error("Failed to save the file!");
                ToastUtil.sendPersistentToast("Failed to save file!");
            }
        });
    }

    /**
     * Print the table.
     *
     * @param decisionTable The decision table
     */
    public static void print(DecisionTable decisionTable) {
        if (decisionTable == null) {
            LOGGER.warn("The DecisionTable was null. Cannot print!");
            ToastUtil.sendToast("An error occurred! Could not print the file.");
            return;
        }
        TablePrintView tablePrintView = new TablePrintView(decisionTable);
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE, Printer.MarginType.HARDWARE_MINIMUM);
        double scaleX = pageLayout.getPrintableWidth() / 1920;
        double scaleY = pageLayout.getPrintableHeight() / 1080;
        tablePrintView.getTransforms().add(new Scale(scaleX, scaleY));

        PrinterJob job = PrinterJob.createPrinterJob();
        job.getJobSettings().setPageLayout(pageLayout);
        if (RootLayoutFactory.getInstance().getScene() != null && RootLayoutFactory.getInstance().getScene().getWindow() != null) {
            if (job.showPrintDialog(RootLayoutFactory.getInstance().getScene().getWindow())) {
                boolean success = job.printPage(tablePrintView);
                if (success) {
                    job.endJob();
                }
            }
        }
    }

    public static File getCurrentFile() {
        return CURRENT_FILE.get();
    }

    public static boolean isDirty() {
        return DIRTY.get();
    }

    public static BooleanProperty dirtyProperty() {
        return DIRTY;
    }

    public static void setDirty(boolean dirty) {
        FileUtil.DIRTY.set(dirty);
    }

    public static ReadOnlyObjectProperty<File> fileProperty() {
        return CURRENT_FILE;
    }

    private static XStream getXStream() {
        if (xStream == null) {
            //create and configure xStream
            xStream = new XStream();
            //configure XStream
            xStream.autodetectAnnotations(true);
            xStream.alias("DecisionTable", DecisionTable.class);
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[]{
                "org.yari.core.table.**",
                "settings.**",
                "objects.**"
            });
        }
        return xStream;
    }
}
