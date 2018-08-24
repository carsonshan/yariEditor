/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */

package testing;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import components.Card;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import objects.DataType;
import org.yari.core.YariException;
import org.yari.core.table.TableCondition;
import utilities.DecisionTableService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestingView extends StackPane {

    private final DecisionTableService decisionTableService = DecisionTableService.getService();
    private final Map<TableCondition, JFXTextField> conditionInputs = new HashMap<>();
    private final VBox displayedContent = new VBox(25);
    private final StringProperty consoleLog = new SimpleStringProperty("");
    private final StringBuilder log = new StringBuilder();

    public TestingView() {
        ScrollPane scroller = new ScrollPane();
        scroller.getStyleClass().add("cardScroller");
        scroller.setFitToWidth(true);
        scroller.setFitToHeight(true);
        scroller.setPadding(new Insets(20, 20, 20, 20));
        scroller.setContent(displayedContent);
        super.setPadding(new Insets(0, 5, 0, 0)); //TODO: Update styling for scroll bar
        super.getChildren().setAll(scroller);

        initRuleCard();
        initTestingCard();


        //TODO: Run and Clear buttons
        //TODO: Collapsible log

    }

    private void initTestingCard(){
        Card card = new Card("Run Test");
        displayedContent.getChildren().add(card);

        VBox content = new VBox();
        card.setDisplayedContent(content);
        TextArea console = new TextArea();
        console.setWrapText(true);
        console.setEditable(false);
        console.textProperty().bind(consoleLog);
        content.getChildren().add(console);
        VBox.setVgrow(console, Priority.ALWAYS);

        JFXButton runButton = new JFXButton("RUN");
        JFXButton resetButton = new JFXButton("RESET");
        HBox buttonBar = new HBox(resetButton, runButton);
        buttonBar.setSpacing(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        card.setFooterContent(buttonBar);

        TableCore tableCore = new TableCore(decisionTableService.getDecisionTable());

        runButton.setOnMouseClicked(me -> {
            logInConsole("Running test.");
            List<String> inputs = new ArrayList<>();

            conditionInputs.forEach((k, v) -> inputs.add(v.getText().trim()));

            try {
                tableCore.run(inputs);
            } catch (YariException ex){
                logInConsole("YariException! Message:" + ex.getMessage());
            }
        });

    }

    private void logInConsole(String string) {
        log.append(string).append("\n");
        consoleLog.setValue(log.toString());
    }

    private void initRuleCard() {
        Card card = new Card("Decision Table Rule");
        card.setPadding(new Insets(10, 10, 10, 10));
        VBox userInputs = new VBox(20);
        decisionTableService.getConditions().forEach(tableCondition -> createInput(tableCondition, userInputs));
        card.setDisplayedContent(userInputs);
        displayedContent.getChildren().add(card);
    }

    private JFXTextField createInput(TableCondition tableCondition, VBox parent) {

        VBox container = new VBox(1);
        Label methodAnnotation = new Label();
        methodAnnotation.setText("@Condition(" + tableCondition.getMethodName() + ")");

        DataType dataType = DataType.getFromTableString(tableCondition.getDataType());

        Label methodDeclaration = new Label();
        StringBuilder mdText = new StringBuilder();
        mdText.append("public ").append(dataType.getJavaCodeCompatibleValue()).append(" ").append(tableCondition.getMethodName()).append(" {");
        methodDeclaration.setText(mdText.toString());

        HBox inputContainer = new HBox(0);
        inputContainer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(inputContainer, new Insets(0, 0, 0, 10));
        Label label = new Label("return ");
        inputContainer.getChildren().add(label);


        JFXTextField textField = new JFXTextField();
        inputContainer.getChildren().add(textField);


        Label closeMethodLabel = new Label("}");

        container.getChildren().setAll(methodAnnotation, methodDeclaration, inputContainer, closeMethodLabel);
        parent.getChildren().add(container);
        conditionInputs.put(tableCondition, textField);

        return textField;
    }
}
