package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcomposer.util.TokenReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Compatibility matrix for sets.
 *
 * @author Simran Gleason
 */
public class Matrix {
    private HashMap columns;
    public Matrix() {
        columns = new HashMap();
    }

    public void setCell(String row, String column, Compatibility compat) {
        HashMap columnMap = (HashMap)columns.get(column);
        if (columnMap == null) {
            columnMap = new HashMap();
            columns.put(column, columnMap);
        }

        columnMap.put(row, compat);
    }

    public Compatibility getCell(String row, String column) {
        HashMap columnMap = (HashMap)columns.get(column);
        if (columnMap == null) {
            return Compatibility.UNITY;
        }

        Compatibility compat = (Compatibility)columnMap.get(row);
        if (compat == null) {
            return Compatibility.UNITY;
        }
        return compat;
    }

    // assumes read has read the <matrix> opening token
    public void readMatrix(TokenReader reader) throws IOException {
        String token = reader.readUntilToken(Bot.HEADERS);
        System.out.println("Should say headers: " + token + " ");
        ArrayList headers = new ArrayList();
        token = reader.readToken();
        while (token != null && !token.equalsIgnoreCase(Bot.END)) {
            System.out.print(token + " ");
            headers.add(token);
            token = reader.readToken();
        }
        System.out.println();
        String row = reader.readToken();
        System.out.print("row[" + row + "] ");
        while (row != null && !row.equalsIgnoreCase(Bot.END_MATRIX)) {
            String cell = reader.readToken();
            System.out.print(cell + " ");
            int colnum = 0;
            while(cell != null && !cell.equalsIgnoreCase(Bot.END)) {
                if (!cell.equals("-")) {
                    String column = (String)headers.get(colnum);
                    System.out.print(" (" + row + ", " + column + ") => " + cell + " " );
                    Compatibility c = new Compatibility(cell);
                    setCell(row, column, c);
                    setCell(column, row, c);
                }
                colnum++;
                cell = reader.readToken();
                System.out.print(cell + " ");
            }
            System.out.println();
            row = reader.readToken();
            System.out.print("row[" + row + "] ");
        }
    }

    public void write(StringBuffer buf, String indent) {
        String indent0 = indent;
        buf.append(indent0);
        buf.append(Bot.MATRIX);
        buf.append('\n');
        indent += "    ";
        int colWidth = 10;
        buf.append(indent);
        writeLength(buf, colWidth + 5, "headers ");
        for(Iterator it = columns.keySet().iterator(); it.hasNext(); ) {
            String columnHeader = (String)it.next();
            writeLength(buf, colWidth, columnHeader + " ");
        }
        buf.append(Bot.END);
        buf.append('\n');
        for(Iterator rowsit = columns.keySet().iterator(); rowsit.hasNext(); ) {
            String rowHeader = (String)rowsit.next();
            HashMap row = (HashMap)columns.get(rowHeader);
            buf.append(indent);
            writeLength(buf, colWidth + 5, rowHeader + " ");
            for(Iterator colsit = columns.keySet().iterator(); colsit.hasNext(); ) {
                String colName = (String)colsit.next();
                Compatibility cell = (Compatibility)row.get(colName);
                if (cell == null) {
                    writeLength(buf, colWidth, " - ");
                } else {
                    writeLength(buf, colWidth, cell.getIndex() + " ");
                }
            }
            buf.append("end\n");
        }

        buf.append(indent0);
        buf.append(Bot.END_MATRIX);
        buf.append('\n');
    }

    public void writeLength(StringBuffer buf, int length, String msg) {
        int padding = 0;
        if (msg.length() < length) {
            padding = length - msg.length();
            for(int i=0; i < padding; i++) {
                buf.append(' ');
            }
        }
        buf.append(msg);
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return(buf.toString());
    }
    
    public void toString(StringBuffer buf) {
        buf.append("Matrix[ ");
        for(Iterator it = columns.keySet().iterator(); it.hasNext(); ) {
            String columnHeader = (String)it.next();
            HashMap column = (HashMap)columns.get(columnHeader);
            for(Iterator cit = column.keySet().iterator(); cit.hasNext(); ) {
                String rowName = (String)cit.next();
                Compatibility cell = (Compatibility)column.get(rowName);
                buf.append("(" + rowName + ", " + columnHeader + ")=" + cell);
                if (cit.hasNext()) {
                    buf.append(" ");
                }
            }
            if (it.hasNext()) {
                buf.append("\n");
            }
        }
        buf.append("]");
    }

}