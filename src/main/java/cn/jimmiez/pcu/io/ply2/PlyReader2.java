package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.ply.PlyElement;
import cn.jimmiez.pcu.io.ply.PlyHeader;
import cn.jimmiez.pcu.io.ply.PlyPropertyType;
import cn.jimmiez.pcu.io.ply2.PlyData;
import cn.jimmiez.pcu.model.Pair;
import cn.jimmiez.pcu.model.PcuPointCloud3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class PlyReader2 {



    public PcuPointCloud3f readPointCloud(File file, Class<PcuPointCloud3f> clazz) {
        PcuPointCloud3f object = null;
        try {
            object = clazz.newInstance();
            FileInputStream stream = new FileInputStream(file);
            Scanner scanner = new Scanner(stream);
            PlyHeader2 header = readHeader(scanner);
            PlyData data = new PlyData(file, header);
            for (PlyElement2 element2 : data) {
            }
            stream.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private PlyHeader2 readHeader(Scanner scanner) throws IOException {
        PlyHeader2 header = new PlyHeader2();
        List<String> headerLines = new ArrayList<>();
        int byteCount = 0;
        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                byteCount += (line.getBytes().length + 1);
                if (line.equals("end_header")) break;
                if (line.startsWith("comment ")) {
                    header.getComments().add(line);
                    continue;
                }
                headerLines.add(line);
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        header.setBytesCount(byteCount);
        String firstLine = headerLines.get(0);
        if (! firstLine.equals(Constants.MAGIC_STRING)) {
            throw new IOException("Invalid ply file: Ply file does not start with ply.");
        }
        String secondLine = headerLines.get(1);
        readPlyFormat(secondLine, header);
        for (int lineNo = 2; lineNo < headerLines.size();) {
            String elementLine = headerLines.get(lineNo);
            PlyHeader2.PlyElementHeader element = new PlyHeader2.PlyElementHeader();
            Pair<String, Integer> pair = readPlyElement(elementLine);
            element.setNumber(pair.getValue());
            element.setElementName(pair.getKey());
            lineNo += 1;
            int propertyStartNo = lineNo;
            while (lineNo < headerLines.size() && headerLines.get(lineNo).startsWith("property ")) lineNo++;
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.getProperties().add(parseProperty(headerLines.get(i)));
            }
            header.getElementHeaders().add(element);
        }
        return header;
    }

    private Pair<String, Integer> readPlyElement(String line) throws IOException {
        String[] elementSlices = line.split(" ");
        if (! line.startsWith("element ") || elementSlices.length < 3) {
            throw new IOException("Invalid ply file: Invalid format.");
        }
        String elementName = elementSlices[1];
        Integer elementNumber = Integer.valueOf(elementSlices[2]);
        return new Pair<>(elementName, elementNumber);
    }

    private PcuDataType parseType(String type) throws IOException {
        switch (type) {
            case "char":
            case "int8":
                return PcuDataType.CHAR;
            case "uchar":
            case "uint8":
                return PcuDataType.UCHAR;
            case "int":
            case "int32":
                return PcuDataType.INT;
            case "uint":
            case "uint32":
                return PcuDataType.UINT;
            case "short":
            case "int16":
                return PcuDataType.SHORT;
            case "ushort":
            case "uint16":
                return PcuDataType.USHORT;
            case "float":
            case "float32":
                return PcuDataType.FLOAT;
            case "double":
            case "float64":
                return PcuDataType.DOUBLE;
        }
        throw new IOException("Cannot parse type: " + type);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public Pair<String, PlyPropertyType2> parseProperty(String line) throws IOException {
        String[] propertySlices = line.split("(\\s)+");
        String propertyName = propertySlices[propertySlices.length - 1];
        PlyPropertyType2 propertyType;
        if (propertySlices[1].equals("list")) {
            if (propertySlices.length < 5) throw new IOException("Too less properties for list type: " + propertyName);
            final PcuDataType sizeType = parseType(propertySlices[2]);
            final PcuDataType dataType = parseType(propertySlices[3]);
            propertyType = new PlyPropertyType2.PlyListType() {
                @Override
                public PcuDataType sizeType() {
                    return sizeType;
                }

                @Override
                public PcuDataType dataType() {
                    return dataType;
                }
            };
        } else {
            if (propertySlices.length < 3) throw new IOException("Too less properties for scalar type: " + propertyName);
            final PcuDataType type = parseType(propertySlices[1]);
            propertyType = new PlyPropertyType2.PlyScalarType() {
                @Override
                public PcuDataType dataType() {
                    return type;
                }
            };
        }

        return new Pair<>(propertyName, propertyType);
    }

    private void readPlyFormat(String line, PlyHeader2 header) throws IOException {
        if (!line.startsWith("format ")) {
            throw new IOException("Invalid ply file: No format information");
        }
        String[] formatSlices = line.split(" ");
        if (formatSlices.length == 3) {
            switch (formatSlices[1]) {
                case "ascii":
                    header.setFormat(PlyFormat.ASCII);
                    break;
                case "binary_little_endian":
                    header.setFormat(PlyFormat.BINARY_LITTLE_ENDIAN);
                    break;
                case "binary_big_endian":
                    header.setFormat(PlyFormat.BINARY_BIG_ENDIAN);
                    break;
            }
            header.setVersion(Float.valueOf(formatSlices[2]));
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }

}