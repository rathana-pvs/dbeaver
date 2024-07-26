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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CubridPlanNode extends AbstractExecutionPlanNode
{
    private static final String OPTIONS_SEPARATOR = ":";
    private static final String COST = "cost";
    private static final String CLASS = "class";
    private Map<String, String> classNode;
    private Map<String, String> terms;
    private String fullText;
    private String nodeName;
    private String name;
    private String index;
    private String term;
    private long cost;
    private long row;
    private Map<String, String> nodeProps = new HashMap<>();
    private CubridPlanNode parent;
    private List<CubridPlanNode> nested;

    public CubridPlanNode(@NotNull String queryPlan) {
        this(null, null, null, null, null, queryPlan);
    }

    private CubridPlanNode(@Nullable CubridPlanNode parent, @Nullable String name, @Nullable List<String> segments, Map<String, String> classNode, Map<String, String> terms,  @NotNull String fullText) {
        this.parent = parent;
        this.name = name;
        this.fullText = fullText;
        this.classNode = classNode == null ? new HashMap<>() : classNode;
        this.terms = terms == null ? new HashMap<>() : terms;
        parseObject(parent == null ? this.getSegments() : segments);
        parseNode();
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
        String name = this.getNameValue();
        return name;
    }

    @NotNull
    @Property(order = 2, viewable = true)
    public String getIndex() {
        return index;
    }

    @NotNull
    @Property(order = 3, viewable = true)
    public String getTerms() {
        return getTermValue();
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
        return this.getTotalValue();
    }

    @NotNull
    @Property(order = 7, viewable = true)
    public String getExtra() {
        return getExtraValue();
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
            default -> method;
        };
    }

    private void addNested(@NotNull String name, @NotNull List<String> value) {
        if (nested == null) {
            nested = new ArrayList<>();
        }
        nested.add(new CubridPlanNode(this, name, value, classNode, terms, fullText));
    }

    private void parseNode() {
        for (String key : nodeProps.keySet()) {
            if (Arrays.asList(CLASS).contains(key)) {               
                this.nodeName = nodeProps.get(CLASS).split(" ")[1];
            } else if (key.equals("index")) {
                this.index = nodeProps.get(key).split(" ")[0];
                this.term = nodeProps.get(key).split(" ")[1];
            } else if (Arrays.asList("sargs", "edge").contains(key)) {
                this.term = nodeProps.get(key);
            } else if (key.equals(COST)) {
                String[] values = nodeProps.get(key).split(" card ");
                this.cost = Long.parseLong(values[0]);
                this.row = Long.parseLong(values[1]);
            }
        }
    }

    private void parseObject(@NotNull List<String> segments) {
        if (!segments.isEmpty()) {
            String[] removes = segments.remove(0).split(OPTIONS_SEPARATOR);
            nodeProps.put(removes[0], removes[1].trim());
            if (removes[0].equals(COST) || segments.isEmpty()) {
                return;
            } 
            String key = segments.get(0).split(OPTIONS_SEPARATOR)[0];
            if(segments.size() == 1 && removes[0].contains("sargs")) {
                nodeProps.remove(removes[0]);
                addNested(removes[0], new ArrayList<String>(Arrays.asList(String.join(":", removes))));
                
            } else if (nodeProps.containsKey(key) || Arrays.asList("subplan", "head").contains(removes[0])) {
                addNested(removes[1].trim(), segments);
                
            } else if (key.equals(CLASS) && !removes[0].equals("Query plan")) {
                addNested(removes[1].trim(), segments);
            }
                parseObject(segments);
        }
    }
    
    @Nullable
    private String getTermValue() {
        if(CommonUtils.isNotEmpty(term)) {
            String value;
            if(this.term.contains("node[")) {
                value = this.term;
                String regex = "node\\[\\d\\]";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(this.term);
                while(m.find()) {
                    value = value.replace(m.group(), classNode.get(m.group()));
                }
                return value;
            }
            
            else {
                value = terms.get(this.term);
                if(CommonUtils.isNotEmpty(value)) {
                    return value.split(" \\(sel")[0];
                }
            }
        }
        
       
        return null;
    }
    
    
    private String getExtraValue() {
        String value = terms.get(term);
        if(CommonUtils.isNotEmpty(value)) {
            return "(sel " + value.split(" \\(sel ")[1];
        }
        return null;
    }

    @Nullable
    private String getNameValue() {
        
        
//        Pattern p;
//        if (isName) {
//            p = Pattern.compile("\\w+ \\w+");
//        } else {
//            p = Pattern.compile("\\w+\\/\\w+");
//        }
//        Matcher m = p.matcher(classNode.get(nodeName));
//        if (m.find()) {
//            return m.group(0);
//        }
        String nameValue = classNode.get(nodeName);
        if(CommonUtils.isNotEmpty(nameValue)){
            return nameValue.split("\\(")[0];
            
        }
        return null;
    }
    
    @Nullable
    private String getTotalValue(){
        String nodeNameValue = classNode.get(nodeName);
        if(CommonUtils.isNotEmpty(nodeNameValue)) {
            Pattern p = Pattern.compile("\\w+\\/\\w+");
            Matcher m = p.matcher(nodeNameValue);
            if (m.find()) {
                return m.group(0);
            }
        }
        
        return null;
    }

    @NotNull
    private List<String> getSegments() {
        Pattern pattern =
                Pattern.compile(
                        "(inner|outer|class|cost|follow|head|index|sargs|edge|Query plan|term\\[..|node\\[..):\\s*([^\\n\\r]*)");
        Matcher matcher = pattern.matcher(fullText);
        List<String> segments = new ArrayList<String>();
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
