package com.dancingcloudservices.heapdumpreader;

import com.dancingcloudservices.heapdumpreader.utils.Tag;
import com.dancingcloudservices.heapdumpreader.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

public class TaggedRecord {
    private Tag tag;
    private long timeOffset;
    private long dataLength;
    private Record contents;

    public Tag getTag() {
        return tag;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public long getDataLength() {
        return dataLength;
    }

    public Record getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return tag + " at offset " + timeOffset + " length " + dataLength
                + ((contents != null) ? " contents: " + contents : "");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TaggedRecord self = new TaggedRecord();
        private InputStream is;
        private Map<Long, StringRecord> stringRecordMap;
        private Map<Long, ClassRecord> classRecordMap;
        private Map<Long, Long> objectCountMap;

        public Builder source(InputStream is) {
            this.is = is;
            return this;
        }

        public Builder stringMap(Map<Long, StringRecord> stringMap) {
            stringRecordMap = stringMap;
            return this;
        }

        public Builder classMap(Map<Long, ClassRecord> classMap) {
            classRecordMap = classMap;
            return this;
        }

        public Builder objectCountMap(Map<Long, Long> objectCountMap) {
            this.objectCountMap = objectCountMap;
            return this;
        }

        public TaggedRecord build() throws IOException {
            self.tag = Tag.ofID((byte) is.read());
            self.timeOffset = Utils.readU4(is);
            self.dataLength = Utils.readU4(is);
            RecordBuilder rb = self.tag.builder; // find builder from the tag
            if (rb != null) {
                self.contents = rb.build(is, self.dataLength, stringRecordMap, classRecordMap, objectCountMap);
            } else {
                // if no builder in tag, we'll simply skip this record
                is.skip(self.dataLength);
            }
            return self;
        }
    }
}
