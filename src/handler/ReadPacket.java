package handler;

import gui.other.AnalogValue;
import gui.other.NHMI;
import gui.other.NHMISignal;
import lombok.Data;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.EtherType;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static handler.DecodingBytes.findElementASDU;
import static handler.DecodingBytes.findAPDU;
import static handler.HandlerArrays.*;

// TODO: Reception and processing class SV.
@Data
public class ReadPacket {

    private static String SQL = "INSERT INTO validity (IaQ, IbQ, IcQ, I0Q, UaQ, UbQ," +
            "UcQ, U0Q, Checksum) VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    // TODO: level 1
    protected static int[] des_mac, src_mac, APPID, frameCheckSequence, trailer_frameCheckSequence;
    protected static int[] type = new int[2], length = new int[2], reserved1 = new int[2], reserved2 = new int[2];
    // TODO: level 2
    protected static int[] svID, savPdu = new int[2], noASDU = new int[3], sequence_ASDU = new int[2];

    private static final int COUNT = 4000;
    private static final String PCAP_FILE_KEY = ReadPacket.class.getName() + ".pcapFile";
    private static final String PCAP_FILE = System.getProperty(PCAP_FILE_KEY, "tests/pcapMissed.pcap");
    static ArrayList<Integer> array = new ArrayList<>();
    public static AnalogValue Ia = new AnalogValue();
    public static AnalogValue Ib = new AnalogValue();
    public static AnalogValue Ic = new AnalogValue();
    public static AnalogValue I0 = new AnalogValue();
    public static AnalogValue Ua = new AnalogValue();
    public static AnalogValue Ub = new AnalogValue();
    public static AnalogValue Uc = new AnalogValue();
    public static AnalogValue U0 = new AnalogValue();


    public static byte[] getRawData() throws PcapNativeException {
        PcapHandle handle;
        byte[] rawData = null;
        try {
            handle = Pcaps.openOffline(PCAP_FILE);

            NHMI nhmi = new NHMI();
            nhmi.addSignals(new NHMISignal("Ia", Ia.getF()));
            nhmi.addSignals(new NHMISignal("Ib", Ib.getF()));
            nhmi.addSignals(new NHMISignal("Ic", Ic.getF()));
            nhmi.addSignals(new NHMISignal("I0", I0.getF()));
            nhmi.addSignals(new NHMISignal("Ua", Ua.getF()));
            nhmi.addSignals(new NHMISignal("Ub", Ub.getF()));
            nhmi.addSignals(new NHMISignal("Uc", Uc.getF()));
            nhmi.addSignals(new NHMISignal("U0", U0.getF()));

            Class.forName(DRIVER).getDeclaredConstructor().newInstance();
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {
                connection.getTransactionIsolation();
                for (int i = 0; i < COUNT; i++) {
                    try {
                        Packet packet = handle.getNextPacketEx();
                        if (packet == null) {
                            System.out.println("Пакет пустой");
                        }
                        String a = "Номер пакета: ";
                        System.out.print(a);
                        System.out.print(i + " -> ");
                        assert packet != null;
                        EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
                        rawData = ethernetPacket.getRawData();
                        System.out.println("unprocessed -> " + Arrays.toString(rawData));

                        int[] output = new int[rawData.length];
                        for (int j = 0; j < rawData.length; j++) {
                            if (rawData[j] < 0) {
                                output[j] = (int) rawData[j] + 256;
                            } else output[j] = rawData[j];
                        }
                        System.out.println();
                        System.out.println("processed -> " + Arrays.toString(output));
                        System.out.println();

                        des_mac       = Arrays.copyOfRange(output, 0, 6);
                        src_mac       = Arrays.copyOfRange(output, 6, 12);
                        type          = Arrays.copyOfRange(output, 12, 14);
                        APPID         = Arrays.copyOfRange(output, 14, 16);
                        length        = Arrays.copyOfRange(output, 16, 18);
                        reserved1     = Arrays.copyOfRange(output, 18, 20);
                        reserved2     = Arrays.copyOfRange(output, 20, 22);
                        savPdu        = Arrays.copyOfRange(output, 22, 24);
                        noASDU        = Arrays.copyOfRange(output, 24, 27);
                        sequence_ASDU = Arrays.copyOfRange(output, 27, 29);
                        svID          = Arrays.copyOfRange(findAPDU(output), 0, findAPDU(output).length - 1);

                        int k = findAPDU(output)[findAPDU(output).length - 1];
                        System.out.println(k);
                        int end = findElementASDU(k - 1, output);
                        System.out.println(end);
                        frameCheckSequence = Arrays.copyOfRange(output, 0, end);
                        trailer_frameCheckSequence = Arrays.copyOfRange(output, end, end + 4);

                        System.out.println(Arrays.toString(frameCheckSequence) + " - frameCheckSequence");
                        System.out.println(Arrays.toString(trailer_frameCheckSequence) + " - trailer_frameCheckSequence");

                        System.out.println((getBytesToLong(getByte(trailer_frameCheckSequence))) + " - trailer_frameCheckSequenceCheck");
                        System.out.println(getCRC32(getByte(frameCheckSequence)) + " - frameCheckSequenceCheck");
                        System.out.println(Arrays.toString(des_mac) + " - des_mac");
                        System.out.println(Arrays.toString(src_mac) + " - scr_mac");
                        System.out.println(Arrays.toString(type) + " - type");
                        System.out.println(Arrays.toString(APPID) + " - APPID");
                        System.out.println(Arrays.toString(length) + " - length");
                        System.out.println(Arrays.toString(reserved1) + " - reserve1");
                        System.out.println(Arrays.toString(reserved2) + " - reserve2");
                        System.out.println(Arrays.toString(savPdu) + " - savPdu");
                        System.out.println(Arrays.toString(noASDU) + " - noASDU");
                        System.out.println(Arrays.toString(sequence_ASDU) + " - sequence_ASDU\n");
                        System.out.println(svID_decode(svID) + " - svID_decode");
                        System.out.println(hexDecode(APPID) + " - appID_decode");
                        System.out.println(hexDecode(des_mac) + " - desMAC_decode");
                        System.out.println(hexDecode(src_mac) + " - srcMAC_decode");
                        System.out.println(getDecode(smpCnt, 2) + " - smpCnt_decode");

                        array.add(getDecode(smpCnt, 2));
                        System.out.println(array + " - mass");
                        System.out.println(HandlerArrays.getMissing(array));

                        System.out.println(getDecode(confRev, 4) + " - confRev_decode");
                        System.out.println(getDecode(smpSynch, 1) + " - smpSynch_decode");
                        System.out.println("PhsMeas_decode - " + Arrays.toString(IU_decode(PhsMeas)) + "\nValidity:");

                        String checksum = null;
                        if (getBytesToLong(getByte(trailer_frameCheckSequence)) == getCRC32(getByte(frameCheckSequence))) {
                            checksum = "ok";
                        } else checksum = "broken";

                        preparedStatement.setLong(1, validity_decode(PhsMeas)[0]);
                            preparedStatement.setLong(2, validity_decode(PhsMeas)[2]);
                            preparedStatement.setLong(3, validity_decode(PhsMeas)[4]);
                            preparedStatement.setLong(4, validity_decode(PhsMeas)[6]);
                            preparedStatement.setLong(5, validity_decode(PhsMeas)[8]);
                            preparedStatement.setLong(6, validity_decode(PhsMeas)[10]);
                            preparedStatement.setLong(7, validity_decode(PhsMeas)[12]);
                            preparedStatement.setLong(8, validity_decode(PhsMeas)[14]);
                            preparedStatement.setString(9, checksum);
                            preparedStatement.execute();

//                            System.out.println(validity_decode(DecodingBytes.PhsMeas)[j]);
//                            switch ((int) validity_decode(DecodingBytes.PhsMeas)[j]) {
//                                case 0 -> System.out.println("Good");
//                                case 1 -> System.out.println("Invalid");
//                                case 2 -> System.out.println("Reserved");
//                                case 3 -> System.out.println("Questionable");
//                            }

                    File file = new File("src/gui/fx/parameters");
                    PrintWriter pw = new PrintWriter(file);
                    String array = Arrays.toString(new String[]{
                            String.valueOf(getDecode(smpCnt,2)),
                            svID_decode(svID),
                            hexDecode(APPID),
                            hexDecode(des_mac),
                            hexDecode(src_mac),
                            String.valueOf(getDecode(confRev, 4)),
                            String.valueOf(getDecode(smpSynch, 1))
                    });
                    array = array.replaceAll("\\[(.*)\\]", "$1");
                    pw.println(array);
                    pw.close();

                        System.out.println("-----------------------------------------");
                        Ia.getF().setValue((float) (IU_decode(PhsMeas)[0] / 1000.0));
                        Ib.getF().setValue((float) (IU_decode(PhsMeas)[2] / 1000.0));
                        Ic.getF().setValue((float) (IU_decode(PhsMeas)[4] / 1000.0));
                        I0.getF().setValue((float) (IU_decode(PhsMeas)[6] / 1000.0));
                        Ua.getF().setValue((float) (IU_decode(PhsMeas)[8] / 1000.0));
                        Ub.getF().setValue((float) (IU_decode(PhsMeas)[10] / 1000.0));
                        Uc.getF().setValue((float) (IU_decode(PhsMeas)[12] / 1000.0));
                        U0.getF().setValue((float) (IU_decode(PhsMeas)[14] / 1000.0));
                        nhmi.process();

                    } catch (TimeoutException ignored) {
                        System.out.println("TimeoutException");
                    } catch (EOFException e) {
                        System.out.println("EOF");
                    } catch (NotOpenException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                handle.close();

            } catch (PcapNativeException e) {
                System.out.println("Open handle error");
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }

            return rawData;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    // processing a single packet
    public static void getOnePacket() {
        PcapHandle handle;
        try {
            // to read data
            handle = Pcaps.openOffline(PCAP_FILE);
            for (int i = 0; i < COUNT; i++) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    if(packet == null) {
                        System.out.println("Пакет пустой");
                        break;
                    }
                    String a = "Номер пакета:";
                    System.out.print(a);
                    System.out.println(i+1);
                    EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
                    EtherType eth_type = ethernetPacket.getHeader().getType();
                    // 0x88ba for SV
                    System.out.println(eth_type);
                    // SV stream data array - an array containing a stream of SV bytes
                    byte[] rawData = ethernetPacket.getRawData();
                    System.out.println(Arrays.toString(rawData));
                } catch (TimeoutException ignored) {
                    System.out.println("TimeoutException");
                } catch (EOFException e) {
                    System.out.println("EOF");
                    break;
                }
            }
            handle.close();
        } catch (PcapNativeException | NotOpenException e) {
            System.out.println("Open handle error");
        }
    }

    public static Connection getConnection() throws SQLException, IOException {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("database.properties"))){
            properties.load(in);
        }
        String url = properties.getProperty("url");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(url, user, password);

    }
}





