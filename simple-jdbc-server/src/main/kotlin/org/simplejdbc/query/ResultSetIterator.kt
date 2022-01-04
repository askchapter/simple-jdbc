package org.simplejdbc.query

import java.io.Closeable
import java.sql.ResultSet
import java.sql.Statement

private data class ResultSetIteratorState<T>(val hasNextValue: Boolean?, val nextValue: T?)

class ResultSetIterator<T>(private val resultSet: ResultSet, private val mapRows: (ResultSet) -> T): Iterator<T>, Closeable {
    private var state: ResultSetIteratorState<T> = ResultSetIteratorState(hasNextValue = null, nextValue = null)

    @Synchronized
    override fun hasNext(): Boolean {
        synchronized(state) {
            if (state.hasNextValue == null) {
                val hasNextValue = resultSet.next()
                var nextValue: T? = null
                if (hasNextValue) {
                    nextValue = mapRows(resultSet)
                }
                state = ResultSetIteratorState(hasNextValue, nextValue)
            }
            return state.hasNextValue as Boolean
        }
    }

    override fun next(): T {
        synchronized(state) {
            val toReturn: T
            if (state.nextValue != null) {
                toReturn = state.nextValue as T
                state = ResultSetIteratorState(hasNextValue = null, nextValue = null)
            } else {
                val more = resultSet.next()
                if (!more) {
                    throw RuntimeException("No rows remaining")
                }
                toReturn = mapRows(resultSet)
            }
            return toReturn
        }
    }

    override fun close() {
        resultSet.close()
    }
}