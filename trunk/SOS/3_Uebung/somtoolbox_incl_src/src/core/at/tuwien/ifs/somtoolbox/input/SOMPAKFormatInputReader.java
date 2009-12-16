package at.tuwien.ifs.somtoolbox.input;

import java.io.BufferedReader;
import java.io.IOException;

import at.tuwien.ifs.somtoolbox.util.FileUtils;

/**
 * Reads a SOM in SOMPAK file format. For details on this format, please see <a
 * href="http://www.cis.hut.fi/research/som_pak/som_doc.txt">http://www.cis.hut.fi/research/som_pak/som_doc.txt</a>.
 * 
 * @author Rudolf Mayer
 * @version $Id: SOMPAKFormatInputReader.java 2874 2009-12-11 16:03:27Z frank $
 */
public class SOMPAKFormatInputReader extends AbstractSOMInputReader {

    private String[] componentNames;

    private String neighbourhoodFunction;

    private String somPakFilename;

    private String topology;

    public SOMPAKFormatInputReader(String somPakFilename) throws IOException {
        this.somPakFilename = somPakFilename;
        BufferedReader br = FileUtils.openFile("SOMPAK File", somPakFilename);
        String line = null;
        String headerLine = br.readLine();
        String[] elements = headerLine.split(" ");
        dim = Integer.parseInt(elements[0]);
        topology = elements[1];
        xSize = Integer.parseInt(elements[2]);
        ySize = Integer.parseInt(elements[3]);
        zSize = 1; // Corrently only 2dim maps.
        neighbourhoodFunction = elements[4];
        String componentNamesLine = br.readLine();
        elements = componentNamesLine.split(" ");
        componentNames = new String[dim];
        System.arraycopy(elements, 1, componentNames, 0, componentNames.length);

        unitInfo = new UnitInformation[xSize][ySize][zSize];
        int index = 0;
        while ((line = br.readLine()) != null) {
            elements = line.split(" ");
            int x = index % xSize;
            int y = index / xSize;
            int z = 0;
            unitInfo[x][y][z] = new UnitInformation(dim);
            for (int i = 0; i < unitInfo[x][y][z].vector.length; i++) {
                unitInfo[x][y][z].vector[i] = Double.parseDouble(elements[i]);
            }
            index++;
        }
    }

    public String[] getComponentNames() {
        return componentNames;
    }

    public String getFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMapDescriptionFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNeighbourhoodFunction() {
        return neighbourhoodFunction;
    }

    public String getSomPakFilename() {
        return somPakFilename;
    }

    public String getTopology() {
        return topology;
    }

    public String getUnitDescriptionFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getWeightVectorFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    public static String getFormatName() {
        return "SOMPak";
    }
}
