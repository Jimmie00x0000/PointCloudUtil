package cn.jimmiez.pcu.io.off;

import cn.jimmiez.pcu.model.PcuPointCloud3f;
import cn.jimmiez.pcu.model.PcuPolygonMesh3f;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class OffReaderTest {

    @Test
    public void readTest1() throws FileNotFoundException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube.off").getFile());
        OffReader reader = new OffReader();
        OffReader.OffData data =  reader.read(file);
        assertTrue(data.getVertices().size() == data.getVerticesNum());
        assertTrue(data.getFaces().size() == data.getFacesNum());
        assertTrue(data.getFaceColors().size() == 0 || data.getFaceColors().size() == data.getFacesNum());
        assertTrue(data.getVertexColors().size() == 0 || data.getVertexColors().size() == data.getVerticesNum());

        PcuPointCloud3f pointCloud = reader.read(file, PcuPointCloud3f.class);
        assertTrue(pointCloud.getPoints().size() == 8);
    }

    @Test
    public void readTest2() throws FileNotFoundException {
        File file = new File(OffReaderTest.class.getClassLoader().getResource("model/off/cube2.off").getFile());
        OffReader reader = new OffReader();
        OffReader.OffData data =  reader.read(file);
        assertTrue(data.getVertices().size() == data.getVerticesNum());
        assertTrue(data.getFaces().size() == data.getFacesNum());
        assertTrue(data.getVertexColors().size() == 0 || data.getVertexColors().size() == data.getVerticesNum());

        PcuPolygonMesh3f pointCloud = reader.read(file, PcuPolygonMesh3f.class);
        assertTrue(pointCloud.getPoints().size() == 8);
        assertTrue(pointCloud.getFaces().size() == 6);
    }
}