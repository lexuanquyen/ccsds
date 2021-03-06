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

import eu.dariolucia.ccsds.tmtc.util.StringUtil;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class FixedLengthChannelReaderTest {


	private static final String FILE_TM1 = "dumpFile_tm_1.hex";

	@Test
	void testReadNext() throws IOException {
		// Prepare the input
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(FILE_TM1)));
		String read;
		while ((read = br.readLine()) != null) {
			if (read.trim().isEmpty()) {
				continue;
			}
			bos.writeBytes(StringUtil.toByteArray(read.toUpperCase()));
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		FixedLengthChannelReader smReader = new FixedLengthChannelReader(bis, 1279);

		byte[] frameWithASMandRS = null;
		int counter = 0;
		while ((frameWithASMandRS = smReader.readNext()) != null) {
			++counter;
			assertEquals(1279, frameWithASMandRS.length);
		}
		smReader.close();

		assertEquals(152, counter);
	}

	@Test
	void testWrongBufferLength() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[20]);

		FixedLengthChannelReader smReader = new FixedLengthChannelReader(bis, 1279);
		try {
			smReader.readNext(new byte[1000], 0, 1000);
			fail("IOException expected");
		} catch (IOException e) {
			// Good
		}
	}

	@Test
	void testShortData() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[20]);

		FixedLengthChannelReader smReader = new FixedLengthChannelReader(bis, 1279);
		try {
			smReader.readNext(new byte[4096], 0, 4096);
			fail("IOException expected");
		} catch (IOException e) {
			// Good
		}
	}

	@Test
	void testShortDataGet() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[20]);

		FixedLengthChannelReader smReader = new FixedLengthChannelReader(bis, 1279);

		byte[] data = smReader.get();
		assertNull(data);
	}

	@Test
	void testNullStream() {
		try {
			FixedLengthChannelReader smReader = new FixedLengthChannelReader(null, 1279);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
			// Good
		}
	}
}