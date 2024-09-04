/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.cubrid.model.plan;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNodeKind;
import org.jkiss.dbeaver.model.impl.plan.AbstractExecutionPlanNode;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.meta.PropertyLength;
import org.jkiss.utils.CommonUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CubridPlanNode extends AbstractExecutionPlanNode
{
    private static final String OPTIONS_SEPARATOR = ":";
    private static Map<String, String> classNode = new HashMap<>();
    private static Map<String, String> terms = new HashMap<>();
    private static int i;
    private static List<String> segments;
    List<String> parentNode = List.of("subplan", "head", "outer", "inner", "Query plan");
    List<String> parentExcept = List.of("iscan", "sscan");
    List<String> singleNode = List.of("sargs", "filtr");
    private String fullText;
    private String nodeName;
    private String totalValue;
    private String name;
    private String index;
    private String term;
    private String extra;
    private long cost;
    private long row;
    private CubridPlanNode parent;
    private List<CubridPlanNode> nested;

    public CubridPlanNode(@NotNull String queryPlan) {
        i = 0;
        this.fullText = queryPlan;
        this.getSegments();
        parsObject();
    }

    private CubridPlanNode(CubridPlanNode parent, boolean single) {
        this.parent = parent;
        this.fullText = parent.fullText;
        if (single) {
            String[] values = segments.get(i - 1).split(":");
            name = values[0];
            term = this.getTermValue(values[1].trim());
            extra = this.getExtraValue(values[1].trim());

        } else {
            parsObject();
        }
    }


    @NotNull
    @Property(order = 0, viewable = true)
    @Override
    public String getNodeType() {
        return getMethodTitle(name);
    }

    @NotNull
    @Property(order = 1, viewable = true)
    @Override
    public String getNodeName() {

        return nodeName;
    }

    @NotNull
    @Property(order = 2, viewable = true)
    public String getIndex() {
        return index;
    }

    @NotNull
    @Property(order = 3, viewable = true)
    public String getTerms() {
        return term;
    }

    @Property(order = 4, viewable = true)
    public long getCost() {
        return cost;
    }

    @Property(order = 5, viewable = true)
    public long getCardinality() {
        return row;
    }

    @NotNull
    @Property(order = 6, viewable = true)
    public String getTotal() {
        return this.totalValue;
    }

    @NotNull
    @Property(order = 7, viewable = true)
    public String getExtra() {
        return extra;
    }

    @NotNull
    @Property(order = 8, length = PropertyLength.MULTILINE)
    public String getFullText() {
        return fullText;
    }

    @Nullable
    @Override
    public CubridPlanNode getParent() {
        return parent;
    }

    @Nullable
    @Override
    public Collection<CubridPlanNode> getNested() {
        return nested;
    }

    @Override
    public DBCPlanNodeKind getNodeKind() {
        if ("sscan".equals(name)) {
            return DBCPlanNodeKind.TABLE_SCAN;
        } else if ("iscan".equals(name)) {
            return DBCPlanNodeKind.INDEX_SCAN;
        }
        return super.getNodeKind();
    }

    @Nullable
    private String getMethodTitle(@NotNull String method) {

        return switch (method) {
            case "iscan" -> "Index Scan";
            case "sscan" -> "Full Scan";
            case "temp(group by)" -> "Group by Temp";
            case "temp(order by)" -> "Order by Temp";
            case "nl-join (inner join)" -> "Nested Loop - Inner Join";
            case "nl-join (cross join)" -> "Nested Loop - Cross Join";
            case "idx-join (inner join)" -> "Index Join - Inner Join";
            case "m-join (inner join)" -> "Merged - Inner Join";
            case "temp" -> "Temp";
            case "follow" -> "Follow";
            case "filtr" -> "Filter";
            default -> method;
        };
    }

    private void addNested(boolean single) {
        if (nested == null) {
            nested = new ArrayList<>();
        }
        parent = this;
        nested.add(new CubridPlanNode(this, single));
    }

    void parsNode() {
        addNested(false);
        while (i < segments.size()) {
            String key = segments.get(i).split(":")[0];
            if (parentNode.contains(key)) {
                addNested(false);
            } else {
                parsObject();
                break;
            }
        }

    }

    void parsObject() {

        while (i < segments.size()) {
            String[] values = segments.get(i).split(":");
            String key = values[0].trim();
            String value = values[1].trim();
            i++;
            switch (key) {
                case "index":
                    String[] indexes = value.split(" ");
                    index = indexes[0];
                    if (indexes.length > 1) {
                        this.getTermValue(indexes[1]);
                        term = this.getTermValue(indexes[1]);
                        extra = this.getExtraValue(indexes[1]);
                    }
                    break;
                case "class":
                    this.getNameValue(value);
                    break;
                case "edge":
                    if (parent.name.equals("follow")) {
                        parent.extra = this.getTermValue(value);
                    } else {
                        parent.term = this.getTermValue(value);
                    }

            }
            if (parentNode.contains(key)) {
                name = value;
                if (!parentExcept.contains(value)) {
                    parsNode();
                    break;
                }
            } else if ("sargs".equals(key)) {
                if (!parentExcept.contains(name)) {
                    addNested(true);
                    break;
                } else {
                    term = this.getTermValue(value);
                    extra = this.getExtraValue(value);
                }

            } else if ("filtr".equals(key)) {
                addNested(true);
            } else if (key.contains("cost")) {
                String[] costs = value.split(" card ");
                this.cost = Long.parseLong(costs[0]);
                this.row = Long.parseLong(costs[1]);
                break;
            }
        }
    }


    @Nullable
    private String getTermValue(String value) {
        if (CommonUtils.isNotEmpty(value)) {
            if (value.contains("node[")) {
                String regex = "node\\[\\d\\]";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(value);
                while (m.find()) {
                    return value.replace(m.group(), classNode.get(m.group()));
                }
            } else {
                String termValue = terms.get(value);
                if (CommonUtils.isNotEmpty(termValue)) {
                    return termValue.split(" \\(sel ")[0];
                }
            }
        }
        return null;

    }

    @Nullable
    private String getExtraValue(String value) {
        String extraValue = terms.get(value);
        if (CommonUtils.isNotEmpty(extraValue)) {
            return "(sel " + extraValue.split(" \\(sel ")[1];
        }
        return null;
    }

    @Nullable
    private void getNameValue(String value) {

        String[] values = value.split(" ");
        String nameValue = classNode.get(values[values.length - 1]);
        if (CommonUtils.isNotEmpty(nameValue)) {
            nodeName = nameValue.split("\\(")[0];
            this.getTotalValue(nameValue);
        }
    }

    @Nullable
    private void getTotalValue(String value) {

        if (CommonUtils.isNotEmpty(value)) {
            Pattern p = Pattern.compile("\\d+\\/\\d+");
            Matcher m = p.matcher(value);
            if (m.find()) {
                totalValue = m.group(0);
            }
        }
    }

    @NotNull
    private List<String> getSegments() {
        Pattern pattern =
                Pattern.compile(
                        "(inner|outer|class|cost|follow|head|subplan|index|filtr|sargs|edge|Query plan|term\\[..|node\\[..):\\s*([^\\n\\r]*)");
        Matcher matcher = pattern.matcher(fullText);
        segments = new ArrayList<String>();
        while (matcher.find()) {
            String segment = matcher.group().trim();
            if (segment.startsWith("node")) {
                String[] values = segment.split(OPTIONS_SEPARATOR);
                classNode.put(values[0], values[1]);
            } else if (segment.startsWith("term")) {
                String[] values = segment.split(OPTIONS_SEPARATOR);
                terms.put(values[0], values[1]);
            } else {
                segments.add(segment);
            }
        }
        this.name = segments.get(0).split(OPTIONS_SEPARATOR)[1].trim();
        return segments;
    }

}
