/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.cache.offheap;


import com.gs.fw.common.mithra.extractor.ShortExtractor;
import com.gs.fw.common.mithra.extractor.Extractor;
import com.gs.fw.common.mithra.util.HashUtil;
import com.gs.fw.common.mithra.util.MithraUnsafe;
import sun.misc.Unsafe;

public class OffHeapShortExtractorWithOffset implements OffHeapShortExtractor
{
    private static Unsafe UNSAFE = MithraUnsafe.getUnsafe();
    private final int fieldOffset;
    private final int nullBitsOffset;
    private final int nullBitsPosition;

    public OffHeapShortExtractorWithOffset(int fieldOffset, int nullBitsOffset, int nullBitsPosition)
    {
        this.fieldOffset = fieldOffset;
        this.nullBitsOffset = nullBitsOffset;
        this.nullBitsPosition = nullBitsPosition;
    }

    @Override
    public short shortValueOf(OffHeapDataStorage dataStorage, int dataOffset)
    {
        return dataStorage.getShort(dataOffset, fieldOffset);
    }

    @Override
    public boolean isAttributeNull(OffHeapDataStorage dataStorage, int dataOffset)
    {
        return nullBitsOffset >= 0 && (dataStorage.getInt(dataOffset, nullBitsOffset) & (1 << nullBitsPosition)) != 0;
    }

    @Override
    public int computeHashFromValue(Object key)
    {
        if (key == null)
        {
            return HashUtil.NULL_HASH;
        }
        return HashUtil.hash(((Short)key).shortValue());
    }

    @Override
    public boolean valueEquals(OffHeapDataStorage dataStorage, int dataOffset, long otherDataAddress)
    {
        if (isAttributeNull(dataStorage, dataOffset))
        {
            return (UNSAFE.getInt(otherDataAddress + nullBitsOffset) & (1 << nullBitsPosition)) != 0;
        }
        return shortValueOf(dataStorage, dataOffset) == UNSAFE.getShort(otherDataAddress + fieldOffset);
    }

    @Override
    public boolean valueEquals(OffHeapDataStorage dataStorage, int dataOffset, int secondOffset)
    {
        return isAttributeNull(dataStorage, dataOffset) ? isAttributeNull(dataStorage, secondOffset) :
                shortValueOf(dataStorage, dataOffset) == shortValueOf(dataStorage, secondOffset);
    }

    @Override
    public boolean valueEquals(OffHeapDataStorage dataStorage, int dataOffset, Object key)
    {
        if (key == null)
        {
            return isAttributeNull(dataStorage, dataOffset);
        }
        return shortValueOf(dataStorage, dataOffset) == ((Short)key).shortValue();
    }

    @Override
    public int computeHashFromOnHeapExtractor(Object valueHolder, Extractor onHeapExtractor)
    {
        return onHeapExtractor.valueHashCode(valueHolder);
    }

    @Override
    public boolean equals(OffHeapDataStorage dataStorage, int dataOffset, Object valueHolder, Extractor extractor)
    {
        if (extractor.isAttributeNull(valueHolder))
        {
            return isAttributeNull(dataStorage, dataOffset);
        }
        return shortValueOf(dataStorage, dataOffset) == ((ShortExtractor)extractor).shortValueOf(valueHolder);
    }

    @Override
    public int computeHash(OffHeapDataStorage dataStorage, int dataOffset)
    {
        if (isAttributeNull(dataStorage, dataOffset))
        {
            return HashUtil.NULL_HASH;
        }
        return HashUtil.hash(shortValueOf(dataStorage, dataOffset));
    }

    @Override
    public int intValueOf(OffHeapDataStorage dataStorage, int dataOffset)
    {
        return this.shortValueOf(dataStorage, dataOffset);
    }
}
