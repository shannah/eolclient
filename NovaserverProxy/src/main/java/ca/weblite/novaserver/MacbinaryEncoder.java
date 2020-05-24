/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaserver;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <p>Useful resources:</p>
 * 
 * <p>
 * <ul>
 * <li><a href="http://livecode.byu.edu/helps/file-creatorcodes.php">Mac Creator Signature and File Types</a></li>
 * <li><a href="http://mail.python.org/pipermail/pythonmac-sig/2005-February/013028.html">Python Dict Mapping App types</a></li>
 * <li><a href="http://www.zeusprod.com/technote/filetype.html">Another list of common file types</a></li>
 * <li><a href="http://www.zeusprod.com/technote/filetype.html">Another list of common file types</a></li>
 * <li><a href="https://introcs.cs.princeton.edu/java/61data/CRC16.java">CRC16 Algorithm</a></li>
 * <li><a href="http://mirror.informatimago.com/next/developer.apple.com/documentation/mac/MoreToolbox/MoreToolbox-11.html">Apple Docs for Data Fork and Resource Fork</a></li>
 * <li><a href="https://code.google.com/archive/p/theunarchiver/wikis/MacBinarySpecs.wiki">MacBinary Format Specs</a></li>
 * </p>
 * @author shannah
 */
public class MacbinaryEncoder extends FilterOutputStream {
    private String fileName;
    private String fileType;
    private String creator;
    private long dateCreated, lastModified;
    private long fileSize;
    private boolean headerWritten;
    private int written;
    private boolean crc;
    
    public MacbinaryEncoder(OutputStream out, String fileName, String fileType, String creator, Date dateCreated, Date lastModified, long fileSize) {
        super(out);
        this.fileName = fileName;
        this.fileType = fileType;
        this.creator = creator;
        this.dateCreated = dateCreated.getTime();
        this.lastModified = lastModified.getTime();
        this.fileSize = fileSize;
        updateFileTypeAndCreator();
        System.out.println("Created Macbinary encoder with {fileName="+this.fileName+", fileType="+this.fileType+", creator="+this.creator+", dateCreated="+this.dateCreated+", lastModified="+this.lastModified+", fileSize="+this.fileSize);
    }

    @Override
    public void write(byte[] b) throws IOException {
        //prependHeader();
        super.write(b);
        //written += b.length;
    }

    @Override
    public void flush() throws IOException {
        super.flush(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public void write(int b) throws IOException {
        prependHeader();
        super.write(b);
        written++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        //prependHeader();
        super.write(b, off, len);
        //written += len;
    }

    @Override
    public void close() throws IOException {
        closeWithoutClosingUnderlyingStream();
        
        super.close();
        
    }
    
    public void closeWithoutClosingUnderlyingStream() throws IOException {
        int overFlow = written % 128;
        if (overFlow > 0) {
            for (int i=overFlow; i<128; i++) {
                write(0);
            }
        }
        super.flush();
    }
    
    public int getWritten() {
        return written;
    }
    
    public void write(OutputStream os) throws IOException {
        writeHeader(os);
        
    }
    
    private void prependHeader() throws IOException {
        if (!headerWritten) {
            headerWritten = true;
            flush();
            writeHeader(out);
            flush();
            
        }
    }
    
    
    // https://code.google.com/archive/p/theunarchiver/wikis/MacBinarySpecs.wiki
    public void writeHeader(OutputStream os) throws IOException {
        
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        OutputStream tmp = os;
        os = buf;
        
        if (fileName.getBytes().length > 63) {
            throw new IOException("FileName too long.  Max 63 chars.");
        }
        if (fileType.getBytes().length > 4) {
            throw new IOException("File type too long.  Max 4 chars");
        }
        if (creator.getBytes().length > 4) {
            throw new IOException("File creator too long.  Max 4 chars");
        }
        //Offset 000-Byte, old version number, must be kept at zero for compatibility
        os.write(0);
        
        //Offset 001-Byte, Length of filename (must be in the range 1-63)
        os.write(fileName.getBytes().length);
        
        //Offset 002-1 to 63 chars, filename (only "length" bytes are significant).
        os.write(fileName.getBytes());
        int padding = 63 - fileName.getBytes().length;
        for (int i=0; i<padding; i++) {
            os.write(0xf1);
        }
        
        //Offset 065-Long Word, file type (normally expressed as four characters)
        // http://livecode.byu.edu/helps/file-creatorcodes.php
        os.write(fileType.getBytes());
        padding = 4 - fileType.getBytes().length;
        for (int i=0; i<padding; i++) {
            os.write((int)' ');
        }
        
        //Offset 069-Long Word, file creator (normally expressed as four characters)
        // http://livecode.byu.edu/helps/file-creatorcodes.php
        os.write(creator.getBytes());
        padding = 4 - creator.getBytes().length;
        for (int i=0; i<padding; i++) {
            os.write((int)' ');
        }
        
        /*
        Offset 073-Byte, original Finder flags
                                 Bit 7 - Locked.
                                 Bit 6 - Invisible.
                                 Bit 5 - Bundle.
                                 Bit 4 - System.
                                 Bit 3 - Bozo.
                                 Bit 2 - Busy.
                                 Bit 1 - Changed.
                                 Bit 0 - Inited.
        */
        os.write(1);
        
        // Offset 074-Byte, zero fill, must be zero for compatibility
        os.write(0);
        
        // Offset 075-Word, file's vertical position within its window.
        os.write(0);
        os.write(0x80);
        
        // Offset 077-Word, file's horizontal position within its window.
        os.write(0);
        os.write(1);
        
        // Offset 079-Word, file's window or folder ID.
        os.write(0);
        os.write(0);
        
        // Offset 081-Byte, "Protected" flag (in low order bit).
        os.write(0);
        
        // Offset 082-Byte, zero fill, must be zero for compatibility
        os.write(0);
        
        // Offset 083-Long Word, Data Fork length (bytes, zero if no Data Fork).
        os.write(0xff & (int)(fileSize >> 24));
        os.write(0xff & (int)(fileSize >> 16));
        os.write(0xff & (int)(fileSize >> 8));
        os.write((int)(0xff & fileSize));
        
        // Offset 087-Long Word, Resource Fork length (bytes, zero if no R.F.).
        os.write(0);
        os.write(0);
        os.write(0);
        os.write(0);
        
        // Offset 091-Long Word, File's creation date
        os.write((int)(0xff & (dateCreated >> 24)));
        os.write((int)(0xff & (dateCreated >> 16)));
        os.write((int)(0xff & (dateCreated >> 8)));
        os.write((int)(0xff & dateCreated));
        
        // Offset 095-Long Word, File's "last modified" date.
        os.write((int)(0xff & (lastModified >> 24)));
        os.write((int)(0xff & (lastModified >> 16)));
        os.write((int)(0xff & (lastModified >> 8)));
        os.write((int)(0xff & lastModified));
        
        // Offset 099-Word, length of Get Info comment to be sent after the resource
        //     fork (if implemented, see below).
        os.write(0);
        os.write(0);
        
        // Offset 101-Byte, Finder Flags, bits 0-7. (Bits 8-15 are already in byte 73)
        os.write(0);
        
        for (int i=0; i<14; i++) {
            os.write(0);
        }
        
        // Offset 116-Long Word, Length of total files when packed files are unpacked.
        //     This is only used by programs that pack and unpack on the fly,
        //     mimicing a standalone utility such as PackIt.  A program that is
        //     uploading a single file must zero this location when sending a
        //     file.  Programs that do not unpack/uncompress files when
        //     downloading may ignore this value.
        os.write(0);
        os.write(0);
        os.write(0);
        os.write(0);
        
        // Offset 120-Word, Length of a secondary header.  If this is non-zero,
        //     Skip this many bytes (rounded up to the next multiple of 128)
        //     This is for future expansion only, when sending files with
        //     MacBinary, this word should be zero.
        os.write(0);
        os.write(0);
        
        // Offset 122-Byte, Version number of Macbinary II that the uploading program
        //     is written for (the version begins at 129)
        os.write(0); //os.write(129);
        
        // Offset 123-Byte, Minimum MacBinary II version needed to read this file
        //     (start this value at 129 129)
        os.write(0); //os.write(129);
        
        // Offset 124-Word, CRC of previous 124 bytes
        
        int crc = crc(buf.toByteArray());
        System.out.println("Writing MacBinaryHeader: "+Arrays.toString(buf.toByteArray())+", crc="+crc);
        os = tmp;
        if (buf.toByteArray().length != 124) {
            throw new RuntimeException("Expected buffer length of 124 but found "+buf.toByteArray().length);
        }
        os.write(buf.toByteArray());
        if (this.crc) {
            os.write(0xff & (crc >> 8));
            os.write(0xff & crc);
        } else {
            // For some reason Novaserver doesn't do the crc
            os.write(0);
            os.write(0);
        }
        
        
        os.write(0);
        os.write(0);
        
        
    }
    
    private static int crc(byte[] bytes) {
        int[] table = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
        };

        int crc = 0x0000;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        return crc;
    }
    
    private void updateFileTypeAndCreator() {
        String ext = "";
        if (fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf(".")+1);
        }
        if (fileType == null || fileType.length() > 4 || fileType.contains("/")) {
            
            if (!ext.isEmpty() && typeMap.containsKey("."+ext)) {
                fileType = typeMap.get("."+ext);
            } else {
                fileType = "????";
            }
            
        }
        
        if (creator == null || creator.length() > 4) {
            if (!ext.isEmpty() && creatorMap.containsKey("."+ext)) {
                creator = creatorMap.get("."+ext);
            } else {
                creator = "????";
            }
            
        }
    }
    
    private static Map<String,String> typeMap = new HashMap<>();
    

    static {
        typeMap.put(".app", "APPL");
        typeMap.put(".anz", "TEXT");
        typeMap.put(".bmp", "BMPf");
        typeMap.put(".cdr", "CDRW");
        typeMap.put(".css", "TEXT");
        typeMap.put(".dmg", "devi");
        typeMap.put(".doc", "WDBN");
        typeMap.put(".eps", "EPSF");
        /*
        'anz' : 'TEXT',  \
	'app' : 'APPL',  \
	'bmp' : 'BMPf',  \
	'cdr' : 'CDRW',  \
	'css' : 'TEXT',  \
	'dmg' : 'devi',  \
	'doc' : 'WDBN',  \
	'eps' : 'EPSF',  \
        */
        typeMap.put(".fhmx", "AGD6");
        typeMap.put(".fh10", "AGD5");
        typeMap.put(".fh9", "AGD4");
        typeMap.put(".fh8", "AGD3");
        typeMap.put(".fh7", "AGD2");
        typeMap.put(".gif", "GIFf");
        typeMap.put(".gz", "GZIP");
        typeMap.put(".htm", "TEXT");
        typeMap.put(".html", "TEXT");
        typeMap.put(".idd", "InDd");
        typeMap.put(".indd", "InDd");
        typeMap.put(".jpg", "JPEG");
        typeMap.put(".jpeg", "JPEG");
        typeMap.put(".p65", "AB65");
        typeMap.put(".pdf", "PDF ");
        typeMap.put(".ps", "EPSF");
        typeMap.put(".psd", "8BPS");
        typeMap.put(".qxd", "XDOC");
        typeMap.put(".rtf", "RTF ");
        typeMap.put(".sit", "SIT5");
        
        /*
	'fhmx' : 'AGD6', 'fh10' : 'AGD5', 'fh9' : 'AGD4',  \
	'fh8' : 'AGD3', 'fh7' : 'AGD2',  \
	'gif' : 'GIFf',  \
	'gz' : 'GZIP',  \
	'htm' : 'TEXT', 'html' : 'TEXT',  \
	'idd' : 'InDd', 'indd' : 'InDd',  \
	'jpg' : 'JPEG', 'jpeg' : 'JPEG',  \
	'p65' : 'AB65',  \
	'pdf' : 'PDF ',  \
	'ps'  : 'EPSF',  \
	'psd' : '8BPS',  \
	'qxd' : 'XDOC',  \
	'rtf' : 'RTF ',  \
        */
        typeMap.put(".sea", "SIT5");
        typeMap.put(".tif", "TIFF");
        typeMap.put(".tiff", "TIFF");
        typeMap.put(".tga", "TPIC");
        typeMap.put(".txt", "TEXT");
        /*
	'sit' : 'SIT5', 'sea' : 'SIT5',  \
	'tif' : 'TIFF', 'tiff' : 'TIFF',  \
	'tga' : 'TPIC',  \
	'txt' : 'TEXT'  \
        */
        
        
    }
    
    private static Map<String,String> creatorMap = new HashMap<>();
    static {
        creatorMap.put(".anz", "ttxt");
        creatorMap.put(".ai", "ART5");
        creatorMap.put(".bmp", "8BIM");
        creatorMap.put(".css", "sfri");
        creatorMap.put(".dmg", "ddsk");
        creatorMap.put(".doc", "MSWD");
        creatorMap.put(".fhmz", "FH11");
        creatorMap.put(".fh10", "FH11");
        creatorMap.put(".fh9", "FH11");
        creatorMap.put(".fh8", "FH11");
        creatorMap.put(".fh7", "FH11");
        creatorMap.put(".fh6", "FH11");
        creatorMap.put(".fh5", "FH11");
        creatorMap.put(".gif", "8BIM");
        creatorMap.put(".html", "sfri");
        creatorMap.put(".htm", "sfri");
        
        /*
        'anz' : 'ttxt',  \
	'ai' : 'ART5',  \
	'bmp' : '8BIM',  \
	'css' : 'sfri',  \
	'dmg' : 'ddsk',  \
	'doc' : 'MSWD',  \
	'fhmx' : 'FH11', 'fh10' : 'FH11', 'fh9' : 'FH11',  \
	'fh8' : 'FH11', 'fh7' : 'FH11', 'fh6' : 'FH11',  \
	'fh5' : 'FH11',  \
	'gif' : '8BIM',  \
	'html' : 'sfri', 'htm' : 'sfri',  \
        */
        creatorMap.put(".idd", "InDn");
        creatorMap.put(".indd", "InDn");
        creatorMap.put(".jpg", "8BIM");
        creatorMap.put(".pdf", "CARO");
        creatorMap.put(".ppt", "PPT3");
        creatorMap.put(".qxb", "XPR3");
        creatorMap.put(".qxd", "XPR3");
        creatorMap.put(".qxl", "XPR3");
        creatorMap.put(".qxt", "XPR3");
        creatorMap.put(".rtf", "MSWD");
        creatorMap.put(".sit", "SITx");
        creatorMap.put(".sea", "SITx");
        creatorMap.put(".sitx", "SITx");
        creatorMap.put(".tif", "8BIM");
        creatorMap.put(".tga", "8BIM");
        creatorMap.put(".xml", "MSIE");
        creatorMap.put(".xsl", "XCEL");
        creatorMap.put(".xtg", "XPR3");
        creatorMap.put(".zip", "SITx");
        /*
	'idd' : 'InDn', 'indd' : 'InDn',  \
	'inc' : 'sfri',  \
	'jpg' : '8BIM',  \
	'pdf' : 'CARO',  \
	'ppt' : 'PPT3',  \
	'qxb' : 'XPR3', 'qxd' : 'XPR3', 'qxl' : 'XPR3', 'qxt' : 'XPR3',  \
	'rtf' : 'MSWD',  \
	'sit' : 'SITx', 'sea' : 'SITx', 'sitx' : 'SITx',  \
	'tif' : '8BIM',  \
	'tga' : '8BIM',  \
	'xml' : 'MSIE',  \
	'xsl' : 'XCEL',  \
	'xtg' : 'XPR3',  \
	'zip' : 'SITx'  \
        */
        
    }
}
