/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.date;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.SymbolTableSource;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.TimestampFunction;
import io.questdb.griffin.engine.functions.constants.TimestampConstant;
import io.questdb.std.Numbers;
import io.questdb.std.ObjList;

public class TimestampSequenceFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "timestamp_sequence(nL)";
    }

    @Override
    public Function newInstance(ObjList<Function> args, int position, CairoConfiguration configuration) {
        final long start = args.getQuick(0).getTimestamp(null);
        if (start == Numbers.LONG_NaN) {
            return new TimestampConstant(args.getQuick(0).getPosition(), Numbers.LONG_NaN);
        }
        return new TimestampSequenceFunction(position, start, args.getQuick(1));
    }

    private static final class TimestampSequenceFunction extends TimestampFunction {
        private final Function longIncrement;
        private final long start;
        private long next;

        public TimestampSequenceFunction(int position, long start, Function longIncrement) {
            super(position);
            this.start = start;
            this.next = start;
            this.longIncrement = longIncrement;
        }

        @Override
        public void close() {
        }

        @Override
        public long getTimestamp(Record rec) {
            final long result = next;
            next += longIncrement.getLong(rec);
            return result;
        }

        @Override
        public void toTop() {
            next = start;
        }

        @Override
        public void init(SymbolTableSource symbolTableSource, SqlExecutionContext executionContext) {
        }
    }
}
