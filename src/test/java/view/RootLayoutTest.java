/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */

package view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import objects.ToolView;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertNotNull;
import static org.testfx.assertions.api.Assertions.assertThat;


public class RootLayoutTest extends ApplicationTest {

    private RootLayout rootLayout;

    @Override
    public void start(Stage stage) throws Exception {
        rootLayout = new RootLayout();
        stage.setScene(new Scene(rootLayout));
        stage.show();
    }

    @Test
    public void handleDirty() {
        Platform.runLater(() -> {
            rootLayout.handleDirty();
            assertThat(rootLayout).hasChild("You have unsaved changes. Do you want to save before continuing?");
            clickOn("DISMISS");
        });
    }

    @Test
    public void showView() {
        rootLayout.showView(ToolView.ACTIONS);
    }

    @Test
    public void open() {
        Platform.runLater(() -> rootLayout.open());
    }

    @Test
    public void save() {
        Platform.runLater(() -> rootLayout.save(false));
    }

    @Test
    public void getDataEditor() {
        rootLayout.getDataEditor();
    }

    @Test
    public void getMinimizeButton() {
        assertNotNull(rootLayout.getMinimizeButton());
    }

    @Test
    public void getMaximizeButton() {
        assertNotNull(rootLayout.getMaximizeButton());
    }

    @Test
    public void getHeader() {
        assertNotNull(rootLayout.getHeader());
    }

    @Test
    public void displayNode() {
        Platform.runLater(() -> rootLayout.displayNode(new Pane()));
    }

    @Test
    public void getToastBar() {
        Platform.runLater(() -> assertNotNull(rootLayout.getToastBar()));
    }
}