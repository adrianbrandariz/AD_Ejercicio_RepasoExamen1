package exa15;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Exa15 {

    public static Connection conexion = null;

    public static Connection getConexion() throws SQLException {
        String usuario = "hr";
        String password = "hr";
        String host = "localhost";
        String puerto = "1521";
        String sid = "orcl";
        String ulrjdbc = "jdbc:oracle:thin:" + usuario + "/" + password + "@" + host + ":" + puerto + ":" + sid;

        conexion = DriverManager.getConnection(ulrjdbc);
        return conexion;
    }

    public static void closeConexion() throws SQLException {
        conexion.close();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, XMLStreamException {
        // Se establece la conexión a la base de datos:
        getConexion();
        // Se abre el flujo de datos:
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("platoss"));
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw = xof.createXMLStreamWriter(new FileWriter("totalgraxas.xml"));
        Statement stmt = null;
        ResultSet rs = null;
        // Escribe la declaracion XML con la versión especificada:
        xsw.writeStartDocument("1.0");
        // Se crea el elemento padre:
        xsw.writeStartElement("platos");
        Platos p2 = (Platos) is.readObject();
        while (p2 != null) {
            int grasasTotales = 0;
            // Se crea un elemento:
            xsw.writeStartElement("plato");
            xsw.writeAttribute("codigop", p2.getCodigop());
            xsw.writeStartElement("nomep");
            xsw.writeCharacters(p2.getNomep());
            xsw.writeEndElement();
            System.out.println("CODIGO DEL PLATO: " + p2.getCodigop());
            System.out.println("nombre del plato:" + p2.getNomep());
            stmt = conexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("select componentes.codc, componentes.graxa, composicion.peso "
                    + "from componentes, composicion where composicion.codp='" + p2.getCodigop() + "' and composicion.codc=componentes.codc");
            while (rs.next() != false) {
                System.out.println("código del componente:" + rs.getString(1) + " -> grasa por cada 100g = " + rs.getString(2));
                System.out.println("peso: " + rs.getString(3));
                int peso = Integer.parseInt(rs.getString(3));
                int grasa = Integer.parseInt(rs.getString(2));
                int resultado = (peso * grasa) / 100;
                System.out.println("total de grasa del componente; " + resultado + "\n");
                grasasTotales = grasasTotales + resultado;
            }
            xsw.writeStartElement("graxatotal");
            xsw.writeCharacters(String.valueOf(grasasTotales));
            System.out.println(grasasTotales);
            p2 = (Platos) is.readObject();
            System.out.println("");
            xsw.writeEndElement();
            xsw.writeEndElement();
        }
        xsw.writeEndElement();
        xsw.flush();
        xsw.close();
        rs.close();
        stmt.close();
        // Se cierra el flujo de datos:
        is.close();
        // Se cierra la conexión:
        closeConexion();
    }
}
