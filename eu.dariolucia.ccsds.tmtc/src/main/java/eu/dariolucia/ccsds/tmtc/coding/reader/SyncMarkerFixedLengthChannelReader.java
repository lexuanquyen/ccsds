/*
 * Copyright 2018-2019 Dario Lucia (https://www.dariolucia.eu)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.dariolucia.ccsds.tmtc.coding.reader;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is an implementation of the {@link IChannelReader} capable to read transfer units of fixed lengths,
 * preceded by a synchronisation marker. The implementation verifies the presence of the synchronisation marker and,
 * depending on its construction, can try to recover a synchronisation loss or can throw an exception.
 */
public class SyncMarkerFixedLengthChannelReader extends AbstractChannelReader {

    private final int fixedLength;

    private final byte[] syncMarker;

    private final boolean includeStartMarker;

    private final boolean throwExceptionOnSyncLoss;

    public SyncMarkerFixedLengthChannelReader(InputStream stream, byte[] syncMarker, int fixedLength) {
        this(stream, syncMarker, fixedLength, true, false);
    }

    public SyncMarkerFixedLengthChannelReader(InputStream stream, byte[] syncMarker, int fixedLength, boolean includeStartMarker, boolean throwExceptionOnSyncLoss) {
        super(stream);
        this.syncMarker = syncMarker.clone();
        this.fixedLength = fixedLength;
        this.includeStartMarker = includeStartMarker;
        this.throwExceptionOnSyncLoss = throwExceptionOnSyncLoss;
    }

    @Override
    public int readNext(byte[] b, int offset, int maxLength) throws IOException {
        int totalLength = fixedLength + (includeStartMarker ? syncMarker.length : 0);
        if(maxLength < totalLength) {
            throw new IOException("Provided buffer free space " + maxLength + " bytes is less than required " + totalLength + " bytes");
        }
        int smCurrIdx = 0;
        boolean smFound = false;
        int countedBytes = 0;
        // Seeking the sync marker (byte after byte)
        while(!smFound) {
            // Read one byte
            int readByte = stream.read();
            ++countedBytes;
            if(readByte == -1) {
                if(smCurrIdx != 0) {
                    throw new IOException("Stream unexpectedly closed (-1)");
                } else {
                    // No more data
                    return -1;
                }
            }
            // Check if byte is the expected one in the SM location (expected SM index: smCurrIdx)
            if((byte) readByte == syncMarker[smCurrIdx]) {
                // If so, increment smCurrIdx by one, and check if the SM is over
                ++smCurrIdx;
                if(smCurrIdx == syncMarker.length) {
                    // If so, read the fixed length
                    smFound = true;
                }
            } else {
                // If not, repeat or throw exception
                if(throwExceptionOnSyncLoss) {
                    throw new SynchronizationLostException("Synchronization lost: expected " + syncMarker[smCurrIdx] + ", got " + readByte);
                }
                // Reset SM index to zero before retrying
                smCurrIdx = 0;
            }
        }

        int read = 0;
        // Copy the sync marker if required
        if(includeStartMarker) {
            System.arraycopy(syncMarker, 0, b, offset, syncMarker.length);
            read += syncMarker.length;
        }

        // Read the fixed length
        while(read < totalLength) {
            int justRead = stream.read(b, offset + read, totalLength - read);
            read += justRead;
            if(justRead <= 0) {
                throw new IOException("Stream unexpectedly closed: stream read() returned 0 or -1 bytes read");
            }
        }

        // Return the buffer
        return read;
    }

    @Override
    public byte[] readNext() throws IOException {
        byte[] b = new byte[fixedLength + (includeStartMarker ? syncMarker.length : 0)];
        int read = readNext(b, 0, b.length);
        if(read > 0) {
            return b;
        } else {
            return null;
        }
    }

}