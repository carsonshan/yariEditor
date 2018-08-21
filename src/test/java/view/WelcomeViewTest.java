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

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.testfx.assertions.api.Assertions.assertThat;

public class WelcomeViewTest extends ApplicationTest {

    private WelcomeView welcomeView;

    @Override
    public void start(Stage stage) throws Exception {
        welcomeView = new WelcomeView(stage);
        stage.setScene(new Scene(welcomeView));
        stage.show();
    }

    @Test
    public void testFX() {
        assertThat(welcomeView).hasChild("#closeButton");
        assertThat(welcomeView).hasChild(".fileList");
        assertThat(welcomeView).hasChild("#titleLabel");
        Label title = lookup("#titleLabel").query();
        assertThat(title).hasText("Yari Editor");
        assertThat(welcomeView).hasChild("#fileIcon");
        assertThat(welcomeView).hasChild("#openIcon");
        assertThat(welcomeView).hasChild("#importIcon");
        assertThat(welcomeView).hasChild("Create New");
        assertThat(welcomeView).hasChild("Import");
        assertThat(welcomeView).hasChild("Open");
        assertThat(welcomeView).hasChild("#settingsButton");
    }

}