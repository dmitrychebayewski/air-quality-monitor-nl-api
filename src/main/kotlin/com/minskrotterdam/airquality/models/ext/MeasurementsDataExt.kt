package com.minskrotterdam.airquality.models.ext

import com.minskrotterdam.airquality.models.AggregateBy
import com.minskrotterdam.airquality.models.measurements.Data

private fun Data.maxOf(a: Data): Data {
    if (value >= a.value) return this
    return a
}

private fun Data.minOf(a: Data): Data {
    if (value <= a.value) return this
    return a
}

private fun List<Data>.minByComponent(): MutableList<Data> {
    return groupBy { it.formula }.toSortedMap().values.map { it ->
        it.reduce { ac, data ->
            ac.minOf(data)
        }
    }.toMutableList()
}

private fun List<Data>.maxByComponent(): MutableList<Data> {
    return groupBy { it.formula }.toSortedMap().values.map { it ->
        it.reduce { ac, data ->
            ac.maxOf(data)
        }
    }.toMutableList()
}

private fun List<Data>.averageValueByComponent(): MutableList<Data> {
    val reducer = { a: Double, b: Double -> a + b }
    return groupBy { it.formula }.toSortedMap().values.map { it ->
        val size = it.size
        val reduce = it.reduce { ac, data ->
            Data(ac.formula,
                    ac.station_number,
                    ac.timestamp_measured, reducer.invoke(ac.value, data.value))
        }
        reduce.value = "%.1f".format(reduce.value / size).toDouble()
        reduce
    }.toMutableList()
}

fun List<Data>.aggregateBy(by: AggregateBy): MutableList<Data> {
    return when (by) {
        AggregateBy.MAX -> maxByComponent()
        AggregateBy.MIN -> minByComponent()
        else -> averageValueByComponent()
    }
}

