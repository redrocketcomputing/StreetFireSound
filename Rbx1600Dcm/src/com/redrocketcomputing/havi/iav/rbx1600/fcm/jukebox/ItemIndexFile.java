/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: ItemIndexFile.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ItemIndexFile
{
  public final static ItemIndex[] EMPTY = new ItemIndex[0];
  public final static int NO_COMPRESSION = 0;
  public final static int GZIP_COMPRESSION = 1;

  private int blockSize;
  private RandomAccessFile blockFile;
  private String version;
  private int compression;
  
  private class InputStreamAdaptor extends InputStream
  {
    private RandomAccessFile randomAccessFile;
    
    public InputStreamAdaptor(RandomAccessFile randomAccessFile)
    {
      this.randomAccessFile = randomAccessFile;
    }

    public int read() throws IOException
    {
      return randomAccessFile.read();
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
      // forward
      return blockFile.read(b, off, len);
    }

    public int read(byte[] b) throws IOException
    {
      // forward
      return read(b, 0, b.length);
    }

    public long skip(long n) throws IOException
    {
      return blockFile.skipBytes((int)n);
    }
  }
  
  private class OutputStreamAdaptor extends OutputStream
  {
    private RandomAccessFile randomAccessFile;
    private int limit;
    private int byteCount = 0;
    
    public OutputStreamAdaptor(RandomAccessFile randomAccessFile, int limit)
    {
      this.randomAccessFile = randomAccessFile;
      this.limit = limit;
    }

    public void flush() throws IOException
    {
      randomAccessFile.getFD().sync();
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException
    {
      if (++byteCount >= limit)
      {
        // Overrun
        throw new IOException("block overrun");
      }
      
      // Forward
      randomAccessFile.write(b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException
    {
      // Check for overrun
      byteCount = byteCount + len;
      if (byteCount >= limit)
      {
        // Overrun
        throw new IOException("block overrun");
      }
        
      // Forward
      randomAccessFile.write(b, off, len);
    }
    
    public void write(byte[] b) throws IOException
    {
      // Forward
      write(b, 0, b.length);
    }
  }

  /**
   * Create an empty ItemIndex block file with the specified header information. If the file exists it will be deleted first.
   * 
   * @param path The full path name to the new file
   * @param blockSize The block size to use for this file
   * @param version The version number of this file
   * @param compression The compression type to use when reading and writing to this file
   * @throws IOException Thrown if a problem is detected when writing the file header
   */
  public static void create(String path, int blockSize, String version, int compression) throws IOException
  {
    // Remove it
    File file = new File(path);
    file.delete();

    // Create the new file
    RandomAccessFile blockFile = new RandomAccessFile(file, "rw");

    // Build file header
    blockFile.writeInt(blockSize);
    blockFile.writeInt(compression);
    blockFile.writeUTF(version);

    // Close the file
    blockFile.close();
  }

  /**
   * Open the specified ItemIndex file.
   * 
   * @param path The full path to this file
   * @throws IOException Thrown if the file does not exist or an error is detected while reading the file header
   */
  public ItemIndexFile(String path) throws IOException
  {
    // Check the patch
    if (path == null)
    {
      throw new IllegalArgumentException("path is null");
    }

    // Open the block file
    blockFile = new RandomAccessFile(path, "rw");

    // Read the block file header
    blockSize = blockFile.readInt();
    compression = blockFile.readInt();
    version = blockFile.readUTF();

    // Seek to first block
    seekBlock(1);
  }

  /**
   * Close the ItemIndexFile and release all resources
   */
  public void close()
  {
    try
    {
      // Ensure it is open
      ensureOpen();

      // Close the block file
      blockFile.close();
    }
    catch (IOException e)
    {
      // Ignore
    }
    finally
    {
      // Release the file
      blockFile = null;
    }
  }
  
  /**
   * Flush any pending writes
   * @throws IOException Thrown if the is a problem flush the file
   */
  public void flush() throws IOException
  {
    // Use sync to flush the file
    blockFile.getFD().sync();
  }

  /**
   * Read an ItemIndex array from the current block
   * @return The ItemIndex array read.  This maybe a zero length array
   * @throws IOException Thrown if there is a problem reading the ItemIndex array
   */
  public ItemIndex[] read() throws IOException
  {
    try
    {
      // Ensure it is open
      ensureOpen();
      
      // Move to block boundary
      long currentPosition = blockFile.getFilePointer();
      if ((currentPosition % blockSize) != 0)
      {
        blockFile.seek(((currentPosition / blockSize) + 1) * blockSize);
      }
      
      // Check for eof
      if (currentPosition >= blockFile.length())
      {
        throw new EOFException();
      }
      
      // Read the block bytes
      InputStream is = new BufferedInputStream(new InputStreamAdaptor(blockFile), blockSize);
      DataInputStream dis = new DataInputStream(is);
      int uncompressedBytes = dis.readInt();
      dis.skipBytes(8);

      // Check for empty block
      if (uncompressedBytes == 0)
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "read", "detected completely empty block at " + getBlockPosition());

        // Return empty index
        return EMPTY;
      }

      // Uncompress the bytes if compressed stream
      if (compression != 0)
      {
        is = new GZIPInputStream(is);
      }

      // Read the buffer uncompressed
      byte[] uncompressedBuffer = new byte[uncompressedBytes];
      int position = 0;
      while (position < uncompressedBytes)
      {
        // Read some byte
        int bytesRead = is.read(uncompressedBuffer, position, uncompressedBytes - position); 
        if (bytesRead == -1)
        {
          // Unexpected EOF
          LoggerSingleton.logError(this.getClass(), "read", "tried to read " + uncompressedBytes + " only read " + bytesRead);
          throw new EOFException("tried to read " + uncompressedBytes + " only read " + bytesRead);
        }
        
        // Update position
        position += bytesRead;
      }

      // Unmarshall the item index
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(uncompressedBuffer);
      ItemIndex[] itemIndex = new ItemIndex[hbais.readInt()];
      for (int i = 0; i < itemIndex.length; i++)
      {
        itemIndex[i] = new ItemIndex(hbais);
      }
      
      // Return the array
      return itemIndex;
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      throw new IOException(e.toString());
    }
  }

  /**
   * Write a ItemIndex array at the current block position
   * @param itemIndex The array to write, this maybe a zero length array
   * @throws IOException Throw if a problem is detected writing the array
   */
  public void write(ItemIndex[] itemIndex) throws IOException
  {
    // Ensure it is open
    ensureOpen();

    try
    {
      // Move to block boundary
      long currentPosition = blockFile.getFilePointer();
      if ((currentPosition % blockSize) != 0)
      {
        blockFile.seek(((currentPosition / blockSize) + 1) * blockSize);
      }

      // Marshall into byte array
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(blockSize);
      hbaos.writeInt(itemIndex.length);
      for (int i = 0; i < itemIndex.length; i++)
      {
        itemIndex[i].marshal(hbaos);
      }
      
      // Write size
      OutputStream os = new BufferedOutputStream(new OutputStreamAdaptor(blockFile, blockSize - 24), 4096);
      DataOutputStream dos = new DataOutputStream(os);
      dos.writeInt(hbaos.size());
      dos.writeInt(0);
      dos.writeInt(0);

      // Compress if specified
      if (compression != 0)
      {
        os = new GZIPOutputStream(os);
      }
      
      // Write the byte array to the output stream
      os.write(hbaos.toByteArray());
      os.close();
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new IOException(e.toString());
    }
  }

  /**
   * Move the file pointer to the requested block number.  This may extend the size of file.
   * @param blockNumber The new block number
   * @throws IOException Thrown if an error is detected or the file is not open.
   */
  public void seekBlock(int blockNumber) throws IOException
  {
    // Ensure it is open
    ensureOpen();

    // Calculate new position
    int newPosition = blockSize * blockNumber;

    // Seek to the correct position
    blockFile.seek(newPosition);
  }

  /**
   * Return current file position in blocks
   * @return The current block number
   * @throws IOException Thrown if an error is detected or the file is not open.
   */
  public int getBlockPosition() throws IOException
  {
    // Ensure it is open
    ensureOpen();

    return (int)(blockFile.getFilePointer() / blockSize);
  }
  
  /**
   * Return the number of blocks in the file, including the header block
   * @return The number file blocks
   * @throws IOException Thrown if an error is detected or the file is not open.
   */
  public int getBlockCount() throws IOException
  {
    // Ensure it is open
    ensureOpen();
    
    // Return the number of block
    long length = blockFile.length();
    return (int)((length / blockSize) + (length % blockSize != 0 ? 1 : 0));
  }
  
  /**
   * @return Returns the blockSize.
   */
  public int getBlockSize()
  {
    return blockSize;
  }
  
  /**
   * @return Returns the compression.
   */
  public int getCompression()
  {
    return compression;
  }
  
  /**
   * @return Returns the version.
   */
  public String getVersion()
  {
    return version;
  }
  
  private final void ensureOpen() throws IOException
  {
    // Make sure the file is opened
    if (blockFile == null)
    {
      throw new IOException("file is closed");
    }
  }
}