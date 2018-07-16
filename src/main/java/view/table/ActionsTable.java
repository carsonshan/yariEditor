/*
 * This file is part of Yari Editor.
 *
 * Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Yari Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Yari Editor.
 *
 * Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Yari Editor.  If not, see <http://www.gnu.org/licenses/>.
 */

package view.table;

import components.EditableTextFieldCell;
import components.UpdateEvent;
import components.YariTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.yari.core.table.Action;

import java.util.List;

public class ActionsTable extends YariTable<Action> {

    public ActionsTable() {
        setPlaceholder(new Label("Add one or more actions to continue"));

        }

    @Override
    protected List<TableColumn<Action, ?>> buildColumns() {

        TableColumn<Action, String> nameCol = new TableColumn<>("NAME");
        nameCol.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getName() == null ? "ENTER A NAME" : cellData.getValue().getName();
            return new SimpleStringProperty(content);
        });
        nameCol.setCellFactory(c -> new EditableTextFieldCell<>());
        nameCol.setOnEditCommit(edit -> {
            String newContent = edit.getNewValue().trim();
            edit.getRowValue().setName(newContent);
        });

        TableColumn<Action, String> methodNameCol = new TableColumn<>("METHOD NAME");
        methodNameCol.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getMethodName() == null ? "ENTER A METHOD NAME" : cellData.getValue().getMethodName();
            return new SimpleStringProperty(content);
        });
        methodNameCol.setCellFactory(c -> new EditableTextFieldCell<>());
        methodNameCol.setOnEditCommit(edit -> {
            String newContent = edit.getNewValue().trim();
            edit.getRowValue().setMethodname(newContent);
        });

        TableColumn<Action, String> dataTypeCol = new TableColumn<>("DATA TYPE");
        dataTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), dataTypeValues));
        dataTypeCol.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getDataType() == null ? "SELECT DATA TYPE" : cellData.getValue().getDataType();
            return new SimpleStringProperty(content);
        });
        dataTypeCol.setOnEditCommit(edit -> {
            String newContent = edit.getNewValue().trim();
            edit.getRowValue().setDatatype(newContent);
            fireEvent(new UpdateEvent());
        });

        return List.of(nameCol, methodNameCol, dataTypeCol);
    }
}