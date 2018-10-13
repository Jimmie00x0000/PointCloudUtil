package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlyWriter {

    public int write(Object object, File file) {
        int result = Constants.ERR_CODE_NO_ERROR;

        return result;
    }


    private String typeString(PlyElement element, int position) {
        int type = element.getPropertiesType().get(position);
        String typeStr = PlyReader.TYPE_NAME[type];
        if (type == PlyReader.TYPE_LIST) {
            int[] typePair = element.listTypes.get(element.getPropertiesName().get(position));
            typeStr += String.format(" %s %s", PlyReader.TYPE_NAME[typePair[0]], PlyReader.TYPE_NAME[typePair[1]]);
        }
        return typeStr;
    }

    private void writeImpl(PlyWriterRequest request) throws IOException {
        StringBuffer buffer = generatePlyHeaderString(request);
        if (request.format == PlyReader.FORMAT_ASCII) {
           // write string
            writeAsciiPlyImpl(buffer, request);
        } else if (request.format == PlyReader.FORMAT_BINARY_BIG_ENDIAN) {
           // write bytes
            writeBinaryPlyImpl(buffer, request);
        } else if (request.format == PlyReader.FORMAT_BINARY_LITTLE_ENDIAN) {
            writeBinaryPlyImpl(buffer, request);
        } else {
            System.err.println("Warning: unsupported ply format.");
        }
    }

    private StringBuffer generatePlyHeaderString(PlyWriterRequest request) {
        StringBuffer buffer = new StringBuffer("ply\n");
        buffer.append("format ");
        switch (request.format) {
            case PlyReader.FORMAT_ASCII:
                buffer.append("ascii");
                break;
            case PlyReader.FORMAT_BINARY_BIG_ENDIAN:
                buffer.append("binary_big_endian");
                break;
            case PlyReader.FORMAT_BINARY_LITTLE_ENDIAN:
                buffer.append("binary_little_endian");
                break;
        }
        buffer.append(" 1.0\n");
        for (String comment : request.comments) {
            if (comment.contains("\n")) {
                System.err.println("Warning: Do not use LF(\\n) in your comment.");
                System.err.println("This comment will be neglected. " + comment);
                continue;
            }
            buffer.append("comment ").append(comment).append("\n");
        }
        for (PlyElement element : request.elements) {
            buffer.append("element ").append(element.elementName);
            buffer.append(" ").append(request.elementData.get(element).size()).append("\n");
            for (int i = 0; i < element.propertiesName.size(); i ++) {
                buffer.append("property ").append(typeString(element, i)).append(" ").append(element.propertiesName.get(i)).append("\n");
            }
        }
        buffer.append("end_header\n");
        return buffer;
    }

    private void writeAsciiPlyImpl(StringBuffer header, PlyWriterRequest pq) throws FileNotFoundException {
        File file = pq.file;
        PrintStream ps = new PrintStream(new FileOutputStream(file));
        ps.print(header.toString());
//        for (PlyElement element : pq.elements) {
//            List data = pq.elementData.get(element);
//            for (int i = 0; i < element.p)
//        }
        ps.close();
    }

    private void writeBinaryPlyImpl(StringBuffer buffer, PlyWriterRequest pq) {

    }

    public PlyWriterRequest prepare() {
        return new PlyWriterRequest();
    }

    public class PlyWriterRequest {

        List<PlyElement> elements = new ArrayList<>();

        Map<PlyElement, List> elementData = new HashMap<>();

        /** FORMAT_ASCII, FORMAT_BINARY_BIG_ENDIAN, FORMAT_BINARY_LITTLE_ENDIAN **/
        int format = PlyReader.FORMAT_ASCII;

        List<String> comments = new ArrayList<>();

        File file = null;

        public PlyWriterRequest defineElement(String elementName) {
            PlyElement element = new PlyElement();
            element.elementName = elementName;
            elements.add(element);
            return this;
        }

        public PlyWriterRequest defineScalarProperty(String propertyName, int valType) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperty()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            element.propertiesName.add(propertyName);
            element.propertiesType.add(valType);
            return this;
        }

        public PlyWriterRequest defineListProperty(String propertyName, int sizeType, int valType) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before defineScalarProperty()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            element.propertiesName.add(propertyName);
            element.propertiesType.add(PlyReader.TYPE_LIST);
            element.listTypes.put(propertyName, new int[]{sizeType, valType});
            return this;
        }

        public PlyWriterRequest putData(List data) {
            if (elements.size() < 1) {
                throw new IllegalStateException("defineElement() must be called before putData()");
            }
            PlyElement element = elements.get(elements.size() - 1);
            if (element.getPropertiesName().size() < 1) {
                throw new IllegalStateException("defineProperty() must be called before putData()");
            }
            // check type
            // put data
            elementData.put(element, data);
            return this;
        }

        public PlyWriterRequest format(int format) {
            this.format = format;
            return this;
        }

        public PlyWriterRequest comment(String comment) {
            this.comments.add(comment);
            return this;
        }

        public PlyWriterRequest writeTo(File file) {
            this.file = file;
            return this;
        }

        public int okay() {
            if (file == null) {
                throw new IllegalStateException("writeTo() must be called before okay()");
            }
            int result = Constants.ERR_CODE_NO_ERROR;
            try {
                writeImpl(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            } catch (IOException e) {
                e.printStackTrace();
                result = Constants.ERR_CODE_FILE_NOT_FOUND;
            }
            return result;
        }
    }
}
