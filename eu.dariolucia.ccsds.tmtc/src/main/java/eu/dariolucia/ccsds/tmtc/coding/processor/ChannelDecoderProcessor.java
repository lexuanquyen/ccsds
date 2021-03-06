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

package eu.dariolucia.ccsds.tmtc.coding.processor;

import eu.dariolucia.ccsds.tmtc.coding.ChannelDecoder;
import eu.dariolucia.ccsds.tmtc.util.internal.TransformationProcessor;
import eu.dariolucia.ccsds.tmtc.datalink.pdu.AbstractTransferFrame;

import java.util.concurrent.ExecutorService;

/**
 * This class is used in reactive programming design, as specified in the {@link java.util.concurrent.Flow} specification,
 * as processor. It receives byte[] and transform them into subclass instances of {@link AbstractTransferFrame}, according
 * to the provided {@link eu.dariolucia.ccsds.tmtc.coding.ChannelDecoder}.
 *
 * The provision of the decoder is mandatory.
 *
 * @param <T> the output type
 */
public class ChannelDecoderProcessor<T extends AbstractTransferFrame> extends TransformationProcessor<byte[], T> {

    public ChannelDecoderProcessor(ChannelDecoder<T> decoder, ExecutorService executor, boolean timely) {
        super(decoder, executor, timely);
    }

    public ChannelDecoderProcessor(ChannelDecoder<T> decoder) {
        super(decoder);
    }
}
