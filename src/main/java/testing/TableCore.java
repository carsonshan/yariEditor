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

import org.yari.core.YariException;
import org.yari.core.table.DecisionTable;
import org.yari.core.table.TableAction;
import org.yari.core.table.TableCondition;
import org.yari.core.table.TableRow;

import java.util.List;

/**
 * As there are no 'simple' ways of utilizing the actual Yari core for testing purposes, here,
 * we mimick the functionality of the core's table logic and add additional reporting for use
 * in the GUI.
 */
public class TableCore {

    private final DecisionTable decisionTable;

    /**
     * Build a new TableCore that will utilize the supplied {@link DecisionTable} to run.
     *
     * @param decisionTable the DecisionTable to run the core with.
     */
    public TableCore(DecisionTable decisionTable) {
        this.decisionTable = decisionTable;
    }

    /**
     * Run the core with the supplied condition map.
     */
    public String run(List<String> inputs) throws YariException {
        StringBuilder sb = new StringBuilder();
        TableRow matchedRow = null;

        //find a row in the decision table that matches (all eval to true)
        for (TableRow tableRow : decisionTable.getRawRowData()) {
            if (matchRow(tableRow, inputs)) {
                matchedRow = tableRow;
                sb.append("A row match was found in row number ").append(matchedRow.getRowNumber()).append(".");
                break;
            }
        }

        //if a match was not found
        if (matchedRow == null) {
            throw new YariException("A row match was not found in the decision table.");
        }

        //run action methods
        int actionIndex = 0;
        for (TableAction currentAction : decisionTable.getTableActions()) {
            sb.append("\nThe Action '").append(currentAction.getName()).append("' resulted in '").append(matchedRow.getConvertedActionValues().get(actionIndex++)).append("'.");
        }

        return sb.toString();


    }

    private boolean matchRow(TableRow initialRow, List<String> evaluatedConditions) {
        int column = 0;
        for (Object currentValue : initialRow.getConvertedValues()) {
            TableCondition condition = decisionTable.getTableConditions().get(column);
            if (!compare(evaluatedConditions.get(column), currentValue, condition.getDataType(), condition.getComparator())) {
                return false;
            }
            column++;
        }
        return true;
    }

    /**
     * Compares two objects with specified dataType and operation
     *
     * @param firstObject  first object to compare
     * @param secondObject second object to compare
     * @param dataType     the type of expected data
     * @param comparator   the compare operator
     * @return true if the comparison is true, false if else.
     */
    private boolean compare(Object firstObject, Object secondObject, String dataType, String comparator) {

        switch (comparator) {
            case "==":
                if (firstObject.equals(secondObject)) {
                    return true;
                } else {
                    return false;
                }
            case "!=":
                if (!firstObject.equals(secondObject)) {
                    return true;
                } else {
                    return false;
                }
            case "GT":
                if (!"boolean".equals(dataType) && !"string".equals(dataType) && !"char".equals(dataType)) {
                    if (Double.parseDouble(firstObject.toString()) > Double.parseDouble(secondObject.toString())) {
                        return true;
                    } else {
                        return false;
                    }
                } else if ("char".equals(dataType)) {
                    if (firstObject.toString().charAt(0) > secondObject.toString().charAt(0)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            case "GE":
                if (!"boolean".equals(dataType) && !"string".equals(dataType) && !"char".equals(dataType)) {
                    if (Double.parseDouble(firstObject.toString()) >= Double.parseDouble(secondObject.toString())) {
                        return true;
                    } else if ("char".equals(dataType)) {
                        if (firstObject.toString().charAt(0) >= secondObject.toString().charAt(0)) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            case "LT":
                if (!"boolean".equals(dataType) && !"string".equals(dataType)) {
                    if (Double.parseDouble(firstObject.toString()) < Double.parseDouble(secondObject.toString())) {
                        return true;
                    } else if ("char".equals(dataType)) {
                        if (firstObject.toString().charAt(0) < secondObject.toString().charAt(0)) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            case "LE":
                if (!"boolean".equals(dataType) && !"string".equals(dataType)) {
                    if (Double.parseDouble(firstObject.toString()) <= Double.parseDouble(secondObject.toString())) {
                        return true;
                    } else if ("char".equals(dataType)) {
                        if (firstObject.toString().charAt(0) <= secondObject.toString().charAt(0)) {
                            return true;
                        }
                    } else {
                        return false;
                    }

                }
        }
        return false;
    }


}
