package org.milyn.ejc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.javabean.pojogen.JClass;
import org.milyn.javabean.pojogen.JNamedType;
import org.milyn.javabean.pojogen.JType;

import java.util.List;

public class WriteMethodTest {
    private static final String WRITE_METHOD_FIELD_OBJECT = "" +
            "\n" +
            "        Writer nodeWriter = new StringWriter();\n" +
            "\n" +
            "        List<String> nodeTokens = new ArrayList<String>();\n" +
            "\n" +
            "        if(c326 != null) {\n" +
            "            c326.write(nodeWriter, delimiters);\n" +
            "            nodeTokens.add(nodeWriter.toString());\n" +
            "            ((StringWriter)nodeWriter).getBuffer().setLength(0);\n" +
            "        }\n" +
            "        nodeTokens.add(nodeWriter.toString());\n" +
            "        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));" +
            "";

    private static final String WRITE_METHOD_FIELD_COLLECTION = "" +
            "\n" +
            "        Writer nodeWriter = new StringWriter();\n" +
            "\n" +
            "        List<String> nodeTokens = new ArrayList<String>();\n" +
            "\n" +
            "        if(c326 != null && !c326.isEmpty()) {\n" +
            "            for(C326 item : c326) {\n" +
            "                if(!nodeTokens.isEmpty()) {\n" +
            "                    nodeWriter.write(delimiters.getField());\n" +
            "                }\n" +
            "                item.write(nodeWriter, delimiters);\n" +
            "                nodeTokens.add(nodeWriter.toString());\n" +
            "                ((StringWriter)nodeWriter).getBuffer().setLength(0);\n" +
            "            }\n" +
            "        }\n" +
            "        for(int i = c326 == null ? 1 : c326.size() + 1; i < 2; i++) {\n" +
            "            nodeWriter.write(delimiters.getField());\n" +
            "        }\n" +
            "        nodeTokens.add(nodeWriter.toString());\n" +
            "        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));" +
            "";

    private static final String WRITE_METHOD_FIELD_OBJECT_AND_COLLECTION = "" +
            "\n" +
            "        Writer nodeWriter = new StringWriter();\n" +
            "\n" +
            "        List<String> nodeTokens = new ArrayList<String>();\n" +
            "\n" +
            "        if(c326 != null) {\n" +
            "            c326.write(nodeWriter, delimiters);\n" +
            "            nodeTokens.add(nodeWriter.toString());\n" +
            "            ((StringWriter)nodeWriter).getBuffer().setLength(0);\n" +
            "        }\n" +
            "        if(c3262 != null && !c3262.isEmpty()) {\n" +
            "            for(C326 item : c3262) {\n" +
            "                nodeWriter.write(delimiters.getField());\n" +
            "                item.write(nodeWriter, delimiters);\n" +
            "                nodeTokens.add(nodeWriter.toString());\n" +
            "                ((StringWriter)nodeWriter).getBuffer().setLength(0);\n" +
            "            }\n" +
            "        }\n" +
            "        for(int i = c3262 == null ? 0 : c3262.size(); i < 2; i++) {\n" +
            "            nodeWriter.write(delimiters.getField());\n" +
            "        }\n" +
            "        nodeTokens.add(nodeWriter.toString());\n" +
            "        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));" +
            "";

    @Test
    public void fieldWithoutMaxOccurs() {
        JClass jFti = new JClass("com.example.common", "Fti");
        JClass jC326 = new JClass("com.example.common.composite", "C326");

        JType jTypeC326 = new JType(jC326.getSkeletonClass());
        JNamedType jNamedC326 = new JNamedType(jTypeC326, "c326");

        Segment fti = new Segment();
        fti.setSegcode("FTI");
        fti.setTruncatable(true);
        fti.setXmltag("FTI");

        Field c326 = new Field();
        c326.setTruncatable(true);
        c326.setXmltag("C326");
        c326.setNodeTypeRef("C326");
        c326.setRequired(true);
        fti.addField(c326);

        WriteMethod method = new WriteMethod(jFti, fti);
        method.writeObject(jNamedC326, DelimiterType.FIELD,null, c326);
        String result = method.getBody();
        assertEquals(WRITE_METHOD_FIELD_OBJECT, result);
    }

    @Test
    public void fieldWithMaxOccurs() {
        JClass jFti = new JClass("com.example.common", "Fti");
        JClass jC326 = new JClass("com.example.common.composite", "C326");

        JType jTypeC326 = new JType(jC326.getSkeletonClass());
        JType jTypeListOfC326 = new JType(List.class, jTypeC326.getType());
        JNamedType jNamedType = new JNamedType(jTypeListOfC326, "c326");

        Segment fti = new Segment();
        fti.setSegcode("FTI");
        fti.setTruncatable(true);
        fti.setXmltag("FTI");

        Field c326 = new Field();
        c326.setMaxOccurs(9);
        c326.setTruncatable(true);
        c326.setXmltag("C326");
        c326.setNodeTypeRef("C326");
        fti.addField(c326);

        WriteMethod method = new WriteMethod(jFti, fti);
        method.writeFieldCollection(jNamedType, DelimiterType.FIELD,null, 2);
        String result = method.getBody();
        assertEquals(WRITE_METHOD_FIELD_COLLECTION, result);
    }

    @Test
    public void fieldWithAndWithoutMaxOccurs() {
        JClass jFti = new JClass("com.example.common", "Fti");
        JClass jC326 = new JClass("com.example.common.composite", "C326");

        JType jTypeC326 = new JType(jC326.getSkeletonClass());
        JNamedType jNamedC326 = new JNamedType(jTypeC326, "c326");
        JType jTypeListOfC326 = new JType(List.class, jTypeC326.getType());
        JNamedType jNamedC3262 = new JNamedType(jTypeListOfC326, "c3262");

        Segment fti = new Segment();
        fti.setSegcode("FTI");
        fti.setTruncatable(true);
        fti.setXmltag("FTI");

        Field c326 = new Field();
        c326.setTruncatable(true);
        c326.setXmltag("C326");
        c326.setNodeTypeRef("C326");
        c326.setRequired(true);
        fti.addField(c326);

        Field c3262 = new Field();
        c3262.setMaxOccurs(8);
        c3262.setTruncatable(true);
        c3262.setXmltag("C326_2");
        c3262.setNodeTypeRef("C326");
        fti.addField(c3262);

        WriteMethod method = new WriteMethod(jFti, fti);
        method.writeObject(jNamedC326, DelimiterType.FIELD,null, c326);
        method.writeFieldCollection(jNamedC3262, DelimiterType.FIELD,null, 2);
        String result = method.getBody();
        assertEquals(WRITE_METHOD_FIELD_OBJECT_AND_COLLECTION, result);
    }
}
